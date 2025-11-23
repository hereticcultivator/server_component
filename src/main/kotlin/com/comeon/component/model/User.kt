package com.comeon.component.model

data class User(
    val id: Long? = null,
    val username: String,
    val password: String,
    val phoneNumber: String,
    val fullName: String? = null,
    val role: Int = 0,  // 0=USER, 1=ADMIN
    val createdAt: Long = System.currentTimeMillis(),  // 时间戳（毫秒）
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)

// 角色常量
object UserRole {
    const val USER = 0
    const val ADMIN = 1
}

