-- =====================================================
-- V1: 전체 초기 스키마
-- 엔티티 의존 순서: 독립 테이블 → 참조 테이블 순으로 생성
-- =====================================================

-- ─────────────────────────────────────────────────────
-- 회원 (member)
-- ─────────────────────────────────────────────────────
CREATE TABLE member (
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

-- ─────────────────────────────────────────────────────
-- QnA 게시판 (qna_board)
-- ─────────────────────────────────────────────────────
CREATE TABLE qna_board (
    qna_board_id   BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    content        TEXT         NOT NULL,
    content_status VARCHAR(15)  NOT NULL,
    member_id      BIGINT       NOT NULL REFERENCES member(member_id),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(255),
    modified_by    VARCHAR(255)
);

-- ─────────────────────────────────────────────────────
-- 댓글 (comment)
-- ─────────────────────────────────────────────────────
CREATE TABLE comment (
    comment_id      BIGSERIAL PRIMARY KEY,
    comment_content TEXT        NOT NULL,
    comment_status  VARCHAR(15) NOT NULL,
    qna_board_id    BIGINT      REFERENCES qna_board(qna_board_id),
    member_id       BIGINT      REFERENCES member(member_id)
);

-- ─────────────────────────────────────────────────────
-- 첨부파일 (attachment)
-- ─────────────────────────────────────────────────────
CREATE TABLE attachment (
    attachment_id BIGSERIAL PRIMARY KEY,
    url           VARCHAR(500) NOT NULL,
    file_path     VARCHAR(500),
    file_type     VARCHAR(50)  NOT NULL,
    file_status   VARCHAR(15)  NOT NULL,
    qna_board_id  BIGINT       REFERENCES qna_board(qna_board_id),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255),
    modified_by   VARCHAR(255)
);

-- ─────────────────────────────────────────────────────
-- 결제 (payment)
-- ─────────────────────────────────────────────────────
CREATE TABLE payment (
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

-- ─────────────────────────────────────────────────────
-- 주문 (order_payment, order_item)
-- ─────────────────────────────────────────────────────
CREATE TABLE order_payment (
    id           BIGSERIAL PRIMARY KEY,
    total_amount INT    NOT NULL DEFAULT 0,
    card_reason  VARCHAR(255),
    member_id    BIGINT REFERENCES member(member_id)
);

CREATE TABLE order_item (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    price            INT          NOT NULL,
    count            INT          NOT NULL,
    order_payment_id BIGINT       REFERENCES order_payment(id)
);

-- ─────────────────────────────────────────────────────
-- 카드 (card, card_bin)
-- ─────────────────────────────────────────────────────
CREATE TABLE card (
    id           BIGSERIAL PRIMARY KEY,
    card_name    VARCHAR(255) NOT NULL,
    card_company VARCHAR(255) NOT NULL,
    card_image   VARCHAR(255)
);

CREATE TABLE card_bin (
    id       BIGSERIAL PRIMARY KEY,
    card_bin VARCHAR(255) NOT NULL,
    card_id  BIGINT       NOT NULL REFERENCES card(id)
);

-- ─────────────────────────────────────────────────────
-- 카드 혜택 (card_benefit)
-- @Table(name = "CARD_BENEFIT") → PostgreSQL에서 card_benefit
-- ─────────────────────────────────────────────────────
CREATE TABLE card_benefit (
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

-- ─────────────────────────────────────────────────────
-- QR 인증 (qr_auth)
-- ─────────────────────────────────────────────────────
CREATE TABLE qr_auth (
    id            BIGSERIAL PRIMARY KEY,
    nonce         VARCHAR(255) NOT NULL,
    authenticated BOOLEAN      NOT NULL DEFAULT FALSE,
    member_id     BIGINT       REFERENCES member(member_id),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255),
    modified_by   VARCHAR(255)
);

-- ─────────────────────────────────────────────────────
-- 상권 분석 (district_flow)
-- ─────────────────────────────────────────────────────
CREATE TABLE district_flow (
    id                     BIGSERIAL PRIMARY KEY,
    store_gender_script    TEXT,
    store_age_script       TEXT,
    store_time_script      TEXT,
    store_day_script       TEXT,
    district_gender_script TEXT,
    district_age_script    TEXT,
    district_time_script   TEXT,
    district_day_script    TEXT,
    created_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by             VARCHAR(255),
    modified_by            VARCHAR(255)
);

-- ─────────────────────────────────────────────────────
-- 얼굴 등록 (face_registration)
-- ─────────────────────────────────────────────────────
CREATE TABLE face_registration (
    id        BIGSERIAL PRIMARY KEY,
    member_id BIGINT       NOT NULL REFERENCES member(member_id),
    image_url VARCHAR(255) NOT NULL
);

-- ─────────────────────────────────────────────────────
-- 상품 (product)
-- ─────────────────────────────────────────────────────
CREATE TABLE product (
    id    BIGSERIAL PRIMARY KEY,
    cafe  VARCHAR(255),
    menu  VARCHAR(255),
    img   VARCHAR(255),
    price INT NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0
);

-- ─────────────────────────────────────────────────────
-- 기본 메뉴 (basic_menu)
-- ─────────────────────────────────────────────────────
CREATE TABLE basic_menu (
    id    BIGSERIAL PRIMARY KEY,
    cafe  VARCHAR(255) NOT NULL,
    img   VARCHAR(255),
    menu  VARCHAR(255) NOT NULL,
    price FLOAT8       NOT NULL,
    stock INT          NOT NULL
);

-- ─────────────────────────────────────────────────────
-- 사용자 카드 데이터 (user_data_test)
-- ─────────────────────────────────────────────────────
CREATE TABLE user_data_test (
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
