<!-- AGENTS.md 와 CLAUDE.md 는 동일하게 유지됩니다. 한쪽을 수정하면 다른 쪽도 같이 수정하세요. -->
# TossBeneface Project Guide (CLAUDE.md = AGENTS.md)

## First Suggestion Next Session
- Before any new implementation / feature work, propose a local runtime smoke check first.
- First action order:
  1. `cd TossBeneface && ./gradlew test`
  2. `bash .claude/hooks/grpc-integration-test.sh`
  3. `docker compose -f docker-compose.local.yml up -d`
  4. Verify backend readiness: `docker compose -f docker-compose.local.yml exec -T backend curl -fsS http://localhost:8080/actuator/health/readiness`
  5. Verify `http://localhost:8000/health`
  6. Verify `http://localhost/health`
- Reason: recent changes touched security, JWT auth flow, gRPC integration, base configuration, monitoring, and repo wiring. Runtime verification comes before additional edits.

## 🛠 Build & Test Commands
- **Spring Boot (Java/Kotlin):** `./gradlew build`, `./gradlew test`
- **FastAPI (Python):** `pytest`, `uvicorn main:app --reload`
- **Frontend (React):** `npm run build`, `npm test`
- **Full Stack (Docker):** `docker-compose up -d`

## 🎨 Coding Standards
### Backend (Spring Boot)
- **Language:** Prefer Kotlin for new features, maintain Java for existing code.
- **Architecture:** Layered Architecture (Controller -> Service -> Repository).
- **Naming:** CamelCase for classes/methods, PascalCase for Types.
- **Patterns:** Use DTOs for API requests/responses. No domain entities in controllers.
- **Error Handling:** Use `@RestControllerAdvice` and custom exceptions.

### AI Backend (FastAPI)
- **Standards:** Type hints are mandatory. Use Pydantic models for validation.
- **Async:** Use `async/await` for all I/O bound operations (DB, Kafka, AI calls).
- **AI Specific:** Wrap LLM calls with fallback logic and robust JSON parsing.

### Common
- **Database:** PostgreSQL (use migrations via Flyway). Avoid raw SQL where JPA/QueryDSL is possible.
- **Messaging:** Kafka for asynchronous tasks. Ensure partition keys (e.g., `memberId`, `cafeId`) are used.
- **Tests:** All new logic must have unit tests. Use MockK (Kotlin) or Mockito (Java).

## 🤖 Agent Instructions (Hooks & Patterns)
- **Refactoring:** Before modifying complex logic, run `grep_search` to find all usages.
- **Validation:** After any code change, attempt to run relevant tests (e.g., `./gradlew test --tests *YourClass*`).
- **Integration Test:** After gRPC changes, run `.claude/hooks/grpc-integration-test.sh` to verify Java-Python communication.
- **Hooks:** Refer to `.claude/hooks/` for automated pre/post processing scripts.

## Ongoing Notes
- If the full stack does not come up cleanly, fix the runtime blockers before starting feature work.
- Keep backend 8080 internal by default. Do not require host `localhost:8080` for smoke checks unless a debug override explicitly publishes it.
- Treat OAuth, external API flows, and frontend browser smoke as secondary checks after the backend/FastAPI/nginx health endpoints pass.
