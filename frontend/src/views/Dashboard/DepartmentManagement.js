// frontend/src/views/Dashboard/DepartmentManagement.js
import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, Space, message } from 'antd';
import DepartmentService from '../../services/departmentService';
import CompanyService from '../../services/companyService';
import UserService from '../../services/userService';

const { Option } = Select;

const DepartmentManagement = () => {
  const [departments, setDepartments] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentDepartment, setCurrentDepartment] = useState(null);
  const [form] = Form.useForm();

  // 获取数据
  const fetchData = async () => {
    setLoading(true);
    try {
      const [deptData, companyData, userData] = await Promise.all([
        DepartmentService.getAllDepartments(),
        CompanyService.getAllCompanies(),
        UserService.getAllUsers()
      ]);
      
      console.log('Departments loaded:', deptData);
      setDepartments(deptData);
      setCompanies(companyData);
      setUsers(userData);
    } catch (error) {
      console.error('Failed to fetch data:', error);
      message.error('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      // 调试：打印表单数据
      console.log('Form values:', values);
      console.log('Current department:', currentDepartment);
      
      // 构建请求数据
      const requestData = {
        name: values.name,
        code: values.code,
        companyId: values.companyId,
        parentDepartmentId: values.parentDepartmentId || null,
        managerId: values.managerId || null,
        budget: values.budget || 0,
        isActive: values.isActive !== undefined ? values.isActive : true
      };
      
      console.log('Request data:', requestData);
      
      if (currentDepartment) {
        // 更新部门
        console.log('Updating department ID:', currentDepartment.departmentId);
        const response = await DepartmentService.updateDepartment(
          currentDepartment.departmentId, 
          requestData
        );
        console.log('Update response:', response);
        message.success('Department updated successfully');
      } else {
        // 创建部门
        const response = await DepartmentService.createDepartment(requestData);
        console.log('Create response:', response);
        message.success('Department created successfully');
      }
      
      setModalVisible(false);
      form.resetFields();
      setCurrentDepartment(null);
      
      // 重新获取数据
      await fetchData();
      
    } catch (error) {
      console.error('Operation failed:', error);
      console.error('Error response:', error.response?.data);
      message.error('Operation failed: ' + (error.response?.data?.message || error.message));
    }
  };

  // 编辑部门
  const handleEdit = (record) => {
    console.log('Editing department:', record);
    setCurrentDepartment(record);
    
    // 设置表单值
    const formValues = {
      name: record.name,
      code: record.code,
      companyId: record.company?.companyId,
      managerId: record.manager?.userId,
      parentDepartmentId: record.parentDepartment?.departmentId,
      budget: record.budget,
      isActive: record.isActive
    };
    
    console.log('Setting form values:', formValues);
    form.setFieldsValue(formValues);
    setModalVisible(true);
  };

  // 表格列
  const columns = [
    {
      title: 'Department Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Code',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: 'Company',
      dataIndex: 'company',
      key: 'company',
      render: (company) => company?.companyName || 'N/A',
    },
    {
      title: 'Manager',
      dataIndex: 'manager',
      key: 'manager',
      render: (manager) => manager?.fullName || 'N/A',
    },
    {
      title: 'Budget',
      dataIndex: 'budget',
      key: 'budget',
      render: (budget) => budget ? `$${budget.toLocaleString()}` : '$0',
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive) => (
        <span style={{ color: isActive ? 'green' : 'red' }}>
          {isActive ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="primary" onClick={() => handleEdit(record)}>
            Edit
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>Department Management</h2>
      
      <Button 
        type="primary" 
        style={{ marginBottom: 16 }}
        onClick={() => {
          setCurrentDepartment(null);
          form.resetFields();
          setModalVisible(true);
        }}
      >
        Add Department
      </Button>
      
      <Table 
        columns={columns} 
        dataSource={departments} 
        rowKey="departmentId"
        loading={loading}
      />
      
      <Modal
        title={currentDepartment ? 'Edit Department' : 'Add Department'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setCurrentDepartment(null);
        }}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="Department Name"
            rules={[{ required: true, message: 'Please input department name!' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="code"
            label="Department Code"
            rules={[{ required: true, message: 'Please input department code!' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="companyId"
            label="Company"
            rules={[{ required: true, message: 'Please select company!' }]}
          >
            <Select placeholder="Select company">
              {companies.map(company => (
                <Option key={company.companyId} value={company.companyId}>
                  {company.companyName}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="parentDepartmentId"
            label="Parent Department"
          >
            <Select allowClear placeholder="Select parent department">
              {departments.map(dept => (
                <Option key={dept.departmentId} value={dept.departmentId}>
                  {dept.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="managerId"
            label="Manager"
          >
            <Select allowClear placeholder="Select manager">
              {users.map(user => (
                <Option key={user.userId} value={user.userId}>
                  {user.fullName} ({user.username})
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="budget"
            label="Budget"
          >
            <InputNumber 
              style={{ width: '100%' }} 
              min={0}
              formatter={value => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => value.replace(/\$\s?|(,*)/g, '')}
            />
          </Form.Item>
          
          <Form.Item
            name="isActive"
            label="Status"
          >
            <Select placeholder="Select status">
              <Option value={true}>Active</Option>
              <Option value={false}>Inactive</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DepartmentManagement;