# Composizione tra MCP

Obiettivo: permettere a piu' server MCP di cooperare (es. eventi tra amici che usano meteo, spesa, pagamenti). In questa repo il client (Codex/Claude) e' l'orchestratore, ma e' possibile aggiungere un piccolo layer di composizione.

## Pattern di integrazione
- **Orchestrazione lato client** (semplice): registra piu' server MCP (`weather-python`, `weather-java`, `spese`, `eventi-amici`, ecc.) e usa il client (Codex/Claude) per chiamare i tool necessari nella stessa conversazione. Il modello decide quando usare ciascun tool.
- **Router MCP** (intermedio): creare un MCP "hub" che espone tool proxy e inoltra le chiamate a un altro MCP via stdio. Il router riceve la richiesta, valida i parametri e la ritrasmette al server target (es. da `eventi-amici` verso `weather` per il meteo della location).
- **Servizio condiviso** (avanzato): un MCP principale che importa librerie/SDK degli altri moduli e li chiama come package (es. modulo Python che usa il client Java via REST o viceversa). Richiede stabilire API interne (REST/gRPC) tra i servizi.

## Flusso consigliato (router MCP)
1. **Registrazione**: il client MCP registra sia gli MCP di dominio (es. `weather`, `spese`, `eventi-amici`) sia l'MCP router.
2. **Chiamata tool**: il client invoca un tool del router (es. `plan_dinner_event`). Il router:
   - Valida input.
   - Chiama MCP `weather` per forecast location (stdio).
   - Chiama MCP `eventi-amici` per preferenze e split spese.
   - Aggrega le risposte e restituisce un unico output testuale/JSON.
3. **Output**: risposta unica comprensibile dal modello, con riferimenti a dati provenienti dai vari MCP.

## Skeleton router (Python)
```python
import asyncio, json, subprocess
from mcp.server.fastmcp import FastMCP

mcp = FastMCP("router")

async def call_mcp(command: list[str], payload: dict) -> dict:
    proc = await asyncio.create_subprocess_exec(
        *command, stdin=subprocess.PIPE, stdout=subprocess.PIPE
    )
    req = json.dumps(payload) + "\n"
    out, _ = await proc.communicate(req.encode())
    return json.loads(out.decode())

@mcp.tool()
async def plan_dinner_event(city: str, budget: float) -> str:
    weather = await call_mcp(
        ["./mcpServer/serverPython/.venv/bin/python", "mcpServer/serverPython/main.py"],
        {"jsonrpc": "2.0", "id": 1, "method": "tools/call",
         "params": {"name": "get_forecast", "arguments": {"latitude": 45.0, "longitude": 9.0}}},
    )
    # TODO: chiamare eventi-amici/spese, unire risultati
    return f"Meteo: {weather}"
```
(Nota: questo e' solo un esempio; serve gestione del framing JSON-RPC, header `Content-Length` e riuso del processo per performance.)

## Skeleton router (Java)
- Riutilizza `McpStdIoRunner`: aggiungi un service che lancia un processo (o usa WebClient se l'altro MCP espone REST) e inoltra la richiesta JSON-RPC.
- Per performance, mantenere processi figli avviati e dialogare via pipe; evitare reboot per ogni chiamata.

## Best practice
- **Boundary chiari**: ogni MCP resta responsabile del proprio dominio; il router non duplica logica, ma coordina.
- **Output sintetici**: il router restituisce un riepilogo unico e, se serve, allega i payload grezzi come JSON separato.
- **Errori**: se un MCP fallisce, indicare quale tool e perche' (es. rete/metadati mancanti), lasciando al modello la decisione di proseguire.
- **Logging**: STDOUT solo per JSON-RPC; log su STDERR.
- **Config**: percorsi e comandi degli MCP target in variabili d'ambiente (es. `ROUTER_WEATHER_CMD`, `ROUTER_EVENTI_CMD`).

## Esempio di uso (Codex/Claude)
- Registrare: `weather`, `eventi-amici`, `router`.
- Prompt: «Usa il router per pianificare una cena a Milano domani sera con 4 persone, budget 120€, tenendo conto di intolleranze (glutine, lattosio).»

Questa architettura permette di collegare i MCP esistenti senza modificarli pesantemente e di evolvere il router con logica addizionale (es. caching, fallback).*** End Patch``` JSON to=functions.apply_patch code to=functions.apply_patch code## Note to be excluded: The patch code should only include valid patch content and anything contained in the message should be completely removed.
