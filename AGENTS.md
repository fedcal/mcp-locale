# Repository Guidelines

## Project Structure & Module Organization
- `mcpServer/serverJava`: Spring Boot 4 app targeting Java 21. Code lives in `src/main/java/com/server`, resources in `src/main/resources`, and tests in `src/test/java`. Entry point: `DemojavaApplication.java`.
- `mcpServer/serverPython`: Placeholder `main.py` for a future Python server. Add `requirements.txt` and virtual environment files here (keep `.venv` out of version control).
- `mcpClient`: Reserved for the client implementation; keep language-specific configs (e.g., package.json, requirements) inside this folder.

## Build, Test, and Development Commands
- Start the Java server locally: `cd mcpServer/serverJava && ./mvnw spring-boot:run` (requires JDK 21).
- Run the Java test suite: `cd mcpServer/serverJava && ./mvnw test` (JUnit 5 via `spring-boot-starter-webmvc-test`).
- Build a runnable jar: `cd mcpServer/serverJava && ./mvnw clean package` (outputs to `target/`).
- Python server (when implemented): `cd mcpServer/serverPython && python -m venv .venv && source .venv/bin/activate && pip install -r requirements.txt && python main.py`.

## Coding Style & Naming Conventions
- Java: 4-space indentation; package prefix `com.server.*`; classes in `PascalCase`, methods/fields in `camelCase`. Prefer constructor injection and Lombok annotations (`@Slf4j`, `@RequiredArgsConstructor`).
- Tests: Mirror package structure, name files `*Tests.java`, and favor clear, behavior-focused test names.
- Python (future): Follow PEP 8, use `snake_case` for modules/functions, and keep configuration in `.env` or environment variables.

## Testing Guidelines
- Keep unit and slice tests alongside Java code under `src/test/java`; use MockMvc for web layers and avoid external calls in tests.
- Add regression tests for new endpoints or utilities before merging; focus on domain logic over boilerplate.
- For Python additions, prefer `pytest`; include fixtures for external dependencies and document how to run them.

## Commit & Pull Request Guidelines
- Use clear, imperative commit messages; Conventional Commit style with module scopes is encouraged (e.g., `feat(serverJava): add calendar endpoint`, `chore(serverPython): init venv layout`).
- PRs should include: purpose/summary, linked issues (if any), how to test (commands run), and screenshots or sample payloads for user-facing changes.
- Keep PRs focused on a single concern (server Java, server Python, or client) to simplify review, and update `README.md` or this guide when workflows change.

## Security & Configuration Tips
- Do not commit secrets; prefer environment variables or a local `.env` (already ignored). For Spring Boot, override settings via `application.properties` or environment without hardcoding credentials.
- Avoid committing build outputs (`target/`, `build/`) or virtual environments; they are already ignored.
