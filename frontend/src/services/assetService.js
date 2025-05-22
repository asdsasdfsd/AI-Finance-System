import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/fixed-assets';

const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

const AssetService = {
  getAll: () => axios.get(API_BASE_URL, getAuthHeader()),

  getById: (id) => axios.get(`${API_BASE_URL}/${id}`, getAuthHeader()),

  getByCompany: (companyId) =>
    axios.get(`${API_BASE_URL}/company/${companyId}`, getAuthHeader()),

  getByDepartment: (departmentId) =>
    axios.get(`${API_BASE_URL}/department/${departmentId}`, getAuthHeader()),

  getByStatus: (status) =>
    axios.get(`${API_BASE_URL}/status/${status}`, getAuthHeader()),

  createAsset: (data) =>
    axios.post(API_BASE_URL, data, getAuthHeader()),

  updateAsset: (id, data) =>
    axios.put(`${API_BASE_URL}/${id}`, data, getAuthHeader()),

  deleteAsset: (id) =>
    axios.delete(`${API_BASE_URL}/${id}`, getAuthHeader()),
};

export default AssetService;
