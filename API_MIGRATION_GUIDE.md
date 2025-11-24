# 客户端API迁移指南

## 概述

本次重构引入了**统一登录接口**（方案A），支持多种登录方式和自动注册功能。**旧接口已完全移除**，客户端必须迁移到新接口。

## ⚠️ 重要变更

### 旧接口已移除

以下旧接口**已完全删除**，不再可用：
- ❌ `POST /api/auth/login/username` - **已删除**
- ❌ `POST /api/auth/login/phone` - **已删除**

**必须迁移到新接口**：
- ✅ `POST /api/auth/login` (loginType="username_password") - 替代旧用户名登录
- ✅ `POST /api/auth/login` (loginType="phone_password") - 替代旧手机号登录

## 主要变更

### 1. 统一登录接口

- **新接口**: `POST /api/auth/login`
- **功能**: 支持多种登录方式，统一接口设计
- **优势**: 
  - 支持自动注册（手机号登录）
  - 统一的请求/响应格式
  - 更好的扩展性

### 2. 发送验证码接口

- **新接口**: `POST /api/auth/send-code`
- **功能**: 发送手机验证码（登录/注册通用）

### 3. 响应增强

- **新增字段**: `isNewUser` - 标识是否为新注册用户（自动注册场景）

### 4. 移除的接口和DTO

以下内容已完全删除：
- `POST /api/auth/login/username` 接口
- `POST /api/auth/login/phone` 接口
- `UsernameLoginRequest` DTO类
- `PhoneLoginRequest` DTO类

## 新API详细说明

### 1. 统一登录接口

#### 接口信息

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Content-Type**: `application/json`

#### 支持的登录类型

| loginType | 说明 | 必填字段 | 自动注册支持 |
|-----------|------|---------|------------|
| `phone_code` | 手机号验证码登录 | phoneNumber, verificationCode | ✅ 支持 |
| `phone_password` | 手机号密码登录 | phoneNumber, password | ✅ 支持 |
| `username_password` | 用户名密码登录 | username, password | ❌ 不支持 |

#### 请求参数

```json
{
  "loginType": "phone_code",  // 必填: "phone_code" | "phone_password" | "username_password"
  "phoneNumber": "13800138000",  // phone_code 和 phone_password 必填
  "verificationCode": "123456",  // phone_code 必填，6位数字
  "username": "testuser",  // username_password 必填
  "password": "password123",  // phone_password 和 username_password 必填
  "autoRegister": true,  // 可选，默认true，是否允许自动注册（仅手机号登录支持）
  "fullName": "张三"  // 可选，自动注册时的用户信息
}
```

#### 请求示例

**1. 手机号验证码登录（支持自动注册）**

```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "phone_code",
    "phoneNumber": "13800138000",
    "verificationCode": "123456",
    "autoRegister": true,
    "fullName": "张三"
  }'
```

**2. 手机号密码登录（支持自动注册）**

```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "phone_password",
    "phoneNumber": "13800138000",
    "password": "password123",
    "autoRegister": true,
    "fullName": "张三"
  }'
```

**3. 用户名密码登录（不支持自动注册）**

```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "username_password",
    "username": "testuser",
    "password": "password123"
  }'
```

#### 响应格式

**成功响应** (200 OK):

```json
{
  "code": "200",
  "data": {
    "user": {
      "id": 1,
      "username": "user_3800",
      "phoneNumber": "13800138000",
      "fullName": "张三",
      "role": 0,
      "createdAt": 1704067200000,
      "lastLoginAt": 1704067300000
    },
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    },
    "isNewUser": true  // 新增字段：标识是否为新注册用户
  },
  "msg": "注册并登录成功"  // 或 "登录成功"
}
```

**错误响应示例**:

```json
{
  "code": "400",
  "data": null,
  "msg": "验证码错误或已过期"
}
```

### 2. 发送验证码接口

#### 接口信息

- **URL**: `/api/auth/send-code`
- **Method**: `POST`
- **Content-Type**: `application/json`

#### 请求参数

```json
{
  "phoneNumber": "13800138000",  // 必填，11位手机号
  "purpose": "login"  // 可选，默认"login"，可选值: "login" | "register" | "reset_password"
}
```

#### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "13800138000",
    "purpose": "login"
  }'
```

#### 响应格式

**成功响应** (200 OK):

```json
{
  "code": "200",
  "data": null,
  "msg": "验证码已发送"
}
```

**注意**: 开发环境验证码会在控制台打印，生产环境应通过短信服务发送。

## 迁移指南

### 场景1: 手机号密码登录迁移

**旧接口**:
```bash
POST /api/auth/login/phone
{
  "phoneNumber": "13800138000",
  "password": "password123"
}
```

**新接口**:
```bash
POST /api/auth/login
{
  "loginType": "phone_password",
  "phoneNumber": "13800138000",
  "password": "password123",
  "autoRegister": true  // 新增：支持自动注册
}
```

**客户端代码迁移示例** (Kotlin):

```kotlin
// 旧代码
fun loginByPhone(phoneNumber: String, password: String) {
    val request = PhoneLoginRequest(phoneNumber, password)
    // 调用旧接口
    apiService.loginByPhone(request)
}

// 新代码
fun loginByPhone(phoneNumber: String, password: String, autoRegister: Boolean = true) {
    val request = UnifiedLoginRequest(
        loginType = "phone_password",
        phoneNumber = phoneNumber,
        password = password,
        autoRegister = autoRegister
    )
    // 调用新接口
    apiService.unifiedLogin(request)
}
```

### 场景2: 用户名密码登录迁移

**旧接口**:
```bash
POST /api/auth/login/username
{
  "username": "testuser",
  "password": "password123"
}
```

**新接口**:
```bash
POST /api/auth/login
{
  "loginType": "username_password",
  "username": "testuser",
  "password": "password123"
}
```

**客户端代码迁移示例** (Kotlin):

```kotlin
// 旧代码
fun loginByUsername(username: String, password: String) {
    val request = UsernameLoginRequest(username, password)
    apiService.loginByUsername(request)
}

// 新代码
fun loginByUsername(username: String, password: String) {
    val request = UnifiedLoginRequest(
        loginType = "username_password",
        username = username,
        password = password
    )
    apiService.unifiedLogin(request)
}
```

### 场景3: 新增手机号验证码登录

**新功能**: 手机号验证码登录（支持自动注册）

**使用流程**:
1. 调用发送验证码接口获取验证码
2. 使用验证码登录（如果用户不存在且autoRegister=true，自动注册）

**客户端代码示例** (Kotlin):

```kotlin
// 1. 发送验证码
fun sendVerificationCode(phoneNumber: String) {
    val request = SendCodeRequest(phoneNumber, "login")
    apiService.sendCode(request)
}

// 2. 验证码登录（支持自动注册）
fun loginByPhoneCode(
    phoneNumber: String, 
    verificationCode: String,
    autoRegister: Boolean = true,
    fullName: String? = null
) {
    val request = UnifiedLoginRequest(
        loginType = "phone_code",
        phoneNumber = phoneNumber,
        verificationCode = verificationCode,
        autoRegister = autoRegister,
        fullName = fullName
    )
    val response = apiService.unifiedLogin(request)
    
    // 检查是否为新用户
    if (response.data?.isNewUser == true) {
        // 新用户引导流程
        showWelcomeScreen()
    } else {
        // 老用户直接进入
        navigateToHome()
    }
}
```

### 场景4: 处理自动注册响应

**新字段**: `isNewUser` - 标识是否为新注册用户

**客户端处理示例**:

```kotlin
fun handleLoginResponse(response: Result<AuthResponse>) {
    val authResponse = response.data
    if (authResponse?.isNewUser == true) {
        // 新用户：显示欢迎页面、引导设置密码等
        showWelcomeScreen(authResponse.user)
        promptSetPassword()  // 提示设置密码
    } else {
        // 老用户：直接进入主界面
        navigateToHome()
    }
    
    // 保存token
    saveTokens(authResponse.tokens)
}
```

## 自动注册说明

### 支持的登录方式

- ✅ **手机号验证码登录** (`phone_code`): 支持自动注册
- ✅ **手机号密码登录** (`phone_password`): 支持自动注册
- ❌ **用户名密码登录** (`username_password`): 不支持自动注册

### 自动注册行为

1. **用户不存在时**:
   - 如果 `autoRegister=true`: 自动创建账号并登录
   - 如果 `autoRegister=false`: 返回错误 "用户不存在，请先注册"

2. **自动生成的用户名**:
   - 格式: `user_手机号后4位` (如: `user_3800`)
   - 如果冲突，自动添加序号 (如: `user_3800_1`)

3. **密码处理**:
   - **验证码登录**: 不设置密码（后续可设置）
   - **密码登录**: 使用提供的密码

4. **响应标识**:
   - `isNewUser=true`: 表示本次登录触发了自动注册
   - `isNewUser=false`: 表示使用已有账号登录

## 兼容性说明

### ⚠️ 不向后兼容

- ❌ **旧接口已完全移除**，不再可用
- ❌ 使用旧接口的客户端将收到 404 错误
- ✅ Token格式和刷新机制保持不变
- ✅ 响应格式基本保持一致（新增 `isNewUser` 字段）

### 迁移要求

**必须立即迁移**，旧接口已不可用。如果客户端仍在使用旧接口，将无法正常工作。

## 错误码说明

| 错误码 | HTTP状态码 | 说明 | 解决方案 |
|--------|-----------|------|---------|
| 400 | 400 Bad Request | 参数错误（如验证码格式错误） | 检查请求参数 |
| 400 | 400 Bad Request | 验证码错误或已过期 | 重新获取验证码 |
| 400 | 400 Bad Request | 不支持的登录类型 | 检查 loginType 值 |
| 401 | 401 Unauthorized | 用户名或密码错误 | 检查账号密码 |
| 404 | 404 Not Found | 用户不存在且不允许自动注册 | 设置 autoRegister=true 或先注册 |

## 最佳实践

### 1. 手机号验证码登录流程

```kotlin
// 完整的验证码登录流程
suspend fun phoneCodeLoginFlow(phoneNumber: String) {
    try {
        // 1. 发送验证码
        sendCode(phoneNumber)
        showToast("验证码已发送")
        
        // 2. 用户输入验证码
        val code = awaitUserInput()
        
        // 3. 验证码登录（自动注册）
        val response = loginByPhoneCode(
            phoneNumber = phoneNumber,
            verificationCode = code,
            autoRegister = true
        )
        
        // 4. 处理响应
        if (response.data?.isNewUser == true) {
            // 新用户：引导设置密码
            showSetPasswordDialog()
        }
        
        // 5. 保存token并跳转
        saveTokens(response.data.tokens)
        navigateToHome()
        
    } catch (e: Exception) {
        handleError(e)
    }
}
```

### 2. 错误处理

```kotlin
fun handleLoginError(error: ApiError) {
    when (error.code) {
        "400" -> {
            when {
                error.msg.contains("验证码") -> showError("验证码错误，请重新获取")
                error.msg.contains("格式") -> showError("手机号格式不正确")
                else -> showError(error.msg)
            }
        }
        "401" -> showError("账号或密码错误")
        "404" -> {
            if (error.msg.contains("用户不存在")) {
                showDialog("用户不存在，是否自动注册？") {
                    // 重新登录，设置 autoRegister=true
                    retryLogin(autoRegister = true)
                }
            } else {
                showError(error.msg)
            }
        }
        else -> showError("登录失败，请稍后重试")
    }
}
```

### 3. Token管理

```kotlin
// Token存储和管理保持不变
fun saveTokens(tokens: TokenInfo) {
    // 保存 accessToken 和 refreshToken
    preferences.save("access_token", tokens.accessToken)
    preferences.save("refresh_token", tokens.refreshToken)
}

// Token刷新逻辑保持不变
fun refreshToken() {
    val refreshToken = preferences.get("refresh_token")
    val request = RefreshTokenRequest(refreshToken)
    val response = apiService.refreshToken(request)
    saveTokens(response.data.tokens)
}
```

## 测试建议

### 1. 功能测试

- [ ] 手机号验证码登录（新用户自动注册）
- [ ] 手机号验证码登录（老用户）
- [ ] 手机号密码登录（新用户自动注册）
- [ ] 手机号密码登录（老用户）
- [ ] 用户名密码登录（不支持自动注册）
- [ ] 验证码错误处理
- [ ] 自动注册关闭时的错误处理

### 2. 兼容性测试

- [ ] 旧接口仍然可用
- [ ] 旧接口响应格式正确
- [ ] 新旧接口token兼容

### 3. 边界测试

- [ ] 手机号格式验证
- [ ] 验证码过期处理
- [ ] 用户名冲突处理（自动注册）
- [ ] 并发登录测试

## 常见问题

### Q1: 自动注册的用户名是什么格式？

A: 格式为 `user_手机号后4位`，如果冲突会自动添加序号。例如：
- `user_3800`
- `user_3800_1` (如果冲突)

### Q2: 自动注册的用户可以设置密码吗？

A: 可以。自动注册后，用户可以通过"设置密码"功能设置密码。如果使用密码登录方式自动注册，则直接使用提供的密码。

### Q3: 旧接口什么时候会被移除？

A: 旧接口已经移除。本次重构直接删除了旧接口，不再提供向后兼容。客户端必须使用新的统一登录接口。

### Q4: 验证码有效期是多久？

A: 验证码有效期为5分钟，一次性使用（验证成功后自动删除）。

### Q5: 自动注册功能可以关闭吗？

A: 可以。在请求中设置 `autoRegister=false`，如果用户不存在会返回404错误。

## 联系支持

如有问题，请联系开发团队或提交Issue。

---

**文档版本**: 2.0  
**最后更新**: 2024-01-XX  
**适用版本**: v2.0.0+  
**重要**: 旧接口已完全移除，必须使用新接口

