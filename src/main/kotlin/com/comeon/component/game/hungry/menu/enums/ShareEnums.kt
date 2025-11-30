package com.comeon.component.game.hungry.menu.enums

/**
 * 平台枚举
 * 与 MenuCategory 保持一致: 1=美团, 2=饿了么, 3=京东
 */
enum class Platform(val code: Int, val key: String) {
    UNKNOWN(0, "UNKNOWN"),
    MEITUAN(1, "MEITUAN"),
    ELEME(2, "ELEME"),
    JINGDONG(3, "JINGDONG"),
    TAOBAO(4, "TAOBAO");

    companion object {
        fun fromCode(code: Int): Platform {
            return entries.find { it.code == code } ?: UNKNOWN
        }
        
        fun fromKey(key: String): Platform {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

/**
 * 动作类型
 */
enum class ActionType {
    SCHEME,
    WEB,
    TOAST
}

