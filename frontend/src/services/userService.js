// frontend/src/services/userService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/users`; // 基础URL不带斜杠

const UserService = {
  // 获取用户列表：GET /api/users
  getAllUsers: async () => {
    const response = await axios.get(API_URL);
    return response.data;
  },
  
  // 获取用户详情：GET /api/users/1
  getUserById: async (id) => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  },
  
  // 创建用户：POST /api/users
  createUser: async (userData) => {
    const response = await axios.post(API_URL, userData);
    return response.data;
  },
  
  // 更新用户：PUT /api/users/1
  updateUser: async (id, userData) => {
    const response = await axios.put(`${API_URL}/${id}`, userData);
    return response.data;
  },
  
  // 删除用户：DELETE /api/users/1
  deleteUser: async (id) => {
    await axios.delete(`${API_URL}/${id}`);
  },
  
  // 分配角色：POST /api/users/1/roles/admin
  assignRole: async (userId, roleName) => {
    const response = await axios.post(`${API_URL}/${userId}/roles/${roleName}`);
    return response.data;
  },
  
  // 移除角色：DELETE /api/users/1/roles/admin
  removeRole: async (userId, roleName) => {
    const response = await axios.delete(`${API_URL}/${userId}/roles/${roleName}`);
    return response.data;
  }
};

export default UserService;