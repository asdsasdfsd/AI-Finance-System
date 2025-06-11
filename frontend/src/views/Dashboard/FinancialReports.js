// frontend/src/views/Dashboard/FinancialReports.js
import React, { useState, useEffect } from 'react';
import { 
  Card, Select, DatePicker, Button, Row, Col, Space, message, 
  Spin, Typography, Tabs, InputNumber 
} from 'antd';
import {
  FileTextOutlined, DownloadOutlined, ReloadOutlined,
  BarChartOutlined, DollarCircleOutlined, PieChartOutlined,
  FundProjectionScreenOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';
import BalanceSheetService from '../../services/balanceSheetService';

const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;

/**
 * Unified Financial Reports Component
 * 
 * Combines all financial reports (Balance Sheet, Income Statement, 
 * Income vs Expense, Financial Grouping) into a single interface
 */
const FinancialReports = () => {
  // State management
  const [reportType, setReportType] = useState('BALANCE_SHEET');
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [startDate, setStartDate] = useState(dayjs().subtract(1, 'month'));
  const [endDate, setEndDate] = useState(dayjs());
  const [loading, setLoading] = useState(false);
  const [reportData, setReportData] = useState(null);
  const [activeTab, setActiveTab] = useState('view');

  // Report configuration
  const reportTypes = [
    { 
      value: 'BALANCE_SHEET', 
      label: 'Balance Sheet',
      icon: <BarChartOutlined />,
      description: 'Assets, Liabilities, and Equity at a specific point in time',
      useAsOfDate: true
    },
    { 
      value: 'INCOME_STATEMENT', 
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and Expenses over a period',
      useAsOfDate: false
    },
    { 
      value: 'INCOME_EXPENSE', 
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Detailed comparison of income and expenses',
      useAsOfDate: true
    },
    { 
      value: 'FINANCIAL_GROUPING', 
      label: 'Financial Grouping Report',
      icon: <FundProjectionScreenOutlined />,
      description: 'Transactions grouped by various criteria',
      useAsOfDate: false
    }
  ];

  const currentReportConfig = reportTypes.find(rt => rt.value === reportType);

  useEffect(() => {
    // Only fetch data automatically if we have basic parameters
    // User needs to click "Generate" for the first load
  }, []);

  const fetchReportData = async () => {
    setLoading(true);
    try {
      let data;
      
      if (reportType === 'BALANCE_SHEET') {
        // Use legacy balance sheet service
        data = await BalanceSheetService.getBalanceSheetJson(
          companyId, 
          asOfDate.format('YYYY-MM-DD')
        );
      } else if (reportType === 'INCOME_EXPENSE') {
        // Use legacy income expense service
        const response = await fetch('/api/financial-report/json?' + new URLSearchParams({
          companyId: companyId,
          asOfDate: asOfDate.format('YYYY-MM-DD')
        }), {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' }
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch income expense data');
        }
        data = await response.json();
      } else {
        // Use DDD report service for future reports
        const reportRequest = {
          reportType,
          reportName: `${currentReportConfig.label} - ${dayjs().format('YYYY-MM-DD HH:mm')}`,
          startDate: startDate.format('YYYY-MM-DD'),
          endDate: endDate.format('YYYY-MM-DD'),
          aiAnalysisEnabled: false
        };
        
        // This would trigger DDD report generation
        const result = await ReportService.generateReport(reportRequest);
        message.info('Report generation started. Check Report Management for progress.');
        return;
      }
      
      setReportData(data);
    } catch (error) {
      console.error('Error fetching report data:', error);
      message.error('Failed to load report data');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      if (reportType === 'BALANCE_SHEET') {
        await BalanceSheetService.exportBalanceSheet(
          companyId,
          asOfDate.format('YYYY-MM-DD')
        );
        message.success('Balance sheet exported successfully');
      } else if (reportType === 'INCOME_EXPENSE') {
        const response = await fetch('/api/financial-report/export?' + new URLSearchParams({
          companyId: companyId,
          asOfDate: asOfDate.format('YYYY-MM-DD')
        }), {
          method: 'GET'
        });
        
        if (!response.ok) {
          throw new Error('Failed to export income expense report');
        }
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${reportType}_${asOfDate.format('YYYY-MM-DD')}.xlsx`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
        
        message.success('Income vs Expense report exported successfully');
      } else {
        message.info('Please use Report Generation for DDD-based reports');
      }
    } catch (error) {
      console.error('Export error:', error);
      message.error('Failed to export report');
    }
  };

  const renderDatePicker = () => {
    if (currentReportConfig.useAsOfDate) {
      return (
        <Col span={6}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>As of Date:</Text>
            <DatePicker
              value={asOfDate}
              onChange={setAsOfDate}
              format="YYYY-MM-DD"
              style={{ width: '100%' }}
            />
          </Space>
        </Col>
      );
    } else {
      return (
        <>
          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Start Date:</Text>
              <DatePicker
                value={startDate}
                onChange={setStartDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>End Date:</Text>
              <DatePicker
                value={endDate}
                onChange={setEndDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
        </>
      );
    }
  };

  const renderReportContent = () => {
    if (!reportData) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Text>Select parameters and click "Generate Report" to view data</Text>
        </div>
      );
    }

    if (reportType === 'BALANCE_SHEET') {
      return renderBalanceSheet();
    } else if (reportType === 'INCOME_EXPENSE') {
      return renderIncomeExpenseReport();
    }

    return <div>Report content will be displayed here</div>;
  };

  const renderBalanceSheet = () => {
    if (!reportData) return null;

    const renderSection = (sectionName, sectionData, total) => (
      <div style={{ marginBottom: 24 }}>
        <Title level={4}>{sectionName}</Title>
        {Object.entries(sectionData).map(([category, accounts]) => (
          <div key={category} style={{ marginBottom: 16 }}>
            <Text strong style={{ fontSize: '16px' }}>{category}</Text>
            <table style={{ width: '100%', marginTop: 8, border: '1px solid #d9d9d9' }}>
              <thead>
                <tr style={{ backgroundColor: '#fafafa' }}>
                  <th style={{ padding: '8px', textAlign: 'left', border: '1px solid #d9d9d9' }}>Account</th>
                  <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Current Month</th>
                  <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Previous Month</th>
                  <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Last Year End</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account, index) => (
                  <tr key={index}>
                    <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>{account.accountName}</td>
                    <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                      {account.currentMonth.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                    </td>
                    <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                      {account.previousMonth.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                    </td>
                    <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                      {account.lastYearEnd.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
        <div style={{ textAlign: 'right', marginTop: 16 }}>
          <Text strong style={{ fontSize: '18px' }}>
            TOTAL {sectionName.toUpperCase()}: {total.toLocaleString(undefined, { minimumFractionDigits: 2 })}
          </Text>
        </div>
      </div>
    );

    return (
      <div>
        <Title level={3}>Balance Sheet as at {reportData.asOfDate}</Title>
        {renderSection('Assets', reportData.assets, reportData.totalAssets)}
        {renderSection('Liabilities', reportData.liabilities, reportData.totalLiabilities)}
        {renderSection('Equity', reportData.equity, reportData.totalEquity)}
        <div style={{ textAlign: 'center', marginTop: 24, padding: '16px', backgroundColor: '#f0f2f5' }}>
          <Text strong style={{ fontSize: '20px' }}>
            IS BALANCED: {reportData.isBalanced ? '✅ YES' : '❌ NO'}
          </Text>
        </div>
      </div>
    );
  };

  const renderIncomeExpenseReport = () => {
    if (!reportData) return null;

    return (
      <div>
        <Title level={3}>Income vs Expense Report</Title>
        <table style={{ width: '100%', border: '1px solid #d9d9d9' }}>
          <thead>
            <tr style={{ backgroundColor: '#fafafa' }}>
              <th style={{ padding: '8px', textAlign: 'left', border: '1px solid #d9d9d9' }}>Type</th>
              <th style={{ padding: '8px', textAlign: 'left', border: '1px solid #d9d9d9' }}>Category</th>
              <th style={{ padding: '8px', textAlign: 'left', border: '1px solid #d9d9d9' }}>Item</th>
              <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Current Month</th>
              <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Previous Month</th>
              <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>YTD</th>
            </tr>
          </thead>
          <tbody>
            {reportData.map((row, index) => (
              <tr key={index}>
                <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>{row.type}</td>
                <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>{row.category}</td>
                <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>{row.description}</td>
                <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                  {row.currentMonth?.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                </td>
                <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                  {row.previousMonth?.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                </td>
                <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                  {row.yearToDate?.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <Card
      title={
        <Space>
          <FileTextOutlined />
          Financial Reports
        </Space>
      }
      style={{ margin: 24 }}
    >
      {/* Report Configuration */}
      <Card size="small" title="Report Configuration" style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Report Type:</Text>
              <Select
                value={reportType}
                onChange={setReportType}
                style={{ width: '100%' }}
              >
                {reportTypes.map(type => (
                  <Option key={type.value} value={type.value}>
                    <Space>
                      {type.icon}
                      {type.label}
                    </Space>
                  </Option>
                ))}
              </Select>
            </Space>
          </Col>
          
          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Company ID:</Text>
              <InputNumber
                min={1}
                value={companyId}
                onChange={setCompanyId}
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
          
          {renderDatePicker()}
          
          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Actions:</Text>
              <Space>
                <Button 
                  type="primary" 
                  icon={<ReloadOutlined />}
                  onClick={fetchReportData}
                  loading={loading}
                >
                  Generate
                </Button>
                <Button 
                  icon={<DownloadOutlined />}
                  onClick={handleExport}
                  disabled={!reportData}
                >
                  Export
                </Button>
              </Space>
            </Space>
          </Col>
        </Row>
        
        {currentReportConfig && (
          <div style={{ marginTop: 16, padding: '12px', backgroundColor: '#f6f8fa', borderRadius: '6px' }}>
            <Text type="secondary">{currentReportConfig.description}</Text>
          </div>
        )}
      </Card>

      {/* Report Content */}
      <Card title={currentReportConfig?.label || 'Report'} loading={loading}>
        {renderReportContent()}
      </Card>
    </Card>
  );
};

export default FinancialReports;