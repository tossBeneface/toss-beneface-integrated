package com.app.global.error

import org.springframework.validation.BindingResult

data class ErrorResponse(
    val errorCode: String,
    val errorMessage: String
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                errorCode = errorCode.errorCode,
                errorMessage = errorCode.message
            )
        }

        fun of(errorCode: ErrorCode, errorMessage: String): ErrorResponse {
            return ErrorResponse(
                errorCode = errorCode.errorCode,
                errorMessage = errorMessage
            )
        }

        fun of(errorCode: ErrorCode, bindingResult: BindingResult): ErrorResponse {
            return ErrorResponse(
                errorCode = errorCode.errorCode,
                errorMessage = createErrorMessage(bindingResult)
            )
        }

        fun of(errorCode: String, errorMessage: String): ErrorResponse {
            return ErrorResponse(
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        }

        private fun createErrorMessage(bindingResult: BindingResult): String {
            return bindingResult.fieldErrors.joinToString(", ") { fieldError ->
                "[${fieldError.field}]${fieldError.defaultMessage}"
            }
        }
    }
}
