import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/transactions';

const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

const TransactionService = {
  getAll: () => axios.get(API_BASE_URL, getAuthHeader()),

  getById: (id) => axios.get(`${API_BASE_URL}/${id}`, getAuthHeader()),

  getByCompanyAndType: (companyId, type) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/type/${type}`, getAuthHeader()),

  getByUserAndType: (userId, type) =>
    axios.get(`${API_BASE_URL}/user/${userId}/type/${type}`, getAuthHeader()),

  getByDepartmentAndType: (departmentId, type) =>
    axios.get(`${API_BASE_URL}/department/${departmentId}/type/${type}`, getAuthHeader()),

  getByDateRange: (companyId, startDate, endDate) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/date-range`, {
      ...getAuthHeader(),
      params: { startDate, endDate }
    }),

  getSumByCompanyAndType: (companyId, type) =>
    axios.get(`${API_BASE_URL}/company/${companyId}/type/${type}/sum`, getAuthHeader()),

  createTransaction: (data) =>
    axios.post(API_BASE_URL, data, getAuthHeader()),

  updateTransaction: (id, data) =>
    axios.put(`${API_BASE_URL}/${id}`, data, getAuthHeader()),

  deleteTransaction: (id) =>
    axios.delete(`${API_BASE_URL}/${id}`, getAuthHeader()),
};

export default TransactionService;
