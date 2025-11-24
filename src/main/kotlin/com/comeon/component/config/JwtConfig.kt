package com.comeon.component.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * JWT Token 配置类
 * 从 application.yml 读取配置
 * 
 * 配置文件位置：src/main/resources/application.yml
 * 配置项：
 *   jwt:
 *     secret: <密钥>
 *     access-token-expiration: <AccessToken有效期（毫秒）>
 *     refresh-token-expiration: <RefreshToken有效期（毫秒）>
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtConfig {
    /**
     * JWT 密钥（用于签名和验证）
     * 建议：至少32个字符，生产环境应使用环境变量或密钥管理服务
     */
    var secret: String = "hahaha-your-secret-key-minimum-32-characters-long-for-security"
    
    /**
     * AccessToken 有效期（毫秒）
     * 默认：1小时 = 3600000 毫秒
     */
    var accessTokenExpiration: Long = 3600000L
    
    /**
     * RefreshToken 有效期（毫秒）
     * 默认：7天 = 604800000 毫秒
     */
    var refreshTokenExpiration: Long = 604800000L
}

