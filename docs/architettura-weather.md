# Architettura server weather (Python e Java)

## Panoramica
Due implementazioni parallele dello stesso dominio (meteo via api.weather.gov):
- **Python**: FastMCP come runtime MCP, strumenti asincroni, output formattato in testo.
- **Java (Spring Boot)**: layer HTTP + bridge MCP stdio, logging su stderr per convivere con JSON-RPC.

## Stack Python
- `config.py`: lettura variabili d'ambiente (`WEATHER_*`, `MCP_TRANSPORT`).
- `client.py` (`NwsClient`): richieste `httpx` con redirect e timeout; errori modellati con eccezioni specifiche.
- `models.py`: dataclass per allerte, gridpoint, periodi di forecast.
- `service.py`: orchestrazione (`alerts_for_state`, `forecast_for_coordinates`), validazione input e trimming periodi.
- `formatters.py`: output testuale compatto per MCP (separatore `---`).
- `server.py`: istanza `FastMCP`, registrazione tool `get_alerts` e `get_forecast` con gestione errori user-friendly.
- Entry point: `mcpServer/serverPython/main.py` (logging + bootstrap) e `weather/main.py` (avvio diretto dal package).

## Stack Java (Spring Boot)
- `WeatherClientProperties`: proprietà configurabili (`weather.api-base`, `weather.user-agent`, timeout, periodi, `weather.mcp-stdio-enabled`).
- `client/NwsClient`: `WebClient` bloccante verso NWS, parsing `JsonNode`, gestione 404/HTTP/ret rete.
- `model/*`: record per allerte, gridpoint, periodi, bundle forecast.
- `service/WeatherService`: validazione input, fallback periodi da config, orchestrazione dei client.
- `formatter/WeatherFormatter`: output testuale per tool MCP.
- `controller/WeatherController`: endpoint REST `GET /api/weather/alerts/{state}` e `GET /api/weather/forecast?lat=&lon=&periods=`.
- `mcp/McpStdIoRunner`: bridge JSON-RPC (metodi `initialize`, `tools/list`, `tools/call`, `ping`) su stdio; risposte MCP in formato `content[type=text]`.
- `logback-spring.xml`: console su `System.err`; `spring.main.banner-mode=off` per evitare rumore su STDOUT.

## Flusso MCP (bridge Java)
1. Il client MCP apre il processo (`java -jar ...`) e parla NDJSON/JSON-RPC su STDIN/STDOUT.
2. `McpStdIoRunner` risponde a `initialize` (metadati server) e `tools/list` (schemi input).
3. `tools/call` inoltra a `WeatherService`, formatta il testo e restituisce `content` testuale.
4. Qualsiasi log applicativo resta su STDERR grazie alla configurazione logback.

## Considerazioni comuni
- Entrambe le versioni limitano l'output dei periodi via `WEATHER_FORECAST_PERIODS` o parametro `periods`.
- Gli errori di rete o input vengono convertiti in messaggi leggibili dal modello per facilitare il retry.
- User-Agent configurabile per il rispetto delle policy NWS.
