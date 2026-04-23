CREATE TABLE daily_settlement (
    id           BIGSERIAL PRIMARY KEY,
    target_date  DATE         NOT NULL,
    payment_id   BIGINT       NOT NULL,
    payment_key  VARCHAR(255) NOT NULL,
    order_id     VARCHAR(255) NOT NULL,
    member_id    BIGINT       NOT NULL REFERENCES member(member_id),
    total_amount INT          NOT NULL,
    approved_at  VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_daily_settlement_target_payment UNIQUE (target_date, payment_id)
);
