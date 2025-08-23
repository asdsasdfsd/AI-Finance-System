// frontend/src/services/unifiedReportService.js
// FIXED Unified Report Service - Handles all report API calls consistently

import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085';

/**
 * Get authentication header for API requests
 */
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

/**
 * Unified Report Service - Consistent API handling for all report types
 */
class UnifiedReportService {
  
  /**
   * Generate report preview data
   * @param {string} reportType - BALANCE_SHEET, INCOME_STATEMENT, INCOME_EXPENSE, FINANCIAL_GROUPING
   * @param {Object} params - Parameters for the report
   */
  async generateReportPreview(reportType, params) {
    try {
      console.log(`[UnifiedReportService] Generating ${reportType} preview with params:`, params);
      
      const endpoint = this.getPreviewEndpoint(reportType);
      const response = await axios.get(endpoint, {
        ...getAuthHeader(),
        params: params
      });
      
      console.log(`[UnifiedReportService] ${reportType} preview response:`, response.data);
      return response.data;
      
    } catch (error) {
      console.error(`[UnifiedReportService] Failed to generate ${reportType} preview:`, error);
      
      if (error.response) {
        throw new Error(`Server error: ${error.response.status} - ${error.response.data?.message || error.response.statusText}`);
      } else if (error.request) {
        throw new Error('Unable to connect to the backend server. Please check if it\'s running on port 8085.');
      } else {
        throw new Error(error.message || 'Failed to generate report preview');
      }
    }
  }

  /**
   * Get report data in JSON format
   * @param {string} reportType - Report type
   * @param {Object} params - Parameters for the report
   */
  async getReportJson(reportType, params) {
    try {
      console.log(`[UnifiedReportService] Getting ${reportType} JSON with params:`, params);
      
      const endpoint = this.getJsonEndpoint(reportType);
      const response = await axios.get(endpoint, {
        ...getAuthHeader(),
        params: params
      });
      
      console.log(`[UnifiedReportService] ${reportType} JSON response:`, response.data);
      return response.data;
      
    } catch (error) {
      console.error(`[UnifiedReportService] Failed to get ${reportType} JSON:`, error);
      throw this.handleError(error, reportType);
    }
  }

  /**
   * Export report as Excel file
   * @param {string} reportType - Report type
   * @param {Object} params - Parameters for the report
   */
  async exportReport(reportType, params) {
    try {
      console.log(`[UnifiedReportService] Exporting ${reportType} with params:`, params);
      
      const endpoint = this.getExportEndpoint(reportType);
      const response = await axios.get(endpoint, {
        ...getAuthHeader(),
        params: params,
        responseType: 'blob'
      });
      
      // Handle file download
      const blob = new Blob([response.data], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      });
      
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = this.generateFilename(reportType, params);
      document.body.appendChild(link);
      link.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(link);
      
      console.log(`[UnifiedReportService] ${reportType} exported successfully`);
      return { success: true, message: 'Report exported successfully' };
      
    } catch (error) {
      console.error(`[UnifiedReportService] Failed to export ${reportType}:`, error);
      throw this.handleError(error, reportType);
    }
  }

  /**
   * Get preview endpoint for report type
   */
  getPreviewEndpoint(reportType) {
    const endpoints = {
      'BALANCE_SHEET': `${API_BASE_URL}/api/balance-sheet/generate`,
      'INCOME_STATEMENT': `${API_BASE_URL}/api/income-statement/generate`,
      'INCOME_EXPENSE': `${API_BASE_URL}/api/income-expense/generate`, // FIXED: Handles both asOfDate and date range
      'FINANCIAL_GROUPING': `${API_BASE_URL}/api/financial-grouping/generate`,
      'FINANCIAL_REPORT': `${API_BASE_URL}/api/financial-report/generate`
    };
    
    return endpoints[reportType] || endpoints['FINANCIAL_REPORT'];
  }

  /**
   * Get JSON endpoint for report type
   */
  getJsonEndpoint(reportType) {
    const endpoints = {
      'BALANCE_SHEET': `${API_BASE_URL}/api/balance-sheet/json`,
      'INCOME_STATEMENT': `${API_BASE_URL}/api/income-statement/json`,
      'INCOME_EXPENSE': `${API_BASE_URL}/api/income-expense/json`,
      'FINANCIAL_GROUPING': `${API_BASE_URL}/api/financial-grouping/json`,
      'FINANCIAL_REPORT': `${API_BASE_URL}/api/financial-report/json`
    };
    
    return endpoints[reportType] || endpoints['FINANCIAL_REPORT'];
  }

  /**
   * Get export endpoint for report type
   */
  getExportEndpoint(reportType) {
    const endpoints = {
      'BALANCE_SHEET': `${API_BASE_URL}/api/balance-sheet/export`,
      'INCOME_STATEMENT': `${API_BASE_URL}/api/income-statement/export`,
      'INCOME_EXPENSE': `${API_BASE_URL}/api/income-expense/export`,
      'FINANCIAL_GROUPING': `${API_BASE_URL}/api/financial-grouping/export`,
      'FINANCIAL_REPORT': `${API_BASE_URL}/api/financial-report/export`
    };
    
    return endpoints[reportType] || endpoints['FINANCIAL_REPORT'];
  }

  /**
   * Generate filename for export
   */
  generateFilename(reportType, params) {
    const today = new Date().toISOString().split('T')[0];
    const companyId = params.companyId || 'unknown';
    
    const baseNames = {
      'BALANCE_SHEET': 'Balance_Sheet',
      'INCOME_STATEMENT': 'Income_Statement',
      'INCOME_EXPENSE': 'Income_Expense_Report',
      'FINANCIAL_GROUPING': 'Financial_Grouping',
      'FINANCIAL_REPORT': 'Financial_Report'
    };
    
    const baseName = baseNames[reportType] || 'Report';
    
    if (params.asOfDate) {
      return `${baseName}_${companyId}_${params.asOfDate}.xlsx`;
    } else if (params.startDate && params.endDate) {
      return `${baseName}_${companyId}_${params.startDate}_to_${params.endDate}.xlsx`;
    } else {
      return `${baseName}_${companyId}_${today}.xlsx`;
    }
  }

  /**
   * Handle API errors consistently
   */
  handleError(error, reportType) {
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.message || error.response.statusText;
      
      if (status === 401) {
        return new Error('Authentication required. Please login again.');
      } else if (status === 403) {
        return new Error('Access denied. You don\'t have permission to access this report.');
      } else if (status === 404) {
        return new Error(`${reportType} endpoint not found. Please check backend configuration.`);
      } else {
        return new Error(`Server error (${status}): ${message}`);
      }
    } else if (error.request) {
      return new Error('Unable to connect to backend server. Please check if it\'s running on port 8085.');
    } else {
      return new Error(error.message || `Failed to process ${reportType} request`);
    }
  }

  /**
   * Convert backend data to frontend table format
   */
  convertToTableData(reportType, backendData) {
    try {
      console.log(`[UnifiedReportService] Converting ${reportType} data:`, backendData);
      
      // If the response already has a 'data' property, use it
      if (backendData.data && Array.isArray(backendData.data)) {
        return backendData.data;
      }
      
      // If the response is an array, use it directly
      if (Array.isArray(backendData)) {
        return backendData.map(item => ({
          key: item.id || item.key || Math.random().toString(36).substr(2, 9),
          ...item
        }));
      }
      
      // Convert specific report types
      switch (reportType) {
        case 'BALANCE_SHEET':
          return this.convertBalanceSheetData(backendData);
        case 'INCOME_STATEMENT':
          return this.convertIncomeStatementData(backendData);
        case 'INCOME_EXPENSE':
          return this.convertIncomeExpenseData(backendData);
        case 'FINANCIAL_GROUPING':
          return this.convertFinancialGroupingData(backendData);
        default:
          console.warn(`[UnifiedReportService] Unknown report type: ${reportType}`);
          return [];
      }
    } catch (error) {
      console.error(`[UnifiedReportService] Data conversion failed for ${reportType}:`, error);
      return [];
    }
  }

  convertBalanceSheetData(data) {
    const result = [];
    
    // Assets
    if (data.assets) {
      data.assets.forEach(asset => {
        result.push({
          key: `asset_${asset.accountId || Math.random()}`,
          Account: asset.accountName || asset.name,
          Amount: asset.balance || asset.amount || 0,
          Type: 'Asset'
        });
      });
    }
    
    // Liabilities
    if (data.liabilities) {
      data.liabilities.forEach(liability => {
        result.push({
          key: `liability_${liability.accountId || Math.random()}`,
          Account: liability.accountName || liability.name,
          Amount: liability.balance || liability.amount || 0,
          Type: 'Liability'
        });
      });
    }
    
    // Equity
    if (data.equity) {
      data.equity.forEach(equity => {
        result.push({
          key: `equity_${equity.accountId || Math.random()}`,
          Account: equity.accountName || equity.name,
          Amount: equity.balance || equity.amount || 0,
          Type: 'Equity'
        });
      });
    }
    
    return result;
  }

  convertIncomeStatementData(data) {
    const result = [];
    
    // Revenues
    if (data.revenues) {
      data.revenues.forEach(revenue => {
        result.push({
          key: `revenue_${revenue.name?.hashCode() || Math.random()}`,
          Account: revenue.name,
          Amount: revenue.amount || 0,
          Category: 'Revenue'
        });
      });
    }
    
    // Expenses
    if (data.expenses) {
      data.expenses.forEach(expense => {
        result.push({
          key: `expense_${expense.name?.hashCode() || Math.random()}`,
          Account: expense.name,
          Amount: expense.amount || 0,
          Category: 'Expense'
        });
      });
    }
    
    return result;
  }

  convertIncomeExpenseData(data) {
    // Handle different response formats from the fixed backend
    if (data.data && Array.isArray(data.data)) {
      // New format: {data: [...], totalIncome: x, totalExpenses: y}
      return data.data.map(item => ({
        key: item.key || `income_expense_${Math.random()}`,
        Account: item.Account || item.description || item.account || item.name,
        Amount: item.Amount || item.amount || 0,
        Type: item.Type || item.type || 'Unknown',
        Category: item.Category || item.category || 'Uncategorized',
        PreviousMonth: item.PreviousMonth || item.previousMonth,
        YearToDate: item.YearToDate || item.yearToDate
      }));
    } else if (Array.isArray(data)) {
      // Legacy format: direct array
      return data.map(item => ({
        key: `income_expense_${item.id || Math.random()}`,
        Account: item.description || item.account || item.name,
        Amount: item.currentMonth || item.amount || 0,
        Type: item.type || 'Unknown',
        Category: item.category || 'Uncategorized',
        PreviousMonth: item.previousMonth,
        YearToDate: item.yearToDate
      }));
    }
    return [];
  }

  convertFinancialGroupingData(data) {
    console.log('[UnifiedReportService] Converting financial grouping data:', data);
    
    // Handle direct array format (legacy)
    if (Array.isArray(data)) {
      return data.map(item => ({
        key: `grouping_${item.id || Math.random()}`,
        Category: item.category || item.group || item.name,
        Amount: item.amount || item.total || 0,
        Count: item.count || item.transactions || 0,
        Percentage: item.percentage || '0%'
      }));
    }
    
    // Handle object structure from backend (actual controller response)
    if (data && typeof data === 'object') {
      const result = [];
      
      // Process categoryGrouping array (based on actual backend field names)
      if (data.categoryGrouping && Array.isArray(data.categoryGrouping)) {
        console.log('[UnifiedReportService] Processing categoryGrouping:', data.categoryGrouping.length, 'items');
        data.categoryGrouping.forEach((item, index) => {
          result.push({
            key: `category_${item.id || index}`,
            Type: 'Category',
            Name: item.category || `Category ${index + 1}`,  // Ensure name is never empty
            Amount: this.formatCurrency(item.totalAmount || 0),
            Count: item.transactionCount || 0,
            Percentage: this.formatPercentage(item.percentage),
            CategoryType: item.type || 'Unknown'
          });
        });
      }
      
      // Process departmentGrouping array (based on actual backend field names) 
      if (data.departmentGrouping && Array.isArray(data.departmentGrouping)) {
        console.log('[UnifiedReportService] Processing departmentGrouping:', data.departmentGrouping.length, 'items');
        data.departmentGrouping.forEach((item, index) => {
          result.push({
            key: `department_${item.id || index}`,
            Type: 'Department',
            Name: item.department || `Department ${index + 1}`,  // Ensure name is never empty
            Amount: this.formatCurrency(item.actualSpent || item.totalAmount || 0),
            Count: item.transactionCount || 0,
            Budget: this.formatCurrency(item.budgetAllocated || 0),
            Utilization: this.formatPercentage(item.budgetUtilization)
          });
        });
      }
      
      // Process transactionTypeGrouping array (based on actual backend field names)
      if (data.transactionTypeGrouping && Array.isArray(data.transactionTypeGrouping)) {
        console.log('[UnifiedReportService] Processing transactionTypeGrouping:', data.transactionTypeGrouping.length, 'items');
        data.transactionTypeGrouping.forEach((item, index) => {
          result.push({
            key: `type_${item.id || index}`,
            Type: 'Transaction Type',
            Name: item.transactionType || `Type ${index + 1}`,  // Ensure name is never empty
            Amount: this.formatCurrency(item.totalAmount || 0),
            Count: item.transactionCount || 0,
            Percentage: this.formatPercentage(item.percentage),
            Average: this.formatCurrency(item.averageAmount || 0)
          });
        });
      }
      
      // Process monthlyTrend array (backend uses 'monthlyTrend' not 'monthlyGrouping')
      if (data.monthlyTrend && Array.isArray(data.monthlyTrend)) {
        console.log('[UnifiedReportService] Processing monthlyTrend:', data.monthlyTrend.length, 'items');
        data.monthlyTrend.forEach((item, index) => {
          result.push({
            key: `month_${item.id || index}`,
            Type: 'Monthly',
            Name: item.month || `Month ${index + 1}`,
            Amount: this.formatCurrency((parseFloat(item.income) || 0) + (parseFloat(item.expenses) || 0)),
            Count: item.transactionCount || 0,
            Income: this.formatCurrency(item.income || 0),     // backend uses 'income'
            Expense: this.formatCurrency(item.expenses || 0), // backend uses 'expenses'
            Net: this.formatCurrency(item.netIncome || 0)     // backend uses 'netIncome'
          });
        });
      }
      
      console.log('[UnifiedReportService] Financial grouping conversion result:', result.length, 'items');
      return result;
    }
    
    console.log('[UnifiedReportService] No valid financial grouping data found');
    return [];
  }

  /**
   * Health check for all report services
   */
  async healthCheck() {
    const services = ['balance-sheet', 'income-statement', 'income-expense', 'financial-grouping', 'financial-report'];
    const results = {};
    
    for (const service of services) {
      try {
        const response = await axios.get(`${API_BASE_URL}/api/${service}/health`, getAuthHeader());
        results[service] = { status: 'OK', message: response.data };
      } catch (error) {
        results[service] = { status: 'ERROR', message: error.message };
      }
    }
    
    return results;
  }

  // Helper method to format currency values
formatCurrency(value) {
  if (typeof value === 'number') {
    return value;
  }
  if (typeof value === 'string') {
    return parseFloat(value) || 0;
  }
  return 0;
}

// Helper method to format percentage values
formatPercentage(value) {
  if (!value) return '0%';
  if (typeof value === 'string' && value.includes('%')) {
    return value;
  }
  const num = parseFloat(value) || 0;
  return `${num.toFixed(1)}%`;
}
}

export default new UnifiedReportService();