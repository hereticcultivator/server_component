package com.comeon.component.example

import com.comeon.component.dto.UserInfo
import com.comeon.component.exception.CustomException
import com.comeon.component.service.account.UserInfoService
import org.springframework.stereotype.Service

/**
 * 业务服务使用示例
 * 展示如何在业务服务中使用账号服务
 * 
 * 注意：这是示例代码，实际使用时请根据业务需求调整
 */
@Service
class BusinessServiceExample(
    private val userInfoService: UserInfoService
) {
    
    /**
     * 示例1：获取当前用户信息
     */
    fun example1_GetCurrentUser() {
        // 获取当前用户信息（适用于已通过JWT验证的请求）
        val currentUser = userInfoService.getCurrentUserInfo()
        
        println("当前用户ID: ${currentUser.id}")
        println("当前用户名: ${currentUser.username}")
        println("是否管理员: ${currentUser.isAdmin}")
    }
    
    /**
     * 示例2：从Token获取用户信息
     */
    fun example2_GetUserFromToken(token: String) {
        try {
            // 从Token获取用户信息（适用于跨服务调用）
            val userInfo = userInfoService.getUserInfoFromToken(token)
            
            println("用户ID: ${userInfo.id}")
            println("用户名: ${userInfo.username}")
        } catch (e: CustomException) {
            if (e.code == 401) {
                println("Token无效或已过期")
            }
        }
    }
    
    /**
     * 示例3：权限校验
     */
    fun example3_CheckPermission() {
        // 检查是否为管理员
        if (userInfoService.isCurrentUserAdmin()) {
            println("当前用户是管理员，可以执行管理员操作")
        } else {
            println("当前用户不是管理员，无权执行该操作")
            throw CustomException("权限不足", 403)
        }
    }
    
    /**
     * 示例4：数据权限校验
     */
    fun example4_CheckDataPermission(targetUserId: Long) {
        val currentUser = userInfoService.getCurrentUserInfo()
        
        // 权限校验：只能操作自己的数据（除非是管理员）
        if (currentUser.id != targetUserId && !currentUser.isAdmin) {
            throw CustomException("无权操作其他用户的数据", 403)
        }
        
        println("权限校验通过，可以操作用户 $targetUserId 的数据")
    }
    
    /**
     * 示例5：批量获取用户信息
     */
    fun example5_BatchGetUsers(userIds: List<Long>) {
        // 批量获取用户信息
        val users = userInfoService.getUsersByIds(userIds)
        
        println("获取到 ${users.size} 个用户信息")
        users.forEach { user ->
            println("用户: ${user.username} (ID: ${user.id})")
        }
    }
    
    /**
     * 示例6：检查用户是否已认证
     */
    fun example6_CheckAuthentication() {
        if (userInfoService.isAuthenticated()) {
            val userInfo = userInfoService.getCurrentUserInfo()
            println("用户已认证: ${userInfo.username}")
        } else {
            println("用户未认证")
            throw CustomException("请先登录", 401)
        }
    }
    
    /**
     * 示例7：根据ID获取用户信息
     */
    fun example7_GetUserById(userId: Long) {
        try {
            val userInfo = userInfoService.getUserInfoById(userId)
            println("用户信息: ${userInfo.username}")
        } catch (e: CustomException) {
            if (e.code == 404) {
                println("用户不存在")
            }
        }
    }
    
    /**
     * 示例8：业务逻辑中使用用户信息
     */
    fun example8_BusinessLogic() {
        // 获取当前用户
        val currentUser = userInfoService.getCurrentUserInfo()
        
        // 执行业务逻辑
        val userData = getUserDataByUserId(currentUser.id)
        
        // 权限校验
        if (!currentUser.isAdmin && userData.ownerId != currentUser.id) {
            throw CustomException("无权访问该数据", 403)
        }
        
        // 处理数据
        processUserData(userData)
    }
    
    // 辅助方法（示例）
    private fun getUserDataByUserId(userId: Long): UserData {
        // 模拟获取用户数据
        return UserData(userId = userId, ownerId = userId, content = "示例数据")
    }
    
    private fun processUserData(data: UserData) {
        // 模拟处理数据
        println("处理用户数据: ${data.content}")
    }
    
    // 示例数据类
    private data class UserData(
        val userId: Long,
        val ownerId: Long,
        val content: String
    )
}

