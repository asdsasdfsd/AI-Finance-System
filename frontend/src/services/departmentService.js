// frontend/src/services/departmentService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/departments`; // 基础URL不带斜杠

const DepartmentService = {
  // 获取部门列表：GET /api/departments
  getAllDepartments: async () => {
    const response = await axios.get(API_URL);
    return response.data;
  },
  
  // 获取部门详情：GET /api/departments/1
  getDepartmentById: async (id) => {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  },
  
  // 创建部门：POST /api/departments
  createDepartment: async (departmentData) => {
    const response = await axios.post(API_URL, departmentData);
    return response.data;
  },
  
  // 更新部门：PUT /api/departments/1
  updateDepartment: async (id, departmentData) => {
    const response = await axios.put(`${API_URL}/${id}`, departmentData);
    return response.data;
  },
  
  // 删除部门：DELETE /api/departments/1
  deleteDepartment: async (id) => {
    await axios.delete(`${API_URL}/${id}`);
  },
  
  // 根据公司获取部门：GET /api/departments/company/1
  getDepartmentsByCompany: async (companyId) => {
    const response = await axios.get(`${API_URL}/company/${companyId}`);
    return response.data;
  },
  
  // 获取子部门：GET /api/departments/1/subdepartments
  getSubDepartments: async (parentId) => {
    const response = await axios.get(`${API_URL}/${parentId}/subdepartments`);
    return response.data;
  },
  
  // 根据经理获取部门：GET /api/departments/manager/1
  getDepartmentsByManager: async (managerId) => {
    const response = await axios.get(`${API_URL}/manager/${managerId}`);
    return response.data;
  }
};

export default DepartmentService;