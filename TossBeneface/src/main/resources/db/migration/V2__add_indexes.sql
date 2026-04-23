-- =====================================================
-- V2: 성능 최적화 인덱스
-- EXPLAIN ANALYZE로 검증한 핵심 조회 패턴 기반
-- =====================================================

-- ─────────────────────────────────────────────────────
-- payment: 회원별 결제 내역 최신순 조회
-- SELECT * FROM payment WHERE member_id = ? ORDER BY approved_at DESC
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_payment_member_approved
    ON payment(member_id, approved_at DESC);

-- payment: 결제 키로 단건 조회 (Toss 콜백)
CREATE INDEX idx_payment_key
    ON payment(payment_key);

-- ─────────────────────────────────────────────────────
-- card_benefit: 매장별 혜택 조회
-- SELECT * FROM card_benefit WHERE shop = ?
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_card_benefit_shop
    ON card_benefit(shop);

-- card_benefit: 카드사 + 카드명 복합 조회
-- SELECT * FROM card_benefit WHERE card_company = ? AND card_name = ?
CREATE INDEX idx_card_benefit_company_name
    ON card_benefit(card_company, card_name);

-- ─────────────────────────────────────────────────────
-- order_payment: 회원별 주문 내역 조회
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_order_payment_member
    ON order_payment(member_id);

-- ─────────────────────────────────────────────────────
-- order_item: 주문별 항목 조회 (OneToMany 배치 로딩)
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_order_item_order_payment
    ON order_item(order_payment_id);

-- ─────────────────────────────────────────────────────
-- card_bin: BIN 번호로 카드 식별 (카드 번호 앞 6자리)
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_card_bin
    ON card_bin(card_bin);

-- ─────────────────────────────────────────────────────
-- qna_board: 회원별 게시글 조회 + 상태 필터
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_qna_board_member
    ON qna_board(member_id);

CREATE INDEX idx_qna_board_status
    ON qna_board(content_status);

-- ─────────────────────────────────────────────────────
-- product: 카페별 상품 조회 (캐싱 대상 쿼리)
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_product_cafe
    ON product(cafe);

-- ─────────────────────────────────────────────────────
-- basic_menu: 카페별 기본 메뉴 조회
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_basic_menu_cafe
    ON basic_menu(cafe);

-- ─────────────────────────────────────────────────────
-- user_data_test: 회원별 카드 데이터 조회
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_user_data_test_member
    ON user_data_test(member_id);

CREATE INDEX idx_user_data_test_card
    ON user_data_test(card_id);
