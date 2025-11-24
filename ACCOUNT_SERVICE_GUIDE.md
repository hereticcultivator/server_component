# 账号服务接入和使用指南

## 概述

账号服务是系统的底层服务，负责用户认证、授权和用户信息管理。本指南说明如何在业务服务中使用账号服务。

### 架构设计

账号服务采用接口抽象的方式，将账号逻辑与业务逻辑解耦：

```
业务服务层
    ↓
UserInfoService (统一用户信息查询服务)
    ↓
AccountService (账号服务接口)
    ↓
AccountServiceImpl (账号服务实现)
    ↓
UserService / TokenService (底层服务)
```

### 核心组件

1. **AccountService** - 账号服务接口，封装所有账号相关操作
2. **AccountServiceImpl** - 账号服务实现类
3. **UserInfoService** - 用户信息查询服务，提供便捷的用户信息获取方法
4. **UserInfo** - 统一的用户信息DTO（不包含敏感信息）

---

## 快速开始

### 1. 依赖注入

在业务服务中注入 `UserInfoService`：

```kotlin
@Service
class YourBusinessService(
    private val userInfoService: UserInfoService
) {
    // 业务逻辑...
}
```

### 2. 获取当前用户信息

如果当前请求已通过JWT验证（通过 `JwtAuthenticationFilter`），可以直接获取当前用户：

```kotlin
@Service
class YourBusinessService(
    private val userInfoService: UserInfoService
) {
    fun doSomething() {
        // 获取当前用户信息
        val currentUser = userInfoService.getCurrentUserInfo()
        
        println("当前用户: ${currentUser.username}")
        println("用户ID: ${currentUser.id}")
        println("是否管理员: ${currentUser.isAdmin}")
    }
}
```

### 3. 从Token获取用户信息

适用于跨服务调用、API网关转发等场景：

```kotlin
@Service
class YourBusinessService(
    private val userInfoService: UserInfoService
) {
    fun processRequest(token: String) {
        // 从Token获取用户信息
        val userInfo = userInfoService.getUserInfoFromToken(token)
        
        // 使用用户信息进行业务处理
        processUserData(userInfo.id)
    }
}
```

---

## 详细使用说明

### UserInfoService API

#### 1. 获取当前用户信息

```kotlin
// 获取完整的用户信息
val userInfo: UserInfo = userInfoService.getCurrentUserInfo()

// 只获取用户ID
val userId: Long = userInfoService.getCurrentUserId()

// 只获取用户名
val username: String = userInfoService.getCurrentUsername()

// 检查是否为管理员
val isAdmin: Boolean = userInfoService.isCurrentUserAdmin()

// 检查是否已认证
val isAuthenticated: Boolean = userInfoService.isAuthenticated()
```

#### 2. 从Token获取用户信息

```kotlin
// 从Token获取用户信息（会验证Token有效性）
val userInfo: UserInfo = userInfoService.getUserInfoFromToken(token)
```

#### 3. 根据ID获取用户信息

```kotlin
// 获取单个用户信息
val userInfo: UserInfo = userInfoService.getUserInfoById(userId)

// 批量获取用户信息
val users: List<UserInfo> = userInfoService.getUsersByIds(listOf(1L, 2L, 3L))
```

### AccountService API

如果需要更底层的账号服务功能，可以直接注入 `AccountService`：

```kotlin
@Service
class YourBusinessService(
    private val accountService: AccountService
) {
    fun checkUser() {
        // 验证Token是否有效
        val isValid = accountService.validateToken(token)
        
        // 检查用户是否存在
        val exists = accountService.userExists(userId)
        
        // 检查是否为管理员
        val isAdmin = accountService.isAdmin(userId)
        
        // 根据用户名获取用户信息
        val userInfo = accountService.getUserInfoByUsername("testuser")
        
        // 根据手机号获取用户信息
        val userInfo = accountService.getUserInfoByPhoneNumber("13800138000")
    }
}
```

---

## 使用场景示例

### 场景1：业务接口中获取当前用户

```kotlin
@RestController
@RequestMapping("/api/business")
class BusinessController(
    private val userInfoService: UserInfoService,
    private val businessService: BusinessService
) {
    @GetMapping("/my-data")
    fun getMyData(): ResponseEntity<Result> {
        // 获取当前用户信息
        val currentUser = userInfoService.getCurrentUserInfo()
        
        // 只查询当前用户的数据
        val data = businessService.getUserData(currentUser.id)
        
        return ResponseEntity.ok(Result.success(data))
    }
}
```

### 场景2：权限校验

```kotlin
@Service
class BusinessService(
    private val userInfoService: UserInfoService
) {
    fun deleteData(dataId: Long) {
        // 检查是否为管理员
        if (!userInfoService.isCurrentUserAdmin()) {
            throw CustomException("只有管理员可以删除数据", "403")
        }
        
        // 执行删除操作
        deleteDataById(dataId)
    }
}
```

### 场景3：跨服务调用（通过Token）

```kotlin
@Service
class OrderService(
    private val userInfoService: UserInfoService
) {
    fun createOrder(token: String, orderData: OrderData) {
        // 从Token获取用户信息
        val userInfo = userInfoService.getUserInfoFromToken(token)
        
        // 创建订单
        val order = Order(
            userId = userInfo.id,
            username = userInfo.username,
            data = orderData
        )
        
        saveOrder(order)
    }
}
```

### 场景4：批量处理用户数据

```kotlin
@Service
class NotificationService(
    private val userInfoService: UserInfoService
) {
    fun sendBatchNotification(userIds: List<Long>, message: String) {
        // 批量获取用户信息
        val users = userInfoService.getUsersByIds(userIds)
        
        // 发送通知
        users.forEach { user ->
            sendNotification(user.id, user.username, message)
        }
    }
}
```

### 场景5：数据权限校验

```kotlin
@Service
class DataService(
    private val userInfoService: UserInfoService
) {
    fun getUserData(dataId: Long): UserData {
        val data = findDataById(dataId)
        
        // 获取当前用户
        val currentUser = userInfoService.getCurrentUserInfo()
        
        // 权限校验：只能查看自己的数据（除非是管理员）
        if (data.userId != currentUser.id && !currentUser.isAdmin) {
            throw CustomException("无权访问该数据", "403")
        }
        
        return data
    }
}
```

---

## UserInfo 数据结构

```kotlin
data class UserInfo(
    val id: Long,                    // 用户ID
    val username: String,             // 用户名
    val phoneNumber: String,          // 手机号
    val fullName: String?,            // 全名（可选）
    val role: Int,                    // 角色：0=USER, 1=ADMIN
    val createdAt: Long,              // 创建时间戳（毫秒）
    val lastLoginAt: Long?            // 最后登录时间戳（毫秒，可选）
) {
    val isAdmin: Boolean              // 是否为管理员
    val isUser: Boolean               // 是否为普通用户
}
```

**注意**：`UserInfo` 不包含密码等敏感信息，可以安全地在服务间传递。

---

## 错误处理

### 常见异常

1. **用户未认证**
   ```kotlin
   try {
       val userInfo = userInfoService.getCurrentUserInfo()
   } catch (e: CustomException) {
       if (e.code == "401") {
           // 处理未认证情况
       }
   }
   ```

2. **用户不存在**
   ```kotlin
   try {
       val userInfo = userInfoService.getUserInfoById(userId)
   } catch (e: CustomException) {
       if (e.code == "404") {
           // 处理用户不存在情况
       }
   }
   ```

3. **Token无效**
   ```kotlin
   try {
       val userInfo = userInfoService.getUserInfoFromToken(token)
   } catch (e: CustomException) {
       if (e.code == "401") {
           // 处理Token无效情况
       }
   }
   ```

---

## 最佳实践

### 1. 优先使用 UserInfoService

业务服务应优先使用 `UserInfoService`，它提供了更便捷的方法和更好的错误处理。

```kotlin
// ✅ 推荐
val userInfo = userInfoService.getCurrentUserInfo()

// ❌ 不推荐（除非有特殊需求）
val userInfo = accountService.getUserInfoById(userId)
```

### 2. 使用 getCurrentUserInfo() 获取当前用户

如果当前请求已通过JWT验证，使用 `getCurrentUserInfo()` 是最简单的方式。

```kotlin
// ✅ 推荐
val currentUser = userInfoService.getCurrentUserInfo()

// ❌ 不推荐
val userId = SecurityContextHelper.getCurrentUserId()
val userInfo = accountService.getUserInfoById(userId)
```

### 3. 批量查询时使用批量方法

需要查询多个用户时，使用批量方法可以提高性能。

```kotlin
// ✅ 推荐
val users = userInfoService.getUsersByIds(userIds)

// ❌ 不推荐
val users = userIds.map { userInfoService.getUserInfoById(it) }
```

### 4. 权限校验使用便捷方法

```kotlin
// ✅ 推荐
if (userInfoService.isCurrentUserAdmin()) {
    // 管理员操作
}

// ❌ 不推荐
val userInfo = userInfoService.getCurrentUserInfo()
if (userInfo.role == 1) {
    // 管理员操作
}
```

### 5. 跨服务调用使用Token方式

```kotlin
// ✅ 推荐（跨服务调用）
val userInfo = userInfoService.getUserInfoFromToken(token)

// ❌ 不推荐（跨服务调用时直接访问数据库）
val user = userService.getUserById(userId)
```

---

## 迁移指南

### 从旧代码迁移

#### 旧代码（使用 SecurityContextHelper）

```kotlin
// 旧代码
val userId = SecurityContextHelper.getCurrentUserId()
val username = SecurityContextHelper.getCurrentUsername()
val role = SecurityContextHelper.getCurrentUserRole()
```

#### 新代码（使用 UserInfoService）

```kotlin
// 新代码
val userInfo = userInfoService.getCurrentUserInfo()
val userId = userInfo.id
val username = userInfo.username
val role = userInfo.role
```

#### 旧代码（直接使用 UserService）

```kotlin
// 旧代码
val user = userService.getUserById(userId)
// 使用 user.password（不安全）
```

#### 新代码（使用 AccountService）

```kotlin
// 新代码
val userInfo = accountService.getUserInfoById(userId)
// userInfo 不包含敏感信息，更安全
```

---

## 常见问题

### Q1: 什么时候使用 UserInfoService，什么时候使用 AccountService？

**A:** 
- **UserInfoService**: 业务服务优先使用，提供更便捷的方法和更好的错误处理
- **AccountService**: 需要更底层功能时使用，如验证Token、检查用户存在性等

### Q2: getCurrentUserInfo() 和 getUserInfoFromToken() 有什么区别？

**A:**
- **getCurrentUserInfo()**: 从 SecurityContext 获取，适用于当前请求已通过JWT验证的场景
- **getUserInfoFromToken()**: 从Token解析获取，适用于跨服务调用、API网关转发等场景

### Q3: 如何判断用户是否已登录？

**A:**
```kotlin
if (userInfoService.isAuthenticated()) {
    // 用户已登录
    val userInfo = userInfoService.getCurrentUserInfo()
} else {
    // 用户未登录
}
```

### Q4: UserInfo 和 UserResponse 有什么区别？

**A:**
- **UserInfo**: 用于服务间传递，不包含敏感信息，结构更简洁
- **UserResponse**: 用于API响应，包含更多字段（如创建时间等）

### Q5: 如何获取用户的完整信息（包括密码）？

**A:** 业务服务不应该获取用户密码。如果需要修改密码等功能，应调用 `AuthService` 的相关方法。

---

## 后续规划

### 短期（已完成）
- ✅ 创建 AccountService 接口
- ✅ 创建 UserInfoService 统一查询服务
- ✅ 创建 UserInfo DTO

### 中期（计划中）
- 🔄 将账号服务独立为HTTP服务
- 🔄 提供RESTful API接口
- 🔄 支持服务注册与发现

### 长期（规划中）
- 📋 微服务架构改造
- 📋 服务网关统一认证
- 📋 分布式配置中心

---

## 技术支持

如有问题，请查看：
- [API文档](./API_DOCUMENTATION.md)
- [安全改进说明](./SECURITY_IMPROVEMENTS.md)
- [Token使用指南](./CLIENT_TOKEN_GUIDE.md)

