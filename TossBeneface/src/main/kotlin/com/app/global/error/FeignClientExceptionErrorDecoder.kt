package com.app.global.error

import feign.FeignException
import feign.Response
import feign.RetryableException
import feign.codec.ErrorDecoder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.util.*

class FeignClientExceptionErrorDecoder : ErrorDecoder {
    private val log = LoggerFactory.getLogger(FeignClientExceptionErrorDecoder::class.java)
    private val errorDecoder = ErrorDecoder.Default()

    override fun decode(methodKey: String, response: Response): Exception {
        log.error("{} 요청 실패, status : {}, response : {}", methodKey, response.status(), response.reason())
        val exception = FeignException.errorStatus(methodKey, response)
        val httpStatus = HttpStatus.valueOf(response.status())

        if (httpStatus.is5xxServerError) {
            return RetryableException(
                response.status(),
                exception.message,
                response.request().httpMethod(),
                exception,
                null as Date?,
                response.request()
            )
        }
        return errorDecoder.decode(methodKey, response)
    }
}
