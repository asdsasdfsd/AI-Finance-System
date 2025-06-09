import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Table, DatePicker, InputNumber, Button, message, Card, Row, Col, Space } from 'antd';
import dayjs from 'dayjs';

const IncomeExpenseReport = () => {
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await axios.get('/api/financial-report/json', {
        params: {
          companyId,
          asOfDate: asOfDate.format('YYYY-MM-DD')
        }
      });
      setData(res.data);
    } catch (err) {
      message.error('获取报表数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const res = await axios.get('/api/financial-report/export', {
        params: {
          companyId,
          asOfDate: asOfDate.format('YYYY-MM-DD')
        },
        responseType: 'blob'
      });
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `IncomeExpenseReport_${asOfDate.format('YYYYMMDD')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      message.error('导出失败');
    }
  };

  useEffect(() => {
    fetchData();
  }, [companyId, asOfDate]);

  const columns = [
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      filters: [
        { text: 'INCOME', value: 'INCOME' },
        { text: 'EXPENSE', value: 'EXPENSE' }
      ],
      onFilter: (value, record) => record.type === value
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
    },
    {
      title: 'Item',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: 'Current Month',
      dataIndex: 'currentMonth',
      key: 'currentMonth',
      align: 'right',
      render: val => val?.toLocaleString(undefined, { minimumFractionDigits: 2 })
    },
    {
      title: 'Previous Month',
      dataIndex: 'previousMonth',
      key: 'previousMonth',
      align: 'right',
      render: val => val?.toLocaleString(undefined, { minimumFractionDigits: 2 })
    },
    {
      title: 'YTD',
      dataIndex: 'yearToDate',
      key: 'yearToDate',
      align: 'right',
      render: val => val?.toLocaleString(undefined, { minimumFractionDigits: 2 })
    },
    {
      title: 'Budget YTD',
      dataIndex: 'budgetYtd',
      key: 'budgetYtd',
      align: 'right',
      render: val => val?.toLocaleString(undefined, { minimumFractionDigits: 2 })
    },
    {
      title: 'Variance',
      dataIndex: 'variance',
      key: 'variance',
      align: 'right',
      render: val => val?.toLocaleString(undefined, { minimumFractionDigits: 2 })
    },
    {
      title: 'Full Year Budget',
      dataIndex: 'fullYearBudget',
      key: 'fullYearBudget',
      align: 'right',
      render: val => val?.toLocaleString(undefined, { minimumFractionDigits: 2 })
    }
  ];

  return (
    <Card
      title="Income & Expense Report"
      style={{ margin: 24 }}
      extra={
        <Row gutter={12} align="middle">
          <Col>
            <InputNumber
              min={1}
              value={companyId}
              onChange={setCompanyId}
              placeholder="Company ID"
            />
          </Col>
          <Col>
            <DatePicker
              value={asOfDate}
              onChange={setAsOfDate}
              format="YYYY-MM-DD"
            />
          </Col>
          <Col>
            <Space>
              <Button type="primary" onClick={fetchData}>Refresh</Button>
              <Button onClick={handleExport}>Export Excel</Button>
            </Space>
          </Col>
        </Row>
      }
    >
      <Table
        rowKey={(record, index) => `${record.type}-${record.category}-${record.description}-${index}`}
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{ pageSize: 20 }}
        bordered
      />
    </Card>
  );
};

export default IncomeExpenseReport;
