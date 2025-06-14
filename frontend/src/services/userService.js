// frontend/src/services/userService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/users`;

const UserService = {
  // Get all users with optional company filter
  getAllUsers: async (companyId = null) => {
    const url = companyId ? `${API_URL}?companyId=${companyId}` : `${API_URL}?companyId=1`; // Default to companyId=1 for now
    const response = await axios.get(url);
    return response.data;
  },
  
  // Get all users without company filter (for system admin)
  getAllUsersGlobal: async () => {
    // This would need a different endpoint or special permission
    const response = await axios.get(`${API_URL}/all`);
    return response.data;
  },
  
  // Get user by ID
  getUserById: async (id) => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  },
  
  // Create user
  createUser: async (userData) => {
    // Ensure roleNames field for backend compatibility
    const requestData = {
      ...userData,
      roleNames: userData.roles || []
    };
    const response = await axios.post(API_URL, requestData);
    return response.data;
  },
  
  // Update user
  updateUser: async (id, userData) => {
    // Ensure roleNames field for backend compatibility
    const requestData = {
      ...userData,
      roleNames: userData.roles || []
    };
    const response = await axios.put(`${API_URL}/${id}`, requestData);
    return response.data;
  },
  
  // Delete user
  deleteUser: async (id) => {
    await axios.delete(`${API_URL}/${id}`);
  },
  
  // Assign role
  assignRole: async (userId, roleName) => {
    const response = await axios.post(`${API_URL}/${userId}/roles/${roleName}`);
    return response.data;
  },
  
  // Remove role
  removeRole: async (userId, roleName) => {
    const response = await axios.delete(`${API_URL}/${userId}/roles/${roleName}`);
    return response.data;
  },
  
  // Get users by company
  getUsersByCompany: async (companyId) => {
    const response = await axios.get(`${API_URL}?companyId=${companyId}`);
    return response.data;
  }
};

export default UserService;