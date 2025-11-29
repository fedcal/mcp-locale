# Architettura generale MCP

Panoramica sull'architettura dei server MCP in questa repo (Python FastMCP, Java Spring Boot) e sui pattern comuni.

## Componenti
- **Server MCP Python**: FastMCP per registrare tool/risorse; logging su STDERR; entrypoint `main.py` o dedicati (es. `eventi_main.py`).
- **Server MCP Java**: Spring Boot + bridge stdio (`McpStdIoRunner`) per parlare JSON-RPC; logback su STDERR; WebClient per HTTP.
- **Client MCP**: Codex/Claude/Ollama come orchestratori; config in `mcpClient/mcp-servers.yaml`.

## Struttura tipica (Python)
- `config.py`: env (`*_API_BASE`, timeout, limiti).
- `client.py`: HTTP/DB, gestione errori dedicata.
- `models.py`: dataclass per DTO.
- `service.py`: logica, validazioni, trimming output.
- `formatters.py`: resa testuale/JSON.
- `server.py`: istanza FastMCP e tool.
- `main.py`/`*_main.py`: logging + `mcp.run`.

## Struttura tipica (Java)
- `config/*Properties.java`: binding configurazione.
- `client/*Client.java`: WebClient/DB con error handling.
- `model/*`: record DTO.
- `service/*`: logica/validazione.
- `formatter/*`: resa testuale per MCP.
- `controller/*` (opzionale): endpoint REST per test manuali.
- `mcp/McpStdIoRunner`: bridge stdio JSON-RPC (`initialize`, `tools/list`, `tools/call`, `ping`).
- `application.properties` + `logback-spring.xml`: default e logging su STDERR.
- **Persistenza**: JPA con H2 di default (per avvio rapido), override a MySQL via `SPRING_DATASOURCE_URL` ecc.; in Python, SQLAlchemy opzionale con `EVENTS_DB_URL`.

## Flusso MCP (stdio/JSON-RPC)
1) Il client avvia il processo MCP e comunica su STDIN/STDOUT (NDJSON/Content-Length).
2) `initialize` espone metadati e capabilities; `tools/list` restituisce gli schema; `tools/call` invoca la logica di dominio.
3) STDOUT riservato al protocollo, STDERR per i log applicativi.

## MCP esistenti
- **weather (Python/Java)**: meteo NWS; tool `get_alerts`, `get_forecast`.
- **eventi-amici (Python)**: pianificazione eventi, preferenze alimentari, suggerimenti ristoranti, split spese.

## Composizione
Vedi `mcp-composizione.md` per orchestrare piu' MCP (router, orchestrazione client). Un router MCP puo' inoltrare chiamate a `weather` e `eventi` per rispondere con un unico output.

## LLM e Ollama
Vedi `llm-ollama.md` per scegliere modelli locali (phi4, llama3.1, mistral-nemo, qwen/deepseek coder) in base al MCP e al dominio.
