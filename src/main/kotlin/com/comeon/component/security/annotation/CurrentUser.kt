package com.comeon.component.security.annotation

/**
 * 当前用户参数注解
 * 用于自动注入当前登录用户的信息到 Controller 方法参数
 * 
 * 使用示例：
 * ```kotlin
 * @GetMapping("/profile")
 * fun getProfile(@CurrentUser userId: Long): UserResponse {
 *     return userService.getUserById(userId)
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser

