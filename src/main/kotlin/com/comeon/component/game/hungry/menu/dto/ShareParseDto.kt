package com.comeon.component.game.hungry.menu.dto

import com.comeon.component.game.hungry.menu.enums.ActionType
import com.comeon.component.game.hungry.menu.enums.Platform

/**
 * 解析请求
 */
data class ParseRequest(
    val text: String
)

/**
 * 解析结果
 */
data class ParseResult(
    val meta: MetaInfo,
    val actions: List<JumpAction>
)

/**
 * 元数据信息
 */
data class MetaInfo(
    val title: String,
    val desc: String,
    val icon: String?,
    val platform: String // 返回字符串枚举值
)

/**
 * 跳转动作
 */
data class JumpAction(
    val type: ActionType,
    val packageName: String? = null,
    val uri: String,
    val intentFlags: List<String> = emptyList()
)

/**
 * 内部使用的店铺信息
 */
data class ShopInfo(
    val platform: Platform,
    val shopName: String,
    val originalUrl: String,
    val realUrl: String,
    val shopId: String? = null,
    val poiId: String? = null, // 美团POI ID
    val extra: Map<String, String> = emptyMap()
)

