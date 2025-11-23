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
  "code": "200",        // 状态码：200-成功，500-服务器错误
  "data": {},           // 响应数据
  "msg": "success"      // 响应消息
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功（注册） |
| 400 | 请求参数错误 |
| 401 | 认证失败（用户名/手机号或密码错误） |
| 404 | 资源不存在（用户不存在） |
| 500 | 服务器错误 |

---

## 1. 用户注册

### 接口信息

- **URL**: `/api/auth/register`
- **Method**: `POST`
- **HTTP Status**: `201 Created`
- **描述**: 注册新用户账号

### 请求参数

**Request Body** (JSON):

```json
{
  "username": "string",      // 必填，用户名，3-50个字符
  "password": "string",      // 必填，密码，至少6个字符
  "phoneNumber": "string",   // 必填，手机号，唯一
  "fullName": "string"        // 可选，全名
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名，唯一，3-50个字符 |
| password | String | 是 | 密码，至少6个字符（明文存储） |
| phoneNumber | String | 是 | 手机号，唯一 |
| fullName | String | 否 | 用户全名 |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "phoneNumber": "13800138000",
    "fullName": "测试用户"
  }'
```

### 响应示例

**成功响应** (201 Created):

```json
{
  "code": "200",
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "phoneNumber": "13800138000",
      "fullName": "测试用户",
      "role": 0,
      "createdAt": 1704067200000,
      "lastLoginAt": null
    }
  },
  "msg": "注册成功"
}
```

**错误响应** (500 Internal Server Error):

```json
{
  "code": "500",
  "data": null,
  "msg": "用户名已存在"
}
```

或

```json
{
  "code": "500",
  "data": null,
  "msg": "手机号已被注册"
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| user.id | Long | 用户ID |
| user.username | String | 用户名 |
| user.phoneNumber | String | 手机号 |
| user.fullName | String | 全名（可为null） |
| user.role | Integer | 角色：0=普通用户，1=管理员 |
| user.createdAt | Long | 创建时间戳（毫秒） |
| user.lastLoginAt | Long | 最后登录时间戳（毫秒，可为null） |

---

## 2. 账号密码登录

### 接口信息

- **URL**: `/api/auth/login/username`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 使用用户名和密码登录

### 请求参数

**Request Body** (JSON):

```json
{
  "username": "string",  // 必填，用户名
  "password": "string"   // 必填，密码
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名，不能为空 |
| password | String | 是 | 密码，不能为空 |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/login/username \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": "200",
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "phoneNumber": "13800138000",
      "fullName": "测试用户",
      "role": 0,
      "createdAt": 1704067200000,
      "lastLoginAt": 1704067300000
    }
  },
  "msg": "登录成功"
}
```

**错误响应** (401 Unauthorized):

```json
{
  "code": "401",
  "data": null,
  "msg": "用户名或密码错误"
}
```

**参数错误响应** (400 Bad Request):

```json
{
  "code": "400",
  "data": null,
  "msg": "用户名不能为空"
}
```

或

```json
{
  "code": "400",
  "data": null,
  "msg": "密码不能为空"
}
```

### 响应字段说明

响应字段与注册接口相同，但 `lastLoginAt` 会在登录成功后更新为当前时间戳。

---

## 3. 手机号登录

### 接口信息

- **URL**: `/api/auth/login/phone`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 使用手机号和密码登录

### 请求参数

**Request Body** (JSON):

```json
{
  "phoneNumber": "string",  // 必填，手机号
  "password": "string"      // 必填，密码
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phoneNumber | String | 是 | 手机号，不能为空 |
| password | String | 是 | 密码，不能为空 |

### 请求示例

```bash
curl -X POST http://localhost:9999/api/auth/login/phone \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "13800138000",
    "password": "123456"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": "200",
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "phoneNumber": "13800138000",
      "fullName": "测试用户",
      "role": 0,
      "createdAt": 1704067200000,
      "lastLoginAt": 1704067300000
    }
  },
  "msg": "登录成功"
}
```

**错误响应** (401 Unauthorized):

```json
{
  "code": "401",
  "data": null,
  "msg": "手机号或密码错误"
}
```

**参数错误响应** (400 Bad Request):

```json
{
  "code": "400",
  "data": null,
  "msg": "手机号不能为空"
}
```

或

```json
{
  "code": "400",
  "data": null,
  "msg": "密码不能为空"
}
```

### 响应字段说明

响应字段与注册接口相同，但 `lastLoginAt` 会在登录成功后更新为当前时间戳。

---

## 4. 绑定手机号

### 接口信息

- **URL**: `/api/auth/bind-phone`
- **Method**: `POST`
- **HTTP Status**: `200 OK`
- **描述**: 为用户绑定手机号

### 请求参数

**Query Parameters**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**Request Body** (JSON):

```json
{
  "phoneNumber": "string"  // 必填，手机号，必须是11位数字
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phoneNumber | String | 是 | 手机号，必须是11位数字，不能为空 |

### 请求示例

```bash
curl -X POST "http://localhost:9999/api/auth/bind-phone?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "13800138000"
  }'
```

### 响应示例

**成功响应** (200 OK):

```json
{
  "code": "200",
  "data": {
    "id": 1,
    "username": "testuser",
    "phoneNumber": "13800138000",
    "fullName": "测试用户",
    "role": 0,
    "createdAt": 1704067200000,
    "lastLoginAt": 1704067300000
  },
  "msg": "绑定手机号成功"
}
```

**错误响应** (400 Bad Request):

```json
{
  "code": "400",
  "data": null,
  "msg": "手机号不能为空"
}
```

或

```json
{
  "code": "400",
  "data": null,
  "msg": "手机号必须是11位数字"
}
```

或

```json
{
  "code": "400",
  "data": null,
  "msg": "手机号已被其他用户使用"
}
```

**错误响应** (404 Not Found):

```json
{
  "code": "404",
  "data": null,
  "msg": "用户不存在"
}
```

---

## 错误码说明

| 错误消息 | 说明 | HTTP状态码 |
|---------|------|-----------|
| 用户名已存在 | 注册时用户名已被使用 | 400 |
| 手机号已被注册 | 注册时手机号已被使用 | 400 |
| 用户名或密码错误 | 账号密码登录时用户名或密码不正确 | 401 |
| 手机号或密码错误 | 手机号登录时手机号或密码不正确 | 401 |
| 用户不存在 | 查询用户时用户不存在 | 404 |
| 手机号不能为空 | 手机号参数为空或只包含空白字符 | 400 |
| 密码不能为空 | 密码参数为空或只包含空白字符 | 400 |
| 用户名不能为空 | 用户名参数为空或只包含空白字符 | 400 |
| 手机号必须是11位数字 | 绑定手机号时手机号长度不正确 | 400 |
| 手机号已被其他用户使用 | 绑定或更新手机号时手机号已被占用 | 400 |

---

## 数据模型

### UserResponse

```json
{
  "id": 1,                      // Long，用户ID
  "username": "testuser",       // String，用户名
  "phoneNumber": "13800138000", // String，手机号
  "fullName": "测试用户",       // String，全名（可为null）
  "role": 0,                    // Integer，角色：0=USER, 1=ADMIN
  "createdAt": 1704067200000,   // Long，创建时间戳（毫秒）
  "lastLoginAt": 1704067300000  // Long，最后登录时间戳（毫秒，可为null）
}
```

### 角色说明

| role值 | 说明 |
|--------|------|
| 0 | 普通用户（USER） |
| 1 | 管理员（ADMIN） |

---

## 注意事项

1. **密码安全**: 当前版本密码以明文存储，仅用于开发测试，生产环境请使用加密存储
2. **时间戳格式**: 所有时间字段使用 Unix 时间戳（毫秒），需要转换为日期时请除以 1000
3. **唯一性约束**: 
   - 用户名（username）必须唯一
   - 手机号（phoneNumber）必须唯一
4. **登录方式**: 
   - 使用 `/api/auth/login/username` 进行账号密码登录
   - 使用 `/api/auth/login/phone` 进行手机号登录
5. **字符编码**: 所有请求和响应使用 UTF-8 编码

---

## 测试用例

### 测试流程

1. **注册新用户**
   ```bash
   POST /api/auth/register
   {
     "username": "testuser",
     "password": "123456",
     "phoneNumber": "13800138000",
     "fullName": "测试用户"
   }
   ```

2. **使用账号密码登录**
   ```bash
   POST /api/auth/login/username
   {
     "username": "testuser",
     "password": "123456"
   }
   ```

3. **使用手机号登录**
   ```bash
   POST /api/auth/login/phone
   {
     "phoneNumber": "13800138000",
     "password": "123456"
   }
   ```

4. **测试错误密码（账号密码登录）**
   ```bash
   POST /api/auth/login/username
   {
     "username": "testuser",
     "password": "wrongpassword"
   }
   ```
   预期返回：`{"code": "401", "msg": "用户名或密码错误"}`

5. **测试错误密码（手机号登录）**
   ```bash
   POST /api/auth/login/phone
   {
     "phoneNumber": "13800138000",
     "password": "wrongpassword"
   }
   ```
   预期返回：`{"code": "401", "msg": "手机号或密码错误"}`

6. **绑定手机号**
   ```bash
   POST /api/auth/bind-phone?userId=1
   {
     "phoneNumber": "13900139000"
   }
   ```

5. **测试重复注册**
   ```bash
   POST /api/auth/register
   {
     "username": "testuser",
     "password": "123456",
     "phoneNumber": "13800138001"
   }
   ```
   预期返回：`{"code": "500", "msg": "用户名已存在"}`

---

## 版本信息

- **API 版本**: v1.0
- **最后更新**: 2024-01-01
- **服务端口**: 9999

