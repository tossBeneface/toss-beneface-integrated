package com.app.global.security.rbac

/**
 * 자원 종류별 소유자(memberId)를 조회한다. 빈 이름 = Casbin 자원 타입.
 */
interface ResourceOwnerResolver {
    fun ownerOf(resourceId: Long): Long?
}
