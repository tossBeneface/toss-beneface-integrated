# Session Notes

## First Suggestion Next Session

- Before any new implementation, suggest a local runtime smoke check.
- Use this order:
  1. `cd TossBeneface && ./gradlew test`
  2. `bash .claude/hooks/grpc-integration-test.sh`
  3. `docker compose -f docker-compose.local.yml up -d`
  4. Check backend readiness with `docker compose -f docker-compose.local.yml exec -T backend curl -fsS http://localhost:8080/actuator/health/readiness`
  5. Check `http://localhost:8000/health`
  6. Check `http://localhost/health`
- Reason: recent changes touched security, JWT auth flow, gRPC integration, base configuration, monitoring, and repo wiring. Runtime verification comes before additional edits.

## Ongoing Notes

- If the full stack does not come up cleanly, fix the runtime blockers before starting feature work.
- Keep backend 8080 internal by default. Do not require host `localhost:8080` for smoke checks unless a debug override explicitly publishes it.
- Treat OAuth, external API flows, and frontend browser smoke as secondary checks after the backend/FastAPI/nginx health endpoints pass.
