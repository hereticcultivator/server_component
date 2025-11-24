# 安全改进说明

本文档说明已实现的安全改进功能。

## 已实现的功能

### 1. ✅ 密码加密存储和验证
- **实现方式**: 使用BCrypt加密算法
- **修改文件**:
  - `UserService.kt`: 注册和登录时使用BCrypt加密/验证密码
  - `SecurityConfig.kt`: 已配置BCryptPasswordEncoder
- **说明**: 密码不再以明文存储，使用BCrypt单向哈希加密

### 2. ✅ 密码强度验证
- **实现方式**: 创建`PasswordValidator`工具类
- **验证规则**: 
  - 至少8位字符
  - 必须包含数字和字母（大小写均可）
- **修改文件**:
  - `util/PasswordValidator.kt`: 新增密码验证工具类
  - `UserService.kt`: 注册时验证密码强度

### 3. ✅ RefreshToken轮换机制
- **实现方式**: 使用Token版本号机制
- **工作原理**:
  1. 用户表新增`token_version`字段
  2. RefreshToken中包含版本号
  3. 刷新Token时验证版本号，成功后递增版本号
  4. 旧RefreshToken因版本号不匹配而失效
- **修改文件**:
  - `model/User.kt`: 添加`tokenVersion`字段
  - `TokenService.kt`: RefreshToken包含版本号
  - `AuthService.kt`: 实现轮换逻辑
  - `UserMapper.kt`: 添加版本号递增方法
- **SQL迁移**: `sql/migration_add_token_version.sql`

### 4. ✅ 密码重置功能
- **实现方式**: 验证码机制
- **流程**:
  1. 发送验证码到手机号
  2. 验证验证码
  3. 重置密码（同时使所有Token失效）
- **修改文件**:
  - `service/VerificationCodeService.kt`: 验证码服务（内存实现）
  - `service/AuthService.kt`: 添加密码重置方法
  - `controller/AuthController.kt`: 添加密码重置接口
  - `dto/UserDto.kt`: 添加密码重置相关DTO
- **API接口**:
  - `POST /api/auth/password-reset/send-code`: 发送验证码
  - `POST /api/auth/password-reset`: 重置密码

### 5. ✅ 登录日志/审计功能
- **实现方式**: 创建登录日志表和服务
- **记录内容**:
  - 用户ID、用户名
  - 登录类型（username/phone/register）
  - IP地址、User-Agent
  - 登录状态（success/failed）
  - 失败原因
  - 时间戳
- **修改文件**:
  - `model/LoginLog.kt`: 登录日志模型
  - `mapper/LoginLogMapper.kt`: 登录日志Mapper
  - `service/LoginLogService.kt`: 登录日志服务
  - `service/AuthService.kt`: 记录登录日志
- **SQL迁移**: `sql/login_logs_table.sql`

### 6. ✅ 输入验证和防护
- **实现方式**: Bean Validation
- **验证规则**:
  - 用户名: 3-50字符
  - 手机号: 11位数字，符合中国手机号格式
  - 密码: 非空（强度验证在业务层）
  - 验证码: 6位数字
- **修改文件**:
  - `pom.xml`: 添加`spring-boot-starter-validation`依赖
  - `dto/UserDto.kt`: 添加验证注解
  - `controller/AuthController.kt`: 使用`@Valid`注解
  - `exception/GlobalExceptionHandler.kt`: 处理验证异常

## 数据库迁移

### 1. 添加token_version字段
```sql
-- 执行文件: sql/migration_add_token_version.sql
ALTER TABLE users 
ADD COLUMN token_version INT NOT NULL DEFAULT 0 COMMENT 'Token版本号，用于RefreshToken轮换' 
AFTER last_login_at;
```

### 2. 创建登录日志表
```sql
-- 执行文件: sql/login_logs_table.sql
CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    login_type VARCHAR(20) NOT NULL COMMENT '登录类型：username/phone',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    login_status VARCHAR(20) NOT NULL COMMENT '登录状态：success/failed',
    failure_reason VARCHAR(200) COMMENT '失败原因',
    created_at BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_login_status (login_status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
```

## 注意事项

### 验证码服务
- 当前实现使用内存存储验证码，仅适用于开发环境
- **生产环境建议**:
  - 使用Redis存储验证码（支持分布式、自动过期）
  - 集成短信服务（阿里云、腾讯云等）发送验证码
  - 添加验证码发送频率限制（防止滥用）

### 密码重置
- 重置密码后，所有现有Token都会失效（通过递增tokenVersion实现）
- 用户需要重新登录

### 登录日志
- 登录日志会记录所有登录尝试（成功和失败）
- 可用于安全分析和异常检测
- 建议定期清理旧日志（如保留90天）

## API变更

### 新增接口
1. `POST /api/auth/password-reset/send-code` - 发送密码重置验证码
2. `POST /api/auth/password-reset` - 重置密码

### 接口变更
- 所有认证相关接口现在需要传递`HttpServletRequest`（用于记录IP和User-Agent）
- 所有DTO现在包含输入验证，会自动验证参数格式

## 测试建议

1. **密码加密测试**: 注册新用户，检查数据库密码是否为BCrypt哈希
2. **密码强度测试**: 尝试注册弱密码，应被拒绝
3. **RefreshToken轮换测试**: 
   - 使用RefreshToken刷新
   - 再次使用旧RefreshToken应失败
4. **密码重置测试**: 
   - 发送验证码
   - 使用验证码重置密码
   - 验证旧Token失效
5. **登录日志测试**: 登录后检查`login_logs`表是否有记录
6. **输入验证测试**: 发送格式错误的请求，应返回验证错误

## 后续建议

虽然已实现核心安全功能，但还可以考虑以下增强：

1. **防暴力破解**: 实现登录失败次数限制
2. **账户锁定**: 多次失败后锁定账户
3. **短信服务集成**: 替换内存验证码服务
4. **Redis集成**: 用于验证码和Token黑名单
5. **HTTPS强制**: 生产环境强制使用HTTPS
6. **CORS配置**: 明确配置跨域策略

