package com.comeon.component.service

import com.comeon.component.dto.*
import com.comeon.component.exception.CustomException
import com.comeon.component.model.User
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService
) {
    
    fun register(request: UserRegistrationRequest): AuthResponse {
        val userResponse = userService.registerUser(request)
        return AuthResponse(user = userResponse)
    }
    
    fun loginByUsername(request: UsernameLoginRequest): AuthResponse {
        val user = userService.authenticateByUsername(request.username, request.password)
            ?: throw CustomException("用户名或密码错误", "401")
        
        val userResponse = user.toResponse()
        return AuthResponse(user = userResponse)
    }
    
    fun loginByPhone(request: PhoneLoginRequest): AuthResponse {
        val user = userService.authenticateByPhone(request.phoneNumber, request.password)
            ?: throw CustomException("手机号或密码错误", "401")
        
        val userResponse = user.toResponse()
        return AuthResponse(user = userResponse)
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

