# RBAC with jCasbin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기존 Spring Security/JWT 위에 jCasbin 기반 RBAC + 소유권 인가를 도입하고, 정책은 정적 파일로 시작하되 어댑터 교체만으로 DB 전환이 가능하도록 경계를 잡는다.

**Architecture:** 인증(JWT 필터)은 그대로 두고 "인가 결정"만 Casbin에 위임한다. 컨트롤러는 `@PreAuthorize("@authz.can(...)")`로 `AccessChecker` 파사드를 호출하고, 파사드가 Casbin `Enforcer`(role→permission)와 `ResourceOwnerResolver`(소유자 조회)를 조합한다. 정책은 `model.conf`(scope 컬럼 방식) + `policy.csv`, `FileAdapter`로 로드한다.

**Tech Stack:** Kotlin 1.9.23, Spring Boot 3.3.5, Spring Security 6, jCasbin, JUnit5 + MockK.

**Spec:** `docs/superpowers/specs/2026-06-20-rbac-casbin-design.md`

---

## File Structure

**Phase 1 — 기반 + role-only/소유권 양 경로**
- Modify `TossBeneface/build.gradle` — jcasbin 의존성
- Create `TossBeneface/src/main/resources/rbac/model.conf` — 인가 모델(scope 방식)
- Create `TossBeneface/src/main/resources/rbac/policy.csv` — 정책
- Create `TossBeneface/src/main/kotlin/com/app/global/config/CasbinConfig.kt` — `Enforcer` 빈
- Create `TossBeneface/src/main/kotlin/com/app/global/security/rbac/ResourceOwnerResolver.kt` — 소유자 조회 인터페이스
- Create `TossBeneface/src/main/kotlin/com/app/global/security/rbac/AccessChecker.kt` — `@authz` 파사드
- Modify `TossBeneface/src/main/kotlin/com/app/auth/infra/security/AuthenticatedMemberContextResolver.kt` — `currentContextOrNull()` 공개
- Modify `TossBeneface/src/main/kotlin/com/app/global/config/SecurityConfig.kt` — `@EnableMethodSecurity`
- Modify member authority 흐름 4파일 (command/usecase/mapper/controller) + 테스트
- Modify card delete 흐름 (repo/resolver/service/controller) + 테스트

**Phase 2 — 나머지 도메인 점검**
- Modify `TossBeneface/src/main/kotlin/com/app/api/cardbenefit/controller/CardBenefitController.kt`
- 조사: payment 확정 흐름 self-scoped 검증

**Phase 3 — 정리 + 통합 테스트**
- Create 통합 테스트, 전체 회귀

> 모든 경로는 저장소 루트 기준. Gradle 명령은 `TossBeneface/` 에서 실행한다(`cd TossBeneface`).

---

## Phase 1 — Foundation + Both Paths

### Task 1: jcasbin 의존성 추가

**Files:**
- Modify: `TossBeneface/build.gradle`

- [ ] **Step 1: 의존성 한 줄 추가**

`build.gradle`의 `dependencies { ... }` 블록에서 Kotlin 의존성 근처에 추가:

```groovy
	// RBAC (Casbin)
	implementation 'org.casbin:jcasbin:1.55.0'
```

> 참고: 1.55.0은 실재 릴리스 좌표다. 구현 시 `https://mvnrepository.com/artifact/org.casbin/jcasbin` 에서 최신 1.x 확인 후 필요하면 상향한다.

- [ ] **Step 2: 의존성 해석 확인**

Run: `cd TossBeneface && ./gradlew dependencies --configuration runtimeClasspath | grep casbin`
Expected: `org.casbin:jcasbin:1.55.0` 라인이 출력됨

- [ ] **Step 3: Commit**

```bash
git add TossBeneface/build.gradle
git commit -m "build: add jcasbin dependency for RBAC"
```

---

### Task 2: Casbin 모델 & 정책 파일 + 정책 단위 테스트

**Files:**
- Create: `TossBeneface/src/main/resources/rbac/model.conf`
- Create: `TossBeneface/src/main/resources/rbac/policy.csv`
- Test: `TossBeneface/src/test/kotlin/com/app/global/security/rbac/CasbinPolicyTest.kt`

- [ ] **Step 1: 실패하는 정책 테스트 작성**

`TossBeneface/src/test/kotlin/com/app/global/security/rbac/CasbinPolicyTest.kt`:

```kotlin
package com.app.global.security.rbac

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CasbinPolicyTest {

    private val enforcer = Enforcer(
        "src/main/resources/rbac/model.conf",
        "src/main/resources/rbac/policy.csv"
    )

    @Test
    fun `admin can manage member`() {
        assertTrue(enforcer.enforce("ADMIN", "member", "manage", "any"))
    }

    @Test
    fun `user cannot manage member`() {
        assertFalse(enforcer.enforce("USER", "member", "manage", "any"))
    }

    @Test
    fun `admin can manage benefit`() {
        assertTrue(enforcer.enforce("ADMIN", "benefit", "manage", "any"))
    }

    @Test
    fun `user can delete own card`() {
        assertTrue(enforcer.enforce("USER", "card", "delete", "own"))
    }

    @Test
    fun `user cannot delete any card`() {
        assertFalse(enforcer.enforce("USER", "card", "delete", "any"))
    }

    @Test
    fun `admin can delete any card via inheritance and explicit policy`() {
        assertTrue(enforcer.enforce("ADMIN", "card", "delete", "any"))
        assertTrue(enforcer.enforce("ADMIN", "card", "delete", "own"))
    }
}
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.security.rbac.CasbinPolicyTest"`
Expected: FAIL — model.conf/policy.csv 파일이 없어 Enforcer 생성 시 예외

- [ ] **Step 3: model.conf 작성**

`TossBeneface/src/main/resources/rbac/model.conf`:

```ini
[request_definition]
r = sub, obj, act, scope

[policy_definition]
p = sub, obj, act, scope

[role_definition]
g = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act && (p.scope == "any" || p.scope == r.scope)
```

- [ ] **Step 4: policy.csv 작성**

`TossBeneface/src/main/resources/rbac/policy.csv`:

```csv
g, ADMIN, OWNER
g, OWNER, USER
p, ADMIN, member, manage, any
p, ADMIN, benefit, manage, any
p, USER, card, delete, own
p, ADMIN, card, delete, any
```

- [ ] **Step 5: 테스트 실행 → 통과 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.security.rbac.CasbinPolicyTest"`
Expected: PASS (6 tests)

- [ ] **Step 6: Commit**

```bash
git add TossBeneface/src/main/resources/rbac TossBeneface/src/test/kotlin/com/app/global/security/rbac/CasbinPolicyTest.kt
git commit -m "feat: add casbin rbac model and policy with tests"
```

---

### Task 3: Enforcer 빈 (CasbinConfig)

**Files:**
- Create: `TossBeneface/src/main/kotlin/com/app/global/config/CasbinConfig.kt`
- Test: `TossBeneface/src/test/kotlin/com/app/global/config/CasbinConfigTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성** (classpath 리소스 로딩 + 어댑터가 동작하는지 검증)

`TossBeneface/src/test/kotlin/com/app/global/config/CasbinConfigTest.kt`:

```kotlin
package com.app.global.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CasbinConfigTest {

    private val enforcer = CasbinConfig().enforcer()

    @Test
    fun `loads model and policy from classpath`() {
        assertTrue(enforcer.enforce("ADMIN", "member", "manage", "any"))
        assertFalse(enforcer.enforce("USER", "member", "manage", "any"))
    }
}
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.config.CasbinConfigTest"`
Expected: FAIL — `CasbinConfig` 클래스 없음(컴파일 실패)

- [ ] **Step 3: CasbinConfig 구현**

`TossBeneface/src/main/kotlin/com/app/global/config/CasbinConfig.kt`:

```kotlin
package com.app.global.config

import org.casbin.jcasbin.main.Enforcer
import org.casbin.jcasbin.model.Model
import org.casbin.jcasbin.persist.file_adapter.FileAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files

@Configuration
class CasbinConfig {

    @Bean
    fun enforcer(): Enforcer {
        val model = Model()
        model.loadModelFromText(readClasspath("rbac/model.conf"))

        // fat jar 내부에서는 classpath 리소스가 파일 경로로 해석되지 않으므로
        // 정책을 임시 파일로 복사해 FileAdapter에 전달한다.
        val policyFile = Files.createTempFile("casbin-policy", ".csv").toFile().apply {
            deleteOnExit()
            writeText(readClasspath("rbac/policy.csv"))
        }

        // ↓↓ DB 전환 시 이 한 줄만 JDBCAdapter(dataSource)로 교체한다 (정책 저장 경계)
        val adapter = FileAdapter(policyFile.absolutePath)

        val enforcer = Enforcer(model, adapter)
        enforcer.loadPolicy()
        return enforcer
    }

    private fun readClasspath(path: String): String =
        ClassPathResource(path).inputStream.bufferedReader().use { it.readText() }
}
```

- [ ] **Step 4: 테스트 실행 → 통과 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.config.CasbinConfigTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/global/config/CasbinConfig.kt TossBeneface/src/test/kotlin/com/app/global/config/CasbinConfigTest.kt
git commit -m "feat: add casbin enforcer bean loading policy from classpath"
```

---

### Task 4: SecurityContext 컨텍스트 공개 메서드

**Files:**
- Modify: `TossBeneface/src/main/kotlin/com/app/auth/infra/security/AuthenticatedMemberContextResolver.kt`
- Test: `TossBeneface/src/test/kotlin/com/app/auth/infra/security/AuthenticatedMemberContextResolverTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

`TossBeneface/src/test/kotlin/com/app/auth/infra/security/AuthenticatedMemberContextResolverTest.kt`:

```kotlin
package com.app.auth.infra.security

import com.app.domain.member.constant.Role
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class AuthenticatedMemberContextResolverTest {

    private val resolver = AuthenticatedMemberContextResolver(mockk(), mockk())

    @AfterEach
    fun tearDown() = SecurityContextHolder.clearContext()

    @Test
    fun `returns context from security context`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                7L, "token", listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
            )

        val ctx = resolver.currentContextOrNull()

        assertEquals(7L, ctx?.memberId)
        assertEquals(Role.ADMIN, ctx?.role)
    }

    @Test
    fun `returns null when no authentication`() {
        SecurityContextHolder.clearContext()
        assertNull(resolver.currentContextOrNull())
    }
}
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.auth.infra.security.AuthenticatedMemberContextResolverTest"`
Expected: FAIL — `currentContextOrNull` 메서드 없음(컴파일 실패)

- [ ] **Step 3: 공개 메서드 추가**

`AuthenticatedMemberContextResolver.kt`에서 `resolveFromSecurityContext()` 바로 위(클래스 본문 안)에 추가:

```kotlin
    fun currentContextOrNull(): AuthenticatedMemberContext? = resolveFromSecurityContext()
```

> 기존 `private fun resolveFromSecurityContext()`는 그대로 둔다. 위 공개 메서드는 SecurityContext만 사용하므로 HttpServletRequest가 필요 없다.

- [ ] **Step 4: 테스트 실행 → 통과 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.auth.infra.security.AuthenticatedMemberContextResolverTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/auth/infra/security/AuthenticatedMemberContextResolver.kt TossBeneface/src/test/kotlin/com/app/auth/infra/security/AuthenticatedMemberContextResolverTest.kt
git commit -m "feat: expose security context resolution for access checks"
```

---

### Task 5: AccessChecker 파사드 + ResourceOwnerResolver 인터페이스

**Files:**
- Create: `TossBeneface/src/main/kotlin/com/app/global/security/rbac/ResourceOwnerResolver.kt`
- Create: `TossBeneface/src/main/kotlin/com/app/global/security/rbac/AccessChecker.kt`
- Test: `TossBeneface/src/test/kotlin/com/app/global/security/rbac/AccessCheckerTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

`TossBeneface/src/test/kotlin/com/app/global/security/rbac/AccessCheckerTest.kt`:

```kotlin
package com.app.global.security.rbac

import com.app.auth.infra.security.AuthenticatedMemberContext
import com.app.auth.infra.security.AuthenticatedMemberContextResolver
import com.app.domain.member.constant.Role
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Test

class AccessCheckerTest {

    private val enforcer = mockk<Enforcer>()
    private val contextResolver = mockk<AuthenticatedMemberContextResolver>()
    private val cardResolver = mockk<ResourceOwnerResolver>()
    private val accessChecker = AccessChecker(
        enforcer, contextResolver, mapOf("card" to cardResolver)
    )

    private fun loginAs(memberId: Long, role: Role) {
        every { contextResolver.currentContextOrNull() } returns AuthenticatedMemberContext(memberId, role)
    }

    @Test
    fun `role-only allow when enforcer grants any`() {
        loginAs(1L, Role.ADMIN)
        every { enforcer.enforce("ADMIN", "member", "manage", "any") } returns true
        assertTrue(accessChecker.can("member", "manage"))
    }

    @Test
    fun `deny when not authenticated`() {
        every { contextResolver.currentContextOrNull() } returns null
        assertFalse(accessChecker.can("member", "manage"))
    }

    @Test
    fun `ownership allow when owner matches`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns true
        every { cardResolver.ownerOf(10L) } returns 5L
        assertTrue(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `ownership deny when owner differs`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns true
        every { cardResolver.ownerOf(10L) } returns 99L
        assertFalse(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `ownership deny when no permission at all`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns false
        assertFalse(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `any scope overrides ownership without resolver`() {
        loginAs(1L, Role.ADMIN)
        every { enforcer.enforce("ADMIN", "card", "delete", "any") } returns true
        assertTrue(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `ownership deny when resource not found`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns true
        every { cardResolver.ownerOf(10L) } returns null
        assertFalse(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `throws when no resolver registered for owned resource`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "order", "cancel", "any") } returns false
        every { enforcer.enforce("USER", "order", "cancel", "own") } returns true
        assertThrows(IllegalStateException::class.java) {
            accessChecker.can("order", "cancel", 10L)
        }
    }
}
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.security.rbac.AccessCheckerTest"`
Expected: FAIL — `AccessChecker`/`ResourceOwnerResolver` 클래스 없음(컴파일 실패)

- [ ] **Step 3: ResourceOwnerResolver 인터페이스 작성**

`TossBeneface/src/main/kotlin/com/app/global/security/rbac/ResourceOwnerResolver.kt`:

```kotlin
package com.app.global.security.rbac

/**
 * 자원 종류별 소유자(memberId)를 조회한다. 빈 이름 = Casbin 자원 타입.
 */
interface ResourceOwnerResolver {
    fun ownerOf(resourceId: Long): Long?
}
```

- [ ] **Step 4: AccessChecker 작성**

`TossBeneface/src/main/kotlin/com/app/global/security/rbac/AccessChecker.kt`:

```kotlin
package com.app.global.security.rbac

import com.app.auth.infra.security.AuthenticatedMemberContextResolver
import org.casbin.jcasbin.main.Enforcer
import org.springframework.stereotype.Component

/**
 * 컨트롤러의 단일 인가 창구(@PreAuthorize("@authz.can(...)")).
 * Casbin enforcer(role→permission)와 소유자 비교를 캡슐화한다.
 */
@Component("authz")
class AccessChecker(
    private val enforcer: Enforcer,
    private val contextResolver: AuthenticatedMemberContextResolver,
    private val ownerResolvers: Map<String, ResourceOwnerResolver>
) {

    /** 자원 id 없는 role-only 검사. */
    fun can(resource: String, action: String): Boolean {
        val ctx = contextResolver.currentContextOrNull() ?: return false
        return enforcer.enforce(ctx.role.name, resource, action, "any")
    }

    /** 소유권 포함 검사. any 권한이면 즉시 허용, own 권한이면 소유자 일치 시 허용. */
    fun can(resource: String, action: String, resourceId: Long): Boolean {
        val ctx = contextResolver.currentContextOrNull() ?: return false
        if (enforcer.enforce(ctx.role.name, resource, action, "any")) return true
        if (!enforcer.enforce(ctx.role.name, resource, action, "own")) return false
        val resolver = ownerResolvers[resource]
            ?: throw IllegalStateException("No ResourceOwnerResolver registered for resource '$resource'")
        val ownerId = resolver.ownerOf(resourceId) ?: return false
        return ownerId == ctx.memberId
    }
}
```

- [ ] **Step 5: 테스트 실행 → 통과 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.security.rbac.AccessCheckerTest"`
Expected: PASS (8 tests)

- [ ] **Step 6: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/global/security/rbac
git commit -m "feat: add access checker facade for casbin authorization"
```

---

### Task 6: 메서드 시큐리티 활성화

**Files:**
- Modify: `TossBeneface/src/main/kotlin/com/app/global/config/SecurityConfig.kt`

- [ ] **Step 1: @EnableMethodSecurity 추가**

`SecurityConfig.kt`의 `import` 구역에 추가:

```kotlin
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
```

클래스 선언의 `@Configuration` 바로 아래에 추가:

```kotlin
@Configuration
@EnableMethodSecurity
class SecurityConfig(
```

> `@EnableMethodSecurity`는 기본적으로 `@PreAuthorize`를 활성화한다(prePostEnabled = true 기본값).

- [ ] **Step 2: 컴파일 확인**

Run: `cd TossBeneface && ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/global/config/SecurityConfig.kt
git commit -m "feat: enable method security for preauthorize"
```

---

### Task 7: member:manage — 권한 변경 가드를 RBAC로 이관

**Files:**
- Modify: `TossBeneface/src/main/kotlin/com/app/member/application/dto/MemberUpdateCommands.kt`
- Modify: `TossBeneface/src/main/kotlin/com/app/member/application/usecase/ChangeMemberAuthorityUseCase.kt`
- Modify: `TossBeneface/src/main/kotlin/com/app/api/member/mapper/MemberCommandRequestMapper.kt`
- Modify: `TossBeneface/src/main/kotlin/com/app/api/member/controller/MemberCommandController.kt`
- Modify: `TossBeneface/src/test/kotlin/com/app/member/application/usecase/ChangeMemberAuthorityUseCaseTest.kt`

- [ ] **Step 1: 테스트를 새 기대값으로 수정** (requesterRole 가드는 AccessChecker로 이동했으므로 유스케이스는 순수 도메인 로직)

`ChangeMemberAuthorityUseCaseTest.kt` 전체를 아래로 교체:

```kotlin
package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.ChangeMemberAuthorityCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChangeMemberAuthorityUseCaseTest {

    private val memberService = mockk<MemberService>()
    private val useCase = ChangeMemberAuthorityUseCase(memberService)

    @Test
    fun `changes member authority`() {
        val member = createMember()
        every { memberService.findMemberById(42L) } returns member
        every { memberService.updateMember(any()) } answers { firstArg() }

        val result = useCase.change(
            ChangeMemberAuthorityCommand(
                memberId = 42L,
                role = "admin"
            )
        )

        assertEquals(42L, result.memberId)
        assertEquals(Role.ADMIN, result.role)
        assertEquals(Role.ADMIN, member.role)
        verify(exactly = 1) { memberService.updateMember(member) }
    }

    private fun createMember() = Member(
        memberId = 42L,
        email = "member@example.com",
        password = "password",
        memberName = "Member",
        phoneNumber = "010-1234-5678",
        gender = Gender.MALE,
        profileImg = "profile.png",
        budget = 10_000,
        role = Role.USER,
        memberStatus = MemberStatus.ACTIVATE
    )
}
```

- [ ] **Step 2: command에서 requesterRole 제거**

`MemberUpdateCommands.kt`:

```kotlin
data class ChangeMemberAuthorityCommand(
    val memberId: Long,
    val role: String
)
```

- [ ] **Step 3: usecase에서 가드 제거**

`ChangeMemberAuthorityUseCase.kt` 전체를 아래로 교체:

```kotlin
package com.app.member.application.usecase

import com.app.domain.member.constant.Role
import com.app.domain.member.model.MemberAuthority
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.ChangeMemberAuthorityCommand
import com.app.member.application.dto.MemberAuthorityResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChangeMemberAuthorityUseCase(
    private val memberService: MemberService
) {

    fun change(command: ChangeMemberAuthorityCommand): MemberAuthorityResult {
        val member = memberService.findMemberById(command.memberId)
        member.changeAuthority(MemberAuthority(Role.from(command.role)))
        return MemberAuthorityResult.from(memberService.updateMember(member))
    }
}
```

- [ ] **Step 4: mapper 시그니처 되돌리기**

`MemberCommandRequestMapper.kt`에서 `import com.app.domain.member.constant.Role` 제거하고, authority toCommand를 아래로 교체:

```kotlin
    fun toCommand(memberId: Long, request: ChangeMemberAuthorityDto.Request): ChangeMemberAuthorityCommand {
        return ChangeMemberAuthorityCommand(
            memberId = memberId,
            role = request.role
        )
    }
```

- [ ] **Step 5: 컨트롤러에 @PreAuthorize 부착, role 전달 제거**

`MemberCommandController.kt`의 import 구역에 추가:

```kotlin
import org.springframework.security.access.prepost.PreAuthorize
```

`changeAuthority` 메서드를 아래로 교체:

```kotlin
    @Operation(
        summary = "회원 권한 변경 API",
        description = "대상 회원의 권한을 변경한다. (ADMIN 전용 — RBAC)"
    )
    @PreAuthorize("@authz.can('member', 'manage')")
    @PutMapping("/{memberId}/authority")
    fun changeAuthority(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: ChangeMemberAuthorityDto.Request
    ): ResponseEntity<ChangeMemberAuthorityDto.Response> {
        val command = memberCommandRequestMapper.toCommand(memberId, request)
        val result = changeMemberAuthorityUseCase.change(command)
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }
```

> `@MemberInfo memberInfoDto` 파라미터는 인증 보장을 위해 유지(미인증 시 리졸버가 막음). role은 더 이상 전달하지 않는다.

- [ ] **Step 6: 테스트 실행 → 통과 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.member.*"`
Expected: PASS — `ChangeMemberAuthorityUseCaseTest` 1건 포함 member 전체 통과

- [ ] **Step 7: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/member TossBeneface/src/main/kotlin/com/app/api/member TossBeneface/src/test/kotlin/com/app/member/application/usecase/ChangeMemberAuthorityUseCaseTest.kt
git commit -m "refactor: enforce member authority change via casbin rbac"
```

---

### Task 8: card:delete — 소유권 검사를 선언적 인가로 이전

**Files:**
- Modify: `TossBeneface/src/main/kotlin/com/app/api/UserDataTest/UserDataTestRepository.kt`
- Create: `TossBeneface/src/main/kotlin/com/app/api/UserDataTest/CardOwnerResolver.kt`
- Test: `TossBeneface/src/test/kotlin/com/app/api/UserDataTest/CardOwnerResolverTest.kt`
- Modify: `TossBeneface/src/main/kotlin/com/app/api/UserDataTest/service/UserCardService.kt`
- Modify: `TossBeneface/src/main/kotlin/com/app/api/UserDataTest/controller/UserCardController.kt`

- [ ] **Step 1: 실패하는 CardOwnerResolver 테스트 작성**

`TossBeneface/src/test/kotlin/com/app/api/UserDataTest/CardOwnerResolverTest.kt`:

```kotlin
package com.app.api.UserDataTest

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CardOwnerResolverTest {

    private val repository = mockk<UserDataTestRepository>()
    private val resolver = CardOwnerResolver(repository)

    @Test
    fun `returns owner member id`() {
        every { repository.findOwnerMemberIdById(10L) } returns 7L
        assertEquals(7L, resolver.ownerOf(10L))
    }

    @Test
    fun `returns null when card not found`() {
        every { repository.findOwnerMemberIdById(10L) } returns null
        assertNull(resolver.ownerOf(10L))
    }
}
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.api.UserDataTest.CardOwnerResolverTest"`
Expected: FAIL — `CardOwnerResolver` / `findOwnerMemberIdById` 없음(컴파일 실패)

- [ ] **Step 3: 리포지토리에 소유자 조회 추가**

`UserDataTestRepository.kt`의 인터페이스 본문에 추가:

```kotlin
    @Query("SELECT u.member.memberId FROM UserDataTestEntity u WHERE u.id = :cardId")
    fun findOwnerMemberIdById(@Param("cardId") cardId: Long): Long?
```

- [ ] **Step 4: CardOwnerResolver 구현**

`TossBeneface/src/main/kotlin/com/app/api/UserDataTest/CardOwnerResolver.kt`:

```kotlin
package com.app.api.UserDataTest

import com.app.global.security.rbac.ResourceOwnerResolver
import org.springframework.stereotype.Component

@Component("card")   // 빈 이름 = Casbin 자원 타입 "card"
class CardOwnerResolver(
    private val repository: UserDataTestRepository
) : ResourceOwnerResolver {
    override fun ownerOf(resourceId: Long): Long? = repository.findOwnerMemberIdById(resourceId)
}
```

- [ ] **Step 5: 테스트 실행 → 통과 확인**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.api.UserDataTest.CardOwnerResolverTest"`
Expected: PASS (2 tests)

- [ ] **Step 6: 서비스 deleteUserCard를 id 단독으로 리팩터**

`UserCardService.kt`의 `deleteUserCard`를 아래로 교체:

```kotlin
    @Transactional
    fun deleteUserCard(cardId: Long) {
        if (!repository.existsById(cardId)) {
            throw IllegalArgumentException("삭제할 카드가 없습니다.")
        }
        repository.deleteById(cardId)
    }
```

> 소유권 판단은 컨트롤러의 `@PreAuthorize`로 이동했으므로 서비스는 더 이상 memberId로 스코프하지 않는다.

- [ ] **Step 7: 사용되지 않게 된 리포지토리 메서드 제거**

`UserDataTestRepository.kt`에서 아래 두 메서드를 삭제(이제 어디서도 호출되지 않음):

```kotlin
    @Query("SELECT u FROM UserDataTestEntity u WHERE u.id = :cardId AND u.member.memberId = :memberId")
    fun findCardByIdAndMemberId(@Param("cardId") cardId: Long, @Param("memberId") memberId: Long): Optional<UserDataTestEntity>

    @Transactional
    @Modifying
    @Query("DELETE FROM UserDataTestEntity u WHERE u.id = :cardId AND u.member.memberId = :memberId")
    fun deleteCardByIdAndMemberId(@Param("cardId") cardId: Long, @Param("memberId") memberId: Long)
```

> 삭제 후 `import` 중 미사용이 되는 `Optional`, `Modifying`, `Transactional`이 다른 메서드에서 여전히 쓰이는지 확인하고, 안 쓰이면 해당 import도 제거한다. (`findCardByNameAndCompany`가 `Optional`을 계속 쓰므로 Optional import는 유지)

- [ ] **Step 8: 컨트롤러 deleteUserCard에 @PreAuthorize 부착**

`UserCardController.kt`의 import 구역에 추가:

```kotlin
import org.springframework.security.access.prepost.PreAuthorize
```

`deleteUserCard`를 아래로 교체:

```kotlin
    @PreAuthorize("@authz.can('card', 'delete', #cardId)")
    @DeleteMapping("/{cardId}")
    fun deleteUserCard(@PathVariable cardId: Long): ResponseEntity<String> {
        userCardService.deleteUserCard(cardId)
        return ResponseEntity.ok("카드 삭제 완료")
    }
```

> `@MemberInfo` 파라미터는 delete에서 제거(소유권/인증을 @PreAuthorize가 담당). `getUserCards`/`registerCard`는 그대로 둔다.

- [ ] **Step 9: 컴파일 + 관련 테스트 실행**

Run: `cd TossBeneface && ./gradlew compileKotlin test --tests "com.app.api.UserDataTest.*"`
Expected: BUILD SUCCESSFUL, CardOwnerResolverTest PASS

- [ ] **Step 10: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/api/UserDataTest
git commit -m "refactor: enforce card delete ownership via casbin preauthorize"
```

---

## Phase 2 — Remaining Domains

### Task 9: benefit:manage — 카드 혜택 등록 ADMIN 전용

**Files:**
- Modify: `TossBeneface/src/main/kotlin/com/app/api/cardbenefit/controller/CardBenefitController.kt`

- [ ] **Step 1: POST에 @PreAuthorize 부착**

`CardBenefitController.kt`의 import 구역에 추가:

```kotlin
import org.springframework.security.access.prepost.PreAuthorize
```

`saveCardBenefit`를 아래로 교체:

```kotlin
    @PreAuthorize("@authz.can('benefit', 'manage')")
    @PostMapping
    fun saveCardBenefit(@RequestBody cardBenefit: CardBenefit): String {
        return cardBenefitService.saveCardBenefit(cardBenefit)
    }
```

> `/api/card-benefits`는 `SecurityConfig`에서 permitAll이지만, 메서드 시큐리티는 URL 규칙과 독립적으로 동작한다. 미인증/비ADMIN은 `@authz.can`이 false → 403. GET 엔드포인트(`/get_shop`, `/get_details`)는 @PreAuthorize가 없으므로 공개 유지된다. 정책 `p, ADMIN, benefit, manage, any`는 Task 2에서 이미 추가됨.

- [ ] **Step 2: 컴파일 확인**

Run: `cd TossBeneface && ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add TossBeneface/src/main/kotlin/com/app/api/cardbenefit/controller/CardBenefitController.kt
git commit -m "feat: restrict card benefit creation to admin via rbac"
```

---

### Task 10: 결제 확정 흐름 소유권 검증 (조사 → 결정)

**Files (조사 대상):**
- Read: `TossBeneface/src/main/kotlin/com/app/api/payment/controller/PaymentController.kt`
- Read: 결제 확정 서비스(예: `PaymentConfirm*`/`PaymentCompletionService`) 및 요청 DTO

- [ ] **Step 1: 결제 확정이 타인 자원을 지정할 수 있는지 조사**

Run: `cd TossBeneface && grep -rn "MemberInfo\|memberId\|orderId\|paymentKey" src/main/kotlin/com/app/api/payment/controller/PaymentController.kt`

확인 기준:
- 확정 대상(order/payment)이 **요청 바디의 식별자**로 지정되고, 그 식별자가 **현재 로그인 멤버 소유인지 검증되지 않으면** → 소유권 정책 필요.
- 대상이 `@MemberInfo`(현재 멤버)에서만 파생되거나, 서비스가 memberId로 스코프해 검증하면 → self-scoped, 정책 불필요.

- [ ] **Step 2: 결정 분기**

**(A) self-scoped로 판명되면:** 코드 변경 없음. 분류 근거를 `docs/superpowers/specs/2026-06-20-rbac-casbin-design.md`의 6절 payment 행에 한 줄로 확정 기록(예: "확정 대상은 서비스가 memberId로 검증 → self-scoped"). 이 Task의 나머지 Step을 건너뛰고 Step 5(Commit)로.

**(B) 타인 지정 가능으로 판명되면:** 아래 Step 3-4 수행.

- [ ] **Step 3 (분기 B): payment 정책 + PaymentOwnerResolver 추가**

`policy.csv`에 추가:
```csv
p, USER, payment, execute, own
```
`CasbinPolicyTest`에 추가:
```kotlin
    @Test
    fun `user can execute own payment`() {
        assertTrue(enforcer.enforce("USER", "payment", "execute", "own"))
    }
```
`PaymentOwnerResolver`를 `@Component("payment")`로 작성(CardOwnerResolver와 동일 패턴, 결제→소유 memberId 조회 리포지토리 메서드 사용). 해당 리포지토리에 소유자 조회 메서드와 단위 테스트를 Task 8과 동일한 구조로 추가.

- [ ] **Step 4 (분기 B): 확정 컨트롤러에 @PreAuthorize 부착**

해당 확정 엔드포인트에 `@PreAuthorize("@authz.can('payment', 'execute', #<식별자>)")`를 부착하고, Toss 콜백 엔드포인트(`/callback-auth`, `/`, `/fail`)에는 부착하지 않는다(public 유지).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: classify payment confirm authorization scope"
```

---

## Phase 3 — Cleanup & Integration Tests

### Task 11: 인가 통합 테스트 (메서드 시큐리티 종단)

**실행 결과: 이 환경에서 통합 테스트 불가 → 단위 커버리지에 의존(플랜 fallback 적용).**

조사 결과:
- 풀 `@SpringBootTest`는 `AbstractIntegrationTest`가 `@Testcontainers(disabledWithoutDocker = true)`라 **Docker 없으면 스킵**된다(실제 검증 0). 독립 `@SpringBootTest`도 kafka/grpc/redis 빈 때문에 부팅 실패.
- `@WebMvcTest` 슬라이스는 이 앱의 `JwtAuthenticationFilter`(잘못된 토큰 시 SecurityContext를 clear) + 인터셉터(`AdminAuthorizationInterceptor` 등, `BearerTokenResolver` 의존) + 커스텀 `@MemberInfo` argument resolver + `WebConfig`가 얽혀, 메서드 시큐리티를 신뢰성 있게 태우기 어렵다(필터를 켜면 JWT 필터가 주입 인증을 지우고, 끄면 403 변환이 사라짐).

결론: 웹 레이어 인가 종단 테스트는 본 환경에서 신뢰성 있게 실행 불가. 인가 로직은 다음 단위 테스트로 결정적으로 커버됨 → 이것을 보증 수단으로 삼는다:
- `CasbinPolicyTest`(6): 정책(ADMIN manage / USER 거부, own/any).
- `AccessCheckerTest`(8): 미인증 거부, any 허용, own 소유자 일치/불일치, 권한 없음, resolver 없음, 자원 미존재.
- `CardOwnerResolverTest`(2), `AuthenticatedMemberContextResolverTest`(컨텍스트 추출).

**미커버 잔여 갭(소규모):** `@PreAuthorize("@authz.can(...)")`의 SpEL 빈 이름 + 메서드 시큐리티 advisor 활성화 "와이어링" 자체. Docker가 있는 CI에서 `AbstractIntegrationTest`를 상속한 MockMvc 인가 테스트로 추후 보강 권장.

아래 원안(참고용, 본 환경 미적용):

> **주의(정직한 한계):** 이 프로젝트는 gRPC/Kafka/Redis 등 외부 의존을 부팅하므로 `@SpringBootTest` 전체 컨텍스트가 로컬에서 뜨지 않을 수 있다. 아래 테스트는 Spring Security test 지원(`SecurityMockMvcRequestPostProcessors.authentication`)으로 JWT 없이 인증 주체를 주입한다. 컨텍스트 부팅이 실패하면, 인가 로직 자체는 Task 5(AccessChecker) 단위 테스트가 이미 보장하므로 이 테스트는 보조 수단이다. 부팅 실패 시 `@SpringBootTest`를 슬라이스로 좁히거나(`classes` 한정), 본 Task를 스킵하고 단위 커버리지에 의존한다 — 실행 결과를 보고할 것.

- [ ] **Step 1: 통합 테스트 작성**

`TossBeneface/src/test/kotlin/com/app/global/security/rbac/AuthorizationIntegrationTest.kt`:

```kotlin
package com.app.global.security.rbac

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class AuthorizationIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun authAs(memberId: Long, role: String) =
        authentication(
            UsernamePasswordAuthenticationToken.authenticated(
                memberId, "token", listOf(SimpleGrantedAuthority("ROLE_$role"))
            )
        )

    // 결정적(DB 비의존) 보안 경계만 검증한다. 허용 경로(ADMIN 통과 후 실제 변경)는
    // AccessChecker 단위 테스트(Task 5)가 보장하므로 여기서는 거부 경로에 집중한다.

    @Test
    fun `user is forbidden from changing member authority`() {
        mockMvc.perform(
            put("/api/member/99/authority")
                .with(authAs(2L, "USER"))
                .contentType("application/json")
                .content("""{"role":"admin"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `anonymous is unauthorized for member authority`() {
        mockMvc.perform(
            put("/api/member/99/authority")
                .contentType("application/json")
                .content("""{"role":"admin"}""")
        ).andExpect(status().isUnauthorized)
    }
}
```

> 필요한 테스트 의존성: `spring-security-test`. `build.gradle`에 `testImplementation 'org.springframework.security:spring-security-test'`가 없으면 추가한다.

- [ ] **Step 2: 테스트 실행**

Run: `cd TossBeneface && ./gradlew test --tests "com.app.global.security.rbac.AuthorizationIntegrationTest"`
Expected: PASS. (컨텍스트 부팅 실패 시 위 주의사항대로 보고 후 스킵 판단)

- [ ] **Step 3: Commit**

```bash
git add TossBeneface/src/test/kotlin/com/app/global/security/rbac/AuthorizationIntegrationTest.kt TossBeneface/build.gradle
git commit -m "test: add rbac authorization integration tests"
```

---

### Task 12: 전체 회귀 검증

- [ ] **Step 1: 전체 테스트**

Run: `cd TossBeneface && ./gradlew test`
Expected: BUILD SUCCESSFUL (전체 통과)

- [ ] **Step 2: (선택) gRPC 통합 스크립트** — CLAUDE.md 권장 스모크

Run: `bash .claude/hooks/grpc-integration-test.sh`
Expected: 정상 종료

- [ ] **Step 3: 최종 정리 커밋(필요 시)**

```bash
git add -A
git commit -m "chore: finalize rbac rollout"
```

---

## Notes / Non-goals
- DB 어댑터(JDBCAdapter), `casbin_rule` 테이블, 런타임 정책 관리 API는 비범위. 전환점은 `CasbinConfig.enforcer()`의 `FileAdapter(...)` 한 줄로 격리됨.
- ABAC 객체 방식은 추후 동일 `AccessChecker` 파사드 뒤에서 교체해 scope 방식과 비교.
- self-scoped 엔드포인트(member profile/initial-state, card register/get, order, payment 콜백)는 정책을 두지 않고 인증만 유지.
