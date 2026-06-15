package com.app.api.order.controller

import com.app.api.order.dto.OrderItemRequest
import com.app.api.order.dto.OrderRequest
import com.app.api.order.service.OrderService
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.order.entity.OrderItem
import com.app.domain.order.entity.OrderPayment
import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.FilterType
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.app.global.config.web.WebConfig
import com.app.auth.infra.security.AuthenticatedMemberContextResolver
import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import com.app.global.interceptor.AdminAuthorizationInterceptor
import com.app.global.interceptor.AuthenticationInterceptor
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@WebMvcTest(
    controllers = [OrderController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [WebConfig::class])
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerSliceTest {

    @MockBean
    private lateinit var bearerTokenResolver: BearerTokenResolver

    @MockBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @MockBean
    private lateinit var authenticatedMemberContextResolver: AuthenticatedMemberContextResolver

    @MockBean
    private lateinit var authenticationInterceptor: AuthenticationInterceptor

    @MockBean
    private lateinit var adminAuthorizationInterceptor: AdminAuthorizationInterceptor

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var orderService: OrderService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun orderService(): OrderService = mockk()

        @Bean
        fun webMvcConfigurer(): WebMvcConfigurer {
            return object : WebMvcConfigurer {
                override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
                    resolvers.add(object : HandlerMethodArgumentResolver {
                        override fun supportsParameter(parameter: MethodParameter): Boolean {
                            return parameter.hasParameterAnnotation(MemberInfo::class.java)
                        }

                        override fun resolveArgument(
                            parameter: MethodParameter,
                            mavContainer: ModelAndViewContainer?,
                            webRequest: NativeWebRequest,
                            binderFactory: WebDataBinderFactory?
                        ): Any {
                            return MemberInfoDto(1L, "test@test.com", "Tester", Role.USER)
                        }
                    })
                }
            }
        }
    }

    @Test
    @DisplayName("주문 생성 API 호출 시 주문 번호를 반환한다")
    fun createOrderApiTest() {
        // given
        val request = OrderRequest(
            memberId = 1L,
            cafeId = 100L,
            cafeName = "스타벅스",
            items = listOf(OrderItemRequest("아메리카노", 4000, 1)),
            totalAmount = 4000
        )
        every { orderService.createOrder(any()) } returns 101L

        // when & then
        mockMvc.perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.orderId").value(101L))
            .andExpect(jsonPath("$.message").value("주문이 성공적으로 접수되었습니다."))
    }

    @Test
    @DisplayName("내 주문 목록 조회 API 호출 시 주문 목록을 반환한다")
    fun getMyOrdersApiTest() {
        // given
        val order = mockk<OrderPayment>()
        val item = mockk<OrderItem>()
        every { order.id } returns 200L
        every { order.totalAmount } returns 5000
        every { order.items } returns mutableListOf(item)
        every { item.name } returns "카페라떼"
        every { item.price } returns 5000
        every { item.count } returns 1

        every { orderService.getMyOrders(1L) } returns listOf(order)

        // when & then
        mockMvc.perform(get("/api/orders/order-list"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.orders[0].orderId").value(200L))
            .andExpect(jsonPath("$.orders[0].items[0].name").value("카페라떼"))
    }
}
