#!/bin/bash
# .claude/hooks/grpc-integration-test.sh
# gRPC 통합 테스트 자동화 스크립트

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOG_DIR="$ROOT_DIR/.claude/logs"
FASTAPI_DIR="$ROOT_DIR/fastApi"
SPRING_DIR="$ROOT_DIR/TossBeneface"
SEED_SQL="$FASTAPI_DIR/sql/seed_test_data.sql"
PROTO_FILE="$FASTAPI_DIR/protos/card_benefit.proto"
FASTAPI_VENV="${GRPC_HOOK_VENV:-$FASTAPI_DIR/.venv}"
GRPC_LOG="$LOG_DIR/grpc_server.log"
GRPC_PID=""
FASTAPI_CONTAINER_WAS_RUNNING=0
DOCKER=()
DOCKER_COMPOSE=()

mkdir -p "$LOG_DIR"

cleanup() {
    if [[ -n "${GRPC_PID}" ]] && kill -0 "${GRPC_PID}" 2>/dev/null; then
        echo "🛑 Shutting down FastAPI gRPC Server..."
        kill "${GRPC_PID}" || true
        wait "${GRPC_PID}" 2>/dev/null || true
    fi
    if [[ "$FASTAPI_CONTAINER_WAS_RUNNING" == "1" && ${#DOCKER_COMPOSE[@]} -gt 0 ]]; then
        if ! wait_for_grpc_port_closed; then
            echo "⚠️ Port 50051 is still busy; skipping fastapi container restore."
            lsof -nP -iTCP:50051 -sTCP:LISTEN || true
            return
        fi
        echo "▶️ Restoring fastapi container..."
        "${DOCKER_COMPOSE[@]}" -f "$ROOT_DIR/docker-compose.local.yml" up -d fastapi >/dev/null || true
    fi
}

trap cleanup EXIT

prepend_path_if_present() {
    local dir="$1"
    if [[ -d "$dir" && ":$PATH:" != *":$dir:"* ]]; then
        export PATH="$dir:$PATH"
    fi
}

python_is_supported() {
    "$1" - <<'PY' >/dev/null 2>&1
import sys
raise SystemExit(0 if sys.version_info >= (3, 10) else 1)
PY
}

resolve_command() {
    local command_name="$1"
    if [[ "$command_name" == /* && -x "$command_name" ]]; then
        printf '%s\n' "$command_name"
        return 0
    fi
    command -v "$command_name" 2>/dev/null || return 1
}

find_base_python() {
    local candidates=(
        "/opt/homebrew/bin/python3.13"
        "/opt/homebrew/bin/python3.12"
        "/opt/homebrew/bin/python3.11"
        "/opt/homebrew/bin/python3.10"
        "python3.13"
        "python3.12"
        "python3.11"
        "python3.10"
        "python3"
        "python"
    )
    local candidate resolved
    for candidate in "${candidates[@]}"; do
        if resolved="$(resolve_command "$candidate")" && python_is_supported "$resolved"; then
            printf '%s\n' "$resolved"
            return 0
        fi
    done
    return 1
}

python_has_hook_deps() {
    "$1" - <<'PY' >/dev/null 2>&1
import importlib.util

required = (
    "psycopg",
    "grpc",
    "grpc_tools",
    "google.protobuf",
    "prometheus_client",
    "maturin",
)
missing = [name for name in required if importlib.util.find_spec(name) is None]
raise SystemExit(1 if missing else 0)
PY
}

select_docker() {
    prepend_path_if_present "/Applications/Docker.app/Contents/Resources/bin"

    if ! DOCKER_BIN="$(command -v docker 2>/dev/null)"; then
        echo "❌ Docker CLI not found. Install Docker or add it to PATH."
        exit 1
    fi
    DOCKER=("$DOCKER_BIN")

    if ! "${DOCKER[@]}" info >/dev/null 2>&1; then
        echo "❌ Docker daemon is not running or not reachable."
        exit 1
    fi

    if "${DOCKER[@]}" compose version >/dev/null 2>&1; then
        DOCKER_COMPOSE=("${DOCKER[@]}" compose)
    elif command -v docker-compose >/dev/null 2>&1; then
        DOCKER_COMPOSE=("$(command -v docker-compose)")
    else
        echo "❌ Docker Compose not found. Install the Docker Compose plugin or docker-compose."
        exit 1
    fi
}

select_python() {
    local candidate base_python
    local env_candidates=(
        "$FASTAPI_VENV/bin/python"
        "$FASTAPI_DIR/fastapi-env/bin/python"
        "$FASTAPI_DIR/fastapi-env/Scripts/python"
    )

    for candidate in "${env_candidates[@]}"; do
        if [[ -x "$candidate" ]] && python_is_supported "$candidate"; then
            PYTHON_BIN="$candidate"
            return 0
        fi
    done

    if ! base_python="$(find_base_python)"; then
        echo "❌ Python 3.10+ not found. Install Python 3.10 or newer for the gRPC hook."
        exit 1
    fi

    echo "🐍 Creating FastAPI hook virtualenv: $FASTAPI_VENV"
    "$base_python" -m venv "$FASTAPI_VENV"
    PYTHON_BIN="$FASTAPI_VENV/bin/python"
}

activate_selected_python() {
    PYTHON_PREFIX="$("$PYTHON_BIN" - <<'PY'
import sys
print(sys.prefix)
PY
)"
    unset CONDA_PREFIX CONDA_DEFAULT_ENV
    export VIRTUAL_ENV="$PYTHON_PREFIX"
    prepend_path_if_present "$PYTHON_PREFIX/bin"
}

ensure_python_deps() {
    if python_has_hook_deps "$PYTHON_BIN" && "$PYTHON_BIN" -m maturin --help >/dev/null 2>&1; then
        return 0
    fi

    echo "📦 Installing hook Python dependencies in: $PYTHON_PREFIX"
    "$PYTHON_BIN" -m ensurepip --upgrade >/dev/null 2>&1 || true
    "$PYTHON_BIN" -m pip install \
        "psycopg[binary]==3.3.3" \
        "grpcio>=1.62.2" \
        "grpcio-tools>=1.62.2" \
        "protobuf>=4.25.1" \
        "prometheus-client==0.20.0" \
        "maturin>=1.13,<2"
}

ensure_proto_stubs() {
    if [[ ! -f "$PROTO_FILE" ]]; then
        echo "❌ Proto file not found: $PROTO_FILE"
        exit 1
    fi

    if [[ ! -f "$FASTAPI_DIR/card_benefit_pb2.py" || ! -f "$FASTAPI_DIR/card_benefit_pb2_grpc.py" || "$PROTO_FILE" -nt "$FASTAPI_DIR/card_benefit_pb2.py" ]]; then
        echo "🔁 Generating Python gRPC stubs..."
        "$PYTHON_BIN" -m grpc_tools.protoc \
            -I "$FASTAPI_DIR/protos" \
            --python_out "$FASTAPI_DIR" \
            --grpc_python_out "$FASTAPI_DIR" \
            "$PROTO_FILE"
    fi
}

grpc_port_is_open() {
    "$PYTHON_BIN" - <<'PY' >/dev/null 2>&1
import socket

with socket.create_connection(("127.0.0.1", 50051), timeout=0.25):
    pass
PY
}

wait_for_grpc_port_closed() {
    local count=0
    local max_retries=20
    while grpc_port_is_open; do
        if [[ $count -ge $max_retries ]]; then
            return 1
        fi
        sleep 0.5
        count=$((count + 1))
    done
    return 0
}

echo "🚀 Starting gRPC Integration Test Suite..."
echo "📁 Root directory: $ROOT_DIR"

prepend_path_if_present "$HOME/.cargo/bin"
select_docker
select_python
activate_selected_python
ensure_python_deps
ensure_proto_stubs

if ! command -v cargo >/dev/null 2>&1; then
    echo "❌ Rust cargo not found. Install Rust with rustup or add cargo to PATH."
    exit 1
fi

echo "🐳 Checking Docker containers..."
"${DOCKER_COMPOSE[@]}" -f "$ROOT_DIR/docker-compose.local.yml" up -d postgres redis

if "${DOCKER[@]}" ps -q -f name=tossbeneface-fastapi | grep -q .; then
    FASTAPI_CONTAINER_WAS_RUNNING=1
    echo "🛑 Stopping fastapi container to free port 50051..."
    "${DOCKER_COMPOSE[@]}" -f "$ROOT_DIR/docker-compose.local.yml" stop fastapi
fi

if grpc_port_is_open; then
    echo "❌ Port 50051 is already in use after stopping the fastapi container."
    lsof -nP -iTCP:50051 -sTCP:LISTEN || true
    exit 1
fi

echo "⏳ Waiting for PostgreSQL to be ready..."
until "${DOCKER[@]}" exec tossbeneface-postgres pg_isready -U toss_beneface_user -d toss_beneface >/dev/null 2>&1; do
    sleep 1
done
echo "✅ Docker infrastructure is ready."

if [[ ! -f "$SEED_SQL" ]]; then
    echo "❌ Seed SQL not found: $SEED_SQL"
    exit 1
fi

echo "🗄️ Seeding test database..."
"${DOCKER[@]}" exec -i tossbeneface-postgres psql -v ON_ERROR_STOP=1 -U toss_beneface_user -d toss_beneface < "$SEED_SQL" >/dev/null
echo "✅ Database seeded."

echo "🦀 Building Rust card_benefit_engine..."
(
    cd "$FASTAPI_DIR/rust/card_benefit_engine"
    PYO3_PYTHON="$PYTHON_BIN" "$PYTHON_BIN" -m maturin develop --release
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
    exec "$PYTHON_BIN" grpc_server.py > "$GRPC_LOG" 2>&1
) &
GRPC_PID=$!

MAX_RETRIES=20
COUNT=0
while ! grpc_port_is_open; do
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
