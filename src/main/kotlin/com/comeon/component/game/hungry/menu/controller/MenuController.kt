package com.comeon.component.game.hungry.menu.controller

import com.comeon.component.common.Result
import com.comeon.component.game.hungry.menu.dto.*
import com.comeon.component.game.hungry.menu.service.MenuService
import com.comeon.component.security.annotation.CurrentUser
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/hungry/menus")
class MenuController {
    
    @Resource
    lateinit var menuService: MenuService
    
    /**
     * 获取菜单列表（分页）- 只返回当前用户的菜单
     * GET /api/v1/game/hungry/menus?page=0&size=20&category=1&q=关键词
     */
    @GetMapping
    fun getMenuList(
        @CurrentUser userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) category: Int?,
        @RequestParam(required = false) q: String?
    ): ResponseEntity<Result<PageResponse<MenuResponse>>> {
        val pageResponse = menuService.getMenuList(page, size, category, q)
        return ResponseEntity.ok(Result.success(data = pageResponse))
    }
    
    /**
     * 获取菜单详情 - 只能查看自己的菜单
     * GET /api/v1/game/hungry/menus/{id}
     */
    @GetMapping("/{id}")
    fun getMenuById(
        @CurrentUser userId: Long,
        @PathVariable id: String
    ): ResponseEntity<Result<MenuResponse>> {
        val menu = menuService.getMenuById(id)
        return ResponseEntity.ok(Result.success(data = menu))
    }
    
    /**
     * 创建菜单 - 自动归属当前用户
     * POST /api/v1/game/hungry/menus
     */
    @PostMapping
    fun createMenu(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: MenuCreateRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Result<MenuResponse>> {
        val menu = menuService.createMenu(request, httpRequest)
        return ResponseEntity.ok(Result.success(data = menu, message = "创建成功"))
    }
    
    /**
     * 更新菜单 - 只能更新自己的菜单
     * PUT /api/v1/game/hungry/menus/{id}
     */
    @PutMapping("/{id}")
    fun updateMenu(
        @CurrentUser userId: Long,
        @PathVariable id: String,
        @Valid @RequestBody request: MenuUpdateRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Result<MenuResponse>> {
        val menu = menuService.updateMenu(id, request, httpRequest)
        return ResponseEntity.ok(Result.success(data = menu, message = "更新成功"))
    }
    
    /**
     * 批量删除菜单 - 只能删除自己的菜单
     * DELETE /api/v1/game/hungry/menus
     */
    @DeleteMapping
    fun deleteMenus(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: MenuDeleteRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Result<DeleteResponse>> {
        val result = menuService.deleteMenus(request, httpRequest)
        return ResponseEntity.ok(Result.success(data = result, message = "删除成功"))
    }
    
    /**
     * 复制菜单 - 新菜单归属当前用户
     * POST /api/v1/game/hungry/menus/{id}/copy
     */
    @PostMapping("/{id}/copy")
    fun copyMenu(
        @CurrentUser userId: Long,
        @PathVariable id: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Result<MenuResponse>> {
        val menu = menuService.copyMenu(id, httpRequest)
        return ResponseEntity.ok(Result.success(data = menu, message = "复制成功"))
    }
}

