// frontend/src/services/departmentService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/departments`;

const DepartmentService = {
  // 获取所有部门
  getAllDepartments: async () => {
    console.log('Fetching all departments...');
    const response = await axios.get(API_URL);
    console.log('Departments response:', response.data);
    return response.data;
  },
  
  // 获取部门详情
  getDepartmentById: async (id) => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  },
  
  // 创建部门
  createDepartment: async (departmentData) => {
    console.log('Creating department:', departmentData);
    const response = await axios.post(API_URL, departmentData);
    console.log('Create response:', response.data);
    return response.data;
  },
  
  // 更新部门
  updateDepartment: async (id, departmentData) => {
    console.log('Updating department ID:', id);
    console.log('Update data:', departmentData);
    const response = await axios.put(`${API_URL}/${id}`, departmentData);
    console.log('Update response:', response.data);
    return response.data;
  },
  
  // 删除部门
  deleteDepartment: async (id) => {
    await axios.delete(`${API_URL}/${id}`);
  },
  
  // 根据公司获取部门
  getDepartmentsByCompany: async (companyId) => {
    const response = await axios.get(`${API_URL}/company/${companyId}`);
    return response.data;
  },
  
  // 获取子部门
  getSubDepartments: async (parentId) => {
    const response = await axios.get(`${API_URL}/${parentId}/subdepartments`);
    return response.data;
  },
  
  // 根据经理获取部门
  getDepartmentsByManager: async (managerId) => {
    const response = await axios.get(`${API_URL}/manager/${managerId}`);
    return response.data;
  }
};

export default DepartmentService;