-- 添加token_version字段到users表（用于RefreshToken轮换）
ALTER TABLE users 
ADD COLUMN token_version INT NOT NULL DEFAULT 0 COMMENT 'Token版本号，用于RefreshToken轮换' 
AFTER last_login_at;

