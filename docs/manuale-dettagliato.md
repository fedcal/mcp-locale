# Manuale dettagliato MCP weather (Python e Java)

Questa guida raccoglie tutto cio' che serve per installare, configurare e usare i server MCP "weather" nelle due varianti (Python FastMCP e Java Spring Boot) con client Codex o Claude.

## Prerequisiti
- Python 3.12+ e `venv` disponibile.
- JDK 21 e Maven wrapper (`./mvnw`) funzionante.
- Accesso rete verso `https://api.weather.gov`.
- Client MCP: Codex CLI oppure Claude con supporto MCP stdio.

## Struttura di riferimento
- `mcpServer/serverPython`: server MCP in Python (package `weather`).
- `mcpServer/serverJava`: server Spring Boot con bridge MCP stdio.
- `mcpClient`: config di esempio per client MCP (`mcp-servers.yaml`).
- `docs`: documentazione (indice in `docs/README.md`).

## Setup e run: server Python
1) Creazione venv e installazione dipendenze:
```bash
cd mcpServer/serverPython
python -m venv .venv
source .venv/bin/activate          # Windows: .\.venv\Scripts\Activate.ps1
pip install --upgrade pip
pip install -r requirements.txt
```
2) Variabili utili (default tra parentesi):
   - `WEATHER_API_BASE` (`https://api.weather.gov`)
   - `WEATHER_USER_AGENT` (`mcp-weather/1.0`)
   - `WEATHER_HTTP_TIMEOUT` (`30.0`)
   - `WEATHER_FORECAST_PERIODS` (`5`)
   - `MCP_TRANSPORT` (`stdio`)
3) Avvio:
```bash
python main.py            # oppure python -m weather.main
```
Logging va su STDERR; STDOUT e' riservato al protocollo MCP.

## Setup e run: server Java
1) Build (senza test) e jar:
```bash
cd mcpServer/serverJava
./mvnw -q -DskipTests package
```
2) Config principali (`application.properties` o env):
   - `WEATHER_API_BASE`, `WEATHER_USER_AGENT`, `WEATHER_TIMEOUT`, `WEATHER_FORECAST_PERIODS`
   - `WEATHER_MCP_STDIO_ENABLED` (default true) abilita il bridge MCP.
3) Avvio HTTP o MCP (stdout parla MCP, stderr log):
```bash
# HTTP + MCP stdio (default)
java -jar target/demojava-0.0.1-SNAPSHOT.jar

# Endpoints di prova:
# GET http://localhost:8080/api/weather/alerts/CA
# GET http://localhost:8080/api/weather/forecast?lat=37.77&lon=-122.42&periods=3
```

## Registrazione su Codex CLI
La versione attuale non usa `--mcp-config`; registra i server globalmente:
```bash
# Python
codex mcp add weather-python -- ./mcpServer/serverPython/.venv/bin/python ./mcpServer/serverPython/main.py --env PYTHONUNBUFFERED=1 --env MCP_TRANSPORT=stdio

# Java (dopo il package)
codex mcp add weather-java -- java -jar ./mcpServer/serverJava/target/demojava-0.0.1-SNAPSHOT.jar --env WEATHER_MCP_STDIO_ENABLED=true

# Eventi-amici (Python)
codex mcp add eventi-amici -- ./mcpServer/serverPython/.venv/bin/python ./mcpServer/serverPython/eventi_main.py --env PYTHONUNBUFFERED=1 --env MCP_TRANSPORT=stdio

# Client con Ollama (scegli modello adeguato)
codex chat --model ollama::phi4   # es. meteo / spese leggere

codex mcp list         # verifica
codex chat             # usa i tool MCP
codex mcp remove ...   # cleanup
```

## Registrazione su Claude
Inserisci nel file JSON MCP di Claude (path variabile per OS):
```json
{
  "weather-python": {
    "transport": "stdio",
    "command": "/abs/percorso/repo/mcpServer/serverPython/.venv/bin/python",
    "args": ["/abs/percorso/repo/mcpServer/serverPython/main.py"],
    "env": { "PYTHONUNBUFFERED": "1", "MCP_TRANSPORT": "stdio" }
  },
  "weather-java": {
    "transport": "stdio",
    "command": "java",
    "args": ["-jar", "/abs/percorso/repo/mcpServer/serverJava/target/demojava-0.0.1-SNAPSHOT.jar"],
    "env": { "WEATHER_MCP_STDIO_ENABLED": "true" }
  },
  "eventi-amici": {
    "transport": "stdio",
    "command": "/abs/percorso/repo/mcpServer/serverPython/.venv/bin/python",
    "args": ["/abs/percorso/repo/mcpServer/serverPython/eventi_main.py"],
    "env": { "PYTHONUNBUFFERED": "1", "MCP_TRANSPORT": "stdio" }
  }
}
```
Riavvia Claude dopo la modifica.

## Scelta LLM con Ollama
- Meteo: `ollama::phi4`, `ollama::llama3.1:8b` (risposte concise).
- Eventi-amici: `ollama::llama3.1:8b`, `ollama::mistral-nemo` (ragionamento su liste/split).
- Dev/Code (futuri MCP): `ollama::qwen2.5-coder:14b`, `ollama::deepseek-coder:6.7b`.
Imposta il modello in `codex chat --model ollama::<nome>` o con la variabile di default del client; usa temperature basse (0.2–0.4) per aderire ai formati MCP.

## Riferimento tool MCP
- `weather/get_alerts`
  - Parametri: `state` (string, due lettere es. CA).
  - Output: elenco formattato di allerte attive (oppure messaggio se none).
- `weather/get_forecast`
  - Parametri: `latitude` (float), `longitude` (float), `periods` (int opzionale; fallback da config).
  - Output: periodi di forecast limitati per evitare risposte verbose.

## Flussi e note operative
- I log finiscono su STDERR (sia Python sia Java) per evitare contaminazioni del canale stdio MCP.
- I tool in Java implementano un bridge JSON-RPC minimale (`initialize`, `tools/list`, `tools/call`, `ping`).
- In caso di rete assente o coordinate errate, i tool restituiscono messaggi leggibili per guidare il retry.
- Riduci `WEATHER_FORECAST_PERIODS` o passa `periods` quando vuoi risposte piu' corte.

## Troubleshooting rapido
- Jar mancante: riesegui `./mvnw package` in `mcpServer/serverJava`.
- Nessuna risposta MCP: controlla che il processo sia in esecuzione e che i log compaiano su STDERR; STDOUT deve contenere solo JSON-RPC.
- Errori HTTP 4xx/5xx: spesso dovuti a stato non valido o limite rete; riprova con altri parametri o verifica con curl verso `api.weather.gov`.
- Timeout: aumenta `WEATHER_HTTP_TIMEOUT` (Python) o `weather.timeout` (Java).
