// frontend/src/views/Dashboard/FundManagement.js
import React, { useEffect, useState } from 'react';
import {
  Table, Button, Popconfirm, Form, Input, InputNumber, Switch, Space, 
  message, Card, Typography, Modal, Alert, Spin
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FundOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import FundService from '../../services/fundService';
import AuthService from '../../services/authService';

const { Text, Title } = Typography;

const FundManagement = () => {
  const [funds, setFunds] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingFund, setEditingFund] = useState(null);
  const [form] = Form.useForm();
  const [error, setError] = useState(null);

  // Get current user info for display
  const currentUser = AuthService.getCurrentUser();
  const userCompany = currentUser?.user?.companyName || currentUser?.companyName || 'Current Company';

  const fetchFunds = async () => {
    setLoading(true);
    setError(null);
    
    try {
      console.log('[FundManagement] Fetching funds for current user\'s company');
      
      // Check authentication first
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication required. Please login first.');
      }

      const response = await FundService.getAllFunds();
      console.log('[FundManagement] Funds fetched successfully:', response.data);
      
      setFunds(response.data || []);
      
      if (!response.data || response.data.length === 0) {
        message.info('No funds found for your company. Create your first fund!');
      }
      
    } catch (error) {
      console.error('[FundManagement] Error fetching funds:', error);
      setError(error.message);
      
      if (error.message.includes('authentication') || error.message.includes('401')) {
        message.error('Authentication failed. Please login again.');
        // Redirect to login or refresh token
      } else if (error.message.includes('403')) {
        message.error('Access denied. You don\'t have permission to view funds.');
      } else if (error.message.includes('Company ID')) {
        message.error('Company information not found. Please contact support.');
      } else {
        message.error('Failed to fetch fund data. Please try again.');
      }
      
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFunds();
  }, []);

  const openModal = (record = null) => {
    setEditingFund(record);
    if (record) {
      form.setFieldsValue({
        name: record.name,
        description: record.description,
        fundType: record.fundType,
        balance: record.balance,
        isActive: record.isActive
      });
    } else {
      form.resetFields();
      // Set default values for new fund
      form.setFieldsValue({
        isActive: true,
        balance: 0
      });
    }
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      console.log(`[FundManagement] Deleting fund ${id}`);
      await FundService.deleteFund(id);
      message.success('Fund deleted successfully');
      await fetchFunds(); // Refresh the list
    } catch (error) {
      console.error('[FundManagement] Delete error:', error);
      if (error.response?.status === 403) {
        message.error('Access denied. You can only delete funds from your company.');
      } else if (error.response?.status === 404) {
        message.error('Fund not found or already deleted.');
      } else {
        message.error('Failed to delete fund. Please try again.');
      }
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      console.log('[FundManagement] Submitting fund data:', values);

      // Validate required fields
      if (!values.name || values.name.trim() === '') {
        message.error('Fund name is required');
        return;
      }

      // Clean and prepare data
      const fundData = {
        name: values.name.trim(),
        description: values.description?.trim() || '',
        fundType: values.fundType?.trim() || 'GENERAL',
        balance: parseFloat(values.balance) || 0,
        isActive: values.isActive !== false // Default to true
      };

      if (editingFund) {
        console.log(`[FundManagement] Updating fund ${editingFund.fundId}`);
        await FundService.updateFund(editingFund.fundId, fundData);
        message.success('Fund updated successfully');
      } else {
        console.log('[FundManagement] Creating new fund');
        await FundService.createFund(fundData);
        message.success('Fund created successfully');
      }

      setModalVisible(false);
      await fetchFunds(); // Refresh the list
      
    } catch (error) {
      console.error('[FundManagement] Submit error:', error);
      
      if (error.response?.status === 403) {
        message.error('Access denied. You can only manage funds for your company.');
      } else if (error.response?.status === 400) {
        const errorMsg = error.response?.data?.message || 'Invalid fund data';
        message.error(`Validation error: ${errorMsg}`);
      } else if (error.response?.data?.message) {
        message.error(error.response.data.message);
      } else {
        message.error('Failed to save fund. Please try again.');
      }
    }
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingFund(null);
    form.resetFields();
  };

  const columns = [
    {
      title: 'Fund Name',
      dataIndex: 'name',
      key: 'name',
      render: (text) => <strong>{text}</strong>
    },
    {
      title: 'Fund Type',
      dataIndex: 'fundType',
      key: 'fundType',
      render: (text) => text || 'General'
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (text) => text || '-'
    },
    {
      title: 'Balance',
      dataIndex: 'balance',
      key: 'balance',
      render: (val) => (
        <Text type={val >= 0 ? 'success' : 'danger'}>
          ¥ {Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
        </Text>
      ),
      sorter: (a, b) => (a.balance || 0) - (b.balance || 0)
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (val) => (
        <Text type={val ? 'success' : 'secondary'}>
          {val ? 'Active' : 'Inactive'}
        </Text>
      ),
      filters: [
        { text: 'Active', value: true },
        { text: 'Inactive', value: false }
      ],
      onFilter: (value, record) => record.isActive === value
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Space>
          <Button 
            icon={<EditOutlined />} 
            type="link" 
            size="small"
            onClick={() => openModal(record)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Delete Fund"
            description="Are you sure you want to delete this fund? This action cannot be undone."
            onConfirm={() => handleDelete(record.fundId)}
            okText="Yes, Delete"
            cancelText="Cancel"
            okType="danger"
          >
            <Button 
              icon={<DeleteOutlined />} 
              type="link" 
              danger
              size="small"
            >
              Delete
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <Card
      title={
        <Space>
          <FundOutlined style={{ color: '#1890ff' }} />
          <span>Fund Management - {userCompany}</span>
        </Space>
      }
      extra={
        <Space>
          <Button 
            icon={<ReloadOutlined />} 
            onClick={fetchFunds}
            loading={loading}
          >
            Refresh
          </Button>
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={() => openModal()}
          >
            Add Fund
          </Button>
        </Space>
      }
      style={{ margin: 24 }}
    >
      {error && (
        <Alert
          message="Error Loading Funds"
          description={error}
          type="error"
          showIcon
          closable
          onClose={() => setError(null)}
          style={{ marginBottom: 16 }}
        />
      )}

      <Table
        bordered
        rowKey="fundId"
        columns={columns}
        dataSource={funds}
        loading={loading}
        pagination={{ 
          pageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `Total ${total} funds`
        }}
        size="middle"
        scroll={{ x: true }}
      />

      <Modal
        title={
          <Space>
            <FundOutlined />
            {editingFund ? 'Edit Fund' : 'Add New Fund'}
          </Space>
        }
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={handleModalCancel}
        okText={editingFund ? 'Update Fund' : 'Create Fund'}
        cancelText="Cancel"
        width={600}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
        >
          <Form.Item
            label="Fund Name"
            name="name"
            rules={[
              { required: true, message: 'Please enter fund name' },
              { max: 100, message: 'Fund name cannot exceed 100 characters' }
            ]}
          >
            <Input placeholder="Enter fund name" />
          </Form.Item>

          <Form.Item
            label="Fund Type"
            name="fundType"
          >
            <Input placeholder="e.g., Operating, Capital, Emergency" />
          </Form.Item>

          <Form.Item
            label="Description"
            name="description"
          >
            <Input.TextArea 
              rows={3}
              placeholder="Enter fund description (optional)"
              maxLength={500}
            />
          </Form.Item>

          <Form.Item
            label="Initial Balance"
            name="balance"
            rules={[
              { type: 'number', min: 0, message: 'Balance cannot be negative' }
            ]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="Enter initial balance"
              precision={2}
              formatter={(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value.replace(/¥\s?|(,*)/g, '')}
            />
          </Form.Item>

          <Form.Item
            name="isActive"
            valuePropName="checked"
          >
            <Switch checkedChildren="Active" unCheckedChildren="Inactive" />
            <span style={{ marginLeft: 8 }}>Fund Status</span>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default FundManagement;