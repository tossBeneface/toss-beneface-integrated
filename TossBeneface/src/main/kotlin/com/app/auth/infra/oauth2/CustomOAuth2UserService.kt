package com.app.auth.infra.oauth2

import com.app.member.application.dto.ResolveSocialMemberCommand
import com.app.member.application.usecase.ResolveSocialMemberUseCase
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val resolveSocialMemberUseCase: ResolveSocialMemberUseCase
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oauth2User = delegate.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId.uppercase()
        val socialId = oauth2User.attributes["sub"]?.toString()
            ?: throw OAuth2AuthenticationException("Google social id is missing")
        val email = oauth2User.attributes["email"]?.toString()
            ?: throw OAuth2AuthenticationException("Google email is missing")
        val name = oauth2User.attributes["name"]?.toString() ?: email.substringBefore("@")

        val member = resolveSocialMemberUseCase.resolve(
            ResolveSocialMemberCommand(
                socialType = registrationId,
                socialId = socialId,
                email = email,
                memberName = name
            )
        )
        val enrichedAttributes = HashMap(oauth2User.attributes).apply {
            put("memberId", member.memberId)
            put("role", member.role.name)
        }

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority("ROLE_${member.role.name}")),
            enrichedAttributes,
            "sub"
        )
    }
}
