package com.comeon.component.game.hungry.menu.controller

import com.comeon.component.common.Result
import com.comeon.component.game.hungry.menu.dto.TagCreateRequest
import com.comeon.component.game.hungry.menu.dto.TagResponse
import com.comeon.component.game.hungry.menu.service.TagService
import jakarta.annotation.Resource
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/hungry/tags")
class TagController {
    
    @Resource
    lateinit var tagService: TagService
    
    /**
     * 获取所有标签
     * GET /api/v1/game/hungry/tags
     */
    @GetMapping
    fun getAllTags(): ResponseEntity<Result<List<TagResponse>>> {
        val tags = tagService.getAllTags()
        return ResponseEntity.ok(Result.success(data = tags))
    }
    
    /**
     * 创建自定义标签
     * POST /api/v1/game/hungry/tags
     */
    @PostMapping
    fun createTag(
        @Valid @RequestBody request: TagCreateRequest
    ): ResponseEntity<Result<TagResponse>> {
        val tag = tagService.createTag(request)
        return ResponseEntity.ok(Result.success(data = tag, message = "创建成功"))
    }
}

