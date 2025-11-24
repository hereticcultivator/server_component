package com.comeon.component.util

import com.comeon.component.exception.CustomException

/**
 * 密码验证工具类
 * 验证密码强度：至少8位，包含数字和字母
 */
object PasswordValidator {
    
    /**
     * 验证密码强度
     * 要求：至少8位，包含数字和字母（大小写均可）
     * 
     * @param password 待验证的密码
     * @throws CustomException 如果密码不符合要求
     */
    fun validate(password: String) {
        if (password.length < 8) {
            throw CustomException("密码长度至少为8位", 400)
        }
        
        val hasDigit = password.any { it.isDigit() }
        val hasLetter = password.any { it.isLetter() }
        
        if (!hasDigit || !hasLetter) {
            throw CustomException("密码必须包含数字和字母", 400)
        }
    }
    
    /**
     * 检查密码是否符合要求（不抛异常，返回布尔值）
     */
    fun isValid(password: String): Boolean {
        if (password.length < 8) {
            return false
        }
        
        val hasDigit = password.any { it.isDigit() }
        val hasLetter = password.any { it.isLetter() }
        
        return hasDigit && hasLetter
    }
}

