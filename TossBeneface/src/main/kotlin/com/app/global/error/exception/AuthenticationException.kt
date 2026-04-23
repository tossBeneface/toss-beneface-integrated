package com.app.global.error.exception

import com.app.global.error.ErrorCode

class AuthenticationException(errorCode: ErrorCode) : BusinessException(errorCode)
