// frontend/src/services/companyService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/companies/`;

/**
 * Service for handling company-related API requests
 */
const CompanyService = {
  /**
   * Get all companies
   * @returns {Promise<Array>} List of companies
   */
  getAllCompanies: async () => {
    const response = await axios.get(API_URL);
    return response.data;
  },
  
  /**
   * Get company by ID
   * @param {number} id - Company ID
   * @returns {Promise<Object>} Company details
   */
  getCompanyById: async (id) => {
    const response = await axios.get(API_URL + id);
    return response.data;
  },
  
  /**
   * Create a new company
   * @param {Object} companyData - Company data
   * @returns {Promise<Object>} Created company data
   */
  createCompany: async (companyData) => {
    const response = await axios.post(API_URL, companyData);
    return response.data;
  },
  
  /**
   * Update a company
   * @param {number} id - Company ID
   * @param {Object} companyData - Updated company data
   * @returns {Promise<Object>} Updated company data
   */
  updateCompany: async (id, companyData) => {
    const response = await axios.put(API_URL + id, companyData);
    return response.data;
  },
  
  /**
   * Delete a company
   * @param {number} id - Company ID
   * @returns {Promise<void>}
   */
  deleteCompany: async (id) => {
    await axios.delete(API_URL + id);
  },

  /**
   * Get company list
   * @returns {Promise<Object>} Updated company list
   */
  getCompanies: async (page = 0, size = 10) => {
    const response = await axios.get(API_URL, {
      params: { page, size }
    });
    return response.data;
  },
};

export default CompanyService;