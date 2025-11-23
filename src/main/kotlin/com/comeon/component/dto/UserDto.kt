package com.comeon.component.dto

// 用户注册请求
data class UserRegistrationRequest(
    val username: String,
    val password: String,
    val phoneNumber: String,
    val fullName: String? = null
)

// 账号密码登录请求
data class UsernameLoginRequest(
    val username: String,
    val password: String
)

// 手机号登录请求
data class PhoneLoginRequest(
    val phoneNumber: String,
    val password: String
)

// 用户更新请求
data class UserUpdateRequest(
    val fullName: String? = null,
    val phoneNumber: String? = null
)

// 用户响应
data class UserResponse(
    val id: Long,
    val username: String,
    val phoneNumber: String,
    val fullName: String?,
    val role: Int,
    val createdAt: Long,
    val lastLoginAt: Long?
)

// 认证响应（简单版本，无 token）
data class AuthResponse(
    val user: UserResponse
)

// 绑定手机号请求
data class BindPhoneRequest(
    val phoneNumber: String
)

