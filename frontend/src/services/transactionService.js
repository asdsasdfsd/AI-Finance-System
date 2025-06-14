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

// Get company ID from current user or use hardcoded value for development
const getCompanyId = () => {
  const user = AuthService.getCurrentUser();
  // TODO: Replace with JWT extraction in production
  // For now, use hardcoded value as requested
  return user?.companyId || 1;
};

const TransactionService = {
  // Fixed: Pass companyId as query parameter
  getAll: () => {
    const companyId = getCompanyId();
    return axios.get(`${API_BASE_URL}?companyId=${companyId}`, getAuthHeader());
  },

  getById: (id) => {
    const companyId = getCompanyId();
    return axios.get(`${API_BASE_URL}/${id}?companyId=${companyId}`, getAuthHeader());
  },

  getByCompanyAndType: (companyId, type) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/type/${type}`, getAuthHeader()),

  getByUserAndType: (userId, type) => {
    const companyId = getCompanyId();
    return axios.get(`${API_BASE_URL}/user/${userId}/type/${type}?companyId=${companyId}`, getAuthHeader());
  },

  getByDepartmentAndType: (departmentId, type) =>
    axios.get(`${API_BASE_URL}/department/${departmentId}/type/${type}`, getAuthHeader()),

  getByDateRange: (companyId, startDate, endDate) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/date-range`, {
      ...getAuthHeader(),
      params: { startDate, endDate }
    }),

  getSumByCompanyAndType: (companyId, type) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/type/${type}/sum`, getAuthHeader()),

  createTransaction: (data) => {
    // Ensure companyId and userId are included
    const enrichedData = {
      ...data,
      companyId: data.companyId || getCompanyId(),
      userId: data.userId || 1 // Hardcoded for development
    };
    return axios.post(API_BASE_URL, enrichedData, getAuthHeader());
  },

  updateTransaction: (id, data) => {
    // Ensure companyId and userId are included
    const enrichedData = {
      ...data,
      companyId: data.companyId || getCompanyId(),
      userId: data.userId || 1 // Hardcoded for development
    };
    return axios.put(`${API_BASE_URL}/${id}`, enrichedData, getAuthHeader());
  },

  deleteTransaction: (id) => {
    const companyId = getCompanyId();
    return axios.delete(`${API_BASE_URL}/${id}?companyId=${companyId}&userId=1`, getAuthHeader());
  },

  // Additional convenience methods for better API usage
  getByCompany: (companyId) =>
    axios.get(`${API_BASE_URL}/company/${companyId}`, getAuthHeader()),

  getByCompanySorted: (companyId) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/sorted`, getAuthHeader()),
};

export default TransactionService;