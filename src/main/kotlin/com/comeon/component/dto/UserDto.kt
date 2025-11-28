package com.comeon.component.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

// 用户注册请求
data class UserRegistrationRequest(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    val username: String,
    
    @field:NotBlank(message = "密码不能为空")
    val password: String,
    
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phoneNumber: String,
    
    val fullName: String? = null
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

// Token信息结构体（统一封装所有token相关字段）
data class TokenInfo(
    val accessToken: String,      // 访问令牌（短期）
    val refreshToken: String      // 刷新令牌（长期）
)

// 认证响应（包含 token）
data class AuthResponse(
    val user: UserResponse,
    val tokens: TokenInfo,  // 使用统一的token结构
    val isNewUser: Boolean = false  // 标识是否为新注册用户（自动注册场景）
)

// 刷新Token请求
data class RefreshTokenRequest(
    val refreshToken: String
)

// 刷新Token响应
data class RefreshTokenResponse(
    val tokens: TokenInfo  // 使用统一的token结构
)

// 发送密码重置验证码请求
data class PasswordResetCodeRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phoneNumber: String
)

// 密码重置请求
data class PasswordResetRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phoneNumber: String,
    
    @field:NotBlank(message = "验证码不能为空")
    @field:Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    val verificationCode: String,
    
    @field:NotBlank(message = "新密码不能为空")
    val newPassword: String
)

// 统一登录请求（方案A：支持多种登录方式和自动注册）
data class UnifiedLoginRequest(
    @field:NotBlank(message = "登录类型不能为空")
    val loginType: String,  // "phone_code" | "phone_password" | "username_password"
    
    // 手机号相关（phone_code 和 phone_password 需要）
    val phoneNumber: String? = null,
    val verificationCode: String? = null,  // 验证码（phone_code 需要）
    
    // 账号密码相关（phone_password 和 username_password 需要）
    val username: String? = null,
    val password: String? = null,
    
    // 自动注册相关（可选）
    val autoRegister: Boolean = true,  // 是否允许自动注册（仅手机号登录支持）
    val fullName: String? = null  // 自动注册时的用户信息
)

// 发送验证码请求（登录/注册通用）
data class SendCodeRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phoneNumber: String,
    
    val purpose: String = "login"  // "login" | "register" | "reset_password"
)

