import React, { useEffect, useState } from 'react';
import {
  Table, Button, Popconfirm, Form, Input, InputNumber, Switch, Space, message, Card, Typography, Modal
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FundOutlined
} from '@ant-design/icons';
import FundService from '../../services/fundService';

const { Text } = Typography;

const FundManagement = () => {
  const [funds, setFunds] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingFund, setEditingFund] = useState(null);
  const [form] = Form.useForm();

  const companyId = 2; // TODO: replace with dynamic value from user context

  const fetchFunds = async () => {
    setLoading(true);
    try {
      const res = await FundService.getFundsByCompany(companyId);
      setFunds(res.data);
    } catch {
      message.error('Failed to fetch fund data.');
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
      form.setFieldsValue(record);
    } else {
      form.resetFields();
    }
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await FundService.deleteFund(id);
      message.success('Deleted successfully.');
      fetchFunds();
    } catch (err) {
      console.error('Delete error:', err);
      message.error('Failed to delete fund.');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      values.company = { companyId };

      if (editingFund) {
        await FundService.updateFund(editingFund.fundId, values);
        message.success('Updated successfully.');
      } else {
        await FundService.createFund(values);
        message.success('Created successfully.');
      }

      await fetchFunds(); // 保证数据更新后再关闭
      setModalVisible(false);
    } catch {
      message.error('Submission failed.');
    }
  };

  const columns = [
    {
      title: 'Fund Name',
      dataIndex: 'name',
    },
    {
      title: 'Fund Type',
      dataIndex: 'fundType',
    },
    {
      title: 'Balance',
      dataIndex: 'balance',
      render: (val) => `¥ ${val}`,
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      render: (val) => <Text type={val ? 'success' : 'secondary'}>{val ? 'Active' : 'Inactive'}</Text>,
    },
    {
      title: 'Actions',
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} type="link" onClick={() => openModal(record)}>
            Edit
          </Button>
          <Popconfirm
            title="Are you sure you want to delete this fund?"
            onConfirm={() => handleDelete(record.fundId)}
            okText="Yes"
            cancelText="Cancel"
          >
            <Button icon={<DeleteOutlined />} danger type="link">
              Delete
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title={<><FundOutlined style={{ marginRight: 8 }} />Fund Management</>}
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>
          Add Fund
        </Button>
      }
      style={{ margin: 24 }}
    >
      <Table
        bordered
        rowKey="fundId"
        columns={columns}
        dataSource={funds}
        loading={loading}
        pagination={{ pageSize: 6 }}
      />

      <Modal
        title={editingFund ? 'Edit Fund' : 'Add Fund'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        okText="Save"
        cancelText="Cancel"
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Fund Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Operational Fund" />
          </Form.Item>
          <Form.Item name="fundType" label="Fund Type" rules={[{ required: true }]}>
            <Input placeholder="e.g. Internal / External" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea />
          </Form.Item>
          <Form.Item name="balance" label="Balance" rules={[{ required: true }]}>
            <InputNumber min={0} precision={2} style={{ width: '100%' }} prefix="¥" />
          </Form.Item>
          <Form.Item name="isActive" label="Active" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default FundManagement;

