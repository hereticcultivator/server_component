package com.comeon.component.security.resolver

import com.comeon.component.security.SecurityContextHelper
import com.comeon.component.security.UserPrincipal
import com.comeon.component.security.annotation.CurrentUser
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 当前用户参数解析器
 * 自动将当前登录用户的信息注入到 Controller 方法参数中
 * 
 * 支持的参数类型：
 * - Long (用户ID)
 * - String (用户名)
 * - UserPrincipal (完整的用户信息)
 */
@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {
    
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java)
    }
    
    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val userPrincipal = SecurityContextHelper.getCurrentUserPrincipal()
        val parameterType = parameter.parameterType
        
        return when {
            parameterType == Long::class.java || parameterType == Long::class.javaObjectType -> {
                userPrincipal.userId
            }
            parameterType == String::class.java -> {
                userPrincipal.usernameValue
            }
            parameterType == UserPrincipal::class.java -> {
                userPrincipal
            }
            else -> {
                throw IllegalArgumentException(
                    "不支持的参数类型: ${parameterType.name}，@CurrentUser 仅支持 Long、String 或 UserPrincipal"
                )
            }
        }
    }
}

