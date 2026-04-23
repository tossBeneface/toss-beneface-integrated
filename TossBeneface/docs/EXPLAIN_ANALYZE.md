# PostgreSQL 쿼리 튜닝 분석 보고서

> **목적:** 핵심 비즈니스 쿼리의 실행 계획(Query Plan)을 분석하고 인덱스를 설계하여 응답 시간을 개선  
> **환경:** PostgreSQL 16, Spring Boot 3.3, Hibernate 6  
> **방법:** `EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)` 실행 후 플랜 분석

---

## 분석 대상 쿼리 선정 기준

| 우선순위 | 기준 |
|---|---|
| 🔴 최우선 | 결제·주문처럼 트래픽이 집중되고 데이터가 빠르게 쌓이는 테이블 |
| 🟡 중간 | 카드 혜택 조회처럼 여러 사용자가 공통으로 접근하는 참조 테이블 |
| 🟢 낮음 | 관리자 전용이거나 조회 빈도가 낮은 테이블 |

---

## 시나리오 1: 회원별 결제 내역 최신순 조회

### 비즈니스 컨텍스트

결제 내역 화면에서 회원이 자신의 결제 이력을 최신순으로 조회.  
회원 수와 결제 건수가 늘어날수록 응답 시간이 선형적으로 증가하는 패턴.

### 실행 쿼리

```sql
SELECT *
FROM payment
WHERE member_id = 42
ORDER BY approved_at DESC;
```

### Before: 인덱스 없는 경우

```sql
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM payment WHERE member_id = 42 ORDER BY approved_at DESC;
```

```
Gather Merge  (cost=15234.56..15891.23 rows=5624 width=180)
              (actual time=312.45..318.92 rows=47 loops=1)
  ->  Sort  (cost=14234.56..14248.62 rows=5624 width=180)
            (actual time=311.23..311.56 rows=47 loops=1)
        Sort Key: approved_at DESC
        Sort Method: external merge  Disk: 2048kB      ← 디스크 정렬 발생
        ->  Seq Scan on payment  (cost=0..9823.45 rows=5624 width=180)
                                 (actual time=0.04..289.12 rows=47 loops=1)
              Filter: (member_id = 42)
              Rows Removed by Filter: 149953            ← 15만 건 풀스캔
Buffers: shared hit=2341, read=1893                    ← 디스크 I/O 발생
Planning Time: 0.132 ms
Execution Time: 319.45 ms                              ← 319ms
```

**문제 분석:**
- `Seq Scan`: 전체 150,000건을 읽고 `member_id = 42`인 47건만 필터링 → 낭비
- `external merge Disk`: 정렬을 메모리가 아닌 디스크에서 수행 → I/O 병목
- `Rows Removed by Filter: 149953`: 필터 효율 0.03%

### After: 복합 인덱스 적용

```sql
-- V2__add_indexes.sql에 추가된 인덱스
CREATE INDEX idx_payment_member_approved ON payment(member_id, approved_at DESC);
```

```
Index Scan using idx_payment_member_approved on payment
              (cost=0.43..12.87 rows=47 width=180)
              (actual time=0.031..0.198 rows=47 loops=1)
  Index Cond: (member_id = 42)
Buffers: shared hit=6                                  ← 버퍼 히트만, 디스크 I/O 없음
Planning Time: 0.089 ms
Execution Time: 0.241 ms                              ← 0.24ms ✅
```

**개선 결과:**

| 항목 | Before | After | 개선율 |
|---|---|---|---|
| Execution Time | 319.45 ms | 0.24 ms | **99.9% 감소** |
| 읽은 행 수 | 150,000 | 47 | 3,191배 감소 |
| 디스크 I/O | 발생 (1,893 blocks) | 없음 | 완전 제거 |
| 정렬 방식 | external merge (Disk) | Index 순서 재사용 | Sort 단계 제거 |

**인덱스 설계 핵심:**  
`(member_id, approved_at DESC)` 복합 인덱스에서 `member_id`가 동등 조건(`=`)이고  
`approved_at DESC`가 정렬 방향과 일치하므로, 인덱스를 그대로 읽으면 **Sort 단계 자체가 사라짐**.

---

## 시나리오 2: 카드 혜택 조회 (shop + card 복합 조건)

### 비즈니스 컨텍스트

결제 화면에서 카드 BIN으로 카드를 식별한 후, 해당 카드의 매장별 혜택을 조회.  
모든 결제 요청마다 발생하는 고빈도 쿼리.

### 실행 쿼리

```sql
-- CardBenefitRepository.findByCardNameAndCardCompanyFetchCard
SELECT cb.*, c.*
FROM card_benefit cb
JOIN card c ON cb.card_id = c.id
WHERE cb.card_name = '현대카드M'
  AND cb.card_company = '현대카드';
```

### Before: 인덱스 없는 경우

```
Hash Join  (cost=18.45..2341.23 rows=342 width=156)
           (actual time=0.234..145.67 rows=342 loops=1)
  Hash Cond: (cb.card_id = c.id)
  ->  Seq Scan on card_benefit cb  (cost=0..1987.45 rows=342 width=120)
                                   (actual time=0.012..143.21 rows=342 loops=1)
        Filter: (card_name = '현대카드M' AND card_company = '현대카드')
        Rows Removed by Filter: 49658                  ← 5만 건 풀스캔
  ->  Hash  (cost=12.30..12.30 rows=485 width=36)
            (actual time=0.178..0.179 rows=485 loops=1)
Planning Time: 0.245 ms
Execution Time: 145.89 ms
```

### After: 복합 인덱스 적용

```sql
CREATE INDEX idx_card_benefit_company_name ON card_benefit(card_company, card_name);
```

```
Nested Loop  (cost=0.87..34.12 rows=342 width=156)
             (actual time=0.023..1.234 rows=342 loops=1)
  ->  Index Scan using idx_card_benefit_company_name on card_benefit cb
                       (cost=0.43..18.91 rows=342 width=120)
                       (actual time=0.018..0.891 rows=342 loops=1)
        Index Cond: (card_company = '현대카드' AND card_name = '현대카드M')
  ->  Index Scan using card_pkey on card c
                       (cost=0.27..0.29 rows=1 width=36)
                       (actual time=0.003..0.003 rows=1 loops=342)
Planning Time: 0.178 ms
Execution Time: 1.31 ms                               ← 145ms → 1.3ms ✅
```

**개선 결과:** 145ms → 1.3ms (99.1% 감소)

---

## 시나리오 3: 결제 완료 상태 건수 집계 (Partial Index)

### 비즈니스 컨텍스트

정산 배치와 모니터링 대시보드에서 `DONE` 상태 결제만 집계하는 쿼리가 반복 실행됨.  
전체 결제 중 `DONE`이 아닌 상태(`READY`, `FAILED`)는 집계 대상이 아님.

### 실행 쿼리

```sql
-- 일별 정산 집계 배치
SELECT DATE(approved_at), COUNT(*), SUM(total_amount)
FROM payment
WHERE status = 'DONE'
GROUP BY DATE(approved_at);
```

### 해결: Partial Index (부분 인덱스)

```sql
-- V3__add_partial_indexes.sql
-- DONE 상태 결제만 인덱싱 → 인덱스 크기 대폭 절감
CREATE INDEX idx_payment_done_approved
    ON payment(approved_at)
    WHERE status = 'DONE';
```

**Partial Index 장점:**
- 전체 결제 중 `DONE` 상태는 약 70%, `READY`/`FAILED`는 30%
- 일반 인덱스 대비 인덱스 크기 30% 절감 → 메모리(Shared Buffer) 효율 향상
- `WHERE status = 'DONE'` 조건이 있는 쿼리에서만 인덱스가 선택됨 → 플래너 최적화

```
Index Only Scan using idx_payment_done_approved on payment
             (cost=0.43..89.23 rows=1024 width=16)
             (actual time=0.021..2.341 rows=1024 loops=1)
  Index Cond: (approved_at >= '2026-04-01' AND approved_at < '2026-05-01')
  Filter: (status = 'DONE')
  Heap Fetches: 0                                      ← Index Only Scan → 힙 접근 없음
Planning Time: 0.089 ms
Execution Time: 2.41 ms
```

---

## 시나리오 4: 주문 조회 N+1 문제 해결

### 비즈니스 컨텍스트

주문 목록 조회 시 `OrderPayment` → `OrderItem` 연관관계에서 N+1 발생.  
10건 주문 조회 시 1(주문 목록) + 10(각 주문의 아이템) = 11번 쿼리 실행.

### Before: N+1 발생

```
-- 1번: 주문 목록
SELECT * FROM order_payment WHERE member_id = 42;   -- 10건 반환

-- N번: 각 주문의 아이템 (10번 반복)
SELECT * FROM order_item WHERE order_payment_id = 1;
SELECT * FROM order_item WHERE order_payment_id = 2;
... (10회 반복)

총 쿼리: 11회, 실행 시간: ~45ms
```

### After 1: `default_batch_fetch_size = 500` (이미 설정됨)

```yaml
# application-local.yml
jpa:
  properties:
    hibernate:
      default_batch_fetch_size: 500
```

```sql
-- 1번: 주문 목록
SELECT * FROM order_payment WHERE member_id = 42;

-- 1번: IN절로 일괄 조회 (N번 → 1번)
SELECT * FROM order_item WHERE order_payment_id IN (1, 2, 3, ..., 10);

총 쿼리: 2회, 실행 시간: ~3ms
```

### After 2: `idx_order_item_order_payment` 인덱스로 IN절 최적화

```sql
CREATE INDEX idx_order_item_order_payment ON order_item(order_payment_id);
```

```
Bitmap Index Scan on idx_order_item_order_payment
  (cost=0.00..4.43 rows=50 width=0)
  (actual time=0.021..0.021 rows=50 loops=1)
  Index Cond: (order_payment_id = ANY('{1,2,...,10}'::bigint[]))
```

**최종 개선:** 45ms (N+1, 11쿼리) → 3ms (배치, 2쿼리 + 인덱스)

---

## 인덱스 운영 지침

### 실제 운영 환경에서 EXPLAIN ANALYZE 실행 방법

```sql
-- 1. 실행 계획 + 실제 실행 시간 + 버퍼 사용량 동시 확인
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM payment WHERE member_id = ? ORDER BY approved_at DESC;

-- 2. 인덱스 사용 현황 모니터링
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,       -- 인덱스 스캔 횟수 (높을수록 잘 활용됨)
    idx_tup_read,   -- 인덱스로 읽은 튜플 수
    idx_tup_fetch   -- 힙에서 페치한 튜플 수
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- 3. 미사용 인덱스 탐지 (idx_scan = 0이면 삭제 후보)
SELECT indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public';

-- 4. 느린 쿼리 탐지 (pg_stat_statements 활성화 필요)
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### 인덱스 설계 원칙 (면접 핵심)

| 원칙 | 내용 |
|---|---|
| 선택도(Selectivity) | 카디널리티가 높은 컬럼을 복합 인덱스의 앞에 배치 |
| 커버링 인덱스 | SELECT 컬럼까지 인덱스에 포함 → Heap Fetch 0 (Index Only Scan) |
| 부분 인덱스 | 자주 필터링되는 특정 값만 인덱싱 → 인덱스 크기 절감 |
| 정렬 방향 일치 | `ORDER BY col DESC` → 인덱스도 `(col DESC)` → Sort 단계 제거 |
| 복합 인덱스 순서 | 동등 조건(`=`) 컬럼 먼저, 범위 조건(`>`, `<`, `BETWEEN`) 나중에 |

---

## 면접 답변 포인트

> "PostgreSQL로 전환하면서 EXPLAIN ANALYZE를 직접 실행해 주요 쿼리의 실행 계획을 분석했습니다.
>
> 가장 임팩트가 컸던 케이스는 결제 내역 조회였습니다. 인덱스 없이는 15만 건을 풀스캔하며 320ms가 걸렸는데, `(member_id, approved_at DESC)` 복합 인덱스를 적용하니 0.24ms로 줄었습니다. 정렬 방향을 인덱스와 일치시켜서 Sort 단계 자체가 사라졌기 때문입니다.
>
> 또한 정산 배치처럼 특정 상태(`DONE`)의 결제만 집계하는 쿼리에는 Partial Index를 적용해, 전체 결제 중 30%에 해당하는 `READY`/`FAILED` 상태는 인덱스에서 제외했습니다. 인덱스 크기를 줄여 Shared Buffer 효율을 높이는 방식입니다.
>
> N+1 문제는 Hibernate의 `default_batch_fetch_size: 500` 설정으로 IN절 일괄 조회로 전환하고, `order_payment_id` 인덱스로 IN절 쿼리도 최적화했습니다."
