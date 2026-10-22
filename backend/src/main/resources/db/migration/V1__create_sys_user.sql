-- V1：用户表
-- V1 只初始化一个所有者账号，但数据模型从一开始就按多用户隔离设计。
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '登录名',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 密码哈希，禁止存明文',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用，0=禁用',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表';
