#!/bin/bash
# .claude/hooks/grpc-integration-test.sh
# gRPC 통합 테스트 자동화 스크립트

echo "🚀 Starting gRPC Integration Test Suite..."

# 0. Docker 인프라 (PostgreSQL, Redis) 확인 및 실행
echo "🐳 Checking Docker containers..."
if command -v docker-compose &> /dev/null; then
    docker-compose -f docker-compose.local.yml up -d postgres redis
else
    docker compose -f docker-compose.local.yml up -d postgres redis
fi

# PostgreSQL이 준비될 때까지 대기
echo "⏳ Waiting for PostgreSQL to be ready..."
until docker exec tossbeneface-postgres pg_isready -U toss_beneface_user -d toss_beneface; do
  sleep 1
done
echo "✅ Docker infrastructure is ready."

# 0.1 DB 시드 데이터 삽입
echo "🗄️ Seeding test database..."
docker exec -i tossbeneface-postgres psql -U toss_beneface_user -d toss_beneface < fastApi/sql/seed_test_data.sql
echo "✅ Database seeded."

# 0.2 Rust 엔진 빌드 및 설치 (Maturin)
echo "🦀 Building Rust card_benefit_engine..."
cd fastApi/rust/card_benefit_engine
maturin develop --release
cd ../../..
echo "✅ Rust engine built and installed."

# 1. FastAPI gRPC 서버 백그라운드 실행
echo "🐍 Starting FastAPI gRPC Server (Port 50051)..."
DB_HOST=127.0.0.1 \
DB_PORT=5432 \
DB_USER=toss_beneface_user \
DB_PASSWORD=ai0310 \
DB_NAME=toss_beneface \
python fastApi/grpc_server.py > .claude/logs/grpc_server.log 2>&1 &
GRPC_PID=$!

# 서버가 뜰 때까지 잠시 대기 (최대 10초)
MAX_RETRIES=10
COUNT=0
while ! lsof -i:50051 > /dev/null; do
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "❌ FastAPI gRPC Server failed to start!"
        kill $GRPC_PID
        exit 1
    fi
    echo "⏳ Waiting for gRPC server... ($COUNT/$MAX_RETRIES)"
    sleep 1
    ((COUNT++))
done

echo "✅ FastAPI gRPC Server is UP (PID: $GRPC_PID)"

# 2. Spring Boot gRPC 클라이언트 테스트 실행
echo "☕ Running Spring Boot gRPC Client Test..."
cd TossBeneface
./gradlew test --rerun-tasks --tests "com.app.api.cardbenefit.service.CardBenefitGrpcClientTest"
TEST_RESULT=$?
cd ..

# 3. FastAPI 서버 종료
echo "🛑 Shutting down FastAPI gRPC Server..."
kill $GRPC_PID

# 4. 결과 보고
if [ $TEST_RESULT -eq 0 ]; then
    echo "🎉 gRPC Integration Test PASSED!"
    exit 0
else
    echo "❌ gRPC Integration Test FAILED!"
    exit 1
fi
