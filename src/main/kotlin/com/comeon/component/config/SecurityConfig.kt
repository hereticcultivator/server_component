package com.comeon.component.config

import com.comeon.component.security.JwtAuthenticationFilter
import com.comeon.component.security.resolver.CurrentUserArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) : WebMvcConfigurer {
    
    /**
     * 配置 Spring Security 过滤器链
     * 使用 JWT 认证，禁用 Session
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 禁用 CSRF（因为使用 JWT，不需要 CSRF 保护）
            .csrf { it.disable() }
            // 禁用 Session（使用无状态 JWT）
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // 配置请求授权
            .authorizeHttpRequests { auth ->
                auth
                    // 公开接口（不需要认证）
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/send-code",
                        "/api/auth/refresh",
                        "/api/auth/password-reset/**",
                        "/api/test/**",
                        "/api/v1/game/hungry/tags",  // 获取标签接口公开
                        "/api/v1/game/hungry/parse/**" // 分享解析接口公开
                    ).permitAll()
                    // 其他所有接口需要认证
                    .anyRequest().authenticated()
            }
            // 添加 JWT 认证过滤器（在 UsernamePasswordAuthenticationFilter 之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        return http.build()
    }
    
    /**
     * 注册自定义参数解析器
     * 用于支持 @CurrentUser 注解
     */
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(CurrentUserArgumentResolver())
    }
}

