# 用户认证 API 文档

## 基础信息

- **Base URL**: `http://localhost:9999`
- **API 前缀**: `/api`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`

## 统一响应格式

所有接口返回统一的响应格式：

```json
{
  "code": 200,              // 状态码：200-成功，其他为错误码（数字类型）
  "message": "success",     // 响应消息
  "data": {},               // 响应数据（泛型，成功时包含数据，失败时为null）
  "success": true,          // 是否成功（true/false）
  "timestamp": 1678888888000  // 时间戳（毫秒）
}
```

**注意**：所有响应字段使用 snake_case 格式，但 `message`、`code`、`data`、`success`、`timestamp` 等单单词字段保持不变。

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 认证失败（Token无效或已过期） |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 1. 统一登录接口（支持自动注册）

### 接口信息

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 统一登录接口，支持多种登录方式和自动注册。**注意：注册功能已集成到此接口中，通过自动注册实现，无需单独的注册接口。**

### 支持的登录方式

1. **手机号验证码登录** (`phone_code`) - **支持自动注册**（推荐用于新用户注册）
2. **手机号密码登录** (`phone_password`) - **支持自动注册**（推荐用于新用户注册）
3. **用户名密码登录** (`username_password`) - 不支持自动注册（仅用于已注册用户登录）

### 自动注册说明

- **自动注册功能**：当使用手机号登录方式（`phone_code` 或 `phone_password`）时，如果用户不存在且 `autoRegister` 为 `true`，系统会自动创建新用户账号
- **用户名生成**：自动注册时，系统会自动生成唯一用户名（格式：`user_手机号后4位`，如 `user_8000`）
- **密码设置**：
  - 使用 `phone_code` 登录时，自动注册的用户初始没有密码，后续可通过密码重置功能设置密码
  - 使用 `phone_password` 登录时，自动注册的用户会使用提供的密码
- **注册标识**：自动注册成功后，响应中的 `is_new_user` 字段为 `true`，消息为 "注册并登录成功"

### 请求参数

**Request Body** (JSON):

```json
{
  "login_type": "string",           // 必填，登录类型："phone_code" | "phone_password" | "username_password"
  "phone_number": "string",          // 手机号登录时必填
  "verification_code": "string",     // 验证码登录时必填
  "username": "string",             // 用户名登录时必填
  "password": "string",              // 密码登录时必填
  "auto_register": true,              // 可选，是否允许自动注册（仅手机号登录支持），默认true
  "full_name": "string"              // 可选，自动注册时的用户全名
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| login_type | String | 是 | 登录类型：`phone_code`（手机号验证码）、`phone_password`（手机号密码）、`username_password`（用户名密码） |
| phone_number | String | 条件必填 | 手机号（`phone_code` 和 `phone_password` 需要），格式：1[3-9]开头的11位数字 |
| verification_code | String | 条件必填 | 验证码（`phone_code` 需要），6位数字 |
| username | String | 条件必填 | 用户名（`username_password` 需要），3-50个字符 |
| password | String | 条件必填 | 密码（`phone_password` 和 `username_password` 需要） |
| auto_register | Boolean | 否 | 是否允许自动注册（仅手机号登录支持），默认 `true` |
| full_name | String | 否 | 自动注册时的用户全名 |

### 请求示例

#### 示例1：手机号验证码登录（自动注册）

```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login_type": "phone_code",
    "phone_number": "13800138000",
    "verification_code": "123456",
    "auto_register": true,
    "full_name": "测试用户"
  }'
```

#### 示例2：手机号密码登录

```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login_type": "phone_password",
    "phone_number": "13800138000",
    "password": "12345678"
  }'
```

#### 示例3：用户名密码登录

```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login_type": "username_password",
    "username": "testuser",
    "password": "12345678"
  }'
```

### 响应示例

**成功响应** (200 OK):

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
      "access_token": "eyJhbGciOiJIUzI1NiJ9...",
      "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
    },
    "is_new_user": false
  },
  "success": true,
  "timestamp": 1704067300000
}
```

**自动注册成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "注册并登录成功",
  "data": {
    "user": {
      "id": 2,
      "username": "user_8000",
      "phone_number": "13800138000",
      "full_name": "测试用户",
      "role": 0,
      "created_at": 1704067400000,
      "last_login_at": 1704067400000
    },
    "tokens": {
      "access_token": "eyJhbGciOiJIUzI1NiJ9...",
      "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
    },
    "is_new_user": true
  },
  "success": true,
  "timestamp": 1704067400000
}
```

**错误响应** (400 Bad Request):

```json
{
  "code": 400,
  "message": "不支持的登录类型: invalid_type",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

或

```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

**错误响应** (401 Unauthorized):

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

或

```json
{
  "code": 401,
  "message": "手机号或密码错误",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

**错误响应** (404 Not Found):

```json
{
  "code": 404,
  "message": "用户不存在，请先注册",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| user | Object | 用户信息对象 |
| user.id | Long | 用户ID |
| user.username | String | 用户名 |
| user.phone_number | String | 手机号 |
| user.full_name | String | 全名（可为null） |
| user.role | Integer | 角色：0=普通用户，1=管理员 |
| user.created_at | Long | 创建时间戳（毫秒） |
| user.last_login_at | Long | 最后登录时间戳（毫秒，可为null） |
| tokens | Object | Token信息对象 |
| tokens.access_token | String | 访问令牌（用于API认证） |
| tokens.refresh_token | String | 刷新令牌（用于刷新AccessToken） |
| is_new_user | Boolean | 是否为新注册用户（自动注册场景） |

---

## 2. 发送验证码

### 接口信息

- **URL**: `/api/auth/send-code`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 发送验证码（登录/注册通用）

### 请求参数

**Request Body** (JSON):

```json
{
  "phone_number": "string",  // 必填，手机号
  "purpose": "login"         // 可选，用途："login" | "register" | "reset_password"，默认"login"
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone_number | String | 是 | 手机号，格式：1[3-9]开头的11位数字 |
| purpose | String | 否 | 用途，默认 `"login"` |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone_number": "13800138000",
    "purpose": "login"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null,
  "success": true,
  "timestamp": 1704067300000
}
```

**注意**：开发环境下，验证码会打印在控制台日志中，格式：`验证码（开发环境）: 13800138000 -> 123456 (purpose: login)`

---

## 3. 刷新Token

### 接口信息

- **URL**: `/api/auth/refresh`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 使用RefreshToken刷新AccessToken和RefreshToken（实现Token轮换机制）

### 请求参数

**Request Body** (JSON):

```json
{
  "refresh_token": "string"  // 必填，刷新令牌
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refresh_token | String | 是 | 刷新令牌 |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

### 响应示例

**成功响应** (200 OK):

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

**错误响应** (401 Unauthorized):

```json
{
  "code": 401,
  "message": "RefreshToken已失效，请重新登录",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

**注意**：刷新Token后，旧的RefreshToken会失效，实现安全轮换机制。

---

## 4. 发送密码重置验证码

### 接口信息

- **URL**: `/api/auth/password-reset/send-code`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 发送密码重置验证码

### 请求参数

**Request Body** (JSON):

```json
{
  "phone_number": "string"  // 必填，手机号
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone_number | String | 是 | 手机号，格式：1[3-9]开头的11位数字 |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/password-reset/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone_number": "13800138000"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null,
  "success": true,
  "timestamp": 1704067300000
}
```

**错误响应** (404 Not Found):

```json
{
  "code": 404,
  "message": "该手机号未注册",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

---

## 5. 重置密码

### 接口信息

- **URL**: `/api/auth/password-reset`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 通过验证码重置密码

### 请求参数

**Request Body** (JSON):

```json
{
  "phone_number": "string",        // 必填，手机号
  "verification_code": "string",   // 必填，验证码，6位数字
  "new_password": "string"         // 必填，新密码
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone_number | String | 是 | 手机号，格式：1[3-9]开头的11位数字 |
| verification_code | String | 是 | 验证码，6位数字 |
| new_password | String | 是 | 新密码，至少8位，需包含数字和字母 |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/password-reset \
  -H "Content-Type: application/json" \
  -d '{
    "phone_number": "13800138000",
    "verification_code": "123456",
    "new_password": "newpass123"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null,
  "success": true,
  "timestamp": 1704067300000
}
```

**错误响应** (400 Bad Request):

```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

或

```json
{
  "code": 400,
  "message": "密码强度不符合要求",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

**错误响应** (404 Not Found):

```json
{
  "code": 404,
  "message": "该手机号未注册",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

**注意**：密码重置成功后，所有现有的Token都会失效，用户需要重新登录。

---

## 6. 获取当前用户信息

### 接口信息

- **URL**: `/api/users/me`
- **Method**: `GET`
- **HTTP Status**: `200 OK`
- **描述**: 获取当前登录用户的信息（需要Token认证）

### 请求头

```
Authorization: Bearer <access_token>
```

### 请求示例

```bash
curl -X GET http://localhost:9999/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 响应示例

**成功响应** (200 OK):

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

**错误响应** (401 Unauthorized):

```json
{
  "code": 401,
  "message": "Token无效或已过期",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

---

## 7. 获取指定用户信息

### 接口信息

- **URL**: `/api/users/{userId}`
- **Method**: `GET`
- **HTTP Status**: `200 OK`
- **描述**: 获取指定用户的信息（需要Token认证，用户只能查看自己的信息，管理员可以查看所有用户）

### 请求头

```
Authorization: Bearer <access_token>
```

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

### 请求示例

```bash
curl -X GET http://localhost:9999/api/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 响应示例

**成功响应** (200 OK):

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

**错误响应** (403 Forbidden):

```json
{
  "code": 403,
  "message": "无权操作其他用户的数据",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

**错误响应** (404 Not Found):

```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

---

## 8. 更新用户信息

### 接口信息

- **URL**: `/api/users/{userId}`
- **Method**: `PUT`
- **HTTP Status**: `200 OK`
- **描述**: 更新用户信息（需要Token认证，用户只能更新自己的信息，管理员可以更新所有用户）

### 请求头

```
Authorization: Bearer <access_token>
```

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

### 请求参数

**Request Body** (JSON):

```json
{
  "full_name": "string",     // 可选，用户全名
  "phone_number": "string"   // 可选，手机号
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| full_name | String | 否 | 用户全名 |
| phone_number | String | 否 | 手机号，格式：1[3-9]开头的11位数字 |

### 请求示例

```bash
curl -X PUT http://localhost:9999/api/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "full_name": "新名字",
    "phone_number": "13900139000"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone_number": "13900139000",
    "full_name": "新名字",
    "role": 0,
    "created_at": 1704067200000,
    "last_login_at": 1704067300000
  },
  "success": true,
  "timestamp": 1704067300000
}
```

**错误响应** (400 Bad Request):

```json
{
  "code": 400,
  "message": "手机号已被其他用户使用",
  "data": null,
  "success": false,
  "timestamp": 1704067300000
}
```

---

## 9. 获取用户资料（示例接口）

### 接口信息

- **URL**: `/api/users/profile`
- **Method**: `GET`
- **HTTP Status**: `200 OK`
- **描述**: 使用 SecurityContextHelper 手动获取用户信息的示例接口（需要Token认证）

### 请求头

```
Authorization: Bearer <access_token>
```

### 请求示例

```bash
curl -X GET http://localhost:9999/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 响应示例

**成功响应** (200 OK):

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

---

## 错误码说明

| 错误消息 | 说明 | HTTP状态码 |
|---------|------|-----------|
| 手机号已被注册 | 自动注册时手机号已被使用 | 400 |
| 验证码错误或已过期 | 验证码验证失败 | 400 |
| 用户不存在，请先注册 | 登录时用户不存在且不允许自动注册 | 404 |
| 该手机号未注册 | 密码重置时手机号未注册 | 404 |
| 用户不存在 | 查询用户时用户不存在 | 404 |
| 手机号不能为空 | 手机号参数为空或只包含空白字符 | 400 |
| 密码不能为空 | 密码参数为空或只包含空白字符 | 400 |
| 用户名不能为空 | 用户名参数为空或只包含空白字符 | 400 |
| 手机号已被其他用户使用 | 更新手机号时手机号已被占用 | 400 |
| 不支持的登录类型 | 登录类型不是支持的值 | 400 |
| 该账号未设置密码，请使用验证码登录 | 手机号密码登录时账号未设置密码 | 400 |
| RefreshToken已失效，请重新登录 | RefreshToken无效或已过期 | 401 |
| Token无效或已过期 | AccessToken无效或已过期 | 401 |
| 无权操作其他用户的数据 | 用户尝试操作其他用户的数据 | 403 |
| 密码强度不符合要求 | 密码不符合强度要求（至少8位，需包含数字和字母） | 400 |

---

## 数据模型

### UserResponse

```json
{
  "id": 1,                      // Long，用户ID
  "username": "testuser",       // String，用户名
  "phone_number": "13800138000", // String，手机号
  "full_name": "测试用户",       // String，全名（可为null）
  "role": 0,                    // Integer，角色：0=USER, 1=ADMIN
  "created_at": 1704067200000,   // Long，创建时间戳（毫秒）
  "last_login_at": 1704067300000  // Long，最后登录时间戳（毫秒，可为null）
}
```

### TokenInfo

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",  // String，访问令牌
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."  // String，刷新令牌
}
```

### AuthResponse

```json
{
  "user": { ... },              // UserResponse，用户信息
  "tokens": { ... },            // TokenInfo，Token信息
  "is_new_user": false          // Boolean，是否为新注册用户
}
```

### 角色说明

| role值 | 说明 |
|--------|------|
| 0 | 普通用户（USER） |
| 1 | 管理员（ADMIN） |

---

## 注意事项

1. **密码安全**: 密码使用BCrypt加密存储，生产环境已安全
2. **时间戳格式**: 所有时间字段使用 Unix 时间戳（毫秒），需要转换为日期时请除以 1000
3. **唯一性约束**: 
   - 用户名（username）必须唯一
   - 手机号（phone_number）必须唯一
4. **登录和注册**: 
   - 使用统一登录接口 `/api/auth/login`，支持三种登录方式：
     - `phone_code`: 手机号验证码登录（**支持自动注册，推荐用于新用户注册**）
     - `phone_password`: 手机号密码登录（**支持自动注册，推荐用于新用户注册**）
     - `username_password`: 用户名密码登录（不支持自动注册，仅用于已注册用户登录）
   - **注册功能已集成到登录接口中**，通过自动注册实现，无需单独的注册接口
5. **Token机制**: 
   - 所有登录和注册接口都会返回Token对（access_token + refresh_token）
   - AccessToken有效期：1小时
   - RefreshToken有效期：7天
   - 刷新Token时会实现Token轮换，旧的RefreshToken会失效
6. **自动注册**: 
   - 仅手机号登录方式支持自动注册
   - 自动注册时会自动生成用户名（格式：user_手机号后4位）
   - 如果自动注册时未提供密码，后续可通过密码重置功能设置密码
7. **验证码**: 
   - 开发环境下验证码会打印在控制台日志中
   - 生产环境需要接入短信服务
   - 验证码有效期：5分钟
8. **数据权限**: 
   - 用户只能查看和修改自己的信息
   - 管理员可以查看和修改所有用户的信息
9. **字符编码**: 所有请求和响应使用 UTF-8 编码
10. **响应格式**: 所有响应字段使用 snake_case 格式

---

## 测试用例

### 测试流程

1. **新用户注册（使用手机号验证码登录自动注册）**
   ```bash
   # 步骤1：发送验证码
   POST /api/auth/send-code
   {
     "phone_number": "13800138000",
     "purpose": "login"
   }
   
   # 步骤2：使用验证码登录（自动注册）
   POST /api/auth/login
   {
     "login_type": "phone_code",
     "phone_number": "13800138000",
     "verification_code": "123456",
     "auto_register": true,
     "full_name": "测试用户"
   }
   ```
   预期返回：包含用户信息和Token对，`is_new_user: true`，消息为 "注册并登录成功"

2. **已注册用户登录（手机号验证码）**
   ```bash
   # 步骤1：发送验证码
   POST /api/auth/send-code
   {
     "phone_number": "13800138000",
     "purpose": "login"
   }
   
   # 步骤2：使用验证码登录
   POST /api/auth/login
   {
     "login_type": "phone_code",
     "phone_number": "13800138000",
     "verification_code": "123456",
     "auto_register": false
   }
   ```
   预期返回：包含用户信息和Token对，`is_new_user: false`，消息为 "登录成功"
   注意：开发环境下查看控制台日志获取验证码

3. **手机号密码登录**
   ```bash
   POST /api/auth/login
   {
     "login_type": "phone_password",
     "phone_number": "13800138000",
     "password": "12345678"
   }
   ```
   预期返回：包含用户信息和Token对

4. **用户名密码登录**
   ```bash
   POST /api/auth/login
   {
     "login_type": "username_password",
     "username": "testuser",
     "password": "12345678"
   }
   ```
   预期返回：包含用户信息和Token对

5. **刷新Token**
   ```bash
   POST /api/auth/refresh
   {
     "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
   }
   ```
   预期返回：新的Token对

6. **获取当前用户信息**
   ```bash
   GET /api/users/me
   Authorization: Bearer <access_token>
   ```
   预期返回：当前用户信息

7. **更新用户信息**
   ```bash
   PUT /api/users/1
   Authorization: Bearer <access_token>
   {
     "full_name": "新名字",
     "phone_number": "13900139000"
   }
   ```
   预期返回：更新后的用户信息

8. **密码重置流程**
    ```bash
    # 步骤1：发送密码重置验证码
    POST /api/auth/password-reset/send-code
    {
      "phone_number": "13800138000"
    }
    
    # 步骤2：重置密码（使用控制台日志中的验证码）
    POST /api/auth/password-reset
    {
      "phone_number": "13800138000",
      "verification_code": "123456",
      "new_password": "newpass123"
    }
    ```
    预期返回：`{"code": 200, "message": "密码重置成功"}`
    注意：密码重置后所有Token失效，需要重新登录

9. **测试错误场景**
    - 错误密码：预期返回 `{"code": 401, "message": "用户名或密码错误"}`
    - 错误验证码：预期返回 `{"code": 400, "message": "验证码错误或已过期"}`
    - 重复注册：预期返回 `{"code": 400, "message": "手机号已被注册"}` 或 `{"code": 400, "message": "用户名已存在"}`
    - 未认证访问：预期返回 `{"code": 401, "message": "Token无效或已过期"}`
    - 权限不足：预期返回 `{"code": 403, "message": "无权操作其他用户的数据"}`

---

## 版本信息

- **API 版本**: v1.0
- **最后更新**: 2024-01-01
- **服务端口**: 9999
