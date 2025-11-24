package com.comeon.component.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * 密码编码器配置类
 * 提供 BCrypt 密码编码器 Bean
 * 
 * 独立配置类，避免与 SecurityConfig 产生循环依赖
 */
@Configuration
class PasswordEncoderConfig {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

