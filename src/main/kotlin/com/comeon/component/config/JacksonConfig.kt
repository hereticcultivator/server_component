package com.comeon.component.config

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
class JacksonConfig {
    
    @Bean
    fun objectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .modules(KotlinModule.Builder().build())
            .build()
    }
    
    @Bean
    fun jsonHttpMessageConverter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter(objectMapper())
    }
}

