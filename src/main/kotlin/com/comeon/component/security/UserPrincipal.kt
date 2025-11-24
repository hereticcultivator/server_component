package com.comeon.component.security

import com.comeon.component.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * 自定义 UserDetails 实现
 * 将 User 模型转换为 Spring Security 的 UserDetails
 */
class UserPrincipal(
    private val user: User
) : UserDetails {
    
    val userId: Long
        get() = user.id ?: throw IllegalStateException("用户ID不能为空")
    
    val usernameValue: String
        get() = user.username
    
    val role: Int
        get() = user.role
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()
        
        // 根据角色添加权限
        when (user.role) {
            0 -> authorities.add(SimpleGrantedAuthority("ROLE_USER"))
            1 -> {
                authorities.add(SimpleGrantedAuthority("ROLE_USER"))
                authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
            }
        }
        
        return authorities
    }
    
    override fun getPassword(): String {
        return user.password
    }
    
    override fun getUsername(): String {
        return user.username
    }
    
    override fun isAccountNonExpired(): Boolean {
        return true
    }
    
    override fun isAccountNonLocked(): Boolean {
        return true
    }
    
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }
    
    override fun isEnabled(): Boolean {
        return true
    }
    
    companion object {
        /**
         * 从 User 对象创建 UserPrincipal
         */
        fun from(user: User): UserPrincipal {
            return UserPrincipal(user)
        }
    }
}

