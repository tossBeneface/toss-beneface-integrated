-- =====================================================
-- V3: 고급 인덱스 — Partial Index + Covering Index
-- EXPLAIN ANALYZE 분석 결과 기반 추가 최적화
-- =====================================================

-- ─────────────────────────────────────────────────────
-- Partial Index: DONE 상태 결제만 인덱싱
-- 정산 배치 및 매출 집계 쿼리 최적화
-- WHERE status = 'DONE' 조건이 있는 쿼리에서만 선택됨
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_payment_done_approved
    ON payment(approved_at)
    WHERE status = 'DONE';

-- ─────────────────────────────────────────────────────
-- Covering Index: 결제 목록 조회 (Index Only Scan 유도)
-- SELECT id, payment_key, total_amount, status, approved_at
-- FROM payment WHERE member_id = ? ORDER BY approved_at DESC
-- → 힙 접근 없이 인덱스만으로 응답 가능
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_payment_member_covering
    ON payment(member_id, approved_at DESC)
    INCLUDE (payment_key, total_amount, status);

-- ─────────────────────────────────────────────────────
-- Partial Index: 활성 회원만 인덱싱
-- 탈퇴/정지 회원은 조회 대상 제외 → 인덱스 크기 절감
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_member_active_email
    ON member(email)
    WHERE member_status = 'ACTIVE';

-- ─────────────────────────────────────────────────────
-- Covering Index: QnA 목록 조회
-- SELECT qna_board_id, title, content_status, created_at
-- FROM qna_board WHERE content_status = 'OPEN' ORDER BY created_at DESC
-- ─────────────────────────────────────────────────────
CREATE INDEX idx_qna_board_status_covering
    ON qna_board(content_status, created_at DESC)
    INCLUDE (qna_board_id, title);
