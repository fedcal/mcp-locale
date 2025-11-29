# Guida MCP: weather (Python + Java)

Due implementazioni parallele del server MCP weather:
- **Python (FastMCP)**: tools `get_alerts`, `get_forecast`, trasporto `stdio`.
- **Java (Spring Boot)**: Web API per verifica manuale e bridge MCP su `stdio` (JSON-RPC minimale) con logging su stderr per non contaminare l'output del protocollo.

## Setup server Python
```bash
cd mcpServer/serverPython
python -m venv .venv
source .venv/bin/activate           # oppure .\\.venv\\Scripts\\Activate.ps1 su Windows
pip install --upgrade pip
pip install -r requirements.txt
```
Variabili utili: `WEATHER_API_BASE`, `WEATHER_USER_AGENT`, `WEATHER_HTTP_TIMEOUT`, `WEATHER_FORECAST_PERIODS`, `MCP_TRANSPORT` (default stdio).

Avvio:
```bash
python main.py
# oppure
python -m weather.main
```

## Setup server Java
Build del jar (Java 21):
```bash
cd mcpServer/serverJava
./mvnw -q -DskipTests package
```
Esecuzione HTTP per test manuali:
```bash
java -jar target/demojava-0.0.1-SNAPSHOT.jar
# GET http://localhost:8080/api/weather/alerts/CA
# GET http://localhost:8080/api/weather/forecast?lat=37.77&lon=-122.42&periods=3
```
Esecuzione come server MCP su stdio (default `weather.mcp-stdio-enabled=true`):
```bash
java -jar target/demojava-0.0.1-SNAPSHOT.jar
# il bridge MCP legge da STDIN e risponde in JSON-RPC; i log vanno su STDERR
```
Config override via env: `WEATHER_API_BASE`, `WEATHER_USER_AGENT`, `WEATHER_TIMEOUT`, `WEATHER_FORECAST_PERIODS`, `WEATHER_MCP_STDIO_ENABLED`.

## Configurazione Codex CLI
Il flag `--mcp-config` non è disponibile: registra i server globalmente.

Python (da root repo):
```bash
codex mcp add weather-python -- ./mcpServer/serverPython/.venv/bin/python ./mcpServer/serverPython/main.py --env PYTHONUNBUFFERED=1 --env MCP_TRANSPORT=stdio
```
Java (dopo il `package`):
```bash
codex mcp add weather-java -- java -jar ./mcpServer/serverJava/target/demojava-0.0.1-SNAPSHOT.jar --env WEATHER_MCP_STDIO_ENABLED=true
```
Verifica con `codex mcp list`; rimozione con `codex mcp remove <id>`.

## Configurazione Claude (file JSON MCP)
Esempio con entrambi i server:
```json
{
  "weather-python": {
    "transport": "stdio",
    "command": "/percorso/alla/repo/mcpServer/serverPython/.venv/bin/python",
    "args": ["/percorso/alla/repo/mcpServer/serverPython/main.py"],
    "env": {
      "PYTHONUNBUFFERED": "1",
      "MCP_TRANSPORT": "stdio"
    }
  },
  "weather-java": {
    "transport": "stdio",
    "command": "java",
    "args": [ "-jar", "/percorso/alla/repo/mcpServer/serverJava/target/demojava-0.0.1-SNAPSHOT.jar" ],
    "env": {
      "WEATHER_MCP_STDIO_ENABLED": "true"
    }
  }
}
```
Usa path assoluti e riavvia Claude dopo la modifica.

## Prompt di esempio
- «Chiama `weather-python/get_alerts` per lo stato FL e riassumi le allerte più critiche.»
- «Con `weather-java/get_forecast` ottieni 4 periodi per lat 40.7128, lon -74.0060 e restituisci una tabella compatta.»

## Troubleshooting
- `demojava-0.0.1-SNAPSHOT.jar` mancante: esegui `./mvnw package` in `mcpServer/serverJava`.
- Nessuna risposta MCP dal server Java: verifica che i log vadano su stderr (config logback inclusa) e che STDIN/STDOUT non siano filtrati dal client.
- Per il server Python, i log sono inviati su STDERR da `main.py`; se non li vedi reindirizza l'output del processo.
- Nessun dato meteo: controlla la connettività verso `https://api.weather.gov` o eventuali limitazioni di rete.
- Output troppo lungo: riduci `WEATHER_FORECAST_PERIODS` o passa `periods` all'invocazione del tool.
