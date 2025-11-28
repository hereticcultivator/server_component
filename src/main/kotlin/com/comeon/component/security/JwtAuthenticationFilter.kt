package com.comeon.component.security

import com.comeon.component.exception.CustomException
import com.comeon.component.service.TokenService
import com.comeon.component.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 认证过滤器
 * 替代原有的 TokenInterceptor，集成到 Spring Security 认证流程中
 * 从请求头中提取 JWT token，验证后设置到 SecurityContext
 */
@Component
class JwtAuthenticationFilter(
    private val tokenService: TokenService,
    private val userService: UserService
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    
    /**
     * 白名单路径（不需要token验证的路径）
     */
    private val publicPaths = listOf(
        "/api/auth/login",
        "/api/auth/send-code",
        "/api/auth/refresh",
        "/api/auth/password-reset",
        "/api/test",
        "/api/v1/game/hungry/tags"  // 获取标签接口公开
    )
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
        
        // 检查是否为公开路径
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response)
            return
        }
        
        // 从请求头获取token
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        
        // 提取token
        val token = authHeader.substring(7).trim()
        if (token.isEmpty()) {
            filterChain.doFilter(request, response)
            return
        }
        
        try {
            // 验证token（只接受access token）
            val claims = tokenService.validateToken(token, "access")
            val userId = claims.subject.toLong()
            
            // 从数据库加载用户信息
            val user = userService.getUserById(userId)
                ?: throw CustomException("用户不存在", 404)
            
            // 创建 UserPrincipal
            val userPrincipal = UserPrincipal.from(user)
            
            // 创建认证对象
            val authentication = UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.authorities
            )
            
            // 设置请求详情
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            
            // 设置到 SecurityContext
            SecurityContextHolder.getContext().authentication = authentication
            
        } catch (e: CustomException) {
            // 自定义异常直接抛出
            throw e
        }
        // 不捕获其他异常（如数据库连接异常等），让它们向上传播
        // TokenService.validateToken 已经将所有 JWT 异常转换为 CustomException
        
        filterChain.doFilter(request, response)
    }
    
    /**
     * 检查路径是否为公开路径
     */
    private fun isPublicPath(path: String): Boolean {
        return publicPaths.any { path.startsWith(it) }
    }
}

