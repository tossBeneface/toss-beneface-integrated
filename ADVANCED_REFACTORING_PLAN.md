# 🚀 스콘에이아이 맞춤형 대규모 아키텍처 전환 및 최적화 계획
*(Advanced Architecture Migration & Optimization Plan)*

이 문서는 단순한 토이 프로젝트를 넘어, 스콘에이아이의 '대규모 글로벌 통역 서비스'에 걸맞은 엔터프라이즈급 아키텍처로 진화하기 위한 단계별 마일스톤입니다.

---

## Phase 1: 애플리케이션 코어 현대화 (Language & Code Level)
**목표:** 유지보수성, 안정성, 테스트 용이성 확보

1.  **Kotlin 기반으로의 점진적 마이그레이션 (Kotlin Migration)**
    *   **전략:** 100% 빅뱅 전환이 아닌, 신규 도메인이나 기존의 독립적인 서비스(예: `PaymentService`)부터 Kotlin으로 전환 (Java와 100% 상호운용성 활용).
    *   **이점:** Null Safety(`?`)를 통한 NPE(NullPointerException) 원천 차단, Coroutine을 활용한 효율적인 비동기/논블로킹 처리 (대규모 통역 트래픽 처리에 필수적).
2.  **클린 코드 및 도메인 주도 설계(DDD) 적용**
    *   **전략:** 현재 계층형(Layered) 아키텍처를 유지하되, 비즈니스 로직을 엔티티와 도메인 서비스로 응집시켜 '빈약한 도메인 모델(Anemic Domain Model)' 탈피.
3.  **테스트 커버리지 80% 이상 달성 (TDD/BDD)**
    *   **전략:** 핵심 결제, 예약, 인증 로직에 대해 JUnit5 + MockK(Kotlin 전환 시)를 활용한 단위 테스트 작성. Testcontainers를 활용해 로컬 환경에서도 실제 DB와 연동되는 통합 테스트 구축.

---

## Phase 2: 데이터 정합성 및 비동기 파이프라인 (Data & Messaging)
**목표:** 글로벌 서비스에 맞는 데이터 확장성 및 실시간 데이터 처리 능력 확보

1.  **데이터베이스 마이그레이션 (H2/MySQL -> PostgreSQL & NoSQL)**
    *   **PostgreSQL 전환:** 수륙양용(Relational + JSONB 지원)이 가능한 PostgreSQL로 메인 RDBMS 전환. 복잡한 트랜잭션과 데이터 무결성 보장.
    *   **NoSQL (Redis/MongoDB) 도입:** 통역 채팅 세션, 실시간 랭킹, 짧은 만료 시간(TTL)을 가지는 JWT Refresh Token 등을 Redis로 이관하여 RDBMS의 부하 분산.
2.  **메시지 큐(MQ) 도입을 통한 결합도 최소화 (Kafka / RabbitMQ)**
    *   **전략:** 결제 완료, 동시 통역 로그 저장, 알림 발송 등 즉시 처리가 필요 없는 '비동기 이벤트'를 Kafka 토픽으로 분리.
    *   **이점:** 특정 서비스(예: 알림 서버)가 죽어도 메인 서비스(예: 결제/통역)는 정상 작동하며, 대규모 트래픽 스파이크(Spike) 발생 시 버퍼(Buffer) 역할을 하여 DB 뻗음(장애)을 방지.

---

## Phase 3: 클라우드 네이티브 인프라 구축 (Infra & CI/CD)
**목표:** 언제든, 어디서든, 안전하고 빠르게 배포 및 스케일 아웃

1.  **Docker & Kubernetes (K8s) 기반 컨테이너 오케스트레이션**
    *   **전략:** Spring Boot, FastAPI 앱을 각각 Docker Image로 빌드하여 경량화. Kubernetes를 도입해 파드(Pod)의 Auto-scaling(HPA), 무중단 배포(Rolling Update), Self-healing(장애 시 자동 재시작) 구현.
    *   **이점:** '글로벌 행사'처럼 특정 시간대에 트래픽이 폭주할 때 유연하게 서버 대수를 늘리고 줄일 수 있음.
2.  **Jenkins & GitHub Actions를 활용한 파이프라인 자동화**
    *   **전략:** `코드 푸시 -> 빌드 -> 테스트 -> 도커 이미지 생성 -> K8s 배포`의 전 과정을 자동화.

---

## Phase 4: 모니터링, 대규모 트래픽 최적화 및 장애 대응
**목표:** 선제적 장애 감지 및 병목 구간 해소

1.  **Prometheus & Grafana 기반 시스템 가시성 확보**
    *   **전략:** Spring Boot Actuator와 Micrometer를 연동하여 Prometheus로 메트릭(CPU, 메모리, DB 커넥션 풀, HTTP 요청 응답 시간 등)을 수집하고, Grafana 대시보드로 시각화.
    *   **장애 대응:** CPU 사용량 80% 초과, 응답 시간 2초 초과 등 이상 징후 발생 시 Slack/Email로 즉각적인 알림(Alert) 전송 체계 구축.
2.  **대규모 트래픽 성능 개선 (Performance Optimization)**
    *   **캐싱(Caching) 전략:** 자주 조회되지만 덜 변하는 데이터(예: 메뉴 목록, 행사 정보)를 Redis의 `@Cacheable`을 활용해 글로벌 캐시 적용.
    *   **DB 인덱싱 및 쿼리 튜닝:** Slow Query 로그를 수집 및 분석하여 인덱스 최적화 및 N+1 문제(Fetch Join, BatchSize) 완벽 해결.
3.  **Circuit Breaker (Resilience4j) 적용**
    *   **전략:** 외부 API(예: 외부 결제망, AI LLM API)가 장애를 일으켰을 때 우리 서버 전체로 장애가 전파(Cascading Failure)되는 것을 막기 위해 서킷 브레이커 도입. 

---

### 💡 면접용 활용 멘트 (필살기)
"현재 프로젝트는 기본적인 MVP 형태로 빠르게 시장성을 검증하기 위해 구현되었습니다. 하지만 저는 스콘에이아이와 같은 **글로벌 대규모 서비스**를 염두에 두고 다음 단계를 계획하고 있습니다.

우선 **Kotlin**으로 점진적 마이그레이션을 진행하여 코루틴을 통한 비동기 처리의 효율성을 높이고, 대규모 트래픽 시 DB 병목을 막기 위해 **Kafka**를 도입해 이벤트를 비동기로 분리할 계획입니다. 또한 특정 행사 기간에 폭증하는 트래픽에 대응하기 위해 **Kubernetes** 기반의 오케스트레이션과 **Prometheus/Grafana** 모니터링 환경을 구축하여, 사전에 장애를 감지하고 유연하게 스케일 아웃할 수 있는 **클라우드 네이티브 아키텍처**로 진화시키는 것이 제 목표입니다."
