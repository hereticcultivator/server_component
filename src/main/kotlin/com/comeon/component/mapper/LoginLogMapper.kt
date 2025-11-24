package com.comeon.component.mapper

import com.comeon.component.model.LoginLog
import org.apache.ibatis.annotations.*

@Mapper
interface LoginLogMapper {
    
    @Insert("""
        INSERT INTO login_logs (user_id, username, login_type, ip_address, user_agent, login_status, failure_reason, created_at)
        VALUES (#{userId}, #{username}, #{loginType}, #{ipAddress}, #{userAgent}, #{loginStatus}, #{failureReason}, #{createdAt})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(loginLog: LoginLog): Int
    
    @Select("SELECT * FROM login_logs WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    fun findByUserId(userId: Long, limit: Int = 50): List<LoginLog>
    
    @Select("SELECT * FROM login_logs WHERE username = #{username} ORDER BY created_at DESC LIMIT #{limit}")
    fun findByUsername(username: String, limit: Int = 50): List<LoginLog>
}

