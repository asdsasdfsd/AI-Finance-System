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
 * Fixed Report Service - Compatible with backend DDD API
 */
class ReportService {
  
  /**
   * Generate a new report
   */
  async generateReport(reportRequest) {
    try {
      console.log('Sending report request:', reportRequest);
      const response = await axios.post(`${API_BASE_URL}/generate`, reportRequest, getAuthHeader());
      console.log('Report generation response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error generating report:', error);
      this.handleError(error, 'Failed to generate report');
    }
  }

  /**
   * Get report details by ID
   */
  async getReport(reportId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/${reportId}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching report:', error);
      this.handleError(error, 'Failed to fetch report');
    }
  }

  /**
   * Get all reports with optional filters
   */
  async getReports(filterParams = {}) {
    try {
      console.log('Fetching reports with filters:', filterParams);
      
      // Convert filter params to query string, excluding empty values
      const queryParams = new URLSearchParams();
      
      Object.entries(filterParams).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
          queryParams.append(key, value);
        }
      });
      
      const url = queryParams.toString() 
        ? `${API_BASE_URL}?${queryParams.toString()}`
        : API_BASE_URL;
      
      console.log('Request URL:', url);
      const response = await axios.get(url, getAuthHeader());
      console.log('Reports response:', response.data);
      
      return response.data;
    } catch (error) {
      console.error('Error fetching reports:', error);
      this.handleError(error, 'Failed to fetch reports');
    }
  }

  /**
   * Get recent reports
   */
  async getRecentReports(limit = 10) {
    try {
      const response = await axios.get(`${API_BASE_URL}/recent?limit=${limit}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching recent reports:', error);
      this.handleError(error, 'Failed to fetch recent reports');
    }
  }

  /**
   * Get reports by type
   */
  async getReportsByType(reportType) {
    try {
      const response = await axios.get(`${API_BASE_URL}/by-type?reportType=${reportType}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error fetching reports by type:', error);
      this.handleError(error, 'Failed to fetch reports by type');
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
      // Don't throw for statistics - just return null
      console.warn('Statistics not available, will generate from report data');
      return null;
    }
  }

  /**
   * Download report file
   */
  async downloadReport(reportId, fileName = null) {
    try {
      console.log(`Downloading report ${reportId}...`);
      
      const response = await axios.get(`${API_BASE_URL}/${reportId}/download`, {
        responseType: 'blob',
        ...getAuthHeader()
      });

      // Check if we got an error response disguised as a blob
      if (response.headers['content-type']?.includes('application/json')) {
        const text = await response.data.text();
        const errorData = JSON.parse(text);
        throw new Error(errorData.message || 'Download failed');
      }

      // Create blob and download
      const blob = new Blob([response.data], { 
        type: response.headers['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      });
      
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      // Determine filename
      let downloadFileName = fileName;
      const contentDisposition = response.headers['content-disposition'];
      
      if (!downloadFileName && contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (fileNameMatch) {
          downloadFileName = fileNameMatch[1].replace(/['"]/g, '');
        }
      }
      
      if (!downloadFileName) {
        downloadFileName = `report_${reportId}.xlsx`;
      }

      // Ensure proper file extension
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
      this.handleError(error, 'Failed to download report');
    }
  }

  /**
   * Archive a report
   */
  async archiveReport(reportId) {
    try {
      const response = await axios.post(`${API_BASE_URL}/${reportId}/archive`, {}, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error archiving report:', error);
      this.handleError(error, 'Failed to archive report');
    }
  }

  /**
   * Delete a report
   */
  async deleteReport(reportId) {
    try {
      const response = await axios.delete(`${API_BASE_URL}/${reportId}`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Error deleting report:', error);
      this.handleError(error, 'Failed to delete report');
    }
  }

  /**
   * Centralized error handling
   */
  handleError(error, defaultMessage) {
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
      throw new Error(error.message || defaultMessage);
    }
  }

  /**
   * Get available report types (static data)
   */
  getReportTypes() {
    return [
      { value: 'BALANCE_SHEET', label: 'Balance Sheet' },
      { value: 'INCOME_STATEMENT', label: 'Income Statement' },
      { value: 'INCOME_EXPENSE', label: 'Income & Expense' },
      { value: 'FINANCIAL_GROUPING', label: 'Financial Grouping' },
    ];
  }

  /**
   * Get available report statuses (static data)
   */
  getReportStatuses() {
    return [
      { value: 'PENDING', label: 'Pending' },
      { value: 'GENERATING', label: 'Generating' },
      { value: 'COMPLETED', label: 'Completed' },
      { value: 'FAILED', label: 'Failed' },
      { value: 'ARCHIVED', label: 'Archived' },
    ];
  }

  /**
   * Health check for service
   */
  async healthCheck() {
    try {
      const response = await axios.get(`${API_BASE_URL}/health`, getAuthHeader());
      return response.data;
    } catch (error) {
      console.error('Health check failed:', error);
      return { status: 'error', message: 'Service unavailable' };
    }
  }
}

export default new ReportService();