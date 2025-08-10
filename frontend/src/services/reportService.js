// frontend/src/services/reportService.js
// FIXED ReportService with proper delete and management functionality

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
 * Report Service - Frontend service for DDD-based report management
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
      console.log('[ReportService] Sending report request:', reportRequest);
      const response = await axios.post(`${API_BASE_URL}/api/reports/generate`, reportRequest, getAuthHeader());
      console.log('[ReportService] Report generation response:', response.data);
      return response.data;
    } catch (error) {
      console.error('[ReportService] Error generating report:', error);
      
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
      const response = await axios.get(`${API_BASE_URL}/api/reports/${reportId}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('[ReportService] Error fetching report:', error);
      
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

      console.log('[ReportService] Fetching reports with params:', params.toString());
      
      const response = await axios.get(`${API_BASE_URL}/api/reports?${params.toString()}`, getAuthHeader());
      
      console.log('[ReportService] Get reports response:', response.data);
      return response.data;
    } catch (error) {
      console.error('[ReportService] Error fetching reports:', error);
      
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
   * FIXED: Delete a report
   * @param {number} reportId - Report ID
   */
  async deleteReport(reportId) {
    try {
      console.log(`[ReportService] Deleting report ${reportId}`);
      
      const response = await axios.delete(`${API_BASE_URL}/api/reports/${reportId}`, getAuthHeader());
      
      console.log(`[ReportService] Delete response:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`[ReportService] Delete failed for report ${reportId}:`, error);
      
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
   * FIXED: Download a report file
   * @param {number} reportId - Report ID
   */
  async downloadReport(reportId) {
    try {
      console.log(`[ReportService] Downloading report ${reportId}`);
      
      const response = await axios.get(`${API_BASE_URL}/api/reports/${reportId}/download`, {
        ...getAuthHeader(),
        responseType: 'blob'
      });
      
      console.log(`[ReportService] Download successful for report ${reportId}`);
      return response.data;
    } catch (error) {
      console.error(`[ReportService] Download failed for report ${reportId}:`, error);
      
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
   * Get recent reports
   * @param {number} limit - Maximum number of reports to return
   */
  async getRecentReports(limit = 10) {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/reports/recent?limit=${limit}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('[ReportService] Error fetching recent reports:', error);
      
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
   * FIXED: Get report statistics
   */
  async getReportStatistics() {
    try {
      console.log('[ReportService] Fetching report statistics');
      
      const response = await axios.get(`${API_BASE_URL}/api/reports/statistics`, getAuthHeader());
      
      console.log('[ReportService] Statistics response:', response.data);
      return response.data;
    } catch (error) {
      console.error('[ReportService] Error fetching report statistics:', error);
      // Don't throw error, return null so UI can generate basic statistics
      return null;
    }
  }

  /**
   * Archive a report
   * @param {number} reportId - Report ID
   */
  async archiveReport(reportId) {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/reports/${reportId}/archive`, {}, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('[ReportService] Error archiving report:', error);
      
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
   * Health check for report service
   */
  async healthCheck() {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/reports/health`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('[ReportService] Health check failed:', error);
      throw new Error('Report service health check failed');
    }
  }
}

export default new ReportService();