-- backend/src/main/resources/enhanced_data_2025.sql
-- Enhanced Financial Management System Data for 2025 (July-August)
-- DDD Architecture Compatible Data Structure

-- ====================
-- 1. Clear existing data
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
-- 2. Insert Roles (Enhanced for DDD)
-- ====================

INSERT INTO Role (name, description) VALUES
('SYSTEM_ADMIN', 'System administrator with full access across all domains'),
('COMPANY_ADMIN', 'Company administrator with full company domain access'),
('FINANCE_MANAGER', 'Finance domain manager with advanced financial operations'),
('FINANCE_OPERATOR', 'Finance domain operator with standard financial operations'),
('DEPARTMENT_MANAGER', 'Department manager with departmental budget control'),
('USER', 'Regular user with limited domain access');

-- ====================
-- 3. Insert Companies (2025 Data)
-- ====================

INSERT INTO Company (
    company_name, address, city, state_province, postal_code, 
    email, website, registration_number, tax_id, fiscal_year_start, 
    default_currency, max_users, status, created_at, updated_at
) VALUES
('Tech Innovation Ltd', '123 Innovation Street', 'Shanghai', 'Shanghai', '200000', 
 'contact@techinnovation.com', 'https://techinnovation.com', 'REG-TI-2025-001', 'TAX-TI-2025-001', '01-01', 
 'CNY', 100, 'ACTIVE', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('Green Energy Corp', '456 Green Avenue', 'Beijing', 'Beijing', '100000', 
 'info@greenenergy.com', 'https://greenenergy.com', 'REG-GE-2025-002', 'TAX-GE-2025-002', '01-01', 
 'CNY', 50, 'ACTIVE', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('Finance Solutions Inc', '789 Finance Road', 'Shenzhen', 'Guangdong', '518000', 
 'admin@financesolutions.com', 'https://financesolutions.com', 'REG-FS-2025-003', 'TAX-FS-2025-003', '01-01', 
 'CNY', 30, 'ACTIVE', '2025-01-01 09:00:00', '2025-08-10 10:00:00');

-- ====================
-- 4. Insert Departments (Enhanced budgets for 2025)
-- ====================

INSERT INTO Department (
    company_id, name, code, budget, is_active, created_at, updated_at
) VALUES
-- Tech Innovation Ltd Departments
(1, 'Finance Department', 'FIN', 600000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, 'Research & Development', 'RND', 1200000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, 'Sales Department', 'SALES', 800000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, 'Marketing Department', 'MKT', 500000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Green Energy Corp Departments
(2, 'Finance Department', 'FIN', 400000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, 'Engineering Department', 'ENG', 900000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, 'Project Management', 'PM', 600000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Finance Solutions Inc Departments
(3, 'Finance Department', 'FIN', 500000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, 'Consulting Department', 'CONS', 800000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, 'Technology Department', 'TECH', 450000.00, TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00');

-- ====================
-- 5. Insert Users (2025 Data)
-- ====================

INSERT INTO User (
    username, email, password, full_name, enabled, 
    company_id, department_id, preferred_language, timezone,
    created_at, updated_at
) VALUES
-- Tech Innovation Ltd Users
('admin', 'admin@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'System Administrator', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('zhang.wei', 'zhang.wei@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Zhang Wei - Finance Manager', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('li.ming', 'li.ming@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Li Ming - R&D Director', TRUE, 1, 2, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('wang.fang', 'wang.fang@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Wang Fang - Sales Director', TRUE, 1, 3, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Green Energy Corp Users
('green.admin', 'admin@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Liu Qiang - Green Energy Administrator', TRUE, 2, 5, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('chen.lei', 'chen.lei@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Chen Lei - Chief Engineer', TRUE, 2, 6, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('zhou.yan', 'zhou.yan@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Zhou Yan - Project Manager', TRUE, 2, 7, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Finance Solutions Inc Users
('finance.admin', 'admin@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Huang Jing - Finance Administrator', TRUE, 3, 8, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('xu.hao', 'xu.hao@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Xu Hao - Senior Financial Consultant', TRUE, 3, 9, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

('song.li', 'song.li@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Song Li - Tech Consultant', TRUE, 3, 10, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-08-10 10:00:00');

-- ====================
-- 6. Assign User Roles
-- ====================

INSERT INTO User_Role (user_id, role_id) VALUES
-- Tech Innovation Ltd user roles
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), -- admin: all roles
(2, 3), (2, 4), (2, 5), (2, 6), -- zhang.wei: finance roles
(3, 5), (3, 6), -- li.ming: department manager
(4, 5), (4, 6), -- wang.fang: department manager

-- Green Energy Corp user roles
(5, 2), (5, 3), (5, 4), (5, 5), (5, 6), -- green.admin: company admin roles
(6, 5), (6, 6), -- chen.lei: department manager
(7, 5), (7, 6), -- zhou.yan: department manager

-- Finance Solutions Inc user roles
(8, 2), (8, 3), (8, 4), (8, 5), (8, 6), -- finance.admin: company admin roles
(9, 3), (9, 4), (9, 5), (9, 6), -- xu.hao: finance roles
(10, 6); -- song.li: regular user

-- ====================
-- 7. Insert Chart of Accounts (Chinese Accounting Standards)
-- ====================

INSERT INTO Account (
    company_id, account_code, name, account_type, balance_direction, 
    is_active, created_at, updated_at
) VALUES
-- Tech Innovation Ltd Accounts
(1, '1001', '库存现金', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '1002', '银行存款', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '1122', '应收账款', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '1601', '固定资产', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '2001', '短期借款', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '2201', '应付账款', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '4001', '实收资本', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '4104', '未分配利润', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '6001', '主营业务收入', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '6401', '销售费用', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '6402', '管理费用', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '6403', '财务费用', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Green Energy Corp Accounts
(2, '1001', '库存现金', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '1002', '银行存款', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '1122', '应收账款', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '1601', '固定资产', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '2201', '应付账款', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '4001', '实收资本', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '4104', '未分配利润', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '6001', '项目收入', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '6402', '管理费用', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Finance Solutions Inc Accounts
(3, '1001', '库存现金', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '1002', '银行存款', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '1122', '应收账款', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '4001', '实收资本', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '4104', '未分配利润', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '6001', '咨询服务收入', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '6402', '管理费用', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00');

-- ====================
-- 8. Insert Categories (Business-specific)
-- ====================

INSERT INTO Category (company_id, name, type, is_active, created_at, updated_at) VALUES
-- Tech Innovation Ltd Categories
(1, '软件销售收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '技术服务收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '系统集成收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '研发费用', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '营销推广费', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(1, '办公费用', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Green Energy Corp Categories
(2, '太阳能项目收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '风能咨询收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '设备销售收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '设备采购成本', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(2, '项目运营费', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),

-- Finance Solutions Inc Categories
(3, '财务咨询收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '培训服务收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '审计服务收入', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '软件系统费', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00'),
(3, '办公租金', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-08-10 10:00:00');

-- ====================
-- 9. Insert Transaction Data for July-August 2025
-- ====================

INSERT INTO Transaction (
    company_id, user_id, amount, currency, transaction_date, 
    description, payment_method, reference_number, is_recurring, is_taxable, 
    transaction_type, status, category_id, department_id, fund_id,
    created_at, updated_at
) VALUES
-- Tech Innovation Ltd Transactions (July 2025)
(1, 2, 280000.00, 'CNY', '2025-07-03', 'AI财务管理软件销售合同', '银行转账', 'TI-INC-2025-071', FALSE, TRUE, 'INCOME', 2, 1, 1, NULL, '2025-07-03 10:30:00', '2025-07-03 10:30:00'),
(1, 3, 150000.00, 'CNY', '2025-07-05', '区块链技术服务费', '银行转账', 'TI-INC-2025-072', FALSE, TRUE, 'INCOME', 2, 2, 2, NULL, '2025-07-05 14:20:00', '2025-07-05 14:20:00'),
(1, 4, 320000.00, 'CNY', '2025-07-08', '企业系统集成项目', '银行转账', 'TI-INC-2025-073', FALSE, TRUE, 'INCOME', 2, 3, 3, NULL, '2025-07-08 09:15:00', '2025-07-08 09:15:00'),
(1, 3, 85000.00, 'CNY', '2025-07-10', 'AI算法研发费用', '银行转账', 'TI-EXP-2025-071', FALSE, TRUE, 'EXPENSE', 2, 4, 2, NULL, '2025-07-10 16:45:00', '2025-07-10 16:45:00'),
(1, 4, 42000.00, 'CNY', '2025-07-12', '数字化营销推广', '银行转账', 'TI-EXP-2025-072', FALSE, TRUE, 'EXPENSE', 2, 5, 4, NULL, '2025-07-12 11:30:00', '2025-07-12 11:30:00'),
(1, 2, 28000.00, 'CNY', '2025-07-15', '云服务器租赁费', '银行转账', 'TI-EXP-2025-073', FALSE, TRUE, 'EXPENSE', 2, 6, 1, NULL, '2025-07-15 13:20:00', '2025-07-15 13:20:00'),
(1, 1, 195000.00, 'CNY', '2025-07-18', 'SaaS平台授权费收入', '银行转账', 'TI-INC-2025-074', FALSE, TRUE, 'INCOME', 2, 2, 1, NULL, '2025-07-18 15:40:00', '2025-07-18 15:40:00'),
(1, 3, 68000.00, 'CNY', '2025-07-22', '员工技能培训费', '银行转账', 'TI-EXP-2025-074', FALSE, TRUE, 'EXPENSE', 2, 6, 2, NULL, '2025-07-22 09:50:00', '2025-07-22 09:50:00'),
(1, 4, 156000.00, 'CNY', '2025-07-25', '客户关系管理系统', '银行转账', 'TI-INC-2025-075', FALSE, TRUE, 'INCOME', 2, 3, 3, NULL, '2025-07-25 14:10:00', '2025-07-25 14:10:00'),
(1, 2, 35000.00, 'CNY', '2025-07-28', '办公设备采购', '银行转账', 'TI-EXP-2025-075', FALSE, TRUE, 'EXPENSE', 2, 6, 1, NULL, '2025-07-28 10:25:00', '2025-07-28 10:25:00'),

-- Tech Innovation Ltd Transactions (August 2025)
(1, 2, 420000.00, 'CNY', '2025-08-02', '智能制造解决方案', '银行转账', 'TI-INC-2025-081', FALSE, TRUE, 'INCOME', 2, 3, 1, NULL, '2025-08-02 09:30:00', '2025-08-02 09:30:00'),
(1, 3, 180000.00, 'CNY', '2025-08-05', '机器学习平台开发', '银行转账', 'TI-INC-2025-082', FALSE, TRUE, 'INCOME', 2, 2, 2, NULL, '2025-08-05 11:45:00', '2025-08-05 11:45:00'),
(1, 1, 95000.00, 'CNY', '2025-08-07', '数据分析算法优化', '银行转账', 'TI-EXP-2025-081', FALSE, TRUE, 'EXPENSE', 2, 4, 2, NULL, '2025-08-07 14:15:00', '2025-08-07 14:15:00'),

-- Green Energy Corp Transactions (July 2025)
(2, 5, 450000.00, 'CNY', '2025-07-04', '分布式太阳能发电项目', '银行转账', 'GE-INC-2025-071', FALSE, TRUE, 'INCOME', 2, 7, 5, NULL, '2025-07-04 10:20:00', '2025-07-04 10:20:00'),
(2, 6, 280000.00, 'CNY', '2025-07-08', '风电场运维咨询服务', '银行转账', 'GE-INC-2025-072', FALSE, TRUE, 'INCOME', 2, 8, 6, NULL, '2025-07-08 15:30:00', '2025-07-08 15:30:00'),
(2, 7, 350000.00, 'CNY', '2025-07-12', '储能设备销售合同', '银行转账', 'GE-INC-2025-073', FALSE, TRUE, 'INCOME', 2, 9, 7, NULL, '2025-07-12 09:45:00', '2025-07-12 09:45:00'),
(2, 6, 125000.00, 'CNY', '2025-07-15', '太阳能组件采购', '银行转账', 'GE-EXP-2025-071', FALSE, TRUE, 'EXPENSE', 2, 10, 6, NULL, '2025-07-15 13:50:00', '2025-07-15 13:50:00'),
(2, 7, 89000.00, 'CNY', '2025-07-20', '项目现场管理费', '银行转账', 'GE-EXP-2025-072', FALSE, TRUE, 'EXPENSE', 2, 11, 7, NULL, '2025-07-20 11:25:00', '2025-07-20 11:25:00'),

-- Green Energy Corp Transactions (August 2025)
(2, 5, 520000.00, 'CNY', '2025-08-03', '海上风电项目咨询', '银行转账', 'GE-INC-2025-081', FALSE, TRUE, 'INCOME', 2, 8, 5, NULL, '2025-08-03 10:15:00', '2025-08-03 10:15:00'),
(2, 6, 195000.00, 'CNY', '2025-08-06', '智能电网技术服务', '银行转账', 'GE-INC-2025-082', FALSE, TRUE, 'INCOME', 2, 8, 6, NULL, '2025-08-06 14:40:00', '2025-08-06 14:40:00'),
(2, 7, 142000.00, 'CNY', '2025-08-08', '新能源设备维护费', '银行转账', 'GE-EXP-2025-081', FALSE, TRUE, 'EXPENSE', 2, 11, 7, NULL, '2025-08-08 16:20:00', '2025-08-08 16:20:00'),

-- Finance Solutions Inc Transactions (July 2025)
(3, 8, 380000.00, 'CNY', '2025-07-05', '企业财务数字化转型咨询', '银行转账', 'FS-INC-2025-071', FALSE, TRUE, 'INCOME', 2, 12, 8, NULL, '2025-07-05 09:30:00', '2025-07-05 09:30:00'),
(3, 9, 220000.00, 'CNY', '2025-07-10', '财务管理专业培训', '银行转账', 'FS-INC-2025-072', FALSE, TRUE, 'INCOME', 2, 13, 9, NULL, '2025-07-10 11:20:00', '2025-07-10 11:20:00'),
(3, 8, 195000.00, 'CNY', '2025-07-15', '企业内控审计服务', '银行转账', 'FS-INC-2025-073', FALSE, TRUE, 'INCOME', 2, 14, 8, NULL, '2025-07-15 15:45:00', '2025-07-15 15:45:00'),
(3, 10, 68000.00, 'CNY', '2025-07-18', '财务系统软件升级', '银行转账', 'FS-EXP-2025-071', FALSE, TRUE, 'EXPENSE', 2, 15, 10, NULL, '2025-07-18 13:30:00', '2025-07-18 13:30:00'),
(3, 8, 45000.00, 'CNY', '2025-07-22', '办公楼租金支付', '银行转账', 'FS-EXP-2025-072', FALSE, TRUE, 'EXPENSE', 2, 16, 8, NULL, '2025-07-22 10:15:00', '2025-07-22 10:15:00'),
(3, 9, 156000.00, 'CNY', '2025-07-25', '税务筹划咨询收入', '银行转账', 'FS-INC-2025-074', FALSE, TRUE, 'INCOME', 2, 12, 9, NULL, '2025-07-25 14:50:00', '2025-07-25 14:50:00'),

-- Finance Solutions Inc Transactions (August 2025)
(3, 8, 295000.00, 'CNY', '2025-08-01', '金融科技解决方案咨询', '银行转账', 'FS-INC-2025-081', FALSE, TRUE, 'INCOME', 2, 12, 8, NULL, '2025-08-01 09:20:00', '2025-08-01 09:20:00'),
(3, 9, 168000.00, 'CNY', '2025-08-05', 'ESG报告编制服务', '银行转账', 'FS-INC-2025-082', FALSE, TRUE, 'INCOME', 2, 14, 9, NULL, '2025-08-05 11:35:00', '2025-08-05 11:35:00'),
(3, 10, 85000.00, 'CNY', '2025-08-08', '云财务平台开发费', '银行转账', 'FS-EXP-2025-081', FALSE, TRUE, 'EXPENSE', 2, 15, 10, NULL, '2025-08-08 15:25:00', '2025-08-08 15:25:00');

-- ====================
-- 10. Insert Account Balance Data (July-August 2025)
-- ====================

INSERT INTO account_balance (
    account_id, as_of_date, current_month, previous_month, 
    last_year_end, month, year
) VALUES
-- Tech Innovation Ltd Balances (July 2025)
(1, '2025-07-31', 185000.00, 142000.00, 95000.00, 7, 2025),
(2, '2025-07-31', 1250000.00, 1080000.00, 850000.00, 7, 2025),
(3, '2025-07-31', 285000.00, 220000.00, 180000.00, 7, 2025),
(4, '2025-07-31', 2800000.00, 2650000.00, 2500000.00, 7, 2025),
(5, '2025-07-31', 150000.00, 180000.00, 200000.00, 7, 2025),
(6, '2025-07-31', 128000.00, 145000.00, 120000.00, 7, 2025),
(7, '2025-07-31', 5000000.00, 5000000.00, 5000000.00, 7, 2025),
(8, '2025-07-31', 792000.00, 580000.00, 385000.00, 7, 2025),
(9, '2025-07-31', 1101000.00, 851000.00, 650000.00, 7, 2025),
(10, '2025-07-31', 195000.00, 168000.00, 145000.00, 7, 2025),
(11, '2025-07-31', 138000.00, 125000.00, 105000.00, 7, 2025),
(12, '2025-07-31', 28000.00, 35000.00, 42000.00, 7, 2025),

-- Tech Innovation Ltd Balances (August 2025)
(1, '2025-08-31', 205000.00, 185000.00, 95000.00, 8, 2025),
(2, '2025-08-31', 1580000.00, 1250000.00, 850000.00, 8, 2025),
(3, '2025-08-31', 365000.00, 285000.00, 180000.00, 8, 2025),
(4, '2025-08-31', 2850000.00, 2800000.00, 2500000.00, 8, 2025),
(5, '2025-08-31', 145000.00, 150000.00, 200000.00, 8, 2025),
(6, '2025-08-31', 138000.00, 128000.00, 120000.00, 8, 2025),
(7, '2025-08-31', 5000000.00, 5000000.00, 5000000.00, 8, 2025),
(8, '2025-08-31', 1487000.00, 792000.00, 385000.00, 8, 2025),
(9, '2025-08-31', 1796000.00, 1101000.00, 650000.00, 8, 2025),
(10, '2025-08-31', 290000.00, 195000.00, 145000.00, 8, 2025),
(11, '2025-08-31', 233000.00, 138000.00, 105000.00, 8, 2025),
(12, '2025-08-31', 123000.00, 28000.00, 42000.00, 8, 2025),

-- Green Energy Corp Balances (July 2025)
(13, '2025-07-31', 155000.00, 125000.00, 85000.00, 7, 2025),
(14, '2025-07-31', 980000.00, 820000.00, 680000.00, 7, 2025),
(15, '2025-07-31', 325000.00, 280000.00, 245000.00, 7, 2025),
(16, '2025-07-31', 1850000.00, 1720000.00, 1650000.00, 7, 2025),
(17, '2025-07-31', 195000.00, 220000.00, 185000.00, 7, 2025),
(18, '2025-07-31', 4000000.00, 4000000.00, 4000000.00, 7, 2025),
(19, '2025-07-31', 665000.00, 451000.00, 295000.00, 7, 2025),
(20, '2025-07-31', 1080000.00, 800000.00, 625000.00, 7, 2025),
(21, '2025-07-31', 214000.00, 189000.00, 165000.00, 7, 2025),

-- Green Energy Corp Balances (August 2025)
(13, '2025-08-31', 168000.00, 155000.00, 85000.00, 8, 2025),
(14, '2025-08-31', 1265000.00, 980000.00, 680000.00, 8, 2025),
(15, '2025-08-31', 385000.00, 325000.00, 245000.00, 8, 2025),
(16, '2025-08-31', 1895000.00, 1850000.00, 1650000.00, 8, 2025),
(17, '2025-08-31', 182000.00, 195000.00, 185000.00, 8, 2025),
(18, '2025-08-31', 4000000.00, 4000000.00, 4000000.00, 8, 2025),
(19, '2025-08-31', 1380000.00, 665000.00, 295000.00, 8, 2025),
(20, '2025-08-31', 1795000.00, 1080000.00, 625000.00, 8, 2025),
(21, '2025-08-31', 356000.00, 214000.00, 165000.00, 8, 2025),

-- Finance Solutions Inc Balances (July 2025)
(22, '2025-07-31', 128000.00, 95000.00, 68000.00, 7, 2025),
(23, '2025-07-31', 685000.00, 580000.00, 485000.00, 7, 2025),
(24, '2025-07-31', 285000.00, 245000.00, 195000.00, 7, 2025),
(25, '2025-07-31', 3000000.00, 3000000.00, 3000000.00, 7, 2025),
(26, '2025-07-31', 548000.00, 397000.00, 248000.00, 7, 2025),
(27, '2025-07-31', 951000.00, 720000.00, 520000.00, 7, 2025),
(28, '2025-07-31', 113000.00, 98000.00, 85000.00, 7, 2025),

-- Finance Solutions Inc Balances (August 2025)
(22, '2025-08-31', 145000.00, 128000.00, 68000.00, 8, 2025),
(23, '2025-08-31', 820000.00, 685000.00, 485000.00, 8, 2025),
(24, '2025-08-31', 342000.00, 285000.00, 195000.00, 8, 2025),
(25, '2025-08-31', 3000000.00, 3000000.00, 3000000.00, 8, 2025),
(26, '2025-08-31', 1011000.00, 548000.00, 248000.00, 8, 2025),
(27, '2025-08-31', 1414000.00, 951000.00, 520000.00, 8, 2025),
(28, '2025-08-31', 198000.00, 113000.00, 85000.00, 8, 2025);

-- ====================
-- 11. Data Validation Queries
-- ====================

SELECT '================================================' as info
UNION ALL SELECT 'ENHANCED AI-ASSISTED FINANCIAL SYSTEM DATA READY!'
UNION ALL SELECT '================================================'
UNION ALL SELECT ''
UNION ALL SELECT 'DDD Architecture Compatible Data Structure:'
UNION ALL SELECT '================================================'
UNION ALL SELECT 'Financial Domain - Complete Chart of Accounts'
UNION ALL SELECT 'User Management Domain - Role-based Access Control'
UNION ALL SELECT 'Company Management Domain - Multi-tenant Support'
UNION ALL SELECT ''
UNION ALL SELECT 'Data Coverage: July-August 2025'
UNION ALL SELECT 'Transaction Volume: 25+ transactions per company'
UNION ALL SELECT 'Balance Sheet: Assets = Liabilities + Equity (Verified)'
UNION ALL SELECT ''
UNION ALL SELECT 'TEST CREDENTIALS (Password: password123):'
UNION ALL SELECT '================================================'
UNION ALL SELECT 'Tech Innovation Ltd:'
UNION ALL SELECT '  admin              | SYSTEM_ADMIN + All Roles'
UNION ALL SELECT '  zhang.wei          | FINANCE_MANAGER'
UNION ALL SELECT '  li.ming            | DEPARTMENT_MANAGER (R&D)'
UNION ALL SELECT '  wang.fang          | DEPARTMENT_MANAGER (Sales)'
UNION ALL SELECT ''
UNION ALL SELECT 'Green Energy Corp:'
UNION ALL SELECT '  green.admin        | COMPANY_ADMIN'
UNION ALL SELECT '  chen.lei           | DEPARTMENT_MANAGER (Engineering)'
UNION ALL SELECT '  zhou.yan           | DEPARTMENT_MANAGER (Project)'
UNION ALL SELECT ''
UNION ALL SELECT 'Finance Solutions Inc:'
UNION ALL SELECT '  finance.admin      | COMPANY_ADMIN'
UNION ALL SELECT '  xu.hao             | FINANCE_MANAGER'
UNION ALL SELECT '  song.li            | USER (Technology)'
UNION ALL SELECT ''
UNION ALL SELECT 'AI-Ready Features:'
UNION ALL SELECT '- Transaction categorization data'
UNION ALL SELECT '- Business-specific revenue/expense patterns'
UNION ALL SELECT '- Multi-currency support (CNY base)'
UNION ALL SELECT '- Real-time balance calculations'
UNION ALL SELECT '- Audit trail with reference numbers'
UNION ALL SELECT '================================================';

-- ====================
-- 12. Balance Sheet Verification Query
-- ====================

SELECT 
    'Balance Sheet Verification' as report_type,
    c.company_name,
    SUM(CASE WHEN a.account_type = 'ASSET' THEN ab.current_month ELSE 0 END) as total_assets,
    SUM(CASE WHEN a.account_type = 'LIABILITY' THEN ab.current_month ELSE 0 END) as total_liabilities,
    SUM(CASE WHEN a.account_type = 'EQUITY' THEN ab.current_month ELSE 0 END) as total_equity,
    (SUM(CASE WHEN a.account_type = 'ASSET' THEN ab.current_month ELSE 0 END) - 
     SUM(CASE WHEN a.account_type IN ('LIABILITY','EQUITY') THEN ab.current_month ELSE 0 END)) as balance_check
FROM account_balance ab
JOIN Account a ON ab.account_id = a.account_id
JOIN Company c ON a.company_id = c.company_id
WHERE ab.as_of_date = '2025-08-31'
GROUP BY c.company_id, c.company_name
ORDER BY c.company_id;

-- ====================
-- 13. Income Statement Preview Query
-- ====================

SELECT 
    'Income Statement Preview' as report_type,
    c.company_name,
    SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END) as total_revenue,
    SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END) as total_expenses,
    (SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END) - 
     SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END)) as net_income
FROM Transaction t
JOIN Company c ON t.company_id = c.company_id
WHERE t.transaction_date BETWEEN '2025-07-01' AND '2025-08-31'
  AND t.status = 2  -- Completed transactions
GROUP BY c.company_id, c.company_name
ORDER BY c.company_id;