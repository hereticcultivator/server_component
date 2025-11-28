package com.comeon.component.game.hungry.menu.service

import com.comeon.component.exception.CustomException
import com.comeon.component.game.hungry.menu.dto.TagCreateRequest
import com.comeon.component.game.hungry.menu.dto.TagResponse
import com.comeon.component.game.hungry.menu.mapper.TagMapper
import com.comeon.component.game.hungry.menu.model.Tag
import com.comeon.component.game.hungry.menu.model.TagType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagMapper: TagMapper
) {
    
    /**
     * 获取所有标签
     */
    fun getAllTags(): List<TagResponse> {
        val tags = tagMapper.findAll()
        return tags.map { TagResponse(it.id!!, it.name, it.type) }
    }
    
    /**
     * 创建自定义标签
     */
    @Transactional
    fun createTag(request: TagCreateRequest): TagResponse {
        // 验证标签名称长度
        if (request.name.length > 6) {
            throw CustomException("标签名称最多6个字", 400)
        }
        
        // 检查标签名称是否已存在
        if (tagMapper.existsByName(request.name)) {
            throw CustomException("标签名称已存在", 400)
        }
        
        // 生成标签ID
        val tagId = generateCustomTagId()
        
        // 创建标签
        val tag = Tag(
            id = tagId,
            name = request.name,
            type = TagType.CUSTOM,
            createTime = System.currentTimeMillis()
        )
        tagMapper.insert(tag)
        
        return TagResponse(tagId, request.name, TagType.CUSTOM)
    }
    
    /**
     * 生成自定义标签ID（格式：custom_label_1001）
     */
    private fun generateCustomTagId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "custom_label_${timestamp % 10000}_$random"
    }
}

