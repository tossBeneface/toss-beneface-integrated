-- Initial Schema for gRPC Integration Test
-- Based on TossBeneface/src/main/resources/db/migration/V1__init_schema.sql

-- 1. Create Tables (if not exists)
CREATE TABLE IF NOT EXISTS member (
    member_id   BIGSERIAL PRIMARY KEY,
    email       VARCHAR(50)  UNIQUE NOT NULL,
    password    VARCHAR(200) NOT NULL,
    member_name VARCHAR(20)  NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    gender      VARCHAR(10)  NOT NULL,
    profile_img VARCHAR(200),
    budget      INT          DEFAULT 10000000,
    role        VARCHAR(10)  NOT NULL,
    member_status VARCHAR(25) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255),
    modified_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS payment (
    id           BIGSERIAL PRIMARY KEY,
    payment_key  VARCHAR(255) NOT NULL,
    order_id     VARCHAR(255) NOT NULL,
    order_name   VARCHAR(255) NOT NULL,
    method       VARCHAR(255) NOT NULL,
    total_amount INT          NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    requested_at VARCHAR(255),
    approved_at  VARCHAR(255),
    receipt_url  VARCHAR(255),
    member_id    BIGINT       REFERENCES member(member_id)
);

CREATE TABLE IF NOT EXISTS card (
    id           BIGSERIAL PRIMARY KEY,
    card_name    VARCHAR(255) NOT NULL,
    card_company VARCHAR(255) NOT NULL,
    card_image   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS card_benefit (
    id           BIGSERIAL PRIMARY KEY,
    card_name    VARCHAR(255),
    card_company VARCHAR(255),
    summary      VARCHAR(255),
    shop         VARCHAR(255),
    benefit      INT NOT NULL DEFAULT 0,
    limit_once   INT NOT NULL DEFAULT 0,
    limit_month  INT NOT NULL DEFAULT 0,
    min_pay      INT NOT NULL DEFAULT 0,
    min_per      INT NOT NULL DEFAULT 0,
    monthly      INT NOT NULL DEFAULT 0,
    card_id      BIGINT NOT NULL REFERENCES card(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(255),
    modified_by  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_data_test (
    id             BIGSERIAL PRIMARY KEY,
    member_id      BIGINT       NOT NULL REFERENCES member(member_id),
    card_id        BIGINT       NOT NULL REFERENCES card(id),
    card_name      VARCHAR(255) NOT NULL,
    card_company   VARCHAR(255) NOT NULL,
    card_number    VARCHAR(20)  NOT NULL,
    expiry_date    VARCHAR(10)  NOT NULL,
    cvc            INT          NOT NULL DEFAULT 0,
    pwd            INT          NOT NULL DEFAULT 0,
    last_per       INT          NOT NULL DEFAULT 0,
    now_per        INT          NOT NULL DEFAULT 0,
    card_limit     INT          NOT NULL DEFAULT 0,
    monthly_split  INT          NOT NULL DEFAULT 0,
    accrue_benefit INT          NOT NULL DEFAULT 0,
    date           VARCHAR(255),
    pay_amount     INT,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(255),
    modified_by    VARCHAR(255)
);

-- 2. Clean existing data (for idempotency)
TRUNCATE user_data_test RESTART IDENTITY CASCADE;
TRUNCATE card_benefit RESTART IDENTITY CASCADE;
TRUNCATE card RESTART IDENTITY CASCADE;
TRUNCATE payment RESTART IDENTITY CASCADE;
TRUNCATE member RESTART IDENTITY CASCADE;

-- 3. Insert Seed Data
-- Member 1
INSERT INTO member (member_id, email, password, member_name, phone_number, gender, role, member_status)
VALUES (1, 'test@example.com', 'password', 'TestUser', '010-1234-5678', 'MALE', 'USER', 'ACTIVE');

-- Cards
INSERT INTO card (id, card_name, card_company)
VALUES (1, 'Toss Beneface Card', 'Toss Bank'),
       (2, 'Daily Cashback Card', 'Toss Bank');

-- Card Benefits for "스타벅스"
-- Toss Beneface Card: 50% discount for Starbucks
INSERT INTO card_benefit (card_id, card_name, card_company, shop, benefit, summary, min_pay, min_per)
VALUES (1, 'Toss Beneface Card', 'Toss Bank', '스타벅스', 50, '스타벅스 50% 할인', 1000, 0);

-- Daily Cashback Card: 20% discount for Starbucks
INSERT INTO card_benefit (card_id, card_name, card_company, shop, benefit, summary, min_pay, min_per)
VALUES (2, 'Daily Cashback Card', 'Toss Bank', '스타벅스', 20, '스타벅스 20% 할인', 1000, 0);

-- User Card Data (Member 1 owns both cards)
INSERT INTO user_data_test (member_id, card_id, card_name, card_company, card_number, expiry_date, last_per)
VALUES (1, 1, 'Toss Beneface Card', 'Toss Bank', '1234-5678-9012-3456', '12/28', 100000),
       (1, 2, 'Daily Cashback Card', 'Toss Bank', '9876-5432-1098-7654', '12/28', 100000);

-- 4. Insert Payment History (to test visit_count based progressive benefits)
-- Approved in the current month (April 2026 based on the session date)
INSERT INTO payment (member_id, order_name, total_amount, status, approved_at, payment_key, order_id, method)
VALUES (1, '스타벅스 강남점', 5000, 'DONE', '2026-04-01 10:00:00', 'key1', 'ord1', 'CARD'),
       (1, '스타벅스 역삼점', 4500, 'DONE', '2026-04-10 14:00:00', 'key2', 'ord2', 'CARD');
