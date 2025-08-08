// frontend/src/views/Dashboard/FinancialReports.js - FIXED VERSION
import React, { useState, useEffect } from 'react';
import {
  Card, Select, Button, Space, message, Typography, DatePicker,
  Row, Col, Spin, Alert, InputNumber, Table, Tabs
} from 'antd';
import {
  FundProjectionScreenOutlined, DownloadOutlined, ReloadOutlined,
  BarChartOutlined, DollarCircleOutlined, PieChartOutlined, FundOutlined  // FIXED: Add FundOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';
import BalanceSheetService from '../../services/balanceSheetService';
import AuthService from '../../services/authService';
import { ReportPreview } from '../../components/ReportPreviewComponents';  // FIXED: Import unified preview

const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;

/**
 * FIXED Financial Reports Component - Updated to use specific controllers
 * 
 * Removed dependencies on deleted FinancialReportController
 * Now uses specific report controllers for each report type
 */
const FinancialReports = () => {
  const [reportType, setReportType] = useState('BALANCE_SHEET');
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Balance Sheet specific states
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs());
  
  // Income Statement specific states
  const [startDate, setStartDate] = useState(dayjs().subtract(1, 'month'));
  const [endDate, setEndDate] = useState(dayjs());

  const reportConfigs = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      icon: <BarChartOutlined />,
      description: 'Assets, Liabilities, and Equity at a specific date',
      apiEndpoint: '/api/balance-sheet',
      useAsOfDate: true,
      canPreview: true
    },
    {
      value: 'INCOME_STATEMENT',
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and expenses over a period',
      apiEndpoint: '/api/income-statement',  // FIXED: Use specific controller
      useAsOfDate: false,
      canPreview: true
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Income and expense analysis over a period',
      apiEndpoint: '/api/income-expense',  // FIXED: Use specific controller
      useAsOfDate: true,
      canPreview: true
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Report',
      icon: <FundOutlined />,  // FIXED: Import FundOutlined
      description: 'Transactions grouped by various criteria',
      apiEndpoint: '/api/financial-grouping',  // FIXED: Add missing config
      useAsOfDate: false,
      canPreview: true
    }
  ];

  const currentReportConfig = reportConfigs.find(config => config.value === reportType);

  useEffect(() => {
    // Auto-load report when component mounts or report type changes
    if (reportType === 'BALANCE_SHEET') {
      loadReportData();
    }
  }, [reportType, companyId, asOfDate]);

  const loadReportData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // FIXED: Check authentication first
      const authData = AuthService.getCurrentUser();
      if (!authData || !authData.token) {
        throw new Error('Authentication required. Please login first.');
      }

      let data = null;
      
      if (reportType === 'BALANCE_SHEET') {
        // Use existing balance sheet API
        const response = await fetch('/api/balance-sheet/json?' + 
          new URLSearchParams({
            companyId: companyId,
            asOfDate: asOfDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: { 
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to fetch balance sheet data');
        }
        data = await response.json();
        
      } else if (reportType === 'INCOME_EXPENSE') {
        // FIXED: Use income-expense controller instead of deleted financial-report
        const response = await fetch('/api/income-expense/json?' + 
          new URLSearchParams({
            companyId: companyId,
            asOfDate: asOfDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: { 
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to fetch income expense data');
        }
        data = await response.json();
        
              } else if (reportType === 'INCOME_STATEMENT') {
        // FIXED: Use income-statement controller with proper date range
        const response = await fetch('/api/income-statement/json?' + 
          new URLSearchParams({
            companyId: companyId,
            startDate: startDate.format('YYYY-MM-DD'),
            endDate: endDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: { 
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to fetch income statement data');
        }
        data = await response.json();
        
      } else if (reportType === 'FINANCIAL_GROUPING') {
        // FIXED: Add financial grouping support
        const response = await fetch('/api/financial-grouping/json?' + 
          new URLSearchParams({
            companyId: companyId,
            startDate: startDate.format('YYYY-MM-DD'),
            endDate: endDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: { 
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to fetch financial grouping data');
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
      
      console.log('Loaded report data:', data);
      setReportData(data);
    } catch (error) {
      console.error('Error fetching report data:', error);
      setError(error.message);
      message.error('Failed to load report data: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      // FIXED: Check authentication first
      const authData = AuthService.getCurrentUser();
      if (!authData || !authData.token) {
        throw new Error('Authentication required. Please login first.');
      }

      if (reportType === 'BALANCE_SHEET') {
        await BalanceSheetService.exportBalanceSheet(
          companyId,
          asOfDate.format('YYYY-MM-DD')
        );
        message.success('Balance sheet exported successfully');
        
      } else if (reportType === 'INCOME_EXPENSE') {
        // FIXED: Use income-expense controller export endpoint
        const response = await fetch('/api/income-expense/export?' + 
          new URLSearchParams({
            companyId: companyId,
            asOfDate: asOfDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to export income expense report');
        }
        
        // Handle file download
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Income_Expense_Report_${companyId}_${asOfDate.format('YYYY-MM-DD')}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        message.success('Income expense report exported successfully');
        
      } else if (reportType === 'INCOME_STATEMENT') {
        // FIXED: Use income-statement controller export endpoint
        const response = await fetch('/api/income-statement/export?' + 
          new URLSearchParams({
            companyId: companyId,
            startDate: startDate.format('YYYY-MM-DD'),
            endDate: endDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to export income statement');
        }
        
        // Handle file download
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Income_Statement_${companyId}_${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        message.success('Income statement exported successfully');
        
      } else if (reportType === 'FINANCIAL_GROUPING') {
        // FIXED: Add financial grouping export support
        const response = await fetch('/api/financial-grouping/export?' + 
          new URLSearchParams({
            companyId: companyId,
            startDate: startDate.format('YYYY-MM-DD'),
            endDate: endDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${authData.token}`  // FIXED: Add auth header
            }
          });
        
        if (!response.ok) {
          throw new Error('Failed to export financial grouping');
        }
        
        // Handle file download
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Financial_Grouping_${companyId}_${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        message.success('Financial grouping exported successfully');
        
      } else {
        message.warning('Export not yet implemented for this report type');
      }
    } catch (error) {
      console.error('Export error:', error);
      message.error('Export failed: ' + error.message);
    }
  };

  // Rest of the component remains the same...
  return (
    <div>
      <Card>
        <Title level={3}>
          <FundProjectionScreenOutlined /> Financial Reports
        </Title>
        <Text type="secondary">
          Generate and export various financial reports for analysis
        </Text>
      </Card>

      <Card style={{ marginTop: 16 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col span={8}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Report Type</Text>
              <Select
                value={reportType}
                onChange={setReportType}
                style={{ width: '100%' }}
                size="large"
              >
                {reportConfigs.map(config => (
                  <Option key={config.value} value={config.value}>
                    <Space>
                      {config.icon}
                      {config.label}
                    </Space>
                  </Option>
                ))}
              </Select>
              <Text type="secondary" style={{ fontSize: '12px' }}>
                {currentReportConfig?.description}
              </Text>
            </Space>
          </Col>

          <Col span={8}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Company ID</Text>
              <InputNumber
                value={companyId}
                onChange={setCompanyId}
                min={1}
                style={{ width: '100%' }}
                size="large"
              />
            </Space>
          </Col>

          <Col span={8}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>
                {(currentReportConfig?.useAsOfDate === false || reportType === 'INCOME_STATEMENT' || reportType === 'FINANCIAL_GROUPING') ? 'Date Range' : 'As of Date'}
              </Text>
              {(currentReportConfig?.useAsOfDate === false || reportType === 'INCOME_STATEMENT' || reportType === 'FINANCIAL_GROUPING') ? (
                <Space direction="vertical" style={{ width: '100%' }}>
                  <DatePicker
                    value={startDate}
                    onChange={setStartDate}
                    style={{ width: '100%' }}
                    size="large"
                    placeholder="Start Date"
                  />
                  <DatePicker
                    value={endDate}
                    onChange={setEndDate}
                    style={{ width: '100%' }}
                    size="large"
                    placeholder="End Date"
                  />
                </Space>
              ) : (
                <DatePicker
                  value={asOfDate}
                  onChange={setAsOfDate}
                  style={{ width: '100%' }}
                  size="large"
                />
              )}
            </Space>
          </Col>
        </Row>

        <Row style={{ marginTop: 24 }}>
          <Col span={24}>
            <Space>
              <Button
                type="primary"
                icon={<ReloadOutlined />}
                onClick={loadReportData}
                loading={loading}
                size="large"
              >
                Generate Report
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={handleExport}
                disabled={!reportData || loading}
                size="large"
              >
                Export Excel
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Display results */}
      {error && (
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          style={{ marginTop: 16 }}
        />
      )}

      {loading && (
        <Card style={{ marginTop: 16 }}>
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <Spin size="large" />
            <div style={{ marginTop: 16 }}>
              <Text>Generating {currentReportConfig?.label}...</Text>
            </div>
          </div>
        </Card>
      )}

      {reportData && !loading && (
        <Card title={`${currentReportConfig?.label} Results`} style={{ marginTop: 16 }}>
          <Tabs defaultActiveKey="preview">
            <TabPane tab="Preview" key="preview">
              <ReportPreview reportType={reportType} data={reportData} />
            </TabPane>
            <TabPane tab="Raw Data" key="raw">
              <pre style={{ 
                background: '#f5f5f5', 
                padding: '16px', 
                borderRadius: '6px',
                overflow: 'auto',
                maxHeight: '400px'
              }}>
                {JSON.stringify(reportData, null, 2)}
              </pre>
            </TabPane>
          </Tabs>
        </Card>
      )}
    </div>
  );
};

export default FinancialReports;