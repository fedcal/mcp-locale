# Weather MCP – package notes

Package `weather` esporta l'istanza `mcp` e i tool basati sull'API NWS:
- `get_alerts(state: str)`
- `get_forecast(latitude: float, longitude: float, periods: int | None = None)`

## Componenti interni
- `config.py`: lettura variabili d'ambiente (API base, user-agent, timeout, periodi di forecast).
- `client.py`: HTTP client verso api.weather.gov.
- `service.py`: logica di orchestrazione (coord -> gridpoint -> forecast).
- `formatters.py`: output umano-leggibile per MCP.
- `server.py`: registrazione dei tool MCP.

## Avvio da modulo
```bash
python -m weather.main         # usa MCP_TRANSPORT o stdio
```

Variabili utili: `WEATHER_USER_AGENT`, `WEATHER_HTTP_TIMEOUT`, `WEATHER_FORECAST_PERIODS`, `WEATHER_API_BASE`, `MCP_TRANSPORT`.
