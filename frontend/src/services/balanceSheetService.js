// frontend/src/services/balanceSheetService.js
import axios from 'axios';

const API_BASE_URL = '/api/balance-sheet';

/**
 * Balance Sheet Service - Frontend service for legacy balance sheet functionality
 * 
 * Handles communication with the traditional balance sheet endpoints
 * Note: This service is maintained for compatibility with existing functionality
 */
class BalanceSheetService {

  /**
   * Get balance sheet data in JSON format
   * @param {number} companyId - Company ID
   * @param {string} asOfDate - Date in YYYY-MM-DD format
   */
  async getBalanceSheetJson(companyId, asOfDate) {
    try {
      const response = await axios.get(`${API_BASE_URL}/json`, {
        params: {
          companyId,
          asOfDate
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching balance sheet data:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch balance sheet data');
    }
  }

  /**
   * Get balance sheet data in text format
   * @param {number} companyId - Company ID
   * @param {string} asOfDate - Date in YYYY-MM-DD format
   */
  async getBalanceSheetText(companyId, asOfDate) {
    try {
      const response = await axios.get(`${API_BASE_URL}/text`, {
        params: {
          companyId,
          asOfDate
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching balance sheet text:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch balance sheet text');
    }
  }

  /**
   * Export balance sheet to Excel
   * @param {number} companyId - Company ID
   * @param {string} asOfDate - Date in YYYY-MM-DD format
   * @param {string} fileName - Optional custom file name
   */
  async exportBalanceSheet(companyId, asOfDate, fileName = null) {
    try {
      const response = await axios.get(`${API_BASE_URL}/export`, {
        params: {
          companyId,
          asOfDate
        },
        responseType: 'blob'
      });

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;

      // Set filename
      const downloadFileName = fileName || `BalanceSheet_${asOfDate}.xlsx`;
      link.setAttribute('download', downloadFileName);
      
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      return { success: true, fileName: downloadFileName };
    } catch (error) {
      console.error('Error exporting balance sheet:', error);
      throw new Error(error.response?.data?.message || 'Failed to export balance sheet');
    }
  }

  /**
   * Validate balance sheet request parameters
   * @param {number} companyId - Company ID
   * @param {string} asOfDate - Date string
   */
  validateRequest(companyId, asOfDate) {
    const errors = [];

    if (!companyId || companyId <= 0) {
      errors.push('Valid company ID is required');
    }

    if (!asOfDate) {
      errors.push('As of date is required');
    } else {
      const date = new Date(asOfDate);
      if (isNaN(date.getTime())) {
        errors.push('Invalid date format');
      } else if (date > new Date()) {
        errors.push('As of date cannot be in the future');
      }
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Format balance sheet data for display
   * @param {Object} balanceSheetData - Raw balance sheet data
   */
  formatBalanceSheetData(balanceSheetData) {
    if (!balanceSheetData) return null;

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'CNY',
        minimumFractionDigits: 2
      }).format(amount);
    };

    const formatSection = (sectionData) => {
      const formatted = {};
      Object.keys(sectionData).forEach(category => {
        formatted[category] = sectionData[category].map(account => ({
          ...account,
          currentMonthFormatted: formatCurrency(account.currentMonth),
          previousMonthFormatted: formatCurrency(account.previousMonth),
          lastYearEndFormatted: formatCurrency(account.lastYearEnd)
        }));
      });
      return formatted;
    };

    return {
      ...balanceSheetData,
      assets: formatSection(balanceSheetData.assets || {}),
      liabilities: formatSection(balanceSheetData.liabilities || {}),
      equity: formatSection(balanceSheetData.equity || {}),
      totalAssetsFormatted: formatCurrency(balanceSheetData.totalAssets || 0),
      totalLiabilitiesFormatted: formatCurrency(balanceSheetData.totalLiabilities || 0),
      totalEquityFormatted: formatCurrency(balanceSheetData.totalEquity || 0)
    };
  }

  /**
   * Get balance sheet summary statistics
   * @param {Object} balanceSheetData - Balance sheet data
   */
  getBalanceSheetSummary(balanceSheetData) {
    if (!balanceSheetData) return null;

    const totalAssets = balanceSheetData.totalAssets || 0;
    const totalLiabilities = balanceSheetData.totalLiabilities || 0;
    const totalEquity = balanceSheetData.totalEquity || 0;
    const totalLiabilitiesAndEquity = totalLiabilities + totalEquity;

    return {
      totalAssets,
      totalLiabilities,
      totalEquity,
      totalLiabilitiesAndEquity,
      isBalanced: balanceSheetData.isBalanced || false,
      balanceDifference: Math.abs(totalAssets - totalLiabilitiesAndEquity),
      debtToEquityRatio: totalEquity !== 0 ? (totalLiabilities / totalEquity).toFixed(2) : 'N/A',
      assetCount: this.countAccounts(balanceSheetData.assets || {}),
      liabilityCount: this.countAccounts(balanceSheetData.liabilities || {}),
      equityCount: this.countAccounts(balanceSheetData.equity || {})
    };
  }

  /**
   * Count total number of accounts in a section
   * @param {Object} section - Balance sheet section data
   */
  countAccounts(section) {
    return Object.values(section).reduce((total, accounts) => total + accounts.length, 0);
  }

  /**
   * Convert balance sheet data to table format for display
   * @param {Object} sectionData - Section data (assets, liabilities, or equity)
   * @param {string} sectionName - Name of the section
   */
  convertToTableData(sectionData, sectionName) {
    const tableData = [];
    let rowKey = 0;

    Object.entries(sectionData).forEach(([category, accounts]) => {
      // Add category header row
      tableData.push({
        key: `${sectionName}-category-${rowKey++}`,
        accountName: category,
        currentMonth: null,
        previousMonth: null,
        lastYearEnd: null,
        isCategory: true
      });

      // Add account rows
      accounts.forEach(account => {
        tableData.push({
          key: `${sectionName}-account-${rowKey++}`,
          accountName: `  ${account.accountName}`,
          currentMonth: account.currentMonth,
          previousMonth: account.previousMonth,
          lastYearEnd: account.lastYearEnd,
          isCategory: false
        });
      });
    });

    return tableData;
  }
}

export default new BalanceSheetService();