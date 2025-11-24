# 可选认证快速开始

## 一分钟上手

### 1. 在接口中使用可选认证

```kotlin
@GetMapping("/products")
fun getProducts(): Result<List<Product>> {
    val currentUser = userInfoService.getCurrentUserInfoOrNull()
    
    return if (currentUser != null) {
        // 已登录用户
        Result.success(getPersonalizedProducts(currentUser.id))
    } else {
        // 游客
        Result.success(getPublicProducts())
    }
}
```

### 2. 配置接口为可选认证

在 `SecurityConfig.kt` 中添加路径到 `permitAll()`：

```kotlin
.requestMatchers(
    "/api/products/**"  // 你的接口路径
).permitAll()
```

## 常用方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getCurrentUserInfoOrNull()` | 获取当前用户（游客返回null） | `UserInfo?` |
| `getCurrentUserIdOrNull()` | 获取当前用户ID（游客返回null） | `Long?` |
| `isGuest()` | 检查是否为游客 | `Boolean` |
| `requireAuthentication()` | 要求必须登录（游客抛异常） | `UserInfo` |

## 完整文档

详细文档请查看：[OPTIONAL_AUTH_GUIDE.md](./OPTIONAL_AUTH_GUIDE.md)

