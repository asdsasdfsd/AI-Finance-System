import React, { useEffect, useState } from 'react';
import {
  Table, Button, Modal, Form, Input, InputNumber, DatePicker,
  Select, Switch, Space, message, Card, Typography
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  AppstoreOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import AssetService from '../../services/assetService';

const { Text } = Typography;
const { Option } = Select;

const AssetManagement = () => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingAsset, setEditingAsset] = useState(null);
  const [form] = Form.useForm();

  const companyId = 2; // TODO: use dynamic companyId from user context

  const fetchAssets = async () => {
    setLoading(true);
    try {
      const res = await AssetService.getByCompany(companyId);
      setAssets(res.data);
    } catch {
      message.error('Failed to load asset data.');
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
    }
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await AssetService.deleteAsset(id);
      message.success('Deleted successfully');
      fetchAssets();
    } catch {
      message.error('Failed to delete asset');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      values.company = { companyId };
      if (values.acquisitionDate) {
        values.acquisitionDate = values.acquisitionDate.format('YYYY-MM-DD');
      }

      if (editingAsset) {
        await AssetService.updateAsset(editingAsset.assetId, values);
        message.success('Updated successfully');
      } else {
        await AssetService.createAsset(values);
        message.success('Created successfully');
      }

      setModalVisible(false);
      fetchAssets();
    } catch {
      message.error('Submission failed');
    }
  };

  const columns = [
    { title: 'Name', dataIndex: 'name' },
    { title: 'Location', dataIndex: 'location' },
    { title: 'Serial Number', dataIndex: 'serialNumber' },
    { title: 'Status', dataIndex: 'status' },
    {
      title: 'Current Value',
      dataIndex: 'currentValue',
      render: val => `¥ ${val}`
    },
    {
      title: 'Actions',
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} type="link" onClick={() => openModal(record)}>Edit</Button>
          <Button icon={<DeleteOutlined />} danger type="link" onClick={() => handleDelete(record.assetId)}>Delete</Button>
        </Space>
      )
    }
  ];

  return (
    <Card
      title={<><AppstoreOutlined style={{ marginRight: 8 }} />Asset Management</>}
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>Add Asset</Button>}
      style={{ margin: 24 }}
    >
      <Table
        bordered
        rowKey="assetId"
        columns={columns}
        dataSource={assets}
        loading={loading}
        pagination={{ pageSize: 6 }}
      />

      <Modal
        title={editingAsset ? 'Edit Asset' : 'Add Asset'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        okText="Save"
        cancelText="Cancel"
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Asset Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="location" label="Location">
            <Input />
          </Form.Item>
          <Form.Item name="serialNumber" label="Serial Number">
            <Input />
          </Form.Item>
          <Form.Item name="acquisitionDate" label="Acquisition Date">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="acquisitionCost" label="Acquisition Cost">
            <InputNumber style={{ width: '100%' }} min={0} prefix="¥" />
          </Form.Item>
          <Form.Item name="currentValue" label="Current Value">
            <InputNumber style={{ width: '100%' }} min={0} prefix="¥" />
          </Form.Item>
          <Form.Item name="accumulatedDepreciation" label="Accumulated Depreciation">
            <InputNumber style={{ width: '100%' }} min={0} prefix="¥" />
          </Form.Item>
          <Form.Item name="status" label="Status" rules={[{ required: true }]}>
            <Select>
              <Option value="ACTIVE">Active</Option>
              <Option value="DISPOSED">Disposed</Option>
              <Option value="WRITTEN_OFF">Written Off</Option>
            </Select>
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default AssetManagement;
