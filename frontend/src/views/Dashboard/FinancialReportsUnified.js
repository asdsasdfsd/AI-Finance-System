// frontend/src/views/Dashboard/FinancialReportsUnified.js
import React, { useState, useEffect } from 'react';
import {
  Card, Select, Button, Space, message, Typography, DatePicker,
  Row, Col, Spin, Alert, Table, Tabs
} from 'antd';
import {
  FundProjectionScreenOutlined, DownloadOutlined, ReloadOutlined,
  BarChartOutlined, DollarCircleOutlined, PieChartOutlined, FundOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import AuthService from '../../services/authService';
import { ReportPreview } from '../../components/ReportPreviewComponents';

const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;

// API Configuration - Updated to use correct specific controllers
const API_CONFIG = {
  BASE_URL: 'http://localhost:8085',
  ENDPOINTS: {
    BALANCE_SHEET: '/api/balance-sheet',
    INCOME_STATEMENT: '/api/income-statement',
    INCOME_EXPENSE: '/api/income-expense',
    FINANCIAL_GROUPING: '/api/financial-grouping'
  }
};

// Helper function to get auth headers for fetch requests
const getAuthHeaders = () => {
  const authData = AuthService.getCurrentUser();
  const headers = {
    'Content-Type': 'application/json'
  };
  
  if (authData && authData.token) {
    headers['Authorization'] = `Bearer ${authData.token}`;
    console.log('✅ [Auth] Adding Authorization header:', authData.token.substring(0, 20) + '...');
  } else {
    console.error('❌ [Auth] No token found in auth data:', authData);
  }
  
  return headers;
};

// Helper function to make authenticated fetch requests
const makeAuthenticatedRequest = async (url, options = {}) => {
  const headers = getAuthHeaders();
  
  const config = {
    method: 'GET',
    headers,
    ...options
  };
  
  console.log(`🔄 [API] Making request to: ${url}`);
  console.log('🔧 [API] Request config:', {
    method: config.method,
    headers: Object.keys(config.headers),
    hasAuth: !!config.headers.Authorization
  });
  
  const response = await fetch(url, config);
  
  console.log(`📡 [API] Response: ${response.status} ${response.statusText}`);
  
  if (!response.ok) {
    const errorText = await response.text();
    console.error('❌ [API] Error response:', errorText);
    throw new Error(`HTTP ${response.status}: ${errorText}`);
  }
  
  return response;
};

/**
 * Enhanced Financial Reports Component with unified styling
 */
const FinancialReportsUnified = () => {
  const [reportType, setReportType] = useState('BALANCE_SHEET');
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState(null);
  const [testingConnection, setTestingConnection] = useState(false);
  
  // Common states
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [startDate, setStartDate] = useState(dayjs().subtract(3, 'month'));
  const [endDate, setEndDate] = useState(dayjs());

  // Complete report configurations
  const reportConfigs = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      icon: <BarChartOutlined />,
      description: 'Assets, Liabilities, and Equity at a specific date',
      useAsOfDate: true,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.BALANCE_SHEET}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.BALANCE_SHEET}/export`,
      suggestedDate: '2024-03-31'
    },
    {
      value: 'INCOME_STATEMENT',
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and Expenses over a period',
      useAsOfDate: false,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.INCOME_STATEMENT}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.INCOME_STATEMENT}/export`,
      suggestedStartDate: '2024-01-01',
      suggestedEndDate: '2024-03-31'
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Detailed Income and Expense Analysis',
      useAsOfDate: true,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.INCOME_EXPENSE}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.INCOME_EXPENSE}/export`,
      suggestedDate: '2024-03-31'
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Report',
      icon: <FundOutlined />,
      description: 'Transactions grouped by various criteria',
      useAsOfDate: false,
      canPreview: true,
      previewApi: `${API_CONFIG.ENDPOINTS.FINANCIAL_GROUPING}/json`,
      exportApi: `${API_CONFIG.ENDPOINTS.FINANCIAL_GROUPING}/export`,
      suggestedStartDate: '2024-01-01',
      suggestedEndDate: '2024-03-31'
    }
  ];

  // Test backend connection with auth
  useEffect(() => {
    testBackendConnection();
  }, []);

  const testBackendConnection = async () => {
    setTestingConnection(true);
    try {
      // Test basic connection first
      const response = await makeAuthenticatedRequest(`${API_CONFIG.BASE_URL}/api/health/ping`);
      
      if (response.ok) {
        console.log('✅ [Connection] Backend connection successful');
        
        // Test auth by trying to access a protected endpoint
        try {
          await makeAuthenticatedRequest(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.BALANCE_SHEET}/json?companyId=1&asOfDate=2024-03-31`);
          console.log('✅ [Auth] Authentication test successful');
        } catch (authError) {
          console.warn('⚠️ [Auth] Authentication test failed:', authError.message);
          if (authError.message.includes('401')) {
            message.warning('Authentication issue detected. You may need to login again.');
          }
        }
      } else {
        console.warn('⚠️ [Connection] Backend responded with non-OK status:', response.status);
      }
    } catch (error) {
      console.error('❌ [Connection] Backend connection failed:', error);
      message.warning('Backend connection test failed. Some features may not work properly.');
    } finally {
      setTestingConnection(false);
    }
  };

  const currentReportConfig = reportConfigs.find(config => config.value === reportType);

  const loadReportData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Check authentication before making request
      const authData = AuthService.getCurrentUser();
      if (!authData || !authData.token) {
        throw new Error('Authentication required. Please login first.');
      }
      
      const config = currentReportConfig;
      let url = `${API_CONFIG.BASE_URL}${config.previewApi}`;
      let params = new URLSearchParams();
      
      // Add common parameters
      params.append('companyId', companyId.toString());
      
      // Add date parameters based on report type
      if (config.useAsOfDate) {
        params.append('asOfDate', asOfDate.format('YYYY-MM-DD'));
      } else {
        params.append('startDate', startDate.format('YYYY-MM-DD'));
        params.append('endDate', endDate.format('YYYY-MM-DD'));
      }
      
      const fullUrl = `${url}?${params.toString()}`;
      console.log(`🔄 [Report] Loading ${reportType} data from: ${fullUrl}`);
      
      // Use authenticated request
      const response = await makeAuthenticatedRequest(fullUrl);
      const data = await response.json();
      
      console.log('✅ [Report] Data loaded successfully:', data);
      setReportData(data);
      
    } catch (error) {
      console.error('❌ [Report] Error loading data:', error);
      setError(error.message);
      message.error(`Failed to load ${currentReportConfig.label}: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Enhanced export functionality with proper authentication
  const handleExport = async () => {
    setExporting(true);
    
    try {
      // Check authentication before making request
      const authData = AuthService.getCurrentUser();
      if (!authData || !authData.token) {
        throw new Error('Authentication required. Please login first.');
      }
      
      const config = currentReportConfig;
      let url = `${API_CONFIG.BASE_URL}${config.exportApi}`;
      let params = new URLSearchParams();
      
      // Add common parameters
      params.append('companyId', companyId.toString());
      
      // Add date parameters based on report type
      if (config.useAsOfDate) {
        params.append('asOfDate', asOfDate.format('YYYY-MM-DD'));
      } else {
        params.append('startDate', startDate.format('YYYY-MM-DD'));
        params.append('endDate', endDate.format('YYYY-MM-DD'));
      }
      
      const fullUrl = `${url}?${params.toString()}`;
      console.log(`🔽 [Export] Exporting ${reportType} from: ${fullUrl}`);
      
      // Use authenticated request for export
      const response = await makeAuthenticatedRequest(fullUrl);
      
      // Handle file download
      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      
      // Generate filename
      const dateStr = config.useAsOfDate 
        ? asOfDate.format('YYYY-MM-DD')
        : `${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}`;
      a.download = `${config.label.replace(/\s+/g, '_')}_${companyId}_${dateStr}.xlsx`;
      
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      document.body.removeChild(a);
      
      message.success(`${config.label} exported successfully`);
      
    } catch (error) {
      console.error('❌ [Export] Export failed:', error);
      message.error(`Export failed: ${error.message}`);
    } finally {
      setExporting(false);
    }
  };

  // Helper functions for legacy summary display
  const renderReportSummary = (data, type) => {
    if (!data) return null;

    switch (type) {
      case 'BALANCE_SHEET':
        return renderBalanceSheetSummary(data);
      case 'INCOME_STATEMENT':
        return renderIncomeStatementSummary(data);
      case 'INCOME_EXPENSE':
        return renderIncomeExpenseSummary(data);
      case 'FINANCIAL_GROUPING':
        return renderFinancialGroupingSummary(data);
      default:
        return <Text>Summary view not available for this report type</Text>;
    }
  };

  const renderBalanceSheetSummary = (data) => {
    const summaryData = [
      { key: 'totalAssets', label: 'Total Assets', value: data.totalAssets || 0 },
      { key: 'totalLiabilities', label: 'Total Liabilities', value: data.totalLiabilities || 0 },
      { key: 'totalEquity', label: 'Total Equity', value: data.totalEquity || 0 },
      { key: 'balanced', label: 'Balanced', value: data.isBalanced ? 'Yes' : 'No' }
    ];

    return (
      <Table 
        dataSource={summaryData}
        columns={[
          { title: 'Item', dataIndex: 'label', key: 'label' },
          { title: 'Value', dataIndex: 'value', key: 'value' }
        ]}
        pagination={false}
        size="small"
      />
    );
  };

  const renderIncomeStatementSummary = (data) => {
    const summaryData = [
      { key: 'totalRevenue', label: 'Total Revenue', value: data.totalRevenue || 0 },
      { key: 'totalExpenses', label: 'Total Expenses', value: data.totalExpenses || 0 },
      { key: 'netIncome', label: 'Net Income', value: data.netIncome || 0 }
    ];

    return (
      <Table 
        dataSource={summaryData}
        columns={[
          { title: 'Item', dataIndex: 'label', key: 'label' },
          { title: 'Value', dataIndex: 'value', key: 'value' }
        ]}
        pagination={false}
        size="small"
      />
    );
  };

  const renderIncomeExpenseSummary = (data) => {
    const summaryData = [
      { key: 'totalIncomeYTD', label: 'Total Income (YTD)', value: data.totalIncomeYTD || 0 },
      { key: 'totalExpenseYTD', label: 'Total Expense (YTD)', value: data.totalExpenseYTD || 0 },
      { key: 'netIncomeYTD', label: 'Net Income (YTD)', value: data.netIncomeYTD || 0 },
      { key: 'totalIncomeMonth', label: 'Total Income (Month)', value: data.totalIncomeMonth || 0 },
      { key: 'totalExpenseMonth', label: 'Total Expense (Month)', value: data.totalExpenseMonth || 0 },
      { key: 'netIncomeMonth', label: 'Net Income (Month)', value: data.netIncomeMonth || 0 }
    ];

    return (
      <Table 
        dataSource={summaryData}
        columns={[
          { title: 'Item', dataIndex: 'label', key: 'label' },
          { title: 'Value', dataIndex: 'value', key: 'value' }
        ]}
        pagination={false}
        size="small"
      />
    );
  };

  const renderFinancialGroupingSummary = (data) => {
    if (!data.groupings && !data.categoryGrouping) {
      return <Text>No grouping data available</Text>;
    }

    const groupingData = data.groupings || data.categoryGrouping || {};
    const summaryData = Object.entries(groupingData).map(([key, value], index) => ({
      key: index,
      label: key,
      value: typeof value === 'object' ? JSON.stringify(value) : value
    }));

    return (
      <Table 
        dataSource={summaryData}
        columns={[
          { title: 'Group', dataIndex: 'label', key: 'label' },
          { title: 'Value', dataIndex: 'value', key: 'value' }
        ]}
        pagination={false}
        size="small"
      />
    );
  };

  return (
    <div>
      <Card>
        <Title level={3}>
          <FundProjectionScreenOutlined /> Financial Reports (Unified)
        </Title>
        <Text type="secondary">
          Generate and export various financial reports using the DDD architecture
        </Text>
        {testingConnection && (
          <Alert 
            message="Testing backend connection..." 
            type="info" 
            showIcon 
            style={{ marginTop: 16 }}
          />
        )}
      </Card>

      <Card style={{ marginTop: 16 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col span={6}>
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

          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Company ID</Text>
              <input
                type="number"
                value={companyId}
                onChange={(e) => setCompanyId(parseInt(e.target.value) || 1)}
                min={1}
                style={{ width: '100%', padding: '8px', fontSize: '16px' }}
              />
            </Space>
          </Col>

          <Col span={12}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>
                {currentReportConfig?.useAsOfDate ? 'As of Date' : 'Date Range'}
              </Text>
              {currentReportConfig?.useAsOfDate ? (
                <DatePicker
                  value={asOfDate}
                  onChange={setAsOfDate}
                  style={{ width: '100%' }}
                  size="large"
                />
              ) : (
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
                loading={exporting}
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
            <TabPane tab="Enhanced Preview" key="preview">
              <ReportPreview reportType={reportType} data={reportData} />
            </TabPane>
            <TabPane tab="Summary" key="summary">
              {renderReportSummary(reportData, reportType)}
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

export default FinancialReportsUnified;