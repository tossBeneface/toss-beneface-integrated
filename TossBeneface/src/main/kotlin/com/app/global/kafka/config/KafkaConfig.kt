package com.app.global.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@EnableKafka
@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}") private val bootstrapServers: String,
    @Value("\${spring.kafka.listener.auto-startup:true}") private val autoStartup: Boolean
) {

    // ─────────────────────────────────────────────────────
    // Producer 설정
    // ─────────────────────────────────────────────────────

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",        // 모든 ISR 확인 후 응답
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,  // 중복 전송 방지
            JsonSerializer.ADD_TYPE_INFO_HEADERS to false
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())

    @Bean
    fun stringProducerFactory(): ProducerFactory<String, String> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun stringKafkaTemplate(): KafkaTemplate<String, String> = KafkaTemplate(stringProducerFactory())

    // ─────────────────────────────────────────────────────
    // Consumer 설정 — 수동 커밋(MANUAL_IMMEDIATE)
    // 처리 성공 후에만 offset 이동 → at-least-once 보장
    // ─────────────────────────────────────────────────────

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val jsonDeserializer = JsonDeserializer<Any>().apply {
            addTrustedPackages("com.app.*")
            setUseTypeMapperForKey(false)
        }
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,  // 수동 커밋
            JsonDeserializer.TRUSTED_PACKAGES to "com.app.*"
        )
        return DefaultKafkaConsumerFactory(config, StringDeserializer(), jsonDeserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            consumerFactory = consumerFactory()
            // 수동 커밋: 처리 완료 후 acknowledger.acknowledge() 호출
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            // Consumer Group 당 스레드 수 (토픽 파티션 수와 일치 권장)
            setConcurrency(3)
            setAutoStartup(autoStartup)
        }
    }

    @Bean
    fun stringConsumerFactory(): ConsumerFactory<String, String> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun stringKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            consumerFactory = stringConsumerFactory()
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            setConcurrency(3)
            setAutoStartup(autoStartup)
        }
    }
}
