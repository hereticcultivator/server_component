package com.comeon.component.controller

import com.comeon.component.common.Result
import com.comeon.component.dto.*
import com.comeon.component.security.SecurityContextHelper
import com.comeon.component.security.annotation.CurrentUser
import com.comeon.component.security.annotation.RequireOwnership
import com.comeon.component.service.UserService
import jakarta.annotation.Resource
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 用户管理控制器
 * 演示如何使用 Spring Security 的 SecurityContext 和数据权限校验
 */
@RestController
@RequestMapping("/api/users")
class UserController {
    
    @Resource
    lateinit var userService: UserService
    
    /**
     * 获取当前用户信息
     * 使用 @CurrentUser 注解自动注入当前用户ID
     */
    @GetMapping("/me")
    fun getCurrentUser(@CurrentUser userId: Long): ResponseEntity<Result<UserResponse>> {
        val user = userService.getUserById(userId)
            ?: return ResponseEntity.ok(Result.error("用户不存在", code = 404))
        
        val userResponse = user.toResponse()
        return ResponseEntity.ok(Result.success(data = userResponse))
    }
    
    /**
     * 获取指定用户信息
     * 使用 @RequireOwnership 注解自动校验数据权限
     * 用户只能查看自己的信息，管理员可以查看所有用户
     */
    @GetMapping("/{userId}")
    @RequireOwnership(userIdParam = "userId", allowAdmin = true)
    fun getUserById(@PathVariable userId: Long): ResponseEntity<Result<UserResponse>> {
        val user = userService.getUserById(userId)
            ?: return ResponseEntity.ok(Result.error("用户不存在", code = 404))
        
        val userResponse = user.toResponse()
        return ResponseEntity.ok(Result.success(data = userResponse))
    }
    
    /**
     * 更新用户信息
     * 使用 @RequireOwnership 注解自动校验数据权限
     * 用户只能更新自己的信息，管理员可以更新所有用户
     */
    @PutMapping("/{userId}")
    @RequireOwnership(userIdParam = "userId", allowAdmin = true)
    fun updateUser(
        @PathVariable userId: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<Result<UserResponse>> {
        val userResponse = userService.updateUser(userId, request)
        return ResponseEntity.ok(Result.success(data = userResponse, message = "更新成功"))
    }
    
    /**
     * 绑定手机号
     * 使用 @CurrentUser 注解获取当前用户ID，确保用户只能绑定自己的手机号
     */
    @PostMapping("/bind-phone")
    fun bindPhone(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: BindPhoneRequest
    ): ResponseEntity<Result<UserResponse>> {
        val userResponse = userService.bindPhoneNumber(userId, request.phoneNumber)
        return ResponseEntity.ok(Result.success(data = userResponse, message = "绑定手机号成功"))
    }
    
    /**
     * 使用 SecurityContextHelper 手动获取用户信息的示例
     * 这种方式适用于需要更灵活控制的场景
     */
    @GetMapping("/profile")
    fun getProfile(): ResponseEntity<Result<UserResponse>> {
        val userId = SecurityContextHelper.getCurrentUserId()
        val username = SecurityContextHelper.getCurrentUsername()
        val role = SecurityContextHelper.getCurrentUserRole()
        val isAdmin = SecurityContextHelper.isAdmin()
        
        val user = userService.getUserById(userId)
            ?: return ResponseEntity.ok(Result.error("用户不存在", code = 404))
        
        val userResponse = user.toResponse()
        return ResponseEntity.ok(Result.success(data = userResponse))
    }
    
    private fun com.comeon.component.model.User.toResponse(): UserResponse {
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

