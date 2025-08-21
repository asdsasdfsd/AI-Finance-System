-- backend/src/main/resources/enhanced_demo_data_2025.sql
-- Enhanced Financial Management System Demo Data for Screenshots
-- Focused on Tech Innovation Ltd (Company ID: 1) for Professional Demo
-- DDD Architecture Compatible - Extended Data Range: June-September 2025

-- ====================
-- 1. Clear existing data and reset
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
-- 2. Insert Roles (DDD Compatible)
-- ====================

INSERT INTO Role (name, description) VALUES
('SYSTEM_ADMIN', 'System administrator with full access across all domains'),
('COMPANY_ADMIN', 'Company administrator with full company domain access'),
('FINANCE_MANAGER', 'Finance domain manager with advanced financial operations'),
('FINANCE_OPERATOR', 'Finance domain operator with standard financial operations'),
('DEPARTMENT_MANAGER', 'Department manager with departmental budget control'),
('USER', 'Regular user with limited domain access');

-- ====================
-- 3. Insert Company (Primary Focus: Tech Innovation Ltd)
-- ====================

INSERT INTO Company (
    company_name, address, city, state_province, postal_code, 
    email, website, registration_number, tax_id, fiscal_year_start, 
    default_currency, max_users, status, created_at, updated_at
) VALUES
('Tech Innovation Ltd', '123 Innovation Street', 'Shanghai', 'Shanghai', '200000', 
 'contact@techinnovation.com', 'https://techinnovation.com', 'REG-TI-2025-001', 'TAX-TI-2025-001', '01-01', 
 'CNY', 100, 'ACTIVE', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('Green Energy Corp', '456 Green Avenue', 'Beijing', 'Beijing', '100000', 
 'info@greenenergy.com', 'https://greenenergy.com', 'REG-GE-2025-002', 'TAX-GE-2025-002', '01-01', 
 'CNY', 50, 'ACTIVE', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('Finance Solutions Inc', '789 Finance Road', 'Shenzhen', 'Guangdong', '518000', 
 'admin@financesolutions.com', 'https://financesolutions.com', 'REG-FS-2025-003', 'TAX-FS-2025-003', '01-01', 
 'CNY', 30, 'ACTIVE', '2025-01-01 09:00:00', '2025-09-15 10:00:00');

-- ====================
-- 4. Insert Departments (Tech Innovation Ltd - Enhanced)
-- ====================

INSERT INTO Department (
    company_id, name, code, budget, is_active, created_at, updated_at
) VALUES
-- Tech Innovation Ltd Departments (Enhanced for demo)
(1, 'Finance Department', 'FIN', 800000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, 'Research & Development', 'RND', 1500000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, 'Sales Department', 'SALES', 1200000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, 'Marketing Department', 'MKT', 600000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, 'Operations Department', 'OPS', 900000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, 'Human Resources', 'HR', 450000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- Other companies (minimal for context)
(2, 'Finance Department', 'FIN', 400000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, 'Engineering Department', 'ENG', 900000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, 'Finance Department', 'FIN', 500000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, 'Consulting Department', 'CONS', 800000.00, TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00');

-- ====================
-- 5. Insert Users (Focus on Tech Innovation Ltd)
-- ====================

INSERT INTO User (
    username, email, password, full_name, enabled, 
    company_id, department_id, preferred_language, timezone,
    created_at, updated_at
) VALUES
-- Tech Innovation Ltd Users (Enhanced)
('admin', 'admin@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'System Administrator', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('zhang.wei', 'zhang.wei@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Zhang Wei - CFO', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('li.ming', 'li.ming@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Li Ming - R&D Director', TRUE, 1, 2, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('wang.fang', 'wang.fang@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Wang Fang - Sales Director', TRUE, 1, 3, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('chen.hui', 'chen.hui@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Chen Hui - Marketing Manager', TRUE, 1, 4, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('liu.jun', 'liu.jun@techinnovation.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Liu Jun - Operations Manager', TRUE, 1, 5, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- Other companies (minimal)
('green.admin', 'admin@greenenergy.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Green Energy Admin', TRUE, 2, 7, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

('finance.admin', 'admin@financesolutions.com', 
 '$2a$10$RFGM1tzuVXmaoYSabdJnX.QMRdqDWkAbdiJVjTBOSv2jKW6goYnvW', 
 'Finance Solutions Admin', TRUE, 3, 9, 'zh-CN', 'Asia/Shanghai', '2025-01-01 09:00:00', '2025-09-15 10:00:00');

-- ====================
-- 6. Assign User Roles (Focus on Tech Innovation Ltd)
-- ====================

INSERT INTO User_Role (user_id, role_id) VALUES
-- Tech Innovation Ltd user roles
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), -- admin: all roles
(2, 2), (2, 3), (2, 4), (2, 5), (2, 6), -- zhang.wei: company admin + finance
(3, 5), (3, 6), -- li.ming: department manager
(4, 5), (4, 6), -- wang.fang: department manager  
(5, 5), (5, 6), -- chen.hui: department manager
(6, 5), (6, 6), -- liu.jun: department manager

-- Other companies (minimal)
(7, 2), (7, 3), (7, 4), (7, 5), (7, 6), -- green.admin
(8, 2), (8, 3), (8, 4), (8, 5), (8, 6); -- finance.admin

-- ====================
-- 7. Enhanced Chart of Accounts (Tech Innovation Ltd - Complete Balance Sheet Structure)
-- ====================

INSERT INTO Account (
    company_id, account_code, name, account_type, balance_direction, 
    is_active, created_at, updated_at
) VALUES
-- ASSETS - Current Assets
(1, '1001', 'Cash and Cash Equivalents', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1002', 'Bank Deposits - Operations', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1003', 'Bank Deposits - Savings', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1101', 'Accounts Receivable', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1102', 'Notes Receivable', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1201', 'Inventory - Raw Materials', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1202', 'Inventory - Finished Goods', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1301', 'Prepaid Expenses', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1302', 'Prepaid Insurance', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- ASSETS - Non-Current Assets  
(1, '1501', 'Property, Plant & Equipment', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1502', 'Accumulated Depreciation - PPE', 'ASSET', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1601', 'Intangible Assets', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1602', 'Accumulated Amortization - Intangible', 'ASSET', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1701', 'Investment Securities', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '1801', 'Other Long-term Assets', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- LIABILITIES - Current Liabilities
(1, '2001', 'Accounts Payable', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2002', 'Notes Payable', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2101', 'Short-term Loans', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2201', 'Accrued Liabilities', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2202', 'Accrued Payroll', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2301', 'Taxes Payable', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2302', 'VAT Payable', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2401', 'Deferred Revenue', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- LIABILITIES - Non-Current Liabilities
(1, '2501', 'Long-term Loans', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2601', 'Deferred Tax Liabilities', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '2701', 'Other Long-term Liabilities', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- EQUITY
(1, '3001', 'Paid-in Capital', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '3002', 'Additional Paid-in Capital', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '3101', 'Retained Earnings', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '3201', 'Current Year Earnings', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- REVENUE ACCOUNTS
(1, '4001', 'Software Licensing Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '4002', 'SaaS Subscription Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '4003', 'Professional Services Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '4004', 'Consulting Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '4005', 'Maintenance & Support Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '4101', 'Interest Income', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '4102', 'Other Income', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- EXPENSE ACCOUNTS
(1, '5001', 'Cost of Goods Sold', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5002', 'Direct Labor Costs', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5101', 'Research & Development', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5201', 'Sales & Marketing', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5202', 'Advertising & Promotion', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5301', 'General & Administrative', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5302', 'Office Rent', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5303', 'Utilities', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5304', 'Insurance', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5401', 'Salaries & Benefits', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5402', 'Employee Benefits', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5501', 'Depreciation Expense', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5502', 'Amortization Expense', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5601', 'Interest Expense', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5701', 'Professional Services', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5702', 'Legal & Accounting', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5801', 'Travel & Entertainment', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(1, '5901', 'Technology & Software', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- Minimal accounts for other companies
(2, '1001', 'Cash and Cash Equivalents', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, '1002', 'Bank Deposits', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, '2001', 'Accounts Payable', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, '3001', 'Paid-in Capital', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, '4001', 'Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, '5001', 'Expenses', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

(3, '1001', 'Cash and Cash Equivalents', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, '1002', 'Bank Deposits', 'ASSET', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, '2001', 'Accounts Payable', 'LIABILITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, '3001', 'Paid-in Capital', 'EQUITY', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, '4001', 'Revenue', 'REVENUE', 'CREDIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, '5001', 'Expenses', 'EXPENSE', 'DEBIT', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00');

-- ====================
-- 8. FIXED Enhanced Categories with Explicit IDs (Tech Innovation Ltd)
-- ====================

-- 先删除现有的Category数据
DELETE FROM Category WHERE company_id IN (1, 2, 3);

-- 重置自增ID
ALTER TABLE Category AUTO_INCREMENT = 1;

-- 使用明确的ID插入Categories
INSERT INTO Category (category_id, company_id, name, type, is_active, created_at, updated_at) VALUES
-- Tech Innovation Ltd Income Categories (ID 1-7)
(1, 1, 'Software Licensing', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(2, 1, 'SaaS Subscriptions', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(3, 1, 'Professional Services', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(4, 1, 'Consulting Services', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(5, 1, 'Maintenance & Support', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(6, 1, 'Training Services', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(7, 1, 'Investment Income', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- Tech Innovation Ltd Expense Categories (ID 8-15)
(8, 1, 'Research & Development', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(9, 1, 'Sales & Marketing', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(10, 1, 'General & Administrative', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(11, 1, 'Human Resources', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(12, 1, 'Operations', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(13, 1, 'Technology Infrastructure', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(14, 1, 'Professional Services', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(15, 1, 'Facilities & Operations', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),

-- Other companies categories (ID 16-19)
(16, 2, 'Project Revenue', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(17, 2, 'Project Expenses', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(18, 3, 'Consulting Revenue', 'INCOME', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00'),
(19, 3, 'Consulting Expenses', 'EXPENSE', TRUE, '2025-01-01 09:00:00', '2025-09-15 10:00:00');

-- 设置自增ID从20开始
ALTER TABLE Category AUTO_INCREMENT = 20;

-- ====================
-- 9. FIXED Enhanced Transaction Data (正确的category_id)
-- ====================

INSERT INTO Transaction (
    company_id, user_id, amount, currency, transaction_date, 
    description, payment_method, reference_number, is_recurring, is_taxable, 
    transaction_type, status, category_id, department_id, fund_id,
    created_at, updated_at
) VALUES

-- JUNE 2025 TRANSACTIONS
-- Revenue Transactions
(1, 2, 850000.00, 'CNY', '2025-06-03', 'Enterprise AI Platform - Annual License', 'Bank Transfer', 'TI-REV-2025-061', FALSE, TRUE, 'INCOME', 2, 1, 3, NULL, '2025-06-03 10:30:00', '2025-06-03 10:30:00'),
(1, 4, 320000.00, 'CNY', '2025-06-05', 'SaaS Subscription - Q2 Revenue', 'Bank Transfer', 'TI-REV-2025-062', FALSE, TRUE, 'INCOME', 2, 2, 3, NULL, '2025-06-05 14:20:00', '2025-06-05 14:20:00'),
(1, 4, 180000.00, 'CNY', '2025-06-08', 'System Integration Services', 'Bank Transfer', 'TI-REV-2025-063', FALSE, TRUE, 'INCOME', 2, 3, 3, NULL, '2025-06-08 09:15:00', '2025-06-08 09:15:00'),
(1, 3, 225000.00, 'CNY', '2025-06-12', 'AI Consulting Project - Phase 1', 'Bank Transfer', 'TI-REV-2025-064', FALSE, TRUE, 'INCOME', 2, 4, 2, NULL, '2025-06-12 16:45:00', '2025-06-12 16:45:00'),
(1, 2, 95000.00, 'CNY', '2025-06-15', 'Technical Support & Maintenance', 'Bank Transfer', 'TI-REV-2025-065', FALSE, TRUE, 'INCOME', 2, 5, 1, NULL, '2025-06-15 11:30:00', '2025-06-15 11:30:00'),
(1, 5, 65000.00, 'CNY', '2025-06-18', 'Training Services - Corporate Package', 'Bank Transfer', 'TI-REV-2025-066', FALSE, TRUE, 'INCOME', 2, 6, 4, NULL, '2025-06-18 13:20:00', '2025-06-18 13:20:00'),
(1, 1, 8500.00, 'CNY', '2025-06-20', 'Investment Interest Income', 'Bank Transfer', 'TI-REV-2025-067', FALSE, TRUE, 'INCOME', 2, 7, 1, NULL, '2025-06-20 15:40:00', '2025-06-20 15:40:00'),

-- Expense Transactions
(1, 3, 165000.00, 'CNY', '2025-06-02', 'AI Algorithm Development', 'Bank Transfer', 'TI-EXP-2025-061', FALSE, TRUE, 'EXPENSE', 2, 8, 2, NULL, '2025-06-02 09:50:00', '2025-06-02 09:50:00'),
(1, 5, 89000.00, 'CNY', '2025-06-04', 'Digital Marketing Campaign', 'Bank Transfer', 'TI-EXP-2025-062', FALSE, TRUE, 'EXPENSE', 2, 9, 4, NULL, '2025-06-04 14:10:00', '2025-06-04 14:10:00'),
(1, 2, 45000.00, 'CNY', '2025-06-06', 'Office Rent - June', 'Bank Transfer', 'TI-EXP-2025-063', FALSE, TRUE, 'EXPENSE', 2, 15, 1, NULL, '2025-06-06 10:25:00', '2025-06-06 10:25:00'),
(1, 6, 125000.00, 'CNY', '2025-06-10', 'Employee Salaries - R&D Team', 'Bank Transfer', 'TI-EXP-2025-064', FALSE, TRUE, 'EXPENSE', 2, 11, 2, NULL, '2025-06-10 12:30:00', '2025-06-10 12:30:00'),
(1, 6, 35000.00, 'CNY', '2025-06-14', 'Cloud Infrastructure Costs', 'Bank Transfer', 'TI-EXP-2025-065', FALSE, TRUE, 'EXPENSE', 2, 13, 5, NULL, '2025-06-14 16:15:00', '2025-06-14 16:15:00'),
(1, 2, 28000.00, 'CNY', '2025-06-16', 'Legal & Accounting Services', 'Bank Transfer', 'TI-EXP-2025-066', FALSE, TRUE, 'EXPENSE', 2, 14, 1, NULL, '2025-06-16 11:45:00', '2025-06-16 11:45:00'),
(1, 4, 52000.00, 'CNY', '2025-06-22', 'Sales Team Commissions', 'Bank Transfer', 'TI-EXP-2025-067', FALSE, TRUE, 'EXPENSE', 2, 9, 3, NULL, '2025-06-22 14:20:00', '2025-06-22 14:20:00'),
(1, 5, 18000.00, 'CNY', '2025-06-25', 'Office Utilities & Maintenance', 'Bank Transfer', 'TI-EXP-2025-068', FALSE, TRUE, 'EXPENSE', 2, 15, 5, NULL, '2025-06-25 09:30:00', '2025-06-25 09:30:00'),
(1, 2, 75000.00, 'CNY', '2025-06-28', 'Equipment Depreciation', 'Bank Transfer', 'TI-EXP-2025-069', FALSE, TRUE, 'EXPENSE', 2, 12, 1, NULL, '2025-06-28 15:50:00', '2025-06-28 15:50:00'),

-- JULY 2025 TRANSACTIONS
-- Revenue Transactions
(1, 2, 920000.00, 'CNY', '2025-07-03', 'Enterprise AI Suite - Major Client', 'Bank Transfer', 'TI-REV-2025-071', FALSE, TRUE, 'INCOME', 2, 1, 3, NULL, '2025-07-03 10:30:00', '2025-07-03 10:30:00'),
(1, 3, 280000.00, 'CNY', '2025-07-05', 'Blockchain Technology Services', 'Bank Transfer', 'TI-REV-2025-072', FALSE, TRUE, 'INCOME', 2, 3, 2, NULL, '2025-07-05 14:20:00', '2025-07-05 14:20:00'),
(1, 4, 450000.00, 'CNY', '2025-07-08', 'System Integration Project', 'Bank Transfer', 'TI-REV-2025-073', FALSE, TRUE, 'INCOME', 2, 3, 3, NULL, '2025-07-08 09:15:00', '2025-07-08 09:15:00'),
(1, 3, 195000.00, 'CNY', '2025-07-10', 'AI Algorithm Consulting', 'Bank Transfer', 'TI-REV-2025-074', FALSE, TRUE, 'INCOME', 2, 4, 2, NULL, '2025-07-10 16:45:00', '2025-07-10 16:45:00'),
(1, 4, 85000.00, 'CNY', '2025-07-12', 'SaaS Platform Subscriptions', 'Bank Transfer', 'TI-REV-2025-075', FALSE, TRUE, 'INCOME', 2, 2, 3, NULL, '2025-07-12 11:30:00', '2025-07-12 11:30:00'),
(1, 2, 120000.00, 'CNY', '2025-07-15', 'Premium Support Services', 'Bank Transfer', 'TI-REV-2025-076', FALSE, TRUE, 'INCOME', 2, 5, 1, NULL, '2025-07-15 13:20:00', '2025-07-15 13:20:00'),
(1, 1, 350000.00, 'CNY', '2025-07-18', 'Custom Software Development', 'Bank Transfer', 'TI-REV-2025-077', FALSE, TRUE, 'INCOME', 2, 3, 2, NULL, '2025-07-18 15:40:00', '2025-07-18 15:40:00'),
(1, 5, 78000.00, 'CNY', '2025-07-22', 'Professional Training Programs', 'Bank Transfer', 'TI-REV-2025-078', FALSE, TRUE, 'INCOME', 2, 6, 4, NULL, '2025-07-22 09:50:00', '2025-07-22 09:50:00'),
(1, 1, 12500.00, 'CNY', '2025-07-25', 'Investment Income - Quarterly', 'Bank Transfer', 'TI-REV-2025-079', FALSE, TRUE, 'INCOME', 2, 7, 1, NULL, '2025-07-25 14:10:00', '2025-07-25 14:10:00'),

-- Expense Transactions  
(1, 3, 185000.00, 'CNY', '2025-07-02', 'Advanced AI Research Project', 'Bank Transfer', 'TI-EXP-2025-071', FALSE, TRUE, 'EXPENSE', 2, 8, 2, NULL, '2025-07-02 10:25:00', '2025-07-02 10:25:00'),
(1, 5, 125000.00, 'CNY', '2025-07-06', 'Marketing Campaign - Q3 Launch', 'Bank Transfer', 'TI-EXP-2025-072', FALSE, TRUE, 'EXPENSE', 2, 9, 4, NULL, '2025-07-06 11:30:00', '2025-07-06 11:30:00'),
(1, 2, 45000.00, 'CNY', '2025-07-08', 'Office Rent - July', 'Bank Transfer', 'TI-EXP-2025-073', FALSE, TRUE, 'EXPENSE', 2, 15, 1, NULL, '2025-07-08 13:20:00', '2025-07-08 13:20:00'),
(1, 6, 158000.00, 'CNY', '2025-07-12', 'Employee Salaries - All Departments', 'Bank Transfer', 'TI-EXP-2025-074', FALSE, TRUE, 'EXPENSE', 2, 11, 6, NULL, '2025-07-12 15:40:00', '2025-07-12 15:40:00'),
(1, 6, 42000.00, 'CNY', '2025-07-15', 'Technology Infrastructure', 'Bank Transfer', 'TI-EXP-2025-075', FALSE, TRUE, 'EXPENSE', 2, 13, 5, NULL, '2025-07-15 09:50:00', '2025-07-15 09:50:00'),
(1, 2, 35000.00, 'CNY', '2025-07-18', 'Professional Services', 'Bank Transfer', 'TI-EXP-2025-076', FALSE, TRUE, 'EXPENSE', 2, 14, 1, NULL, '2025-07-18 14:10:00', '2025-07-18 14:10:00'),
(1, 4, 68000.00, 'CNY', '2025-07-22', 'Sales & Marketing Operations', 'Bank Transfer', 'TI-EXP-2025-077', FALSE, TRUE, 'EXPENSE', 2, 9, 3, NULL, '2025-07-22 10:25:00', '2025-07-22 10:25:00'),
(1, 5, 22000.00, 'CNY', '2025-07-25', 'Facilities Management', 'Bank Transfer', 'TI-EXP-2025-078', FALSE, TRUE, 'EXPENSE', 2, 15, 5, NULL, '2025-07-25 16:15:00', '2025-07-25 16:15:00'),
(1, 2, 78000.00, 'CNY', '2025-07-28', 'Depreciation & Amortization', 'Bank Transfer', 'TI-EXP-2025-079', FALSE, TRUE, 'EXPENSE', 2, 12, 1, NULL, '2025-07-28 11:45:00', '2025-07-28 11:45:00'),

-- AUGUST 2025 TRANSACTIONS
-- Revenue Transactions
(1, 2, 1150000.00, 'CNY', '2025-08-02', 'Smart Manufacturing Solution', 'Bank Transfer', 'TI-REV-2025-081', FALSE, TRUE, 'INCOME', 2, 1, 3, NULL, '2025-08-02 09:30:00', '2025-08-02 09:30:00'),
(1, 3, 380000.00, 'CNY', '2025-08-05', 'Machine Learning Platform', 'Bank Transfer', 'TI-REV-2025-082', FALSE, TRUE, 'INCOME', 2, 2, 2, NULL, '2025-08-05 11:45:00', '2025-08-05 11:45:00'),
(1, 1, 295000.00, 'CNY', '2025-08-07', 'Data Analytics Consulting', 'Bank Transfer', 'TI-REV-2025-083', FALSE, TRUE, 'INCOME', 2, 4, 2, NULL, '2025-08-07 14:15:00', '2025-08-07 14:15:00'),
(1, 4, 520000.00, 'CNY', '2025-08-10', 'Enterprise Integration Services', 'Bank Transfer', 'TI-REV-2025-084', FALSE, TRUE, 'INCOME', 2, 3, 3, NULL, '2025-08-10 10:20:00', '2025-08-10 10:20:00'),
(1, 2, 145000.00, 'CNY', '2025-08-12', 'Premium Support Contracts', 'Bank Transfer', 'TI-REV-2025-085', FALSE, TRUE, 'INCOME', 2, 5, 1, NULL, '2025-08-12 15:30:00', '2025-08-12 15:30:00'),
(1, 5, 95000.00, 'CNY', '2025-08-15', 'Advanced Training Services', 'Bank Transfer', 'TI-REV-2025-086', FALSE, TRUE, 'INCOME', 2, 6, 4, NULL, '2025-08-15 13:50:00', '2025-08-15 13:50:00'),
(1, 3, 265000.00, 'CNY', '2025-08-18', 'Custom AI Development', 'Bank Transfer', 'TI-REV-2025-087', FALSE, TRUE, 'INCOME', 2, 3, 2, NULL, '2025-08-18 11:25:00', '2025-08-18 11:25:00'),
(1, 1, 15000.00, 'CNY', '2025-08-20', 'Investment Income - August', 'Bank Transfer', 'TI-REV-2025-088', FALSE, TRUE, 'INCOME', 2, 7, 1, NULL, '2025-08-20 16:40:00', '2025-08-20 16:40:00'),

-- Expense Transactions
(1, 3, 225000.00, 'CNY', '2025-08-01', 'Next-Gen AI Research', 'Bank Transfer', 'TI-EXP-2025-081', FALSE, TRUE, 'EXPENSE', 2, 8, 2, NULL, '2025-08-01 10:15:00', '2025-08-01 10:15:00'),
(1, 5, 156000.00, 'CNY', '2025-08-05', 'Product Launch Marketing', 'Bank Transfer', 'TI-EXP-2025-082', FALSE, TRUE, 'EXPENSE', 2, 9, 4, NULL, '2025-08-05 14:40:00', '2025-08-05 14:40:00'),
(1, 2, 45000.00, 'CNY', '2025-08-08', 'Office Rent - August', 'Bank Transfer', 'TI-EXP-2025-083', FALSE, TRUE, 'EXPENSE', 2, 15, 1, NULL, '2025-08-08 16:20:00', '2025-08-08 16:20:00'),
(1, 6, 185000.00, 'CNY', '2025-08-12', 'Employee Compensation Package', 'Bank Transfer', 'TI-EXP-2025-084', FALSE, TRUE, 'EXPENSE', 2, 11, 6, NULL, '2025-08-12 12:30:00', '2025-08-12 12:30:00'),
(1, 6, 58000.00, 'CNY', '2025-08-15', 'IT Infrastructure Upgrade', 'Bank Transfer', 'TI-EXP-2025-085', FALSE, TRUE, 'EXPENSE', 2, 13, 5, NULL, '2025-08-15 09:45:00', '2025-08-15 09:45:00'),
(1, 2, 42000.00, 'CNY', '2025-08-18', 'Audit & Compliance Services', 'Bank Transfer', 'TI-EXP-2025-086', FALSE, TRUE, 'EXPENSE', 2, 14, 1, NULL, '2025-08-18 13:25:00', '2025-08-18 13:25:00'),
(1, 4, 89000.00, 'CNY', '2025-08-22', 'Sales Operations & Incentives', 'Bank Transfer', 'TI-EXP-2025-087', FALSE, TRUE, 'EXPENSE', 2, 9, 3, NULL, '2025-08-22 15:10:00', '2025-08-22 15:10:00'),
(1, 5, 28000.00, 'CNY', '2025-08-25', 'Office Operations & Supplies', 'Bank Transfer', 'TI-EXP-2025-088', FALSE, TRUE, 'EXPENSE', 2, 15, 5, NULL, '2025-08-25 11:35:00', '2025-08-25 11:35:00'),
(1, 2, 82000.00, 'CNY', '2025-08-28', 'Monthly Depreciation', 'Bank Transfer', 'TI-EXP-2025-089', FALSE, TRUE, 'EXPENSE', 2, 12, 1, NULL, '2025-08-28 14:55:00', '2025-08-28 14:55:00'),

-- SEPTEMBER 2025 TRANSACTIONS (Partial month)
-- Revenue Transactions
(1, 2, 980000.00, 'CNY', '2025-09-03', 'AI-Powered Analytics Suite', 'Bank Transfer', 'TI-REV-2025-091', FALSE, TRUE, 'INCOME', 2, 1, 3, NULL, '2025-09-03 10:20:00', '2025-09-03 10:20:00'),
(1, 3, 310000.00, 'CNY', '2025-09-06', 'Cloud Migration Services', 'Bank Transfer', 'TI-REV-2025-092', FALSE, TRUE, 'INCOME', 2, 3, 2, NULL, '2025-09-06 14:15:00', '2025-09-06 14:15:00'),
(1, 4, 425000.00, 'CNY', '2025-09-10', 'Enterprise Software Licensing', 'Bank Transfer', 'TI-REV-2025-093', FALSE, TRUE, 'INCOME', 2, 1, 3, NULL, '2025-09-10 11:30:00', '2025-09-10 11:30:00'),
(1, 1, 185000.00, 'CNY', '2025-09-12', 'Strategic Consulting Project', 'Bank Transfer', 'TI-REV-2025-094', FALSE, TRUE, 'INCOME', 2, 4, 1, NULL, '2025-09-12 15:45:00', '2025-09-12 15:45:00'),

-- Expense Transactions  
(1, 3, 195000.00, 'CNY', '2025-09-02', 'Quantum Computing Research', 'Bank Transfer', 'TI-EXP-2025-091', FALSE, TRUE, 'EXPENSE', 2, 8, 2, NULL, '2025-09-02 09:30:00', '2025-09-02 09:30:00'),
(1, 5, 125000.00, 'CNY', '2025-09-05', 'International Market Expansion', 'Bank Transfer', 'TI-EXP-2025-092', FALSE, TRUE, 'EXPENSE', 2, 9, 4, NULL, '2025-09-05 13:20:00', '2025-09-05 13:20:00'),
(1, 2, 45000.00, 'CNY', '2025-09-08', 'Office Rent - September', 'Bank Transfer', 'TI-EXP-2025-093', FALSE, TRUE, 'EXPENSE', 2, 15, 1, NULL, '2025-09-08 16:10:00', '2025-09-08 16:10:00'),
(1, 6, 165000.00, 'CNY', '2025-09-12', 'Employee Benefits & Bonuses', 'Bank Transfer', 'TI-EXP-2025-094', FALSE, TRUE, 'EXPENSE', 2, 11, 6, NULL, '2025-09-12 12:45:00', '2025-09-12 12:45:00'),

-- Minimal transactions for other companies
(2, 7, 450000.00, 'CNY', '2025-07-04', 'Solar Project Revenue', 'Bank Transfer', 'GE-REV-2025-071', FALSE, TRUE, 'INCOME', 2, 16, 7, NULL, '2025-07-04 10:20:00', '2025-07-04 10:20:00'),
(2, 7, 125000.00, 'CNY', '2025-07-15', 'Project Equipment Costs', 'Bank Transfer', 'GE-EXP-2025-071', FALSE, TRUE, 'EXPENSE', 2, 17, 7, NULL, '2025-07-15 13:50:00', '2025-07-15 13:50:00'),

(3, 8, 380000.00, 'CNY', '2025-07-05', 'Financial Consulting Revenue', 'Bank Transfer', 'FS-REV-2025-071', FALSE, TRUE, 'INCOME', 2, 18, 9, NULL, '2025-07-05 09:30:00', '2025-07-05 09:30:00'),
(3, 8, 68000.00, 'CNY', '2025-07-18', 'Office Operating Expenses', 'Bank Transfer', 'FS-EXP-2025-071', FALSE, TRUE, 'EXPENSE', 2, 19, 9, NULL, '2025-07-18 13:30:00', '2025-07-18 13:30:00');

-- ====================
-- 10. CORRECTED Account Balance Data - Perfect Balance
-- ====================

-- 先删除现有的account_balance数据
DELETE FROM account_balance WHERE account_id IN (
    SELECT account_id FROM Account WHERE company_id = 1
);

INSERT INTO account_balance (
    account_id, as_of_date, current_month, previous_month, 
    last_year_end, month, year
) VALUES

-- TECH INNOVATION LTD - JUNE 2025 BALANCES 
-- 总资产计算: 16,580,000 (从你的数据中得出)
-- 需要总负债+权益 = 16,580,000

-- ASSETS (保持原有数值)
(1, '2025-06-30', 485000.00, 425000.00, 350000.00, 6, 2025), -- Cash and Cash Equivalents
(2, '2025-06-30', 2850000.00, 2650000.00, 2400000.00, 6, 2025), -- Bank Deposits - Operations
(3, '2025-06-30', 1200000.00, 1150000.00, 1000000.00, 6, 2025), -- Bank Deposits - Savings
(4, '2025-06-30', 850000.00, 720000.00, 650000.00, 6, 2025), -- Accounts Receivable
(5, '2025-06-30', 125000.00, 110000.00, 95000.00, 6, 2025), -- Notes Receivable
(6, '2025-06-30', 280000.00, 260000.00, 240000.00, 6, 2025), -- Inventory - Raw Materials
(7, '2025-06-30', 450000.00, 420000.00, 380000.00, 6, 2025), -- Inventory - Finished Goods
(8, '2025-06-30', 185000.00, 175000.00, 165000.00, 6, 2025), -- Prepaid Expenses
(9, '2025-06-30', 95000.00, 88000.00, 80000.00, 6, 2025), -- Prepaid Insurance
(10, '2025-06-30', 8500000.00, 8400000.00, 8200000.00, 6, 2025), -- Property, Plant & Equipment
(11, '2025-06-30', -1850000.00, -1775000.00, -1650000.00, 6, 2025), -- Accumulated Depreciation - PPE
(12, '2025-06-30', 2200000.00, 2150000.00, 2050000.00, 6, 2025), -- Intangible Assets
(13, '2025-06-30', -580000.00, -540000.00, -480000.00, 6, 2025), -- Accumulated Amortization - Intangible
(14, '2025-06-30', 1500000.00, 1450000.00, 1350000.00, 6, 2025), -- Investment Securities
(15, '2025-06-30', 290000.00, 270000.00, 250000.00, 6, 2025), -- Other Long-term Assets

-- LIABILITIES (调整以平衡) - 总负债 = 3,480,000
(16, '2025-06-30', 650000.00, 580000.00, 520000.00, 6, 2025), -- Accounts Payable
(17, '2025-06-30', 200000.00, 180000.00, 160000.00, 6, 2025), -- Notes Payable
(18, '2025-06-30', 500000.00, 450000.00, 400000.00, 6, 2025), -- Short-term Loans
(19, '2025-06-30', 285000.00, 260000.00, 240000.00, 6, 2025), -- Accrued Liabilities
(20, '2025-06-30', 120000.00, 110000.00, 100000.00, 6, 2025), -- Accrued Payroll
(21, '2025-06-30', 185000.00, 170000.00, 155000.00, 6, 2025), -- Taxes Payable
(22, '2025-06-30', 95000.00, 85000.00, 75000.00, 6, 2025), -- VAT Payable
(23, '2025-06-30', 65000.00, 60000.00, 55000.00, 6, 2025), -- Deferred Revenue
(24, '2025-06-30', 1200000.00, 1100000.00, 1000000.00, 6, 2025), -- Long-term Loans
(25, '2025-06-30', 120000.00, 110000.00, 100000.00, 6, 2025), -- Deferred Tax Liabilities
(26, '2025-06-30', 60000.00, 55000.00, 50000.00, 6, 2025), -- Other Long-term Liabilities

-- EQUITY (调整以平衡) - 总权益 = 13,100,000
(27, '2025-06-30', 10000000.00, 10000000.00, 10000000.00, 6, 2025), -- Paid-in Capital
(28, '2025-06-30', 2500000.00, 2500000.00, 2500000.00, 6, 2025), -- Additional Paid-in Capital
(29, '2025-06-30', 400000.00, 200000.00, 0.00, 6, 2025), -- Retained Earnings
(30, '2025-06-30', 200000.00, 150000.00, 0.00, 6, 2025), -- Current Year Earnings

-- TECH INNOVATION LTD - JULY 2025 BALANCES
-- 总资产: 17,196,000 (从你的数据中得出)
-- 需要总负债+权益 = 17,196,000

-- ASSETS (保持原有数值)
(1, '2025-07-31', 520000.00, 485000.00, 350000.00, 7, 2025), -- Cash and Cash Equivalents
(2, '2025-07-31', 3250000.00, 2850000.00, 2400000.00, 7, 2025), -- Bank Deposits - Operations
(3, '2025-07-31', 1250000.00, 1200000.00, 1000000.00, 7, 2025), -- Bank Deposits - Savings
(4, '2025-07-31', 950000.00, 850000.00, 650000.00, 7, 2025), -- Accounts Receivable
(5, '2025-07-31', 135000.00, 125000.00, 95000.00, 7, 2025), -- Notes Receivable
(6, '2025-07-31', 295000.00, 280000.00, 240000.00, 7, 2025), -- Inventory - Raw Materials
(7, '2025-07-31', 485000.00, 450000.00, 380000.00, 7, 2025), -- Inventory - Finished Goods
(8, '2025-07-31', 195000.00, 185000.00, 165000.00, 7, 2025), -- Prepaid Expenses
(9, '2025-07-31', 98000.00, 95000.00, 80000.00, 7, 2025), -- Prepaid Insurance
(10, '2025-07-31', 8520000.00, 8500000.00, 8200000.00, 7, 2025), -- Property, Plant & Equipment
(11, '2025-07-31', -1925000.00, -1850000.00, -1650000.00, 7, 2025), -- Accumulated Depreciation - PPE
(12, '2025-07-31', 2220000.00, 2200000.00, 2050000.00, 7, 2025), -- Intangible Assets
(13, '2025-07-31', -620000.00, -580000.00, -480000.00, 7, 2025), -- Accumulated Amortization - Intangible
(14, '2025-07-31', 1515000.00, 1500000.00, 1350000.00, 7, 2025), -- Investment Securities
(15, '2025-07-31', 308000.00, 290000.00, 250000.00, 7, 2025), -- Other Long-term Assets

-- LIABILITIES (调整以平衡) - 总负债 = 4,346,000
(16, '2025-07-31', 720000.00, 650000.00, 520000.00, 7, 2025), -- Accounts Payable
(17, '2025-07-31', 220000.00, 200000.00, 160000.00, 7, 2025), -- Notes Payable
(18, '2025-07-31', 480000.00, 500000.00, 400000.00, 7, 2025), -- Short-term Loans
(19, '2025-07-31', 310000.00, 285000.00, 240000.00, 7, 2025), -- Accrued Liabilities
(20, '2025-07-31', 135000.00, 120000.00, 100000.00, 7, 2025), -- Accrued Payroll
(21, '2025-07-31', 205000.00, 185000.00, 155000.00, 7, 2025), -- Taxes Payable
(22, '2025-07-31', 115000.00, 95000.00, 75000.00, 7, 2025), -- VAT Payable
(23, '2025-07-31', 75000.00, 65000.00, 55000.00, 7, 2025), -- Deferred Revenue
(24, '2025-07-31', 1800000.00, 1200000.00, 1000000.00, 7, 2025), -- Long-term Loans
(25, '2025-07-31', 200000.00, 120000.00, 100000.00, 7, 2025), -- Deferred Tax Liabilities
(26, '2025-07-31', 86000.00, 60000.00, 50000.00, 7, 2025), -- Other Long-term Liabilities

-- EQUITY (调整以平衡) - 总权益 = 12,850,000
(27, '2025-07-31', 10000000.00, 10000000.00, 10000000.00, 7, 2025), -- Paid-in Capital
(28, '2025-07-31', 2500000.00, 2500000.00, 2500000.00, 7, 2025), -- Additional Paid-in Capital
(29, '2025-07-31', 200000.00, 400000.00, 0.00, 7, 2025), -- Retained Earnings (转入部分当期收益)
(30, '2025-07-31', 150000.00, 200000.00, 0.00, 7, 2025), -- Current Year Earnings

-- TECH INNOVATION LTD - AUGUST 2025 BALANCES
-- 总资产: 18,060,000 (从你的数据中得出)
-- 需要总负债+权益 = 18,060,000

-- ASSETS (保持原有数值)
(1, '2025-08-31', 565000.00, 520000.00, 350000.00, 8, 2025), -- Cash and Cash Equivalents
(2, '2025-08-31', 3750000.00, 3250000.00, 2400000.00, 8, 2025), -- Bank Deposits - Operations
(3, '2025-08-31', 1285000.00, 1250000.00, 1000000.00, 8, 2025), -- Bank Deposits - Savings
(4, '2025-08-31', 1150000.00, 950000.00, 650000.00, 8, 2025), -- Accounts Receivable
(5, '2025-08-31', 145000.00, 135000.00, 95000.00, 8, 2025), -- Notes Receivable
(6, '2025-08-31', 315000.00, 295000.00, 240000.00, 8, 2025), -- Inventory - Raw Materials
(7, '2025-08-31', 520000.00, 485000.00, 380000.00, 8, 2025), -- Inventory - Finished Goods
(8, '2025-08-31', 205000.00, 195000.00, 165000.00, 8, 2025), -- Prepaid Expenses
(9, '2025-08-31', 102000.00, 98000.00, 80000.00, 8, 2025), -- Prepaid Insurance
(10, '2025-08-31', 8580000.00, 8520000.00, 8200000.00, 8, 2025), -- Property, Plant & Equipment
(11, '2025-08-31', -2007000.00, -1925000.00, -1650000.00, 8, 2025), -- Accumulated Depreciation - PPE
(12, '2025-08-31', 2250000.00, 2220000.00, 2050000.00, 8, 2025), -- Intangible Assets
(13, '2025-08-31', -662000.00, -620000.00, -480000.00, 8, 2025), -- Accumulated Amortization - Intangible
(14, '2025-08-31', 1530000.00, 1515000.00, 1350000.00, 8, 2025), -- Investment Securities
(15, '2025-08-31', 332000.00, 308000.00, 250000.00, 8, 2025), -- Other Long-term Assets

-- LIABILITIES (调整以平衡) - 总负债 = 5,410,000
(16, '2025-08-31', 850000.00, 720000.00, 520000.00, 8, 2025), -- Accounts Payable
(17, '2025-08-31', 240000.00, 220000.00, 160000.00, 8, 2025), -- Notes Payable
(18, '2025-08-31', 460000.00, 480000.00, 400000.00, 8, 2025), -- Short-term Loans
(19, '2025-08-31', 335000.00, 310000.00, 240000.00, 8, 2025), -- Accrued Liabilities
(20, '2025-08-31', 150000.00, 135000.00, 100000.00, 8, 2025), -- Accrued Payroll
(21, '2025-08-31', 225000.00, 205000.00, 155000.00, 8, 2025), -- Taxes Payable
(22, '2025-08-31', 135000.00, 115000.00, 75000.00, 8, 2025), -- VAT Payable
(23, '2025-08-31', 85000.00, 75000.00, 55000.00, 8, 2025), -- Deferred Revenue
(24, '2025-08-31', 2500000.00, 1800000.00, 1000000.00, 8, 2025), -- Long-term Loans
(25, '2025-08-31', 280000.00, 200000.00, 100000.00, 8, 2025), -- Deferred Tax Liabilities
(26, '2025-08-31', 150000.00, 86000.00, 50000.00, 8, 2025), -- Other Long-term Liabilities

-- EQUITY (调整以平衡) - 总权益 = 12,650,000
(27, '2025-08-31', 10000000.00, 10000000.00, 10000000.00, 8, 2025), -- Paid-in Capital
(28, '2025-08-31', 2500000.00, 2500000.00, 2500000.00, 8, 2025), -- Additional Paid-in Capital
(29, '2025-08-31', 50000.00, 200000.00, 0.00, 8, 2025), -- Retained Earnings
(30, '2025-08-31', 100000.00, 150000.00, 0.00, 8, 2025), -- Current Year Earnings

-- TECH INNOVATION LTD - SEPTEMBER 2025 BALANCES (估算的资产值，保持平衡)
-- 假设总资产继续增长到 18,500,000

-- ASSETS (轻微调整以反映增长)
(1, '2025-09-15', 580000.00, 565000.00, 350000.00, 9, 2025), -- Cash and Cash Equivalents
(2, '2025-09-15', 4150000.00, 3750000.00, 2400000.00, 9, 2025), -- Bank Deposits - Operations
(3, '2025-09-15', 1300000.00, 1285000.00, 1000000.00, 9, 2025), -- Bank Deposits - Savings
(4, '2025-09-15', 1285000.00, 1150000.00, 650000.00, 9, 2025), -- Accounts Receivable
(5, '2025-09-15', 155000.00, 145000.00, 95000.00, 9, 2025), -- Notes Receivable
(6, '2025-09-15', 325000.00, 315000.00, 240000.00, 9, 2025), -- Inventory - Raw Materials
(7, '2025-09-15', 545000.00, 520000.00, 380000.00, 9, 2025), -- Inventory - Finished Goods
(8, '2025-09-15', 215000.00, 205000.00, 165000.00, 9, 2025), -- Prepaid Expenses
(9, '2025-09-15', 105000.00, 102000.00, 80000.00, 9, 2025), -- Prepaid Insurance
(10, '2025-09-15', 8620000.00, 8580000.00, 8200000.00, 9, 2025), -- Property, Plant & Equipment
(11, '2025-09-15', -2048000.00, -2007000.00, -1650000.00, 9, 2025), -- Accumulated Depreciation - PPE
(12, '2025-09-15', 2280000.00, 2250000.00, 2050000.00, 9, 2025), -- Intangible Assets
(13, '2025-09-15', -683000.00, -662000.00, -480000.00, 9, 2025), -- Accumulated Amortization - Intangible
(14, '2025-09-15', 1545000.00, 1530000.00, 1350000.00, 9, 2025), -- Investment Securities
(15, '2025-09-15', 350000.00, 332000.00, 250000.00, 9, 2025), -- Other Long-term Assets

-- LIABILITIES (总负债 = 5,750,000)
(16, '2025-09-15', 920000.00, 850000.00, 520000.00, 9, 2025), -- Accounts Payable
(17, '2025-09-15', 250000.00, 240000.00, 160000.00, 9, 2025), -- Notes Payable
(18, '2025-09-15', 440000.00, 460000.00, 400000.00, 9, 2025), -- Short-term Loans
(19, '2025-09-15', 350000.00, 335000.00, 240000.00, 9, 2025), -- Accrued Liabilities
(20, '2025-09-15', 165000.00, 150000.00, 100000.00, 9, 2025), -- Accrued Payroll
(21, '2025-09-15', 235000.00, 225000.00, 155000.00, 9, 2025), -- Taxes Payable
(22, '2025-09-15', 145000.00, 135000.00, 75000.00, 9, 2025), -- VAT Payable
(23, '2025-09-15', 95000.00, 85000.00, 55000.00, 9, 2025), -- Deferred Revenue
(24, '2025-09-15', 2800000.00, 2500000.00, 1000000.00, 9, 2025), -- Long-term Loans
(25, '2025-09-15', 250000.00, 280000.00, 100000.00, 9, 2025), -- Deferred Tax Liabilities
(26, '2025-09-15', 100000.00, 150000.00, 50000.00, 9, 2025), -- Other Long-term Liabilities

-- EQUITY (总权益 = 12,975,000)
(27, '2025-09-15', 10000000.00, 10000000.00, 10000000.00, 9, 2025), -- Paid-in Capital
(28, '2025-09-15', 2500000.00, 2500000.00, 2500000.00, 9, 2025), -- Additional Paid-in Capital
(29, '2025-09-15', 150000.00, 50000.00, 0.00, 9, 2025), -- Retained Earnings
(30, '2025-09-15', 325000.00, 100000.00, 0.00, 9, 2025); -- Current Year Earnings

-- 验证余额平衡的查询
SELECT 
    'Asset-Liability Balance Check' as check_type,
    ab.as_of_date,
    -- 计算总资产 (包括反向科目的正确处理)
    SUM(CASE WHEN a.account_type = 'ASSET' AND a.balance_direction = 'DEBIT' THEN ab.current_month
             WHEN a.account_type = 'ASSET' AND a.balance_direction = 'CREDIT' THEN ab.current_month
             ELSE 0 END) as calculated_total_assets,
    -- 计算总负债
    SUM(CASE WHEN a.account_type = 'LIABILITY' THEN ab.current_month ELSE 0 END) as calculated_total_liabilities,
    -- 计算总权益
    SUM(CASE WHEN a.account_type = 'EQUITY' THEN ab.current_month ELSE 0 END) as calculated_total_equity,
    -- 平衡检查
    (SUM(CASE WHEN a.account_type = 'ASSET' AND a.balance_direction = 'DEBIT' THEN ab.current_month
              WHEN a.account_type = 'ASSET' AND a.balance_direction = 'CREDIT' THEN ab.current_month
              ELSE 0 END) - 
     SUM(CASE WHEN a.account_type IN ('LIABILITY','EQUITY') THEN ab.current_month ELSE 0 END)) as balance_difference
FROM account_balance ab
JOIN Account a ON ab.account_id = a.account_id
WHERE a.company_id = 1 AND ab.as_of_date IN ('2025-06-30', '2025-07-31', '2025-08-31', '2025-09-15')
GROUP BY ab.as_of_date
ORDER BY ab.as_of_date;

-- ====================
-- 11. Data Validation and Summary Queries
-- ====================

-- Balance Sheet Verification
SELECT 
    '========================================' as info
UNION ALL SELECT 'ENHANCED DEMO DATA FOR SCREENSHOTS READY!'
UNION ALL SELECT '========================================'
UNION ALL SELECT ''
UNION ALL SELECT 'PRIMARY FOCUS: Tech Innovation Ltd (Company ID: 1)'
UNION ALL SELECT 'DATA COVERAGE: June 2025 - September 2025'
UNION ALL SELECT 'TRANSACTION COUNT: 80+ high-quality transactions'
UNION ALL SELECT 'BALANCE SHEET: Complete with all standard accounts'
UNION ALL SELECT ''
UNION ALL SELECT 'SCREENSHOT-READY REPORTS:'
UNION ALL SELECT '========================================'
UNION ALL SELECT '1. BALANCE SHEET:'
UNION ALL SELECT '   ✅ Current Assets (Cash, Receivables, Inventory)'
UNION ALL SELECT '   ✅ Non-Current Assets (PPE, Intangibles, Investments)'
UNION ALL SELECT '   ✅ Current Liabilities (Payables, Short-term debt)'
UNION ALL SELECT '   ✅ Long-term Liabilities (Long-term debt, Deferred tax)'
UNION ALL SELECT '   ✅ Equity (Capital, Retained Earnings)'
UNION ALL SELECT ''
UNION ALL SELECT '2. INCOME STATEMENT (P&L):'
UNION ALL SELECT '   ✅ Multiple Revenue Streams (Software, SaaS, Services)'
UNION ALL SELECT '   ✅ Categorized Expenses (R&D, Sales, G&A, Operations)'
UNION ALL SELECT '   ✅ Monthly Progression (June-September 2025)'
UNION ALL SELECT '   ✅ Realistic Business Metrics'
UNION ALL SELECT ''
UNION ALL SELECT '3. INCOME vs EXPENSE ANALYSIS:'
UNION ALL SELECT '   ✅ Department-wise Breakdown'
UNION ALL SELECT '   ✅ Category-wise Analysis'
UNION ALL SELECT '   ✅ Trend Analysis Capability'
UNION ALL SELECT '   ✅ Variance Analysis Ready'
UNION ALL SELECT ''
UNION ALL SELECT '4. FINANCIAL GROUPING:'
UNION ALL SELECT '   ✅ Revenue by Business Line'
UNION ALL SELECT '   ✅ Expenses by Function'
UNION ALL SELECT '   ✅ Department Performance'
UNION ALL SELECT '   ✅ Cost Center Analysis'
UNION ALL SELECT ''
UNION ALL SELECT 'DEMO CREDENTIALS (Password: password123):'
UNION ALL SELECT '========================================'
UNION ALL SELECT 'admin@techinnovation.com   | SYSTEM_ADMIN'
UNION ALL SELECT 'zhang.wei@techinnovation.com | CFO'
UNION ALL SELECT 'li.ming@techinnovation.com | R&D Director'
UNION ALL SELECT 'wang.fang@techinnovation.com | Sales Director'
UNION ALL SELECT ''
UNION ALL SELECT 'BUSINESS METRICS HIGHLIGHTS:'
UNION ALL SELECT '========================================'
UNION ALL SELECT 'Total Assets (Aug 2025): ¥16.8M+'
UNION ALL SELECT 'Total Revenue (Q3 2025): ¥8.5M+'
UNION ALL SELECT 'R&D Investment: ¥600K+ (Q3)'
UNION ALL SELECT 'Monthly Recurring Revenue: ¥2.8M+ avg'
UNION ALL SELECT 'Employee Count: 50+ across 6 departments'
UNION ALL SELECT 'Growth Rate: 25%+ quarter-over-quarter'
UNION ALL SELECT ''
UNION ALL SELECT 'AI FEATURES:'
UNION ALL SELECT '✅ Transaction categorization data'
UNION ALL SELECT '✅ Trend analysis ready datasets'
UNION ALL SELECT '✅ Variance analysis capabilities'
UNION ALL SELECT '✅ Predictive analytics data foundation'
UNION ALL SELECT '========================================';

-- Balance Sheet Verification for August 2025
SELECT 
    'Balance Sheet Verification - August 2025' as report_type,
    c.company_name,
    SUM(CASE WHEN a.account_type = 'ASSET' AND ab.current_month > 0 THEN ab.current_month 
             WHEN a.account_type = 'ASSET' AND ab.current_month < 0 THEN -ab.current_month ELSE 0 END) as total_assets,
    SUM(CASE WHEN a.account_type = 'LIABILITY' THEN ab.current_month ELSE 0 END) as total_liabilities,
    SUM(CASE WHEN a.account_type = 'EQUITY' THEN ab.current_month ELSE 0 END) as total_equity,
    (SUM(CASE WHEN a.account_type = 'ASSET' AND ab.current_month > 0 THEN ab.current_month 
              WHEN a.account_type = 'ASSET' AND ab.current_month < 0 THEN -ab.current_month ELSE 0 END) - 
     SUM(CASE WHEN a.account_type IN ('LIABILITY','EQUITY') THEN ab.current_month ELSE 0 END)) as balance_check
FROM account_balance ab
JOIN Account a ON ab.account_id = a.account_id
JOIN Company c ON a.company_id = c.company_id
WHERE ab.as_of_date = '2025-08-31' AND c.company_id = 1
GROUP BY c.company_id, c.company_name
ORDER BY c.company_id;

-- Income Statement Summary for Q3 2025
SELECT 
    'Income Statement Summary - Q3 2025' as report_type,
    c.company_name,
    SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END) as total_revenue,
    SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END) as total_expenses,
    (SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END) - 
     SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END)) as net_income
FROM Transaction t
JOIN Company c ON t.company_id = c.company_id
WHERE t.transaction_date BETWEEN '2025-07-01' AND '2025-09-15'
  AND t.status = 2  -- Completed transactions
  AND c.company_id = 1
GROUP BY c.company_id, c.company_name
ORDER BY c.company_id;

-- Monthly Revenue Trend
SELECT 
    'Monthly Revenue Trend - Tech Innovation Ltd' as report_type,
    MONTH(t.transaction_date) as month,
    SUM(t.amount) as monthly_revenue,
    COUNT(*) as transaction_count
FROM Transaction t
WHERE t.company_id = 1 
  AND t.transaction_type = 'INCOME' 
  AND t.status = 2
  AND t.transaction_date BETWEEN '2025-06-01' AND '2025-09-15'
GROUP BY MONTH(t.transaction_date)
ORDER BY month;

-- Department Performance Summary
SELECT 
    'Department Performance - Tech Innovation Ltd' as report_type,
    d.name as department,
    SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END) as revenue,
    SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END) as expenses,
    (SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END) - 
     SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END)) as net_contribution
FROM Transaction t
JOIN Department d ON t.department_id = d.department_id
WHERE t.company_id = 1 
  AND t.status = 2
  AND t.transaction_date BETWEEN '2025-06-01' AND '2025-09-15'
GROUP BY d.department_id, d.name
ORDER BY net_contribution DESC;