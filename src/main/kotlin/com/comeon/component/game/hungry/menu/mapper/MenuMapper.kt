package com.comeon.component.game.hungry.menu.mapper

import com.comeon.component.game.hungry.menu.model.Menu
import org.apache.ibatis.annotations.*

@Mapper
interface MenuMapper {
    
    @Insert("""
        INSERT INTO menus (id, title, category, icon_url, extra_info, create_time, update_time)
        VALUES (#{id}, #{title}, #{category}, #{iconUrl}, #{extraInfo}, #{createTime}, #{updateTime})
    """)
    fun insert(menu: Menu): Int
    
    @Select("SELECT * FROM menus WHERE id = #{id}")
    fun findById(id: String): Menu?
    
    /**
     * 查询菜单列表（通过关联表过滤用户菜单）
     * @param userId 当前用户ID（非管理员必须传）
     * @param isAdmin 是否为管理员（管理员查询所有菜单）
     * @param category 分类筛选
     * @param keyword 关键词搜索
     * @param offset 偏移量
     * @param limit 每页数量
     */
    @Select("""
        <script>
        SELECT DISTINCT m.* FROM menus m
        <if test="!isAdmin">
            INNER JOIN user_menus um ON m.id = um.menu_id AND um.user_id = #{userId}
        </if>
        WHERE 1=1
        <if test="category != null and category != 0">
            AND m.category = #{category}
        </if>
        <if test="keyword != null and keyword != ''">
            AND m.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        ORDER BY m.create_time DESC
        LIMIT #{offset}, #{limit}
        </script>
    """)
    fun findList(
        userId: Long?,
        isAdmin: Boolean,
        category: Int?,
        keyword: String?,
        offset: Int,
        limit: Int
    ): List<Menu>
    
    @Select("""
        <script>
        SELECT COUNT(DISTINCT m.id) FROM menus m
        <if test="!isAdmin">
            INNER JOIN user_menus um ON m.id = um.menu_id AND um.user_id = #{userId}
        </if>
        WHERE 1=1
        <if test="category != null and category != 0">
            AND m.category = #{category}
        </if>
        <if test="keyword != null and keyword != ''">
            AND m.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        </script>
    """)
    fun count(
        userId: Long?,
        isAdmin: Boolean,
        category: Int?,
        keyword: String?
    ): Long
    
    @Update("""
        UPDATE menus 
        SET title = #{title}, 
            category = #{category},
            icon_url = #{iconUrl},
            extra_info = #{extraInfo},
            update_time = #{updateTime}
        WHERE id = #{id}
    """)
    fun update(menu: Menu): Int
    
    @Delete("DELETE FROM menus WHERE id = #{id}")
    fun deleteById(id: String): Int
    
    @Delete("""
        <script>
        DELETE FROM menus WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    fun deleteByIds(ids: List<String>): Int
}

