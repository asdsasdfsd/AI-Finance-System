// frontend/src/services/reportService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/reports';

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
      console.log('Sending report request:', reportRequest);
      const response = await axios.post(`${API_BASE_URL}/generate`, reportRequest, getAuthHeader());
      console.log('Report generation response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error generating report:', error);
      
      // Enhanced error handling
      if (error.response) {
        // Server responded with error status
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        // Request was made but no response received
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        // Something else happened
        throw new Error(error.message || 'Failed to generate report');
      }
    }
  }

  /**
   * Get report details by ID
   * @param {number} reportId - Report ID
   */
  async getReport(reportId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/${reportId}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching report:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to fetch report');
      }
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

      const response = await axios.get(`${API_BASE_URL}?${params.toString()}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching reports:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to fetch reports');
      }
    }
  }

  /**
   * Get recent reports
   * @param {number} limit - Maximum number of reports to return
   */
  async getRecentReports(limit = 10) {
    try {
      const response = await axios.get(`${API_BASE_URL}/recent?limit=${limit}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching recent reports:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to fetch recent reports');
      }
    }
  }

  /**
   * Get reports by type
   * @param {string} reportType - Report type
   */
  async getReportsByType(reportType) {
    try {
      const response = await axios.get(`${API_BASE_URL}/by-type/${reportType}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching reports by type:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to fetch reports by type');
      }
    }
  }

  /**
   * Download report file
   * @param {number} reportId - Report ID
   * @param {string} fileName - Optional custom file name
   */// frontend/src/services/reportService.js 中的 downloadReport 函数
  async downloadReport(reportId, fileName = null) {
    try {
      const authHeader = getAuthHeader();
      const response = await axios.get(`${API_BASE_URL}/${reportId}/download`, {
        responseType: 'blob',
        ...authHeader
      });

      // 检查响应的 Content-Type
      const contentType = response.headers['content-type'];
      console.log('Content-Type:', contentType);

      // 创建正确类型的 Blob
      const blob = new Blob([response.data], { 
        type: contentType || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      });
      
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      // 设置文件名，确保有正确的扩展名
      const contentDisposition = response.headers['content-disposition'];
      let downloadFileName = fileName;
      
      if (!downloadFileName && contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (fileNameMatch) {
          downloadFileName = fileNameMatch[1].replace(/['"]/g, '');
        }
      }
      
      if (!downloadFileName) {
        downloadFileName = `report_${reportId}.xlsx`;
      }

      // 确保文件名有正确的扩展名
      if (!downloadFileName.match(/\.(xlsx|xls)$/i)) {
        downloadFileName += '.xlsx';
      }

      link.setAttribute('download', downloadFileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      return { success: true, fileName: downloadFileName };
    } catch (error) {
      console.error('Error downloading report:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                          error.response.data?.error || 
                          `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to download report');
      }
    }
  }

  /**
   * Archive a report
   * @param {number} reportId - Report ID
   */
  async archiveReport(reportId) {
    try {
      const response = await axios.post(`${API_BASE_URL}/${reportId}/archive`, {}, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error archiving report:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to archive report');
      }
    }
  }

  /**
   * Delete a report
   * @param {number} reportId - Report ID
   */
  async deleteReport(reportId) {
    try {
      const response = await axios.delete(`${API_BASE_URL}/${reportId}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error deleting report:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to delete report');
      }
    }
  }

  /**
   * Get report statistics
   */
  async getReportStatistics() {
    try {
      const response = await axios.get(`${API_BASE_URL}/statistics`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching report statistics:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Server error: ${error.response.status}`;
        throw new Error(errorMessage);
      } else if (error.request) {
        throw new Error('Unable to connect to the server. Please check if the backend is running.');
      } else {
        throw new Error(error.message || 'Failed to fetch report statistics');
      }
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

  /**
   * Test backend connectivity by making a simple request
   */
  async testConnection() {
    try {
      // Use statistics endpoint instead of health for connectivity test
      const response = await axios.get(`${API_BASE_URL}/statistics`, getAuthHeader());
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Backend connection test failed:', error);
      
      if (error.response) {
        // Server is reachable but returned an error
        return { 
          success: false, 
          error: `Server responded with ${error.response.status}: ${error.response.data?.message || error.response.statusText}`,
          serverReachable: true
        };
      } else if (error.request) {
        // Server is not reachable
        return { 
          success: false, 
          error: 'Cannot connect to server. Please check if backend is running on localhost:8085',
          serverReachable: false
        };
      } else {
        return { 
          success: false, 
          error: error.message || 'Unknown connection error',
          serverReachable: false
        };
      }
    }
  }
}

export default new ReportService();