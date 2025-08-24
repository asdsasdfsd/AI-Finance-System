// frontend/src/views/Dashboard/AssetManagement.js
import React, { useEffect, useState } from 'react';
import {
  Table, Button, Modal, Form, Input, InputNumber, DatePicker,
  Select, Switch, Space, message, Card, Typography, Alert, Popconfirm
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  AppstoreOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import AssetService from '../../services/assetService';
import AuthService from '../../services/authService';

const { Text, Title } = Typography;
const { Option } = Select;

const AssetManagement = () => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingAsset, setEditingAsset] = useState(null);
  const [form] = Form.useForm();
  const [error, setError] = useState(null);

  // Get current user info for display
  const currentUser = AuthService.getCurrentUser();
  const userCompany = currentUser?.user?.companyName || currentUser?.companyName || 'Current Company';

  const fetchAssets = async () => {
    setLoading(true);
    setError(null);
    
    try {
      console.log('[AssetManagement] Fetching assets for current user\'s company');
      
      // Check authentication first
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication required. Please login first.');
      }

      const response = await AssetService.getAllAssets();
      console.log('[AssetManagement] Assets fetched successfully:', response.data);
      
      setAssets(response.data || []);
      
      if (!response.data || response.data.length === 0) {
        message.info('No assets found for your company. Add your first asset!');
      }
      
    } catch (error) {
      console.error('[AssetManagement] Error fetching assets:', error);
      setError(error.message);
      
      if (error.message.includes('authentication') || error.message.includes('401')) {
        message.error('Authentication failed. Please login again.');
      } else if (error.message.includes('403')) {
        message.error('Access denied. You don\'t have permission to view assets.');
      } else if (error.message.includes('Company ID')) {
        message.error('Company information not found. Please contact support.');
      } else {
        message.error('Failed to load asset data. Please try again.');
      }
      
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAssets();
  }, []);

  const openModal = (record = null) => {
    setEditingAsset(record);
    if (record) {
      form.setFieldsValue({
        ...record,
        acquisitionDate: record.acquisitionDate ? dayjs(record.acquisitionDate) : null
      });
    } else {
      form.resetFields();
      // Set default values for new asset
      form.setFieldsValue({
        status: 'ACTIVE',
        acquisitionCost: 0,
        currentValue: 0,
        accumulatedDepreciation: 0
      });
    }
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      console.log(`[AssetManagement] Deleting asset ${id}`);
      await AssetService.deleteAsset(id);
      message.success('Asset deleted successfully');
      await fetchAssets(); // Refresh the list
    } catch (error) {
      console.error('[AssetManagement] Delete error:', error);
      if (error.response?.status === 403) {
        message.error('Access denied. You can only delete assets from your company.');
      } else if (error.response?.status === 404) {
        message.error('Asset not found or already deleted.');
      } else {
        message.error('Failed to delete asset. Please try again.');
      }
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      console.log('[AssetManagement] Submitting asset data:', values);

      // Validate required fields
      if (!values.name || values.name.trim() === '') {
        message.error('Asset name is required');
        return;
      }

      // Clean and prepare data
      const assetData = {
        name: values.name.trim(),
        description: values.description?.trim() || '',
        acquisitionDate: values.acquisitionDate ? values.acquisitionDate.format('YYYY-MM-DD') : null,
        acquisitionCost: parseFloat(values.acquisitionCost) || 0,
        currentValue: parseFloat(values.currentValue) || parseFloat(values.acquisitionCost) || 0,
        accumulatedDepreciation: parseFloat(values.accumulatedDepreciation) || 0,
        location: values.location?.trim() || '',
        serialNumber: values.serialNumber?.trim() || '',
        status: values.status || 'ACTIVE',
        department: values.departmentId ? { departmentId: values.departmentId } : null
      };

      if (editingAsset) {
        console.log(`[AssetManagement] Updating asset ${editingAsset.assetId}`);
        await AssetService.updateAsset(editingAsset.assetId, assetData);
        message.success('Asset updated successfully');
      } else {
        console.log('[AssetManagement] Creating new asset');
        await AssetService.createAsset(assetData);
        message.success('Asset created successfully');
      }

      setModalVisible(false);
      await fetchAssets(); // Refresh the list
      
    } catch (error) {
      console.error('[AssetManagement] Submit error:', error);
      
      if (error.response?.status === 403) {
        message.error('Access denied. You can only manage assets for your company.');
      } else if (error.response?.status === 400) {
        const errorMsg = error.response?.data?.message || 'Invalid asset data';
        message.error(`Validation error: ${errorMsg}`);
      } else if (error.response?.data?.message) {
        message.error(error.response.data.message);
      } else {
        message.error('Failed to save asset. Please try again.');
      }
    }
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingAsset(null);
    form.resetFields();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'DISPOSED': return 'warning';
      case 'WRITTEN_OFF': return 'danger';
      default: return 'default';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'ACTIVE': return 'Active';
      case 'DISPOSED': return 'Disposed';
      case 'WRITTEN_OFF': return 'Written Off';
      default: return status;
    }
  };

  const columns = [
    {
      title: 'Asset Name',
      dataIndex: 'name',
      key: 'name',
      render: (text) => <strong>{text}</strong>
    },
    {
      title: 'Location',
      dataIndex: 'location',
      key: 'location',
      render: (text) => text || '-'
    },
    {
      title: 'Serial Number',
      dataIndex: 'serialNumber',
      key: 'serialNumber',
      render: (text) => text || '-'
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Text type={getStatusColor(status)}>
          {getStatusText(status)}
        </Text>
      ),
      filters: [
        { text: 'Active', value: 'ACTIVE' },
        { text: 'Disposed', value: 'DISPOSED' },
        { text: 'Written Off', value: 'WRITTEN_OFF' }
      ],
      onFilter: (value, record) => record.status === value
    },
    {
      title: 'Current Value',
      dataIndex: 'currentValue',
      key: 'currentValue',
      render: (val) => (
        <Text>
          ¥ {Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
        </Text>
      ),
      sorter: (a, b) => (a.currentValue || 0) - (b.currentValue || 0)
    },
    {
      title: 'Acquisition Date',
      dataIndex: 'acquisitionDate',
      key: 'acquisitionDate',
      render: (date) => date ? dayjs(date).format('YYYY-MM-DD') : '-',
      sorter: (a, b) => {
        if (!a.acquisitionDate && !b.acquisitionDate) return 0;
        if (!a.acquisitionDate) return 1;
        if (!b.acquisitionDate) return -1;
        return new Date(a.acquisitionDate) - new Date(b.acquisitionDate);
      }
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
            title="Delete Asset"
            description="Are you sure you want to delete this asset? This action cannot be undone."
            onConfirm={() => handleDelete(record.assetId)}
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
          <AppstoreOutlined style={{ color: '#1890ff' }} />
          <span>Asset Management - {userCompany}</span>
        </Space>
      }
      extra={
        <Space>
          <Button 
            icon={<ReloadOutlined />} 
            onClick={fetchAssets}
            loading={loading}
          >
            Refresh
          </Button>
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={() => openModal()}
          >
            Add Asset
          </Button>
        </Space>
      }
      style={{ margin: 24 }}
    >
      {error && (
        <Alert
          message="Error Loading Assets"
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
        rowKey="assetId"
        columns={columns}
        dataSource={assets}
        loading={loading}
        pagination={{ 
          pageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `Total ${total} assets`
        }}
        size="middle"
        scroll={{ x: true }}
      />

      <Modal
        title={
          <Space>
            <AppstoreOutlined />
            {editingAsset ? 'Edit Asset' : 'Add New Asset'}
          </Space>
        }
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={handleModalCancel}
        okText={editingAsset ? 'Update Asset' : 'Create Asset'}
        cancelText="Cancel"
        width={700}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
        >
          <Form.Item
            label="Asset Name"
            name="name"
            rules={[
              { required: true, message: 'Please enter asset name' },
              { max: 200, message: 'Asset name cannot exceed 200 characters' }
            ]}
          >
            <Input placeholder="Enter asset name" />
          </Form.Item>

          <Form.Item
            label="Description"
            name="description"
          >
            <Input.TextArea 
              rows={3}
              placeholder="Enter asset description (optional)"
              maxLength={500}
            />
          </Form.Item>

          <div style={{ display: 'flex', gap: '16px' }}>
            <Form.Item
              label="Location"
              name="location"
              style={{ flex: 1 }}
            >
              <Input placeholder="Asset location" />
            </Form.Item>

            <Form.Item
              label="Serial Number"
              name="serialNumber"
              style={{ flex: 1 }}
            >
              <Input placeholder="Serial/Model number" />
            </Form.Item>
          </div>

          <div style={{ display: 'flex', gap: '16px' }}>
            <Form.Item
              label="Acquisition Date"
              name="acquisitionDate"
              style={{ flex: 1 }}
            >
              <DatePicker 
                style={{ width: '100%' }}
                placeholder="Select acquisition date"
              />
            </Form.Item>

            <Form.Item
              label="Status"
              name="status"
              style={{ flex: 1 }}
            >
              <Select placeholder="Select asset status">
                <Option value="ACTIVE">Active</Option>
                <Option value="DISPOSED">Disposed</Option>
                <Option value="WRITTEN_OFF">Written Off</Option>
              </Select>
            </Form.Item>
          </div>

          <div style={{ display: 'flex', gap: '16px' }}>
            <Form.Item
              label="Acquisition Cost"
              name="acquisitionCost"
              style={{ flex: 1 }}
              rules={[
                { type: 'number', min: 0, message: 'Cost cannot be negative' }
              ]}
            >
              <InputNumber
                style={{ width: '100%' }}
                placeholder="Acquisition cost"
                precision={2}
                formatter={(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={(value) => value.replace(/¥\s?|(,*)/g, '')}
              />
            </Form.Item>

            <Form.Item
              label="Current Value"
              name="currentValue"
              style={{ flex: 1 }}
              rules={[
                { type: 'number', min: 0, message: 'Value cannot be negative' }
              ]}
            >
              <InputNumber
                style={{ width: '100%' }}
                placeholder="Current value"
                precision={2}
                formatter={(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={(value) => value.replace(/¥\s?|(,*)/g, '')}
              />
            </Form.Item>
          </div>

          <Form.Item
            label="Accumulated Depreciation"
            name="accumulatedDepreciation"
            rules={[
              { type: 'number', min: 0, message: 'Depreciation cannot be negative' }
            ]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="Accumulated depreciation"
              precision={2}
              formatter={(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value.replace(/¥\s?|(,*)/g, '')}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default AssetManagement;