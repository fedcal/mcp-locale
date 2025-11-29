# Documentazione di progetto

## Perimetro
Repository dedicato alla sperimentazione del Model Context Protocol con un server Java (Spring Boot 4, Java 21), un server MCP Python (FastMCP) e un client in cartella dedicata.

## Struttura della repository
- `mcpServer/serverJava`: applicazione Spring Boot con sorgenti in `src/main/java/com/server`, bridge MCP stdio in `mcp/McpStdIoRunner`, test in `src/test/java`. Entry point: `DemojavaApplication.java`.
- `mcpServer/serverPython`: server MCP meteo con pacchetto `weather` (client/service/formatter/server); requirements in `requirements.txt`, entrypoint `main.py`.
- `mcpClient`: area riservata al client MCP; mantenere le dipendenze specifiche del linguaggio all'interno di questa cartella.
- `docs`: documentazione (indice in `docs/README.md`).

## Comandi rapidi
### Avvio server Java (HTTP)
```bash
cd mcpServer/serverJava
./mvnw spring-boot:run
```

### Avvio server Java come MCP stdio (dopo il package)
```bash
cd mcpServer/serverJava
./mvnw -q -DskipTests package
java -jar target/demojava-0.0.1-SNAPSHOT.jar   # log su STDERR, STDOUT riservato a MCP
```

### Test suite Java
```bash
cd mcpServer/serverJava
./mvnw test
```

### Server Python (MCP)
```bash
cd mcpServer/serverPython
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

## Convenzioni di sviluppo
- Java: indentazione a 4 spazi, package `com.server.*`, classi in PascalCase, metodi/campi in camelCase. Preferire injection via costruttore.
- Test: JUnit 5, file `*Tests.java`; usare MockMvc/WebTestClient per il layer web ed evitare dipendenze esterne nei test.
- Python: seguire PEP 8, `snake_case` per moduli e funzioni, configurazioni tramite variabili d'ambiente o `.env` (già ignorato).
- Sicurezza: non committare segreti; usare `application.properties` o variabili d'ambiente per override di configurazione. Evitare build output (`target/`, `.venv`) nel VCS.

## Note operative
- Aggiornare questa documentazione quando cambiano i flussi di build/test o la struttura della repo (inclusi i path usati nelle config MCP).
- Mantenere il focus delle PR su un solo ambito (server Java, server Python o client) e documentare comandi di verifica eseguiti.
