# TossBeneface Project — Gemini Handoff

이 문서는 Claude Code 세션에서 이어받아 Gemini에서 작업을 계속하기 위한 컨텍스트입니다.

---

## 프로젝트 개요

**TossBeneface** — 카페 결제 시 회원의 카드 혜택을 실시간으로 분석·추천하는 서비스.

| 레이어 | 기술 | 역할 |
|---|---|---|
| API 서버 | Spring Boot (Kotlin) | REST API, Kafka Producer, gRPC Client |
| AI 엔진 | FastAPI (Python) | gRPC Server, Kafka Consumer, 혜택 계산 |
| 계산 엔진 | Rust (PyO3) | 배치 혜택 계산 (Python 대비 16x 빠름) |
| DB | PostgreSQL (Flyway) | 회원/카드/혜택 데이터 |
| 메시징 | Kafka (KRaft) | 비동기 AI 파이프라인 |
| 모니터링 | Prometheus + Grafana | 메트릭 수집 및 시각화 |
| 인프라 | Nginx, Redis, Docker Compose | 로컬 통합 환경 |

**레포지토리:** `https://github.com/tossBeneface/toss-beneface-integrated`  
**브랜치:** `main` (최신 커밋 기준으로 작업)

---

## 오늘 완료된 작업 (2026-04-24)

### 1. 커밋 정리 (미커밋 변경사항 → 4개 커밋)
- `feat(engine)` — Rust PyO3 엔진 강화 + 벤치마크 스크립트
- `chore(infra)` — docker-compose에 Prometheus/Grafana 추가
- `test(spring)` — gRPC 통합 테스트 및 서비스 테스트 보강
- `docs(infra)` — K8s 전략 문서 + GitHub Actions CI

### 2. 벤치마크 실행 및 BENCHMARK.md 수치 채우기
- Rust 휠 빌드 (`maturin build --release`) 및 설치
- 단건(2000 iter): Python이 빠름 — PyO3 JSON 경계 오버헤드 때문
- **배치(500 iter, 100 가게 × 10 카드): Rust 2.62ms vs Python 41.64ms → 15.88x 향상**
- `fastApi/BENCHMARK.md`에 결과 및 면접 발언 가이드 포함

### 3. 인터뷰 서사 스크립트 작성
- `INTERVIEW_SCRIPT.md` — 9개 핵심 질문별 즉시 사용 가능한 스크립트
- 리스크 발언 체크리스트 포함 (MZ, 열정 식음 등 금지 표현 명시)
- 모든 기술 답변에 실제 수치 연결

### 4. K8s Sidecar 아키텍처 다이어그램
- `K8S_MIGRATION_STRATEGY.md` 대폭 보강
- 현재 docker-compose → K8s Sidecar → 독립 Deployment ASCII 다이어그램
- 실제 배포 가능한 Pod YAML (readinessProbe, resource limits 포함)
- Sidecar vs 독립 Deployment 선택 기준 비교표

### 5. OAuth2 SecurityConfig 보안 수정
- **문제:** `/api/orders/**` 전체가 `permitAll` → 인증 없이 주문 데이터 접근 가능했음
- **문제:** `/api/payment/**` 전체가 `permitAll` → 결제 API 인증 우회 가능했음
- **문제:** `http://localhost:8080/...` 풀 URL이 permitAll에 있었으나 Spring Security는 경로만 매칭 → 무의미
- **문제:** OAuth2 failureHandler 없어 실패 시 존재하지 않는 `/login?error`로 리다이렉트
- **수정:** 주문/결제/사용자카드 → `authenticated()` 강제, Toss 브라우저 콜백 3개만 public 유지
- **수정:** failureHandler 추가 → `{redirectUri}?error=oauth2_failed`로 리다이렉트

---

## 현재 상태 (코드 기준)

```
fastApi/
├── benefit_engine.py       — Python 혜택 계산 + Rust 폴백 로직
├── rust/card_benefit_engine/src/lib.rs  — PyO3 Rust 엔진
├── BENCHMARK.md            — 실측 벤치마크 결과 (15.88x speedup)
├── tests/
│   ├── benchmark_benefit_engine.py  — 단건 벤치마크
│   ├── benchmark_batch_engine.py    — 배치 벤치마크
│   └── test_benefit_engine_rust_regression.py  — Rust 회귀 테스트

TossBeneface/src/main/kotlin/com/app/
├── global/config/SecurityConfig.kt    — OAuth2 보안 수정 완료
├── auth/infra/oauth2/
│   ├── CustomOAuth2UserService.kt
│   └── OAuth2SuccessHandler.kt

K8S_MIGRATION_STRATEGY.md  — Sidecar 아키텍처 다이어그램 포함
INTERVIEW_SCRIPT.md         — 면접 발언 스크립트
```

---

## 남은 작업 (다음 세션에서 이어갈 것)

### 우선순위 높음
1. **Grafana 대시보드 실제 구성**
   - `monitoring/grafana/provisioning/` 폴더는 있으나 Kafka Consumer Lag 패널이 비어 있음
   - `docker-compose up`으로 띄운 후 Prometheus → Grafana 데이터소스 연결 확인 필요

2. **gRPC 통합 테스트 안정화**
   - `.claude/hooks/grpc-integration-test.sh`가 있으나 실제 FastAPI gRPC 서버 기동 후 Spring Boot 테스트까지 자동화되는지 재검증 필요

3. **`backup_data.sql` 처리**
   - 현재 `.gitignore`에 의해 추적 안 됨
   - 실제 시드 데이터인지, 민감 데이터인지 확인 후 `seed_test_data.sql`로 교체 또는 삭제

### 우선순위 낮음
4. **OAuth2 `SessionCreationPolicy` 정리**
   - 현재 `IF_REQUIRED` — OAuth2 flow에는 맞지만, JWT 인증 후 세션이 남음
   - 세션 정리 로직 또는 `STATELESS`로 전환 검토

5. **`TOTAL_STRATEGY_PLAN.md` 체크리스트 업데이트**
   - gRPC/PyO3 실습 ✅, 모니터링 ✅ — 파일 내 체크박스가 아직 미체크 상태

---

## 로컬 실행 방법

```bash
# 전체 스택 실행
docker-compose -f docker-compose.local.yml up -d

# Rust 휠 빌드 (FastAPI 벤치마크 전 필요)
cd fastApi
maturin build --manifest-path rust/card_benefit_engine/Cargo.toml --release --interpreter python3
pip3 install --force-reinstall "$(find rust/card_benefit_engine/target/wheels -name '*.whl' | head -1)"

# 벤치마크
python3 tests/benchmark_batch_engine.py --iterations 500 --seed 42 --card-count 10 --store-count 100

# Spring Boot 테스트
cd TossBeneface && ./gradlew test
```

---

## 핵심 수치 (면접 레퍼런스)

| 항목 | 수치 |
|---|---|
| Rust 배치 speedup | **15.88x** (41.64ms → 2.62ms) |
| 배치 규모 | 100 가게 × 10 카드, 500 iter |
| Access Token 만료 | 15분 |
| Refresh Token 만료 | 14일 (Redis 저장) |
| Spring Boot 테스트 | 전체 PASS (2026-04-24 기준) |
