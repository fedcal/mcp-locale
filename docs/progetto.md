# Documentazione di progetto

## Perimetro
Repository dedicato alla sperimentazione del Model Context Protocol con un server Java (Spring Boot 4, Java 21) e un client in una cartella dedicata, con spazio per un futuro server Python.

## Struttura della repository
- `mcpServer/serverJava`: applicazione Spring Boot con sorgenti in `src/main/java/com/server` e test in `src/test/java`. Entry point: `DemojavaApplication.java`.
- `mcpServer/serverPython`: placeholder per un server MCP in Python; aggiungere qui `requirements.txt` e risorse specifiche.
- `mcpClient`: area riservata al client MCP; mantenere le dipendenze specifiche del linguaggio all'interno di questa cartella.
- `docs`: documentazione (indice in `docs/README.md`).

## Comandi rapidi
### Avvio server Java
```bash
cd mcpServer/serverJava
./mvnw spring-boot:run
```

### Test suite Java
```bash
cd mcpServer/serverJava
./mvnw test
```

### Build jar eseguibile
```bash
cd mcpServer/serverJava
./mvnw clean package
```

### Server Python (quando implementato)
```bash
cd mcpServer/serverPython
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

## Convenzioni di sviluppo
- Java: indentazione a 4 spazi, package `com.server.*`, classi in PascalCase, metodi/campi in camelCase. Preferire injection via costruttore e annotazioni Lombok (`@Slf4j`, `@RequiredArgsConstructor`).
- Test: JUnit 5, file `*Tests.java`, usare MockMvc per il layer web ed evitare dipendenze esterne nei test.
- Python (futuro): seguire PEP 8, `snake_case` per moduli e funzioni, configurazioni tramite variabili d'ambiente o `.env` (già ignorato).
- Sicurezza: non committare segreti; usare `application.properties` o variabili d'ambiente per override di configurazione. Evitare build output (`target/`, `.venv`) nel VCS.

## Note operative
- Aggiornare questa documentazione quando cambiano i flussi di build/test o la struttura della repo.
- Mantenere il focus delle PR su un solo ambito (server Java, server Python o client) e documentare comandi di verifica eseguiti.
