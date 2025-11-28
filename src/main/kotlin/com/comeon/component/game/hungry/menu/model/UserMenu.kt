package com.comeon.component.game.hungry.menu.model

/**
 * 用户菜单关联模型
 * 用于记录用户拥有的菜单，支持未来分享功能
 */
data class UserMenu(
    var id: Long? = null,
    val userId: Long,
    val menuId: String,
    val isOwner: Boolean = true,  // 是否拥有者
    val permission: String = "write",  // read/write
    val createTime: Long = System.currentTimeMillis()
)

object MenuPermission {
    const val READ = "read"
    const val WRITE = "write"
}

