package com.comeon.component

import com.comeon.component.config.JwtConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig::class)
class ComeonApplication

fun main(args: Array<String>) {
    runApplication<ComeonApplication>(*args)
}
