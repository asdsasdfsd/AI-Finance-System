// frontend/src/services/fundService.js
import axios from 'axios';
import { createApiClient, getAuthHeaders } from '../config/apiConfig';

const API_BASE_URL = 'http://localhost:8085';
const FUND_API_URL = `${API_BASE_URL}/api/funds`;

// Create API client with unified auth handling
const apiClient = createApiClient();

const FundService = {
  /**
   * Get all funds for current user's company (from JWT)
   * @returns {Promise<Array>}
   */
  getAllFunds: () => {
    console.log('[FundService] Fetching all funds for current user\'s company');
    return apiClient.get('/api/funds');
  },

  /**
   * Get all funds by company (with validation)
   * @returns {Promise<Array>}
   */
  getFundsByCompany: () => {
    console.log('[FundService] Fetching funds by company (from JWT)');
    return apiClient.get('/api/funds/company');
  },

  /**
   * Get only active funds for current user's company
   * @returns {Promise<Array>}
   */
  getActiveFunds: () => {
    console.log('[FundService] Fetching active funds for current user\'s company');
    return apiClient.get('/api/funds/active');
  },

  /**
   * Get a single fund by ID (with company validation)
   * @param {number} fundId 
   * @returns {Promise<Object>}
   */
  getFundById: (fundId) => {
    console.log(`[FundService] Fetching fund by ID: ${fundId}`);
    return apiClient.get(`/api/funds/${fundId}`);
  },

  /**
   * Create a new fund (company auto-assigned from JWT)
   * @param {Object} fundData 
   * @returns {Promise<Object>}
   */
  createFund: (fundData) => {
    console.log('[FundService] Creating new fund:', fundData);
    // Remove any company data from frontend - backend will auto-assign
    const cleanFundData = {
      name: fundData.name,
      description: fundData.description,
      fundType: fundData.fundType,
      isActive: fundData.isActive !== false, // Default to true
      balance: fundData.balance || 0
    };
    return apiClient.post('/api/funds', cleanFundData);
  },

  /**
   * Update an existing fund (with company validation)
   * @param {number} fundId 
   * @param {Object} fundData 
   * @returns {Promise<Object>}
   */
  updateFund: (fundId, fundData) => {
    console.log(`[FundService] Updating fund ${fundId}:`, fundData);
    // Clean data - remove company info as backend will validate
    const cleanFundData = {
      name: fundData.name,
      description: fundData.description,
      fundType: fundData.fundType,
      isActive: fundData.isActive,
      balance: fundData.balance
    };
    return apiClient.put(`/api/funds/${fundId}`, cleanFundData);
  },

  /**
   * Delete a fund by ID (with company validation)
   * @param {number} fundId 
   * @returns {Promise<void>}
   */
  deleteFund: (fundId) => {
    console.log(`[FundService] Deleting fund ${fundId}`);
    return apiClient.delete(`/api/funds/${fundId}`);
  },

  // ========== Legacy Methods (Backward Compatibility) ==========
  
  /**
   * @deprecated Use getAllFunds() instead
   */
  getFundsByCompany: (companyId) => {
    console.warn('[FundService] getFundsByCompany(companyId) is deprecated. Use getAllFunds() instead.');
    console.log(`[FundService] Legacy: Fetching funds for company ${companyId}`);
    return apiClient.get(`/api/funds/company/${companyId}`);
  },

  /**
   * @deprecated Use getActiveFunds() instead
   */
  getActiveFundsByCompany: (companyId) => {
    console.warn('[FundService] getActiveFundsByCompany(companyId) is deprecated. Use getActiveFunds() instead.');
    console.log(`[FundService] Legacy: Fetching active funds for company ${companyId}`);
    return apiClient.get(`/api/funds/company/${companyId}/active`);
  }
};

export default FundService;