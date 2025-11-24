package com.comeon.component.security.annotation

/**
 * 数据权限校验注解
 * 用于标记需要验证数据所有权的接口
 * 
 * 使用场景：
 * - 用户只能操作自己的数据
 * - 管理员可以操作所有数据
 * 
 * @param userIdParam 请求参数中用户ID的字段名，默认为 "userId"
 * @param allowAdmin 是否允许管理员操作所有数据，默认为 true
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireOwnership(
    val userIdParam: String = "userId",
    val allowAdmin: Boolean = true
)

