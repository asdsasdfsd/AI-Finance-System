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

  // Currently hardcoded companyId - should be dynamic from user context
  const currentCompanyId = 1;

  // Load user list and company list
  const fetchData = async () => {
    setLoading(true);
    try {
      console.log('Fetching data...');
      
      const [usersData, companiesData] = await Promise.all([
        UserService.getUsersByCompany(currentCompanyId), // Use specific company
        CompanyService.getAllCompanies()
      ]);
      
      console.log('Users data:', usersData);
      console.log('Companies data:', companiesData);
      
      setUsers(usersData || []);
      setCompanies(companiesData || []);
    } catch (error) {
      console.error('Failed to fetch data:', error);
      message.error('Failed to fetch data: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Handle form submission
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      console.log('Form values:', values);
      
      // Ensure role data format is correct
      const userData = {
        ...values,
        roles: values.roles || [], // Ensure roles is an array
        roleNames: values.roles || [] // Backend expects roleNames field
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
      const errorMessage = error.response?.data?.message || 'Operation failed';
      message.error(errorMessage);
    }
  };

  // Handle edit user
  const handleEdit = (record) => {
    console.log('Editing user:', record);
    setCurrentUser(record);
    setModalType('edit');
    
    // Extract user role names array
    const userRoles = record.roles ? record.roles.map(role => 
      typeof role === 'string' ? role : role.name
    ) : [];
    
    console.log('User roles:', userRoles);
    
    // Set form values, ensure roles are displayed correctly
    const formValues = {
      username: record.username,
      fullName: record.fullName,
      email: record.email,
      companyId: record.company?.companyId || record.companyId,
      roles: userRoles, // Set roles array
      enabled: record.enabled
    };
    
    console.log('Setting form values:', formValues);
    
    // Delay setting form values to ensure modal is fully rendered
    setTimeout(() => {
      form.setFieldsValue(formValues);
    }, 100);
    
    setModalVisible(true);
  };

  // Handle delete user
  const handleDelete = async (id) => {
    try {
      await UserService.deleteUser(id);
      message.success('User deleted successfully');
      fetchData();
    } catch (error) {
      console.error('Delete error:', error);
      message.error('Failed to delete user: ' + (error.response?.data?.message || error.message));
    }
  };

  // Handle add user
  const handleAdd = () => {
    setModalType('add');
    setCurrentUser(null);
    form.resetFields();
    
    // Set default values
    const defaultValues = { 
      enabled: true,
      roles: [],
      companyId: currentCompanyId
    };
    
    // Delay setting form values to ensure modal is fully rendered
    setTimeout(() => {
      form.setFieldsValue(defaultValues);
    }, 100);
    
    setModalVisible(true);
  };

  // Table column definitions
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
        maskClosable={false}
      >
        <Form 
          form={form} 
          layout="vertical"
          preserve={false}
          initialValues={{ 
            enabled: true,
            roles: [],
            companyId: currentCompanyId
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