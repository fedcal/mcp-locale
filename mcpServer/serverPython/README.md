# Server Python (MCP) – weather ed eventi-amici

Server MCP scritti in Python:
- `weather`: strumenti meteo basati su `api.weather.gov`.
- `eventi` (eventi-amici): pianificazione eventi sociali, preferenze alimentari, suggerimenti ristoranti e split spese.

## Setup rapido
```bash
cd mcpServer/serverPython
python3 -m venv .venv
source .venv/bin/activate      # Linux/macOS
# Windows PowerShell: .\\.venv\\Scripts\\Activate.ps1
pip install --upgrade pip
pip install -r requirements.txt
```
Per uscire dall'ambiente: `deactivate`.

## Configurazione
Le variabili d'ambiente più utili:
- `WEATHER_API_BASE` (default `https://api.weather.gov`)
- `WEATHER_USER_AGENT` (default `mcp-weather/1.0`)
- `WEATHER_HTTP_TIMEOUT` (secondi, default `30.0`)
- `WEATHER_FORECAST_PERIODS` (numero di periodi restituiti, default `5`)
- `MCP_TRANSPORT` (default `stdio`, usato dagli entry point `main.py` e `weather/main.py`)

## Avvio
Con venv attivo:
```bash
python main.py                          # usa trasporto MCP_TRANSPORT o stdio
# oppure
python -m weather.main                  # entry point nel package
# eventi-amici
python mcpServer/serverPython/eventi_main.py   # trasporto MCP_TRANSPORT o stdio
```

## Struttura dei tool esposti
- `weather/get_alerts(state)`: allerte meteo attive per uno stato USA.
- `weather/get_forecast(latitude, longitude, periods=None)`: forecast puntuale per coordinate; `periods` opzionale per limitare l'output.
- `eventi/create_event`, `eventi/add_participant`, `eventi/update_preferences`, `eventi/event_summary`, `eventi/suggest_restaurants`, `eventi/split_bill`.

## Note operative
- L'istanza MCP è esportata da `weather/__init__.py` come `mcp` e vive in `weather/server.py`.
- L'API client è in `weather/client.py`; la logica di business in `weather/service.py`; la formattazione in `weather/formatters.py`.
- Tutte le richieste usano header `User-Agent` configurabile e seguono redirect; gli errori vengono restituiti in forma leggibile al modello.
- Il logging configurato in `main.py` scrive su STDERR per non interferire con il trasporto MCP (STDOUT).
- L'MCP eventi usa dati in-memory e suggerimenti ristoranti statici; l'istanza è in `eventi/server.py`, entry `eventi_main.py`.
