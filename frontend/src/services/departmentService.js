// frontend/src/services/departmentService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/departments`;

// Get authentication headers
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

const DepartmentService = {
  // 获取所有部门
  getAllDepartments: async () => {
    console.log('🏢 Fetching all departments...');
    try {
      const response = await axios.get(API_URL, getAuthHeader());
      console.log('✅ Departments response:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ Error fetching departments:', error);
      throw error;
    }
  },
  
  // 获取部门详情
  getDepartmentById: async (id) => {
    console.log(`🏢 Fetching department by ID: ${id}`);
    try {
      const response = await axios.get(`${API_URL}/${id}`, getAuthHeader());
      console.log('✅ Department fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error fetching department ${id}:`, error);
      throw error;
    }
  },
  
  // 创建部门
  createDepartment: async (departmentData) => {
    console.log('🏢 Creating department:', departmentData);
    try {
      const response = await axios.post(API_URL, departmentData, getAuthHeader());
      console.log('✅ Department created successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ Error creating department:', error);
      throw error;
    }
  },
  
  // 更新部门
  updateDepartment: async (id, departmentData) => {
    console.log(`🏢 Updating department ID: ${id}`, departmentData);
    try {
      const response = await axios.put(`${API_URL}/${id}`, departmentData, getAuthHeader());
      console.log('✅ Department updated successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error updating department ${id}:`, error);
      throw error;
    }
  },
  
  // 删除部门
  deleteDepartment: async (id) => {
    console.log(`🏢 Deleting department ID: ${id}`);
    try {
      await axios.delete(`${API_URL}/${id}`, getAuthHeader());
      console.log('✅ Department deleted successfully');
    } catch (error) {
      console.error(`❌ Error deleting department ${id}:`, error);
      throw error;
    }
  },
  
  // 根据公司获取部门
  getDepartmentsByCompany: async (companyId) => {
    console.log(`🏢 Fetching departments for company: ${companyId}`);
    try {
      const response = await axios.get(`${API_URL}/company/${companyId}`, getAuthHeader());
      console.log('✅ Company departments fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error fetching departments for company ${companyId}:`, error);
      throw error;
    }
  },
  
  // 获取子部门
  getSubDepartments: async (parentId) => {
    console.log(`🏢 Fetching sub-departments for parent: ${parentId}`);
    try {
      const response = await axios.get(`${API_URL}/${parentId}/subdepartments`, getAuthHeader());
      console.log('✅ Sub-departments fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error fetching sub-departments for ${parentId}:`, error);
      throw error;
    }
  },
  
  // 根据经理获取部门
  getDepartmentsByManager: async (managerId) => {
    console.log(`🏢 Fetching departments for manager: ${managerId}`);
    try {
      const response = await axios.get(`${API_URL}/manager/${managerId}`, getAuthHeader());
      console.log('✅ Manager departments fetched successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ Error fetching departments for manager ${managerId}:`, error);
      throw error;
    }
  }
};

export default DepartmentService;