# MCP Eventi tra amici (Python/FastMCP)

Server MCP per pianificare cene/uscite tra amici, gestire intolleranze/preferenze e dividere il conto.

## Tool disponibili
- `create_event(name, date, location, budget=None, notes=None)`: crea evento con valuta di default `EVENTS_DEFAULT_CURRENCY` (default EUR).
- `add_participant(event_id, name, intolerances=None, preferences=None, weight=None)`: aggiunge partecipante con preferenze/pesi per split.
- `update_preferences(event_id, participant_id, intolerances=None, preferences=None, weight=None)`: aggiorna un partecipante esistente.
- `event_summary(event_id)`: riepilogo evento + partecipanti.
- `suggest_restaurants(event_id, limit=None)`: suggerimenti ristoranti compatibili con intolleranze/preferenze (lista statica di esempio).
- `split_bill(event_id, total_amount, mode="equal"|"weighted")`: ripartizione conto equa o pesata.

## Avvio (stdio)
Con venv attivo in `mcpServer/serverPython/.venv`:
```bash
python mcpServer/serverPython/eventi_main.py
```
Variabili utili:
- `MCP_TRANSPORT` (default `stdio`)
- `EVENTS_DEFAULT_CURRENCY` (default `EUR`)
- `EVENTS_SUGGESTION_LIMIT` (default `5`)

## Integrazione Codex/Claude
Config YAML già inclusa in `mcpClient/mcp-servers.yaml` con id `eventi-amici`:
```yaml
  - id: eventi-amici
    transport: stdio
    command: "./mcpServer/serverPython/.venv/bin/python"
    args: ["./mcpServer/serverPython/eventi_main.py"]
    env:
      PYTHONUNBUFFERED: "1"
      MCP_TRANSPORT: "stdio"
```

## Note
- Stato in-memory: riavviare resetta eventi e partecipanti.
- Suggerimenti ristoranti sono placeholder statici; sostituire con API reali (es. Google Places/Yelp) se necessario.
- Modalità split: `equal` quote uguali, `weighted` usa il campo `weight` per partecipante (default 1.0).
