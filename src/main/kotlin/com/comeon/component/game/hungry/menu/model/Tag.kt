package com.comeon.component.game.hungry.menu.model

/**
 * 标签模型（全局共享）
 */
data class Tag(
    var id: String? = null,
    val name: String,
    val type: String = "system",  // system/custom
    val createTime: Long = System.currentTimeMillis()
)

object TagType {
    const val SYSTEM = "system"
    const val CUSTOM = "custom"
}

