package com.comeon.component.game.hungry.menu.model

/**
 * 菜单操作日志模型
 */
data class MenuOperationLog(
    var id: Long? = null,
    val menuId: String? = null,
    val userId: Long? = null,
    val username: String? = null,
    val operationType: String,  // create/update/delete/copy
    val operationDetail: String? = null,  // JSON格式
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

object MenuOperationType {
    const val CREATE = "create"
    const val UPDATE = "update"
    const val DELETE = "delete"
    const val COPY = "copy"
}

