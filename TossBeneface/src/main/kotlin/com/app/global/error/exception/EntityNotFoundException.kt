package com.app.global.error.exception

import com.app.global.error.ErrorCode

class EntityNotFoundException(errorCode: ErrorCode) : BusinessException(errorCode)
