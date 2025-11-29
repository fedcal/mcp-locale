# Boilerplate MCP (Python e Java)

Template rapido per creare un nuovo server MCP coerente con la repo. Usa questi scheletri come base per ciascun MCP (es. agenda, spese, eventi tra amici, ecc.).

## Struttura consigliata
- `mcpServer/serverPython/<nome>/`
  - `config.py`: lettura variabili d'ambiente (API base, timeout, ecc.).
  - `client.py`: chiamate HTTP/DB, gestione errori specifici.
  - `service.py`: logica di business e validazione input.
  - `formatters.py`: output compatto per MCP.
  - `server.py`: registrazione tool con FastMCP.
  - `main.py`: entry point (usa `MCP_TRANSPORT`), logging su STDERR.
- `mcpServer/serverJava/src/main/java/com/server/<nome>/`
  - `config/`: proprietà e binding.
  - `client/`: chiamate WebClient/DB.
  - `model/`: record DTO.
  - `service/`: logica e validazioni.
  - `formatter/`: output per MCP.
  - `mcp/`: bridge stdio (`initialize`, `tools/list`, `tools/call`, `ping`).
  - `controller/` (opzionale): endpoint REST per debug/manual test.
  - `application.properties`: default config; log su STDERR (logback).

## Python (FastMCP) – scheletro
```python
# server.py
from typing import Annotated
from mcp.server.fastmcp import FastMCP

mcp = FastMCP("nome-mcp")

@mcp.tool()
async def example_tool(
    param: Annotated[str, "Descrizione"],
) -> str:
    # TODO: chiama service, gestisci errori, restituisci testo/JSON compatto
    return "risultato"
```

```python
# main.py
import os, sys, logging
from nome_mcp import mcp

def main(transport: str | None = None) -> None:
    logging.basicConfig(level=logging.INFO, stream=sys.stderr)
    selected = transport or os.getenv("MCP_TRANSPORT", "stdio")
    mcp.run(transport=selected)

if __name__ == "__main__":
    main()
```

## Java (Spring Boot + stdio bridge) – scheletro
```java
// mcp/MyMcpStdIoRunner.java
@Component
@ConditionalOnProperty(name = "mcp.stdio-enabled", havingValue = "true", matchIfMissing = true)
public class MyMcpStdIoRunner implements CommandLineRunner {
    private final ObjectMapper mapper;
    // service, formatter ...

    public void run(String... args) { /* loop stdio */ }
    // handle initialize, tools/list, tools/call
}
```

```java
// service/MyService.java
@Service
public class MyService {
    public String doSomething(String param) {
        // TODO: business logic + validazione
        return "result";
    }
}
```

```java
// formatter/MyFormatter.java
@Component
public class MyFormatter {
    public String formatResult(Object dto) {
        return dto.toString();
    }
}
```

`application.properties` (estratto):
```properties
spring.main.banner-mode=off
mcp.stdio-enabled=true
mcp.api-base=https://example/api
mcp.timeout=30s
```

## JSON Schema per tool
Descrivi sempre gli input nel bridge MCP Java o nei decorator FastMCP:
- Tipi: `string`, `number`, `integer`, `boolean`, `array`, `object`.
- Aggiungi `description`, `minimum/maximum`, `enum` se applicabile.
- Limita output (paginazione o `limit`) per evitare risposte verbose.

## Stato/Testing
- Unit/slice per `service` e `client` con mock API.
- Golden output per `formatter`.
- End-to-end leggero: chiamata tool con parametri validi + path di errore.

## Naming e convenzioni
- ID MCP: kebab-case breve (es. `agenda-mcp`, `spese-mcp`, `eventi-amici`).
- Tool: `verb_object` chiari (`create_event`, `split_bill`).
- Log su STDERR; STDOUT riservato a JSON-RPC.
