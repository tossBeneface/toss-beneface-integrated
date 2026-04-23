package com.app.api.order.repository

import com.app.domain.member.entity.Member
import com.app.domain.order.entity.OrderPayment
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderPayment, Long> {
    @EntityGraph(attributePaths = ["items"])
    fun findByMember(member: Member): List<OrderPayment>
}
