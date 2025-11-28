package com.comeon.component.game.hungry.menu.model

/**
 * 菜单标签关联模型
 */
data class MenuTag(
    var id: Long? = null,
    val menuId: String,
    val tagId: String,
    val createTime: Long = System.currentTimeMillis()
)

