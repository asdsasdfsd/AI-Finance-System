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

const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;

// API Configuration
const API_CONFIG = {
  BASE_URL: 'http://localhost:8085',
  ENDPOINTS: {
    BALANCE_SHEET: '/api/balance-sheet',
    INCOME_STATEMENT: '/api/income-statement',
    INCOME_EXPENSE: '/api/income-expense',
    FINANCIAL_GROUPING: '/api/financial-grouping'
  }
};

// FIXED: Helper function to get auth headers for fetch requests
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

// FIXED: Helper function to make authenticated fetch requests
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
 * Enhanced Financial Reports Component with FIXED authentication
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

  // Report configurations
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

  // FIXED: Test backend connection with auth
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
      // FIXED: Check authentication before making request
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
      
      // FIXED: Use authenticated request
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

  // FIXED: Enhanced export functionality with proper authentication
  const handleExport = async () => {
    setExporting(true);
    
    try {
      // FIXED: Check authentication before making request
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
      console.log(`📥 [Export] Exporting ${reportType} from: ${fullUrl}`);
      
      // FIXED: Use authenticated request with proper headers
      const response = await makeAuthenticatedRequest(fullUrl, {
        headers: {
          ...getAuthHeaders(),
          'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        }
      });
      
      // Check if response is actually Excel content
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('spreadsheetml')) {
        throw new Error('Server did not return Excel file. Check server logs.');
      }
      
      const blob = await response.blob();
      
      if (blob.size === 0) {
        throw new Error('Received empty file from server');
      }
      
      // Generate filename
      const dateStr = config.useAsOfDate 
        ? asOfDate.format('YYYY-MM-DD')
        : `${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}`;
      const filename = `${config.label.replace(/\s+/g, '_')}_${dateStr}.xlsx`;
      
      // Download file
      const url_obj = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url_obj;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url_obj);
      
      console.log(`✅ [Export] ${reportType} exported successfully: ${filename} (${blob.size} bytes)`);
      message.success(`${config.label} exported successfully!`);
      
    } catch (error) {
      console.error('❌ [Export] Export failed:', error);
      message.error(`Export failed: ${error.message}`);
      
      // FIXED: Handle auth errors specifically
      if (error.message.includes('401') || error.message.includes('Authorization')) {
        message.error('Authentication failed. Please login again.');
        // Optionally redirect to login
        // window.location.href = '/login';
      }
    } finally {
      setExporting(false);
    }
  };

  // Auto-load report when dependencies change
  useEffect(() => {
    if (reportType && companyId) {
      loadReportData();
    }
  }, [reportType, companyId, asOfDate, startDate, endDate]);

  const renderReportPreview = () => {
    if (loading) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <div style={{ marginTop: 16 }}>Loading report data...</div>
        </div>
      );
    }

    if (error) {
      return (
        <Alert 
          message="Error Loading Report" 
          description={error}
          type="error" 
          showIcon
          action={
            <Button size="small" danger onClick={loadReportData}>
              Retry
            </Button>
          }
        />
      );
    }

    if (!reportData) {
      return (
        <Alert 
          message="No Data" 
          description="No report data available. Click 'Load Report' to fetch data."
          type="info" 
          showIcon
        />
      );
    }

    // Render different previews based on report type
    switch (reportType) {
      case 'BALANCE_SHEET':
        return renderBalanceSheetPreview();
      case 'INCOME_EXPENSE':
        return renderIncomeExpensePreview();
      case 'FINANCIAL_GROUPING':
        return renderFinancialGroupingPreview();
      case 'INCOME_STATEMENT':
        return renderIncomeStatementPreview();
      default:
        return <div>Preview not available for this report type</div>;
    }
  };

  const renderBalanceSheetPreview = () => {
    if (!reportData) return null;

    const columns = [
      { title: 'Account', dataIndex: 'accountName', key: 'accountName' },
      { title: 'Current Month', dataIndex: 'currentMonth', key: 'currentMonth', 
        render: (value) => `$${Number(value || 0).toLocaleString()}` },
      { title: 'Previous Month', dataIndex: 'previousMonth', key: 'previousMonth',
        render: (value) => `$${Number(value || 0).toLocaleString()}` },
      { title: 'Last Year End', dataIndex: 'lastYearEnd', key: 'lastYearEnd',
        render: (value) => `$${Number(value || 0).toLocaleString()}` }
    ];

    return (
      <Tabs defaultActiveKey="assets">
        <TabPane tab="Assets" key="assets">
          {reportData.assets && Object.entries(reportData.assets).map(([category, accounts]) => (
            <div key={category} style={{ marginBottom: 24 }}>
              <Title level={4}>{category}</Title>
              <Table 
                dataSource={accounts} 
                columns={columns} 
                pagination={false}
                size="small"
                rowKey="accountName"
              />
            </div>
          ))}
        </TabPane>
        <TabPane tab="Liabilities" key="liabilities">
          {reportData.liabilities && Object.entries(reportData.liabilities).map(([category, accounts]) => (
            <div key={category} style={{ marginBottom: 24 }}>
              <Title level={4}>{category}</Title>
              <Table 
                dataSource={accounts} 
                columns={columns} 
                pagination={false}
                size="small"
                rowKey="accountName"
              />
            </div>
          ))}
        </TabPane>
        <TabPane tab="Equity" key="equity">
          {reportData.equity && Object.entries(reportData.equity).map(([category, accounts]) => (
            <div key={category} style={{ marginBottom: 24 }}>
              <Title level={4}>{category}</Title>
              <Table 
                dataSource={accounts} 
                columns={columns} 
                pagination={false}
                size="small"
                rowKey="accountName"
              />
            </div>
          ))}
        </TabPane>
      </Tabs>
    );
  };

  const renderIncomeExpensePreview = () => {
    if (!reportData || !Array.isArray(reportData)) return null;

    const columns = [
      { title: 'Type', dataIndex: 'type', key: 'type' },
      { title: 'Category', dataIndex: 'category', key: 'category' },
      { title: 'Description', dataIndex: 'description', key: 'description' },
      { title: 'Current Month', dataIndex: 'currentMonth', key: 'currentMonth',
        render: (value) => `$${Number(value || 0).toLocaleString()}` },
      { title: 'Year to Date', dataIndex: 'yearToDate', key: 'yearToDate',
        render: (value) => `$${Number(value || 0).toLocaleString()}` }
    ];

    return (
      <Table 
        dataSource={reportData} 
        columns={columns} 
        pagination={{ pageSize: 10 }}
        size="small"
        rowKey={(record, index) => `${record.type}_${record.category}_${index}`}
      />
    );
  };

  const renderFinancialGroupingPreview = () => {
    if (!reportData) return null;

    return (
      <Tabs defaultActiveKey="category">
        <TabPane tab="By Category" key="category">
          {reportData.categoryGrouping && (
            <Table 
              dataSource={Object.entries(reportData.categoryGrouping).map(([key, value]) => ({ key, value }))}
              columns={[
                { title: 'Category', dataIndex: 'key', key: 'key' },
                { title: 'Amount', dataIndex: 'value', key: 'value', 
                  render: (value) => `$${Number(value || 0).toLocaleString()}` }
              ]}
              pagination={false}
              size="small"
            />
          )}
        </TabPane>
        <TabPane tab="By Department" key="department">
          {reportData.departmentGrouping && (
            <Table 
              dataSource={Object.entries(reportData.departmentGrouping).map(([key, value]) => ({ key, value }))}
              columns={[
                { title: 'Department', dataIndex: 'key', key: 'key' },
                { title: 'Amount', dataIndex: 'value', key: 'value', 
                  render: (value) => `$${Number(value || 0).toLocaleString()}` }
              ]}
              pagination={false}
              size="small"
            />
          )}
        </TabPane>
      </Tabs>
    );
  };

  const renderIncomeStatementPreview = () => {
    return <div>Income Statement preview will be implemented when backend is ready</div>;
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Title level={2}>
          <FundProjectionScreenOutlined /> Financial Reports
        </Title>
        
        {testingConnection && (
          <Alert 
            message="Testing backend connection..." 
            type="info" 
            style={{ marginBottom: 16 }}
          />
        )}

        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} md={8}>
            <label>Report Type:</label>
            <Select 
              value={reportType} 
              onChange={setReportType}
              style={{ width: '100%' }}
              placeholder="Select report type"
            >
              {reportConfigs.map(config => (
                <Option key={config.value} value={config.value}>
                  {config.icon} {config.label}
                </Option>
              ))}
            </Select>
            {currentReportConfig && (
              <Text type="secondary" style={{ fontSize: '12px' }}>
                {currentReportConfig.description}
              </Text>
            )}
          </Col>

          <Col xs={24} sm={12} md={4}>
            <label>Company ID:</label>
            <Select 
              value={companyId} 
              onChange={setCompanyId}
              style={{ width: '100%' }}
            >
              <Option value={1}>Company 1</Option>
              <Option value={2}>Company 2</Option>
              <Option value={3}>Company 3</Option>
            </Select>
          </Col>

          {currentReportConfig?.useAsOfDate ? (
            <Col xs={24} sm={12} md={6}>
              <label>As of Date:</label>
              <DatePicker 
                value={asOfDate}
                onChange={setAsOfDate}
                style={{ width: '100%' }}
                format="YYYY-MM-DD"
              />
            </Col>
          ) : (
            <>
              <Col xs={24} sm={12} md={6}>
                <label>Start Date:</label>
                <DatePicker 
                  value={startDate}
                  onChange={setStartDate}
                  style={{ width: '100%' }}
                  format="YYYY-MM-DD"
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <label>End Date:</label>
                <DatePicker 
                  value={endDate}
                  onChange={setEndDate}
                  style={{ width: '100%' }}
                  format="YYYY-MM-DD"
                />
              </Col>
            </>
          )}
        </Row>

        <Space style={{ marginBottom: 16 }}>
          <Button 
            type="primary" 
            icon={<ReloadOutlined />}
            onClick={loadReportData}
            loading={loading}
          >
            Load Report
          </Button>
          
          <Button 
            type="default"
            icon={<DownloadOutlined />}
            onClick={handleExport}
            loading={exporting}
            disabled={!currentReportConfig}
          >
            {exporting ? 'Exporting...' : 'Export to Excel'}
          </Button>
        </Space>

        <Card 
          title={`${currentReportConfig?.label || 'Report'} Preview`}
          size="small"
          style={{ minHeight: '400px' }}
        >
          {renderReportPreview()}
        </Card>
      </Card>
    </div>
  );
};

export default FinancialReportsUnified;