-- V5：分类管理与去重相关索引
-- 1) 按 type + name 查询（重名校验）的辅助索引
-- 2) 个人分类在 user_id + type + name 上的唯一约束：
--    系统分类 user_id 为 NULL，MySQL 唯一索引对 NULL 不判重，因此不会阻止多个系统分类；
--    应用层仍会按“系统分类 + 当前用户个人分类”的可见范围做重名校验。
ALTER TABLE category
    ADD KEY idx_category_type_name (type, name),
    ADD UNIQUE KEY uk_category_user_type_name (user_id, type, name);
