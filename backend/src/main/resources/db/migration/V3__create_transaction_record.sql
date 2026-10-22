-- V3：账单表
-- 使用 transaction_record 而不是 transaction，避免与 SQL 关键字冲突。
-- user_id 由服务端根据认证上下文写入，请求 DTO 不接受该字段。
CREATE TABLE transaction_record (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id          BIGINT        NOT NULL COMMENT '数据所有者',
    category_id      BIGINT        NOT NULL COMMENT '分类 ID',
    type             VARCHAR(10)   NOT NULL COMMENT 'INCOME 或 EXPENSE',
    amount           DECIMAL(12,2) NOT NULL COMMENT '正数金额',
    transaction_date DATE          NOT NULL COMMENT '账单发生日期',
    note             VARCHAR(200)  NULL COMMENT '备注，可为空',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_txn_user_date_id (user_id, transaction_date, id),
    KEY idx_txn_user_type_date (user_id, type, transaction_date),
    KEY idx_txn_user_category_date (user_id, category_id, transaction_date),
    CONSTRAINT ck_transaction_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT ck_transaction_amount CHECK (amount > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '账单表';
