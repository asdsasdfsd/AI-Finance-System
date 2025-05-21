// frontend/src/views/Dashboard/DepartmentManagement.js
import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, Space, message } from 'antd';
import axios from 'axios';

const { Option } = Select;
const API_BASE_URL = 'http://localhost:8085';

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
      const [deptResponse, companyResponse, userResponse] = await Promise.all([
        axios.get(`${API_BASE_URL}/api/departments`),
        axios.get(`${API_BASE_URL}/api/companies`),
        axios.get(`${API_BASE_URL}/api/users`)
      ]);
      
      setDepartments(deptResponse.data);
      setCompanies(companyResponse.data);
      setUsers(userResponse.data);
    } catch (error) {
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
      
      if (currentDepartment) {
        // 更新部门
        await axios.put(`${API_BASE_URL}/api/departments/${currentDepartment.departmentId}`, values);
        message.success('Department updated successfully');
      } else {
        // 创建部门
        await axios.post(`${API_BASE_URL}/api/departments`, values);
        message.success('Department created successfully');
      }
      
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('Operation failed');
    }
  };

  // 编辑部门
  const handleEdit = (record) => {
    setCurrentDepartment(record);
    form.setFieldsValue({
      ...record,
      companyId: record.company?.companyId,
      managerId: record.manager?.userId,
      parentDepartmentId: record.parentDepartment?.departmentId
    });
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
      render: (company) => company?.companyName,
    },
    {
      title: 'Manager',
      dataIndex: 'manager',
      key: 'manager',
      render: (manager) => manager?.fullName,
    },
    {
      title: 'Budget',
      dataIndex: 'budget',
      key: 'budget',
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive) => isActive ? 'Active' : 'Inactive',
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
        onCancel={() => setModalVisible(false)}
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
            <Select>
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
            <Select allowClear>
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
            <Select allowClear>
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
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          
          <Form.Item
            name="isActive"
            label="Status"
          >
            <Select>
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