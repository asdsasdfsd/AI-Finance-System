// frontend/src/views/Dashboard/UserManagement.js
import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message } from 'antd';
import UserService from '../../services/userService';
import CompanyService from '../../services/companyService';

const { Option } = Select;

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [roles, setRoles] = useState(['USER', 'FINANCE_MANAGER', 'COMPANY_ADMIN', 'SYSTEM_ADMIN']);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState('add'); // 'add' or 'edit'
  const [currentUser, setCurrentUser] = useState(null);
  const [form] = Form.useForm();

  // 加载用户列表和公司列表
  const fetchData = async () => {
    setLoading(true);
    try {
      const [usersData, companiesData] = await Promise.all([
        UserService.getAllUsers(),
        CompanyService.getAllCompanies()
      ]);
      
      setUsers(usersData);
      setCompanies(companiesData);
    } catch (error) {
      message.error('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // 处理表单提交
// 在前端UserManagement.js中添加调试代码
// 处理表单提交时，打印发送的数据
const handleSubmit = async () => {
  try {
    const values = await form.validateFields();
    
    // 调试：打印表单数据
    console.log('Form values:', values);
    
    // 确保角色数据格式正确
    const userData = {
      ...values,
      roles: values.roles || [] // 确保roles是数组
    };
    
    console.log('Sending user data:', userData);
    
    if (modalType === 'add') {
      await UserService.createUser(userData);
      message.success('User created successfully');
    } else {
      await UserService.updateUser(currentUser.userId, userData);
      message.success('User updated successfully');
    }
    
    setModalVisible(false);
    fetchData();
  } catch (error) {
    console.error('Submit error:', error);
    console.error('Error response:', error.response?.data);
    message.error('Operation failed');
  }
};

  // 处理编辑用户
  const handleEdit = (record) => {
    setCurrentUser(record);
    setModalType('edit');
    form.setFieldsValue({
      ...record,
      companyId: record.company?.companyId,
      // 不设置密码字段
    });
    setModalVisible(true);
  };

  // 处理删除用户
  const handleDelete = async (id) => {
    try {
      await UserService.deleteUser(id);
      message.success('User deleted successfully');
      fetchData();
    } catch (error) {
      message.error('Failed to delete user');
    }
  };

  // 表格列定义
  const columns = [
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: 'Full Name',
      dataIndex: 'fullName',
      key: 'fullName',
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Company',
      dataIndex: 'company',
      key: 'company',
      render: (company) => company?.companyName,
    },
    {
      title: 'Roles',
      dataIndex: 'roles',
      key: 'roles',
      render: (_, record) => (
        <>
          {record.roles?.map(role => (
            <Tag color="blue" key={role.name || role}>
              {role.name || role}
            </Tag>
          ))}
        </>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled) => (
        <Tag color={enabled ? 'green' : 'red'}>
          {enabled ? 'Active' : 'Inactive'}
        </Tag>
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
          <Button danger onClick={() => handleDelete(record.userId)}>
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>User Management</h2>
      
      <Button 
        type="primary" 
        style={{ marginBottom: 16 }}
        onClick={() => {
          setModalType('add');
          setCurrentUser(null);
          form.resetFields();
          setModalVisible(true);
        }}
      >
        Add User
      </Button>
      
      <Table 
        columns={columns} 
        dataSource={users} 
        rowKey="userId"
        loading={loading}
      />
      
      <Modal
        title={modalType === 'add' ? 'Add User' : 'Edit User'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="username"
            label="Username"
            rules={[{ required: true, message: 'Please input username!' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="fullName"
            label="Full Name"
            rules={[{ required: true, message: 'Please input full name!' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: 'Please input email!' },
              { type: 'email', message: 'Please enter a valid email!' }
            ]}
          >
            <Input />
          </Form.Item>
          
          {modalType === 'add' && (
            <Form.Item
              name="password"
              label="Password"
              rules={[{ required: true, message: 'Please input password!' }]}
            >
              <Input.Password />
            </Form.Item>
          )}
          
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
            name="roles"
            label="Roles"
          >
            <Select mode="multiple">
              {roles.map(role => (
                <Option key={role} value={role}>
                  {role}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="enabled"
            label="Status"
            valuePropName="checked"
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

export default UserManagement;