// src/views/Dashboard/TransactionManagement.js

import React, { useEffect, useState, useCallback } from 'react';
import {
  Table, Button, Modal, Form, Input, InputNumber, DatePicker, Select,
  Space, message, Card, Switch
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, DollarCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import TransactionService from '../../services/transactionService';

const { RangePicker } = DatePicker;
const { Option } = Select;

const TransactionManagement = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [filterType, setFilterType] = useState('ALL');
  const [dateRange, setDateRange] = useState(null);
  const [form] = Form.useForm();

  const companyId = 2;
  const userId = 1;

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      let res;
      if (filterType === 'ALL') {
        res = await TransactionService.getAll();
      } else if (filterType === 'COMPANY_ALL') {
        res = await TransactionService.getByCompany(companyId);
      } else if (filterType === 'INCOME' || filterType === 'EXPENSE') {
        res = await TransactionService.getByCompanyAndType(companyId, filterType);
      } else if (filterType === 'SORTED') {
        res = await TransactionService.getByCompanySorted(companyId);
      } else if (filterType === 'DATE_RANGE' && dateRange?.length === 2) {
        const [start, end] = dateRange.map(d => d.format('YYYY-MM-DD'));
        res = await TransactionService.getByDateRange(companyId, start, end);
      }
      setTransactions(res?.data || []);
    } catch {
      message.error('Failed to load transactions');
    } finally {
      setLoading(false);
    }
  }, [filterType, dateRange, companyId]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleFilterChange = (val) => {
    setFilterType(val);
    if (val !== 'DATE_RANGE') setDateRange(null);
  };

  const openModal = (record = null) => {
    setEditingTransaction(record);
    form.setFieldsValue({
      ...record,
      transactionDate: record?.transactionDate ? dayjs(record.transactionDate) : null,
    });
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      values.company = { companyId };
      values.user = { userId };
      values.transactionDate = values.transactionDate?.format('YYYY-MM-DD');

      if (editingTransaction) {
        await TransactionService.updateTransaction(editingTransaction.transactionId, values);
        message.success('Updated');
      } else {
        await TransactionService.createTransaction(values);
        message.success('Created');
      }

      setModalVisible(false);
      fetchData();
    } catch {
      message.error('Submit failed');
    }
  };

  const handleDelete = async (transactionId) => {
    try {
      await TransactionService.deleteTransaction(transactionId);
      message.success('Deleted');
      fetchData();
    } catch {
      message.error('Delete failed');
    }
  };

  const columns = [
    { title: 'Type', dataIndex: 'transactionType' },
    { title: 'Amount', dataIndex: 'amount', render: val => `¥ ${val}` },
    { title: 'Currency', dataIndex: 'currency' },
    { title: 'Date', dataIndex: 'transactionDate' },
    { title: 'Description', dataIndex: 'description' },
    {
      title: 'Actions',
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} type="link" onClick={() => openModal(record)}>Edit</Button>
          <Button icon={<DeleteOutlined />} danger type="link" onClick={() => handleDelete(record.transactionId)}>Delete</Button>
        </Space>
      )
    }
  ];

  return (
    <Card
      title={<><DollarCircleOutlined style={{ marginRight: 8 }} />Transaction Management</>}
      style={{ margin: 24 }}
      extra={
        <Space>
          <Select value={filterType} onChange={handleFilterChange} style={{ width: 200 }}>
            <Option value="ALL">All Transactions</Option>
            <Option value="COMPANY_ALL">Company All</Option>
            <Option value="INCOME">Company INCOME</Option>
            <Option value="EXPENSE">Company EXPENSE</Option>
            <Option value="SORTED">Company Sorted by Date</Option>
            <Option value="DATE_RANGE">By Date Range</Option>
          </Select>
          {filterType === 'DATE_RANGE' && (
            <RangePicker onChange={(dates) => setDateRange(dates)} />
          )}
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>
            Add
          </Button>
        </Space>
      }
    >
      <Table
        bordered
        rowKey="transactionId"
        columns={columns}
        dataSource={transactions}
        loading={loading}
        pagination={{ pageSize: 6 }}
      />

      <Modal
        title={editingTransaction ? 'Edit Transaction' : 'Add Transaction'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        okText="Save"
        cancelText="Cancel"
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="transactionType" label="Type" rules={[{ required: true }]}>
            <Select>
              <Option value="INCOME">INCOME</Option>
              <Option value="EXPENSE">EXPENSE</Option>
            </Select>
          </Form.Item>

          <Form.Item name="amount" label="Amount" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} prefix="¥" />
          </Form.Item>

          <Form.Item name="currency" label="Currency" rules={[{ required: true }]}>
            <Input placeholder="e.g. CNY, USD" />
          </Form.Item>

          <Form.Item name="transactionDate" label="Date" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="paymentMethod" label="Payment Method">
            <Input />
          </Form.Item>

          <Form.Item name="referenceNumber" label="Reference Number">
            <Input />
          </Form.Item>

          <Form.Item name="isRecurring" label="Recurring" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item name="isTaxable" label="Taxable" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item name="description" label="Description">
            <Input.TextArea />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default TransactionManagement;
