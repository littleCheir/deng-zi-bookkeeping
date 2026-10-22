-- V2：分类表
-- user_id IS NULL 表示系统默认分类；否则为该用户的个人分类。
CREATE TABLE category (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT      NULL COMMENT 'NULL=系统分类；非 NULL=该用户的个人分类',
    name       VARCHAR(50) NOT NULL COMMENT '分类名称',
    type       VARCHAR(10) NOT NULL COMMENT 'INCOME 或 EXPENSE',
    icon       VARCHAR(50) NULL COMMENT '图标：单个 emoji 或短文本',
    sort_order INT         NOT NULL DEFAULT 0 COMMENT '展示顺序，升序',
    is_active  TINYINT     NOT NULL DEFAULT 1 COMMENT '1=启用，0=停用（V1 不提供启停接口）',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_user_type_active_sort (user_id, type, is_active, sort_order),
    KEY idx_category_type_sort (type, sort_order),
    CONSTRAINT ck_category_type CHECK (type IN ('INCOME', 'EXPENSE'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '分类表';
