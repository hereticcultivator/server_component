package com.comeon.component.mapper

import com.comeon.component.model.User
import org.apache.ibatis.annotations.*

@Mapper
interface UserMapper {
    
    @Insert("""
        INSERT INTO users (username, password, phone_number, full_name, role, created_at, updated_at)
        VALUES (#{username}, #{password}, #{phoneNumber}, #{fullName}, #{role}, #{createdAt}, #{updatedAt})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(user: User): Int
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    fun findById(id: Long): User?
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    fun findByUsername(username: String): User?
    
    @Select("SELECT * FROM users WHERE phone_number = #{phoneNumber}")
    fun findByPhoneNumber(phoneNumber: String): User?
    
    @Select("SELECT * FROM users WHERE username = #{username} OR phone_number = #{username}")
    fun findByUsernameOrPhone(username: String): User?
    
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    fun existsByUsername(username: String): Boolean
    
    @Select("SELECT COUNT(*) > 0 FROM users WHERE phone_number = #{phoneNumber}")
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    
    @Update("""
        UPDATE users 
        SET full_name = #{fullName}, 
            phone_number = #{phoneNumber}, 
            updated_at = #{updatedAt}
        WHERE id = #{id}
    """)
    fun update(user: User): Int
    
    @Update("""
        UPDATE users 
        SET last_login_at = #{lastLoginAt}, 
            updated_at = #{updatedAt}
        WHERE id = #{id}
    """)
    fun updateLastLogin(user: User): Int
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    fun deleteById(id: Long): Int
}

