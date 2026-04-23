package com.app.global.error

import com.app.global.error.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BindException::class)
    protected fun handleBindException(e: BindException): ResponseEntity<ErrorResponse> {
        log.error("handleBindException", e)
        val errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.httpStatus)
            .body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error("handleMethodArgumentNotValidException", e)
        val errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.httpStatus)
            .body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        log.error("handleMethodArgumentTypeMismatchException", e)
        val errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.message ?: "")
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.httpStatus)
            .body(errorResponse)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        log.error("handleHttpRequestMethodNotSupportedException", e)
        val errorResponse = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.error("handleHttpMessageNotReadableException", e)
        val errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문을 읽을 수 없습니다.")
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.error("BusinessException [{}]: {}", e.errorCode.errorCode, e.message)
        val errorResponse = ErrorResponse.of(e.errorCode, e.message ?: "")
        return ResponseEntity.status(e.errorCode.httpStatus)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Exception", e)
        val errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus).body(errorResponse)
    }
}
