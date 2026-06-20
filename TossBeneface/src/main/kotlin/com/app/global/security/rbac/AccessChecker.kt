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
