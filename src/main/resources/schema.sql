-- 主体属性表（若使用 ddl-auto=create-drop 可不用此文件；生产用 MySQL 时可执行）
CREATE TABLE IF NOT EXISTS subject_attribute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id VARCHAR(64) NOT NULL,
    attr_key VARCHAR(128) NOT NULL,
    attr_value VARCHAR(512) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (subject_id, attr_key)
);
CREATE INDEX idx_subject_attr_subject ON subject_attribute (subject_id);

-- 资源属性表
CREATE TABLE IF NOT EXISTS resource_attribute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128) NOT NULL,
    attr_key VARCHAR(128) NOT NULL,
    attr_value VARCHAR(512) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (resource_type, resource_id, attr_key)
);
CREATE INDEX idx_resource_attr_type_id ON resource_attribute (resource_type, resource_id);

-- 策略表
CREATE TABLE IF NOT EXISTS policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    effect VARCHAR(16) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    condition_json TEXT NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_policy_enabled_priority ON policy (enabled, priority DESC);
