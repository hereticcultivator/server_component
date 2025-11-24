package com.comeon.component.dto

/**
 * 统一的用户信息结构（不包含敏感信息）
 * 用于业务服务间传递用户信息，避免暴露密码等敏感数据
 */
data class UserInfo(
    val id: Long,
    val username: String,
    val phoneNumber: String,
    val fullName: String?,
    val role: Int,
    val createdAt: Long,
    val lastLoginAt: Long?
) {
    /**
     * 是否为管理员
     */
    val isAdmin: Boolean
        get() = role == 1
    
    /**
     * 是否为普通用户
     */
    val isUser: Boolean
        get() = role == 0
}

