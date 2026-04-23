# 🧠 TossBeneface Project Status (Current Session)

## 🎯 Current Milestone: Full gRPC + Rust + DB Integration Verified
- **Spring Boot (Kotlin) ↔ FastAPI (Python)** 간의 gRPC 통신 및 실제 DB 연동 완료.
- **Protocol Buffer:** `CardBenefitService` 정의 (`card_benefit.proto`).
- **Implementation:**
  - FastAPI: `grpc_server.py`가 실제 `benefit_engine.py`를 호출하며, PostgreSQL에서 데이터를 조회함.
  - PostgreSQL Query: `benefit_repository.py`에서 `user_data_test + card + card_benefit`를 실제 조회하여 member별 카드 혜택 입력을 구성함.
  - Rust: `fastApi/rust/card_benefit_engine/`에 PyO3 + maturin 기반 실제 할인 계산 로직이 통합 테스트 시점에 매번 빌드/설치됨.
  - Integration: `grpc-integration-test.sh` 훅에서 DB 시딩(`seed_test_data.sql`) 및 Rust 빌드 후 gRPC 서버 실행 루프가 완성됨.
- **Verification:**
  - Spring Boot `CardBenefitGrpcClientTest`가 실제 DB/Rust 엔진 경로를 타서 PASS됨을 확인 (로그: `AnalyzeBestBenefit served by Rust engine`).
  - `member_id=1`, `store_name=스타벅스` 요청에 대해 DB의 2개 후보군(50% 할인, 20% 할인)을 Rust가 계산하여 최적 혜택 반환 성공.

## ⚠️ Last Known Issues & Fixes
- **DB Relation Missing:** 통합 테스트용 Postgres에 테이블이 없던 문제를 `seed_test_data.sql` 및 훅 스크립트 개선으로 해결.
- **Rust Toolchain:** `maturin develop`를 훅 스크립트에 포함하여 런타임에 Rust 모듈을 확실히 설치하도록 보강.
- **Dependency Conflict:** `grpc-netty-shaded`를 강제하여 Spring Boot와 gRPC 간의 Netty 버전 충돌 해결 완료.


## 🚀 Next Steps (Priority)
1. **Benchmarking:** Python evaluator vs Rust 엔진 성능 비교 스크립트 추가.
2. **Seed/Test Data:** member/card/card_benefit 시드 데이터를 추가해 실제 DB 기반 gRPC 통합 테스트도 PASS 하도록 보강.
3. **Observability:** Prometheus/Grafana 인프라를 `docker-compose.local.yml`에 추가하여 메트릭 수집.
