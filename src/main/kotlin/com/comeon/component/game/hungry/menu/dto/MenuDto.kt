package com.comeon.component.game.hungry.menu.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 菜单标签响应
 */
data class TagResponse(
    val id: String,
    val name: String,
    val type: String
)

/**
 * 菜单响应
 */
data class MenuResponse(
    val id: String,
    val title: String,
    val category: Int,
    val iconUrl: String?,
    val extraInfo: String?,
    val tags: List<TagResponse>,
    val createTime: Long
)

/**
 * 创建菜单请求
 */
data class MenuCreateRequest(
    @field:NotBlank(message = "标题不能为空")
    @field:Size(max = 200, message = "标题长度不能超过200个字符")
    val title: String,
    
    val category: Int = 0,
    
    val iconUrl: String? = null,
    
    val extraInfo: String? = null,
    
    val tagIds: List<String> = emptyList()
)

/**
 * 更新菜单请求
 */
data class MenuUpdateRequest(
    @field:Size(max = 200, message = "标题长度不能超过200个字符")
    val title: String? = null,
    
    val category: Int? = null,
    
    val iconUrl: String? = null,
    
    val extraInfo: String? = null,
    
    val tagIds: List<String>? = null
)

/**
 * 批量删除请求
 */
data class MenuDeleteRequest(
    @field:NotBlank(message = "菜单ID列表不能为空")
    val ids: List<String>
)

/**
 * 分页响应（Spring Boot Page格式）
 */
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val last: Boolean,
    val first: Boolean,
    val empty: Boolean
)

/**
 * 标签创建请求
 */
data class TagCreateRequest(
    @field:NotBlank(message = "标签名称不能为空")
    @field:Size(max = 6, message = "标签名称最多6个字")
    val name: String
)

/**
 * 批量删除响应
 */
data class DeleteResponse(
    val deletedCount: Int
)

