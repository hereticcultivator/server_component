package com.comeon.component.service

import com.comeon.component.dto.*
import com.comeon.component.exception.CustomException
import com.comeon.component.mapper.UserMapper
import com.comeon.component.model.User
import com.comeon.component.model.UserRole
import com.comeon.component.security.SecurityContextHelper
import com.comeon.component.util.PasswordValidator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder
) {
    
    fun registerUser(request: UserRegistrationRequest): UserResponse {
        // 校验用户名不能为空
        if (request.username.isBlank()) {
            throw CustomException("用户名不能为空", 400)
        }
        
        // 校验密码不能为空
        if (request.password.isBlank()) {
            throw CustomException("密码不能为空", 400)
        }
        
        // 校验密码强度（8位+数字字母组合）
        PasswordValidator.validate(request.password)
        
        // 校验手机号不能为空
        if (request.phoneNumber.isBlank()) {
            throw CustomException("手机号不能为空", 400)
        }
        
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(request.username)) {
            throw CustomException("用户名已存在", 400)
        }
        
        // 检查手机号是否已存在
        if (userMapper.existsByPhoneNumber(request.phoneNumber)) {
            throw CustomException("手机号已被注册", 400)
        }
        
        // 加密密码
        val encodedPassword = passwordEncoder.encode(request.password)
        
        // 创建新用户（加密密码）
        val currentTime = System.currentTimeMillis()
        val user = User(
            username = request.username,
            password = encodedPassword,  // 存储加密后的密码
            phoneNumber = request.phoneNumber,
            // 如果 fullName 为空，自动生成8位随机字母
            fullName = if (request.fullName.isNullOrBlank()) generateRandomFullName() else request.fullName,
            role = UserRole.USER,
            createdAt = currentTime,
            updatedAt = currentTime,
            tokenVersion = 0  // 初始化token版本号
        )
        
        userMapper.insert(user)
        return user.toResponse()
    }
    
    fun authenticateByUsername(username: String, password: String): User? {
        // 校验用户名不能为空
        if (username.isBlank()) {
            throw CustomException("用户名不能为空", 400)
        }
        
        // 校验密码不能为空
        if (password.isBlank()) {
            throw CustomException("密码不能为空", 400)
        }
        
        val user = userMapper.findByUsername(username) ?: return null
        
        // 使用BCrypt验证密码
        val matches = try {
            passwordEncoder.matches(password, user.password)
        } catch (e: Exception) {
            false
        }
        
        return if (matches) {
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
            throw CustomException("手机号不能为空", 400)
        }
        
        // 校验密码不能为空
        if (password.isBlank()) {
            throw CustomException("密码不能为空", 400)
        }
        
        val user = userMapper.findByPhoneNumber(phoneNumber) ?: return null
        
        // 使用BCrypt验证密码
        val matches = try {
            passwordEncoder.matches(password, user.password)
        } catch (e: Exception) {
            false
        }
        
        return if (matches) {
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
    
    /**
     * 递增Token版本号（用于RefreshToken轮换）
     */
    fun incrementTokenVersion(userId: Long) {
        val currentTime = System.currentTimeMillis()
        userMapper.incrementTokenVersion(userId, currentTime)
    }
    
    /**
     * 根据手机号查找用户
     */
    fun getUserByPhoneNumber(phoneNumber: String): User? {
        return userMapper.findByPhoneNumber(phoneNumber)
    }
    
    /**
     * 从手机号自动创建用户（用于自动注册场景）
     * @param phoneNumber 手机号
     * @param username 可选用户名，如果不提供则自动生成
     * @param fullName 可选全名
     * @param password 可选密码，如果不提供则留空（后续可设置）
     * @return 创建的用户
     */
    fun createUserFromPhone(
        phoneNumber: String,
        username: String? = null,
        fullName: String? = null,
        password: String? = null
    ): User {
        // 校验手机号不能为空
        if (phoneNumber.isBlank()) {
            throw CustomException("手机号不能为空", 400)
        }
        
        // 检查手机号是否已被注册
        if (userMapper.existsByPhoneNumber(phoneNumber)) {
            throw CustomException("手机号已被注册", 400)
        }
        
        // 生成唯一用户名
        val finalUsername = username ?: generateUniqueUsername(phoneNumber)
        
        // 检查生成的用户名是否已存在
        if (userMapper.existsByUsername(finalUsername)) {
            throw CustomException("用户名已存在", 400)
        }
        
        // 如果提供了密码，验证密码强度并加密
        val encodedPassword = if (password != null && password.isNotBlank()) {
            PasswordValidator.validate(password)
            passwordEncoder.encode(password)
        } else {
            ""  // 留空，后续可设置
        }
        
        // 创建新用户
        val currentTime = System.currentTimeMillis()
        val user = User(
            username = finalUsername,
            password = encodedPassword,
            phoneNumber = phoneNumber,
            // 如果 fullName 为空，自动生成8位随机字母
            fullName = if (fullName.isNullOrBlank()) generateRandomFullName() else fullName,
            role = UserRole.USER,
            createdAt = currentTime,
            updatedAt = currentTime,
            tokenVersion = 0
        )
        
        userMapper.insert(user)
        return user
    }
    
    /**
     * 生成8位随机字母组合作为默认昵称
     */
    private fun generateRandomFullName(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * 生成唯一用户名（基于手机号）
     * 策略：user_手机号后4位，如果冲突则添加序号
     */
    private fun generateUniqueUsername(phoneNumber: String): String {
        if (phoneNumber.length < 4) {
            throw CustomException("手机号格式不正确", 400)
        }
        
        val suffix = phoneNumber.substring(phoneNumber.length - 4)
        var baseUsername = "user_$suffix"
        var username = baseUsername
        var counter = 1
        
        // 如果用户名已存在，添加序号
        while (userMapper.existsByUsername(username)) {
            username = "${baseUsername}_$counter"
            counter++
            
            // 防止无限循环（理论上不会发生）
            if (counter > 1000) {
                // 使用时间戳作为后缀
                username = "user_${System.currentTimeMillis() % 10000}"
                break
            }
        }
        
        return username
    }
    
    /**
     * 更新用户密码
     */
    fun updatePassword(userId: Long, encodedPassword: String) {
        userMapper.updatePassword(userId, encodedPassword, System.currentTimeMillis())
    }
    
    /**
     * 更新用户最后登录时间
     */
    fun updateLastLoginAt(userId: Long) {
        val user = userMapper.findById(userId) ?: return
        val currentTime = System.currentTimeMillis()
        val updatedUser = user.copy(
            lastLoginAt = currentTime,
            updatedAt = currentTime
        )
        userMapper.updateLastLogin(updatedUser)
    }
    
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userMapper.findById(id)
            ?: throw CustomException("用户不存在", 404)
        
        // 检查手机号是否被其他用户使用
        if (request.phoneNumber != null && request.phoneNumber != user.phoneNumber) {
            // 校验手机号不能为空字符串
            if (request.phoneNumber.isBlank()) {
                throw CustomException("手机号不能为空", 400)
            }
            
            val existingUser = userMapper.findByPhoneNumber(request.phoneNumber)
            if (existingUser != null && existingUser.id != id) {
                throw CustomException("手机号已被其他用户使用", 400)
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
    
    /**
     * 校验数据权限
     * 确保用户只能操作自己的数据（除非是管理员）
     * 
     * @param targetUserId 目标用户ID
     * @param allowAdmin 是否允许管理员操作所有数据，默认为 true
     * @throws CustomException 如果权限校验失败
     */
    fun checkDataPermission(targetUserId: Long, allowAdmin: Boolean = true) {
        if (!SecurityContextHelper.isAuthenticated()) {
            throw CustomException("用户未认证", 401)
        }
        
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val currentUserRole = SecurityContextHelper.getCurrentUserRole()
        
        // 如果是管理员且允许管理员操作，则跳过校验
        if (allowAdmin && currentUserRole == UserRole.ADMIN) {
            return
        }
        
        // 校验权限：只能操作自己的数据
        if (currentUserId != targetUserId) {
            throw CustomException("无权操作其他用户的数据", 403)
        }
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    fun isCurrentUserAdmin(): Boolean {
        return try {
            SecurityContextHelper.isAdmin()
        } catch (e: Exception) {
            false
        }
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

