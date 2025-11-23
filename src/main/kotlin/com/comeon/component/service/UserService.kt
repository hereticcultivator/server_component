package com.comeon.component.service

import com.comeon.component.dto.*
import com.comeon.component.exception.CustomException
import com.comeon.component.mapper.UserMapper
import com.comeon.component.model.User
import com.comeon.component.model.UserRole
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userMapper: UserMapper
) {
    
    fun registerUser(request: UserRegistrationRequest): UserResponse {
        // 校验用户名不能为空
        if (request.username.isBlank()) {
            throw CustomException("用户名不能为空", "400")
        }
        
        // 校验密码不能为空
        if (request.password.isBlank()) {
            throw CustomException("密码不能为空", "400")
        }
        
        // 校验手机号不能为空
        if (request.phoneNumber.isBlank()) {
            throw CustomException("手机号不能为空", "400")
        }
        
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(request.username)) {
            throw CustomException("用户名已存在", "400")
        }
        
        // 检查手机号是否已存在
        if (userMapper.existsByPhoneNumber(request.phoneNumber)) {
            throw CustomException("手机号已被注册", "400")
        }
        
        // 创建新用户（明文密码）
        val currentTime = System.currentTimeMillis()
        val user = User(
            username = request.username,
            password = request.password,  // 直接存储明文密码
            phoneNumber = request.phoneNumber,
            fullName = request.fullName,
            role = UserRole.USER,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        userMapper.insert(user)
        return user.toResponse()
    }
    
    fun authenticateByUsername(username: String, password: String): User? {
        // 校验用户名不能为空
        if (username.isBlank()) {
            throw CustomException("用户名不能为空", "400")
        }
        
        // 校验密码不能为空
        if (password.isBlank()) {
            throw CustomException("密码不能为空", "400")
        }
        
        val user = userMapper.findByUsername(username) ?: return null
        
        // 简单密码比较（明文）
        return if (user.password == password) {
            // 更新最后登录时间
            val currentTime = System.currentTimeMillis()
            val updatedUser = user.copy(
                lastLoginAt = currentTime,
                updatedAt = currentTime
            )
            userMapper.updateLastLogin(updatedUser)
            updatedUser
        } else {
            null
        }
    }
    
    fun authenticateByPhone(phoneNumber: String, password: String): User? {
        // 校验手机号不能为空
        if (phoneNumber.isBlank()) {
            throw CustomException("手机号不能为空", "400")
        }
        
        // 校验密码不能为空
        if (password.isBlank()) {
            throw CustomException("密码不能为空", "400")
        }
        
        val user = userMapper.findByPhoneNumber(phoneNumber) ?: return null
        
        // 简单密码比较（明文）
        return if (user.password == password) {
            // 更新最后登录时间
            val currentTime = System.currentTimeMillis()
            val updatedUser = user.copy(
                lastLoginAt = currentTime,
                updatedAt = currentTime
            )
            userMapper.updateLastLogin(updatedUser)
            updatedUser
        } else {
            null
        }
    }
    
    fun getUserById(id: Long): User? {
        return userMapper.findById(id)
    }
    
    fun getUserByUsername(username: String): User? {
        return userMapper.findByUsername(username)
    }
    
    fun bindPhoneNumber(userId: Long, phoneNumber: String): UserResponse {
        // 校验手机号不能为空
        if (phoneNumber.isBlank()) {
            throw CustomException("手机号不能为空", "400")
        }
        
        // 校验手机号位数（11位）
        if (phoneNumber.length != 11) {
            throw CustomException("手机号必须是11位数字", "400")
        }
        
        // 检查手机号是否已被其他用户使用
        val existingUser = userMapper.findByPhoneNumber(phoneNumber)
        if (existingUser != null && existingUser.id != userId) {
            throw CustomException("手机号已被其他用户使用", "400")
        }
        
        // 获取用户信息
        val user = userMapper.findById(userId)
            ?: throw CustomException("用户不存在", "404")
        
        // 更新手机号
        val updatedUser = user.copy(
            phoneNumber = phoneNumber,
            updatedAt = System.currentTimeMillis()
        )
        
        userMapper.update(updatedUser)
        return updatedUser.toResponse()
    }
    
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userMapper.findById(id)
            ?: throw CustomException("用户不存在", "404")
        
        // 检查手机号是否被其他用户使用
        if (request.phoneNumber != null && request.phoneNumber != user.phoneNumber) {
            // 校验手机号不能为空字符串
            if (request.phoneNumber.isBlank()) {
                throw CustomException("手机号不能为空", "400")
            }
            
            val existingUser = userMapper.findByPhoneNumber(request.phoneNumber)
            if (existingUser != null && existingUser.id != id) {
                throw CustomException("手机号已被其他用户使用", "400")
            }
        }
        
        val updatedUser = user.copy(
            fullName = request.fullName ?: user.fullName,
            phoneNumber = request.phoneNumber ?: user.phoneNumber,
            updatedAt = System.currentTimeMillis()
        )
        
        userMapper.update(updatedUser)
        return updatedUser.toResponse()
    }
    
    private fun User.toResponse(): UserResponse {
        return UserResponse(
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

