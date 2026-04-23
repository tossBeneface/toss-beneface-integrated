package com.app.global.kafka.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OutboxRepository : JpaRepository<OutboxEvent, Long> {

    // 미발행 이벤트를 생성 순서대로 조회 (OutboxPublisher가 폴링)
    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false ORDER BY o.createdAt ASC")
    fun findUnpublished(): List<OutboxEvent>
}
