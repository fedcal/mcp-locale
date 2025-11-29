# Tool MCP: weather

## get_alerts(state: string)
- **Descrizione**: allerte meteo attive per uno stato USA (fonte NWS).
- **Parametri**: `state` (2 lettere, es. "CA").
- **Output**: testo con elenco allerte (evento, area, severita', descrizione, istruzioni) o messaggio se nessuna/errore.
- **Note**: usa `WEATHER_API_BASE` (`https://api.weather.gov`), timeout configurabile (`WEATHER_HTTP_TIMEOUT`).

## get_forecast(latitude: number, longitude: number, periods?: int)
- **Descrizione**: previsioni puntuali per coordinate fornite.
- **Parametri**: `latitude`, `longitude`, `periods` opzionale (default `WEATHER_FORECAST_PERIODS`).
- **Output**: prossimi periodi (nome, temperatura, vento, descrizione), limitati per evitare risposte verbose.
