package com.app.global.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    @Profile("test")
    fun testCacheManager(): CacheManager {
        return ConcurrentMapCacheManager(ALL_PRODUCTS_CACHE, PRODUCTS_BY_CAFE_CACHE)
    }

    @Bean
    @Profile("!test")
    fun redisCacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )

        val cacheConfigurations = mapOf(
            ALL_PRODUCTS_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(3)),
            PRODUCTS_BY_CAFE_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            CARD_BENEFITS_BY_SHOP_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            CARD_BENEFIT_DETAILS_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(30))
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    companion object {
        const val ALL_PRODUCTS_CACHE = "products:all"
        const val PRODUCTS_BY_CAFE_CACHE = "products:byCafe"
        const val CARD_BENEFITS_BY_SHOP_CACHE = "cardBenefits:byShop"
        const val CARD_BENEFIT_DETAILS_CACHE = "cardBenefits:details"
    }
}
