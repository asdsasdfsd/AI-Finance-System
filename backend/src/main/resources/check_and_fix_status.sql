-- backend/src/main/resources/check_and_fix_status.sql
-- 首先检查 Transaction 表的结构

-- ====================
-- 1. 检查表结构
-- ====================

DESCRIBE Transaction;

-- 查看 status 字段的具体定义
SHOW COLUMNS FROM Transaction LIKE 'status';

-- 查看当前 Transaction 表的数据
SELECT transaction_id, company_id, transaction_type, amount, status, transaction_date 
FROM Transaction 
LIMIT 10;

-- ====================
-- 2. 根据状态字段类型修复
-- ====================

-- 如果 status 字段是整数类型，需要按照枚举序号设置
-- DRAFT = 0, PENDING_APPROVAL = 1, APPROVED = 2, REJECTED = 3, CANCELLED = 4, VOIDED = 5

-- 情况1：如果 status 字段是整数类型
UPDATE Transaction 
SET status = 2  -- APPROVED = 2
WHERE status IS NULL OR status = 0;

-- 情况2：如果 status 字段是字符串类型
-- UPDATE Transaction 
-- SET status = 'APPROVED' 
-- WHERE status IS NULL OR status = '' OR status = 'DRAFT';

-- ====================
-- 3. 验证修复结果
-- ====================

SELECT 'Status Distribution' as report,
       status, 
       COUNT(*) as count,
       transaction_type,
       SUM(amount) as total_amount
FROM Transaction 
GROUP BY status, transaction_type
ORDER BY status, transaction_type;

-- ====================
-- 4. 检查是否有 account_balance 表
-- ====================

SHOW TABLES LIKE 'account_balance';

-- 查看 account_balance 表结构（如果存在）
-- DESCRIBE account_balance;

-- 查看 Account 表数据
SELECT company_id, account_type, COUNT(*) as account_count
FROM Account 
WHERE is_active = TRUE
GROUP BY company_id, account_type
ORDER BY company_id, account_type;

-- ====================
-- 5. 添加 account_balance 数据（简化版本）
-- ====================

-- 如果 account_balance 表存在但没有数据，添加一些测试数据
INSERT IGNORE INTO account_balance (account_id, current_month, year_to_date, previous_year, created_at, updated_at) 
SELECT 
    a.account_id,
    CASE 
        WHEN a.account_type = 'ASSET' THEN 
            CASE a.account_id % 5
                WHEN 0 THEN 500000.00
                WHEN 1 THEN 300000.00
                WHEN 2 THEN 150000.00
                WHEN 3 THEN 200000.00
                ELSE 100000.00
            END
        WHEN a.account_type = 'LIABILITY' THEN
            CASE a.account_id % 3
                WHEN 0 THEN 100000.00
                WHEN 1 THEN 50000.00
                ELSE 30000.00
            END
        WHEN a.account_type = 'EQUITY' THEN
            CASE a.account_id % 2
                WHEN 0 THEN 800000.00
                ELSE 400000.00
            END
        ELSE 50000.00
    END as current_month,
    CASE 
        WHEN a.account_type = 'ASSET' THEN 
            CASE a.account_id % 5
                WHEN 0 THEN 600000.00
                WHEN 1 THEN 350000.00
                WHEN 2 THEN 180000.00
                WHEN 3 THEN 250000.00
                ELSE 120000.00
            END
        WHEN a.account_type = 'LIABILITY' THEN
            CASE a.account_id % 3
                WHEN 0 THEN 120000.00
                WHEN 1 THEN 60000.00
                ELSE 35000.00
            END
        WHEN a.account_type = 'EQUITY' THEN
            CASE a.account_id % 2
                WHEN 0 THEN 900000.00
                ELSE 500000.00
            END
        ELSE 60000.00
    END as year_to_date,
    CASE 
        WHEN a.account_type = 'ASSET' THEN 
            CASE a.account_id % 5
                WHEN 0 THEN 450000.00
                WHEN 1 THEN 280000.00
                WHEN 2 THEN 120000.00
                WHEN 3 THEN 200000.00
                ELSE 80000.00
            END
        WHEN a.account_type = 'LIABILITY' THEN
            CASE a.account_id % 3
                WHEN 0 THEN 80000.00
                WHEN 1 THEN 40000.00
                ELSE 20000.00
            END
        WHEN a.account_type = 'EQUITY' THEN
            CASE a.account_id % 2
                WHEN 0 THEN 700000.00
                ELSE 300000.00
            END
        ELSE 40000.00
    END as previous_year,
    NOW() as created_at,
    NOW() as updated_at
FROM Account a 
WHERE a.company_id = 1 
AND a.is_active = TRUE;

-- 为其他公司复制数据
INSERT IGNORE INTO account_balance (account_id, current_month, year_to_date, previous_year, created_at, updated_at) 
SELECT 
    a.account_id,
    ab.current_month * 0.8,
    ab.year_to_date * 0.8,
    ab.previous_year * 0.8,
    NOW(),
    NOW()
FROM Account a 
JOIN Account a1 ON a1.company_id = 1 AND a1.account_code = a.account_code
JOIN account_balance ab ON ab.account_id = a1.account_id
WHERE a.company_id = 2 
AND a.is_active = TRUE;

INSERT IGNORE INTO account_balance (account_id, current_month, year_to_date, previous_year, created_at, updated_at) 
SELECT 
    a.account_id,
    ab.current_month * 0.6,
    ab.year_to_date * 0.6,
    ab.previous_year * 0.6,
    NOW(),
    NOW()
FROM Account a 
JOIN Account a1 ON a1.company_id = 1 AND a1.account_code = a.account_code
JOIN account_balance ab ON ab.account_id = a1.account_id
WHERE a.company_id = 3 
AND a.is_active = TRUE;

-- ====================
-- 6. 最终验证
-- ====================

-- 验证 Transaction 数据
SELECT 'Final Transaction Check' as report,
       company_id,
       status,
       transaction_type,
       COUNT(*) as count,
       SUM(amount) as total_amount
FROM Transaction
WHERE company_id = 1
GROUP BY company_id, status, transaction_type
ORDER BY status, transaction_type;

-- 验证 Account Balance 数据
SELECT 'Final Balance Check' as report,
       a.company_id,
       a.account_type,
       COUNT(ab.account_id) as accounts_with_balance,
       SUM(ab.current_month) as total_current
FROM Account a
LEFT JOIN account_balance ab ON a.account_id = ab.account_id
WHERE a.company_id = 1 AND a.is_active = TRUE
GROUP BY a.company_id, a.account_type
ORDER BY a.account_type;