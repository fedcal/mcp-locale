# Tool MCP: eventi-amici

## create_event(name, date, location, budget=None, notes=None)
- Crea un evento con valuta di default `EVENTS_DEFAULT_CURRENCY` (EUR). Output: conferma con ID evento.

## add_participant(event_id, name, intolerances=None, preferences=None, weight=None)
- Aggiunge partecipante con intolleranze/allergie e preferenze cucina; `weight` per split pesato (default 1.0).

## update_preferences(event_id, participant_id, intolerances=None, preferences=None, weight=None)
- Aggiorna preferenze/peso di un partecipante. Output: conferma o errore se evento/partecipante mancante.

## event_summary(event_id)
- Riepilogo evento (data, luogo, budget, note) e partecipanti (intolleranze, preferenze, peso).

## suggest_restaurants(event_id, limit=None)
- Suggerisce ristoranti compatibili con intolleranze/preferenze. Lista statica filtrata; `limit` default da `EVENTS_SUGGESTION_LIMIT`.

## split_bill(event_id, total_amount, mode="equal"|"weighted")
- Divide il conto: `equal` quote uguali, `weighted` usa il peso per partecipante. Output: quote per nome.

## Note
- Stato in-memory; riavvio azzera dati.
- Logging su STDERR; STDOUT riservato a JSON-RPC.
