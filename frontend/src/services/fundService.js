import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085';
const FUND_API_URL = `${API_BASE_URL}/api/funds`;

// Automatically attach auth header if token exists
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  if (user && user.token) {
    return {
      headers: {
        Authorization: `Bearer ${user.token}`
      }
    };
  }
  return {};
};
const FundService = {
  /**
   * Get all funds by companyId
   * @param {number} companyId 
   * @returns {Promise<Array>}
   */
  getFundsByCompany: (companyId) => {
    return axios.get(`${FUND_API_URL}/company/${companyId}`, getAuthHeader());
  },

  /**
   * Get only active funds by companyId
   * @param {number} companyId 
   * @returns {Promise<Array>}
   */
  getActiveFundsByCompany: (companyId) => {
    return axios.get(`${FUND_API_URL}/company/${companyId}/active`, getAuthHeader());
  },

  /**
   * Get a single fund by ID
   * @param {number} fundId 
   * @returns {Promise<Object>}
   */
  getFundById: (fundId) => {
    return axios.get(`${FUND_API_URL}/${fundId}`, getAuthHeader());
  },

  /**
   * Create a new fund
   * @param {Object} fundData 
   * @returns {Promise<Object>}
   */
  createFund: (fundData) => {
    return axios.post(FUND_API_URL, fundData, getAuthHeader());
  },

  /**
   * Update an existing fund
   * @param {number} fundId 
   * @param {Object} fundData 
   * @returns {Promise<Object>}
   */
  updateFund: (fundId, fundData) => {
    return axios.put(`${FUND_API_URL}/${fundId}`, fundData, getAuthHeader());
  },

  /**
   * Delete a fund by ID
   * @param {number} fundId 
   * @returns {Promise<void>}
   */
  deleteFund: (fundId) => {
    return axios.delete(`${FUND_API_URL}/${fundId}`, getAuthHeader());
  }
};

export default FundService;
