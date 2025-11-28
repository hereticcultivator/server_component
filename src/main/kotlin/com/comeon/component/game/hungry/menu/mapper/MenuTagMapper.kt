package com.comeon.component.game.hungry.menu.mapper

import com.comeon.component.game.hungry.menu.model.MenuTag
import org.apache.ibatis.annotations.*

@Mapper
interface MenuTagMapper {
    
    @Insert("""
        INSERT INTO menu_tags (menu_id, tag_id, create_time)
        VALUES (#{menuId}, #{tagId}, #{createTime})
    """)
    fun insert(menuTag: MenuTag): Int
    
    @Select("SELECT * FROM menu_tags WHERE menu_id = #{menuId}")
    fun findByMenuId(menuId: String): List<MenuTag>
    
    @Delete("DELETE FROM menu_tags WHERE menu_id = #{menuId}")
    fun deleteByMenuId(menuId: String): Int
    
    @Delete("""
        <script>
        DELETE FROM menu_tags WHERE menu_id IN
        <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">
            #{menuId}
        </foreach>
        </script>
    """)
    fun deleteByMenuIds(menuIds: List<String>): Int
}

