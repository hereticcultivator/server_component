package com.comeon.component.model

/**
 * 登录日志模型
 */
data class LoginLog(
    var id: Long? = null,
    val userId: Long?,  // 改为可空，失败时可以为 null
    val username: String,
    val loginType: String,  // username/phone
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val loginStatus: String,  // success/failed
    val failureReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

