package com.app.global.resolver.memberInfo

import com.app.auth.infra.security.AuthenticatedMemberContext
import com.app.auth.infra.security.AuthenticatedMemberContextResolver
import com.app.domain.member.constant.Role
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest

class MemberInfoArgumentResolverTest {

    private val authenticatedMemberContextResolver = mockk<AuthenticatedMemberContextResolver>()
    private val resolver = MemberInfoArgumentResolver(authenticatedMemberContextResolver)
    private val methodParameter = MethodParameter(
        TestController::class.java.getDeclaredMethod("endpoint", MemberInfoDto::class.java),
        0
    )

    @Test
    fun `resolves member info from authenticated member context`() {
        every {
            authenticatedMemberContextResolver.resolve(any<HttpServletRequest>())
        } returns AuthenticatedMemberContext(7L, Role.ADMIN)

        val result = resolver.resolveArgument(
            methodParameter,
            null,
            ServletWebRequest(MockHttpServletRequest()),
            null
        ) as MemberInfoDto

        assertEquals(7L, result.memberId)
        assertEquals(Role.ADMIN, result.role)
    }

    @Test
    fun `supports member info annotated dto parameter`() {
        assertTrue(resolver.supportsParameter(methodParameter))
    }

    private class TestController {
        @Suppress("unused", "UNUSED_PARAMETER")
        fun endpoint(@MemberInfo memberInfoDto: MemberInfoDto) {
        }
    }
}
