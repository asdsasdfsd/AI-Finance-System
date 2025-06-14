-- backend/src/main/resources/fix_data.sql
-- 修复脚本：添加缺失的 status 字段和 account_balance 数据

-- backend/src/main/resources/correct_structure_fix.sql
-- 基于实际 account_balance 表结构的修复脚本

-- ====================
-- Transaction Status 映射:
-- 0: DRAFT ("Draft")
-- 1: PENDING_APPROVAL ("Pending Approval")
-- 2: APPROVED ("Approved")
-- 3: REJECTED ("Rejected")
-- 4: CANCELLED ("Cancelled")
-- 5: VOIDED ("Voided")
-- ====================

-- ====================
-- 1. 检查和修复 Transaction Status
-- ====================

-- 查看当前 Transaction 状态
SELECT 'Current Transaction Status' as report,
       status,
       COUNT(*) as count
FROM Transaction 
GROUP BY status;

-- 首先将现有的字符串状态值转换为对应的整数值
UPDATE Transaction 
SET status = CASE 
    WHEN status = 'Draft' OR status = 'DRAFT' THEN '0'
    WHEN status = 'Pending Approval' OR status = 'PENDING_APPROVAL' THEN '1'
    WHEN status = 'Approved' OR status = 'APPROVED' THEN '2'
    WHEN status = 'Rejected' OR status = 'REJECTED' THEN '3'
    WHEN status = 'Cancelled' OR status = 'CANCELLED' THEN '4'
    WHEN status = 'Voided' OR status = 'VOIDED' THEN '5'
    ELSE '0'  -- 默认为DRAFT
END
WHERE status IS NOT NULL;

-- 然后修改字段类型
ALTER TABLE Transaction 
MODIFY COLUMN status INT DEFAULT 0;

-- 修复 Transaction status 为 APPROVED (整数值 2)
UPDATE Transaction 
SET status = 2 
WHERE status IS NULL OR status = 0 OR status = '';

-- 验证修复结果
SELECT 'After Status Fix' as report,
       status,
       CASE 
           WHEN status = 0 THEN 'DRAFT'
           WHEN status = 1 THEN 'PENDING_APPROVAL'
           WHEN status = 2 THEN 'APPROVED'
           WHEN status = 3 THEN 'REJECTED'
           WHEN status = 4 THEN 'CANCELLED'
           WHEN status = 5 THEN 'VOIDED'
           ELSE 'UNKNOWN'
       END as status_name,
       COUNT(*) as count,
       transaction_type,
       SUM(amount) as total_amount
FROM Transaction 
GROUP BY status, transaction_type
ORDER BY status, transaction_type;

-- ====================
-- 2. 检查现有的 account_balance 数据
-- ====================

-- 查看现有数据
SELECT COUNT(*) as existing_balance_records FROM account_balance;

-- 查看每个公司的账户余额情况
SELECT 
    a.company_id,
    a.account_type,
    COUNT(a.account_id) as total_accounts,
    COUNT(ab.account_id) as accounts_with_balance
FROM Account a
LEFT JOIN account_balance ab ON a.account_id = ab.account_id
WHERE a.is_active = TRUE
GROUP BY a.company_id, a.account_type
ORDER BY a.company_id, a.account_type;

-- ====================
-- 3. 清理和重新添加 account_balance 数据
-- ====================

-- 先删除现有的测试数据（如果有的话）
DELETE FROM account_balance WHERE account_id IN (
    SELECT account_id FROM Account WHERE company_id IN (1, 2, 3)
);

-- 为公司1添加账户余额数据（使用正确的字段名）
INSERT INTO account_balance (
    account_id, 
    as_of_date, 
    current_month, 
    previous_month, 
    last_year_end, 
    month, 
    year
) 
SELECT 
    a.account_id,
    '2024-03-31' as as_of_date,
    CASE 
        WHEN a.account_type = 'ASSET' THEN 
            CASE 
                WHEN a.name LIKE '%Cash%' OR a.name LIKE '%现金%' THEN 500000.00
                WHEN a.name LIKE '%Bank%' OR a.name LIKE '%银行%' THEN 800000.00
                WHEN a.name LIKE '%Receivable%' OR a.name LIKE '%应收%' THEN 200000.00
                WHEN a.name LIKE '%Inventory%' OR a.name LIKE '%存货%' THEN 150000.00
                WHEN a.name LIKE '%Equipment%' OR a.name LIKE '%设备%' THEN 300000.00
                WHEN a.name LIKE '%Building%' OR a.name LIKE '%建筑%' THEN 1000000.00
                WHEN a.name LIKE '%Fixed%' OR a.name LIKE '%固定%' THEN 250000.00
                ELSE 100000.00
            END
        WHEN a.account_type = 'LIABILITY' THEN
            CASE 
                WHEN a.name LIKE '%Payable%' OR a.name LIKE '%应付%' THEN 150000.00
                WHEN a.name LIKE '%Loan%' OR a.name LIKE '%贷款%' THEN 300000.00
                WHEN a.name LIKE '%Tax%' OR a.name LIKE '%税%' THEN 50000.00
                ELSE 80000.00
            END
        WHEN a.account_type = 'EQUITY' THEN
            CASE 
                WHEN a.name LIKE '%Capital%' OR a.name LIKE '%资本%' THEN 1000000.00
                WHEN a.name LIKE '%Retained%' OR a.name LIKE '%留存%' THEN 500000.00
                ELSE 200000.00
            END
        ELSE 50000.00
    END as current_month,
    CASE 
        WHEN a.account_type = 'ASSET' THEN 
            CASE 
                WHEN a.name LIKE '%Cash%' OR a.name LIKE '%现金%' THEN 450000.00
                WHEN a.name LIKE '%Bank%' OR a.name LIKE '%银行%' THEN 750000.00
                WHEN a.name LIKE '%Receivable%' OR a.name LIKE '%应收%' THEN 180000.00
                WHEN a.name LIKE '%Inventory%' OR a.name LIKE '%存货%' THEN 130000.00
                WHEN a.name LIKE '%Equipment%' OR a.name LIKE '%设备%' THEN 280000.00
                WHEN a.name LIKE '%Building%' OR a.name LIKE '%建筑%' THEN 950000.00
                WHEN a.name LIKE '%Fixed%' OR a.name LIKE '%固定%' THEN 230000.00
                ELSE 90000.00
            END
        WHEN a.account_type = 'LIABILITY' THEN
            CASE 
                WHEN a.name LIKE '%Payable%' OR a.name LIKE '%应付%' THEN 130000.00
                WHEN a.name LIKE '%Loan%' OR a.name LIKE '%贷款%' THEN 280000.00
                WHEN a.name LIKE '%Tax%' OR a.name LIKE '%税%' THEN 45000.00
                ELSE 70000.00
            END
        WHEN a.account_type = 'EQUITY' THEN
            CASE 
                WHEN a.name LIKE '%Capital%' OR a.name LIKE '%资本%' THEN 950000.00
                WHEN a.name LIKE '%Retained%' OR a.name LIKE '%留存%' THEN 450000.00
                ELSE 180000.00
            END
        ELSE 45000.00
    END as previous_month,
    CASE 
        WHEN a.account_type = 'ASSET' THEN 
            CASE 
                WHEN a.name LIKE '%Cash%' OR a.name LIKE '%现金%' THEN 400000.00
                WHEN a.name LIKE '%Bank%' OR a.name LIKE '%银行%' THEN 700000.00
                WHEN a.name LIKE '%Receivable%' OR a.name LIKE '%应收%' THEN 160000.00
                WHEN a.name LIKE '%Inventory%' OR a.name LIKE '%存货%' THEN 110000.00
                WHEN a.name LIKE '%Equipment%' OR a.name LIKE '%设备%' THEN 260000.00
                WHEN a.name LIKE '%Building%' OR a.name LIKE '%建筑%' THEN 900000.00
                WHEN a.name LIKE '%Fixed%' OR a.name LIKE '%固定%' THEN 200000.00
                ELSE 80000.00
            END
        WHEN a.account_type = 'LIABILITY' THEN
            CASE 
                WHEN a.name LIKE '%Payable%' OR a.name LIKE '%应付%' THEN 110000.00
                WHEN a.name LIKE '%Loan%' OR a.name LIKE '%贷款%' THEN 250000.00
                WHEN a.name LIKE '%Tax%' OR a.name LIKE '%税%' THEN 40000.00
                ELSE 60000.00
            END
        WHEN a.account_type = 'EQUITY' THEN
            CASE 
                WHEN a.name LIKE '%Capital%' OR a.name LIKE '%资本%' THEN 800000.00
                WHEN a.name LIKE '%Retained%' OR a.name LIKE '%留存%' THEN 400000.00
                ELSE 150000.00
            END
        ELSE 40000.00
    END as last_year_end,
    3 as month,  -- March
    2024 as year
FROM Account a 
WHERE a.company_id = 1 
AND a.is_active = TRUE;

-- 为公司2添加数据（规模稍小）
INSERT INTO account_balance (
    account_id, 
    as_of_date, 
    current_month, 
    previous_month, 
    last_year_end, 
    month, 
    year
) 
SELECT 
    a.account_id,
    '2024-03-31' as as_of_date,
    ab.current_month * 0.8,
    ab.previous_month * 0.8,
    ab.last_year_end * 0.8,
    3 as month,
    2024 as year
FROM Account a 
JOIN Account a1 ON a1.company_id = 1 AND a1.account_code = a.account_code
JOIN account_balance ab ON ab.account_id = a1.account_id
WHERE a.company_id = 2 
AND a.is_active = TRUE;

-- 为公司3添加数据（规模更小）
INSERT INTO account_balance (
    account_id, 
    as_of_date, 
    current_month, 
    previous_month, 
    last_year_end, 
    month, 
    year
) 
SELECT 
    a.account_id,
    '2024-03-31' as as_of_date,
    ab.current_month * 0.6,
    ab.previous_month * 0.6,
    ab.last_year_end * 0.6,
    3 as month,
    2024 as year
FROM Account a 
JOIN Account a1 ON a1.company_id = 1 AND a1.account_code = a.account_code
JOIN account_balance ab ON ab.account_id = a1.account_id
WHERE a.company_id = 3 
AND a.is_active = TRUE;

-- ====================
-- 4. 最终验证
-- ====================

-- 验证交易数据
SELECT 'FINAL: Transaction Data' as report,
       t.company_id,
       t.status,
       CASE 
           WHEN t.status = 0 THEN 'DRAFT'
           WHEN t.status = 1 THEN 'PENDING_APPROVAL'
           WHEN t.status = 2 THEN 'APPROVED'
           WHEN t.status = 3 THEN 'REJECTED'
           WHEN t.status = 4 THEN 'CANCELLED'
           WHEN t.status = 5 THEN 'VOIDED'
           ELSE 'UNKNOWN'
       END as status_name,
       t.transaction_type,
       COUNT(*) as transaction_count,
       SUM(t.amount) as total_amount
FROM Transaction t
WHERE t.company_id = 1
AND t.transaction_date BETWEEN '2024-01-01' AND '2024-03-31'
GROUP BY t.company_id, t.status, t.transaction_type
ORDER BY t.status, t.transaction_type;

-- 验证账户余额数据
SELECT 'FINAL: Balance Sheet Data' as report,
       a.company_id,
       a.account_type,
       COUNT(ab.account_id) as accounts_with_balance,
       SUM(ab.current_month) as total_current_month
FROM Account a
JOIN account_balance ab ON a.account_id = ab.account_id
WHERE a.company_id = 1 AND a.is_active = TRUE
GROUP BY a.company_id, a.account_type
ORDER BY a.account_type;

-- 检查资产负债平衡
SELECT 'BALANCE SHEET CHECK' as report,
       SUM(CASE WHEN a.account_type = 'ASSET' THEN ab.current_month ELSE 0 END) as total_assets,
       SUM(CASE WHEN a.account_type = 'LIABILITY' THEN ab.current_month ELSE 0 END) as total_liabilities,
       SUM(CASE WHEN a.account_type = 'EQUITY' THEN ab.current_month ELSE 0 END) as total_equity,
       (SUM(CASE WHEN a.account_type = 'LIABILITY' THEN ab.current_month ELSE 0 END) + 
        SUM(CASE WHEN a.account_type = 'EQUITY' THEN ab.current_month ELSE 0 END)) as liabilities_plus_equity,
       (SUM(CASE WHEN a.account_type = 'ASSET' THEN ab.current_month ELSE 0 END) - 
        (SUM(CASE WHEN a.account_type = 'LIABILITY' THEN ab.current_month ELSE 0 END) + 
         SUM(CASE WHEN a.account_type = 'EQUITY' THEN ab.current_month ELSE 0 END))) as difference
FROM Account a
JOIN account_balance ab ON a.account_id = ab.account_id
WHERE a.company_id = 1 AND a.is_active = TRUE;

-- 验证分类统计数据
SELECT 'CATEGORY GROUPING CHECK' as report,
       c.name as category_name,
       c.type as category_type,
       COUNT(t.transaction_id) as transaction_count,
       COALESCE(SUM(t.amount), 0) as total_amount
FROM Category c
LEFT JOIN Transaction t ON c.category_id = t.category_id 
    AND t.status = 2  -- APPROVED
    AND t.transaction_date BETWEEN '2024-01-01' AND '2024-03-31'
WHERE c.company_id = 1
GROUP BY c.category_id, c.name, c.type
ORDER BY c.type, total_amount DESC;