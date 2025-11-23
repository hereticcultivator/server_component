package com.comeon.component.controller

import com.comeon.component.common.Result
import com.comeon.component.dto.AuthResponse
import com.comeon.component.dto.BindPhoneRequest
import com.comeon.component.dto.PhoneLoginRequest
import com.comeon.component.dto.UserRegistrationRequest
import com.comeon.component.dto.UsernameLoginRequest
import com.comeon.component.service.AuthService
import com.comeon.component.service.UserService
import jakarta.annotation.Resource
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
    fun register(@RequestBody request: UserRegistrationRequest): ResponseEntity<Result> {
        val authResponse = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Result.success(data = authResponse, msg = "注册成功"))
    }
    
    /**
     * 账号密码登录
     * POST /api/auth/login/username
     */
    @PostMapping("/login/username")
    fun loginByUsername(@RequestBody request: UsernameLoginRequest): ResponseEntity<Result> {
        val authResponse = authService.loginByUsername(request)
        return ResponseEntity.ok(Result.success(data = authResponse, msg = "登录成功"))
    }
    
    /**
     * 手机号登录
     * POST /api/auth/login/phone
     */
    @PostMapping("/login/phone")
    fun loginByPhone(@RequestBody request: PhoneLoginRequest): ResponseEntity<Result> {
        val authResponse = authService.loginByPhone(request)
        return ResponseEntity.ok(Result.success(data = authResponse, msg = "登录成功"))
    }
    
    /**
     * 绑定手机号
     * POST /api/auth/bind-phone
     */
    @PostMapping("/bind-phone")
    fun bindPhone(
        @RequestParam userId: Long,
        @RequestBody request: BindPhoneRequest
    ): ResponseEntity<Result> {
        val userResponse = userService.bindPhoneNumber(userId, request.phoneNumber)
        return ResponseEntity.ok(Result.success(data = userResponse, msg = "绑定手机号成功"))
    }
}

