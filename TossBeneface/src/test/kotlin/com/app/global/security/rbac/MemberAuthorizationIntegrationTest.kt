package com.app.global.security.rbac

import com.app.auth.infra.security.JwtTokenProvider
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date

/**
 * 실제 JWT를 발급해 JwtAuthenticationFilter → 메서드 시큐리티(@PreAuthorize)
 * → AccessChecker → Casbin 전체 체인을 종단 검증한다.
 * Docker 불필요: application-test.yml의 H2(PostgreSQL 모드)로 풀 앱 컨텍스트를 부팅한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MemberAuthorizationIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private lateinit var target: Member

    @BeforeEach
    fun setUp() {
        memberRepository.deleteAll()
        target = memberRepository.save(
            Member(
                email = "authz-target@test.com",
                password = "password",
                memberName = "대상",
                phoneNumber = "010-0000-0000",
                gender = Gender.MALE,
                budget = 100000,
                role = Role.USER,
                memberStatus = MemberStatus.ACTIVATE
            )
        )
    }

    private fun bearer(memberId: Long, role: Role): String {
        val token = jwtTokenProvider.createAccessToken(memberId, role, Date(System.currentTimeMillis() + 3_600_000))
        return "Bearer $token"
    }

    @Test
    fun `USER token is forbidden from changing member authority`() {
        mockMvc.perform(
            put("/api/member/${target.memberId}/authority")
                .header(HttpHeaders.AUTHORIZATION, bearer(2L, Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"admin"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `ADMIN token can change member authority`() {
        mockMvc.perform(
            put("/api/member/${target.memberId}/authority")
                .header(HttpHeaders.AUTHORIZATION, bearer(1L, Role.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"admin"}""")
        ).andExpect(status().isOk)

        assertEquals(Role.ADMIN, memberRepository.findById(target.memberId!!).get().role)
    }
}
