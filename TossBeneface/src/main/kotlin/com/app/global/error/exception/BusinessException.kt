package com.app.global.error.exception

import com.app.global.error.ErrorCode

open class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
