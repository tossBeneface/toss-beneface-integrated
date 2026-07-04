package com.app.global.config

import org.casbin.jcasbin.main.Enforcer
import org.casbin.jcasbin.model.Model
import org.casbin.jcasbin.persist.file_adapter.FileAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files

@Configuration
class CasbinConfig {

    @Bean
    fun enforcer(): Enforcer {
        val model = Model()
        model.loadModelFromText(readClasspath("rbac/model.conf"))

        // fat jar 내부에서는 classpath 리소스가 파일 경로로 해석되지 않으므로
        // 정책을 임시 파일로 복사해 FileAdapter에 전달한다.
        val policyFile = Files.createTempFile("casbin-policy", ".csv").toFile().apply {
            deleteOnExit()
            writeText(readClasspath("rbac/policy.csv"))
        }

        // ↓↓ DB 전환 시 이 한 줄만 JDBCAdapter(dataSource)로 교체한다 (정책 저장 경계)
        val adapter = FileAdapter(policyFile.absolutePath)

        val enforcer = Enforcer(model, adapter)
        enforcer.loadPolicy()
        return enforcer
    }

    private fun readClasspath(path: String): String =
        ClassPathResource(path).inputStream.bufferedReader().use { it.readText() }
}
