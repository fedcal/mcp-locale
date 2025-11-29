# Teoria: Model Context Protocol (MCP)

## Che cos'è
Il Model Context Protocol è uno standard aperto per mettere in comunicazione un client alimentato da un modello linguistico con server che espongono strumenti, dati o prompt predefiniti. L'obiettivo è separare il modello dai servizi esterni tramite un contratto stabile e versionato, riducendo l'accoppiamento e facilitando l'interoperabilità tra implementazioni diverse.

## Obiettivi principali
- Scoperta dinamica di capacità: il client può interrogare un server per conoscere strumenti, risorse e prompt disponibili con relativi metadati e schemi.
- Composizione di contesto: il client usa ciò che il server espone per arricchire i messaggi al modello, evitando di codificare credenziali o logica di accesso direttamente nel prompt.
- Portabilità: server e client possono essere scritti in linguaggi diversi purché rispettino il protocollo, che definisce messaggi JSON-RPC 2.0 e rimane agnostico rispetto al trasporto (tipicamente WebSocket o stdio).
- Sicurezza e governance: permette di isolare l'accesso a dati e side effect dietro interfacce controllate e documentate.

## Componenti
- **Client MCP**: parte che ospita l'interazione con il modello (es. UI desktop, CLI o servizio). Gestisce handshake, scoperta delle capacità e orchestrazione delle chiamate verso il server.
- **Server MCP**: servizio che descrive e implementa capacità invocabili. Può offrire tre categorie principali:
  - **Tools**: azioni eseguibili con parametri descritti via JSON Schema (es. creare un evento, interrogare un'API interna).
  - **Resources**: contenuti leggibili (file, record, output di query) identificati da URI e accompagnati da metadati su formato e scopo.
  - **Prompts**: template riutilizzabili con segnaposto, utili per garantire coerenza e ridurre la ripetizione nei messaggi al modello.
- **Trasporto**: il protocollo definisce i messaggi; il canale può essere WebSocket o stdio. L'importante è mantenere la semantica JSON-RPC (richiesta/risposta e notifiche).

## Flusso di alto livello
1. **Inizializzazione**: client e server scambiano informazioni base (versione, capacità supportate) per stabilire una sessione.
2. **Scoperta**: il client richiede l'elenco di tools, resources e prompts; riceve descrizioni e schemi di input/output.
3. **Invocazione**: il client chiama un tool passando parametri validati rispetto allo schema, oppure legge una resource per popolare il contesto.
4. **Context building**: le risposte del server vengono incorporate nei messaggi al modello, che decide se e come usare strumenti aggiuntivi.
5. **Aggiornamenti e notifiche**: il server può inviare eventi (ad esempio variazioni di stato o nuovi dati) se il canale lo supporta, mantenendo il client sincronizzato.

## Linee guida per progettare un server MCP
- Definire strumenti piccoli e mirati, con input chiari e output prevedibili; preferire operazioni idempotenti quando possibile.
- Descrivere sempre i parametri con JSON Schema completo, includendo tipi, vincoli e descrizioni comprensibili dal modello.
- Limitare la superficie di side effect: i tool che modificano stato esterno dovrebbero essere espliciti e protetti da controlli di autorizzazione.
- Esporre risorse in modo coerente (URI stabili, indicazione del formato dei dati) e paginare elenchi voluminosi per evitare risposte troppo grandi.
- Gestire gli errori con codici e messaggi chiari; gli errori prevedibili dovrebbero aiutare il client a correggere i parametri o a ritentare.
- Tenere separata la configurazione sensibile (token, URL interni) tramite variabili d'ambiente o secret manager, mai hardcoded nei prompt o nel codice.

## Collegamento con questo repository
- Server Java (`mcpServer/serverJava`): Web API + bridge MCP stdio che implementa un sottoinsieme di JSON-RPC (`initialize`, `tools/list`, `tools/call`, `ping`), con log su `stderr` per non contaminare il canale di protocollo.
- Server Python (`mcpServer/serverPython`): usa `FastMCP` per gestire handshake/capabilities e registrare tool asincroni.
- `mcpClient`: config esempi per Codex/Claude che puntano a entrambi i server via trasporto `stdio`.

## Note sul trasporto stdio
- Molti client MCP parlano NDJSON su STDIN/STDOUT; alcuni inviano header `Content-Length`. Il bridge Java gestisce entrambi.
- Qualsiasi log su STDOUT rompe il framing JSON-RPC: per questo logback è configurato su STDERR e il banner Spring è disattivato.
- I tool restituiscono `content` testuale; eventuali risorse/prompts non implementati restituiscono errore `-32601` (metodo non supportato).

Per ulteriori approfondimenti e aggiornamenti del protocollo è consigliato consultare la documentazione ufficiale su modelcontextprotocol.io.
