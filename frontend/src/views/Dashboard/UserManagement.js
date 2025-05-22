// frontend/src/views/Dashboard/UserManagement.js
import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message } from 'antd';
import UserService from '../../services/userService';
import CompanyService from '../../services/companyService';

const { Option } = Select;

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [companies, setCompanies] = useState([]);
  const roles = ['USER', 'FINANCE_MANAGER', 'COMPANY_ADMIN', 'SYSTEM_ADMIN'];
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
      form.resetFields();
      setCurrentUser(null);
      fetchData();
    } catch (error) {
      console.error('Submit error:', error);
      console.error('Error response:', error.response?.data);
      message.error('Operation failed');
    }
  };

  // 处理编辑用户
  const handleEdit = (record) => {
    console.log('Editing user:', record);
    setCurrentUser(record);
    setModalType('edit');
    
    // 提取用户角色名称数组
    const userRoles = record.roles ? record.roles.map(role => 
      typeof role === 'string' ? role : role.name
    ) : [];
    
    console.log('User roles:', userRoles);
    
    // 设置表单值，确保角色正确显示
    const formValues = {
      username: record.username,
      fullName: record.fullName,
      email: record.email,
      companyId: record.company?.companyId,
      roles: userRoles, // 设置角色数组
      enabled: record.enabled
    };
    
    console.log('Setting form values:', formValues);
    form.setFieldsValue(formValues);
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

  // 处理新增用户
  const handleAdd = () => {
    setModalType('add');
    setCurrentUser(null);
    form.resetFields();
    // 设置默认值
    form.setFieldsValue({ 
      enabled: true,
      roles: [] 
    });
    setModalVisible(true);
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
      render: (company) => company?.companyName || 'N/A',
    },
    {
      title: 'Roles',
      dataIndex: 'roles',
      key: 'roles',
      render: (_, record) => (
        <>
          {record.roles?.map(role => {
            const roleName = typeof role === 'string' ? role : role.name;
            return (
              <Tag color="blue" key={roleName}>
                {roleName}
              </Tag>
            );
          })}
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
        onClick={handleAdd}
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
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setCurrentUser(null);
        }}
        width={600}
        destroyOnClose={true}
      >
        <Form 
          form={form} 
          layout="vertical"
          preserve={false}
          initialValues={{ 
            enabled: true,
            roles: [] 
          }}
        >
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
            <Select placeholder="Select company">
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
            <Select 
              mode="multiple" 
              placeholder="Select roles"
              allowClear
            >
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
            rules={[{ required: true, message: 'Please select status!' }]}
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

export default UserManagement;