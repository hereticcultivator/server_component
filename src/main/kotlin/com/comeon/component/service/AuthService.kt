package com.comeon.component.service

import com.comeon.component.dto.*
import com.comeon.component.exception.CustomException
import com.comeon.component.model.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val loginLogService: LoginLogService,
    private val verificationCodeService: VerificationCodeService,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) {
    
    /**
     * 用户注册
     * 注册成功后自动生成token
     */
    fun register(request: UserRegistrationRequest, httpRequest: HttpServletRequest? = null): AuthResponse {
        val userResponse = userService.registerUser(request)
        
        // 获取用户信息（包含tokenVersion）
        val user = userService.getUserById(userResponse.id)
            ?: throw CustomException("用户注册失败，无法获取用户信息", 500)
        
        // 生成token对
        val accessToken = tokenService.generateAccessToken(
            userId = userResponse.id,
            username = userResponse.username,
            role = userResponse.role
        )
        val refreshToken = tokenService.generateRefreshToken(userResponse.id, user.tokenVersion)
        
        // 记录登录日志
        loginLogService.logSuccess(userResponse.id, userResponse.username, "register", httpRequest)
        
        return AuthResponse(
            user = userResponse,
            tokens = TokenInfo(
                accessToken = accessToken,
                refreshToken = refreshToken
            ),
            isNewUser = false
        )
    }
    
    /**
     * 统一登录接口（方案A）
     * 支持多种登录方式：phone_code, phone_password, username_password
     * 支持自动注册（仅手机号登录方式）
     */
    fun unifiedLogin(request: UnifiedLoginRequest, httpRequest: HttpServletRequest? = null): AuthResponse {
        return when (request.loginType) {
            "phone_code" -> loginByPhoneCode(
                phoneNumber = request.phoneNumber ?: throw CustomException("手机号不能为空", 400),
                verificationCode = request.verificationCode ?: throw CustomException("验证码不能为空", 400),
                autoRegister = request.autoRegister,
                fullName = request.fullName,
                httpRequest = httpRequest
            )
            "phone_password" -> loginByPhonePassword(
                phoneNumber = request.phoneNumber ?: throw CustomException("手机号不能为空", 400),
                password = request.password ?: throw CustomException("密码不能为空", 400),
                autoRegister = request.autoRegister,
                fullName = request.fullName,
                httpRequest = httpRequest
            )
            "username_password" -> loginByUsernamePassword(
                username = request.username ?: throw CustomException("用户名不能为空", 400),
                password = request.password ?: throw CustomException("密码不能为空", 400),
                httpRequest = httpRequest
            )
            else -> throw CustomException("不支持的登录类型: ${request.loginType}", 400)
        }
    }
    
    /**
     * 手机号验证码登录（支持自动注册）
     */
    private fun loginByPhoneCode(
        phoneNumber: String,
        verificationCode: String,
        autoRegister: Boolean,
        fullName: String?,
        httpRequest: HttpServletRequest?
    ): AuthResponse {
        // 1. 验证验证码
        if (!verificationCodeService.verifyCode(phoneNumber, verificationCode)) {
            loginLogService.logFailure(phoneNumber, "phone_code", "验证码错误或已过期", httpRequest)
            throw CustomException("验证码错误或已过期", 400)
        }
        
        // 2. 查找用户
        var user = userService.getUserByPhoneNumber(phoneNumber)
        var isNewUser = false
        
        // 3. 用户不存在，自动注册
        if (user == null) {
            if (!autoRegister) {
                loginLogService.logFailure(phoneNumber, "phone_code", "用户不存在，请先注册", httpRequest)
                throw CustomException("用户不存在，请先注册", 404)
            }
            
            // 自动创建用户
            user = userService.createUserFromPhone(
                phoneNumber = phoneNumber,
                username = null,  // 自动生成
                fullName = fullName,
                password = null  // 不设置密码，后续可设置
            )
            isNewUser = true
        }
        
        // 4. 检查用户ID
        val userId = user.id ?: run {
            loginLogService.logFailure(phoneNumber, "phone_code", "用户数据异常", httpRequest)
            throw CustomException("用户数据异常", 500)
        }
        
        // 5. 更新最后登录时间
        userService.updateLastLoginAt(userId)
        val updatedUser = userService.getUserById(userId)
            ?: run {
                loginLogService.logFailure(phoneNumber, "phone_code", "用户数据异常", httpRequest)
                throw CustomException("用户数据异常", 500)
            }
        
        val userResponse = updatedUser.toResponse()
        
        // 6. 生成token对
        val accessToken = tokenService.generateAccessToken(
            userId = userResponse.id,
            username = userResponse.username,
            role = userResponse.role
        )
        val refreshToken = tokenService.generateRefreshToken(userResponse.id, updatedUser.tokenVersion)
        
        // 7. 记录登录成功日志
        loginLogService.logSuccess(userResponse.id, userResponse.username, "phone_code", httpRequest)
        
        return AuthResponse(
            user = userResponse,
            tokens = TokenInfo(
                accessToken = accessToken,
                refreshToken = refreshToken
            ),
            isNewUser = isNewUser
        )
    }
    
    /**
     * 手机号密码登录（支持自动注册）
     */
    private fun loginByPhonePassword(
        phoneNumber: String,
        password: String,
        autoRegister: Boolean,
        fullName: String?,
        httpRequest: HttpServletRequest?
    ): AuthResponse {
        // 1. 查找用户
        var user = userService.getUserByPhoneNumber(phoneNumber)
        var isNewUser = false
        
        // 2. 用户不存在，自动注册
        if (user == null) {
            if (!autoRegister) {
                loginLogService.logFailure(phoneNumber, "phone_password", "用户不存在，请先注册", httpRequest)
                throw com.comeon.component.exception.CustomException("用户不存在，请先注册", 404)
            }
            
            // 自动创建用户（使用提供的密码）
            user = userService.createUserFromPhone(
                phoneNumber = phoneNumber,
                username = null,  // 自动生成
                fullName = fullName,
                password = password
            )
            isNewUser = true
        } else {
            // 3. 检查用户是否有密码（如果用户是通过验证码注册的，可能没有密码）
            if (user.password.isBlank()) {
                loginLogService.logFailure(phoneNumber, "phone_password", "该账号未设置密码，请使用验证码登录", httpRequest)
                throw CustomException("该账号未设置密码，请使用验证码登录", 400)
            }
            
            // 4. 验证密码
            var matches = false
            try {
                matches = passwordEncoder.matches(password, user.password)
            } catch (e: Exception) {
                // 密码格式可能不正确（如旧数据为明文），视为验证失败
                matches = false
            }
            
            if (!matches) {
                loginLogService.logFailure(phoneNumber, "phone_password", "手机号或密码错误", httpRequest)
                throw CustomException("手机号或密码错误", 401)
            }
        }
        
        // 5. 检查用户ID
        val userId = user.id ?: run {
            loginLogService.logFailure(phoneNumber, "phone_password", "用户数据异常", httpRequest)
            throw CustomException("用户数据异常", 500)
        }
        
        // 6. 更新最后登录时间
        userService.updateLastLoginAt(userId)
        val updatedUser = userService.getUserById(userId)
            ?: run {
                loginLogService.logFailure(phoneNumber, "phone_password", "用户数据异常", httpRequest)
                throw CustomException("用户数据异常", 500)
            }
        
        val userResponse = updatedUser.toResponse()
        
        // 7. 生成token对
        val accessToken = tokenService.generateAccessToken(
            userId = userResponse.id,
            username = userResponse.username,
            role = userResponse.role
        )
        val refreshToken = tokenService.generateRefreshToken(userResponse.id, updatedUser.tokenVersion)
        
        // 8. 记录登录成功日志
        loginLogService.logSuccess(userResponse.id, userResponse.username, "phone_password", httpRequest)
        
        return AuthResponse(
            user = userResponse,
            tokens = TokenInfo(
                accessToken = accessToken,
                refreshToken = refreshToken
            ),
            isNewUser = isNewUser
        )
    }
    
    /**
     * 用户名密码登录（不支持自动注册）
     */
    private fun loginByUsernamePassword(
        username: String,
        password: String,
        httpRequest: HttpServletRequest?
    ): AuthResponse {
        val user = userService.authenticateByUsername(username, password)
            ?: run {
                loginLogService.logFailure(username, "username_password", "用户名或密码错误", httpRequest)
                throw CustomException("用户名或密码错误", 401)
            }
        
        val userResponse = user.toResponse()
        
        // 生成token对
        val accessToken = tokenService.generateAccessToken(
            userId = userResponse.id,
            username = userResponse.username,
            role = userResponse.role
        )
        val refreshToken = tokenService.generateRefreshToken(userResponse.id, user.tokenVersion)
        
        // 记录登录成功日志
        loginLogService.logSuccess(userResponse.id, userResponse.username, "username_password", httpRequest)
        
        return AuthResponse(
            user = userResponse,
            tokens = TokenInfo(
                accessToken = accessToken,
                refreshToken = refreshToken
            ),
            isNewUser = false
        )
    }
    
    /**
     * 发送验证码（登录/注册通用）
     */
    fun sendCode(phoneNumber: String, purpose: String = "login") {
        // 生成验证码
        val code = verificationCodeService.generateCode(phoneNumber)
        
        // TODO: 生产环境应通过短信服务发送验证码，这里仅打印日志
        println("验证码（开发环境）: $phoneNumber -> $code (purpose: $purpose)")
    }
    
    /**
     * 刷新Token（实现轮换机制）
     * 使用RefreshToken获取新的AccessToken和RefreshToken
     * 刷新后旧RefreshToken失效，实现安全轮换
     */
    fun refreshToken(refreshToken: String): RefreshTokenResponse {
        // 验证RefreshToken
        val claims = tokenService.validateToken(refreshToken, "refresh")
        val userId = claims.subject.toLong()
        val tokenVersion = tokenService.getTokenVersionFromRefreshToken(refreshToken)
        
        // 查询用户信息
        val user = userService.getUserById(userId)
            ?: throw CustomException("用户不存在", 404)
        
        // 验证Token版本号（防止旧Token被重用）
        if (user.tokenVersion != tokenVersion) {
            throw CustomException("RefreshToken已失效，请重新登录", 401)
        }
        
        // 递增Token版本号（使旧RefreshToken失效）
        userService.incrementTokenVersion(userId)
        
        // 获取更新后的用户信息
        val updatedUser = userService.getUserById(userId)!!
        
        // 生成新的token对（使用新的tokenVersion）
        val newAccessToken = tokenService.generateAccessToken(
            userId = updatedUser.id!!,
            username = updatedUser.username,
            role = updatedUser.role
        )
        val newRefreshToken = tokenService.generateRefreshToken(updatedUser.id!!, updatedUser.tokenVersion)
        
        return RefreshTokenResponse(
            tokens = TokenInfo(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )
    }
    
    /**
     * 发送密码重置验证码
     */
    fun sendPasswordResetCode(phoneNumber: String) {
        // 检查手机号是否已注册
        val user = userService.getUserByPhoneNumber(phoneNumber)
            ?: throw CustomException("该手机号未注册", 404)
        
        // 生成验证码（开发环境返回，生产环境应通过短信发送）
        val code = verificationCodeService.generateCode(phoneNumber)
        
        // TODO: 生产环境应通过短信服务发送验证码，这里仅打印日志
        println("密码重置验证码（开发环境）: $phoneNumber -> $code")
    }
    
    /**
     * 重置密码
     */
    fun resetPassword(request: com.comeon.component.dto.PasswordResetRequest) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(request.phoneNumber, request.verificationCode)) {
            throw CustomException("验证码错误或已过期", 400)
        }
        
        // 查找用户
        val user = userService.getUserByPhoneNumber(request.phoneNumber)
            ?: throw CustomException("该手机号未注册", 404)
        
        // 验证新密码强度
        com.comeon.component.util.PasswordValidator.validate(request.newPassword)
        
        // 加密新密码
        val encodedPassword = passwordEncoder.encode(request.newPassword)
        
        // 更新密码
        userService.updatePassword(user.id!!, encodedPassword)
        
        // 递增Token版本号（使所有现有Token失效，强制重新登录）
        userService.incrementTokenVersion(user.id!!)
    }
    
    private fun User.toResponse(): UserResponse {
        return UserResponse(
            id = this.id!!,
            username = this.username,
            phoneNumber = this.phoneNumber,
            fullName = this.fullName,
            role = this.role,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt
        )
    }
}

