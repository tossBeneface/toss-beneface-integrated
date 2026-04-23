package com.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.app"])
@EnableConfigurationProperties
@EnableScheduling
class TossBenefaceApplication

fun main(args: Array<String>) {
    runApplication<TossBenefaceApplication>(*args)
}
