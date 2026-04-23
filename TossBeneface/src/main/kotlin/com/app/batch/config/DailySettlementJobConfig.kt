package com.app.batch.config

import com.app.batch.entity.DailySettlement
import com.app.domain.payment.entity.Payment
import com.app.domain.payment.entity.PaymentStatus
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.batch.core.repository.JobRepository
import java.time.LocalDate

@Configuration
class DailySettlementJobConfig {

    @Bean
    fun dailySettlementJob(
        jobRepository: JobRepository,
        dailySettlementStep: Step
    ): Job {
        return JobBuilder("dailySettlementJob", jobRepository)
            .start(dailySettlementStep)
            .build()
    }

    @Bean
    fun dailySettlementStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        dailySettlementReader: JpaPagingItemReader<Payment>,
        dailySettlementProcessor: ItemProcessor<Payment, DailySettlement>,
        dailySettlementWriter: JpaItemWriter<DailySettlement>
    ): Step {
        return StepBuilder("dailySettlementStep", jobRepository)
            .chunk<Payment, DailySettlement>(100, transactionManager)
            .reader(dailySettlementReader)
            .processor(dailySettlementProcessor)
            .writer(dailySettlementWriter)
            .build()
    }

    @Bean
    @StepScope
    fun dailySettlementReader(
        entityManagerFactory: EntityManagerFactory,
        @Value("#{jobParameters['targetDate']}") targetDate: String?
    ): JpaPagingItemReader<Payment> {
        val settlementDate = LocalDate.parse(targetDate ?: LocalDate.now().minusDays(1).toString())
        val startApprovedAt = "${settlementDate}T00:00:00"
        val endApprovedAt = "${settlementDate.plusDays(1)}T00:00:00"

        return JpaPagingItemReaderBuilder<Payment>()
            .name("dailySettlementReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(100)
            .queryString(
                """
                SELECT p
                FROM Payment p
                WHERE p.status = :status
                  AND p.approvedAt IS NOT NULL
                  AND p.approvedAt >= :startApprovedAt
                  AND p.approvedAt < :endApprovedAt
                ORDER BY p.id
                """.trimIndent()
            )
            .parameterValues(
                mapOf(
                    "status" to PaymentStatus.DONE,
                    "startApprovedAt" to startApprovedAt,
                    "endApprovedAt" to endApprovedAt
                )
            )
            .build()
    }

    @Bean
    fun dailySettlementProcessor(): ItemProcessor<Payment, DailySettlement> {
        return ItemProcessor { payment ->
            val paymentId = payment.id ?: return@ItemProcessor null
            val memberId = payment.member?.memberId ?: return@ItemProcessor null
            val approvedAt = payment.approvedAt
            val targetDate = approvedAt
                ?.take(10)
                ?.let(LocalDate::parse)
                ?: LocalDate.now().minusDays(1)

            DailySettlement(
                targetDate = targetDate,
                paymentId = paymentId,
                paymentKey = payment.paymentKey,
                orderId = payment.orderId,
                memberId = memberId,
                totalAmount = payment.totalAmount,
                approvedAt = approvedAt
            )
        }
    }

    @Bean
    fun dailySettlementWriter(entityManagerFactory: EntityManagerFactory): JpaItemWriter<DailySettlement> {
        return JpaItemWriterBuilder<DailySettlement>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
}
