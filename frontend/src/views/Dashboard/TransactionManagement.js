import React, { useEffect, useState } from 'react';
import {
  Table, Button, Modal, Form, Input, InputNumber, DatePicker, Select,
  Space, message, Card, Typography, Switch
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, DollarCircleOutlined, FilterOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import TransactionService from '../../services/transactionService';

const { Text } = Typography;
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

  useEffect(() => {
    fetchData();
  }, [filterType, dateRange]);

  const fetchData = async () => {
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
  };

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
          <Button icon={<DeleteOutlined />} danger type="link" onClick={() => TransactionService.deleteTransaction(record.transactionId).then(fetchData)}>Delete</Button>
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
          {/* 表单字段略，如前所述 */}
        </Form>
      </Modal>
    </Card>
  );
};

export default TransactionManagement;


