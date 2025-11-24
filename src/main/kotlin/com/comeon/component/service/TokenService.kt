package com.comeon.component.service

import com.comeon.component.config.JwtConfig
import com.comeon.component.exception.CustomException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

/**
 * Token 服务
 * 负责 JWT token 的生成和验证
 * 实现行业标准的双Token机制（AccessToken + RefreshToken）
 */
@Service
class TokenService(
    private val jwtConfig: JwtConfig
) {
    
    /**
     * 生成密钥（使用配置的secret）
     */
    private val secretKey: SecretKey by lazy {
        val secretBytes = jwtConfig.secret.toByteArray(Charsets.UTF_8)
        // 确保密钥长度至少32字节（HMAC-SHA256要求）
        val keyBytes = if (secretBytes.size < 32) {
            secretBytes + ByteArray(32 - secretBytes.size) { 0 }
        } else {
            secretBytes
        }
        Keys.hmacShaKeyFor(keyBytes)
    }
    
    /**
     * 生成 AccessToken（访问令牌）
     * 用于日常API请求，有效期较短
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return JWT token 字符串
     */
    fun generateAccessToken(userId: Long, username: String, role: Int): String {
        val now = Date()
        val expiration = Date(now.time + jwtConfig.accessTokenExpiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .claim("type", "access")  // 标记token类型
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }
    
    /**
     * 生成 RefreshToken（刷新令牌）
     * 用于获取新的AccessToken，有效期较长
     * 包含tokenVersion用于轮换机制
     * 
     * @param userId 用户ID
     * @param tokenVersion Token版本号
     * @return JWT token 字符串
     */
    fun generateRefreshToken(userId: Long, tokenVersion: Int): String {
        val now = Date()
        val expiration = Date(now.time + jwtConfig.refreshTokenExpiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")  // 标记token类型
            .claim("version", tokenVersion)  // Token版本号，用于轮换
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }
    
    /**
     * 验证并解析 Token
     * 
     * @param token JWT token 字符串
     * @return Claims 对象，包含用户信息
     * @throws CustomException 如果token无效或过期
     */
    fun validateToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: io.jsonwebtoken.ExpiredJwtException) {
            throw CustomException("Token已过期", 401)
        } catch (e: io.jsonwebtoken.security.SignatureException) {
            throw CustomException("Token签名无效", 401)
        } catch (e: Exception) {
            throw CustomException("Token无效", 401)
        }
    }
    
    /**
     * 验证Token并检查类型
     * 
     * @param token JWT token 字符串
     * @param expectedType 期望的token类型（"access" 或 "refresh"）
     * @return Claims 对象
     */
    fun validateToken(token: String, expectedType: String): Claims {
        val claims = validateToken(token)
        
        // 验证token类型
        val tokenType = claims["type"] as? String
        if (tokenType != expectedType) {
            throw CustomException("Token类型错误，期望: $expectedType，实际: $tokenType", 401)
        }
        
        return claims
    }
    
    /**
     * 从RefreshToken中获取版本号
     */
    fun getTokenVersionFromRefreshToken(token: String): Int {
        val claims = validateToken(token, "refresh")
        return (claims["version"] as? Number)?.toInt() ?: 0
    }
    
    /**
     * 从 Token 中获取用户ID
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = validateToken(token)
        return claims.subject.toLong()
    }
    
    /**
     * 从 Token 中获取用户名
     */
    fun getUsernameFromToken(token: String): String {
        val claims = validateToken(token)
        return claims["username"] as? String
            ?: throw CustomException("Token中缺少用户名信息", 401)
    }
    
    /**
     * 从 Token 中获取角色
     */
    fun getRoleFromToken(token: String): Int {
        val claims = validateToken(token)
        return (claims["role"] as? Number)?.toInt()
            ?: throw CustomException("Token中缺少角色信息", 401)
    }
    
    /**
     * 检查 Token 是否过期
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = validateToken(token)
            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
}

