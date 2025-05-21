// frontend/src/views/Dashboard/CompanyManagement.js
import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Space, message } from 'antd';
import CompanyService from '../../services/companyService';

const CompanyManagement = () => {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [currentCompany, setCurrentCompany] = useState(null);
  const [form] = Form.useForm();

  // 加载公司列表
  const fetchCompanies = async () => {
    setLoading(true);
    try {
      const data = await CompanyService.getAllCompanies();
      setCompanies(data);
    } catch (error) {
      message.error('Failed to fetch companies');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCompanies();
  }, []);

  // 编辑公司
  const handleEdit = (record) => {
    setCurrentCompany(record);
    form.setFieldsValue(record);
    setEditModalVisible(true);
  };

  // 保存编辑
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      await CompanyService.updateCompany(currentCompany.companyId, values);
      message.success('Company updated successfully');
      setEditModalVisible(false);
      fetchCompanies();
    } catch (error) {
      message.error('Failed to update company');
    }
  };

  // 表格列定义
  const columns = [
    {
      title: 'Company Name',
      dataIndex: 'companyName',
      key: 'companyName',
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Address',
      dataIndex: 'address',
      key: 'address',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
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
      <h2>Company Management</h2>
      
      <Button 
        type="primary" 
        style={{ marginBottom: 16 }}
        onClick={() => {
          setCurrentCompany(null);
          form.resetFields();
          setEditModalVisible(true);
        }}
      >
        Add Company
      </Button>
      
      <Table 
        columns={columns} 
        dataSource={companies} 
        rowKey="companyId"
        loading={loading}
      />
      
      <Modal
        title={currentCompany ? "Edit Company" : "Add Company"}
        open={editModalVisible}
        onOk={handleSave}
        onCancel={() => setEditModalVisible(false)}
      >
        <Form 
          form={form}
          layout="vertical"
        >
          <Form.Item
            name="companyName"
            label="Company Name"
            rules={[{ required: true, message: 'Please input company name!' }]}
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
          
          <Form.Item
            name="address"
            label="Address"
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="city"
            label="City"
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="status"
            label="Status"
          >
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CompanyManagement;