package com.comeon.component.service

import com.comeon.component.mapper.LoginLogMapper
import com.comeon.component.model.LoginLog
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

/**
 * 登录日志服务
 * 记录用户登录行为，用于审计和安全分析
 */
@Service
class LoginLogService(
    private val loginLogMapper: LoginLogMapper
) {
    
    /**
     * 记录登录成功日志
     */
    fun logSuccess(
        userId: Long,
        username: String,
        loginType: String,
        request: HttpServletRequest? = null
    ) {
        val loginLog = LoginLog(
            userId = userId,
            username = username,
            loginType = loginType,
            ipAddress = request?.remoteAddr,
            userAgent = request?.getHeader("User-Agent"),
            loginStatus = "success",
            createdAt = System.currentTimeMillis()
        )
        loginLogMapper.insert(loginLog)
    }
    
    /**
     * 记录登录失败日志
     */
    fun logFailure(
        username: String,
        loginType: String,
        failureReason: String,
        request: HttpServletRequest? = null
    ) {
        val loginLog = LoginLog(
            userId = null,  // 失败时设为 null，而不是 0L
            username = username,
            loginType = loginType,
            ipAddress = request?.remoteAddr,
            userAgent = request?.getHeader("User-Agent"),
            loginStatus = "failed",
            failureReason = failureReason,
            createdAt = System.currentTimeMillis()
        )
        loginLogMapper.insert(loginLog)
    }
    
    /**
     * 获取用户的登录历史
     */
    fun getUserLoginHistory(userId: Long, limit: Int = 50): List<LoginLog> {
        return loginLogMapper.findByUserId(userId, limit)
    }
}

