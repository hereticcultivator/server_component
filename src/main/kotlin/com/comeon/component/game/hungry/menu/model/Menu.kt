package com.comeon.component.game.hungry.menu.model

/**
 * 菜单模型（共享表）
 */
data class Menu(
    var id: String? = null,
    val title: String,
    val category: Int,  // 0=全部, 1=美团, 2=饿了么, 3=京东
    val iconUrl: String? = null,
    val extraInfo: String? = null,
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis()
)

// 分类常量
object MenuCategory {
    const val ALL = 0
    const val MEITUAN = 1
    const val ELEME = 2
    const val JD = 3
}

