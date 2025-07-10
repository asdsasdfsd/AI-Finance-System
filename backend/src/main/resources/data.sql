-- backend/src/main/resources/data.sql
-- 渐进式完整版本 - 基于极简版扩展

-- ====================
-- 1. 清理现有数据
-- ====================

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM account_balance;
DELETE FROM Transaction;
DELETE FROM User_Role;
DELETE FROM Category;
DELETE FROM Account;
DELETE FROM Department;
DELETE FROM User;
DELETE FROM Role;
DELETE FROM Company;

ALTER TABLE Company AUTO_INCREMENT = 1;
ALTER TABLE User AUTO_INCREMENT = 1;
ALTER TABLE Role AUTO_INCREMENT = 1;
ALTER TABLE Department AUTO_INCREMENT = 1;
ALTER TABLE Account AUTO_INCREMENT = 1;
ALTER TABLE Category AUTO_INCREMENT = 1;
ALTER TABLE Transaction AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ====================
-- 2. 插入角色
-- ====================

INSERT INTO Role (name, description) VALUES
('SYSTEM_ADMIN', 'System administrator with full access'),
('COMPANY_ADMIN', 'Company administrator'),
('FINANCE_MANAGER', 'Finance manager'),
('FINANCE_OPERATOR', 'Finance operator'),
('USER', 'Regular user');

-- ====================
-- 3. 插入公司（添加更多字段）
-- ====================

INSERT INTO Company (
    company_name, address, city, state_province, postal_code, 
    email, website, registration_number, tax_id, fiscal_year_start, 
    default_currency, max_users, status, created_at, updated_at
) VALUES
('Tech Innovation Ltd', '123 Innovation Street', 'Shanghai', 'Shanghai', '200000', 
 'contact@techinnovation.com', 'https://techinnovation.com', 'REG-TI-001', 'TAX-TI-001', '01-01', 
 'CNY', 100, 'ACTIVE', NOW(), NOW()),

('Green Energy Corp', '456 Green Avenue', 'Beijing', 'Beijing', '100000', 
 'info@greenenergy.com', 'https://greenenergy.com', 'REG-GE-002', 'TAX-GE-002', '01-01', 
 'CNY', 50, 'ACTIVE', NOW(), NOW()),

('Finance Solutions Inc', '789 Finance Road', 'Shenzhen', 'Guangdong', '518000', 
 'admin@financesolutions.com', 'https://financesolutions.com', 'REG-FS-003', 'TAX-FS-003', '01-01', 
 'CNY', 30, 'ACTIVE', NOW(), NOW());

-- ====================
-- 4. 插入部门
-- ====================

INSERT INTO Department (
    company_id, name, code, budget, is_active, created_at, updated_at
) VALUES
-- 公司1部门
(1, 'Finance Department', 'FIN', 500000.00, TRUE, NOW(), NOW()),
(1, 'IT Department', 'IT', 800000.00, TRUE, NOW(), NOW()),
(1, 'Sales Department', 'SALES', 600000.00, TRUE, NOW(), NOW()),
(1, 'Marketing Department', 'MKT', 400000.00, TRUE, NOW(), NOW()),

-- 公司2部门
(2, 'Finance Department', 'FIN', 300000.00, TRUE, NOW(), NOW()),
(2, 'Engineering Department', 'ENG', 700000.00, TRUE, NOW(), NOW()),
(2, 'Sales Department', 'SALES', 400000.00, TRUE, NOW(), NOW()),

-- 公司3部门
(3, 'Finance Department', 'FIN', 400000.00, TRUE, NOW(), NOW()),
(3, 'Consulting Department', 'CONS', 600000.00, TRUE, NOW(), NOW()),
(3, 'Technology Department', 'TECH', 350000.00, TRUE, NOW(), NOW());

-- ====================
-- 5. 插入用户（添加更多字段和用户）
-- ====================

INSERT INTO User (
    username, email, password, full_name, enabled, 
    company_id, department_id, preferred_language, timezone,
    created_at, updated_at
) VALUES
-- 公司1用户 (正确的password123哈希)
('admin', 'admin@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'System Administrator', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

('finance.manager', 'finance.manager@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Finance Manager', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

('john.doe', 'john.doe@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'John Doe - IT Specialist', TRUE, 1, 2, 'en-US', 'Asia/Shanghai', NOW(), NOW()),

('jane.smith', 'jane.smith@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Jane Smith - Sales Manager', TRUE, 1, 3, 'en-US', 'Asia/Shanghai', NOW(), NOW()),

-- 公司2用户
('green.admin', 'admin@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Green Energy Administrator', TRUE, 2, 5, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

('green.finance', 'finance@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Green Finance Manager', TRUE, 2, 5, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

('green.engineer', 'engineer@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Green Energy Engineer', TRUE, 2, 6, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

-- 公司3用户
('finance.admin', 'admin@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Finance Solutions Administrator', TRUE, 3, 8, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

('consultant', 'consultant@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Senior Financial Consultant', TRUE, 3, 9, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),

('tech.support', 'tech@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Technology Support Specialist', TRUE, 3, 10, 'zh-CN', 'Asia/Shanghai', NOW(), NOW());

-- ====================
-- 6. 分配用户角色
-- ====================

INSERT INTO User_Role (user_id, role_id) VALUES
-- 公司1用户角色
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), -- admin: 所有角色
(2, 3), (2, 4), (2, 5), -- finance.manager: 财务角色
(3, 5), -- john.doe: 普通用户
(4, 5), -- jane.smith: 普通用户

-- 公司2用户角色
(5, 2), (5, 3), (5, 4), (5, 5), -- green.admin: 公司管理员角色
(6, 3), (6, 4), (6, 5), -- green.finance: 财务角色
(7, 5), -- green.engineer: 普通用户

-- 公司3用户角色
(8, 2), (8, 3), (8, 4), (8, 5), -- finance.admin: 公司管理员角色
(9, 3), (9, 4), (9, 5), -- consultant: 财务角色
(10, 5); -- tech.support: 普通用户

-- ====================
-- 7. 插入会计科目
-- ====================

INSERT INTO Account (
    company_id, account_code, name, account_type, balance_direction, 
    is_active, created_at, updated_at
) VALUES
-- 公司1科目
(1, '1001', '库存现金', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1002', '银行存款', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1101', '应收账款', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '2001', '应付账款', 'LIABILITY', 'CREDIT', TRUE, NOW(), NOW()),
(1, '3001', '实收资本', 'EQUITY', 'CREDIT', TRUE, NOW(), NOW()),
(1, '3201', '未分配利润', 'EQUITY', 'CREDIT', TRUE, NOW(), NOW()),
(1, '4001', '主营业务收入', 'REVENUE', 'CREDIT', TRUE, NOW(), NOW()),
(1, '5001', '主营业务成本', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),
(1, '5201', '管理费用', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),

-- 公司2科目
(2, '1001', '库存现金', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(2, '1002', '银行存款', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(2, '2001', '应付账款', 'LIABILITY', 'CREDIT', TRUE, NOW(), NOW()),
(2, '3001', '实收资本', 'EQUITY', 'CREDIT', TRUE, NOW(), NOW()),
(2, '4001', '主营业务收入', 'REVENUE', 'CREDIT', TRUE, NOW(), NOW()),
(2, '5201', '管理费用', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),

-- 公司3科目
(3, '1001', '库存现金', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(3, '1002', '银行存款', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(3, '3001', '实收资本', 'EQUITY', 'CREDIT', TRUE, NOW(), NOW()),
(3, '4001', '咨询服务收入', 'REVENUE', 'CREDIT', TRUE, NOW(), NOW()),
(3, '5201', '管理费用', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW());

-- ====================
-- 8. 插入分类（基于成功的极简版本）
-- ====================

INSERT INTO Category (company_id, name, type, is_active, created_at, updated_at) VALUES
-- 公司1分类
(1, '销售收入', 'INCOME', TRUE, NOW(), NOW()),
(1, '服务收入', 'INCOME', TRUE, NOW(), NOW()),
(1, '办公费用', 'EXPENSE', TRUE, NOW(), NOW()),
(1, '差旅费', 'EXPENSE', TRUE, NOW(), NOW()),
(1, '市场推广', 'EXPENSE', TRUE, NOW(), NOW()),

-- 公司2分类
(2, '项目收入', 'INCOME', TRUE, NOW(), NOW()),
(2, '咨询收入', 'INCOME', TRUE, NOW(), NOW()),
(2, '设备采购', 'EXPENSE', TRUE, NOW(), NOW()),
(2, '运营费用', 'EXPENSE', TRUE, NOW(), NOW()),

-- 公司3分类
(3, '咨询服务', 'INCOME', TRUE, NOW(), NOW()),
(3, '培训收入', 'INCOME', TRUE, NOW(), NOW()),
(3, '软件费用', 'EXPENSE', TRUE, NOW(), NOW()),
(3, '办公租金', 'EXPENSE', TRUE, NOW(), NOW());

-- ====================
-- 9. 插入交易数据（基于实际Transaction表结构）
-- ====================

INSERT INTO Transaction (
    company_id, user_id, amount, currency, transaction_date, 
    description, payment_method, reference_number, is_recurring, is_taxable, 
    transaction_type, status, category_id, department_id, fund_id,
    created_at, updated_at
) VALUES
-- 公司1交易
(1, 1, 150000.00, 'CNY', '2024-07-01', 'Q2软件销售收入', '银行转账', 'TI-INC-2024-001', FALSE, TRUE, 'INCOME', 2, 1, 1, NULL, NOW(), NOW()),
(1, 2, 85000.00, 'CNY', '2024-07-05', '技术服务费收入', '银行转账', 'TI-INC-2024-002', FALSE, TRUE, 'INCOME', 2, 2, 1, NULL, NOW(), NOW()),
(1, 1, 25000.00, 'CNY', '2024-07-03', '办公用品采购', '现金', 'TI-EXP-2024-001', FALSE, TRUE, 'EXPENSE', 2, 3, 1, NULL, NOW(), NOW()),
(1, 4, 18000.00, 'CNY', '2024-07-07', '市场推广费用', '银行转账', 'TI-EXP-2024-002', FALSE, TRUE, 'EXPENSE', 2, 5, 4, NULL, NOW(), NOW()),
(1, 3, 12000.00, 'CNY', '2024-07-08', 'IT设备维护费', '银行转账', 'TI-EXP-2024-003', FALSE, TRUE, 'EXPENSE', 2, 3, 2, NULL, NOW(), NOW()),

-- 公司2交易
(2, 5, 200000.00, 'CNY', '2024-07-02', '太阳能项目收入', '银行转账', 'GE-INC-2024-001', FALSE, TRUE, 'INCOME', 2, 6, 5, NULL, NOW(), NOW()),
(2, 6, 120000.00, 'CNY', '2024-07-06', '新能源咨询收入', '银行转账', 'GE-INC-2024-002', FALSE, TRUE, 'INCOME', 2, 7, 5, NULL, NOW(), NOW()),
(2, 6, 75000.00, 'CNY', '2024-07-04', '太阳能设备采购', '银行转账', 'GE-EXP-2024-001', FALSE, TRUE, 'EXPENSE', 2, 8, 6, NULL, NOW(), NOW()),
(2, 7, 15000.00, 'CNY', '2024-07-08', '项目运营费用', '现金', 'GE-EXP-2024-002', FALSE, TRUE, 'EXPENSE', 2, 9, 6, NULL, NOW(), NOW()),

-- 公司3交易
(3, 8, 180000.00, 'CNY', '2024-07-01', '财务咨询收入', '银行转账', 'FS-INC-2024-001', FALSE, TRUE, 'INCOME', 2, 10, 8, NULL, NOW(), NOW()),
(3, 9, 95000.00, 'CNY', '2024-07-05', '财务培训收入', '银行转账', 'FS-INC-2024-002', FALSE, TRUE, 'INCOME', 2, 11, 9, NULL, NOW(), NOW()),
(3, 8, 28000.00, 'CNY', '2024-07-03', '财务软件费', '银行转账', 'FS-EXP-2024-001', FALSE, TRUE, 'EXPENSE', 2, 12, 10, NULL, NOW(), NOW()),
(3, 10, 12000.00, 'CNY', '2024-07-07', '办公室租金', '银行转账', 'FS-EXP-2024-002', FALSE, TRUE, 'EXPENSE', 2, 13, 8, NULL, NOW(), NOW());

-- ====================
-- 10. 插入账户余额数据
-- ====================

INSERT INTO account_balance (
    account_id, as_of_date, current_month, previous_month, 
    last_year_end, month, year
) VALUES
-- 公司1余额
(1, '2024-07-31', 125000.00, 98000.00, 85000.00, 7, 2024),
(2, '2024-07-31', 850000.00, 720000.00, 650000.00, 7, 2024),
(3, '2024-07-31', 180000.00, 165000.00, 145000.00, 7, 2024),
(4, '2024-07-31', 95000.00, 110000.00, 85000.00, 7, 2024),
(5, '2024-07-31', 1000000.00, 1000000.00, 1000000.00, 7, 2024),
(6, '2024-07-31', 580000.00, 368000.00, 265000.00, 7, 2024),

-- 公司2余额
(10, '2024-07-31', 95000.00, 75000.00, 65000.00, 7, 2024),
(11, '2024-07-31', 680000.00, 580000.00, 520000.00, 7, 2024),
(12, '2024-07-31', 85000.00, 95000.00, 75000.00, 7, 2024),
(13, '2024-07-31', 800000.00, 800000.00, 800000.00, 7, 2024),

-- 公司3余额
(16, '2024-07-31', 85000.00, 68000.00, 58000.00, 7, 2024),
(17, '2024-07-31', 520000.00, 450000.00, 385000.00, 7, 2024),
(18, '2024-07-31', 600000.00, 600000.00, 600000.00, 7, 2024);

-- ====================
-- 11. 验证和显示信息
-- ====================

SELECT '================================================' as info
UNION ALL SELECT 'JWT AUTHENTICATION DATABASE READY!'
UNION ALL SELECT '================================================'
UNION ALL SELECT ''
UNION ALL SELECT 'TEST USER CREDENTIALS (Password: password123):'
UNION ALL SELECT '================================================'
UNION ALL SELECT 'Company 1 - Tech Innovation Ltd:'
UNION ALL SELECT '  admin              | SYSTEM_ADMIN'
UNION ALL SELECT '  finance.manager    | FINANCE_MANAGER'
UNION ALL SELECT '  john.doe           | USER'
UNION ALL SELECT '  jane.smith         | USER'
UNION ALL SELECT ''
UNION ALL SELECT 'Company 2 - Green Energy Corp:'
UNION ALL SELECT '  green.admin        | COMPANY_ADMIN'
UNION ALL SELECT '  green.finance      | FINANCE_MANAGER'
UNION ALL SELECT '  green.engineer     | USER'
UNION ALL SELECT ''
UNION ALL SELECT 'Company 3 - Finance Solutions Inc:'
UNION ALL SELECT '  finance.admin      | COMPANY_ADMIN'
UNION ALL SELECT '  consultant         | FINANCE_MANAGER'
UNION ALL SELECT '  tech.support       | USER'
UNION ALL SELECT ''
UNION ALL SELECT 'Data Summary:'
UNION ALL SELECT '- 3 Companies with full data isolation'
UNION ALL SELECT '- 10 Users with JWT-ready company associations'
UNION ALL SELECT '- Complete financial data (accounts, transactions, balances)'
UNION ALL SELECT '- Ready for JWT authentication implementation'
UNION ALL SELECT '================================================';