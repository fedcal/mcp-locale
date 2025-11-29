# Documentazione

Questa cartella raccoglie la documentazione del progetto e una panoramica teorica sul Model Context Protocol (MCP).

- `progetto.md`: istruzioni operative, struttura della repo e convenzioni di sviluppo.
- `teoria-mcp.md`: concetti chiave del protocollo MCP, lessico e flussi di alto livello.
- `weather-mcp.md`: setup del server weather (Python e Java) e collegamento a client (Codex CLI, Claude).
- `architettura-sistema.md`: architettura generale dei server MCP (Python/Java), pattern stdio/JSON-RPC.
- `manuale-dettagliato.md`: guida operativa completa (setup, registrazione client, troubleshooting).
- `idee-mcp.md`: catalogo di server MCP futuri divisi per categoria (vita quotidiana, sviluppo).
- `idee-mcp-tracker.md`: tabella roadmap con stato di avanzamento e stack suggeriti per ogni MCP.
- `boilerplate-mcp.md`: scheletro per creare nuovi server MCP (Python FastMCP, Java Spring Boot + stdio).
- `mcp-composizione.md`: strategie per far cooperare piu' server MCP (orchestrazione client, router, servizi condivisi).
- `eventi-mcp.md`: guida al server MCP per eventi tra amici (preferenze alimentari, suggerimenti, split spese).
- `llm-ollama.md`: linee guida per usare Ollama e scegliere il modello LLM piu' adatto per ogni MCP.

Aggiorna questi file man mano che evolvono il server Java, il client o eventuali estensioni.
