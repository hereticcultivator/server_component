-- 创建用户表（不含邮箱字段）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    phone_number VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    full_name VARCHAR(100) COMMENT '全名',
    role INT NOT NULL DEFAULT 0 COMMENT '角色：0=USER, 1=ADMIN',
    created_at BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳（毫秒）',
    last_login_at BIGINT COMMENT '最后登录时间戳（毫秒）',
    token_version INT NOT NULL DEFAULT 0 COMMENT 'Token版本号，用于RefreshToken轮换',
    INDEX idx_username (username),
    INDEX idx_phone_number (phone_number),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

