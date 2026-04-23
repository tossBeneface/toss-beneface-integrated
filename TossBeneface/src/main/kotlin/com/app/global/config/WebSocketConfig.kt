package com.app.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal
import java.util.UUID

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .setHandshakeHandler(MemberHandshakeHandler())
            .withSockJS()
    }

    private class MemberHandshakeHandler : DefaultHandshakeHandler() {
        override fun determineUser(
            request: ServerHttpRequest,
            wsHandler: WebSocketHandler,
            attributes: MutableMap<String, Any>
        ): Principal {
            val memberId = UriComponentsBuilder.fromUri(request.uri)
                .build()
                .queryParams
                .getFirst("memberId")
                ?.takeIf { it.isNotBlank() }
                ?: "anonymous-${UUID.randomUUID()}"

            return Principal { memberId }
        }
    }
}
