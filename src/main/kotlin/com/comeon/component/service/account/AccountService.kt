package com.comeon.component.service.account

import com.comeon.component.dto.UserInfo

/**
 * 账号服务接口
 * 封装所有账号相关的操作，为业务服务提供统一的账号服务接口
 * 
 * 设计目标：
 * 1. 将账号逻辑与业务逻辑解耦
 * 2. 提供统一的账号服务接口，便于后续服务化拆分
 * 3. 隐藏账号服务的内部实现细节
 */
interface AccountService {
    
    /**
     * 根据Token获取用户信息
     * 
     * @param token JWT AccessToken
     * @return 用户信息，如果token无效则返回null
     */
    fun getUserInfoByToken(token: String): UserInfo?
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息，如果用户不存在则返回null
     */
    fun getUserInfoById(userId: Long): UserInfo?
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息，如果用户不存在则返回null
     */
    fun getUserInfoByUsername(username: String): UserInfo?
    
    /**
     * 根据手机号获取用户信息
     * 
     * @param phoneNumber 手机号
     * @return 用户信息，如果用户不存在则返回null
     */
    fun getUserInfoByPhoneNumber(phoneNumber: String): UserInfo?
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT AccessToken
     * @return true表示token有效，false表示无效或过期
     */
    fun validateToken(token: String): Boolean
    
    /**
     * 批量获取用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表，不存在的用户会被跳过
     */
    fun getUsersByIds(userIds: List<Long>): List<UserInfo>
    
    /**
     * 检查用户是否存在
     * 
     * @param userId 用户ID
     * @return true表示用户存在，false表示不存在
     */
    fun userExists(userId: Long): Boolean
    
    /**
     * 检查用户是否为管理员
     * 
     * @param userId 用户ID
     * @return true表示是管理员，false表示不是或用户不存在
     */
    fun isAdmin(userId: Long): Boolean
}

