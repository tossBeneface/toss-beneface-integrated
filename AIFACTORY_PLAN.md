# 🏭 인공지능팩토리 백엔드 경력 채용 — 완전 정복 플랜

> **목표:** JD 자격요건 100% 충족 + 우대사항 최대한 커버  
> **기간:** 약 1주일  
> **전략:** 실제 코드 구현 → 면접 멘트 내재화 → "경험자처럼 말하기"

---

## 🎯 프로젝트 기획 확장 — "AI 기반 카페 실시간 운영 플랫폼"

### 왜 기획을 확장하는가

기존 TossBeneface는 결제 + 주문 기능을 갖춘 MVP였습니다.  
Kafka를 "억지로 끼워넣는" 오버엔지니어링이 되지 않으려면,  
**Kafka가 없으면 구조적으로 풀 수 없는 문제**가 기획 안에 있어야 합니다.

### 확장된 서비스 시나리오

**"여러 카페 매장이 입점한 AI 기반 실시간 주문·운영 플랫폼"**

| 이전 MVP | 확장 후 |
|---|---|
| 단일 카페 주문 처리 | 멀티 매장 실시간 주문 처리 |
| 결제 후 동기 처리 | 결제·주문 이벤트 → 독립 서비스들이 비동기 소비 |
| AI 음성 처리 후 동기 응답 | AI 처리 요청 → Kafka → FastAPI 소비 → 완료 후 WebSocket 푸시 |

### Kafka가 자연스럽게 필요한 이유 2가지

#### 이유 1: 주문 하나에 3개 팀이 동시에 반응해야 한다 (Direction 1)

```
[고객이 주문 버튼 클릭]
         │
         ▼
   order.created 토픽 발행
         │
    ┌────┴─────────────────┐
    │                      │                      │
    ▼                      ▼                      ▼
kitchen-service      inventory-service      analytics-service
(주방 화면 표시)      (재고 즉시 차감)      (실시간 매출 집계)
```

- 주방 화면이 다운돼도 재고 차감은 일어나야 한다
- 정산 서비스가 느려도 주방은 즉시 주문을 받아야 한다
- **3개 Consumer Group이 같은 이벤트를 독립적으로 소비** → Kafka Consumer Group이 구조적 필수

#### 이유 2: AI 처리는 수 초가 걸리기 때문에 동기 호출이 불가능하다 (Direction 2)

```
[기존 - 동기 방식의 문제]
클라이언트 → HTTP 요청 → FastAPI (3~10초 대기) → 응답
                              ↑ 타임아웃, 연결 점유, 재시도 폭풍 발생

[개선 - Kafka 비동기 파이프라인]
클라이언트 → "요청 접수됨" 즉시 응답
                │
                ▼
        voice.requested 토픽 발행
                │
                ▼
        [FastAPI Kafka Consumer]
        AI 처리 (3~10초)
                │
                ▼
        voice.completed 토픽 발행
                │
                ▼
        [Spring Boot Consumer]
        WebSocket으로 결과 클라이언트에 푸시
```

- 동기 방식에서는 AI 처리 중 타임아웃, 연결 점유, 재시도가 복합적으로 발생
- **비동기 파이프라인** → 클라이언트는 즉시 응답 받고, 결과는 WebSocket으로 수신
- FastAPI와 Spring Boot가 **Kafka 토픽을 통해 느슨하게 결합** → 어느 쪽이 재시작돼도 메시지 유실 없음

### 전체 시스템 아키텍처

```
                         ┌─────────────────────────────────────────┐
                         │              Nginx (80)                  │
                         │     리버스 프록시 + 로드밸런싱             │
                         └───────────┬─────────────┬───────────────┘
                                     │             │
                          ┌──────────▼──┐    ┌─────▼──────────┐
                          │ Spring Boot │    │    FastAPI      │
                          │   :8080     │    │    :8000        │
                          └──────┬──────┘    └────────┬────────┘
                                 │                    │
                    ┌────────────▼────────────────────▼──────────┐
                    │                  Kafka                      │
                    │                                             │
                    │  order.created ──────► kitchen-service      │
                    │                  └───► inventory-service    │
                    │                  └───► analytics-service    │
                    │                                             │
                    │  voice.requested ────► FastAPI Consumer     │
                    │  voice.completed ◄──── FastAPI Producer     │
                    │                  └───► WebSocket 푸시        │
                    │                                             │
                    │  payment.completed ──► notification-service │
                    │                  └───► analytics-service    │
                    └─────────────────────────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │       PostgreSQL         │
                    │   + outbox_event 테이블  │
                    └─────────────────────────┘
```

---

## 📊 현황 갭 분석 (Gap Analysis)

### ✅ 이미 있음 — 어필 가능한 것들

| 기술 | 현재 상태 | 면접 연결 포인트 |
|---|---|---|
| Kotlin + Spring Boot | ✅ 전체 전환 완료 | Coroutine, Null Safety |
| Spring Data JPA + QueryDSL | ✅ 비관적 락까지 | 트랜잭션 설계, N+1 해결 |
| Redis | ✅ Cache + RefreshToken | TTL 기반 토큰 관리, @Cacheable |
| Docker + GitHub Actions CI/CD | ✅ EC2 배포 자동화까지 | 빌드→테스트→도커→배포 파이프라인 |
| Prometheus + Grafana | ✅ Micrometer 연동 | 실시간 메트릭, 병목 가시화 |
| Resilience4j Circuit Breaker | ✅ Toss + FastAPI 적용 | Cascading Failure 방지, Fallback |
| FastAPI AI 서비스 연동 | ✅ Circuit Breaker 포함 | LLM 백엔드 경험, AI 워크플로우 |
| AWS (EC2, S3, Secrets Manager) | ✅ 운영 중 | 클라우드 배포·운영 경험 |

### ❌ 갭 — 구현 필요한 것들

| JD 항목 | 분류 | 난이도 | 예상 시간 |
|---|---|---|---|
| **Kafka** (Topic, Consumer Group, Exactly-Once) | 자격요건 | 🔴 높음 | 2~3일 |
| **Nginx** (리버스 프록시, 로드밸런싱) | 자격요건 | 🟡 중간 | 0.5일 |
| **PostgreSQL** (트랜잭션 튜닝, EXPLAIN) | 자격요건 | 🟡 중간 | 1일 |
| **OAuth2 / SSO** (Google 로그인) | 주요업무 + 우대 | 🟡 중간 | 1일 |
| **Spring Batch** (배치/잡 시스템) | 우대사항 | 🟡 중간 | 1일 |

---

## 🗺️ Phase별 실행 계획

---

### Phase 1: 인프라 기반 강화 — Nginx + PostgreSQL
> **목표:** "온프레미스 + 클라우드 하이브리드 환경 운영" 자격요건 충족  
> **소요 시간:** 1~1.5일

#### 1-1. Nginx 리버스 프록시 + 로드밸런싱

**구현 내용:**
- `docker-compose.local.yml`에 Nginx 컨테이너 추가
- Spring Boot(8080), FastAPI(8000) 앞단 통합 진입점 구성
- `upstream` 블록으로 Spring Boot 멀티 인스턴스 로드밸런싱 (least_conn 전략)
- 헬스체크 기반 자동 서버 제거 (`max_fails`, `fail_timeout`)
- Rate Limiting (`limit_req_zone`) — DDoS 기초 방어

**구현 파일:**
```
nginx/
├── nginx.conf          # 메인 설정
└── conf.d/
    └── default.conf    # upstream + location 블록
```

**docker-compose 변경:**
```yaml
nginx:
  image: nginx:alpine
  ports:
    - "80:80"
  volumes:
    - ./nginx/conf.d:/etc/nginx/conf.d
  depends_on:
    - backend
    - fastapi
```

**면접 멘트:**
> "인공지능팩토리의 온프레미스 환경처럼, 저는 Nginx를 API Gateway 역할로 구성해 Spring Boot와 FastAPI를 단일 진입점으로 통합했습니다. upstream 블록에 least_conn 로드밸런싱을 적용해 트래픽을 균등 분산하고, max_fails로 장애 인스턴스를 자동으로 제외하도록 설계했습니다."

---

#### 1-2. MySQL → PostgreSQL 마이그레이션

**구현 내용:**
- `docker-compose`에서 MySQL → PostgreSQL 16 교체
- `application-local.yml` 드라이버 및 Dialect 변경 (`PostgreSQLDialect`)
- `build.gradle`에서 `mysql-connector-j` → `postgresql` 드라이버 교체
- 마이그레이션 스크립트 작성 (Flyway 도입)
- 주요 테이블 인덱스 명시적 생성 (JPA DDL auto → Flyway 제어로 전환)

**DB 튜닝 구현 (면접 핵심):**
```sql
-- Payment 테이블: 회원별 결제 내역 조회 최적화
CREATE INDEX idx_payment_member_id_approved_at 
ON payment(member_id, approved_at DESC);

-- CardBenefit 테이블: shop + card 복합 조회 최적화
CREATE INDEX idx_card_benefit_shop_card 
ON card_benefit(shop, card_name);

-- EXPLAIN ANALYZE로 쿼리 플랜 검증
EXPLAIN ANALYZE SELECT * FROM payment 
WHERE member_id = 1 ORDER BY approved_at DESC;
```

**Flyway 도입:**
```
resources/db/migration/
├── V1__init_schema.sql
├── V2__add_indexes.sql
└── V3__add_audit_columns.sql
```

**면접 멘트:**
> "PostgreSQL을 선택한 이유는 크게 두 가지입니다. 첫째, JSONB 타입으로 AI 응답 로그처럼 스키마가 유동적인 데이터를 유연하게 저장할 수 있습니다. 둘째, EXPLAIN ANALYZE를 통해 쿼리 플랜을 직접 분석하고 복합 인덱스를 설계하여 결제 내역 조회 응답 시간을 개선했습니다. 또한 DDL 변경을 Flyway로 버전 관리해 온프레미스 서버에 배포할 때도 스키마 변경 이력이 코드와 함께 추적되도록 했습니다."

---

### Phase 2: Kafka — 이벤트 드리븐 아키텍처
> **목표:** "주문 이벤트 멀티 Consumer" + "AI 비동기 파이프라인" — Kafka가 구조적으로 필수인 설계  
> **소요 시간:** 2~3일

#### 2-1. Kafka 인프라 설정

**구현 내용:**
- `docker-compose`에 Kafka (KRaft 모드, Zookeeper 없이) 추가
- Kafka UI 추가 (`kafka-ui`) — 토픽/Consumer Lag 시각화

```yaml
kafka:
  image: confluentinc/cp-kafka:7.6.0
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'false'
    KAFKA_DEFAULT_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1

kafka-ui:
  image: provectuslabs/kafka-ui:latest
  ports:
    - "8989:8080"
  environment:
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
```

#### 2-2. 토픽 설계 (Topic Design)

| 토픽 | Partition | 발행자 | 소비자 (Consumer Group) |
|---|---|---|---|
| `order.created` | 3 | OrderService | kitchen-service, inventory-service, analytics-service |
| `order.status.updated` | 3 | KitchenConsumer | notification-service (WebSocket 푸시) |
| `voice.requested` | 2 | VoiceController | ai-pipeline-service (FastAPI) |
| `voice.completed` | 2 | FastAPI | notification-service (WebSocket 푸시) |
| `payment.completed` | 3 | OutboxPublisher | notification-service, analytics-service |

**Partition 설계 근거:**
- `order.created`: `cafe_id`를 Key로 → 같은 카페의 주문은 같은 Partition → 순서 보장
- `voice.requested`: `member_id`를 Key로 → 한 사용자의 요청은 순서 보장

#### 2-3. Direction 1 — 주문 이벤트 멀티 Consumer Group

**Outbox 패턴으로 Exactly-Once 보장:**

```kotlin
// OrderService: 주문 저장 + Outbox 이벤트 저장 = 같은 트랜잭션
@Transactional
fun createOrder(request: OrderRequest, memberId: Long): OrderResponse {
    val order = orderRepository.save(Order.of(request, memberId))

    // Outbox 이벤트 저장 — Kafka 발행 실패해도 주문은 저장됨
    outboxRepository.save(OutboxEvent(
        aggregateId = order.id.toString(),
        aggregateType = "ORDER",
        eventType = "ORDER_CREATED",
        partitionKey = order.cafeId.toString(),   // cafe_id로 파티셔닝
        payload = objectMapper.writeValueAsString(OrderCreatedEvent(order))
    ))
    return OrderResponse.from(order)
}

// OutboxPublisher: 미발행 이벤트 폴링 → Kafka 발행
@Scheduled(fixedDelay = 500)
@Transactional
fun publishPendingEvents() {
    outboxRepository.findByPublishedFalse().forEach { event ->
        kafkaTemplate.send(
            topicOf(event.eventType),
            event.partitionKey,   // Key 지정 → 같은 카페 주문은 같은 Partition
            event.payload
        )
        event.published = true
    }
}
```

**3개 Consumer Group — 독립적으로 소비:**

```kotlin
// 1. 주방 Consumer: 주방 화면에 주문 표시
@Component
class KitchenConsumer {
    @KafkaListener(topics = ["order.created"], groupId = "kitchen-service")
    fun handle(event: OrderCreatedEvent) {
        // 주방 디스플레이에 주문 push (WebSocket)
        // 처리 완료 후 order.status.updated 발행 → "제조 중"
        kitchenDisplayService.display(event)
        kafkaTemplate.send("order.status.updated", OrderStatusEvent(event.orderId, "PREPARING"))
    }
}

// 2. 재고 Consumer: 재고 즉시 차감
@Component
class InventoryConsumer {
    @KafkaListener(topics = ["order.created"], groupId = "inventory-service")
    fun handle(event: OrderCreatedEvent) {
        // 주방 화면이 다운돼도 재고 차감은 독립적으로 실행
        inventoryService.deduct(event.items)
    }
}

// 3. 분석 Consumer: 실시간 매출 집계
@Component
class AnalyticsConsumer {
    @KafkaListener(
        topics = ["order.created", "payment.completed"],
        groupId = "analytics-service"
    )
    fun handle(event: String) {
        // 카페별/시간대별 매출 집계 → Redis에 캐시
        analyticsService.aggregate(event)
    }
}
```

#### 2-4. Direction 2 — AI 음성 처리 비동기 파이프라인

**Spring Boot 측 — 요청 즉시 수락 후 Kafka 발행:**

```kotlin
@RestController
class VoiceController(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    @PostMapping("/api/voice/process")
    fun requestVoiceProcess(
        @RequestBody request: VoiceRequest,
        @AuthenticationPrincipal memberId: Long
    ): ResponseEntity<VoiceAcceptedResponse> {
        val requestId = UUID.randomUUID().toString()

        // 즉시 Kafka 발행 — FastAPI를 기다리지 않음
        kafkaTemplate.send("voice.requested", memberId.toString(),
            objectMapper.writeValueAsString(VoiceRequestedEvent(requestId, memberId, request.audioUrl))
        )

        // 클라이언트에게 "접수됨" 즉시 응답
        return ResponseEntity.accepted().body(VoiceAcceptedResponse(requestId))
    }
}

// voice.completed 수신 → WebSocket으로 클라이언트에 결과 푸시
@Component
class VoiceCompletedConsumer(private val messagingTemplate: SimpMessagingTemplate) {
    @KafkaListener(topics = ["voice.completed"], groupId = "notification-service")
    fun handle(event: VoiceCompletedEvent) {
        // WebSocket 채널로 결과 전송 (클라이언트가 구독 중)
        messagingTemplate.convertAndSendToUser(
            event.memberId.toString(),
            "/queue/voice-result",
            VoiceResultResponse(event.requestId, event.result)
        )
    }
}
```

**FastAPI 측 — Kafka Consumer + Producer:**

```python
# FastAPI: voice.requested 소비 → AI 처리 → voice.completed 발행
@app.on_event("startup")
async def start_kafka_consumer():
    consumer = AIOKafkaConsumer(
        "voice.requested",
        bootstrap_servers="kafka:9092",
        group_id="ai-pipeline-service"
    )
    asyncio.create_task(consume_voice_requests(consumer))

async def consume_voice_requests(consumer):
    async for msg in consumer:
        event = json.loads(msg.value)
        
        # AI 처리 (수 초 소요) — Kafka가 이 시간 동안 연결을 유지
        result = await process_voice(event["audioUrl"])
        
        # 완료 후 voice.completed 토픽 발행
        await producer.send("voice.completed", value={
            "requestId": event["requestId"],
            "memberId": event["memberId"],
            "result": result
        })
```

**면접 멘트:**
> "Kafka를 도입한 이유는 두 가지 구조적 문제를 해결하기 위해서였습니다.
>
> 첫째, 주문 하나에 주방·재고·정산 서비스가 동시에 반응해야 하는데, 동기 호출로는 하나가 실패하면 전체가 실패하는 강결합 문제가 생깁니다. Consumer Group으로 분리하면 주방 서비스가 다운돼도 재고 차감과 정산은 독립적으로 계속 작동합니다. 같은 카페의 주문이 항상 같은 Partition에 도달하도록 cafe_id를 Key로 지정해 순서도 보장했습니다.
>
> 둘째, AI 음성 처리는 수 초가 걸리기 때문에 동기 HTTP 호출이 불가능합니다. 클라이언트가 요청하면 즉시 '접수됨'을 응답하고, FastAPI가 voice.requested를 소비해 처리한 뒤 voice.completed를 발행하면 Spring Boot가 WebSocket으로 결과를 푸시합니다. 이 구조에서 FastAPI가 재시작돼도 Kafka에 쌓인 메시지는 유실되지 않습니다.
>
> Exactly-Once는 Outbox 패턴으로 보장했습니다. 주문 저장과 Outbox 이벤트 저장을 같은 DB 트랜잭션으로 묶고, Scheduler가 미발행 이벤트를 폴링해 Kafka에 발행합니다."

---

### Phase 3: OAuth2 / SSO 통합
> **목표:** 주요업무 "사용자 인증/인가 (SSO 통합 포함)" + 우대사항 "OAuth2, OIDC" 충족  
> **소요 시간:** 1일

#### 3-1. Google OAuth2 연동

**구현 내용:**
- `build.gradle`에 `spring-boot-starter-oauth2-client` 추가
- Google Cloud Console에서 OAuth2 클라이언트 ID 발급
- `SecurityConfig`에 OAuth2 로그인 엔드포인트 추가
- **OAuth2 → 내부 JWT 발급 플로우 구현:**

```
[브라우저]
    → GET /oauth2/authorize/google
    → Google 동의 화면
    → GET /login/oauth2/code/google?code=...
    → OAuth2SuccessHandler.onAuthenticationSuccess()
        → Google 사용자 정보 추출
        → DB에 소셜 회원 저장 또는 기존 회원 연결
        → 내부 JWT(AccessToken + RefreshToken) 발급
        → 쿠키에 담아 프론트엔드로 Redirect
```

**OAuth2SuccessHandler:**
```kotlin
@Component
class OAuth2SuccessHandler(
    private val memberRepository: MemberRepository,
    private val jwtTokenProvider: JwtTokenProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauthUser = authentication.principal as OAuth2User
        val email = oauthUser.getAttribute<String>("email")!!
        
        val member = memberRepository.findByEmail(email)
            ?: memberRepository.save(Member.ofSocial(email, oauthUser))
        
        val accessToken = jwtTokenProvider.generateAccessToken(member.id)
        // 쿠키 설정 후 프론트 리다이렉트
        response.sendRedirect("${frontendUrl}/oauth2/callback?token=$accessToken")
    }
}
```

**면접 멘트:**
> "OAuth2 표준 흐름을 구현하면서 주의한 점은 외부 OAuth2 토큰과 내부 JWT를 분리한 것입니다. Google AccessToken을 그대로 API 인증에 사용하면 Google에 대한 의존성이 생기므로, OAuth2 인증 성공 후 즉시 내부 JWT로 교체 발급합니다. 이 방식은 향후 Kakao, GitHub 등 다른 OAuth2 Provider를 추가할 때도 SuccessHandler만 공통화하면 되기 때문에 확장성이 높습니다. SSO 관점에서는 이 구조가 IdP(Identity Provider) 기반 인증의 기초이며, OIDC로 확장하면 엔터프라이즈 SSO도 동일한 방식으로 통합됩니다."

---

### Phase 4: Spring Batch — 배치/잡 시스템
> **목표:** 우대사항 "배치/잡 시스템 설계 경험" 충족  
> **소요 시간:** 1일

#### 4-1. 배치 시나리오 선택

**비즈니스 맥락:** AI 경진대회 플랫폼이라면 다음이 자연스럽다
- 매일 자정: 일별 결제 정산 집계 배치
- 매주 월요일: 비활성 회원 데이터 정리 배치
- 카드 혜택 데이터 대량 업로드 배치 (CSV → DB)

**구현: 일별 결제 정산 집계 배치**

```kotlin
@Configuration
class DailySettlementJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val paymentRepository: PaymentRepository,
    private val settlementRepository: SettlementRepository
) {

    @Bean
    fun dailySettlementJob(): Job = JobBuilder("dailySettlementJob", jobRepository)
        .start(settlementStep())
        .build()

    @Bean
    fun settlementStep(): Step = StepBuilder("settlementStep", jobRepository)
        .chunk<Payment, DailySettlement>(100, transactionManager)  // 100건씩 청크 처리
        .reader(paymentItemReader())
        .processor(settlementItemProcessor())
        .writer(settlementItemWriter())
        .build()

    @Bean
    fun paymentItemReader(): JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .name("paymentItemReader")
            .pageSize(100)
            .queryString("SELECT p FROM Payment p WHERE p.approvedAt >= :startDate AND p.approvedAt < :endDate AND p.status = 'DONE'")
            .build()
}
```

**스케줄러로 Job 트리거:**
```kotlin
@Scheduled(cron = "0 0 0 * * *")  // 매일 자정
fun runDailySettlementJob() {
    val jobParameters = JobParametersBuilder()
        .addLocalDate("targetDate", LocalDate.now().minusDays(1))
        .toJobParameters()
    jobLauncher.run(dailySettlementJob, jobParameters)
}
```

**면접 멘트:**
> "AI 경진대회 플랫폼에서 일별 참가자 결제 정산이나 대량 데이터 처리가 필요한 경우 Spring Batch를 활용했습니다. Chunk 기반 처리로 메모리 OOM 없이 수십만 건을 안전하게 처리하고, JobParameters로 재실행 시 멱등성을 보장합니다. 실패한 Step은 재시작 시 해당 Chunk부터 이어서 처리하는 재시작 가능성(Restartability)도 확보했습니다."

---

### Phase 5: 면접 문서 전면 재작성
> **목표:** 위 구현들을 인공지능팩토리 JD 언어로 재포장  
> **소요 시간:** 0.5일

**새 파일:** `INTERVIEW_PREP_AIFACTORY.md`

**섹션 구성:**
1. Kafka + Outbox 패턴 (Exactly-Once)
2. Nginx 리버스 프록시 + 로드밸런싱
3. PostgreSQL 트랜잭션 설계 + EXPLAIN ANALYZE
4. OAuth2 / SSO 통합 인증
5. Spring Batch 배치 시스템
6. LLM/AI 백엔드 + Circuit Breaker (기존 강화)
7. 성능 모니터링 + 장애 대응 (기존 강화)
8. CI/CD 파이프라인 (기존 강화)

---

## 📅 주간 일정표

| 일차 | 작업 | 결과물 |
|---|---|---|
| **Day 1 (오전)** | Nginx 설정 + docker-compose 추가 | 80 포트 통합 진입점 |
| **Day 1 (오후)** | MySQL → PostgreSQL + Flyway 도입 | 스키마 버전 관리 |
| **Day 2 (오전)** | 인덱스 설계 + EXPLAIN ANALYZE 튜닝 | 쿼리 최적화 문서화 |
| **Day 2 (오후)** | Kafka docker-compose + 토픽 생성 스크립트 | Kafka + Kafka UI 기동 |
| **Day 3** | OutboxEvent 엔티티 + Flyway 마이그레이션 + OutboxPublisher | Exactly-Once 기반 완성 |
| **Day 4 (오전)** | OrderService Outbox 연동 + 3개 Consumer Group 구현 | Direction 1 완성 |
| **Day 4 (오후)** | VoiceController Kafka 발행 + VoiceCompletedConsumer + WebSocket | Direction 2 완성 |
| **Day 5** | FastAPI Kafka Consumer/Producer 구현 | AI 비동기 파이프라인 완성 |
| **Day 6 (오전)** | OAuth2 Google 연동 + JWT 통합 | SSO 인증 완성 |
| **Day 6 (오후)** | Spring Batch 일별 정산 배치 | 배치 시스템 완성 |
| **Day 7** | 전체 통합 테스트 + 면접 문서 재작성 | `INTERVIEW_PREP_AIFACTORY.md` |

---

## 💬 최종 면접 필살기 멘트 (전체 통합)

> "저는 이번 프로젝트를 단순 MVP에서 'AI 기반 카페 실시간 운영 플랫폼'으로 확장하면서, 인공지능팩토리의 온프레미스+클라우드 하이브리드 환경과 유사한 아키텍처를 직접 설계했습니다.
>
> **인프라 측면**에서는 Nginx를 API Gateway로 구성해 Spring Boot와 FastAPI를 단일 진입점으로 통합하고, least_conn 로드밸런싱과 max_fails 기반 장애 인스턴스 자동 제거를 구현했습니다. DB는 MySQL에서 PostgreSQL로 전환하며 Flyway로 스키마 변경을 버전 관리하고, EXPLAIN ANALYZE로 주문 조회 쿼리를 분석해 복합 인덱스를 설계했습니다.
>
> **Kafka 도입**은 두 가지 구조적 필요에서 출발했습니다. 첫째, 주문 이벤트 하나를 주방·재고·정산 서비스가 독립적으로 소비해야 하는 문제입니다. 동기 호출로는 하나가 실패하면 전체가 실패하는 강결합이 생기기 때문에, Consumer Group으로 각 서비스를 분리하고 cafe_id를 파티션 키로 지정해 같은 매장의 주문 순서를 보장했습니다. 둘째, AI 음성 처리는 수 초가 걸려 동기 HTTP 호출이 불가능합니다. 클라이언트는 즉시 접수 응답을 받고, FastAPI가 voice.requested를 소비해 처리한 뒤 voice.completed를 발행하면 WebSocket으로 결과를 푸시하는 완전 비동기 파이프라인을 구현했습니다. Exactly-Once는 Outbox 패턴으로 보장했습니다.
>
> **인증**은 Google OAuth2를 연동해 외부 토큰을 내부 JWT로 즉시 교환하는 SSO 구조를 구현했습니다. 이 방식은 Kakao·GitHub Provider 추가 시 SuccessHandler만 확장하면 되고, OIDC 기반 엔터프라이즈 SSO로도 동일하게 적용 가능합니다.
>
> **관찰 가능성** 측면에서는 Prometheus로 Kafka Consumer Lag, Circuit Breaker 상태, HTTP 응답 시간을 실시간 수집하고, Resilience4j로 AI 서버 장애 시 Fallback을 통해 핵심 서비스가 중단되지 않도록 장애를 격리했습니다."

---

## 🔗 구현 파일 체크리스트

### Phase 1 (Nginx + PostgreSQL)
- [ ] `nginx/conf.d/default.conf` — 리버스 프록시 + upstream 설정
- [ ] `docker-compose.local.yml` — nginx, postgresql 서비스 추가
- [ ] `build.gradle` — postgresql 드라이버 추가
- [ ] `application-local.yml` — PostgreSQL Dialect 변경
- [ ] `resources/db/migration/V1__init_schema.sql` — Flyway 초기 스키마
- [ ] `resources/db/migration/V2__add_indexes.sql` — 인덱스 추가

### Phase 2 (Kafka)
- [ ] `docker-compose.local.yml` — Kafka (KRaft), Kafka UI 추가
- [ ] `domain/outbox/entity/OutboxEvent.kt` — partitionKey 필드 포함
- [ ] `domain/outbox/repository/OutboxRepository.kt`
- [ ] `global/config/KafkaConfig.kt` — Producer/Consumer/KafkaTemplate 설정
- [ ] `global/config/WebSocketConfig.kt` — STOMP WebSocket 설정
- [ ] `resources/db/migration/V3__add_outbox_table.sql`
- [ ] `api/order/service/OrderService.kt` — Outbox 저장 추가
- [ ] `api/order/event/OrderCreatedEvent.kt` — 이벤트 DTO
- [ ] `api/order/consumer/KitchenConsumer.kt` — groupId: kitchen-service
- [ ] `api/order/consumer/InventoryConsumer.kt` — groupId: inventory-service
- [ ] `api/analytics/consumer/AnalyticsConsumer.kt` — groupId: analytics-service
- [ ] `api/voice/controller/VoiceController.kt` — Kafka 발행으로 교체
- [ ] `api/voice/event/VoiceRequestedEvent.kt`
- [ ] `api/voice/event/VoiceCompletedEvent.kt`
- [ ] `api/voice/consumer/VoiceCompletedConsumer.kt` — WebSocket 푸시
- [ ] `fastApi/kafka_consumer.py` — voice.requested 소비 + voice.completed 발행
- [ ] `scheduler/OutboxPublisher.kt` — 미발행 이벤트 폴링 발행

### Phase 3 (OAuth2)
- [ ] `build.gradle` — oauth2-client 의존성 추가
- [ ] `global/config/SecurityConfig.kt` — OAuth2 설정 추가
- [ ] `auth/infra/oauth2/OAuth2SuccessHandler.kt`
- [ ] `auth/infra/oauth2/CustomOAuth2UserService.kt`
- [ ] `application-local.yml` — Google OAuth2 클라이언트 설정
- [ ] `domain/member/entity/Member.kt` — 소셜 로그인 필드 추가

### Phase 4 (Spring Batch)
- [ ] `build.gradle` — spring-batch 의존성 추가
- [ ] `batch/config/DailySettlementJobConfig.kt`
- [ ] `batch/entity/DailySettlement.kt`
- [ ] `batch/repository/SettlementRepository.kt`
- [ ] `scheduler/BatchScheduler.kt`
- [ ] `resources/db/migration/V3__add_batch_tables.sql`

### Phase 5 (면접 문서)
- [ ] `INTERVIEW_PREP_AIFACTORY.md` — 인공지능팩토리 맞춤 면접 준비 문서
