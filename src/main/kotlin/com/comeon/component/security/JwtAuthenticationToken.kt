package com.comeon.component.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * JWT 认证 Token
 * 用于 Spring Security 的认证流程
 */
class JwtAuthenticationToken(
    private val token: String,
    authorities: Collection<GrantedAuthority>? = null
) : AbstractAuthenticationToken(authorities ?: emptyList()) {
    
    private var principal: Any? = null
    private var credentials: String? = token
    
    init {
        isAuthenticated = false
    }
    
    constructor(
        principal: Any,
        token: String,
        authorities: Collection<GrantedAuthority>
    ) : this(token, authorities) {
        this.principal = principal
        this.credentials = token
        isAuthenticated = true
    }
    
    override fun getPrincipal(): Any? {
        return principal
    }
    
    override fun getCredentials(): Any? {
        return credentials
    }
    
    fun getToken(): String {
        return token
    }
}

