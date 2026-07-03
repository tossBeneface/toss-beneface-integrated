package com.app.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus, val errorCode: String, val message: String) {

    // 공통 오류
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G-001", "올바르지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G-002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G-003", "서버 내부 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "G-004", "요청한 리소스를 찾을 수 없습니다."),

    // 인증 관련 오류들
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A-001", "토큰이 만료되었습니다."),
    NOT_VALID_TOKEN(HttpStatus.UNAUTHORIZED, "A-002", "해당 토큰은 유효한 토큰이 아닙니다."),
    NOT_EXISTS_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "A-003", "Authorization Header가 비어있습니다."),
    NOT_VALID_BEARER_GRANT_TYPE(HttpStatus.UNAUTHORIZED, "A-004", "인증 타입이 Bearer 타입이 아닙니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A-005", "해당 REFRESH TOKEN은 존재하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A-006", "해당 REFRESH TOKEN은 만료됐습니다."),
    NOT_ACCESS_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "A-007", "해당 토큰은 ACCESS TOKEN이 아닙니다."),
    // 인가(권한) 관련 오류
    FORBIDDEN_ADMIN(HttpStatus.FORBIDDEN, "A-008", "관리자 권한이 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A-009", "접근 권한이 없습니다."),

    // 회원
    ALREADY_REGISTERED_MEMBER(HttpStatus.BAD_REQUEST, "M-001", "이미 가입된 회원입니다."),
    MEMBER_NOT_EXIST(HttpStatus.BAD_REQUEST, "M-002", "해당 회원은 존재하지 않습니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "M-003", "이메일 주소가 잘못되었습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "M-004", "비밀번호가 틀렸습니다."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "M-005", "회원 온보딩이 이미 완료되었습니다."),
    INVALID_ONBOARDING_STEP(HttpStatus.BAD_REQUEST, "M-006", "회원 온보딩 단계가 올바르지 않습니다."),

    // 결제
    INSUFFICIENT_BUDGET(HttpStatus.BAD_REQUEST, "P-001", "잔액이 부족합니다."),
    PAYMENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P-002", "결제 처리 중 오류가 발생했습니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "P-003", "해당 customerKey에 대한 billing key가 없습니다."),

    // 카드
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "C-001", "해당 카드를 찾을 수 없습니다."),

    // 얼굴 등록
    FACE_NOT_FOUND(HttpStatus.NOT_FOUND, "F-001", "해당 얼굴 데이터를 찾을 수 없습니다."),

    // 입력값 검증
    EMPTY_REQUEST_TEXT(HttpStatus.BAD_REQUEST, "V-001", "전송된 텍스트가 비어 있습니다."),
    EMPTY_BRAND(HttpStatus.BAD_REQUEST, "V-002", "brand(카페명)이 비어 있습니다."),

    // 외부 API
    FASTAPI_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E-001", "FastAPI 호출 중 오류가 발생했습니다."),
    ;
}
