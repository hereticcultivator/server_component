# 可选认证使用指南

## 概述

可选认证（Optional Authentication）允许部分接口在未认证的情况下也能访问，但功能可能受限。这是业界常用的游客模式实现方式。

### 设计目标

1. **提升用户体验**：允许游客浏览内容，降低使用门槛
2. **渐进式引导**：在关键操作点引导用户注册登录
3. **功能分层**：公开功能游客可用，私有功能需要登录
4. **数据迁移**：登录后自动合并游客数据到用户账户

---

## 快速开始

### 1. 在接口中使用可选认证

```kotlin
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val userInfoService: UserInfoService
) {
    @GetMapping
    fun getProducts(): Result<List<Product>> {
        // 获取当前用户信息（可选）
        val currentUser = userInfoService.getCurrentUserInfoOrNull()
        
        // 根据是否有用户信息返回不同内容
        return if (currentUser != null) {
            // 已登录用户：返回个性化内容
            Result.success(getPersonalizedProducts(currentUser.id))
        } else {
            // 游客：返回公开内容
            Result.success(getPublicProducts())
        }
    }
}
```

### 2. 配置接口为可选认证

在 `SecurityConfig.kt` 中将需要支持游客的接口路径添加到 `permitAll()`：

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
        .authorizeHttpRequests { auth ->
            auth
                // 完全公开的接口
                .requestMatchers(
                    "/api/auth/**",
                    "/api/test/**"
                ).permitAll()
                // 可选认证的接口（允许游客访问）
                .requestMatchers(
                    "/api/products/**",      // 商品浏览
                    "/api/articles/**",      // 文章浏览
                    "/api/example/**"        // 示例接口
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
        }
    return http.build()
}
```

---

## API 参考

### UserInfoService 可选认证方法

#### 1. getCurrentUserInfoOrNull()

获取当前用户信息，如果是游客则返回 `null`。

```kotlin
val currentUser = userInfoService.getCurrentUserInfoOrNull()

if (currentUser != null) {
    // 已登录用户
    println("用户ID: ${currentUser.id}")
} else {
    // 游客
    println("游客模式")
}
```

#### 2. getCurrentUserIdOrNull()

获取当前用户ID，如果是游客则返回 `null`。

```kotlin
val userId = userInfoService.getCurrentUserIdOrNull()

val cart = if (userId != null) {
    cartService.getUserCart(userId)
} else {
    cartService.getGuestCart(sessionId)
}
```

#### 3. isGuest()

检查是否为游客。

```kotlin
if (userInfoService.isGuest()) {
    // 游客逻辑
} else {
    // 已登录用户逻辑
}
```

#### 4. requireAuthentication()

要求必须认证，游客会抛出异常。

```kotlin
// 必须登录的操作
val userId = userInfoService.requireAuthenticationUserId()
processPayment(userId, orderId)
```

---

## 使用场景

### 场景1：浏览商品（支持游客）

```kotlin
@GetMapping("/products")
fun getProducts(): Result<ProductListResponse> {
    val currentUser = userInfoService.getCurrentUserInfoOrNull()
    
    val products = if (currentUser != null) {
        // 已登录：个性化推荐
        productService.getPersonalizedProducts(currentUser.id)
    } else {
        // 游客：热门商品
        productService.getPublicProducts()
    }
    
    return Result.success(ProductListResponse(
        products = products,
        isGuest = currentUser == null,
        message = if (currentUser == null) "登录后可查看个性化推荐" else null
    ))
}
```

### 场景2：添加购物车（游客可用但功能受限）

```kotlin
@PostMapping("/cart")
fun addToCart(
    @RequestBody request: AddToCartRequest,
    @RequestHeader("X-Session-Id", required = false) sessionId: String?
): Result<CartItem> {
    val userId = userInfoService.getCurrentUserIdOrNull()
    
    val cartItem = if (userId != null) {
        // 已登录：存储到用户购物车
        cartService.addToUserCart(userId, request)
    } else {
        // 游客：存储到临时会话
        val guestSessionId = sessionId ?: generateSessionId()
        cartService.addToGuestCart(guestSessionId, request)
    }
    
    return Result.success(cartItem)
}
```

### 场景3：创建订单（游客可创建但需登录支付）

```kotlin
@PostMapping("/orders")
fun createOrder(@RequestBody request: CreateOrderRequest): Result<OrderResponse> {
    val userId = userInfoService.getCurrentUserIdOrNull()
    
    val order = orderService.createOrder(userId, request)
    
    return Result.success(OrderResponse(
        order = order,
        requireLogin = userId == null,
        message = if (userId == null) "订单已创建，请登录完成支付" else null
    ))
}
```

### 场景4：支付订单（必须登录）

```kotlin
@PostMapping("/orders/{orderId}/pay")
fun payOrder(@PathVariable orderId: Long): Result<PaymentResponse> {
    // 必须登录，游客会抛出401异常
    val userId = userInfoService.requireAuthenticationUserId()
    
    val payment = paymentService.processPayment(userId, orderId)
    
    return Result.success(payment)
}
```

### 场景5：功能标记模式

```kotlin
@GetMapping("/product/{id}")
fun getProduct(@PathVariable id: Long): Result<ProductDetailResponse> {
    val product = productService.getProductById(id)
    val currentUser = userInfoService.getCurrentUserInfoOrNull()
    
    // 根据认证状态设置功能标记
    val features = FeatureFlags(
        canPurchase = currentUser != null,
        canFavorite = currentUser != null,
        canComment = true,
        canShare = true,
        requireLogin = currentUser == null
    )
    
    return Result.success(ProductDetailResponse(
        product = product,
        features = features
    ))
}
```

---

## 最佳实践

### 1. 使用 getCurrentUserInfoOrNull() 处理可选认证

```kotlin
// ✅ 推荐
val currentUser = userInfoService.getCurrentUserInfoOrNull()
if (currentUser != null) {
    // 已登录逻辑
} else {
    // 游客逻辑
}

// ❌ 不推荐（会抛出异常）
try {
    val currentUser = userInfoService.getCurrentUserInfo()
} catch (e: CustomException) {
    // 游客逻辑
}
```

### 2. 使用 requireAuthentication() 强制认证

```kotlin
// ✅ 推荐（清晰表达意图）
@PostMapping("/pay")
fun pay(@RequestBody request: PayRequest): Result<Payment> {
    val userId = userInfoService.requireAuthenticationUserId()
    return Result.success(paymentService.pay(userId, request))
}

// ❌ 不推荐（需要手动检查）
@PostMapping("/pay")
fun pay(@RequestBody request: PayRequest): Result<Payment> {
    val userId = userInfoService.getCurrentUserIdOrNull()
        ?: throw CustomException("请先登录", 401)
    return Result.success(paymentService.pay(userId, request))
}
```

### 3. 在响应中提供功能可用性标记

```kotlin
// ✅ 推荐：前端可以根据标记显示/隐藏功能
data class ProductResponse(
    val product: Product,
    val features: FeatureFlags  // 功能标记
)

// ❌ 不推荐：前端需要自己判断
data class ProductResponse(
    val product: Product
)
```

### 4. 提供清晰的引导信息

```kotlin
// ✅ 推荐：在响应中提供引导信息
return Result.success(
    data = products,
    message = if (isGuest) "登录后可查看个性化推荐" else null
)

// ❌ 不推荐：没有引导信息
return Result.success(products)
```

---

## 配置说明

### SecurityConfig 配置

在 `SecurityConfig.kt` 中配置可选认证的接口路径：

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
        .authorizeHttpRequests { auth ->
            auth
                // 完全公开的接口
                .requestMatchers(
                    "/api/auth/**",
                    "/api/test/**"
                ).permitAll()
                // 可选认证的接口（允许游客访问）
                .requestMatchers(
                    "/api/products/**",
                    "/api/articles/**"
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
        }
    return http.build()
}
```

**注意**：
- `permitAll()` 表示允许未认证访问
- `JwtAuthenticationFilter` 仍然会尝试解析Token，如果Token有效会设置认证信息
- 业务代码中通过 `getCurrentUserInfoOrNull()` 判断是否有用户信息

---

## 错误处理

### 游客访问需要认证的接口

如果游客访问了需要认证的接口（使用了 `requireAuthentication()`），会抛出异常：

```kotlin
try {
    val userId = userInfoService.requireAuthenticationUserId()
} catch (e: CustomException) {
    if (e.code == 401) {
        // 处理未认证情况
        return Result.error("请先登录", 401)
    }
}
```

### 前端处理

前端可以根据响应中的标记决定显示内容：

```javascript
const response = await fetch('/api/product/1');
const data = await response.json();

if (data.data.features.requireLogin) {
    showLoginPrompt();
}

if (data.data.features.canPurchase) {
    showPurchaseButton();
} else {
    showLoginToPurchaseButton();
}
```

---

## 使用示例

所有使用场景都在上面的"使用场景"章节中提供了完整的代码示例。

---

## 常见问题

### Q1: 可选认证和完全公开有什么区别？

**A:** 
- **完全公开**：接口不需要任何认证，任何人都可以访问
- **可选认证**：接口允许未认证访问，但如果提供了有效Token，会识别用户身份并提供个性化内容

### Q2: 如何判断用户是游客还是已登录？

**A:** 使用 `getCurrentUserInfoOrNull()` 方法：
```kotlin
val user = userInfoService.getCurrentUserInfoOrNull()
if (user == null) {
    // 游客
} else {
    // 已登录
}
```

### Q3: 游客数据如何存储？

**A:** 可以使用以下方式：
- **Session ID**：前端生成并存储在Cookie中
- **临时Token**：后端生成临时Token标识游客
- **设备ID**：使用设备唯一标识

### Q4: 登录后如何合并游客数据？

**A:** 在登录接口中合并游客数据：
```kotlin
@PostMapping("/login")
fun login(
    @RequestBody request: LoginRequest,
    @RequestHeader("X-Guest-Session", required = false) guestSessionId: String?
): Result<AuthResponse> {
    val authResponse = authService.login(request)
    
    // 如果提供了游客会话ID，合并数据
    if (guestSessionId != null) {
        guestSessionService.migrateGuestDataToUser(
            guestSessionId, 
            authResponse.user.id
        )
    }
    
    return Result.success(authResponse)
}
```

---

## 总结

可选认证提供了灵活的认证机制，允许：
- ✅ 游客浏览公开内容
- ✅ 已登录用户享受个性化服务
- ✅ 渐进式引导用户注册
- ✅ 数据无缝迁移

通过合理使用可选认证，可以显著提升用户体验，同时保持系统的安全性。

