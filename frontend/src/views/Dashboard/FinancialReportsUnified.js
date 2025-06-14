// frontend/src/views/Dashboard/FinancialReportsUnified.js
import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Select, DatePicker, Space, Spin, Typography, message } from 'antd';
import { FileTextOutlined, DownloadOutlined, EyeOutlined, DollarCircleOutlined, 
         PieChartOutlined, FundOutlined, BarChartOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { API_CONFIG, createApiClient } from '../../config/apiConfig';

const { Option } = Select;
const { Text, Title } = Typography;

export default function FinancialReportsUnified() {
  // State management
  const [companyId] = useState(1); // Hardcoded for development
  const [previewReportType, setPreviewReportType] = useState('BALANCE_SHEET');
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [startDate, setStartDate] = useState(dayjs().startOf('month'));
  const [endDate, setEndDate] = useState(dayjs());
  const [reportData, setReportData] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState('unknown');
  const [testingConnection, setTestingConnection] = useState(false);

  const apiClient = createApiClient();

  // Report type configurations
  const reportTypes = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      icon: <FileTextOutlined />,
      description: 'Assets, Liabilities, and Equity at a point in time',
      useAsOfDate: true,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.BALANCE_SHEET}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.BALANCE_SHEET}/export`
    },
    {
      value: 'INCOME_STATEMENT',
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and Expenses over a period',
      useAsOfDate: false,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.INCOME_STATEMENT}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.INCOME_STATEMENT}/export`
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Detailed Income and Expense Analysis',
      useAsOfDate: true,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.FINANCIAL_REPORT}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.FINANCIAL_REPORT}/export`
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Report',
      icon: <FundOutlined />,
      description: 'Transactions grouped by various criteria',
      useAsOfDate: false,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.FINANCIAL_GROUPING}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.FINANCIAL_GROUPING}/export`
    }
  ];

  // Test backend connection on component mount
  useEffect(() => {
    testBackendConnection();
  }, []);

  const testBackendConnection = async () => {
    setTestingConnection(true);
    try {
      // Test with a simple endpoint
      await apiClient.get('/api/health', { timeout: 5000 });
      setConnectionStatus('connected');
      message.success('Backend connection successful');
    } catch (error) {
      console.error('Backend connection failed:', error);
      setConnectionStatus('disconnected');
      message.error('Backend connection failed. Please check if the server is running on port 8085.');
    } finally {
      setTestingConnection(false);
    }
  };

  // Preview Functions
  const handlePreview = async () => {
    if (connectionStatus === 'disconnected') {
      message.error('Backend is not connected. Please check the server.');
      return;
    }

    setPreviewLoading(true);
    try {
      const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
      
      if (!currentConfig?.previewApi) {
        throw new Error('Preview not available for this report type');
      }

      // Build parameters based on report type
      const params = {
        companyId: companyId,
      };

      if (currentConfig.useAsOfDate) {
        params.asOfDate = asOfDate.format('YYYY-MM-DD');
      } else {
        params.startDate = startDate.format('YYYY-MM-DD');
        params.endDate = endDate.format('YYYY-MM-DD');
      }

      console.log('Requesting preview with params:', params);
      console.log('API endpoint:', currentConfig.previewApi);

      const response = await apiClient.get(currentConfig.previewApi, { params });
      
      console.log('Preview response:', response.data);
      setReportData(response.data);
      message.success(`${currentConfig.label} preview loaded successfully`);
    } catch (error) {
      console.error('Error fetching report data:', error);
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          'Unknown error occurred';
      message.error(`Failed to load report preview: ${errorMessage}`);
      setReportData(null);
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleExport = async () => {
    if (connectionStatus === 'disconnected') {
      message.error('Backend is not connected. Please check the server.');
      return;
    }

    try {
      const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
      
      if (!currentConfig?.exportApi) {
        throw new Error('Export not available for this report type');
      }

      // Build parameters for export
      const params = {
        companyId: companyId,
      };

      if (currentConfig.useAsOfDate) {
        params.asOfDate = asOfDate.format('YYYY-MM-DD');
      } else {
        params.startDate = startDate.format('YYYY-MM-DD');
        params.endDate = endDate.format('YYYY-MM-DD');
      }

      const response = await apiClient.get(currentConfig.exportApi, { 
        params,
        responseType: 'blob'
      });
      
      const blob = new Blob([response.data], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
      // Generate filename
      const dateStr = currentConfig.useAsOfDate 
        ? asOfDate.format('YYYY-MM-DD')
        : `${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}`;
      
      link.setAttribute('download', `${currentConfig.label.replace(/ /g, '_')}_${dateStr}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      message.success('Report exported successfully');
    } catch (error) {
      console.error('Error exporting report:', error);
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          'Export failed';
      message.error(`Failed to export report: ${errorMessage}`);
    }
  };

  // Render Parameter Selection
  const renderParameterSelection = () => {
    const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
    
    return (
      <Row gutter={[16, 16]} align="middle">
        <Col xs={24} sm={8}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Report Type</Text>
            <Select
              value={previewReportType}
              onChange={setPreviewReportType}
              style={{ width: '100%' }}
              size="large"
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
            <Text type="secondary" style={{ fontSize: '12px' }}>
              {currentConfig?.description}
            </Text>
          </Space>
        </Col>
        
        <Col xs={24} sm={currentConfig?.useAsOfDate ? 8 : 16}>
          {currentConfig?.useAsOfDate ? (
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>As of Date</Text>
              <DatePicker
                value={asOfDate}
                onChange={setAsOfDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
                size="large"
              />
            </Space>
          ) : (
            <Row gutter={[8, 8]}>
              <Col span={12}>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Text strong>Start Date</Text>
                  <DatePicker
                    value={startDate}
                    onChange={setStartDate}
                    format="YYYY-MM-DD"
                    style={{ width: '100%' }}
                    size="large"
                  />
                </Space>
              </Col>
              <Col span={12}>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Text strong>End Date</Text>
                  <DatePicker
                    value={endDate}
                    onChange={setEndDate}
                    format="YYYY-MM-DD"
                    style={{ width: '100%' }}
                    size="large"
                  />
                </Space>
              </Col>
            </Row>
          )}
        </Col>
        
        <Col xs={24} sm={8}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Actions</Text>
            <Space size="middle" style={{ width: '100%' }}>
              <Button
                icon={<EyeOutlined />}
                onClick={handlePreview}
                type="primary"
                loading={previewLoading}
                size="large"
                disabled={connectionStatus === 'disconnected'}
              >
                Preview
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={handleExport}
                disabled={!reportData || connectionStatus === 'disconnected'}
                size="large"
              >
                Export
              </Button>
            </Space>
            <div style={{ fontSize: '12px' }}>
              Status: 
              <span style={{ 
                color: connectionStatus === 'connected' ? '#52c41a' : '#ff4d4f',
                marginLeft: '4px'
              }}>
                {testingConnection ? 'Testing...' : connectionStatus}
              </span>
              {connectionStatus === 'disconnected' && (
                <Button 
                  type="link" 
                  size="small" 
                  onClick={testBackendConnection}
                  style={{ padding: '0 4px' }}
                >
                  Retry
                </Button>
              )}
            </div>
          </Space>
        </Col>
      </Row>
    );
  };

  const renderReportContent = () => {
    if (previewLoading) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <div style={{ marginTop: 16 }}>
            <Text>Loading report preview...</Text>
          </div>
        </div>
      );
    }

    if (!reportData) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Text>Select parameters and click "Preview" to view report data</Text>
        </div>
      );
    }

    // Render different report types
    switch (previewReportType) {
      case 'BALANCE_SHEET':
        return renderBalanceSheet();
      case 'INCOME_STATEMENT':
        return renderIncomeStatement();
      case 'INCOME_EXPENSE':
        return renderIncomeExpenseReport();
      case 'FINANCIAL_GROUPING':
        return renderFinancialGroupingReport();
      default:
        return <div>Report content will be displayed here</div>;
    }
  };

  const renderBalanceSheet = () => {
    // Implementation for balance sheet rendering
    return (
      <div>
        <Title level={4}>Balance Sheet Preview</Title>
        <pre style={{ background: '#f5f5f5', padding: '16px', overflow: 'auto' }}>
          {JSON.stringify(reportData, null, 2)}
        </pre>
      </div>
    );
  };

  const renderIncomeStatement = () => {
    // Implementation for income statement rendering
    return (
      <div>
        <Title level={4}>Income Statement Preview</Title>
        <pre style={{ background: '#f5f5f5', padding: '16px', overflow: 'auto' }}>
          {JSON.stringify(reportData, null, 2)}
        </pre>
      </div>
    );
  };

  const renderIncomeExpenseReport = () => {
    // Implementation for income expense report rendering
    return (
      <div>
        <Title level={4}>Income vs Expense Report Preview</Title>
        <pre style={{ background: '#f5f5f5', padding: '16px', overflow: 'auto' }}>
          {JSON.stringify(reportData, null, 2)}
        </pre>
      </div>
    );
  };

  const renderFinancialGroupingReport = () => {
    // Implementation for financial grouping report rendering
    return (
      <div>
        <Title level={4}>Financial Grouping Report Preview</Title>
        <pre style={{ background: '#f5f5f5', padding: '16px', overflow: 'auto' }}>
          {JSON.stringify(reportData, null, 2)}
        </pre>
      </div>
    );
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Title level={2}>Financial Reports</Title>
        
        {/* Parameter Selection */}
        <Card style={{ marginBottom: '24px' }}>
          {renderParameterSelection()}
        </Card>
        
        {/* Report Content */}
        <Card>
          {renderReportContent()}
        </Card>
      </Card>
    </div>
  );
}