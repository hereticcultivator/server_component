package com.comeon.component.game.hungry.menu.mapper

import com.comeon.component.game.hungry.menu.model.UserMenu
import org.apache.ibatis.annotations.*

@Mapper
interface UserMenuMapper {
    
    @Insert("""
        INSERT INTO user_menus (user_id, menu_id, is_owner, permission, create_time)
        VALUES (#{userId}, #{menuId}, #{isOwner}, #{permission}, #{createTime})
    """)
    fun insert(userMenu: UserMenu): Int
    
    @Select("SELECT * FROM user_menus WHERE user_id = #{userId} AND menu_id = #{menuId}")
    fun findByUserAndMenu(userId: Long, menuId: String): UserMenu?
    
    @Select("SELECT * FROM user_menus WHERE menu_id = #{menuId}")
    fun findByMenuId(menuId: String): List<UserMenu>
    
    @Select("SELECT * FROM user_menus WHERE user_id = #{userId}")
    fun findByUserId(userId: Long): List<UserMenu>
    
    /**
     * 检查用户是否有权限访问菜单
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM user_menus 
        WHERE user_id = #{userId} AND menu_id = #{menuId}
    """)
    fun hasAccess(userId: Long, menuId: String): Boolean
    
    /**
     * 检查用户是否是菜单的拥有者
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM user_menus 
        WHERE user_id = #{userId} AND menu_id = #{menuId} AND is_owner = 1
    """)
    fun isOwner(userId: Long, menuId: String): Boolean
    
    /**
     * 检查用户是否有编辑权限
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM user_menus 
        WHERE user_id = #{userId} AND menu_id = #{menuId} AND permission = 'write'
    """)
    fun hasWritePermission(userId: Long, menuId: String): Boolean
    
    @Delete("DELETE FROM user_menus WHERE menu_id = #{menuId}")
    fun deleteByMenuId(menuId: String): Int
    
    @Delete("DELETE FROM user_menus WHERE user_id = #{userId} AND menu_id = #{menuId}")
    fun deleteByUserAndMenu(userId: Long, menuId: String): Int
    
    @Delete("""
        <script>
        DELETE FROM user_menus WHERE menu_id IN
        <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">
            #{menuId}
        </foreach>
        </script>
    """)
    fun deleteByMenuIds(menuIds: List<String>): Int
}

