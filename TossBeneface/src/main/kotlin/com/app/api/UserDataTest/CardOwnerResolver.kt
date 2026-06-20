package com.app.api.UserDataTest

import com.app.global.security.rbac.ResourceOwnerResolver
import org.springframework.stereotype.Component

@Component("card")   // 빈 이름 = Casbin 자원 타입 "card"
class CardOwnerResolver(
    private val repository: UserDataTestRepository
) : ResourceOwnerResolver {
    override fun ownerOf(resourceId: Long): Long? = repository.findOwnerMemberIdById(resourceId)
}
