#!/bin/bash
# .claude/hooks/grpc-integration-test.sh
# gRPC 통합 테스트 자동화 스크립트

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOG_DIR="$ROOT_DIR/.claude/logs"
FASTAPI_DIR="$ROOT_DIR/fastApi"
SPRING_DIR="$ROOT_DIR/TossBeneface"
SEED_SQL="$FASTAPI_DIR/sql/seed_test_data.sql"
GRPC_LOG="$LOG_DIR/grpc_server.log"
GRPC_PID=""

mkdir -p "$LOG_DIR"

cleanup() {
    if [[ -n "${GRPC_PID}" ]] && kill -0 "${GRPC_PID}" 2>/dev/null; then
        echo "🛑 Shutting down FastAPI gRPC Server..."
        kill "${GRPC_PID}" || true
        wait "${GRPC_PID}" 2>/dev/null || true
    fi
}

trap cleanup EXIT

if command -v docker-compose >/dev/null 2>&1; then
    DOCKER_COMPOSE=(docker-compose)
else
    DOCKER_COMPOSE=(docker compose)
fi

PYTHON_CANDIDATES=()
if [[ -x "$FASTAPI_DIR/fastapi-env/bin/python" ]]; then
    PYTHON_CANDIDATES+=("$FASTAPI_DIR/fastapi-env/bin/python")
fi
if [[ -x "$FASTAPI_DIR/fastapi-env/Scripts/python" ]]; then
    PYTHON_CANDIDATES+=("$FASTAPI_DIR/fastapi-env/Scripts/python")
fi
if command -v python >/dev/null 2>&1; then
    PYTHON_CANDIDATES+=("$(command -v python)")
fi
if command -v python3 >/dev/null 2>&1; then
    PYTHON_CANDIDATES+=("$(command -v python3)")
fi

PYTHON_BIN=""
for candidate in "${PYTHON_CANDIDATES[@]}"; do
    if "$candidate" - <<'PY' >/dev/null 2>&1
import importlib.util
required = ("psycopg", "grpc")
missing = [name for name in required if importlib.util.find_spec(name) is None]
raise SystemExit(0 if not missing else 1)
PY
    then
        PYTHON_BIN="$candidate"
        break
    fi
done

if [[ -z "$PYTHON_BIN" ]]; then
    echo "❌ No Python interpreter with required modules (psycopg, grpc) was found."
    exit 1
fi

if "$PYTHON_BIN" -m maturin --help >/dev/null 2>&1; then
    MATURIN_CMD=("$PYTHON_BIN" -m maturin)
elif command -v maturin >/dev/null 2>&1; then
    MATURIN_CMD=("$(command -v maturin)")
else
    echo "❌ maturin not found. Install it in the FastAPI environment or on PATH."
    exit 1
fi

echo "🚀 Starting gRPC Integration Test Suite..."
echo "📁 Root directory: $ROOT_DIR"

echo "🐳 Checking Docker containers..."
"${DOCKER_COMPOSE[@]}" -f "$ROOT_DIR/docker-compose.local.yml" up -d postgres redis

if docker ps -q -f name=tossbeneface-fastapi | grep -q .; then
    echo "🛑 Stopping fastapi container to free port 50051..."
    "${DOCKER_COMPOSE[@]}" -f "$ROOT_DIR/docker-compose.local.yml" stop fastapi
fi

echo "⏳ Waiting for PostgreSQL to be ready..."
until docker exec tossbeneface-postgres pg_isready -U toss_beneface_user -d toss_beneface >/dev/null 2>&1; do
    sleep 1
done
echo "✅ Docker infrastructure is ready."

if [[ ! -f "$SEED_SQL" ]]; then
    echo "❌ Seed SQL not found: $SEED_SQL"
    exit 1
fi

echo "🗄️ Seeding test database..."
docker exec -i tossbeneface-postgres psql -v ON_ERROR_STOP=1 -U toss_beneface_user -d toss_beneface < "$SEED_SQL" >/dev/null
echo "✅ Database seeded."

echo "🦀 Building Rust card_benefit_engine..."
(
    cd "$FASTAPI_DIR/rust/card_benefit_engine"
    "${MATURIN_CMD[@]}" develop --release
)
echo "✅ Rust engine built and installed."

echo "🐍 Starting FastAPI gRPC Server (Port 50051)..."
(
    cd "$FASTAPI_DIR"
    DB_HOST=127.0.0.1 \
    DB_PORT=5432 \
    DB_USER=toss_beneface_user \
    DB_PASSWORD=ai0310 \
    DB_NAME=toss_beneface \
    "$PYTHON_BIN" grpc_server.py > "$GRPC_LOG" 2>&1
) &
GRPC_PID=$!

MAX_RETRIES=20
COUNT=0
while ! lsof -i :50051 >/dev/null 2>&1; do
    if [[ $COUNT -ge $MAX_RETRIES ]]; then
        echo "❌ FastAPI gRPC Server failed to start!"
        cat "$GRPC_LOG"
        exit 1
    fi
    if ! kill -0 "$GRPC_PID" 2>/dev/null; then
        echo "❌ FastAPI gRPC Server process died unexpectedly!"
        cat "$GRPC_LOG"
        exit 1
    fi
    echo "⏳ Waiting for gRPC server... ($COUNT/$MAX_RETRIES)"
    sleep 1
    COUNT=$((COUNT + 1))
done

echo "✅ FastAPI gRPC Server is UP (PID: $GRPC_PID)"

echo "☕ Running Spring Boot gRPC Client Test..."
(
    cd "$SPRING_DIR"
    ./gradlew test --rerun-tasks --tests "com.app.api.cardbenefit.service.CardBenefitGrpcClientTest"
)

echo "🎉 gRPC Integration Test PASSED!"
