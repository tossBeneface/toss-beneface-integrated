package com.app

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractIntegrationTest {

    companion object {
        // Singleton container: started once for the whole JVM and shared by every
        // AbstractIntegrationTest subclass. It is intentionally NOT annotated with @Container,
        // because that binds the container to a single test class's lifecycle and stops it after
        // that class finishes — leaving later integration classes in the same run pointing at a
        // dead port. Testcontainers' Ryuk reaper cleans it up when the JVM exits.
        @JvmStatic
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
        }

        init {
            // Let Testcontainers auto-detect Docker (DOCKER_HOST env / default socket / Docker Desktop)
            // instead of hardcoding a single user's socket path, so tests run on CI and other machines.
            System.setProperty("testcontainers.reuse.enable", "true")
            // Guarded so a Docker-less machine still gets a clean skip via disabledWithoutDocker
            // rather than an exception while the companion object initializes.
            if (DockerClientFactory.instance().isDockerAvailable()) {
                postgresContainer.start()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
            // Batch metadata tables aren't JPA entities, so ddl-auto won't create them and the
            // default 'embedded' schema init skips the (non-embedded) Postgres container.
            registry.add("spring.batch.jdbc.initialize-schema") { "always" }
        }
    }
}
