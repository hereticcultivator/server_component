package com.comeon.component.service.account

import com.comeon.component.dto.UserInfo
import com.comeon.component.exception.CustomException
import com.comeon.component.model.User
import com.comeon.component.service.TokenService
import com.comeon.component.service.UserService
import org.springframework.stereotype.Service

/**
 * 账号服务实现类
 * 实现AccountService接口，封装账号相关的业务逻辑
 */
@Service
class AccountServiceImpl(
    private val tokenService: TokenService,
    private val userService: UserService
) : AccountService {
    
    override fun getUserInfoByToken(token: String): UserInfo? {
        return try {
            // 验证token并获取claims
            val claims = tokenService.validateToken(token, "access")
            val userId = claims.subject.toLong()
            
            // 从数据库获取用户信息
            val user = userService.getUserById(userId) ?: return null
            
            // 转换为UserInfo（不包含敏感信息）
            user.toUserInfo()
        } catch (e: CustomException) {
            // Token无效或过期（预期的业务异常）
            null
        }
        // 不捕获其他异常，让它们向上传播（如数据库连接异常等）
    }
    
    override fun getUserInfoById(userId: Long): UserInfo? {
        return userService.getUserById(userId)?.toUserInfo()
    }
    
    override fun getUserInfoByUsername(username: String): UserInfo? {
        return userService.getUserByUsername(username)?.toUserInfo()
    }
    
    override fun getUserInfoByPhoneNumber(phoneNumber: String): UserInfo? {
        return userService.getUserByPhoneNumber(phoneNumber)?.toUserInfo()
    }
    
    override fun validateToken(token: String): Boolean {
        return try {
            tokenService.validateToken(token, "access")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getUsersByIds(userIds: List<Long>): List<UserInfo> {
        return userIds.mapNotNull { userId ->
            userService.getUserById(userId)?.toUserInfo()
        }
    }
    
    override fun userExists(userId: Long): Boolean {
        return userService.getUserById(userId) != null
    }
    
    override fun isAdmin(userId: Long): Boolean {
        val user = userService.getUserById(userId) ?: return false
        return user.role == 1
    }
    
    /**
     * 将User模型转换为UserInfo DTO
     */
    private fun User.toUserInfo(): UserInfo {
        return UserInfo(
            id = this.id!!,
            username = this.username,
            phoneNumber = this.phoneNumber,
            fullName = this.fullName,
            role = this.role,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt
        )
    }
}

