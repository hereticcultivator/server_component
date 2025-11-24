package com.comeon.component.security

import com.comeon.component.exception.CustomException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * SecurityContext 工具类
 * 用于从 Spring Security 的 SecurityContext 中获取当前登录用户信息
 * 替代原有的 UserContext，使用标准的 Spring Security 机制
 */
object SecurityContextHelper {
    
    /**
     * 获取当前认证信息
     * @throws IllegalStateException 如果用户未认证
     */
    fun getAuthentication(): Authentication {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("用户未认证，请确保请求已通过JWT验证")
        }
        return authentication
    }
    
    /**
     * 获取当前用户 Principal
     * @throws IllegalStateException 如果用户未认证
     */
    fun getCurrentUserPrincipal(): UserPrincipal {
        val authentication = getAuthentication()
        val principal = authentication.principal
        
        if (principal !is UserPrincipal) {
            throw IllegalStateException("认证信息类型错误，期望 UserPrincipal，实际: ${principal::class.simpleName}")
        }
        
        return principal
    }
    
    /**
     * 获取当前用户ID
     * @throws IllegalStateException 如果用户未认证
     */
    fun getCurrentUserId(): Long {
        return getCurrentUserPrincipal().userId
    }
    
    /**
     * 获取当前用户名
     * @throws IllegalStateException 如果用户未认证
     */
    fun getCurrentUsername(): String {
        return getCurrentUserPrincipal().usernameValue
    }
    
    /**
     * 获取当前用户角色
     * @throws IllegalStateException 如果用户未认证
     */
    fun getCurrentUserRole(): Int {
        return getCurrentUserPrincipal().role
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    fun isAdmin(): Boolean {
        return try {
            getCurrentUserRole() == 1
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查当前用户是否已认证
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }
    
    /**
     * 检查当前用户是否有指定角色
     */
    fun hasRole(role: String): Boolean {
        return try {
            val authentication = getAuthentication()
            authentication.authorities.any { it.authority == "ROLE_$role" }
        } catch (e: Exception) {
            false
        }
    }
}

