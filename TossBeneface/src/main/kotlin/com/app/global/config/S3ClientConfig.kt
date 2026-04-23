package com.app.global.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3ClientConfig {

    private val log = LoggerFactory.getLogger(S3ClientConfig::class.java)

    @Value("\${cloud.aws.credentials.accessKey}")
    private lateinit var accessKey: String

    @Value("\${cloud.aws.credentials.secretKey}")
    private lateinit var secretKey: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    @Bean
    fun s3Client(): S3Client {
        val awsCreds = AwsBasicCredentials.create(accessKey, secretKey)

        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .region(Region.of(region))
            .build()
    }
}
