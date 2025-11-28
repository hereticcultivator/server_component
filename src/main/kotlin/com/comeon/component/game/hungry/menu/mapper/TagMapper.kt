package com.comeon.component.game.hungry.menu.mapper

import com.comeon.component.game.hungry.menu.model.Tag
import org.apache.ibatis.annotations.*

@Mapper
interface TagMapper {
    
    @Select("SELECT * FROM tags ORDER BY type, create_time DESC")
    fun findAll(): List<Tag>
    
    @Select("SELECT * FROM tags WHERE id = #{id}")
    fun findById(id: String): Tag?
    
    @Select("SELECT COUNT(*) > 0 FROM tags WHERE name = #{name}")
    fun existsByName(name: String): Boolean
    
    @Insert("""
        INSERT INTO tags (id, name, type, create_time)
        VALUES (#{id}, #{name}, #{type}, #{createTime})
    """)
    fun insert(tag: Tag): Int
}

