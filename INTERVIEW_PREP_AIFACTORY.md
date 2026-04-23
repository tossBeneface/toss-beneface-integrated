# 인공지능팩토리 면접 준비 노트

## 1. Kafka + Outbox + Consumer Group

### 6세 비유
- 주문 쪽지를 하나 쓰면 요리하는 친구, 재고 보는 친구, 기록하는 친구가 각각 자기 바구니에 같은 쪽지를 받아서 따로 일하는 거예요.

### Before
- 결제 후 주문, 재고, 통계를 한 서버가 순서대로 직접 처리했습니다.
- 하나라도 느리거나 실패하면 전체 응답이 같이 늦어지고, 서로 강하게 묶여 있었습니다.
- AI 음성 처리도 HTTP로 바로 기다려서 타임아웃 위험이 컸습니다.

### After
- 주문 저장과 이벤트 저장을 같은 트랜잭션으로 묶는 Outbox 패턴을 적용했습니다.
- `order.created`를 `kitchen-service`, `inventory-service`, `analytics-service`가 Consumer Group으로 독립 소비합니다.
- `cafeId`, `memberId`를 파티션 키로 써서 같은 카페/사용자 이벤트 순서를 보장했습니다.
- AI 요청은 `voice.requested`로 보내고, FastAPI가 처리 후 `voice.completed`를 발행하면 Spring Boot가 WebSocket으로 결과를 푸시합니다.

### 면접 답변 핵심
- "Kafka는 그냥 넣은 게 아니라, 주문 하나를 여러 서비스가 동시에 독립 소비해야 하는 구조와 AI 비동기 처리 문제를 풀기 위해 넣었습니다."
- "Exactly-Once 성격은 Outbox로 확보했고, Consumer는 수동 커밋으로 처리 성공 후에만 offset을 이동시켰습니다."

## 2. Nginx

### 6세 비유
- 학교 문 앞에 선생님 한 분이 서서, 들어오는 아이들을 덜 바쁜 줄로 보내주는 것과 같아요.

### Before
- Spring Boot와 FastAPI가 각자 포트를 직접 열고 있었습니다.
- 클라이언트가 어느 서버로 가야 하는지 알아야 했고, 장애 인스턴스 제어도 약했습니다.

### After
- Nginx를 단일 진입점으로 두고 Spring Boot, FastAPI 앞단 프록시로 배치했습니다.
- `least_conn`으로 덜 바쁜 인스턴스에 보내고, `max_fails`, `fail_timeout`으로 문제 있는 인스턴스를 자동 제외했습니다.
- Rate limiting도 걸어 기본적인 트래픽 방어선을 만들었습니다.

### 면접 답변 핵심
- "API Gateway 역할을 Nginx로 분리해서 애플리케이션과 트래픽 제어 책임을 나눴습니다."

## 3. PostgreSQL 튜닝

### 6세 비유
- 장난감 상자를 종류별로 나눠 담아 두면 찾고 싶은 장난감을 훨씬 빨리 찾는 것과 같아요.

### Before
- MySQL 기반이었고, 인덱스와 DDL 변경 이력이 코드와 느슨하게 연결돼 있었습니다.
- 쿼리가 왜 느린지 실행 계획을 기준으로 설명하기 어려웠습니다.

### After
- PostgreSQL로 전환하고 Flyway로 스키마 변경을 버전 관리했습니다.
- `EXPLAIN ANALYZE`로 실제 실행 계획을 보고 복합 인덱스, Partial Index, Covering Index를 설계했습니다.
- JPA `ddl-auto`는 `validate`로 낮추고, 실제 스키마 책임은 Flyway로 일원화했습니다.

### 면접 답변 핵심
- "튜닝을 감으로 하지 않고, 실행 계획을 근거로 인덱스를 설계했습니다."

## 4. OAuth2 / SSO

### 6세 비유
- 학교 출입증은 구글이 확인해 주지만, 우리 교실에 들어올 때는 우리 반 이름표로 다시 바꿔 다는 것과 같아요.

### Before
- 이메일/비밀번호 기반 자체 로그인만 있었습니다.
- 외부 IdP 연동이나 SSO 확장 포인트가 없었습니다.

### After
- Google OAuth2 로그인 흐름을 추가했습니다.
- Google 인증이 끝나면 외부 토큰을 그대로 쓰지 않고, 내부 JWT를 다시 발급해서 access token 쿠키와 refresh token 쿠키로 넘깁니다.
- `socialType`, `socialId`를 `Member`에 저장해 같은 이메일 사용자의 소셜 계정을 연결하거나 신규 회원을 자동 생성합니다.

### 면접 답변 핵심
- "외부 OAuth2 토큰과 내부 인증 토큰을 분리해서, Google에 종속되지 않는 내부 보안 구조를 유지했습니다."

## 5. Spring Batch

### 6세 비유
- 큰 숙제를 한 번에 다 하지 않고 100장씩 나눠서 차근차근 검사하는 것과 같아요.

### Before
- 일별 정산 같은 대량 후처리 작업을 안정적으로 돌릴 배치 구조가 없었습니다.

### After
- `Payment` 중 `DONE` 상태와 승인 일시 범위를 읽어 `DailySettlement`로 적재하는 Batch Job을 만들었습니다.
- Chunk size를 100으로 두어 메모리 사용량을 통제했습니다.
- 자정 스케줄러가 전날 날짜를 `JobParameters`로 넣어 실행하게 해서 같은 날짜 배치의 중복 실행을 제어했습니다.

### 면접 답변 핵심
- "온라인 요청 처리와 대량 후처리를 분리해서 운영 안정성을 높였습니다."

## 6. AI 비동기 파이프라인

### 6세 비유
- 긴 그림 그리기 숙제는 바로 끝날 때까지 기다리지 않고, 번호표를 주고 나중에 다 그리면 알려주는 것과 같아요.

### Before
- Spring Boot가 FastAPI를 동기 HTTP로 바로 호출했습니다.
- AI 응답이 몇 초 걸리면 서버 연결이 그 시간 동안 묶였습니다.

### After
- 음성 요청은 Kafka에 넣고 바로 `202 Accepted`를 반환합니다.
- FastAPI는 Kafka Consumer로 요청을 읽고 기존 AI 추천 로직을 실행합니다.
- 결과는 `voice.completed`로 다시 보내고, Spring Boot가 WebSocket으로 사용자별 `/queue/voice-result`에 푸시합니다.

### 면접 답변 핵심
- "AI처럼 느린 작업은 요청/응답 모델보다 메시지 큐가 훨씬 잘 맞습니다."

## 7. 모니터링 / 장애 대응

### 6세 비유
- 자동차 계기판처럼 속도, 온도, 기름 양을 계속 보고 이상하면 바로 멈추는 것과 같아요.

### Before
- 장애가 나면 로그를 뒤져야 했고, 병목을 늦게 알 수 있었습니다.

### After
- Micrometer, Prometheus, Grafana로 HTTP 지표, 캐시, Kafka 흐름, 애플리케이션 메트릭을 볼 수 있게 했습니다.
- Resilience4j Circuit Breaker로 외부 AI/결제 호출 장애가 전체 서비스로 번지지 않게 막았습니다.

### 면접 답변 핵심
- "기능 구현만 한 게 아니라, 운영 중에 어디가 느리고 어디가 실패하는지 보이는 상태까지 만들었습니다."
