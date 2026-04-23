-- =====================================================
-- V4: Outbox 패턴 — Exactly-Once 이벤트 발행 보장
-- 비즈니스 트랜잭션과 이벤트 저장을 원자적으로 처리
-- =====================================================

CREATE TABLE outbox_event (
    id             BIGSERIAL    PRIMARY KEY,
    aggregate_id   VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(50)  NOT NULL,
    event_type     VARCHAR(50)  NOT NULL,
    partition_key  VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- OutboxPublisher 폴링 쿼리 최적화
-- WHERE published = false ORDER BY created_at ASC
CREATE INDEX idx_outbox_published_created
    ON outbox_event(published, created_at)
    WHERE published = false;   -- Partial Index: 미발행 이벤트만 인덱싱
