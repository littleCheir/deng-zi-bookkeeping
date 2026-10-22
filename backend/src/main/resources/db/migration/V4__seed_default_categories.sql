-- V4：初始化默认分类（共 3 个）
-- 幂等写法：已存在同名系统分类时不重复插入；ID 全部由数据库生成。
INSERT INTO category (user_id, name, type, icon, sort_order, is_active)
SELECT NULL, '餐饮', 'EXPENSE', '🍜', 10, 1
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE user_id IS NULL AND type = 'EXPENSE' AND name = '餐饮'
);

INSERT INTO category (user_id, name, type, icon, sort_order, is_active)
SELECT NULL, '其他', 'EXPENSE', '📦', 90, 1
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE user_id IS NULL AND type = 'EXPENSE' AND name = '其他'
);

INSERT INTO category (user_id, name, type, icon, sort_order, is_active)
SELECT NULL, '工资', 'INCOME', '💰', 10, 1
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE user_id IS NULL AND type = 'INCOME' AND name = '工资'
);
