-- backend/src/test/resources/data-test.sql
-- Test data for unit and integration tests

-- Test Companies
INSERT INTO Company (company_id, company_name, registration_number, address, contact_email, phone_number, website, is_active, created_at, updated_at) 
VALUES 
(999, 'Test Company 1', 'TEST999', 'Test Address 1', 'test1@company.com', '123-456-7890', 'www.testcompany1.com', TRUE, NOW(), NOW()),
(998, 'Test Company 2', 'TEST998', 'Test Address 2', 'test2@company.com', '123-456-7891', 'www.testcompany2.com', TRUE, NOW(), NOW()),
(997, 'Inactive Test Company', 'TEST997', 'Test Address 3', 'test3@company.com', '123-456-7892', 'www.testcompany3.com', FALSE, NOW(), NOW());

-- Test Users
INSERT INTO User (user_id, username, password_hash, email, full_name, phone_number, is_active, external_id, created_at, updated_at)
VALUES 
(1, 'testuser1', '$2a$10$password1', 'testuser1@test.com', 'Test User 1', '123-456-7890', TRUE, 'test-external-1', NOW(), NOW()),
(2, 'testuser2', '$2a$10$password2', 'testuser2@test.com', 'Test User 2', '123-456-7891', TRUE, 'test-external-2', NOW(), NOW()),
(3, 'testmanager', '$2a$10$password3', 'testmanager@test.com', 'Test Manager', '123-456-7892', TRUE, 'test-external-3', NOW(), NOW()),
(4, 'inactive_user', '$2a$10$password4', 'inactive@test.com', 'Inactive User', '123-456-7893', FALSE, 'test-external-4', NOW(), NOW());

-- Test Roles
INSERT INTO Role (role_id, role_name, description, is_active, created_at, updated_at)
VALUES 
(1, 'ADMIN', 'Administrator role for testing', TRUE, NOW(), NOW()),
(2, 'USER', 'Regular user role for testing', TRUE, NOW(), NOW()),
(3, 'MANAGER', 'Manager role for testing', TRUE, NOW(), NOW());

-- Test User Roles
INSERT INTO User_Role (user_id, role_id, assigned_at, assigned_by)
VALUES 
(1, 2, NOW(), 1), -- testuser1 is USER
(2, 2, NOW(), 1), -- testuser2 is USER  
(3, 3, NOW(), 1); -- testmanager is MANAGER

-- Test Departments
INSERT INTO Department (department_id, company_id, name, code, budget, is_active, manager_id, parent_department_id, created_at, updated_at)
VALUES 
(1, 999, 'Test Department 1', 'DEPT001', 100000.00, TRUE, 3, NULL, NOW(), NOW()),
(2, 999, 'Test Department 2', 'DEPT002', 50000.00, TRUE, NULL, 1, NOW(), NOW()),
(3, 998, 'Test Department 3', 'DEPT003', 75000.00, TRUE, NULL, NULL, NOW(), NOW()),
(4, 999, 'Inactive Department', 'DEPT004', 25000.00, FALSE, NULL, NULL, NOW(), NOW());

-- Test Accounts
INSERT INTO Account (account_id, company_id, account_code, name, account_type, balance_direction, parent_account_id, is_active, created_at, updated_at)
VALUES 
-- Assets for Company 999
(1, 999, '1001', 'Test Cash Account', 'ASSET', 'DEBIT', NULL, TRUE, NOW(), NOW()),
(2, 999, '1002', 'Test Bank Account', 'ASSET', 'DEBIT', NULL, TRUE, NOW(), NOW()),
-- Liabilities for Company 999
(3, 999, '2001', 'Test Accounts Payable', 'LIABILITY', 'CREDIT', NULL, TRUE, NOW(), NOW()),
-- Equity for Company 999
(4, 999, '3001', 'Test Owner Equity', 'EQUITY', 'CREDIT', NULL, TRUE, NOW(), NOW()),
-- Revenue for Company 999
(5, 999, '4001', 'Test Service Revenue', 'REVENUE', 'CREDIT', NULL, TRUE, NOW(), NOW()),
(6, 999, '4002', 'Test Product Revenue', 'REVENUE', 'CREDIT', NULL, TRUE, NOW(), NOW()),
-- Expenses for Company 999
(7, 999, '5001', 'Test Operating Expenses', 'EXPENSE', 'DEBIT', NULL, TRUE, NOW(), NOW()),
(8, 999, '5002', 'Test Administrative Expenses', 'EXPENSE', 'DEBIT', NULL, TRUE, NOW(), NOW());

-- Test Categories
INSERT INTO Category (category_id, company_id, name, type, account_id, is_active, created_at, updated_at)
VALUES 
-- Income Categories for Company 999
(1, 999, 'Service Income', 'INCOME', 5, TRUE, NOW(), NOW()),
(2, 999, 'Product Sales', 'INCOME', 6, TRUE, NOW(), NOW()),
-- Expense Categories for Company 999
(3, 999, 'Office Expenses', 'EXPENSE', 7, TRUE, NOW(), NOW()),
(4, 999, 'Administrative Costs', 'EXPENSE', 8, TRUE, NOW(), NOW()),
-- Categories for Company 998
(5, 998, 'Consulting Income', 'INCOME', NULL, TRUE, NOW(), NOW()),
(6, 998, 'Travel Expenses', 'EXPENSE', NULL, TRUE, NOW(), NOW());

-- Test Funds
INSERT INTO Fund (fund_id, company_id, name, description, fund_type, balance, is_active, created_at, updated_at)
VALUES 
(1, 999, 'Test Operating Fund', 'Main operating fund for testing', 'OPERATING', 500000.00, TRUE, NOW(), NOW()),
(2, 999, 'Test Investment Fund', 'Investment fund for testing', 'INVESTMENT', 200000.00, TRUE, NOW(), NOW()),
(3, 998, 'Test Project Fund', 'Project fund for testing', 'PROJECT', 100000.00, TRUE, NOW(), NOW());

-- Test Fixed Assets
INSERT INTO Fixed_Asset (asset_id, company_id, department_id, name, description, acquisition_date, acquisition_cost, current_value, accumulated_depreciation, location, serial_number, status, created_at, updated_at)
VALUES 
(1, 999, 1, 'Test Computer', 'Test desktop computer', '2024-01-01', 2000.00, 1800.00, 200.00, 'Office A', 'COMP001', 'ACTIVE', NOW(), NOW()),
(2, 999, 1, 'Test Printer', 'Test laser printer', '2024-02-01', 800.00, 720.00, 80.00, 'Office A', 'PRINT001', 'ACTIVE', NOW(), NOW()),
(3, 998, 3, 'Test Vehicle', 'Test company vehicle', '2024-01-15', 25000.00, 22000.00, 3000.00, 'Parking Lot', 'VEH001', 'ACTIVE', NOW(), NOW()),
(4, 999, 2, 'Disposed Asset', 'Asset for disposal testing', '2023-01-01', 1000.00, 0.00, 1000.00, 'Storage', 'DISP001', 'DISPOSED', NOW(), NOW());

-- Test Fiscal Periods
INSERT INTO Fiscal_Period (period_id, company_id, period_name, period_type, start_date, end_date, status, created_at, updated_at)
VALUES 
(1, 999, '2024', 'ANNUAL', '2024-01-01', '2024-12-31', 'ACTIVE', NOW(), NOW()),
(2, 999, '2024-Q1', 'QUARTERLY', '2024-01-01', '2024-03-31', 'CLOSED', NOW(), NOW()),
(3, 999, '2024-Q2', 'QUARTERLY', '2024-04-01', '2024-06-30', 'ACTIVE', NOW(), NOW()),
(4, 998, '2024', 'ANNUAL', '2024-01-01', '2024-12-31', 'ACTIVE', NOW(), NOW());

-- Test Budgets
INSERT INTO Budget (budget_id, company_id, department_id, fiscal_period_id, budget_name, total_amount, status, created_at, updated_at)
VALUES 
(1, 999, 1, 1, 'Department 1 Annual Budget', 100000.00, 'APPROVED', NOW(), NOW()),
(2, 999, 2, 1, 'Department 2 Annual Budget', 50000.00, 'DRAFT', NOW(), NOW()),
(3, 998, 3, 4, 'Department 3 Annual Budget', 75000.00, 'APPROVED', NOW(), NOW());

-- Test Budget Lines
INSERT INTO Budget_Line (budget_line_id, budget_id, account_id, category_id, budgeted_amount, actual_amount, variance, created_at, updated_at)
VALUES 
(1, 1, 7, 3, 60000.00, 55000.00, 5000.00, NOW(), NOW()),
(2, 1, 8, 4, 40000.00, 35000.00, 5000.00, NOW(), NOW()),
(3, 2, 7, 3, 30000.00, 25000.00, 5000.00, NOW(), NOW()),
(4, 3, NULL, 5, 50000.00, 45000.00, 5000.00, NOW(), NOW());

-- Test Transactions (for TransactionAggregate testing)
-- Note: These will be managed by the application, but included for integration tests
INSERT INTO Transaction (transaction_id, company_id, amount, currency, description, transaction_type, transaction_date, category_id, department_id, fund_id, status, created_by, created_at, updated_at)
VALUES 
(1, 999, 5000.00, 'CNY', 'Test income transaction', 'INCOME', '2024-06-01', 1, 1, 1, 'APPROVED', 1, NOW(), NOW()),
(2, 999, 2000.00, 'CNY', 'Test expense transaction', 'EXPENSE', '2024-06-02', 3, 1, 1, 'APPROVED', 1, NOW(), NOW()),
(3, 999, 1500.00, 'CNY', 'Pending income transaction', 'INCOME', '2024-06-03', 2, 1, 1, 'PENDING', 2, NOW(), NOW()),
(4, 998, 3000.00, 'CNY', 'Company 2 transaction', 'INCOME', '2024-06-01', 5, 3, 3, 'APPROVED', 1, NOW(), NOW()),
(5, 999, 800.00, 'CNY', 'Cancelled transaction', 'EXPENSE', '2024-06-01', 4, 2, 1, 'CANCELLED', 1, NOW(), NOW());

-- Test Journal Entries
INSERT INTO Journal_Entry (entry_id, company_id, entry_number, entry_date, description, reference_type, reference_id, total_debit, total_credit, status, created_by, created_at, updated_at)
VALUES 
(1, 999, 'JE001', '2024-06-01', 'Test journal entry 1', 'TRANSACTION', 1, 5000.00, 5000.00, 'POSTED', 1, NOW(), NOW()),
(2, 999, 'JE002', '2024-06-02', 'Test journal entry 2', 'TRANSACTION', 2, 2000.00, 2000.00, 'POSTED', 1, NOW(), NOW());

-- Test Journal Lines
INSERT INTO Journal_Line (line_id, journal_entry_id, account_id, description, debit_amount, credit_amount, created_at, updated_at)
VALUES 
-- Journal Entry 1 lines (Income transaction)
(1, 1, 1, 'Cash receipt from service', 5000.00, 0.00, NOW(), NOW()),
(2, 1, 5, 'Service revenue recognition', 0.00, 5000.00, NOW(), NOW()),
-- Journal Entry 2 lines (Expense transaction)
(3, 2, 7, 'Office expense payment', 2000.00, 0.00, NOW(), NOW()),
(4, 2, 1, 'Cash payment for expenses', 0.00, 2000.00, NOW(), NOW());

-- Test Audit Logs
INSERT INTO Audit_Log (log_id, user_id, action, entity_type, entity_id, description, ip_address, user_agent, created_at)
VALUES 
(1, 1, 'CREATE', 'Transaction', '1', 'Created income transaction', '127.0.0.1', 'Test Agent', NOW()),
(2, 1, 'UPDATE', 'Transaction', '1', 'Updated transaction description', '127.0.0.1', 'Test Agent', NOW()),
(3, 1, 'APPROVE', 'Transaction', '1', 'Approved transaction', '127.0.0.1', 'Test Agent', NOW()),
(4, 2, 'CREATE', 'Transaction', '3', 'Created pending transaction', '127.0.0.1', 'Test Agent', NOW());