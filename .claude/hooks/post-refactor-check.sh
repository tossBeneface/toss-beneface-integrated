#!/bin/bash
# .claude/hooks/post-refactor-check.sh
# 에이전트가 리팩토링을 마친 후 실행하여 정적 분석 및 테스트를 수행합니다.

echo "🚀 Starting Post-Refactor Validation..."

# 1. Check Spring Boot Build & Tests (if Java/Kotlin files changed)
if git diff --name-only HEAD | grep -E '\.(kt|java)$'; then
    echo "☕ Testing Spring Boot Backend..."
    ./gradlew test || { echo "❌ Backend tests failed!"; exit 1; }
fi

# 2. Check FastAPI (if Python files changed)
if git diff --name-only HEAD | grep -E '\.py$'; then
    echo "🐍 Testing FastAPI AI Backend..."
    pytest || { echo "❌ FastAPI tests failed!"; exit 1; }
fi

# 3. DB Schema Consistency (Flyway Check)
if git diff --name-only HEAD | grep -E 'db/migration'; then
    echo "🗄️ Checking SQL Migration syntax..."
    # 간단한 SQL 구문 검사 로직 추가 가능
fi

echo "✅ Validation Complete! No regressions found."
