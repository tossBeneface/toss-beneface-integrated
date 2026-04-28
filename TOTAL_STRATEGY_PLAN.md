# 🏆 통합 아키텍처 고도화 및 면접 필승 전략 (TOTAL_STRATEGY_PLAN)

이 문서는 기존의 모든 프로젝트 계획서와 면접 준비 자료를 하나로 통합하고, 최근 면접 피드백을 반영하여 기술적 깊이와 인터뷰 전략을 강화한 최종 로드맵입니다.

---

## 📅 Part 1: 아키텍처 현대화 마일스톤 (Technical Roadmap)

### Phase 1: 인프라 기반 강화 및 앱 현대화 (Infra & Code)
*   **Nginx API Gateway:** 단일 진입점 구축, `least_conn` 로드밸런싱, 장애 인스턴스 자동 제외.
*   **PostgreSQL 전환:** MySQL에서 전환 후 Flyway로 버전 관리. `EXPLAIN ANALYZE` 기반 쿼리 튜닝.
*   **Kotlin 마이그레이션:** Null Safety와 Coroutine을 활용한 비동기 성능 최적화. (PaymentService 등 독립 도메인부터 우선 적용)
*   **테스트 커버리지:** JUnit5 + MockK/Mockito를 활용하여 핵심 로직 커버리지 80% 달성.

### Phase 2: 이벤트 드리븐 및 비동기 파이프라인 (Data & Messaging)
*   **Kafka 도입:** 주문/결제 이벤트를 비동기로 분리하여 시스템 결합도 최소화.
*   **Outbox 패턴:** DB 트랜잭션과 메시지 발행의 원자성 확보 (Exactly-Once 보장).
*   **AI 비동기 처리:** `voice.requested` -> FastAPI -> `voice.completed` 구조로 동기 호출의 타임아웃 문제 해결.

### Phase 3: 인증, 배치 및 클라우드 네이티브 (Scale & Security)
*   **OAuth2 / SSO:** Google 로그인 연동 및 내부 JWT 교환 플로우 구축.
*   **Spring Batch:** 일별 정산 및 대량 데이터 처리를 위한 Chunk 기반 배치 시스템.
*   **K8s & CI/CD:** Docker 컨테이너화 및 Kubernetes 오케스트레이션, GitHub Actions 파이프라인 자동화.

### Phase 4: 모니터링 및 성능 최적화 (Ops & Performance)
*   **시스템 가시성:** Prometheus & Grafana 대시보드 구축 및 Slack 알림 연동.
*   **장애 격리:** Resilience4j Circuit Breaker로 외부 API 장애 전파 방지.
*   **캐싱 전략:** Redis를 활용한 글로벌 캐시(@Cacheable) 및 성능 튜닝.

---

## 🐣 Part 2: 기술 개념 무기화 (6세 비유 & Interview Logic)

| 항목 | 6세 눈높이 비유 | 면접 답변 포인트 (Before & After) |
| :--- | :--- | :--- |
| **JWT** | 비밀 도장이 찍힌 3칸(머리/몸통/꼬리) 마법 신분증 | **Before:** 로직 혼재로 구조 파악 어려움 -> **After:** 파트별 주석 명시 및 로직 분리로 무결성 증명 |
| **비관적 락** | 장난감 상자에 채우는 튼튼한 자물쇠 | **Before:** 경쟁 상태로 인한 데이터 오류 위험 -> **After:** DB 레벨 점유로 결제 정합성 100% 보장 |
| **Kafka** | 여러 친구가 각자 자기 바구니에 쪽지를 받는 것 | **Before:** 동기 호출로 인한 강결합 및 장애 전파 -> **After:** 독립적 소비 및 AI 비동기 처리로 안정성 확보 |
| **Nginx** | 덜 바쁜 줄로 아이들을 보내주는 선생님 | **Before:** 각 서버 포트 직접 노출 및 관리 어려움 -> **After:** 단일 진입점 및 로드밸런싱으로 보안/효율 강화 |
| **Batch** | 큰 숙제를 100장씩 나눠서 차근차근 검사하는 것 | **Before:** 대량 작업 시 OOM 위험 및 운영 불안정 -> **After:** Chunk 기반 처리로 안정적인 후처리 프로세스 구축 |

---

## 💻 Part 3: 핵심 상세 구현 (Technical Deep Dive)

### 1. Kafka - 이벤트 드리븐 아키텍처
**Outbox 패턴으로 데이터 정합성 보장:**
```kotlin
@Transactional
fun createOrder(request: OrderRequest, memberId: Long): OrderResponse {
    val order = orderRepository.save(Order.of(request, memberId))
    outboxRepository.save(OutboxEvent(
        aggregateId = order.id.toString(),
        aggregateType = "ORDER",
        eventType = "ORDER_CREATED",
        partitionKey = order.cafeId.toString(),
        payload = objectMapper.writeValueAsString(OrderCreatedEvent(order))
    ))
    return OrderResponse.from(order)
}
```

### 2. Nginx - 리버스 프록시 설정
```nginx
upstream backend_servers {
    least_conn;
    server backend1:8080 max_fails=3 fail_timeout=30s;
    server backend2:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    location /api/ {
        proxy_pass http://backend_servers;
        limit_req zone=mylimit burst=10 nodelay;
    }
}
```

### 3. PostgreSQL - 쿼리 튜닝
```sql
-- 복합 인덱스 설계 및 EXPLAIN ANALYZE 검증
CREATE INDEX idx_payment_member_id_approved_at ON payment(member_id, approved_at DESC);
EXPLAIN ANALYZE SELECT * FROM payment WHERE member_id = 1 ORDER BY approved_at DESC;
```

---

## 🚀 Part 4: AI 비동기 파이프라인 (FastAPI 통합)

*   **구조:** Spring Boot(Producer) -> Kafka -> FastAPI(Consumer/AI 처리) -> Kafka -> Spring Boot(Consumer/WebSocket Push)
*   **이점:** AI의 긴 처리 시간 동안 HTTP 연결을 점유하지 않으며, 서버 재시작 시에도 메시지 유실 없음.
*   **FastAPI 구현 예시:**
```python
async def consume_voice_requests(consumer):
    async for msg in consumer:
        event = json.loads(msg.value)
        result = await process_voice(event["audioUrl"])
        await producer.send("voice.completed", value={"requestId": event["requestId"], "result": result})
```

---

## 🔥 Part 5: 면접 개선 계획 (Interview Strategy & Learning)

최근 면접 피드백을 바탕으로 보완한 **"전문성 깊이"**와 **"서사 전략"**입니다.

### 1. 파이썬-자바 통합 기술 심화
*   **PyO3 / maturin:** Rust 바인딩을 통해 파이썬 AI 로직을 자바에서 네이티브 수준으로 고속 호출하는 구조 학습.
*   **gRPC / Protobuf:** 서비스 간 HTTP/1.1 대비 저지연/고성능 통신 환경 구축.
*   **Sidecar / Mesh:** K8s 환경에서 AI 모델 서빙(Python)과 비즈니스 로직(Java)의 독립적 확장 전략.

### 2. AI 에이전트 협업 시스템 (Agentic Workflow - Active)
단순 보조를 넘어, 에이전트가 프로젝트를 스스로 이해하고 검증할 수 있는 인프라를 구축하여 활용 중입니다.
*   **CLAUDE.md (Project Constitution):** 에이전트가 상시 참조하는 가이드라인을 수립하여 코드 일관성과 품질을 자동 유지.
*   **Claude Code Hooks (Auto-Validation):** `.claude/hooks/post-refactor-check.sh`를 통해 리팩토링 직후 빌드/테스트를 자동 검증.
*   **MCP (Model Context Protocol):** `mcp_server.py`를 통해 에이전트가 직접 DB 스키마를 조회하고 `explain_query` 도구로 성능을 분석하는 환경 구축.

### 3. 인터뷰 서사 및 리스크 관리 (Soft Skills)
*   **열정 전환 서사:** "식었다" 대신 **"기술적 호기심이 제품의 현실적 가치와 사용자 연결로 진화했다"**고 정의.
*   **이력 공백/이탈:** "두 문장"으로 요약 후 현재의 준비성 강조. "과거의 경험이 현재 AI 실무를 위한 탄탄한 밑거름이 됨."
*   **위험 발언 필터링:** 세대론(MZ 등) 언급 지양. 대신 **"팀 전체의 생산성을 위해 블로커를 제거하고 유연하게 협업하는 태도"** 강조.
*   **AI 시대의 역할:** "단순 코더를 넘어 아키텍처를 설계하고 AI를 도구로 활용해 비즈니스 임팩트를 만드는 엔지니어"로 포지셔닝.

### 4. 전략적 역질문 (킬러 퀘스천)
*   "현재 파이썬 백엔드의 자바 전환 시 가장 큰 페인 포인트는 성능인가요, 유지보수성인가요?"
*   "전환 과정에서 기존 AI 모델의 인터페이스(gRPC/MQ 등)는 어떤 방식으로 통합할 계획이신가요?"

---

## 🔗 체크리스트 및 관리
- [ ] `TOTAL_STRATEGY_PLAN.md` 숙지 및 면접 멘트 연습
- [x] gRPC 및 PyO3 기초 실습 진행
- [x] Claude Code Hooks 실제 프로젝트 적용
- [ ] 공백기/열정 서사 2문장 버전 녹음 및 피드백
