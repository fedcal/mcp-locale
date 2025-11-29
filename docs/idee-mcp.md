# Idee MCP per implementazioni future

Catalogo di server MCP suddiviso per categoria, con suggerimenti per stack Java/Python, fonti dati e funzionalita' chiave. Le idee sono pensate per l'uso quotidiano e per lo sviluppo software.

## Vita quotidiana
- **Agenda & Promemoria**: CRUD eventi, TODO, reminder multicanale (email/Telegram); integrazione ICS/Google/Microsoft; tool: `list_events`, `add_event`, `remind`.
- **Spese & Budget**: registrazione spese, categorie, limiti mensili, export CSV; eventuale OCR scontrini. Tool: `add_expense`, `monthly_report`, `set_budget`.
- **Lista della spesa / Pantry**: gestisci dispensa e lista condivisa; suggerisci ricette da ingredienti; output in testo/CSV. Tool: `list_items`, `add_item`, `suggest_recipes`.
- **Trasporti & Traffico**: ETA casa-lavoro, orari mezzi, traffico live; salvare tragitti frequenti. Tool: `commute_eta(origin, destination)`, `list_transit(station)`.
- **Meteo esteso**: oltre NWS, integrazione MeteoAM/Open-Meteo; alert pioggia/vento/UV. Tool: `get_alerts`, `get_forecast`, `subscribe_alerts`.
- **Casa & Manutenzione**: scadenze (caldaia, auto), checklist stagionali, log interventi. Tool: `list_maintenance`, `add_task`, `due_soon`.
- **Salute & Benessere**: tracker acqua/passi/sonno (API wearable), promemoria farmaci. Tool: `log_metric`, `summary`, `medication_reminder`.
- **Ricette & Meal Planning**: planner settimanale, lista ingredienti, sostituzioni; suggerimenti porzioni. Tool: `plan_week`, `shopping_list`, `swap_ingredient`.
- **Email & Notifiche smart**: filtri regole (es. “bollette”, “pacchi”), riepilogo giornaliero, snooze. Tool: `summarize_inbox`, `apply_rules`, `snooze`.
- **Note & Snippet**: blocco note con tagging/ricerca; snippet codice/link. Tool: `add_note`, `search_notes`, `list_tags`.
- **Domotica (bridge)**: controlli Home Assistant/MQTT; scene e preset. Tool: `toggle_device`, `set_scene`, `sensor_snapshot`.
- **Documenti & Scadenze**: reminder bollo/assicurazione, parsing PDF via regex/OCR locale. Tool: `add_document`, `extract_dates`, `upcoming_deadlines`.
- **Valute & Tassi**: conversioni live, alert soglie, storici brevi. Tool: `convert(amount, from, to)`, `rate_alert`.
- **Pacchi & Tracking**: tracking corrieri (API pubbliche/dedicate), timeline consegne. Tool: `track_package`, `carrier_status`.
- **Pulizie & Routine**: scheduler attivita', turni, stato fatto/da fare. Tool: `assign_task`, `status`, `next_tasks`.
- **Password check locale**: verifica password deboli/riusate, generatore sicuro (senza vault). Tool: `check_password`, `generate_password`.
- **Viaggi brevi**: planner weekend, meteo destinazione, packing list per attivita'. Tool: `plan_trip`, `packing_list`, `poi_suggestions`.
- **Scuola/Famiglia**: orari lezioni/attivita', promemoria compiti, check-in/out. Tool: `daily_schedule`, `homework`, `notify_guardian`.

## Sviluppo software
- **Repo & Git assistant**: lista branch, diff, patch apply, changelog; tool: `list_branches`, `show_diff`, `apply_patch`.
- **Issue tracker bridge**: creare/chiudere issue, aggiungere label/commenti; integrazione GitHub/GitLab/Jira. Tool: `create_issue`, `comment_issue`, `list_issue`.
- **Doc & README generator**: riassunto di cartelle/file, skeleton README, aggiornamento changelog. Tool: `summarize_dir`, `generate_readme`, `update_changelog`.
- **Linters & formatter as tool**: eseguire eslint/black/spotless e restituire output; tool: `run_lint`, `format_preview`.
- **Schema & DB helper**: introspezione schema (SQL/NoSQL), query safe (read-only), explain plan. Tool: `list_tables`, `describe_table`, `run_query_readonly`.
- **Dependency audit**: elenchi licenze, CVE note, versioni consigliate. Tool: `scan_deps`, `suggest_updates`.
- **Logs & observability**: tail/grep log, estrazione errori ricorrenti, grafici semplici (min/max/avg). Tool: `tail_logs`, `find_errors`, `metrics_snapshot`.
- **Build & CI helper**: lanciare build/test locali con preset, riassunto risultati. Tool: `run_tests(profile)`, `build_artifact`.
- **Env & secrets hygiene**: validazione `.env`, chiavi mancanti, valori dummy. Tool: `validate_env`, `mask_secrets`.
- **Prompt library**: raccolta prompt riusabili (per LLM o chatbot interni) con parametri. Tool: `list_prompts`, `render_prompt`.
- **API inspector**: test endpoint con schemi (OpenAPI/GraphQL), restituzione sample request/response. Tool: `list_endpoints`, `try_request`.
- **Code search/impact**: ricerca per simboli e stima impatto su file correlati. Tool: `search_symbol`, `impact_map`.
- **Release notes**: genera note da commit/PR, grouping per scope. Tool: `release_notes(from, to)`.

## Note di implementazione
- **Java**: Spring Boot 4, WebClient per HTTP, record per DTO, Lombok opzionale; MCP bridge su stdio come in `weather` Java (metodi `initialize`, `tools/list`, `tools/call`, `ping`).
- **Python**: FastMCP per tool/risorse; `httpx`/`aiohttp` per HTTP; dataclass o Pydantic per schemi; logging su STDERR per stdio.
- **Input/Output**: descrivere sempre parametri in JSON Schema; limitare output (paginazione/trimming) per evitare risposte verbose.
- **Test**: preferire test unit/slice per service/client; mock di API esterne; golden output per formatter; integrare check offline dove possibile.
