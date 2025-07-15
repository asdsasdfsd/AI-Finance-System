// frontend/src/services/companyService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/companies';

// Get authentication headers
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

const CompanyService = {
  // Get all companies
  getAllCompanies: async () => {
    console.log('🏢 Fetching all companies...');
    try {
      const response = await axios.get(API_BASE_URL, getAuthHeader());
      console.log('✅ Companies fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ Error fetching companies:', error);
      throw error;
    }
  },

  // Get company by ID
  getCompanyById: async (id) => {
    console.log(`🏢 Fetching company by ID: ${id}`);
    try {
      const response = await axios.get(`${API_BASE_URL}/${id}`, getAuthHeader());
      console.log('✅ Company fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error fetching company ${id}:`, error);
      throw error;
    }
  },

  // Create company
  createCompany: async (companyData) => {
    console.log('🏢 Creating new company:', companyData);
    try {
      const response = await axios.post(API_BASE_URL, companyData, getAuthHeader());
      console.log('✅ Company created successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ Error creating company:', error);
      throw error;
    }
  },

  // Update company
  updateCompany: async (id, companyData) => {
    console.log(`🏢 Updating company ${id}:`, companyData);
    try {
      const response = await axios.put(`${API_BASE_URL}/${id}`, companyData, getAuthHeader());
      console.log('✅ Company updated successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error updating company ${id}:`, error);
      throw error;
    }
  },

  // Delete company
  deleteCompany: async (id) => {
    console.log(`🏢 Deleting company ${id}`);
    try {
      await axios.delete(`${API_BASE_URL}/${id}`, getAuthHeader());
      console.log('✅ Company deleted successfully');
    } catch (error) {
      console.error(`❌ Error deleting company ${id}:`, error);
      throw error;
    }
  },

  // Get company statistics
  getCompanyStats: async (companyId) => {
    console.log(`📊 Fetching stats for company ${companyId}`);
    try {
      const response = await axios.get(`${API_BASE_URL}/${companyId}/stats`, getAuthHeader());
      console.log('✅ Company stats fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error fetching company stats for ${companyId}:`, error);
      throw error;
    }
  }
};

export default CompanyService;