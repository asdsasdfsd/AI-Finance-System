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

// frontend/src/views/Dashboard/FinancialReportsUnified.js

  // Replace the renderIncomeStatementPreview function with this implementation:
  const renderIncomeStatementPreview = () => {
    if (!reportData) return null;

    // Create sections for display
    const sections = [
      {
        title: 'I. Operating Revenue',
        items: reportData.revenues || [],
        total: reportData.totalRevenue,
        isRevenue: true
      },
      {
        title: 'II. Operating Expenses', 
        items: reportData.operatingExpenses || [],
        total: reportData.totalOperatingExpenses,
        isRevenue: false
      },
      {
        title: 'III. Administrative Expenses',
        items: reportData.administrativeExpenses || [],
        total: reportData.totalAdministrativeExpenses,
        isRevenue: false
      },
      {
        title: 'IV. Financial Expenses',
        items: reportData.financialExpenses || [],
        total: reportData.totalFinancialExpenses,
        isRevenue: false
      }
    ];

    // Add other income section if exists
    if (reportData.otherIncomes && reportData.otherIncomes.length > 0) {
      sections.push({
        title: 'V. Other Income',
        items: reportData.otherIncomes,
        total: reportData.totalOtherIncomes,
        isRevenue: true
      });
    }

    // Add other expenses section if exists
    if (reportData.otherExpenses && reportData.otherExpenses.length > 0) {
      sections.push({
        title: 'VI. Other Expenses',
        items: reportData.otherExpenses,
        total: reportData.totalOtherExpenses,
        isRevenue: false
      });
    }

    return (
      <div style={{ padding: '20px' }}>
        {/* Company Header */}
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          <Title level={3}>{reportData.companyName}</Title>
          <Title level={4}>Income Statement</Title>
          <Text>{reportData.periodDescription}</Text>
        </div>

        {/* Revenue and Expense Sections */}
        {sections.map((section, index) => (
          <div key={index} style={{ marginBottom: '25px' }}>
            <Title level={5} style={{ 
              backgroundColor: '#f5f5f5', 
              padding: '10px',
              margin: '0 0 10px 0',
              fontWeight: 'bold'
            }}>
              {section.title}
            </Title>
            
            {/* Section Items */}
            {section.items.map((item, itemIndex) => (
              <Row key={itemIndex} style={{ padding: '5px 20px' }}>
                <Col span={16}>
                  <Text>{item.name || item.category}</Text>
                </Col>
                <Col span={8} style={{ textAlign: 'right' }}>
                  <Text>${Number(item.amount || 0).toLocaleString()}</Text>
                </Col>
              </Row>
            ))}
            
            {/* Section Total */}
            <Row style={{ 
              padding: '5px 20px', 
              borderTop: '1px solid #d9d9d9',
              fontWeight: 'bold',
              backgroundColor: '#fafafa'
            }}>
              <Col span={16}>
                <Text strong>Subtotal</Text>
              </Col>
              <Col span={8} style={{ textAlign: 'right' }}>
                <Text strong>${Number(section.total || 0).toLocaleString()}</Text>
              </Col>
            </Row>
          </div>
        ))}

        {/* Summary Section */}
        <div style={{ 
          marginTop: '30px', 
          borderTop: '2px solid #000',
          paddingTop: '15px'
        }}>
          <Title level={5}>Summary</Title>
          
          <Row style={{ padding: '5px 0' }}>
            <Col span={16}>
              <Text strong>Total Revenue</Text>
            </Col>
            <Col span={8} style={{ textAlign: 'right' }}>
              <Text strong>${Number(reportData.totalRevenue || 0).toLocaleString()}</Text>
            </Col>
          </Row>
          
          <Row style={{ padding: '5px 0' }}>
            <Col span={16}>
              <Text strong>Total Expenses</Text>
            </Col>
            <Col span={8} style={{ textAlign: 'right' }}>
              <Text strong>${Number(reportData.totalExpenses || 0).toLocaleString()}</Text>
            </Col>
          </Row>
          
          <Row style={{ 
            padding: '10px 0',
            borderTop: '1px solid #000',
            fontSize: '16px'
          }}>
            <Col span={16}>
              <Text strong style={{ fontSize: '16px' }}>Net Income</Text>
            </Col>
            <Col span={8} style={{ textAlign: 'right' }}>
              <Text strong style={{ 
                fontSize: '16px',
                color: reportData.netIncome >= 0 ? '#52c41a' : '#ff4d4f'
              }}>
                ${Number(reportData.netIncome || 0).toLocaleString()}
              </Text>
            </Col>
          </Row>

          {/* Additional Metrics */}
          {reportData.grossProfitMargin !== undefined && (
            <Row style={{ padding: '5px 0', marginTop: '10px' }}>
              <Col span={16}>
                <Text>Gross Profit Margin</Text>
              </Col>
              <Col span={8} style={{ textAlign: 'right' }}>
                <Text>{Number(reportData.grossProfitMargin || 0).toFixed(1)}%</Text>
              </Col>
            </Row>
          )}
          
          {reportData.netProfitMargin !== undefined && (
            <Row style={{ padding: '5px 0' }}>
              <Col span={16}>
                <Text>Net Profit Margin</Text>
              </Col>
              <Col span={8} style={{ textAlign: 'right' }}>
                <Text>{Number(reportData.netProfitMargin || 0).toFixed(1)}%</Text>
              </Col>
            </Row>
          )}
        </div>
      </div>
    );
  };

  // Replace the renderIncomeExpensePreview function with this implementation:
  const renderIncomeExpensePreview = () => {
    if (!reportData) return null;
    
    // Handle the correct data structure - reportData is an object with incomeRows and expenseRows
    const { incomeRows = [], expenseRows = [] } = reportData;

    const columns = [
      { 
        title: 'Category', 
        dataIndex: 'category', 
        key: 'category',
        width: '20%'
      },
      { 
        title: 'Description', 
        dataIndex: 'description', 
        key: 'description',
        width: '25%'
      },
      { 
        title: 'Current Month', 
        dataIndex: 'currentMonth', 
        key: 'currentMonth',
        width: '15%',
        align: 'right',
        render: (value) => `$${Number(value || 0).toLocaleString()}`
      },
      { 
        title: 'Year to Date', 
        dataIndex: 'yearToDate', 
        key: 'yearToDate',
        width: '15%',
        align: 'right',
        render: (value) => `$${Number(value || 0).toLocaleString()}`
      }
    ];

    return (
      <div style={{ padding: '20px' }}>
        {/* Company Header */}
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          <Title level={3}>{reportData.companyName}</Title>
          <Title level={4}>Income vs Expense Report</Title>
          <Text>As of {reportData.asOfDate}</Text>
        </div>

        <Tabs defaultActiveKey="income">
          <TabPane tab="Income" key="income">
            <div style={{ marginBottom: '20px' }}>
              <Title level={5} style={{ color: '#52c41a' }}>Income Details</Title>
              <Table 
                dataSource={incomeRows} 
                columns={columns} 
                pagination={false}
                size="small"
                rowKey={(record, index) => `income_${index}`}
                summary={() => (
                  <Table.Summary>
                    <Table.Summary.Row style={{ backgroundColor: '#f6ffed' }}>
                      <Table.Summary.Cell colSpan={2}>
                        <Text strong>Total Income</Text>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell>
                        <Text strong style={{ color: '#52c41a' }}>
                          ${Number(reportData.totalIncomeMonth || 0).toLocaleString()}
                        </Text>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell>
                        <Text strong style={{ color: '#52c41a' }}>
                          ${Number(reportData.totalIncomeYTD || 0).toLocaleString()}
                        </Text>
                      </Table.Summary.Cell>
                    </Table.Summary.Row>
                  </Table.Summary>
                )}
              />
            </div>
          </TabPane>

          <TabPane tab="Expenses" key="expenses">
            <div style={{ marginBottom: '20px' }}>
              <Title level={5} style={{ color: '#ff4d4f' }}>Expense Details</Title>
              <Table 
                dataSource={expenseRows} 
                columns={columns} 
                pagination={false}
                size="small"
                rowKey={(record, index) => `expense_${index}`}
                summary={() => (
                  <Table.Summary>
                    <Table.Summary.Row style={{ backgroundColor: '#fff2f0' }}>
                      <Table.Summary.Cell colSpan={2}>
                        <Text strong>Total Expenses</Text>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell>
                        <Text strong style={{ color: '#ff4d4f' }}>
                          ${Number(reportData.totalExpenseMonth || 0).toLocaleString()}
                        </Text>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell>
                        <Text strong style={{ color: '#ff4d4f' }}>
                          ${Number(reportData.totalExpenseYTD || 0).toLocaleString()}
                        </Text>
                      </Table.Summary.Cell>
                    </Table.Summary.Row>
                  </Table.Summary>
                )}
              />
            </div>
          </TabPane>

          <TabPane tab="Summary" key="summary">
            <div style={{ padding: '20px' }}>
              <Title level={5}>Financial Summary</Title>
              
              <Row gutter={[16, 16]}>
                <Col xs={24} md={12}>
                  <Card size="small" title="Current Month">
                    <div style={{ marginBottom: '10px' }}>
                      <Text>Total Income: </Text>
                      <Text strong style={{ color: '#52c41a' }}>
                        ${Number(reportData.totalIncomeMonth || 0).toLocaleString()}
                      </Text>
                    </div>
                    <div style={{ marginBottom: '10px' }}>
                      <Text>Total Expenses: </Text>
                      <Text strong style={{ color: '#ff4d4f' }}>
                        ${Number(reportData.totalExpenseMonth || 0).toLocaleString()}
                      </Text>
                    </div>
                    <div style={{ 
                      paddingTop: '10px', 
                      borderTop: '1px solid #d9d9d9' 
                    }}>
                      <Text>Net Income: </Text>
                      <Text strong style={{ 
                        color: reportData.netIncomeMonth >= 0 ? '#52c41a' : '#ff4d4f',
                        fontSize: '16px'
                      }}>
                        ${Number(reportData.netIncomeMonth || 0).toLocaleString()}
                      </Text>
                    </div>
                  </Card>
                </Col>
                
                <Col xs={24} md={12}>
                  <Card size="small" title="Year to Date">
                    <div style={{ marginBottom: '10px' }}>
                      <Text>Total Income: </Text>
                      <Text strong style={{ color: '#52c41a' }}>
                        ${Number(reportData.totalIncomeYTD || 0).toLocaleString()}
                      </Text>
                    </div>
                    <div style={{ marginBottom: '10px' }}>
                      <Text>Total Expenses: </Text>
                      <Text strong style={{ color: '#ff4d4f' }}>
                        ${Number(reportData.totalExpenseYTD || 0).toLocaleString()}
                      </Text>
                    </div>
                    <div style={{ 
                      paddingTop: '10px', 
                      borderTop: '1px solid #d9d9d9' 
                    }}>
                      <Text>Net Income: </Text>
                      <Text strong style={{ 
                        color: reportData.netIncomeYTD >= 0 ? '#52c41a' : '#ff4d4f',
                        fontSize: '16px'
                      }}>
                        ${Number(reportData.netIncomeYTD || 0).toLocaleString()}
                      </Text>
                    </div>
                  </Card>
                </Col>
              </Row>
            </div>
          </TabPane>
        </Tabs>
      </div>
    );
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