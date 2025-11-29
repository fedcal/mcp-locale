# MCP Eventi tra amici

Server MCP per pianificare eventi sociali (cene/uscite), gestire preferenze alimentari e dividere il conto.

## Tool
- `create_event(name, date, location, budget=None, notes=None)`
- `add_participant(event_id, name, intolerances=None, preferences=None, weight=None)`
- `update_preferences(event_id, participant_id, intolerances=None, preferences=None, weight=None)`
- `event_summary(event_id)`
- `suggest_restaurants(event_id, limit=None)`
- `split_bill(event_id, total_amount, mode="equal"|"weighted")`

## Avvio
Con venv attivo in `mcpServer/serverPython/.venv`:
```bash
python mcpServer/serverPython/eventi_main.py          # da root repo
# oppure
cd mcpServer/serverPython && python -m eventi.main
```
Usa `MCP_TRANSPORT` (default `stdio`) per scegliere il trasporto.

## Note
- Persistenza: in-memory di default; se `EVENTS_DB_URL` e' impostata (es. `mysql+pymysql://user:pass@host:3306/mcp`), usa MySQL via SQLAlchemy (tabelle create all'avvio).
- Dati in-memory (reset a ogni riavvio, se DB non configurato).
- Suggerimenti ristoranti basati su lista statica filtrata per intolleranze/preferenze.
- Split spese: `equal` (quote uguali) o `weighted` (pesi per partecipante, default 1.0).
