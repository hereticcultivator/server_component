package com.comeon.component.controller

import com.comeon.component.common.Result
import com.comeon.component.dto.*
import com.comeon.component.service.AuthService
import com.comeon.component.service.UserService
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 用户认证控制器
 * 提供用户注册、登录等认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {
    
    @Resource
    lateinit var authService: AuthService
    
    @Resource
    lateinit var userService: UserService
    
    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: UserRegistrationRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Result<AuthResponse>> {
        val authResponse = authService.register(request, httpRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Result.success(data = authResponse, message = "注册成功"))
    }
    
    /**
     * 统一登录接口（方案A）
     * POST /api/auth/login
     * 支持多种登录方式：phone_code, phone_password, username_password
     * 支持自动注册（仅手机号登录方式）
     */
    @PostMapping("/login")
    fun unifiedLogin(
        @Valid @RequestBody request: UnifiedLoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Result<AuthResponse>> {
        val authResponse = authService.unifiedLogin(request, httpRequest)
        val message = if (authResponse.isNewUser) "注册并登录成功" else "登录成功"
        return ResponseEntity.ok(Result.success(data = authResponse, message = message))
    }
    
    /**
     * 发送验证码（登录/注册通用）
     * POST /api/auth/send-code
     */
    @PostMapping("/send-code")
    fun sendCode(
        @Valid @RequestBody request: SendCodeRequest
    ): ResponseEntity<Result<Nothing>> {
        authService.sendCode(request.phoneNumber, request.purpose)
        return ResponseEntity.ok(Result.success(message = "验证码已发送"))
    }
    
    /**
     * 刷新Token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<Result<RefreshTokenResponse>> {
        val refreshResponse = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(Result.success(data = refreshResponse, message = "刷新成功"))
    }
    
    /**
     * 发送密码重置验证码
     * POST /api/auth/password-reset/send-code
     */
    @PostMapping("/password-reset/send-code")
    fun sendPasswordResetCode(
        @Valid @RequestBody request: PasswordResetCodeRequest
    ): ResponseEntity<Result<Nothing>> {
        authService.sendPasswordResetCode(request.phoneNumber)
        return ResponseEntity.ok(Result.success(message = "验证码已发送"))
    }
    
    /**
     * 重置密码
     * POST /api/auth/password-reset
     */
    @PostMapping("/password-reset")
    fun resetPassword(
        @Valid @RequestBody request: PasswordResetRequest
    ): ResponseEntity<Result<Nothing>> {
        authService.resetPassword(request)
        return ResponseEntity.ok(Result.success(message = "密码重置成功"))
    }
}

