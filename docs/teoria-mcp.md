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

## Pattern architetturali
- **Single-tenant MCP**: un processo per dominio (es. meteo, eventi) con tool mirati; semplice da deployare e da limitare.
- **MCP router/aggregatore**: un MCP che inoltra chiamate ad altri MCP (via stdio o REST interno) e aggrega le risposte; utile per risposte composte (es. evento + meteo + spesa).
- **Risorse/Prompt**: oltre ai tool, un MCP può esporre risorse statiche o dinamiche (file, query) e prompt riusabili; aiuta a standardizzare risposte e ridurre prompt injection.
- **Backpressure e limiti**: definire timeouts, `limit`/paginazione e massimi di dimensione per evitare risposte ingestibili al modello.
- **Logging/STDOUT**: STDOUT va riservato al protocollo; log e diagnostica sempre su STDERR o su file separati.

## JSON-RPC e schemi
- **Schema input**: ogni tool deve avere `type`, `properties`, `required`; usare `enum`, `minimum/maximum`, `pattern` per guidare il modello.
- **Errori**: usare codici standard (`-32601` metodo non supportato; `-32602` parametri non validi) o errori personalizzati `-32000` con messaggi chiari.
- **Content**: le risposte spesso sono `content` di tipo `text`; si possono restituire più segmenti (text, markdown, data) secondo le specifiche MCP.

## Sicurezza e robustezza
- Validare input lato server (oltre allo schema) per prevenire costi eccessivi o dati fuori dominio.
- Evitare chiamate esterne nei test; usare mock/stub per servizi upstream.
- Non serializzare segreti nei log; separare configurazioni in env/secret manager.
- Considerare rate limiting o circuit breaker se il server contatta terze parti.

## LLM e comportamento del client
- Il client (Codex/Claude) decide quando usare i tool in base a descrizioni e schemi; descrizioni brevi e chiare aiutano.
- Temperature basse migliorano l'aderenza al formato richiesto; esempi nel prompt utente guidano il modello a scegliere il tool giusto.
- Backend LLM (es. Ollama) è intercambiabile: il contratto MCP (schema/semantica) resta costante.

## Collegamento con questo repository
- Server Java (`mcpServer/serverJava`): Web API + bridge MCP stdio (`initialize`, `tools/list`, `tools/call`, `ping`), log su `stderr`.
- Server Python (`mcpServer/serverPython`): FastMCP per tool asincroni; include `weather` e `eventi-amici`.
- `mcpClient`: config per Codex/Claude via trasporto `stdio`; modelli LLM configurabili (vedi `llm-ollama.md`).

Per approfondire il protocollo: documentazione ufficiale su modelcontextprotocol.io.
