# 客户端 Token 接入指南

## 概述

本文档说明如何接入本系统的 JWT Token 认证机制。系统采用行业标准的**双Token机制**（AccessToken + RefreshToken），确保安全性和用户体验的平衡。

## Token 机制说明

### AccessToken（访问令牌）
- **用途**：用于访问受保护的API资源
- **有效期**：1小时（可在配置文件中修改）
- **使用频率**：每次API请求都需要携带
- **存储建议**：内存或临时存储（如 sessionStorage）

### RefreshToken（刷新令牌）
- **用途**：用于获取新的 AccessToken
- **有效期**：7天（可在配置文件中修改）
- **使用频率**：仅在 AccessToken 过期时使用
- **存储建议**：安全存储（如 localStorage 或 HttpOnly Cookie）

## 认证流程

### 1. 用户登录/注册

系统提供统一登录接口，支持三种登录方式：

#### 1.1 用户名密码登录

```bash
POST /api/auth/login
Content-Type: application/json

{
  "loginType": "username_password",
  "username": "testuser",
  "password": "12345678"
}
```

#### 1.2 手机号密码登录（支持自动注册）

```bash
POST /api/auth/login
Content-Type: application/json

{
  "loginType": "phone_password",
  "phoneNumber": "13800138000",
  "password": "12345678",
  "autoRegister": true,
  "fullName": "测试用户"
}
```

#### 1.3 手机号验证码登录（支持自动注册）

```bash
# 步骤1：发送验证码
POST /api/auth/send-code
Content-Type: application/json

{
  "phoneNumber": "13800138000",
  "purpose": "login"
}

# 步骤2：使用验证码登录
POST /api/auth/login
Content-Type: application/json

{
  "loginType": "phone_code",
  "phoneNumber": "13800138000",
  "verificationCode": "123456",
  "autoRegister": true,
  "fullName": "测试用户"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "phone_number": "13800138000",
      "full_name": "测试用户",
      "role": 0,
      "created_at": 1704067200000,
      "last_login_at": 1704067300000
    },
    "tokens": {
      "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsInJvbGUiOjAsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3MDQwNjczMDAsImV4cCI6MTcwNDA3MDkwMH0...",
      "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MDQwNjczMDAsImV4cCI6MTcwNDY3MjEwMH0..."
    },
    "is_new_user": false
  },
  "success": true,
  "timestamp": 1704067300000
}
```

**注意**：
- 自动注册时，`is_new_user` 为 `true`，消息为 "注册并登录成功"
- 所有登录方式都会返回Token对（access_token + refresh_token）
- 所有响应字段使用 snake_case 格式

#### 客户端处理

```javascript
// 存储token
const response = await login(loginType, credentials);
localStorage.setItem('accessToken', response.data.tokens.access_token);
localStorage.setItem('refreshToken', response.data.tokens.refresh_token);

// 示例：用户名密码登录
async function loginByUsername(username, password) {
  const response = await fetch('http://localhost:9999/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      loginType: 'username_password',
      username: username,
      password: password
    })
  });
  const data = await response.json();
  if (data.code === 200) {
    localStorage.setItem('accessToken', data.data.tokens.access_token);
    localStorage.setItem('refreshToken', data.data.tokens.refresh_token);
  }
  return data;
}

// 示例：手机号验证码登录
async function loginByPhoneCode(phoneNumber, verificationCode) {
  const response = await fetch('http://localhost:9999/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      loginType: 'phone_code',
      phoneNumber: phoneNumber,
      verificationCode: verificationCode,
      autoRegister: true
    })
  });
  const data = await response.json();
  if (data.code === 200) {
    localStorage.setItem('accessToken', data.data.tokens.access_token);
    localStorage.setItem('refreshToken', data.data.tokens.refresh_token);
  }
  return data;
}
```

### 2. 访问受保护的API

#### 请求头格式

所有需要认证的API请求，必须在请求头中携带 AccessToken：

```
Authorization: Bearer <accessToken>
```

#### 请求示例

```bash
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone_number": "13800138000",
    "full_name": "测试用户",
    "role": 0,
    "created_at": 1704067200000,
    "last_login_at": 1704067300000
  },
  "success": true,
  "timestamp": 1704067300000
}
```

#### Token过期响应（401）

```json
{
  "code": 401,
  "message": "Token已过期",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

### 3. 刷新Token

当 AccessToken 过期时（返回401），客户端应自动使用 RefreshToken 获取新的 AccessToken。

#### 请求示例

```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "tokens": {
      "access_token": "eyJhbGciOiJIUzI1NiJ9...新token",
      "refresh_token": "eyJhbGciOiJIUzI1NiJ9...新token"
    }
  },
  "success": true,
  "timestamp": 1704067300000
}
```

#### RefreshToken过期响应（401）

```json
{
  "code": 401,
  "message": "Token已过期",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

当 RefreshToken 也过期时，需要引导用户重新登录。

## 客户端实现示例

### JavaScript/TypeScript (Axios)

```javascript
import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:9999/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器：自动添加AccessToken
apiClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器：自动处理Token过期
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 如果是401错误且未重试过
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // 尝试刷新token
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          // 没有refreshToken，跳转登录
          window.location.href = '/login';
          return Promise.reject(error);
        }

        const response = await axios.post(
          'http://localhost:9999/api/auth/refresh',
          { refreshToken }
        );

        const { access_token, refresh_token } = response.data.data.tokens;

        // 更新token
        localStorage.setItem('accessToken', access_token);
        localStorage.setItem('refreshToken', refresh_token);

        // 重试原请求
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // 刷新失败，清除token并跳转登录
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// 使用示例
export const login = async (loginType, credentials) => {
  const response = await apiClient.post('/auth/login', {
    loginType,
    ...credentials
  });
  
  // 存储token
  if (response.data.data.tokens) {
    localStorage.setItem('accessToken', response.data.data.tokens.access_token);
    localStorage.setItem('refreshToken', response.data.data.tokens.refresh_token);
  }
  
  return response.data;
};

// 示例：用户名密码登录
export const loginByUsername = async (username, password) => {
  return login('username_password', { username, password });
};

// 示例：手机号密码登录
export const loginByPhonePassword = async (phoneNumber, password) => {
  return login('phone_password', { phoneNumber, password, autoRegister: true });
};

// 示例：手机号验证码登录
export const loginByPhoneCode = async (phoneNumber, verificationCode) => {
  return login('phone_code', { phoneNumber, verificationCode, autoRegister: true });
};

// 发送验证码
export const sendCode = async (phoneNumber, purpose = 'login') => {
  const response = await apiClient.post('/auth/send-code', {
    phoneNumber,
    purpose
  });
  return response.data;
};

export const getUserProfile = async () => {
  const response = await apiClient.get('/user/profile');
  return response.data;
};
```

### React Hook 示例

```typescript
import { useState, useEffect } from 'react';
import axios from 'axios';

// Token管理Hook
export const useToken = () => {
  const [accessToken, setAccessToken] = useState<string | null>(
    localStorage.getItem('accessToken')
  );
  const [refreshToken, setRefreshToken] = useState<string | null>(
    localStorage.getItem('refreshToken')
  );

  const saveTokens = (access: string, refresh: string) => {
    setAccessToken(access);
    setRefreshToken(refresh);
    localStorage.setItem('accessToken', access);
    localStorage.setItem('refreshToken', refresh);
  };

  const clearTokens = () => {
    setAccessToken(null);
    setRefreshToken(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  };

  return { accessToken, refreshToken, saveTokens, clearTokens };
};

// API调用Hook
export const useApi = () => {
  const { accessToken, saveTokens, clearTokens } = useToken();

  const apiCall = async (url: string, options: RequestInit = {}) => {
    const response = await fetch(`http://localhost:9999/api${url}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        ...options.headers,
      },
    });

    // 处理401错误
    if (response.status === 401) {
      // 尝试刷新token
      const refreshResult = await refreshAccessToken();
      if (refreshResult) {
        // 重试原请求
        return apiCall(url, options);
      } else {
        // 刷新失败，清除token
        clearTokens();
        window.location.href = '/login';
        throw new Error('Token已过期，请重新登录');
      }
    }

    return response.json();
  };

  return { apiCall };
};
```

### Android (Kotlin) 示例

```kotlin
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Token管理
object TokenManager {
    private const val PREF_NAME = "token_pref"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }
    
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}

// 拦截器：自动添加Token和刷新
class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 添加AccessToken
        val accessToken = TokenManager.getAccessToken()
        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }
        
        var response = chain.proceed(request)
        
        // 处理401错误
        if (response.code == 401) {
            val refreshToken = TokenManager.getRefreshToken()
            if (refreshToken != null) {
                // 尝试刷新token
                val newTokens = refreshToken(refreshToken)
                if (newTokens != null) {
                    // 重试原请求
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                    response = chain.proceed(newRequest)
                } else {
                    // 刷新失败，清除token
                    TokenManager.clearTokens()
                    // 跳转登录页面
                }
            }
        }
        
        return response
    }
    
    private fun refreshToken(refreshToken: String): TokenResponse? {
        // 调用刷新接口
        // ...
        return null
    }
}
```

### iOS (Swift) 示例

```swift
import Foundation
import Alamofire

class TokenManager {
    static let shared = TokenManager()
    
    private let accessTokenKey = "accessToken"
    private let refreshTokenKey = "refreshToken"
    
    var accessToken: String? {
        get { UserDefaults.standard.string(forKey: accessTokenKey) }
        set { UserDefaults.standard.set(newValue, forKey: accessTokenKey) }
    }
    
    var refreshToken: String? {
        get { UserDefaults.standard.string(forKey: refreshTokenKey) }
        set { UserDefaults.standard.set(newValue, forKey: refreshTokenKey) }
    }
    
    func saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }
    
    func clearTokens() {
        accessToken = nil
        refreshToken = nil
    }
}

// 请求适配器：自动添加Token
class TokenRequestAdapter: RequestInterceptor {
    func adapt(_ urlRequest: URLRequest, for session: Session, completion: @escaping (Result<URLRequest, Error>) -> Void) {
        var request = urlRequest
        if let token = TokenManager.shared.accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        completion(.success(request))
    }
    
    func retry(_ request: Request, for session: Session, dueTo error: Error, completion: @escaping (RetryResult) -> Void) {
        guard let response = request.task?.response as? HTTPURLResponse,
              response.statusCode == 401 else {
            completion(.doNotRetry)
            return
        }
        
        // 尝试刷新token
        refreshAccessToken { success in
            completion(success ? .retry : .doNotRetry)
        }
    }
    
    private func refreshAccessToken(completion: @escaping (Bool) -> Void) {
        guard let refreshToken = TokenManager.shared.refreshToken else {
            completion(false)
            return
        }
        
        AF.request("http://localhost:9999/api/auth/refresh",
                   method: .post,
                   parameters: ["refreshToken": refreshToken],
                   encoding: JSONEncoding.default)
            .responseDecodable(of: RefreshResponse.self) { response in
                switch response.result {
                case .success(let data):
                    TokenManager.shared.saveTokens(
                        access: data.data.tokens.access_token,
                        refresh: data.data.tokens.refresh_token
                    )
                    completion(true)
                case .failure:
                    TokenManager.shared.clearTokens()
                    completion(false)
                }
            }
    }
}
```

## 错误处理

### 常见错误码

| HTTP状态码 | 错误码 | 说明 | 处理方式 |
|-----------|--------|------|---------|
| 401 | 401 | Token无效或已过期 | 尝试刷新Token，失败则重新登录 |
| 400 | 400 | 请求参数错误 | 检查请求参数 |
| 404 | 404 | 资源不存在 | 检查请求路径 |
| 500 | 500 | 服务器错误 | 记录错误，提示用户稍后重试 |

### 错误响应格式

```json
{
  "code": 401,
  "message": "Token已过期",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

## 安全建议

1. **Token存储**
   - AccessToken：建议存储在内存中（如 sessionStorage），页面关闭后自动清除
   - RefreshToken：可以存储在 localStorage，但要注意XSS攻击防护

2. **HTTPS传输**
   - 生产环境必须使用HTTPS，防止Token被中间人攻击窃取

3. **Token轮换**
   - 每次刷新Token时，建议同时更新RefreshToken（本系统已实现）

4. **登出处理**
   - 用户登出时，清除本地存储的Token
   - 可以考虑实现服务端Token黑名单机制

5. **错误处理**
   - 不要将Token信息记录到日志中
   - 401错误时自动刷新，避免频繁弹出登录提示

## 测试接口

### 公开接口（不需要Token）

- `POST /api/auth/login` - 统一登录接口（支持三种登录方式，支持自动注册）
- `POST /api/auth/send-code` - 发送验证码
- `POST /api/auth/refresh` - 刷新Token
- `POST /api/auth/password-reset/send-code` - 发送密码重置验证码
- `POST /api/auth/password-reset` - 重置密码
- `GET /api/test/**` - 测试接口

**注意**：注册功能已集成到统一登录接口中，通过自动注册实现，无需单独的注册接口。

### 受保护接口（需要Token）

所有其他 `/api/**` 接口都需要在请求头中携带 `Authorization: Bearer <accessToken>`

## 配置说明

Token有效期配置在服务端 `application.yml` 文件中：

```yaml
jwt:
  secret: hahaha-your-secret-key-minimum-32-characters-long-for-security
  access-token-expiration: 3600000  # AccessToken有效期：1小时（毫秒）
  refresh-token-expiration: 604800000  # RefreshToken有效期：7天（毫秒）
```

客户端无需关心这些配置，只需按照本文档实现Token的存储和刷新逻辑即可。

## 常见问题

### Q1: AccessToken过期后，每次都要手动刷新吗？

A: 不需要。建议在HTTP客户端拦截器中实现自动刷新逻辑，当收到401错误时自动调用刷新接口。

### Q2: RefreshToken也过期了怎么办？

A: RefreshToken过期后，需要引导用户重新登录。可以在刷新失败时清除本地Token并跳转到登录页面。

### Q3: 可以在多个设备同时登录吗？

A: 当前实现支持多设备登录。每个设备登录后会获得独立的Token对。

### Q4: Token被盗用了怎么办？

A: 由于AccessToken有效期较短（1小时），影响有限。如果发现Token泄露，可以：
1. 等待Token自然过期
2. 修改密码（会导致所有Token失效）
3. 实现Token黑名单机制（需要服务端支持）

### Q5: 如何实现登出功能？

A: 客户端登出时清除本地存储的Token即可。服务端可以考虑实现Token黑名单，但当前版本暂不支持。

## 更新日志

- **v1.0** (2024-01-01): 初始版本，实现双Token机制

## 技术支持

如有问题，请联系开发团队或查看服务端API文档。


