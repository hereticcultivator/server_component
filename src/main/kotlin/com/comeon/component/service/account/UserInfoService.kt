package com.comeon.component.service.account

import com.comeon.component.dto.UserInfo
import com.comeon.component.exception.CustomException
import com.comeon.component.security.SecurityContextHelper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

/**
 * 用户信息查询服务
 * 提供多种方式获取用户信息，方便业务服务使用
 * 
 * 支持的方式：
 * 1. 从Token中获取（适用于跨服务调用）
 * 2. 从SecurityContext获取（适用于当前请求已通过JWT验证）
 * 3. 可选认证（支持游客模式）
 */
@Service
class UserInfoService(
    private val accountService: AccountService
) {
    
    /**
     * 从Token中获取用户信息
     * 适用于：跨服务调用、API网关转发等场景
     * 
     * @param token JWT AccessToken
     * @return 用户信息
     * @throws CustomException 如果token无效或用户不存在
     */
    fun getUserInfoFromToken(token: String): UserInfo {
        return accountService.getUserInfoByToken(token)
            ?: throw CustomException("Token无效或用户不存在", 401)
    }
    
    /**
     * 从SecurityContext获取当前用户信息
     * 适用于：当前请求已通过JWT验证的场景
     * 
     * @return 用户信息
     * @throws CustomException 如果用户未认证
     */
    fun getCurrentUserInfo(): UserInfo {
        return try {
            val userId = SecurityContextHelper.getCurrentUserId()
            accountService.getUserInfoById(userId)
                ?: throw CustomException("用户不存在", 404)
        } catch (e: IllegalStateException) {
            throw CustomException("用户未认证，请确保请求已通过JWT验证", 401)
        }
    }
    
    /**
     * 从SecurityContext获取当前用户ID
     * 
     * @return 用户ID
     * @throws CustomException 如果用户未认证
     */
    fun getCurrentUserId(): Long {
        return try {
            SecurityContextHelper.getCurrentUserId()
        } catch (e: IllegalStateException) {
            throw CustomException("用户未认证，请确保请求已通过JWT验证", 401)
        }
    }
    
    /**
     * 从SecurityContext获取当前用户名
     * 
     * @return 用户名
     * @throws CustomException 如果用户未认证
     */
    fun getCurrentUsername(): String {
        return try {
            SecurityContextHelper.getCurrentUsername()
        } catch (e: IllegalStateException) {
            throw CustomException("用户未认证，请确保请求已通过JWT验证", 401)
        }
    }
    
    /**
     * 检查当前用户是否为管理员
     * 
     * @return true表示是管理员，false表示不是
     */
    fun isCurrentUserAdmin(): Boolean {
        return try {
            SecurityContextHelper.isAdmin()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查当前用户是否已认证
     * 
     * @return true表示已认证，false表示未认证
     */
    fun isAuthenticated(): Boolean {
        return SecurityContextHelper.isAuthenticated()
    }
    
    /**
     * 根据用户ID获取用户信息（业务服务使用）
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws CustomException 如果用户不存在
     */
    fun getUserInfoById(userId: Long): UserInfo {
        return accountService.getUserInfoById(userId)
            ?: throw CustomException("用户不存在", 404)
    }
    
    /**
     * 批量获取用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    fun getUsersByIds(userIds: List<Long>): List<UserInfo> {
        return accountService.getUsersByIds(userIds)
    }
    
    // ========== 可选认证相关方法 ==========
    
    /**
     * 获取当前用户信息（可选认证）
     * 适用于支持游客模式的接口
     * 
     * @return 用户信息，如果是游客则返回null
     */
    fun getCurrentUserInfoOrNull(): UserInfo? {
        return if (isAuthenticated()) {
            try {
                getCurrentUserInfo()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * 获取当前用户ID（可选认证）
     * 
     * @return 用户ID，如果是游客则返回null
     */
    fun getCurrentUserIdOrNull(): Long? {
        return if (isAuthenticated()) {
            try {
                getCurrentUserId()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * 检查是否为游客（未认证用户）
     * 
     * @return true表示是游客，false表示已登录
     */
    fun isGuest(): Boolean {
        return !isAuthenticated()
    }
    
    /**
     * 执行需要认证的操作
     * 如果用户未认证，会抛出异常
     * 
     * @return 用户信息
     * @throws CustomException 如果用户未认证
     */
    fun requireAuthentication(): UserInfo {
        return getCurrentUserInfo()
    }
    
    /**
     * 执行需要认证的操作（获取用户ID）
     * 
     * @return 用户ID
     * @throws CustomException 如果用户未认证
     */
    fun requireAuthenticationUserId(): Long {
        return getCurrentUserId()
    }
}

