package com.app.scheduler

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    private val dailySettlementJob: Job
) {

    private val log = LoggerFactory.getLogger(BatchScheduler::class.java)

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailySettlementJob() {
        val targetDate = LocalDate.now().minusDays(1)
        val jobParameters = JobParametersBuilder()
            .addString("targetDate", targetDate.toString())
            .toJobParameters()

        val execution = jobLauncher.run(dailySettlementJob, jobParameters)
        log.info(
            "Daily settlement job launched. targetDate={}, jobExecutionId={}, status={}",
            targetDate,
            execution.id,
            execution.status
        )
    }
}
