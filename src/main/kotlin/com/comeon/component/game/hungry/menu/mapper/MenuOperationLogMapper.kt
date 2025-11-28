package com.comeon.component.game.hungry.menu.mapper

import com.comeon.component.game.hungry.menu.model.MenuOperationLog
import org.apache.ibatis.annotations.*

@Mapper
interface MenuOperationLogMapper {
    
    @Insert("""
        INSERT INTO menu_operation_logs 
        (menu_id, user_id, username, operation_type, operation_detail, ip_address, user_agent, created_at)
        VALUES (#{menuId}, #{userId}, #{username}, #{operationType}, #{operationDetail}, 
                #{ipAddress}, #{userAgent}, #{createdAt})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(log: MenuOperationLog): Int
}

