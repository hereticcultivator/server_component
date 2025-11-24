package com.comeon.component.security.aspect

import com.comeon.component.exception.CustomException
import com.comeon.component.model.UserRole
import com.comeon.component.security.SecurityContextHelper
import com.comeon.component.security.annotation.RequireOwnership
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * 数据权限校验切面
 * 自动校验用户是否有权限操作指定的数据
 * 
 * 校验规则：
 * 1. 用户只能操作自己的数据（userId 必须匹配）
 * 2. 管理员可以操作所有数据（如果 allowAdmin = true）
 */
@Aspect
@Component
@Order(1) // 确保在事务之前执行
class DataPermissionAspect {
    
    @Before("@annotation(requireOwnership)")
    fun checkOwnership(joinPoint: JoinPoint, requireOwnership: RequireOwnership) {
        val currentUserId = SecurityContextHelper.getCurrentUserId()
        val currentUserRole = SecurityContextHelper.getCurrentUserRole()
        
        // 如果是管理员且允许管理员操作，则跳过校验
        if (requireOwnership.allowAdmin && currentUserRole == UserRole.ADMIN) {
            return
        }
        
        // 获取目标用户ID
        val targetUserId = extractUserIdFromArgs(joinPoint, requireOwnership.userIdParam)
            ?: throw CustomException("无法获取目标用户ID，请确保参数中包含 ${requireOwnership.userIdParam}", 400)
        
        // 校验权限：只能操作自己的数据
        if (currentUserId != targetUserId) {
            throw CustomException("无权操作其他用户的数据", 403)
        }
    }
    
    /**
     * 从方法参数中提取用户ID
     * 支持从 @PathVariable、@RequestParam 或方法参数名中提取
     */
    private fun extractUserIdFromArgs(joinPoint: JoinPoint, userIdParam: String): Long? {
        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args
        val parameters = method.parameters
        
        for (i in parameters.indices) {
            val parameter = parameters[i]
            val arg = args[i]
            
            // 检查参数名是否匹配
            if (parameter.name == userIdParam && arg is Long) {
                return arg
            }
            
            // 检查 @PathVariable 注解
            val pathVariable = parameter.getAnnotation(PathVariable::class.java)
            if (pathVariable != null) {
                val paramName = pathVariable.value.ifEmpty { parameter.name }
                if (paramName == userIdParam && arg is Long) {
                    return arg
                }
            }
            
            // 检查 @RequestParam 注解
            val requestParam = parameter.getAnnotation(RequestParam::class.java)
            if (requestParam != null) {
                val paramName = requestParam.value.ifEmpty { parameter.name }
                if (paramName == userIdParam && arg is Long) {
                    return arg
                }
            }
            
            // 如果是对象，尝试从对象属性中获取
            if (arg != null && arg !is Long && arg !is String && arg !is Number) {
                try {
                    val field = arg.javaClass.getDeclaredField(userIdParam)
                    field.isAccessible = true
                    val value = field.get(arg)
                    if (value is Long) {
                        return value
                    } else if (value is Number) {
                        return value.toLong()
                    }
                } catch (e: Exception) {
                    // 忽略，继续查找
                }
            }
        }
        
        return null
    }
}

