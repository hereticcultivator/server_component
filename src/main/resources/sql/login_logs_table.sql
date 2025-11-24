-- 创建登录日志表
CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL COMMENT '用户ID（登录失败时为NULL）',
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
    INDEX idx_created_at (created_at)
    -- 不设置外键约束，原因：
    -- 1. 日志表应保留历史记录，即使用户已删除
    -- 2. 登录失败时 user_id 为 NULL，外键无法处理
    -- 3. 外键会影响插入性能
    -- 4. 在微服务架构中，外键难以维护
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

