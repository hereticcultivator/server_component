# 账号服务快速开始

## 一分钟上手

### 1. 注入服务

```kotlin
@Service
class YourService(
    private val userInfoService: UserInfoService
) {
    // 你的业务逻辑
}
```

### 2. 获取当前用户

```kotlin
val userInfo = userInfoService.getCurrentUserInfo()
println("当前用户: ${userInfo.username}")
```

### 3. 权限校验

```kotlin
if (userInfoService.isCurrentUserAdmin()) {
    // 管理员操作
}
```

## 常用方法速查

| 方法 | 说明 | 使用场景 |
|------|------|----------|
| `getCurrentUserInfo()` | 获取当前用户信息 | 已通过JWT验证的请求 |
| `getUserInfoFromToken(token)` | 从Token获取用户信息 | 跨服务调用 |
| `getCurrentUserId()` | 获取当前用户ID | 快速获取ID |
| `isCurrentUserAdmin()` | 检查是否为管理员 | 权限校验 |
| `isAuthenticated()` | 检查是否已认证 | 登录状态检查 |
| `getUserInfoById(userId)` | 根据ID获取用户信息 | 查询指定用户 |
| `getUsersByIds(userIds)` | 批量获取用户信息 | 批量查询 |

## 完整文档

详细文档请查看：[ACCOUNT_SERVICE_GUIDE.md](./ACCOUNT_SERVICE_GUIDE.md)

