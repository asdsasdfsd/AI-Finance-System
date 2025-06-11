-- backend/src/main/resources/data.sql
-- Database Reset and Mock Data Script for AI Finance System
-- Run this script to clear existing data and create comprehensive test data

-- ====================
-- 1. CLEAR EXISTING DATA (in dependency order)
-- ====================

-- Clear transaction-related tables first
DELETE FROM Journal_Line;
DELETE FROM Journal_Entry;
DELETE FROM Transaction;

-- Clear reference data
DELETE FROM User_Role;
DELETE FROM Budget_Line;
DELETE FROM Budget;
DELETE FROM account_balance;

-- Clear master data
DELETE FROM Fixed_Asset;
DELETE FROM Fund;
DELETE FROM Category;
DELETE FROM Account;
DELETE FROM Department;
DELETE FROM User;
DELETE FROM Role;
DELETE FROM Fiscal_Period;
DELETE FROM Company;

-- Reset auto-increment counters
ALTER TABLE Company AUTO_INCREMENT = 1;
ALTER TABLE User AUTO_INCREMENT = 1;
ALTER TABLE Role AUTO_INCREMENT = 1;
ALTER TABLE Department AUTO_INCREMENT = 1;
ALTER TABLE Account AUTO_INCREMENT = 1;
ALTER TABLE Category AUTO_INCREMENT = 1;
ALTER TABLE Fund AUTO_INCREMENT = 1;
ALTER TABLE Fixed_Asset AUTO_INCREMENT = 1;
ALTER TABLE Transaction AUTO_INCREMENT = 1;
ALTER TABLE Journal_Entry AUTO_INCREMENT = 1;
ALTER TABLE Journal_Line AUTO_INCREMENT = 1;

-- ====================
-- 2. INSERT MOCK DATA
-- ====================

-- Insert Roles
INSERT INTO Role (name, description) VALUES
('SYSTEM_ADMIN', 'System administrator with full access'),
('COMPANY_ADMIN', 'Company administrator'),
('FINANCE_MANAGER', 'Finance manager with financial reporting access'),
('USER', 'Regular user with basic access');

-- Insert Companies
INSERT INTO Company (company_name, address, city, state_province, postal_code, email, website, registration_number, tax_id, fiscal_year_start, default_currency, status, created_at, updated_at) VALUES
('Tech Innovation Ltd', '123 Tech Street', 'Shanghai', 'Shanghai', '200000', 'contact@techinnovation.com', 'https://techinnovation.com', 'REG001', 'TAX001', '01-01', 'CNY', 'ACTIVE', NOW(), NOW()),
('Green Energy Corp', '456 Green Avenue', 'Beijing', 'Beijing', '100000', 'info@greenenergy.com', 'https://greenenergy.com', 'REG002', 'TAX002', '01-01', 'CNY', 'ACTIVE', NOW(), NOW()),
('Finance Solutions Inc', '789 Finance Road', 'Shenzhen', 'Guangdong', '518000', 'admin@financesolutions.com', 'https://financesolutions.com', 'REG003', 'TAX003', '01-01', 'CNY', 'ACTIVE', NOW(), NOW());

-- Insert Departments
INSERT INTO Department (company_id, name, code, budget, is_active, created_at, updated_at) VALUES
(1, 'Finance Department', 'FIN', 500000.00, TRUE, NOW(), NOW()),
(1, 'IT Department', 'IT', 800000.00, TRUE, NOW(), NOW()),
(1, 'Sales Department', 'SALES', 600000.00, TRUE, NOW(), NOW()),
(1, 'Marketing Department', 'MKT', 400000.00, TRUE, NOW(), NOW()),
(2, 'Finance Department', 'FIN', 300000.00, TRUE, NOW(), NOW()),
(2, 'Operations Department', 'OPS', 700000.00, TRUE, NOW(), NOW()),
(3, 'Finance Department', 'FIN', 400000.00, TRUE, NOW(), NOW());

-- Insert Users
INSERT INTO User (username, email, password, full_name, enabled, company_id, department_id, preferred_language, timezone, created_at, updated_at) VALUES
('admin', 'admin@techinnovation.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.ozSYkOTa', 'System Administrator', TRUE, 1, 1, 'zh-CN', 'Asia/Shanghai', NOW(), NOW()),
('john.doe', 'john.doe@techinnovation.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.ozSYkOTa', 'John Doe', TRUE, 1, 1, 'en-US', 'Asia/Shanghai', NOW(), NOW()),
('jane.smith', 'jane.smith@techinnovation.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.ozSYkOTa', 'Jane Smith', TRUE, 1, 2, 'en-US', 'Asia/Shanghai', NOW(), NOW()),
('mike.wilson', 'mike.wilson@techinnovation.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.ozSYkOTa', 'Mike Wilson', TRUE, 1, 3, 'en-US', 'Asia/Shanghai', NOW(), NOW()),
('sarah.brown', 'sarah.brown@greenenergy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.ozSYkOTa', 'Sarah Brown', TRUE, 2, 5, 'en-US', 'Asia/Shanghai', NOW(), NOW()),
('david.lee', 'david.lee@financesolutions.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.ozSYkOTa', 'David Lee', TRUE, 3, 7, 'zh-CN', 'Asia/Shanghai', NOW(), NOW());

-- Insert User-Role mappings
INSERT INTO User_Role (user_id, role_id) VALUES
(1, 1), -- admin has SYSTEM_ADMIN
(1, 2), -- admin also has COMPANY_ADMIN
(2, 3), -- john.doe has FINANCE_MANAGER
(3, 4), -- jane.smith has USER
(4, 4), -- mike.wilson has USER
(5, 3), -- sarah.brown has FINANCE_MANAGER
(6, 2); -- david.lee has COMPANY_ADMIN

-- Insert Accounts (Chart of Accounts)
INSERT INTO Account (company_id, account_code, name, account_type, balance_direction, is_active, created_at, updated_at) VALUES
-- Assets
(1, '1001', 'Cash', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1002', 'Bank Account', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1101', 'Accounts Receivable', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1201', 'Inventory', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1301', 'Office Equipment', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
(1, '1302', 'Computer Equipment', 'ASSET', 'DEBIT', TRUE, NOW(), NOW()),
-- Liabilities
(1, '2001', 'Accounts Payable', 'LIABILITY', 'CREDIT', TRUE, NOW(), NOW()),
(1, '2101', 'Short-term Loans', 'LIABILITY', 'CREDIT', TRUE, NOW(), NOW()),
(1, '2201', 'Long-term Debt', 'LIABILITY', 'CREDIT', TRUE, NOW(), NOW()),
-- Equity
(1, '3001', 'Owner Equity', 'EQUITY', 'CREDIT', TRUE, NOW(), NOW()),
(1, '3101', 'Retained Earnings', 'EQUITY', 'CREDIT', TRUE, NOW(), NOW()),
-- Revenue
(1, '4001', 'Service Revenue', 'REVENUE', 'CREDIT', TRUE, NOW(), NOW()),
(1, '4002', 'Product Sales', 'REVENUE', 'CREDIT', TRUE, NOW(), NOW()),
(1, '4101', 'Other Income', 'REVENUE', 'CREDIT', TRUE, NOW(), NOW()),
-- Expenses
(1, '5001', 'Salary Expense', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),
(1, '5002', 'Rent Expense', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),
(1, '5003', 'Utilities Expense', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),
(1, '5004', 'Office Supplies', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),
(1, '5005', 'Marketing Expense', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW()),
(1, '5006', 'Travel Expense', 'EXPENSE', 'DEBIT', TRUE, NOW(), NOW());

-- Copy accounts for other companies
INSERT INTO Account (company_id, account_code, name, account_type, balance_direction, is_active, created_at, updated_at)
SELECT 2, account_code, name, account_type, balance_direction, is_active, NOW(), NOW()
FROM Account WHERE company_id = 1;

INSERT INTO Account (company_id, account_code, name, account_type, balance_direction, is_active, created_at, updated_at)
SELECT 3, account_code, name, account_type, balance_direction, is_active, NOW(), NOW()
FROM Account WHERE company_id = 1;

-- Insert Categories
INSERT INTO Category (company_id, name, type, account_id, is_active, created_at, updated_at) VALUES
-- Income Categories for Company 1
(1, 'Service Income', 'INCOME', 12, TRUE, NOW(), NOW()),
(1, 'Product Sales', 'INCOME', 13, TRUE, NOW(), NOW()),
(1, 'Other Income', 'INCOME', 14, TRUE, NOW(), NOW()),
-- Expense Categories for Company 1
(1, 'Personnel Costs', 'EXPENSE', 15, TRUE, NOW(), NOW()),
(1, 'Office Expenses', 'EXPENSE', 16, TRUE, NOW(), NOW()),
(1, 'Utilities', 'EXPENSE', 17, TRUE, NOW(), NOW()),
(1, 'Office Supplies', 'EXPENSE', 18, TRUE, NOW(), NOW()),
(1, 'Marketing', 'EXPENSE', 19, TRUE, NOW(), NOW()),
(1, 'Travel', 'EXPENSE', 20, TRUE, NOW(), NOW());

-- Copy categories for other companies
INSERT INTO Category (company_id, name, type, account_id, is_active, created_at, updated_at)
SELECT 2, name, type, (account_id + 18), TRUE, NOW(), NOW()
FROM Category WHERE company_id = 1;

INSERT INTO Category (company_id, name, type, account_id, is_active, created_at, updated_at)
SELECT 3, name, type, (account_id + 36), TRUE, NOW(), NOW()
FROM Category WHERE company_id = 1;

-- Insert Funds
INSERT INTO Fund (company_id, name, description, fund_type, balance, is_active, created_at, updated_at) VALUES
(1, 'Operating Fund', 'Main operating fund for daily operations', 'OPERATING', 1000000.00, TRUE, NOW(), NOW()),
(1, 'Investment Fund', 'Fund for capital investments', 'INVESTMENT', 500000.00, TRUE, NOW(), NOW()),
(1, 'Emergency Fund', 'Emergency reserve fund', 'RESERVE', 200000.00, TRUE, NOW(), NOW()),
(2, 'Operating Fund', 'Main operating fund for daily operations', 'OPERATING', 800000.00, TRUE, NOW(), NOW()),
(2, 'Project Fund', 'Fund for specific projects', 'PROJECT', 300000.00, TRUE, NOW(), NOW()),
(3, 'Operating Fund', 'Main operating fund for daily operations', 'OPERATING', 600000.00, TRUE, NOW(), NOW());

-- Insert Fixed Assets
INSERT INTO Fixed_Asset (company_id, department_id, name, description, acquisition_date, acquisition_cost, current_value, accumulated_depreciation, location, serial_number, status, created_at, updated_at) VALUES
(1, 1, 'Desktop Computer', 'Dell OptiPlex 7090', '2023-01-15', 8000.00, 6400.00, 1600.00, 'Finance Office Room 101', 'DELL001', 'ACTIVE', NOW(), NOW()),
(1, 1, 'Printer', 'HP LaserJet Pro', '2023-02-01', 3000.00, 2400.00, 600.00, 'Finance Office Room 102', 'HP001', 'ACTIVE', NOW(), NOW()),
(1, 2, 'Server', 'Dell PowerEdge R750', '2023-03-01', 50000.00, 42500.00, 7500.00, 'Data Center', 'DELL-SRV001', 'ACTIVE', NOW(), NOW()),
(1, 2, 'Network Switch', 'Cisco Catalyst 9300', '2023-03-15', 15000.00, 13000.00, 2000.00, 'Data Center', 'CISCO001', 'ACTIVE', NOW(), NOW()),
(1, 3, 'Office Furniture Set', 'Ergonomic desk and chair', '2023-01-10', 5000.00, 4500.00, 500.00, 'Sales Office', 'FURN001', 'ACTIVE', NOW(), NOW()),
(2, 5, 'Manufacturing Equipment', 'Solar Panel Production Line', '2023-01-01', 200000.00, 180000.00, 20000.00, 'Factory Floor A', 'SOLAR001', 'ACTIVE', NOW(), NOW()),
(2, 6, 'Delivery Truck', 'Mercedes Sprinter', '2023-02-15', 80000.00, 72000.00, 8000.00, 'Warehouse', 'MB001', 'ACTIVE', NOW(), NOW()),
(3, 7, 'Conference Room Setup', 'Complete meeting room equipment', '2023-01-20', 25000.00, 22500.00, 2500.00, 'Conference Room A', 'CONF001', 'ACTIVE', NOW(), NOW());

-- Insert Transactions (Comprehensive test data for reports)
INSERT INTO Transaction (user_id, company_id, department_id, fund_id, category_id, amount, transaction_type, currency, transaction_date, description, payment_method, reference_number, is_recurring, is_taxable, created_at, updated_at) VALUES
-- Company 1 - Tech Innovation Ltd - Income Transactions
(2, 1, 3, 1, 1, 50000.00, 'INCOME', 'CNY', '2024-01-15', 'Software Development Service', 'Bank Transfer', 'INV001', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 1, 75000.00, 'INCOME', 'CNY', '2024-01-20', 'System Integration Service', 'Bank Transfer', 'INV002', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 2, 30000.00, 'INCOME', 'CNY', '2024-01-25', 'Software License Sales', 'Bank Transfer', 'INV003', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 1, 80000.00, 'INCOME', 'CNY', '2024-02-05', 'Consulting Service', 'Bank Transfer', 'INV004', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 2, 45000.00, 'INCOME', 'CNY', '2024-02-15', 'Product Sales', 'Bank Transfer', 'INV005', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 3, 5000.00, 'INCOME', 'CNY', '2024-02-20', 'Training Income', 'Cash', 'INV006', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 1, 90000.00, 'INCOME', 'CNY', '2024-03-01', 'Custom Software Development', 'Bank Transfer', 'INV007', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 2, 35000.00, 'INCOME', 'CNY', '2024-03-10', 'Hardware Sales', 'Bank Transfer', 'INV008', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 1, 60000.00, 'INCOME', 'CNY', '2024-03-20', 'Maintenance Service', 'Bank Transfer', 'INV009', FALSE, TRUE, NOW(), NOW()),
(2, 1, 3, 1, 3, 8000.00, 'INCOME', 'CNY', '2024-03-25', 'Technical Support', 'Bank Transfer', 'INV010', FALSE, TRUE, NOW(), NOW()),

-- Company 1 - Expense Transactions
(2, 1, 1, 1, 4, 25000.00, 'EXPENSE', 'CNY', '2024-01-05', 'Monthly Salaries - Finance', 'Bank Transfer', 'SAL001', TRUE, FALSE, NOW(), NOW()),
(3, 1, 2, 1, 4, 35000.00, 'EXPENSE', 'CNY', '2024-01-05', 'Monthly Salaries - IT', 'Bank Transfer', 'SAL002', TRUE, FALSE, NOW(), NOW()),
(4, 1, 3, 1, 4, 30000.00, 'EXPENSE', 'CNY', '2024-01-05', 'Monthly Salaries - Sales', 'Bank Transfer', 'SAL003', TRUE, FALSE, NOW(), NOW()),
(2, 1, 1, 1, 5, 8000.00, 'EXPENSE', 'CNY', '2024-01-10', 'Office Rent', 'Bank Transfer', 'RENT001', TRUE, FALSE, NOW(), NOW()),
(2, 1, 1, 1, 6, 2500.00, 'EXPENSE', 'CNY', '2024-01-15', 'Electricity Bill', 'Bank Transfer', 'UTIL001', FALSE, TRUE, NOW(), NOW()),
(2, 1, 1, 1, 7, 1200.00, 'EXPENSE', 'CNY', '2024-01-20', 'Office Supplies', 'Cash', 'SUP001', FALSE, TRUE, NOW(), NOW()),
(4, 1, 4, 1, 8, 5000.00, 'EXPENSE', 'CNY', '2024-01-25', 'Marketing Campaign', 'Bank Transfer', 'MKT001', FALSE, TRUE, NOW(), NOW()),
(3, 1, 2, 1, 9, 3000.00, 'EXPENSE', 'CNY', '2024-01-30', 'Business Travel', 'Credit Card', 'TRV001', FALSE, TRUE, NOW(), NOW()),

-- February Expenses
(2, 1, 1, 1, 4, 26000.00, 'EXPENSE', 'CNY', '2024-02-05', 'Monthly Salaries - Finance', 'Bank Transfer', 'SAL004', TRUE, FALSE, NOW(), NOW()),
(3, 1, 2, 1, 4, 36000.00, 'EXPENSE', 'CNY', '2024-02-05', 'Monthly Salaries - IT', 'Bank Transfer', 'SAL005', TRUE, FALSE, NOW(), NOW()),
(4, 1, 3, 1, 4, 31000.00, 'EXPENSE', 'CNY', '2024-02-05', 'Monthly Salaries - Sales', 'Bank Transfer', 'SAL006', TRUE, FALSE, NOW(), NOW()),
(2, 1, 1, 1, 5, 8000.00, 'EXPENSE', 'CNY', '2024-02-10', 'Office Rent', 'Bank Transfer', 'RENT002', TRUE, FALSE, NOW(), NOW()),
(2, 1, 1, 1, 6, 2800.00, 'EXPENSE', 'CNY', '2024-02-15', 'Electricity Bill', 'Bank Transfer', 'UTIL002', FALSE, TRUE, NOW(), NOW()),
(2, 1, 1, 1, 7, 1500.00, 'EXPENSE', 'CNY', '2024-02-20', 'Office Supplies', 'Cash', 'SUP002', FALSE, TRUE, NOW(), NOW()),
(4, 1, 4, 1, 8, 7000.00, 'EXPENSE', 'CNY', '2024-02-25', 'Online Advertising', 'Credit Card', 'MKT002', FALSE, TRUE, NOW(), NOW()),

-- March Expenses
(2, 1, 1, 1, 4, 27000.00, 'EXPENSE', 'CNY', '2024-03-05', 'Monthly Salaries - Finance', 'Bank Transfer', 'SAL007', TRUE, FALSE, NOW(), NOW()),
(3, 1, 2, 1, 4, 37000.00, 'EXPENSE', 'CNY', '2024-03-05', 'Monthly Salaries - IT', 'Bank Transfer', 'SAL008', TRUE, FALSE, NOW(), NOW()),
(4, 1, 3, 1, 4, 32000.00, 'EXPENSE', 'CNY', '2024-03-05', 'Monthly Salaries - Sales', 'Bank Transfer', 'SAL009', TRUE, FALSE, NOW(), NOW()),
(2, 1, 1, 1, 5, 8000.00, 'EXPENSE', 'CNY', '2024-03-10', 'Office Rent', 'Bank Transfer', 'RENT003', TRUE, FALSE, NOW(), NOW()),
(2, 1, 1, 1, 6, 3200.00, 'EXPENSE', 'CNY', '2024-03-15', 'Electricity Bill', 'Bank Transfer', 'UTIL003', FALSE, TRUE, NOW(), NOW()),
(2, 1, 1, 1, 7, 1800.00, 'EXPENSE', 'CNY', '2024-03-20', 'Office Supplies', 'Cash', 'SUP003', FALSE, TRUE, NOW(), NOW()),
(4, 1, 4, 1, 8, 6000.00, 'EXPENSE', 'CNY', '2024-03-25', 'Trade Show Participation', 'Bank Transfer', 'MKT003', FALSE, TRUE, NOW(), NOW()),
(3, 1, 2, 1, 9, 4500.00, 'EXPENSE', 'CNY', '2024-03-30', 'Conference Travel', 'Credit Card', 'TRV002', FALSE, TRUE, NOW(), NOW()),

-- Company 2 - Green Energy Corp Transactions
(5, 2, 5, 4, 10, 120000.00, 'INCOME', 'CNY', '2024-01-10', 'Solar Panel Installation', 'Bank Transfer', 'GE001', FALSE, TRUE, NOW(), NOW()),
(5, 2, 5, 4, 10, 85000.00, 'INCOME', 'CNY', '2024-01-25', 'Wind Turbine Maintenance', 'Bank Transfer', 'GE002', FALSE, TRUE, NOW(), NOW()),
(5, 2, 5, 4, 11, 45000.00, 'INCOME', 'CNY', '2024-02-15', 'Green Energy Consulting', 'Bank Transfer', 'GE003', FALSE, TRUE, NOW(), NOW()),
(5, 2, 5, 4, 10, 150000.00, 'INCOME', 'CNY', '2024-02-28', 'Large Solar Project', 'Bank Transfer', 'GE004', FALSE, TRUE, NOW(), NOW()),
(5, 2, 5, 4, 12, 25000.00, 'INCOME', 'CNY', '2024-03-05', 'Equipment Sales', 'Bank Transfer', 'GE005', FALSE, TRUE, NOW(), NOW()),
(5, 2, 5, 4, 10, 95000.00, 'INCOME', 'CNY', '2024-03-20', 'Renewable Energy Service', 'Bank Transfer', 'GE006', FALSE, TRUE, NOW(), NOW()),

-- Company 2 Expenses
(5, 2, 5, 4, 13, 40000.00, 'EXPENSE', 'CNY', '2024-01-05', 'Employee Salaries', 'Bank Transfer', 'GE-SAL001', TRUE, FALSE, NOW(), NOW()),
(5, 2, 6, 4, 13, 35000.00, 'EXPENSE', 'CNY', '2024-01-05', 'Operations Salaries', 'Bank Transfer', 'GE-SAL002', TRUE, FALSE, NOW(), NOW()),
(5, 2, 5, 4, 14, 12000.00, 'EXPENSE', 'CNY', '2024-01-15', 'Factory Rent', 'Bank Transfer', 'GE-RENT001', TRUE, FALSE, NOW(), NOW()),
(5, 2, 5, 4, 15, 8000.00, 'EXPENSE', 'CNY', '2024-01-20', 'Manufacturing Utilities', 'Bank Transfer', 'GE-UTIL001', FALSE, TRUE, NOW(), NOW()),
(5, 2, 6, 4, 16, 15000.00, 'EXPENSE', 'CNY', '2024-01-25', 'Raw Materials', 'Bank Transfer', 'GE-MAT001', FALSE, TRUE, NOW(), NOW()),

-- Company 3 - Finance Solutions Inc Transactions
(6, 3, 7, 6, 19, 60000.00, 'INCOME', 'CNY', '2024-01-12', 'Financial Consulting', 'Bank Transfer', 'FS001', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 19, 45000.00, 'INCOME', 'CNY', '2024-01-28', 'Investment Advisory', 'Bank Transfer', 'FS002', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 20, 35000.00, 'INCOME', 'CNY', '2024-02-10', 'Financial Software License', 'Bank Transfer', 'FS003', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 19, 55000.00, 'INCOME', 'CNY', '2024-02-25', 'Risk Management Service', 'Bank Transfer', 'FS004', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 21, 15000.00, 'INCOME', 'CNY', '2024-03-08', 'Training Revenue', 'Bank Transfer', 'FS005', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 19, 70000.00, 'INCOME', 'CNY', '2024-03-22', 'Strategic Planning Service', 'Bank Transfer', 'FS006', FALSE, TRUE, NOW(), NOW()),

-- Company 3 Expenses
(6, 3, 7, 6, 22, 25000.00, 'EXPENSE', 'CNY', '2024-01-05', 'Consultant Salaries', 'Bank Transfer', 'FS-SAL001', TRUE, FALSE, NOW(), NOW()),
(6, 3, 7, 6, 23, 6000.00, 'EXPENSE', 'CNY', '2024-01-10', 'Office Rent', 'Bank Transfer', 'FS-RENT001', TRUE, FALSE, NOW(), NOW()),
(6, 3, 7, 6, 24, 2000.00, 'EXPENSE', 'CNY', '2024-01-15', 'Office Utilities', 'Bank Transfer', 'FS-UTIL001', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 25, 3000.00, 'EXPENSE', 'CNY', '2024-01-20', 'Professional Development', 'Bank Transfer', 'FS-TRN001', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 26, 4000.00, 'EXPENSE', 'CNY', '2024-01-25', 'Marketing and Networking', 'Credit Card', 'FS-MKT001', FALSE, TRUE, NOW(), NOW()),
(6, 3, 7, 6, 27, 2500.00, 'EXPENSE', 'CNY', '2024-01-30', 'Business Travel', 'Credit Card', 'FS-TRV001', FALSE, TRUE, NOW(), NOW());

-- Insert Journal Entries (for accounting accuracy)
INSERT INTO Journal_Entry (company_id, entry_date, description, status, created_by, created_at, updated_at) VALUES
(1, '2024-01-15', 'Service Revenue Recognition - INV001', 'POSTED', 2, NOW(), NOW()),
(1, '2024-01-20', 'Service Revenue Recognition - INV002', 'POSTED', 2, NOW(), NOW()),
(1, '2024-01-05', 'Salary Payment - January', 'POSTED', 2, NOW(), NOW()),
(1, '2024-01-10', 'Office Rent Payment', 'POSTED', 2, NOW(), NOW()),
(2, '2024-01-10', 'Solar Panel Revenue - GE001', 'POSTED', 5, NOW(), NOW()),
(3, '2024-01-12', 'Consulting Revenue - FS001', 'POSTED', 6, NOW(), NOW());

-- Insert Journal Lines (Double-entry bookkeeping)
INSERT INTO Journal_Line (entry_id, account_id, description, debit_amount, credit_amount) VALUES
-- Entry 1: Service Revenue INV001
(1, 2, 'Accounts Receivable - INV001', 50000.00, 0.00),
(1, 12, 'Service Revenue - INV001', 0.00, 50000.00),
-- Entry 2: Service Revenue INV002  
(2, 2, 'Accounts Receivable - INV002', 75000.00, 0.00),
(2, 12, 'Service Revenue - INV002', 0.00, 75000.00),
-- Entry 3: Salary Payment
(3, 15, 'Salary Expense', 90000.00, 0.00),
(3, 1, 'Cash Payment', 0.00, 90000.00),
-- Entry 4: Rent Payment
(4, 16, 'Rent Expense', 8000.00, 0.00),
(4, 2, 'Bank Payment', 0.00, 8000.00),
-- Entry 5: Green Energy Revenue
(5, 20, 'Accounts Receivable', 120000.00, 0.00),
(5, 30, 'Service Revenue', 0.00, 120000.00),
-- Entry 6: Finance Solutions Revenue
(6, 38, 'Accounts Receivable', 60000.00, 0.00),
(6, 48, 'Consulting Revenue', 0.00, 60000.00);

-- Insert Account Balances (for Balance Sheet generation)
INSERT INTO account_balance (account_id, year, month, current_month, previous_month, last_year_end, as_of_date) VALUES
-- Company 1 - March 2024 balances
(1, 2024, 3, 150000.00, 120000.00, 80000.00, '2024-03-31'),  -- Cash
(2, 2024, 3, 285000.00, 200000.00, 150000.00, '2024-03-31'), -- Bank Account
(3, 2024, 3, 125000.00, 100000.00, 75000.00, '2024-03-31'),  -- Accounts Receivable
(4, 2024, 3, 50000.00, 55000.00, 60000.00, '2024-03-31'),    -- Inventory
(5, 2024, 3, 78400.00, 80000.00, 85000.00, '2024-03-31'),    -- Office Equipment
(6, 2024, 3, 192500.00, 195000.00, 200000.00, '2024-03-31'), -- Computer Equipment
(7, 2024, 3, 75000.00, 70000.00, 65000.00, '2024-03-31'),    -- Accounts Payable
(8, 2024, 3, 100000.00, 120000.00, 150000.00, '2024-03-31'), -- Short-term Loans
(9, 2024, 3, 200000.00, 200000.00, 200000.00, '2024-03-31'), -- Long-term Debt
(10, 2024, 3, 500000.00, 500000.00, 500000.00, '2024-03-31'), -- Owner Equity
(11, 2024, 3, 106900.00, 80000.00, 45000.00, '2024-03-31');  -- Retained Earnings

-- Update Department manager assignments
UPDATE Department SET manager_id = 2 WHERE department_id = 1; -- John Doe manages Finance
UPDATE Department SET manager_id = 3 WHERE department_id = 2; -- Jane Smith manages IT
UPDATE Department SET manager_id = 4 WHERE department_id = 3; -- Mike Wilson manages Sales
UPDATE Department SET manager_id = 5 WHERE department_id = 5; -- Sarah Brown manages Finance (Company 2)
UPDATE Department SET manager_id = 6 WHERE department_id = 7; -- David Lee manages Finance (Company 3)

-- ====================
-- 3. VERIFICATION QUERIES (Optional - for testing)
-- ====================

-- Verify data insertion
SELECT 'Companies' as Table_Name, COUNT(*) as Record_Count FROM Company
UNION ALL
SELECT 'Users', COUNT(*) FROM User
UNION ALL  
SELECT 'Departments', COUNT(*) FROM Department
UNION ALL
SELECT 'Accounts', COUNT(*) FROM Account
UNION ALL
SELECT 'Categories', COUNT(*) FROM Category
UNION ALL
SELECT 'Transactions', COUNT(*) FROM Transaction
UNION ALL
SELECT 'Fixed Assets', COUNT(*) FROM Fixed_Asset
UNION ALL
SELECT 'Funds', COUNT(*) FROM Fund
UNION ALL
SELECT 'Journal Entries', COUNT(*) FROM Journal_Entry
UNION ALL
SELECT 'Journal Lines', COUNT(*) FROM Journal_Line;

-- Sample queries for testing reports
-- Balance Sheet data for Company 1
SELECT 'BALANCE_SHEET_TEST' as report_type,
       a.account_type,
       COUNT(*) as account_count,
       COALESCE(SUM(ab.current_month), 0) as total_amount
FROM Account a
LEFT JOIN account_balance ab ON a.account_id = ab.account_id
WHERE a.company_id = 1 AND a.is_active = TRUE
GROUP BY a.account_type;

-- Income Statement data for Company 1
SELECT 'INCOME_STATEMENT_TEST' as report_type,
       t.transaction_type,
       c.name as category_name,
       COUNT(*) as transaction_count,
       SUM(t.amount) as total_amount
FROM Transaction t
JOIN Category c ON t.category_id = c.category_id
WHERE t.company_id = 1 
  AND t.transaction_date >= '2024-01-01' 
  AND t.transaction_date <= '2024-03-31'
GROUP BY t.transaction_type, c.name
ORDER BY t.transaction_type, total_amount DESC;