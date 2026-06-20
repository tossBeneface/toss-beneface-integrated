# RBAC with jCasbin — Design

- Date: 2026-06-20
- Status: Approved (design)
- Scope: 주요 도메인 전체에 RBAC + 소유권(ownership) 인가 도입

## 1. 목표와 배경

### 목표
- 인지도 있는 오픈소스 인가 라이브러리(**jCasbin**)를 도입해 "제대로 된 RBAC"를 학습 겸 실제 적용한다.
- 인가 모델은 **RBAC + 소유권**: 역할 기반 권한에 더해 "OWNER/USER는 자기 자원만" 규칙을 표현한다.
- 정책은 **정적 파일로 시작**하되, 어댑터 교체만으로 **DB 전환**이 가능하도록 경계를 잡는다.
- 이번엔 **scope 컬럼 방식 RBAC**로 구현하고, **ABAC 객체 방식은 향후 동일 파사드 뒤에서 교체해 비교**할 수 있게 둔다.

### 현재 상태 (배경)
- Spring Boot 3.3.5 / Kotlin 1.9.23 / Java 17, Spring Security + JWT 사용 중.
- `JwtAuthenticationFilter`가 SecurityContext에 `principal=memberId`, `authority=ROLE_USER|OWNER|ADMIN`를 채운다. (변경하지 않음)
- 인가는 현재 `SecurityConfig`의 URL 매칭(`permitAll`/`authenticated`)만 존재 — role 기반 분기 없음.
- 직전 세션에서 `ChangeMemberAuthorityUseCase`에 `requesterRole != ADMIN` if 가드를 임시로 넣음. 본 설계에서 Casbin 방식으로 이관(제거)한다.
- `Role` enum: `USER, OWNER, ADMIN`.

## 2. 아키텍처 & 통합 경계

기존 인증/인가 흐름은 그대로 두고 "인가 결정"만 Casbin에 위임한다.

```
요청 → JwtAuthenticationFilter (기존, 변경 없음)
         └ SecurityContext: principal=memberId, authority=ROLE_xxx
      → @PreAuthorize 평가 (신규, 메서드 시큐리티)
         └ @authz.can('card','delete', #cardId)
              → AccessChecker (@Component "authz", 신규)
                   ├ SecurityContext에서 현재 member(id, role)
                   ├ (own 액션이면) ResourceOwnerResolver로 소유자 조회
                   └ Casbin Enforcer.enforce(...) → allow/deny
      → 허용 시 Controller/UseCase 실행 (기존)
```

### 단위와 책임
- **SecurityConfig**: `@EnableMethodSecurity` 추가. URL 매칭은 인증 여부 게이트로 유지. 세분화된 role/소유권은 메서드 레벨로 내림.
- **AccessChecker (`@authz` 파사드)**: 컨트롤러의 단일 인가 창구. Casbin enforcer + OwnerResolver + SecurityContext 조합을 캡슐화. 컨트롤러는 Casbin을 직접 모른다.
- **Enforcer (Casbin 빈)**: `model.conf` + 정책 어댑터를 받아 "이 sub가 이 obj에 이 act 가능?"만 판단.
- **PolicyAdapter 경계**: 지금은 `FileAdapter`(csv), 나중에 `JDBCAdapter`. 교체는 `CasbinConfig` 한 곳에 격리.
- **ResourceOwnerResolver**: 자원 타입별 소유자(memberId) 조회. AccessChecker는 "어떻게 찾는지" 모른다.

이 분리로 각 단위가 한 가지 일만 하고(필터=인증, AccessChecker=결정 위임, Enforcer=규칙 평가, Adapter=정책 저장, Resolver=소유자 조회) 독립적으로 테스트된다.

## 3. Casbin 모델 & 정책 (scope 방식)

소유권은 정책의 `scope` 컬럼(`own`/`any`)으로 표현하고, Casbin이 scope까지 매칭한 뒤 `own`이면 AccessChecker가 소유자 비교를 명시적으로 수행한다.

### `src/main/resources/rbac/model.conf`
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
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act \
    && (p.scope == "any" || p.scope == r.scope)
```

### `src/main/resources/rbac/policy.csv` (정적 시작)
```csv
# 역할 상속
g, ADMIN, OWNER
g, OWNER, USER

# member: 권한 관리 (role-only, ADMIN)
p, ADMIN, member, manage, any

# card-benefit: 관리 (role-only, ADMIN)
p, ADMIN, benefit, manage, any

# card 삭제: USER는 본인 것만, ADMIN은 전체 (own vs any 시연)
p, USER,  card, delete, own
p, ADMIN, card, delete, any
```
> 정책표(엔드포인트 ↔ resource/action/scope)는 6절 참고. 역할 상속으로 OWNER/ADMIN은 USER 권한(본인 카드 삭제 등)을 자동 포함한다. self-scoped 엔드포인트는 정책을 두지 않으므로 정책 행이 적다(6절 원칙).

### 판단 흐름 (AccessChecker)
1. `enforce(role, obj, act, "any")` → 통과 시 즉시 allow (예: ADMIN).
2. 실패 시 `enforce(role, obj, act, "own")` → 실패면 deny(권한 자체 없음).
3. own 통과면 OwnerResolver로 소유자 조회 후 **소유자 == 현재 멤버**이면 allow, 아니면 deny.

## 4. 인가 집행 레이어

### 컨트롤러 사용
```kotlin
@PreAuthorize("@authz.can('card', 'delete', #cardId)")  // 소유권 포함
@PreAuthorize("@authz.can('member', 'manage')")         // role-only
```
- 기본은 선언적(@PreAuthorize).
- 자원이 이미 로딩된 복잡한 경우는 유스케이스 안에서 `accessChecker.can(...)` 명령형 호출 허용.

### AccessChecker
```kotlin
@Component("authz")
class AccessChecker(
    private val enforcer: Enforcer,
    private val ownerResolvers: Map<String, ResourceOwnerResolver>  // 빈 이름 = 자원 type
) {
    fun can(resource: String, action: String): Boolean {
        val (_, role) = currentMember()
        return enforcer.enforce(role.name, resource, action, "any")
    }

    fun can(resource: String, action: String, resourceId: Long): Boolean {
        val (memberId, role) = currentMember()
        if (enforcer.enforce(role.name, resource, action, "any")) return true
        if (!enforcer.enforce(role.name, resource, action, "own")) return false
        val ownerId = ownerResolvers[resource]?.ownerOf(resourceId)
            ?: throw IllegalStateException("no owner resolver for $resource")
        return ownerId == memberId
    }

    private fun currentMember(): Pair<Long, Role> { /* SecurityContext에서 추출 */ }
}
```

### ResourceOwnerResolver
```kotlin
interface ResourceOwnerResolver {
    fun ownerOf(resourceId: Long): Long?   // 자원의 소유 memberId
}

@Component("card")   // 빈 이름 = 자원 type
class CardOwnerResolver(...) : ResourceOwnerResolver { ... }
```
- 새 소유 자원은 resolver 하나 추가로 끝. Spring이 `Map<String, ResourceOwnerResolver>`로 빈 이름을 키 삼아 주입.

## 5. 정책 저장 경계 (정적 → DB)

### 현재 (정적 파일)
```kotlin
@Configuration
class CasbinConfig {
    @Bean
    fun enforcer(
        @Value("classpath:rbac/model.conf") model: Resource,
        @Value("classpath:rbac/policy.csv") policy: Resource
    ): Enforcer {
        val adapter = FileAdapter(policy.inputStream)   // ← 교체 지점
        return Enforcer(model.path, adapter)
    }
}
```
리소스: `src/main/resources/rbac/{model.conf, policy.csv}`

### 향후 (DB 전환 — 설계만 반영, 이번 미구현)
- `JDBCAdapter(dataSource)`로 교체. `casbin_rule` 테이블은 Flyway(`V8__...`)로 생성, csv를 시드.
- **변경 코드는 `CasbinConfig` 어댑터 한 줄.** AccessChecker/컨트롤러/resolver 무변경.
- 런타임 정책 변경이 필요하면 `enforcer.addPolicy/savePolicy` 기반 관리 유스케이스를 추가(이번 범위 밖).

### 이번 범위
- `model.conf`/`policy.csv` + `FileAdapter` 빈만 구현.
- DB 어댑터·`casbin_rule`·관리 API는 "확장 지점"으로 문서화만(YAGNI).
- 핵심 산출물: 어댑터 교체가 `CasbinConfig` 한 곳에 격리됨을 보장.

## 6. 적용 범위와 정책표

**중요 원칙: self-scoped 엔드포인트는 RBAC 정책을 붙이지 않고 인증(`authenticated`)만 유지한다.** 이미 `@MemberInfo` 기반으로 쿼리가 본인 데이터로 필터되는 엔드포인트(`getMyOrders`, `getUserCards` 등)는 소유권이 쿼리에 내재되어 별도 정책이 불필요하다. 진짜 소유권 검사는 **경로에 남의 id가 올 수 있는** 경우에만 필요하다.

정책이 실제로 필요한 엔드포인트(경로에 남의 id가 올 수 있거나 role 게이트가 필요한 것)는 **셋뿐**이다. 나머지는 self-scoped로 분류해 인증만 유지한다.

**정책 부착 (RBAC 적용)**

| 도메인 | 엔드포인트 | resource | action | scope | 비고 |
|---|---|---|---|---|---|
| member | `PUT /api/member/{id}/authority` | member | manage | any | role-only, ADMIN만. path id가 타인 가능 |
| card | `DELETE /api/user-cards/{cardId}` | card | delete | own/any | 소유권 핵심(path id). USER 본인 / ADMIN 전체 |
| card-benefit | `POST /api/card-benefits` | benefit | manage | any | role-only, ADMIN (현재 무방비) |

**인증만 유지 (self-scoped, 정책 미부착)**

| 도메인 | 엔드포인트 | 사유 |
|---|---|---|
| member | `PUT /profile`, `/initial-state` | 대상이 항상 @MemberInfo(본인). 타인 지정 불가 |
| member | `GET /info`, `GET /name` | 본인/공개 조회 |
| card | `POST /user-cards/register`, `GET /user-cards` | @MemberInfo 본인, 쿼리 내재 |
| order | `POST /orders`, `GET /orders/order-list` | @MemberInfo 본인, 쿼리 내재 |
| payment | `POST /payment/confirm/{widget,payment}` | **(구현 시 검증 완료 — self-scoped)** 멤버를 `@MemberInfo`에서 가져와 `command.withMemberId(memberInfoDto.memberId)`로 인증된 본인 id로 덮어씀. 요청 바디의 memberId를 신뢰하지 않아 타인으로 확정 불가 |
| payment | `POST /confirm-billing`, `/issue-billing-key`, `/confirm/brandpay` | **(검증 완료)** Toss 측 식별자(customerKey/billingKey) 기반 빌링 흐름. 경로/바디에 남의 저장 자원을 가리키는 id 없음. SecurityConfig에서 이미 `.authenticated()` |
| payment | `GET /callback-auth`, `/`, `/fail` | Toss 콜백, public 유지 |

### 단계별 롤아웃 (한 스펙, 3 Phase)
- **Phase 1 — 기반 + 양 경로 검증**: 의존성 추가, `CasbinConfig`, `model.conf`/`policy.csv`, `AccessChecker`, `ResourceOwnerResolver` 인터페이스, `@EnableMethodSecurity`. 핵심 메커니즘(role-only·역할상속·소유권·any override)을 두 엔드포인트로 관통:
  - `member:manage` → `changeAuthority` 이관 (+ 기존 `requesterRole` 가드 제거)
  - `card:delete` → `DELETE /user-cards/{cardId}` (`CardOwnerResolver` 포함, USER own / ADMIN any)
  - 해당 단위·통합 테스트 포함.
- **Phase 2 — 나머지 도메인 점검**: `benefit:manage`를 `POST /card-benefits`에 부착. 나머지 member/card/order/payment 엔드포인트를 위 표대로 점검해 self-scoped 분류 확정, 특히 payment 확정 흐름의 타인 지정 가능성 검증 후 필요 시 `payment:execute`/`PaymentOwnerResolver` 추가.
- **Phase 3 — 정리·검증**: self-scoped 분류를 코드/문서에 명문화, 도메인 횡단 통합 테스트 보강, `SecurityConfig` URL 규칙과 메서드 시큐리티 정합성 최종 확인.

## 7. 테스트 전략

1. **Casbin 정책 단위 테스트** (실제 enforcer, 도메인/Spring 없이): policy.csv/model.conf 회귀 방지.
   - 예: `enforce("ADMIN","member","manage","any")==true`, `enforce("USER","member","manage","any")==false`, `enforce("USER","card","delete","own")==true`.
2. **AccessChecker 단위 테스트** (MockK, enforcer+resolver mock): any 즉시 allow / own+소유자일치 allow / own+소유자불일치 deny(← 기존 "non-admin 거부"가 여기로) / 권한없음 deny / resolver없음 예외.
3. **ResourceOwnerResolver 단위 테스트** (repository mock): 올바른 memberId 반환 / 없는 자원 처리.
4. **통합 테스트** (`@SpringBootTest` + MockMvc, JWT 토큰별): `PUT /member/{id}/authority` ADMIN 200 / USER 403; `DELETE /user-cards/{cardId}` 본인 200 / 타인 403 / 미인증 401.

### 기존 테스트 변경
- `ChangeMemberAuthorityUseCaseTest`: `requesterRole` 제거 → happy path만. "non-admin 거부"는 AccessChecker/통합 테스트로 이전.

도구: MockK(프로젝트 표준). 정책 테스트는 실제 Enforcer 인스턴스(파일 로드).

## 8. 의존성

```groovy
// build.gradle
implementation 'org.casbin:jcasbin:1.x'           // 또는 org.casbin:casbin-spring-boot-starter
```
> Spring Boot 3.3.5 / Java 17 호환 최신 버전을 구현 시 확정. starter 사용 시 Enforcer 자동구성 활용, 미사용 시 `CasbinConfig`에서 수동 빈 구성.

## 9. 비범위 (Non-goals / 확장 지점)
- DB 어댑터(`JDBCAdapter`), `casbin_rule` 테이블, 런타임 정책 관리 API.
- ABAC 객체 방식(향후 동일 `AccessChecker` 파사드 뒤에서 교체해 scope 방식과 비교).
- self-scoped 엔드포인트의 정책화(인증만 유지).
- OAuth2/JWT 발급 흐름 변경.
