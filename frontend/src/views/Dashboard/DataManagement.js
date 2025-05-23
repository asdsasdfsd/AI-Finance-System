import React, { useState, useEffect } from 'react';
import axios from 'axios';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { Table, Button, Space, Card } from 'antd';
import '../../assets/styles/DataManagement.css';

export default function FinancialDashboard() {
  const [incomeData, setIncomeData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    axios.get('http://localhost:8085/api/transactions', {
      withCredentials: true
    })
    .then(res => {
      console.log("✅ 成功获取数据：", res.data);
      const incomeOnly = res.data.filter(txn => txn.transactionType === 'INCOME');
      setIncomeData(incomeOnly);
    })
    .catch(err => {
      console.error("❌ 获取失败：", err);
    })
    .finally(() => setLoading(false));
  }, []);

  const handleExport = () => {
    const worksheetData = incomeData.map(txn => ({
      Date: txn.transactionDate,
      Description: txn.description,
      Amount: txn.amount,
      Currency: txn.currency,
      PaymentMethod: txn.paymentMethod,
      Reference: txn.referenceNumber
    }));

    const worksheet = XLSX.utils.json_to_sheet(worksheetData);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Income Report");

    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([excelBuffer], { type: "application/octet-stream" });
    saveAs(blob, "Income_Report.xlsx");
  };

  const columns = [
    { title: 'Date', dataIndex: 'transactionDate', key: 'date' },
    { title: 'Description', dataIndex: 'description', key: 'description' },
    { title: 'Amount', dataIndex: 'amount', key: 'amount', render: val => val.toLocaleString() },
    { title: 'Currency', dataIndex: 'currency', key: 'currency' },
    { title: 'Payment Method', dataIndex: 'paymentMethod', key: 'paymentMethod' },
    { title: 'Reference', dataIndex: 'referenceNumber', key: 'reference' },
  ];

  return (
    <Card
      title="Income Report"
      extra={
        <Button type="primary" onClick={handleExport}>
          Export to Excel
        </Button>
      }
      style={{ margin: 24 }}
    >
      <Table
        rowKey="transactionId"
        columns={columns}
        dataSource={incomeData}
        loading={loading}
        pagination={{ pageSize: 10 }}
        bordered
      />
    </Card>
  );
}
