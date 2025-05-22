// frontend/src/services/companyService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/companies`; // 基础URL不带斜杠

/**
 * Service for handling company-related API requests
 */
const CompanyService = {
  /**
   * Get all companies
   * @returns {Promise<Array>} List of companies
   */
  getAllCompanies: async () => {
    // 列表操作：GET /api/companies
    const response = await axios.get(API_URL);
    return response.data;
  },
  
  /**
   * Get company by ID
   * @param {number} id - Company ID
   * @returns {Promise<Object>} Company details
   */
  getCompanyById: async (id) => {
    // 单个资源：GET /api/companies/1
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  },
  
  /**
   * Create a new company
   * @param {Object} companyData - Company data
   * @returns {Promise<Object>} Created company data
   */
  createCompany: async (companyData) => {
    // 创建操作：POST /api/companies
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
    // 更新操作：PUT /api/companies/1
    const response = await axios.put(`${API_URL}/${id}`, companyData);
    return response.data;
  },
  
  /**
   * Delete a company
   * @param {number} id - Company ID
   * @returns {Promise<void>}
   */
  deleteCompany: async (id) => {
    // 删除操作：DELETE /api/companies/1
    await axios.delete(`${API_URL}/${id}`);
  },

  /**
   * Get company list with pagination
   * @param {number} page - Page number  
   * @param {number} size - Page size
   * @returns {Promise<Object>} Paginated company list
   */
  getCompanies: async (page = 0, size = 10) => {
    // 分页列表：GET /api/companies?page=0&size=10
    const response = await axios.get(API_URL, {
      params: { page, size }
    });
    return response.data;
  },
};

export default CompanyService;