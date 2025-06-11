// frontend/src/services/reportService.js
import axios from 'axios';

const API_BASE_URL = '/api/reports';

/**
 * Report Service - Frontend service for DDD-based report generation
 * 
 * Handles communication with the backend DDD report system
 */
class ReportService {
  
  /**
   * Generate a new report
   * @param {Object} reportRequest - Report generation request
   * @param {string} reportRequest.reportType - BALANCE_SHEET, INCOME_STATEMENT, INCOME_EXPENSE, FINANCIAL_GROUPING
   * @param {string} reportRequest.reportName - Display name for the report
   * @param {string} reportRequest.startDate - Start date (YYYY-MM-DD)
   * @param {string} reportRequest.endDate - End date (YYYY-MM-DD)
   * @param {boolean} reportRequest.aiAnalysisEnabled - Whether to enable AI analysis
   */
  async generateReport(reportRequest) {
    try {
      const response = await axios.post(`${API_BASE_URL}/generate`, reportRequest);
      return response.data;
    } catch (error) {
      console.error('Error generating report:', error);
      throw new Error(error.response?.data?.message || 'Failed to generate report');
    }
  }

  /**
   * Get report details by ID
   * @param {number} reportId - Report ID
   */
  async getReport(reportId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/${reportId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching report:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch report');
    }
  }

  /**
   * Get list of reports with optional filtering
   * @param {Object} filters - Optional filters
   * @param {string} filters.reportType - Filter by report type
   * @param {string} filters.status - Filter by status
   * @param {string} filters.startDate - Filter by start date
   * @param {string} filters.endDate - Filter by end date
   * @param {string} filters.searchTerm - Search term
   * @param {number} filters.page - Page number (0-based)
   * @param {number} filters.size - Page size
   */
  async getReports(filters = {}) {
    try {
      const params = new URLSearchParams();
      
      Object.keys(filters).forEach(key => {
        if (filters[key] !== null && filters[key] !== undefined && filters[key] !== '') {
          params.append(key, filters[key]);
        }
      });

      const response = await axios.get(`${API_BASE_URL}?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching reports:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch reports');
    }
  }

  /**
   * Get recent reports
   * @param {number} limit - Maximum number of reports to return
   */
  async getRecentReports(limit = 10) {
    try {
      const response = await axios.get(`${API_BASE_URL}/recent?limit=${limit}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching recent reports:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch recent reports');
    }
  }

  /**
   * Get reports by type
   * @param {string} reportType - Report type
   */
  async getReportsByType(reportType) {
    try {
      const response = await axios.get(`${API_BASE_URL}/by-type/${reportType}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching reports by type:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch reports by type');
    }
  }

  /**
   * Download report file
   * @param {number} reportId - Report ID
   * @param {string} fileName - Optional custom file name
   */
  async downloadReport(reportId, fileName = null) {
    try {
      const response = await axios.get(`${API_BASE_URL}/${reportId}/download`, {
        responseType: 'blob'
      });

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;

      // Set filename
      const contentDisposition = response.headers['content-disposition'];
      let downloadFileName = fileName;
      
      if (!downloadFileName && contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="(.+)"/);
        if (fileNameMatch) {
          downloadFileName = fileNameMatch[1];
        }
      }
      
      if (!downloadFileName) {
        downloadFileName = `report_${reportId}.xlsx`;
      }

      link.setAttribute('download', downloadFileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      return { success: true, fileName: downloadFileName };
    } catch (error) {
      console.error('Error downloading report:', error);
      throw new Error(error.response?.data?.message || 'Failed to download report');
    }
  }

  /**
   * Archive a report
   * @param {number} reportId - Report ID
   */
  async archiveReport(reportId) {
    try {
      const response = await axios.post(`${API_BASE_URL}/${reportId}/archive`);
      return response.data;
    } catch (error) {
      console.error('Error archiving report:', error);
      throw new Error(error.response?.data?.message || 'Failed to archive report');
    }
  }

  /**
   * Delete a report
   * @param {number} reportId - Report ID
   */
  async deleteReport(reportId) {
    try {
      const response = await axios.delete(`${API_BASE_URL}/${reportId}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting report:', error);
      throw new Error(error.response?.data?.message || 'Failed to delete report');
    }
  }

  /**
   * Get report statistics
   */
  async getReportStatistics() {
    try {
      const response = await axios.get(`${API_BASE_URL}/statistics`);
      return response.data;
    } catch (error) {
      console.error('Error fetching report statistics:', error);
      throw new Error(error.response?.data?.message || 'Failed to fetch report statistics');
    }
  }

  /**
   * Get available report types
   */
  getReportTypes() {
    return [
      { value: 'BALANCE_SHEET', label: 'Balance Sheet', description: 'Assets, Liabilities, and Equity' },
      { value: 'INCOME_STATEMENT', label: 'Income Statement', description: 'Revenue and Expenses' },
      { value: 'INCOME_EXPENSE', label: 'Income vs Expense Report', description: 'Detailed Income and Expense Analysis' },
      { value: 'FINANCIAL_GROUPING', label: 'Financial Grouping Report', description: 'Transactions grouped by various criteria' }
    ];
  }

  /**
   * Get available report statuses
   */
  getReportStatuses() {
    return [
      { value: 'GENERATING', label: 'Generating', color: 'processing' },
      { value: 'COMPLETED', label: 'Completed', color: 'success' },
      { value: 'FAILED', label: 'Failed', color: 'error' },
      { value: 'ARCHIVED', label: 'Archived', color: 'default' }
    ];
  }

  /**
   * Validate report generation request
   * @param {Object} request - Report request to validate
   */
  validateReportRequest(request) {
    const errors = [];

    if (!request.reportType) {
      errors.push('Report type is required');
    }

    if (!request.reportName || request.reportName.trim().length === 0) {
      errors.push('Report name is required');
    }

    if (!request.startDate) {
      errors.push('Start date is required');
    }

    if (!request.endDate) {
      errors.push('End date is required');
    }

    if (request.startDate && request.endDate) {
      const startDate = new Date(request.startDate);
      const endDate = new Date(request.endDate);
      
      if (startDate > endDate) {
        errors.push('Start date cannot be after end date');
      }

      if (endDate > new Date()) {
        errors.push('End date cannot be in the future');
      }
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Format report period for display
   * @param {string} startDate - Start date
   * @param {string} endDate - End date
   */
  formatReportPeriod(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    const formatOptions = { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    };
    
    return `${start.toLocaleDateString('en-US', formatOptions)} - ${end.toLocaleDateString('en-US', formatOptions)}`;
  }
}

export default new ReportService();