-- ============================================
-- 菜单管理模块数据库表结构（关联表版本）
-- ============================================

-- 菜单表（共享表，不包含用户信息）
CREATE TABLE IF NOT EXISTS menus (
    id VARCHAR(50) PRIMARY KEY COMMENT '菜单ID，格式：menu_1001',
    title VARCHAR(200) NOT NULL COMMENT '菜单标题',
    category INT NOT NULL DEFAULT 0 COMMENT '分类：0=全部, 1=美团, 2=饿了么, 3=京东',
    icon_url VARCHAR(500) COMMENT '图标URL',
    extra_info VARCHAR(500) COMMENT '额外信息',
    create_time BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
    update_time BIGINT NOT NULL COMMENT '更新时间戳（毫秒）',
    INDEX idx_category (category),
    INDEX idx_title (title),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表（共享）';

-- 用户菜单关联表（记录用户拥有的菜单，支持未来分享功能）
CREATE TABLE IF NOT EXISTS user_menus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    menu_id VARCHAR(50) NOT NULL COMMENT '菜单ID',
    is_owner TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否拥有者：1=拥有者, 0=分享获得',
    permission VARCHAR(20) NOT NULL DEFAULT 'write' COMMENT '权限：read=只读, write=可编辑',
    create_time BIGINT NOT NULL COMMENT '关联创建时间戳（毫秒）',
    UNIQUE KEY uk_user_menu (user_id, menu_id),
    INDEX idx_user_id (user_id),
    INDEX idx_menu_id (menu_id),
    INDEX idx_is_owner (is_owner)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户菜单关联表';

-- 标签表（全局共享）
CREATE TABLE IF NOT EXISTS tags (
    id VARCHAR(50) PRIMARY KEY COMMENT '标签ID，格式：label_meal 或 custom_label_1001',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    type VARCHAR(20) NOT NULL DEFAULT 'system' COMMENT '标签类型：system=系统标签, custom=自定义标签',
    create_time BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
    INDEX idx_type (type),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 菜单标签关联表
CREATE TABLE IF NOT EXISTS menu_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id VARCHAR(50) NOT NULL COMMENT '菜单ID',
    tag_id VARCHAR(50) NOT NULL COMMENT '标签ID',
    create_time BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
    UNIQUE KEY uk_menu_tag (menu_id, tag_id),
    INDEX idx_menu_id (menu_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单标签关联表';

-- 菜单操作日志表
CREATE TABLE IF NOT EXISTS menu_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id VARCHAR(50) COMMENT '菜单ID（删除操作时可能为NULL）',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：create/update/delete/copy',
    operation_detail TEXT COMMENT '操作详情（JSON格式，记录变更内容）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    created_at BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
    INDEX idx_menu_id (menu_id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单操作日志表';

-- 初始化系统标签
SET NAMES utf8mb4;
INSERT INTO tags (id, name, type, create_time) VALUES
('label_meal', '大餐', 'system', UNIX_TIMESTAMP(NOW()) * 1000),
('label_hamburger', '汉堡', 'system', UNIX_TIMESTAMP(NOW()) * 1000),
('label_fried', '炸食', 'system', UNIX_TIMESTAMP(NOW()) * 1000)
ON DUPLICATE KEY UPDATE name=name;

