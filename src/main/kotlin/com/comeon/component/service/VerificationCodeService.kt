package com.comeon.component.service

import com.comeon.component.exception.CustomException
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * 验证码服务
 * 用于密码重置等场景
 * 注意：这是简化实现，使用内存存储。生产环境应使用Redis或短信服务
 */
@Service
class VerificationCodeService {
    
    // 存储验证码：key=手机号，value=验证码信息
    private val codes = ConcurrentHashMap<String, CodeInfo>()
    
    // 验证码有效期：5分钟
    private val CODE_EXPIRATION = 5 * 60 * 1000L
    
    /**
     * 生成并存储验证码
     * @param phoneNumber 手机号
     * @return 验证码（开发环境返回，生产环境应通过短信发送）
     */
    fun generateCode(phoneNumber: String): String {
        // 生成6位随机数字验证码
        val code = (100000..999999).random().toString()
        
        codes[phoneNumber] = CodeInfo(
            code = code,
            createdAt = System.currentTimeMillis()
        )
        
        // 返回验证码（开发环境，生产环境应通过短信发送）
        return code
    }
    
    /**
     * 验证验证码
     * @param phoneNumber 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    fun verifyCode(phoneNumber: String, code: String): Boolean {
        val codeInfo = codes[phoneNumber] ?: return false
        
        // 检查是否过期
        if (System.currentTimeMillis() - codeInfo.createdAt > CODE_EXPIRATION) {
            codes.remove(phoneNumber)
            return false
        }
        
        // 验证码匹配
        if (codeInfo.code == code) {
            // 验证成功后删除验证码（一次性使用）
            codes.remove(phoneNumber)
            return true
        }
        
        return false
    }
    
    /**
     * 验证码信息
     */
    private data class CodeInfo(
        val code: String,
        val createdAt: Long
    )
}

