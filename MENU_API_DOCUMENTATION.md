# 菜单管理 API 文档

## 基础信息

- **Base URL**: `/api/v1/game/hungry`
- **字符编码**: UTF-8
- **认证方式**: JWT Token（在请求头中携带：`Authorization: Bearer {token}`）
- **响应格式**: JSON（使用 snake_case 命名）

## 通用响应结构

所有接口均返回以下统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "success": true,
  "timestamp": 1678888888000
}
```

**字段说明**：
- `code`: 业务状态码（200=成功，其他为错误码）
- `message`: 响应消息
- `data`: 响应数据（泛型，成功时包含数据，失败时为null）
- `success`: 是否成功（true/false）
- `timestamp`: 时间戳（毫秒）

**错误响应示例**：
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null,
  "success": false,
  "timestamp": 1678888888000
}
```

---

## 1. 菜单管理接口

### 1.1 获取菜单列表（分页）

**接口地址**: `GET /menus`

**认证要求**: ✅ 需要JWT Token

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码，从0开始 |
| size | int | 否 | 20 | 每页数量 |
| category | int | 否 | - | 分类筛选：0=全部, 1=美团, 2=饿了么, 3=京东 |
| q | string | 否 | - | 搜索关键词，匹配标题 |

**请求示例**:
```http
GET /api/v1/game/hungry/menus?page=0&size=20&category=1&q=麦当劳
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "menu_1001",
        "title": "麦当劳巨无霸套餐",
        "category": 1,
        "icon_url": "https://pic1.zhimg.com/v2-dbe7bb1af51fd232aaacab8a226e3383.png",
        "extra_info": "包含薯条和可乐",
        "tags": [
          {
            "id": "label_meal",
            "name": "大餐",
            "type": "system"
          },
          {
            "id": "label_hamburger",
            "name": "汉堡",
            "type": "system"
          }
        ],
        "create_time": 1678888888000
      }
    ],
    "total_elements": 100,
    "total_pages": 5,
    "number": 0,
    "size": 20,
    "last": false,
    "first": true,
    "empty": false
  },
  "success": true,
  "timestamp": 1678888888000
}
```

**说明**: 
- 只返回当前用户拥有的菜单（通过 `user_menus` 关联表过滤）
- 管理员可以查看所有菜单
- 分页从0开始（Spring Boot标准）

---

### 1.2 获取菜单详情

**接口地址**: `GET /menus/{id}`

**认证要求**: ✅ 需要JWT Token

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 菜单ID |

**请求示例**:
```http
GET /api/v1/game/hungry/menus/menu_1001
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "menu_1001",
    "title": "麦当劳巨无霸套餐",
    "category": 1,
    "icon_url": "https://pic1.zhimg.com/v2-dbe7bb1af51fd232aaacab8a226e3383.png",
    "extra_info": "包含薯条和可乐",
    "tags": [
      {
        "id": "label_meal",
        "name": "大餐",
        "type": "system"
      },
      {
        "id": "label_hamburger",
        "name": "汉堡",
        "type": "system"
      }
    ],
    "create_time": 1678888888000
  },
  "success": true,
  "timestamp": 1678888888000
}
```

**说明**: 
- 只能查看自己有权限访问的菜单
- 管理员可以查看所有菜单

---

### 1.3 创建菜单

**接口地址**: `POST /menus`

**认证要求**: ✅ 需要JWT Token

**请求体**:
```json
{
  "title": "肯德基全家桶",
  "category": 2,
  "icon_url": "https://picx.zhimg.com/v2-627fda7c0b5131dc613c0f65dfdfde23.png",
  "extra_info": "8块鸡",
  "tag_ids": ["label_fried", "label_meal"]
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | 是 | 菜单标题（最多200个字符） |
| category | int | 否 | 分类：0=全部, 1=美团, 2=饿了么, 3=京东（默认0） |
| icon_url | string | 否 | 图标URL |
| extra_info | string | 否 | 额外信息 |
| tag_ids | array[string] | 否 | 标签ID列表（默认空数组） |

**请求示例**:
```http
POST /api/v1/game/hungry/menus
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "肯德基全家桶",
  "category": 2,
  "icon_url": "https://picx.zhimg.com/v2-627fda7c0b5131dc613c0f65dfdfde23.png",
  "extra_info": "8块鸡",
  "tag_ids": ["label_fried", "label_meal"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "menu_1002",
    "title": "肯德基全家桶",
    "category": 2,
    "icon_url": "https://picx.zhimg.com/v2-627fda7c0b5131dc613c0f65dfdfde23.png",
    "extra_info": "8块鸡",
    "tags": [
      {
        "id": "label_fried",
        "name": "炸食",
        "type": "system"
      },
      {
        "id": "label_meal",
        "name": "大餐",
        "type": "system"
      }
    ],
    "create_time": 1679999999000
  },
  "success": true,
  "timestamp": 1679999999000
}
```

**说明**: 
- 创建的菜单自动关联到当前用户（在 `user_menus` 表中建立关联）
- 当前用户自动成为菜单的拥有者
- 菜单ID自动生成，格式：`menu_数字`

---

### 1.4 更新菜单

**接口地址**: `PUT /menus/{id}`

**认证要求**: ✅ 需要JWT Token

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 菜单ID |

**请求体**（所有字段可选）:
```json
{
  "title": "肯德基全家桶 (升级版)",
  "category": 2,
  "icon_url": "https://picx.zhimg.com/v2-627fda7c0b5131dc613c0f65dfdfde23.png",
  "extra_info": "10块鸡",
  "tag_ids": ["label_fried"]
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | 否 | 菜单标题（最多200个字符） |
| category | int | 否 | 分类：0=全部, 1=美团, 2=饿了么, 3=京东 |
| icon_url | string | 否 | 图标URL |
| extra_info | string | 否 | 额外信息 |
| tag_ids | array[string] | 否 | 标签ID列表（传入后会替换所有标签） |

**请求示例**:
```http
PUT /api/v1/game/hungry/menus/menu_1002
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "肯德基全家桶 (升级版)",
  "category": 2,
  "extra_info": "10块鸡",
  "tag_ids": ["label_fried"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "menu_1002",
    "title": "肯德基全家桶 (升级版)",
    "category": 2,
    "icon_url": "https://picx.zhimg.com/v2-627fda7c0b5131dc613c0f65dfdfde23.png",
    "extra_info": "10块鸡",
    "tags": [
      {
        "id": "label_fried",
        "name": "炸食",
        "type": "system"
      }
    ],
    "create_time": 1679999999000
  },
  "success": true,
  "timestamp": 1679999999000
}
```

**说明**: 
- 只能更新自己有编辑权限的菜单（`permission = 'write'`）
- 管理员可以更新所有菜单
- 如果传入 `tag_ids`，会替换所有现有标签

---

### 1.5 批量删除菜单

**接口地址**: `DELETE /menus`

**认证要求**: ✅ 需要JWT Token

**请求体**:
```json
{
  "ids": ["menu_1001", "menu_1002"]
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | array[string] | 是 | 菜单ID列表 |

**请求示例**:
```http
DELETE /api/v1/game/hungry/menus
Authorization: Bearer {token}
Content-Type: application/json

{
  "ids": ["menu_1001", "menu_1002"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": {
    "deleted_count": 2
  },
  "success": true,
  "timestamp": 1678888888000
}
```

**说明**: 
- 只能删除自己拥有的菜单（`is_owner = 1`）
- 管理员可以删除所有菜单
- 删除菜单会同时删除关联的用户菜单关系和标签关系
- 如果菜单还有其他用户关联，只删除当前用户的关联关系

---

### 1.6 复制菜单

**接口地址**: `POST /menus/{id}/copy`

**认证要求**: ✅ 需要JWT Token

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 菜单ID |

**请求示例**:
```http
POST /api/v1/game/hungry/menus/menu_1001/copy
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "复制成功",
  "data": {
    "id": "menu_1003",
    "title": "麦当劳巨无霸套餐 (副本)",
    "category": 1,
    "icon_url": "https://pic1.zhimg.com/v2-dbe7bb1af51fd232aaacab8a226e3383.png",
    "extra_info": "包含薯条和可乐",
    "tags": [
      {
        "id": "label_meal",
        "name": "大餐",
        "type": "system"
      },
      {
        "id": "label_hamburger",
        "name": "汉堡",
        "type": "system"
      }
    ],
    "create_time": 1680000000000
  },
  "success": true,
  "timestamp": 1680000000000
}
```

**说明**: 
- 只能复制自己有权限访问的菜单
- 新菜单自动关联到当前用户，当前用户成为拥有者
- 标题自动追加 "(副本)" 后缀
- 会复制所有标签关联

---

## 2. 标签管理接口

### 2.1 获取所有标签

**接口地址**: `GET /tags`

**认证要求**: ❌ 不需要认证（公开接口）

**请求示例**:
```http
GET /api/v1/game/hungry/tags
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "label_meal",
      "name": "大餐",
      "type": "system"
    },
    {
      "id": "label_fried",
      "name": "炸食",
      "type": "system"
    },
    {
      "id": "custom_label_1001",
      "name": "我的最爱",
      "type": "custom"
    }
  ],
  "success": true,
  "timestamp": 1678888888000
}
```

**说明**: 
- 标签是全局共享的，所有用户都可以看到
- 包含系统标签和自定义标签
- 按类型和时间排序

---

### 2.2 创建自定义标签

**接口地址**: `POST /tags`

**认证要求**: ❌ 不需要认证（公开接口）

**请求体**:
```json
{
  "name": "夜宵"
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 是 | 标签名称（最多6个字符） |

**请求示例**:
```http
POST /api/v1/game/hungry/tags
Content-Type: application/json

{
  "name": "夜宵"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "custom_label_1002",
    "name": "夜宵",
    "type": "custom"
  },
  "success": true,
  "timestamp": 1678888888000
}
```

**说明**: 
- 标签名称最多6个字符
- 标签名称不能重复
- 标签ID自动生成，格式：`custom_label_数字`

---

## 3. 分享解析接口

### 3.1 解析分享链接

**接口地址**: `POST /parse/share`

**认证要求**: ❌ 不需要认证（公开接口）

**功能说明**: 
解析各大外卖及电商平台（美团、饿了么、京东、淘宝、大众点评）的分享文案，自动处理短链重定向，提取关键信息并返回客户端可执行的跳转指令列表。

**请求体**:
```json
{
  "text": "【美团外卖】猪八戒烤鸭... http://dpurl.cn/..."
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | string | 是 | 包含链接的分享文本 |

**请求示例**:
```http
POST /api/v1/game/hungry/parse/share
Content-Type: application/json

{
  "text": "【美团外卖】猪八戒烤鸭... http://dpurl.cn/..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "meta": {
      "title": "解析成功",
      "desc": "检测到【美团外卖】店铺：猪八戒烤鸭\n建议跳转。",
      "icon": null,
      "platform": "MEITUAN" // 枚举: MEITUAN, ELEME, JINGDONG, TAOBAO, DIANPING, UNKNOWN
    },
    "actions": [
      {
        "type": "SCHEME", // 动作类型: SCHEME, WEB, TOAST
        "packageName": "com.sankuai.meituan",
        "uri": "imeituan://www.meituan.com/search?q=猪八戒烤鸭",
        "intentFlags": ["NEW_TASK"]
      }
    ]
  },
  "success": true,
  "timestamp": 1678888888000
}
```

**平台策略说明**:

| 平台 | 标识 | 策略说明 |
|------|------|----------|
| 美团外卖 | MEITUAN | 使用 `imeituan://` 搜索协议跳转 |
| 京东 | JINGDONG | 优先识别小程序协议 (`openapp.jdmobile://`)，支持复杂参数解析 |
| 淘宝 | TAOBAO | 依次尝试：ShopID跳转 -> ItemID跳转 -> HTTPS直连 (App Link) -> Scheme降级 (`taobao://`) |
| 大众点评 | DIANPING | 使用 `dianping://` 搜索协议跳转 |
| 饿了么 | ELEME | 暂未实现特殊跳转，回退到 Web |

**说明**: 
- 接口会自动处理短链重定向以获取真实 URL
- 模拟移动端 User-Agent 以获取正确的页面内容
- 优先从分享文本中提取店名（如「...」格式），其次尝试从网页标题提取

---

## 4. 数据模型

### 4.1 MenuResponse（菜单响应）

```json
{
  "id": "string",           // 菜单ID，格式：menu_1001
  "title": "string",        // 菜单标题
  "category": 0,            // 分类：0=全部, 1=美团, 2=饿了么, 3=京东
  "icon_url": "string",     // 图标URL（可选）
  "extra_info": "string",   // 额外信息（可选）
  "tags": [                 // 标签列表
    {
      "id": "string",
      "name": "string",
      "type": "string"      // system/custom
    }
  ],
  "create_time": 1678888888000  // 创建时间戳（毫秒）
}
```

### 4.2 TagResponse（标签响应）

```json
{
  "id": "string",      // 标签ID，格式：label_xxx 或 custom_label_xxx
  "name": "string",    // 标签名称
  "type": "string"     // 标签类型：system/custom
}
```

### 4.3 ParseResult（解析结果）

```json
{
  "meta": {
    "title": "string",
    "desc": "string",
    "icon": "string",
    "platform": "string" // MEITUAN, ELEME, JINGDONG, TAOBAO, DIANPING, UNKNOWN
  },
  "actions": [
    {
      "type": "string",  // SCHEME, WEB, TOAST
      "packageName": "string",
      "uri": "string",
      "intentFlags": ["string"]
    }
  ]
}
```

### 4.4 PageResponse（分页响应）

```json
{
  "content": [],              // 数据列表
  "total_elements": 100,      // 总记录数
  "total_pages": 5,           // 总页数
  "number": 0,                // 当前页码（从0开始）
  "size": 20,                 // 每页数量
  "last": false,              // 是否最后一页
  "first": true,              // 是否第一页
  "empty": false              // 是否为空
}
```

---

## 5. 数据权限说明

### 5.1 菜单权限

- **拥有者** (`is_owner = 1`): 可以查看、编辑、删除菜单
- **分享用户** (`is_owner = 0`): 
  - `permission = 'write'`: 可以查看、编辑菜单
  - `permission = 'read'`: 只能查看菜单
- **管理员**: 可以查看、编辑、删除所有菜单

### 5.2 查询过滤

- **普通用户**：只能查询到自己有权限访问的菜单（通过 `user_menus` 关联表过滤）
- **管理员**：可以查询所有菜单

### 5.3 操作权限

| 操作 | 拥有者 | 分享用户(write) | 分享用户(read) | 管理员 |
|------|--------|----------------|----------------|--------|
| 查看菜单 | ✅ | ✅ | ✅ | ✅ |
| 创建菜单 | ✅ | ✅ | ✅ | ✅ |
| 更新菜单 | ✅ | ✅ | ❌ | ✅ |
| 删除菜单 | ✅ | ❌ | ❌ | ✅ |
| 复制菜单 | ✅ | ✅ | ✅ | ✅ |

---

## 6. 错误码说明

| 错误码 | 说明 | 示例 |
|--------|------|------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 参数验证失败、标签不存在 |
| 401 | 未认证 | 需要登录、Token无效 |
| 403 | 无权限 | 无权访问该菜单、无权修改该菜单 |
| 404 | 资源不存在 | 菜单不存在、用户不存在 |
| 500 | 服务器内部错误 | 系统异常、数据库连接失败 |

**错误响应示例**：
```json
{
  "code": 403,
  "message": "无权访问该菜单",
  "data": null,
  "success": false,
  "timestamp": 1678888888000
}
```

---

## 7. 注意事项

1. **时间戳格式**：所有时间戳均为毫秒级Unix时间戳
2. **ID格式**：
   - 菜单ID：`menu_数字`
   - 系统标签ID：`label_标识符`
   - 自定义标签ID：`custom_label_数字`
3. **标签限制**：标签名称最多6个字符
4. **分页规则**：分页从0开始（Spring Boot标准）
5. **认证要求**：
   - 菜单管理接口：需要JWT Token
   - 标签获取接口：不需要认证（公开）
   - 标签创建接口：不需要认证（公开）
   - 分享解析接口：不需要认证（公开）
6. **数据隔离**：菜单是共享的，但通过 `user_menus` 关联表实现用户维度的数据隔离
7. **未来扩展**：未来分享功能可以通过在 `user_menus` 表中添加记录实现

---

## 8. 数据库设计说明

### 8.1 表结构

- **menus**: 菜单共享表，存储菜单基本信息
- **user_menus**: 用户菜单关联表，记录用户拥有的菜单及权限
- **tags**: 标签表，全局共享
- **menu_tags**: 菜单标签关联表
- **menu_operation_logs**: 菜单操作日志表

### 8.2 关联关系

- 一个菜单可以被多个用户拥有（通过 `user_menus` 表）
- 一个用户可以拥有多个菜单
- 一个菜单可以有多个标签（通过 `menu_tags` 表）
- 一个标签可以关联多个菜单

### 8.3 未来扩展

- **分享功能**：在 `user_menus` 表中添加记录，设置 `is_owner = 0` 和相应的 `permission`
- **分享码功能**：可以添加 `menu_shares` 表存储分享码和分享关系

---

## 9. 更新日志

- **2025-11-30**:
  - 新增分享链接解析接口 (`/parse/share`)
  - 添加 `ParseResult` 等数据模型定义
  - 移除客户端示例，仅保留服务端文档
  - 更新解析策略，支持 DIANPING 及更完善的京东/淘宝跳转逻辑
- **2025-11-28**: 
  - 初始版本
  - 支持菜单的CRUD操作
  - 支持标签管理
  - 实现用户维度的数据隔离
  - 标签接口设为公开（不需要认证）

