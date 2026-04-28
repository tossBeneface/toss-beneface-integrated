# CLAUDE.md — TossBeneface Project Guide

## First Suggestion Next Session
- Before feature work, propose a local runtime smoke check first.
- First action order:
  1. Run `./gradlew test` in `TossBeneface/`
  2. Run `.claude/hooks/grpc-integration-test.sh`
  3. Run `docker compose -f docker-compose.local.yml up -d`
  4. Verify `http://localhost:8080/actuator/health/readiness`, `http://localhost:8000/health`, and `http://localhost/health`
- Reason: this repository recently had broad security, gRPC, config, and repo-structure changes, so local run verification must happen before new edits.

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
