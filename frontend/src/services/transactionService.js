// frontend/src/services/transactionService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/transactions';

const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

// Get company ID from current user (JWT will be used by backend to extract company ID)
const getCompanyId = () => {
  const user = AuthService.getCurrentUser();
  return user?.companyId || user?.user?.companyId || null;
};

// Get user ID from current user
const getUserId = () => {
  const user = AuthService.getCurrentUser();
  return user?.userId || user?.user?.userId || null;
};

const TransactionService = {
  // Get all transactions (backend will use JWT to determine company)
  getAll: () => {
    return axios.get(API_BASE_URL, getAuthHeader());
  },

  // Get transaction by ID (backend will validate company access via JWT)
  getById: (id) => {
    return axios.get(`${API_BASE_URL}/${id}`, getAuthHeader());
  },

  // Get transactions by type (backend will use JWT for company context)
  getByType: (type) => {
    return axios.get(`${API_BASE_URL}/type/${type}`, getAuthHeader());
  },

  // Get transactions by date range (backend will use JWT for company context)
  getByDateRange: (startDate, endDate) => {
    return axios.get(`${API_BASE_URL}/date-range`, {
      ...getAuthHeader(),
      params: { startDate, endDate }
    });
  },

  // Get transaction sum by type (backend will use JWT for company context)
  getSumByType: (type) => {
    return axios.get(`${API_BASE_URL}/sum/type/${type}`, getAuthHeader());
  },

  // Create new transaction
  createTransaction: (data) => {
    // Remove explicit companyId and userId as backend will extract from JWT
    const transactionData = {
      amount: data.amount,
      description: data.description,
      transactionDate: data.transactionDate,
      category: data.category,
      paymentMethod: data.paymentMethod,
      reference: data.reference,
      notes: data.notes
    };
    
    return axios.post(API_BASE_URL, transactionData, getAuthHeader());
  },

  // Update existing transaction
  updateTransaction: (id, data) => {
    // Remove explicit companyId and userId as backend will extract from JWT
    const transactionData = {
      amount: data.amount,
      description: data.description,
      transactionDate: data.transactionDate,
      category: data.category,
      paymentMethod: data.paymentMethod,
      reference: data.reference,
      notes: data.notes
    };
    
    return axios.put(`${API_BASE_URL}/${id}`, transactionData, getAuthHeader());
  },

  // Delete transaction (backend will validate company access via JWT)
  deleteTransaction: (id) => {
    return axios.delete(`${API_BASE_URL}/${id}`, getAuthHeader());
  },

  // Legacy methods for backward compatibility (will be deprecated)
  getByCompanyAndType: (companyId, type) => {
    console.warn('getByCompanyAndType is deprecated. Use getByType instead.');
    return TransactionService.getByType(type);
  },

  getByUserAndType: (userId, type) => {
    console.warn('getByUserAndType is deprecated. Use getByType instead.');
    return TransactionService.getByType(type);
  },

  getByDepartmentAndType: (departmentId, type) => {
    console.warn('getByDepartmentAndType is deprecated. Backend will handle department filtering.');
    return TransactionService.getByType(type);
  },

  getSumByCompanyAndType: (companyId, type) => {
    console.warn('getSumByCompanyAndType is deprecated. Use getSumByType instead.');
    return TransactionService.getSumByType(type);
  },

  getByCompany: (companyId) => {
    console.warn('getByCompany is deprecated. Use getAll instead.');
    return TransactionService.getAll();
  },

  getByCompanySorted: (companyId) => {
    console.warn('getByCompanySorted is deprecated. Use getAll instead.');
    return TransactionService.getAll();
  },

  // Utility methods for frontend components
  getCurrentUserCompanyId: () => {
    return getCompanyId();
  },

  getCurrentUserId: () => {
    return getUserId();
  },

  // Validate if user is authenticated
  isAuthenticated: () => {
    return AuthService.isAuthenticated();
  }
};

export default TransactionService;