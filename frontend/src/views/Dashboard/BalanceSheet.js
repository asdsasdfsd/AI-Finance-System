import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Button, Row, Col, Space, message, Table, Typography, DatePicker, InputNumber, Card } from 'antd';
import dayjs from 'dayjs';

const { Title } = Typography;

export default function BalanceSheet() {
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [response, setResponse] = useState(null);
  const [error, setError] = useState(null);

  const fetchBalanceSheet = async () => {
    try {
      const res = await axios.get('/api/balance-sheet/json', {
        params: {
          companyId,
          asOfDate: asOfDate.format('YYYY-MM-DD')
        }
      });
      setResponse(res.data);
      setError(null);
    } catch (err) {
      setError('Failed to load balance sheet.');
      setResponse(null);
    }
  };

  const handleExport = async () => {
    try {
      const res = await axios.get('/api/balance-sheet/export', {
        params: { companyId, asOfDate: asOfDate.format('YYYY-MM-DD') },
        responseType: 'blob'
      });

      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `BalanceSheet_${asOfDate.format('YYYY-MM-DD')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      message.error('导出失败，请稍后再试');
    }
  };

  useEffect(() => {
    fetchBalanceSheet();
  }, [companyId, asOfDate]);

  const renderTable = (sectionData) => {
    const tableData = Object.entries(sectionData).flatMap(([category, accounts]) =>
      accounts.map((acc, index) => ({
        key: `${category}-${index}`,
        category,
        account: acc.accountName,
        currentMonth: acc.currentMonth,
        previousMonth: acc.previousMonth,
        lastYearEnd: acc.lastYearEnd
      }))
    );

    const columns = [
      {
        title: 'Category',
        dataIndex: 'category',
        key: 'category'
      },
      {
        title: 'Account',
        dataIndex: 'account',
        key: 'account'
      },
      {
        title: 'Current Month',
        dataIndex: 'currentMonth',
        key: 'currentMonth',
        align: 'right',
        render: val => val.toLocaleString(undefined, { minimumFractionDigits: 2 })
      },
      {
        title: 'Previous Month',
        dataIndex: 'previousMonth',
        key: 'previousMonth',
        align: 'right',
        render: val => val.toLocaleString(undefined, { minimumFractionDigits: 2 })
      },
      {
        title: 'Last Year End',
        dataIndex: 'lastYearEnd',
        key: 'lastYearEnd',
        align: 'right',
        render: val => val.toLocaleString(undefined, { minimumFractionDigits: 2 })
      }
    ];

    return <Table dataSource={tableData} columns={columns} pagination={false} bordered />;
  };

  return (
    <Card
      title="Balance Sheet Viewer"
      extra={
        <Space>
          <InputNumber min={1} value={companyId} onChange={setCompanyId} />
          <DatePicker value={asOfDate} onChange={setAsOfDate} format="YYYY-MM-DD" />
          <Button type="primary" onClick={handleExport}>Export to Excel</Button>
        </Space>
      }
      style={{ margin: 24 }}
    >
      {error && <p className="text-red-500">{error}</p>}

      {response && (
        <div>
          <Title level={4}>Balance Sheet as at {response.asOfDate}</Title>

          <Title level={5}>Assets</Title>
          {renderTable(response.assets)}
          <p className="text-right font-bold">TOTAL ASSETS: {response.totalAssets.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>

          <Title level={5} style={{ marginTop: 24 }}>Liabilities</Title>
          {renderTable(response.liabilities)}
          <p className="text-right font-bold">TOTAL LIABILITIES: {response.totalLiabilities.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>

          <Title level={5} style={{ marginTop: 24 }}>Equity</Title>
          {renderTable(response.equity)}
          <p className="text-right font-bold">TOTAL EQUITY: {response.totalEquity.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>

          <p className="mt-4 font-semibold text-right">IS BALANCED: {response.balanced ? '✅ YES' : '❌ NO'}</p>
        </div>
      )}
    </Card>
  );
}    
