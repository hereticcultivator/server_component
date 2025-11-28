package com.comeon.component.game.hungry.menu.service

import com.comeon.component.exception.CustomException
import com.comeon.component.game.hungry.menu.dto.*
import com.comeon.component.game.hungry.menu.mapper.*
import com.comeon.component.game.hungry.menu.model.*
import com.comeon.component.security.SecurityContextHelper
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuService(
    private val menuMapper: MenuMapper,
    private val userMenuMapper: UserMenuMapper,
    private val tagMapper: TagMapper,
    private val menuTagMapper: MenuTagMapper,
    private val menuOperationLogMapper: MenuOperationLogMapper,
    private val objectMapper: ObjectMapper
) {
    
    /**
     * 获取菜单列表（分页）- 通过关联表过滤
     */
    fun getMenuList(
        page: Int = 0,
        size: Int = 20,
        category: Int? = null,
        keyword: String? = null
    ): PageResponse<MenuResponse> {
        // 获取当前用户信息
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val isAdmin = SecurityContextHelper.isAdmin()
        
        val offset = page * size
        val menus = menuMapper.findList(
            userId = if (isAdmin) null else currentUserId,
            isAdmin = isAdmin,
            category = category,
            keyword = keyword,
            offset = offset,
            limit = size
        )
        val total = menuMapper.count(
            userId = if (isAdmin) null else currentUserId,
            isAdmin = isAdmin,
            category = category,
            keyword = keyword
        )
        
        val menuResponses = menus.map { menu ->
            val tags = getMenuTags(menu.id!!)
            menu.toResponse(tags)
        }
        
        val totalPages = if (total > 0) ((total - 1) / size + 1).toInt() else 0
        
        return PageResponse(
            content = menuResponses,
            totalElements = total,
            totalPages = totalPages,
            number = page,
            size = size,
            last = page >= totalPages - 1,
            first = page == 0,
            empty = menuResponses.isEmpty()
        )
    }
    
    /**
     * 获取菜单详情 - 通过关联表校验权限
     */
    fun getMenuById(id: String): MenuResponse {
        val menu = menuMapper.findById(id)
            ?: throw CustomException("菜单不存在", 404)
        
        // 权限校验：通过关联表检查用户是否有权限访问
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val isAdmin = SecurityContextHelper.isAdmin()
        
        if (!isAdmin && !userMenuMapper.hasAccess(currentUserId, id)) {
            throw CustomException("无权访问该菜单", 403)
        }
        
        val tags = getMenuTags(id)
        return menu.toResponse(tags)
    }
    
    /**
     * 创建菜单 - 创建菜单并建立用户关联
     */
    @Transactional
    fun createMenu(request: MenuCreateRequest, httpRequest: HttpServletRequest): MenuResponse {
        // 获取当前用户ID
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        
        // 生成菜单ID
        val menuId = generateMenuId()
        
        // 验证标签是否存在
        if (request.tagIds.isNotEmpty()) {
            validateTagIds(request.tagIds)
        }
        
        // 创建菜单（共享表）
        val currentTime = System.currentTimeMillis()
        val menu = Menu(
            id = menuId,
            title = request.title,
            category = request.category,
            iconUrl = request.iconUrl,
            extraInfo = request.extraInfo,
            createTime = currentTime,
            updateTime = currentTime
        )
        menuMapper.insert(menu)
        
        // 建立用户菜单关联（当前用户为拥有者）
        userMenuMapper.insert(UserMenu(
            userId = currentUserId,
            menuId = menuId,
            isOwner = true,
            permission = MenuPermission.WRITE,
            createTime = currentTime
        ))
        
        // 关联标签
        if (request.tagIds.isNotEmpty()) {
            request.tagIds.forEach { tagId ->
                menuTagMapper.insert(MenuTag(
                    menuId = menuId,
                    tagId = tagId,
                    createTime = currentTime
                ))
            }
        }
        
        // 记录操作日志
        logOperation(
            menuId = menuId,
            operationType = MenuOperationType.CREATE,
            operationDetail = objectMapper.writeValueAsString(request),
            httpRequest = httpRequest
        )
        
        return getMenuById(menuId)
    }
    
    /**
     * 更新菜单 - 通过关联表校验权限
     */
    @Transactional
    fun updateMenu(id: String, request: MenuUpdateRequest, httpRequest: HttpServletRequest): MenuResponse {
        val menu = menuMapper.findById(id)
            ?: throw CustomException("菜单不存在", 404)
        
        // 权限校验：检查用户是否有编辑权限
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val isAdmin = SecurityContextHelper.isAdmin()
        
        if (!isAdmin && !userMenuMapper.hasWritePermission(currentUserId, id)) {
            throw CustomException("无权修改该菜单", 403)
        }
        
        // 验证标签是否存在
        if (request.tagIds != null && request.tagIds.isNotEmpty()) {
            validateTagIds(request.tagIds)
        }
        
        // 更新菜单
        val updatedMenu = menu.copy(
            title = request.title ?: menu.title,
            category = request.category ?: menu.category,
            iconUrl = request.iconUrl ?: menu.iconUrl,
            extraInfo = request.extraInfo ?: menu.extraInfo,
            updateTime = System.currentTimeMillis()
        )
        menuMapper.update(updatedMenu)
        
        // 更新标签关联
        if (request.tagIds != null) {
            menuTagMapper.deleteByMenuId(id)
            val currentTime = System.currentTimeMillis()
            request.tagIds.forEach { tagId ->
                menuTagMapper.insert(MenuTag(
                    menuId = id,
                    tagId = tagId,
                    createTime = currentTime
                ))
            }
        }
        
        // 记录操作日志
        logOperation(
            menuId = id,
            operationType = MenuOperationType.UPDATE,
            operationDetail = objectMapper.writeValueAsString(request),
            httpRequest = httpRequest
        )
        
        return getMenuById(id)
    }
    
    /**
     * 批量删除菜单 - 只能删除自己拥有的菜单
     */
    @Transactional
    fun deleteMenus(request: MenuDeleteRequest, httpRequest: HttpServletRequest): DeleteResponse {
        if (request.ids.isEmpty()) {
            throw CustomException("菜单ID列表不能为空", 400)
        }
        
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val isAdmin = SecurityContextHelper.isAdmin()
        
        // 校验所有权：用户只能删除自己拥有的菜单
        if (!isAdmin) {
            request.ids.forEach { menuId ->
                if (!userMenuMapper.isOwner(currentUserId, menuId)) {
                    throw CustomException("无权删除该菜单: $menuId（只能删除自己拥有的菜单）", 403)
                }
            }
        }
        
        // 删除用户菜单关联
        userMenuMapper.deleteByMenuIds(request.ids)
        
        // 删除标签关联
        menuTagMapper.deleteByMenuIds(request.ids)
        
        // 删除菜单（如果菜单没有被其他用户关联，可以考虑物理删除或逻辑删除）
        // 这里采用物理删除，如果菜单还有其他用户关联，需要先检查
        // 为了安全，只删除当前用户拥有的菜单
        val deletedCount = if (isAdmin) {
            menuMapper.deleteByIds(request.ids)
        } else {
            // 非管理员只能删除自己拥有的菜单
            request.ids.count { menuId ->
                if (userMenuMapper.isOwner(currentUserId, menuId)) {
                    menuMapper.deleteById(menuId) > 0
                } else {
                    false
                }
            }
        }
        
        // 记录操作日志
        request.ids.forEach { menuId ->
            logOperation(
                menuId = menuId,
                operationType = MenuOperationType.DELETE,
                operationDetail = objectMapper.writeValueAsString(mapOf("ids" to request.ids)),
                httpRequest = httpRequest
            )
        }
        
        return DeleteResponse(deletedCount = deletedCount)
    }
    
    /**
     * 复制菜单 - 创建新菜单并关联到当前用户
     */
    @Transactional
    fun copyMenu(id: String, httpRequest: HttpServletRequest): MenuResponse {
        val originalMenu = menuMapper.findById(id)
            ?: throw CustomException("菜单不存在", 404)
        
        // 权限校验：用户只能复制自己有权限访问的菜单
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val isAdmin = SecurityContextHelper.isAdmin()
        
        if (!isAdmin && !userMenuMapper.hasAccess(currentUserId, id)) {
            throw CustomException("无权复制该菜单", 403)
        }
        
        // 生成新菜单ID
        val newMenuId = generateMenuId()
        
        // 创建副本（共享表）
        val currentTime = System.currentTimeMillis()
        val newMenu = originalMenu.copy(
            id = newMenuId,
            title = "${originalMenu.title} (副本)",
            createTime = currentTime,
            updateTime = currentTime
        )
        menuMapper.insert(newMenu)
        
        // 建立用户菜单关联（当前用户为拥有者）
        userMenuMapper.insert(UserMenu(
            userId = currentUserId,
            menuId = newMenuId,
            isOwner = true,
            permission = MenuPermission.WRITE,
            createTime = currentTime
        ))
        
        // 复制标签关联
        val originalTags = menuTagMapper.findByMenuId(id)
        originalTags.forEach { menuTag ->
            menuTagMapper.insert(MenuTag(
                menuId = newMenuId,
                tagId = menuTag.tagId,
                createTime = currentTime
            ))
        }
        
        // 记录操作日志
        logOperation(
            menuId = newMenuId,
            operationType = MenuOperationType.COPY,
            operationDetail = objectMapper.writeValueAsString(mapOf("originalMenuId" to id)),
            httpRequest = httpRequest
        )
        
        return getMenuById(newMenuId)
    }
    
    /**
     * 获取菜单的标签列表
     */
    private fun getMenuTags(menuId: String): List<TagResponse> {
        val menuTags = menuTagMapper.findByMenuId(menuId)
        return menuTags.mapNotNull { menuTag ->
            val tag = tagMapper.findById(menuTag.tagId)
            tag?.let { TagResponse(it.id!!, it.name, it.type) }
        }
    }
    
    /**
     * 验证标签ID是否存在
     */
    private fun validateTagIds(tagIds: List<String>) {
        tagIds.forEach { tagId ->
            val tag = tagMapper.findById(tagId)
            if (tag == null) {
                throw CustomException("标签不存在: $tagId", 400)
            }
        }
    }
    
    /**
     * 生成菜单ID（格式：menu_1001）
     */
    private fun generateMenuId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "menu_${timestamp % 10000}_$random"
    }
    
    /**
     * 记录操作日志
     */
    private fun logOperation(
        menuId: String?,
        operationType: String,
        operationDetail: String?,
        httpRequest: HttpServletRequest
    ) {
        val userId = try {
            SecurityContextHelper.getCurrentUserId()
        } catch (e: Exception) {
            null
        }
        
        val username = try {
            SecurityContextHelper.getCurrentUsername()
        } catch (e: Exception) {
            null
        }
        
        val log = MenuOperationLog(
            menuId = menuId,
            userId = userId,
            username = username,
            operationType = operationType,
            operationDetail = operationDetail,
            ipAddress = getClientIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent"),
            createdAt = System.currentTimeMillis()
        )
        
        menuOperationLogMapper.insert(log)
    }
    
    /**
     * 获取客户端IP地址
     */
    private fun getClientIpAddress(request: HttpServletRequest): String? {
        var ip = request.getHeader("X-Forwarded-For")
        if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        return ip?.split(",")?.firstOrNull()?.trim()
    }
    
    /**
     * Menu转MenuResponse
     */
    private fun Menu.toResponse(tags: List<TagResponse>): MenuResponse {
        return MenuResponse(
            id = this.id!!,
            title = this.title,
            category = this.category,
            iconUrl = this.iconUrl,
            extraInfo = this.extraInfo,
            tags = tags,
            createTime = this.createTime
        )
    }
}

