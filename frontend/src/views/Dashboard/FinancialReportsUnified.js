// frontend/src/views/Dashboard/FinancialReportsUnified.js
import React, { useState, useEffect } from 'react';
import { 
  Card, Row, Col, Button, Select, DatePicker, Space, Spin, Typography, message, 
  Alert, Tag, Table, Divider, Descriptions, Tooltip
} from 'antd';
import { 
  FileTextOutlined, DownloadOutlined, EyeOutlined, DollarCircleOutlined, 
  PieChartOutlined, FundOutlined, BarChartOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined, LoadingOutlined, ReloadOutlined, InfoCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { API_CONFIG, createApiClient } from '../../config/apiConfig';

const { Option } = Select;
const { Text, Title } = Typography;

export default function FinancialReportsUnified() {
  // State management
  const [companyId] = useState(1);
  const [previewReportType, setPreviewReportType] = useState('BALANCE_SHEET');
  const [asOfDate, setAsOfDate] = useState(dayjs('2024-03-31'));
  const [startDate, setStartDate] = useState(dayjs('2024-01-01'));
  const [endDate, setEndDate] = useState(dayjs('2024-03-31'));
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
      previewApi: '/api/income-expense/json',
      exportApi: '/api/income-expense/export',
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

  // Test backend connection
  useEffect(() => {
    testBackendConnection();
  }, []);

  const testBackendConnection = async () => {
    setTestingConnection(true);
    try {
      await apiClient.get('/api/health', { timeout: 5000 });
      setConnectionStatus('connected');
    } catch (error) {
      console.error('Backend connection failed:', error);
      setConnectionStatus('disconnected');
    } finally {
      setTestingConnection(false);
    }
  };

  const getConnectionStatusTag = () => {
    if (testingConnection) {
      return <Tag icon={<LoadingOutlined />} color="processing">Testing Connection...</Tag>;
    }
    
    switch (connectionStatus) {
      case 'connected':
        return <Tag icon={<CheckCircleOutlined />} color="success">Backend Connected</Tag>;
      case 'disconnected':
        return <Tag icon={<ExclamationCircleOutlined />} color="error">Backend Disconnected</Tag>;
      default:
        return <Tag color="default">Connection Status Unknown</Tag>;
    }
  };

  const handleReportTypeChange = (value) => {
    setPreviewReportType(value);
    const config = reportTypes.find(rt => rt.value === value);
    
    if (config) {
      if (config.useAsOfDate && config.suggestedDate) {
        setAsOfDate(dayjs(config.suggestedDate));
      } else if (!config.useAsOfDate && config.suggestedStartDate && config.suggestedEndDate) {
        setStartDate(dayjs(config.suggestedStartDate));
        setEndDate(dayjs(config.suggestedEndDate));
      }
    }
  };

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
      message.error(`Failed to export report: ${error.message}`);
    }
  };

  const renderParameterSelection = () => {
    const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
    
    return (
      <div>
        <Alert
          message="Test Data Available"
          description={
            <div>
              📊 <strong>Available test data period:</strong> 2024-01-01 to 2024-03-31 for Company ID: 1
              <br />
              💡 <strong>Recommended dates:</strong> Balance Sheet (2024-03-31), Income Statement (2024-01-01 to 2024-03-31)
            </div>
          }
          type="info"
          style={{ marginBottom: 16 }}
          showIcon
        />
        
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={8}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Report Type</Text>
              <Select
                value={previewReportType}
                onChange={handleReportTypeChange}
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
                <Text strong>
                  As of Date 
                  <Tooltip title="Select a date to view financial position. Recommended: 2024-03-31">
                    <InfoCircleOutlined style={{ marginLeft: 4, color: '#1890ff' }} />
                  </Tooltip>
                </Text>
                <DatePicker
                  value={asOfDate}
                  onChange={setAsOfDate}
                  format="YYYY-MM-DD"
                  style={{ width: '100%' }}
                  size="large"
                />
                <Text type="secondary" style={{ fontSize: '12px' }}>
                  Suggested: {currentConfig.suggestedDate}
                </Text>
              </Space>
            ) : (
              <Row gutter={16}>
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
                    <Text type="secondary" style={{ fontSize: '12px' }}>
                      Suggested: {currentConfig?.suggestedStartDate}
                    </Text>
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
                    <Text type="secondary" style={{ fontSize: '12px' }}>
                      Suggested: {currentConfig?.suggestedEndDate}
                    </Text>
                  </Space>
                </Col>
              </Row>
            )}
          </Col>
          
          <Col xs={24} sm={8}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Actions</Text>
              <Space>
                <Button 
                  type="primary" 
                  icon={<EyeOutlined />}
                  onClick={handlePreview}
                  loading={previewLoading}
                  size="large"
                >
                  Preview
                </Button>
                <Button 
                  icon={<DownloadOutlined />}
                  onClick={handleExport}
                  size="large"
                >
                  Export
                </Button>
              </Space>
            </Space>
          </Col>
        </Row>
      </div>
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

  // Balance Sheet Renderer
  const renderBalanceSheet = () => {
    if (!reportData) return null;

    const renderSection = (sectionName, sectionData, sectionTotal) => {
      if (!sectionData || typeof sectionData !== 'object') {
        return (
          <Card title={sectionName} style={{ marginBottom: 16 }}>
            <Text type="secondary">No data available</Text>
          </Card>
        );
      }

      const columns = [
        {
          title: 'Account Name',
          dataIndex: 'accountName',
          key: 'accountName',
          width: 300,
        },
        {
          title: 'Current Month',
          dataIndex: 'currentMonth',
          key: 'currentMonth',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
        },
        {
          title: 'Previous Month',
          dataIndex: 'previousMonth',
          key: 'previousMonth',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
        },
        {
          title: 'Last Year End',
          dataIndex: 'lastYearEnd',
          key: 'lastYearEnd',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
        }
      ];

      let dataSource = [];
      Object.entries(sectionData).forEach(([categoryName, accounts]) => {
        if (Array.isArray(accounts)) {
          accounts.forEach((account, index) => {
            dataSource.push({
              key: `${categoryName}-${index}`,
              accountName: account.accountName || 'Unknown Account',
              currentMonth: account.currentMonth || 0,
              previousMonth: account.previousMonth || 0,
              lastYearEnd: account.lastYearEnd || 0,
            });
          });
        }
      });

      return (
        <Card 
          title={
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>{sectionName}</span>
              <span style={{ fontWeight: 'bold', color: '#1890ff' }}>
                Total: CNY {Number(sectionTotal || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
          }
          style={{ marginBottom: 16 }}
        >
          <Table
            columns={columns}
            dataSource={dataSource}
            pagination={false}
            size="small"
            bordered
          />
        </Card>
      );
    };

    return (
      <div>
        <Descriptions 
          title="Balance Sheet Overview" 
          bordered 
          column={2}
          style={{ marginBottom: 24 }}
        >
          <Descriptions.Item label="Company">Tech Innovation Ltd</Descriptions.Item>
          <Descriptions.Item label="As of Date">{reportData.asOfDate}</Descriptions.Item>
          <Descriptions.Item label="Currency">CNY</Descriptions.Item>
          <Descriptions.Item label="Balance Status">
            <Tag color={reportData.isBalanced ? 'green' : 'red'}>
              {reportData.isBalanced ? 'Balanced' : 'Not Balanced'}
            </Tag>
          </Descriptions.Item>
        </Descriptions>

        {renderSection('Assets', reportData.assets, reportData.totalAssets)}
        {renderSection('Liabilities', reportData.liabilities, reportData.totalLiabilities)}
        {renderSection('Equity', reportData.equity, reportData.totalEquity)}

        <Card title="Balance Sheet Summary" style={{ marginTop: 16 }}>
          <Row gutter={24}>
            <Col span={8}>
              <div style={{ textAlign: 'center', padding: 16 }}>
                <Title level={4} style={{ color: '#52c41a' }}>Total Assets</Title>
                <Title level={3}>CNY {Number(reportData.totalAssets || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
              </div>
            </Col>
            <Col span={8}>
              <div style={{ textAlign: 'center', padding: 16 }}>
                <Title level={4} style={{ color: '#ff4d4f' }}>Total Liabilities</Title>
                <Title level={3}>CNY {Number(reportData.totalLiabilities || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
              </div>
            </Col>
            <Col span={8}>
              <div style={{ textAlign: 'center', padding: 16 }}>
                <Title level={4} style={{ color: '#1890ff' }}>Total Equity</Title>
                <Title level={3}>CNY {Number(reportData.totalEquity || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
              </div>
            </Col>
          </Row>
        </Card>
      </div>
    );
  };

  // Income Statement Renderer
  const renderIncomeStatement = () => {
    if (!reportData) return null;

    const columns = [
      {
        title: 'Category',
        dataIndex: 'category',
        key: 'category',
        width: 200,
      },
      {
        title: 'Amount',
        dataIndex: 'amount',
        key: 'amount',
        align: 'right',
        render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
      }
    ];

    const revenueData = reportData.revenueByCategory ? 
      Object.entries(reportData.revenueByCategory).map(([category, amount], index) => ({
        key: `revenue-${index}`,
        category: category,
        amount
      })) : [];

    const expenseData = reportData.expensesByCategory ? 
      Object.entries(reportData.expensesByCategory).map(([category, amount], index) => ({
        key: `expense-${index}`,
        category: category,
        amount
      })) : [];

    return (
      <div>
        <Descriptions 
          title="Income Statement Overview" 
          bordered 
          column={2}
          style={{ marginBottom: 24 }}
        >
          <Descriptions.Item label="Company">Tech Innovation Ltd</Descriptions.Item>
          <Descriptions.Item label="Period">{reportData.periodStartDate || reportData.startDate} to {reportData.periodEndDate || reportData.endDate}</Descriptions.Item>
          <Descriptions.Item label="Currency">CNY</Descriptions.Item>
          <Descriptions.Item label="Generated At">{reportData.generatedAt}</Descriptions.Item>
          <Descriptions.Item label="Transaction Count">{reportData.transactionCount || 0}</Descriptions.Item>
        </Descriptions>

        <Card title="Revenue" style={{ marginBottom: 16 }}>
          <Table
            columns={columns}
            dataSource={revenueData}
            pagination={false}
            size="small"
            bordered
            summary={() => (
              <Table.Summary.Row>
                <Table.Summary.Cell><strong>Total Revenue</strong></Table.Summary.Cell>
                <Table.Summary.Cell align="right">
                  <strong style={{ color: '#52c41a' }}>
                    CNY {Number(reportData.totalRevenue || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </strong>
                </Table.Summary.Cell>
              </Table.Summary.Row>
            )}
          />
        </Card>

        <Card title="Expenses" style={{ marginBottom: 16 }}>
          <Table
            columns={columns}
            dataSource={expenseData}
            pagination={false}
            size="small"
            bordered
            summary={() => (
              <Table.Summary.Row>
                <Table.Summary.Cell><strong>Total Expenses</strong></Table.Summary.Cell>
                <Table.Summary.Cell align="right">
                  <strong style={{ color: '#ff4d4f' }}>
                    CNY {Number(reportData.totalExpenses || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </strong>
                </Table.Summary.Cell>
              </Table.Summary.Row>
            )}
          />
        </Card>

        <Card title="Net Income Summary">
          <div style={{ textAlign: 'center', padding: 20 }}>
            <Title level={3} style={{ 
              color: (reportData.netIncome || 0) >= 0 ? '#52c41a' : '#ff4d4f' 
            }}>
              Net Income: CNY {Number(reportData.netIncome || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </Title>
          </div>
        </Card>
      </div>
    );
  };

  // Income vs Expense Report Renderer
  const renderIncomeExpenseReport = () => {
    if (!reportData) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Text>No income expense data available</Text>
        </div>
      );
    }

    // Check if we have the expected data structure
    if (reportData.incomeRows || reportData.expenseRows) {
      // Structured format with rows
      const incomeColumns = [
        { title: 'Category', dataIndex: 'category', key: 'category' },
        { title: 'Description', dataIndex: 'description', key: 'description' },
        { 
          title: 'Current Month', 
          dataIndex: 'currentMonth', 
          key: 'currentMonth',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` 
        },
        { 
          title: 'Year to Date', 
          dataIndex: 'yearToDate', 
          key: 'yearToDate',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` 
        },
        { 
          title: 'Variance', 
          dataIndex: 'variance', 
          key: 'variance',
          align: 'right',
          render: (value) => value ? `CNY ${Number(value).toLocaleString('en-US', { minimumFractionDigits: 2 })}` : 'N/A'
        }
      ];

      return (
        <div>
          <Descriptions 
            title="Income vs Expense Report Overview" 
            bordered 
            column={2}
            style={{ marginBottom: 24 }}
          >
            <Descriptions.Item label="Company">Tech Innovation Ltd</Descriptions.Item>
            <Descriptions.Item label="As of Date">{reportData.asOfDate}</Descriptions.Item>
            <Descriptions.Item label="Currency">CNY</Descriptions.Item>
            <Descriptions.Item label="Generated At">{reportData.generatedAt}</Descriptions.Item>
          </Descriptions>

          {reportData.incomeRows && reportData.incomeRows.length > 0 && (
            <Card title="Income Analysis" style={{ marginBottom: 16 }}>
              <Table 
                columns={incomeColumns}
                dataSource={reportData.incomeRows.map((row, index) => ({ ...row, key: `income-${index}` }))}
                pagination={false}
                size="small"
                bordered
              />
            </Card>
          )}
          
          {reportData.expenseRows && reportData.expenseRows.length > 0 && (
            <Card title="Expense Analysis">
              <Table 
                columns={incomeColumns}
                dataSource={reportData.expenseRows.map((row, index) => ({ ...row, key: `expense-${index}` }))}
                pagination={false}
                size="small"
                bordered
              />
            </Card>
          )}

          {reportData.totalIncome !== undefined && reportData.totalExpense !== undefined && (
            <Card title="Summary" style={{ marginTop: 16 }}>
              <Row gutter={24}>
                <Col span={8}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4} style={{ color: '#52c41a' }}>Total Income</Title>
                    <Title level={3}>CNY {Number(reportData.totalIncome || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                  </div>
                </Col>
                <Col span={8}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4} style={{ color: '#ff4d4f' }}>Total Expense</Title>
                    <Title level={3}>CNY {Number(reportData.totalExpense || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                  </div>
                </Col>
                <Col span={8}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4} style={{ color: (reportData.totalIncome - reportData.totalExpense) >= 0 ? '#52c41a' : '#ff4d4f' }}>Net Result</Title>
                    <Title level={3}>CNY {Number((reportData.totalIncome || 0) - (reportData.totalExpense || 0)).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                  </div>
                </Col>
              </Row>
            </Card>
          )}
        </div>
      );
    } else {
      // Raw data display for debugging
      return (
        <div>
          <Title level={4}>Income vs Expense Report Preview</Title>
          <Alert 
            message="Data Structure Analysis" 
            description="Displaying available data. The backend API structure may need adjustment for optimal presentation."
            type="info" 
            style={{ marginBottom: 16 }}
          />
          <Card title="Raw Report Data">
            <pre style={{ 
              background: '#f5f5f5', 
              padding: '16px', 
              overflow: 'auto', 
              borderRadius: 4,
              fontSize: '12px',
              lineHeight: '1.4'
            }}>
              {JSON.stringify(reportData, null, 2)}
            </pre>
          </Card>
        </div>
      );
    }
  };

  // Financial Grouping Report Renderer
  const renderFinancialGroupingReport = () => {
    if (!reportData) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Text>No financial grouping data available</Text>
        </div>
      );
    }

    // Check if we have the expected data structure for financial grouping
    // Based on FinancialGroupingData structure from backend
    if (reportData.byCategory || reportData.byDepartment || reportData.byMonth || reportData.byFund) {
      const groupingColumns = [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { 
          title: 'Total Amount', 
          dataIndex: 'totalAmount', 
          key: 'totalAmount',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` 
        },
        { title: 'Transaction Count', dataIndex: 'transactionCount', key: 'transactionCount', align: 'center' },
        { 
          title: 'Average Amount', 
          dataIndex: 'averageAmount', 
          key: 'averageAmount',
          align: 'right',
          render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` 
        }
      ];

      return (
        <div>
          <Descriptions 
            title="Financial Grouping Report Overview" 
            bordered 
            column={2}
            style={{ marginBottom: 24 }}
          >
            <Descriptions.Item label="Company">Tech Innovation Ltd</Descriptions.Item>
            <Descriptions.Item label="Period">{reportData.startDate} to {reportData.endDate}</Descriptions.Item>
            <Descriptions.Item label="Currency">CNY</Descriptions.Item>
            <Descriptions.Item label="Generated At">{reportData.generatedAt}</Descriptions.Item>
            <Descriptions.Item label="Period Description">{reportData.periodDescription}</Descriptions.Item>
            <Descriptions.Item label="Total Transactions">{reportData.totalTransactionCount || 0}</Descriptions.Item>
          </Descriptions>

          {reportData.byCategory && Object.keys(reportData.byCategory).length > 0 && (
            <Card title="Grouping by Category" style={{ marginBottom: 16 }}>
              <Table 
                columns={groupingColumns}
                dataSource={Object.entries(reportData.byCategory).map(([name, data], index) => ({
                  key: `category-${index}`,
                  name: name,
                  totalAmount: data.totalAmount,
                  transactionCount: data.transactionCount,
                  averageAmount: data.averageAmount
                }))}
                pagination={false}
                size="small"
                bordered
              />
            </Card>
          )}

          {reportData.byDepartment && Object.keys(reportData.byDepartment).length > 0 && (
            <Card title="Grouping by Department" style={{ marginBottom: 16 }}>
              <Table 
                columns={groupingColumns}
                dataSource={Object.entries(reportData.byDepartment).map(([name, data], index) => ({
                  key: `department-${index}`,
                  name,
                  totalAmount: data.totalAmount,
                  transactionCount: data.transactionCount,
                  averageAmount: data.averageAmount
                }))}
                pagination={false}
                size="small"
                bordered
              />
            </Card>
          )}

          {reportData.byFund && Object.keys(reportData.byFund).length > 0 && (
            <Card title="Grouping by Fund" style={{ marginBottom: 16 }}>
              <Table 
                columns={groupingColumns}
                dataSource={Object.entries(reportData.byFund).map(([name, data], index) => ({
                  key: `fund-${index}`,
                  name,
                  totalAmount: data.totalAmount,
                  transactionCount: data.transactionCount,
                  averageAmount: data.averageAmount
                }))}
                pagination={false}
                size="small"
                bordered
              />
            </Card>
          )}

          {reportData.byMonth && Object.keys(reportData.byMonth).length > 0 && (
            <Card title="Grouping by Month" style={{ marginBottom: 16 }}>
              <Table 
                columns={[
                  { title: 'Month', dataIndex: 'name', key: 'name' },
                  { 
                    title: 'Total Amount', 
                    dataIndex: 'totalAmount', 
                    key: 'totalAmount',
                    align: 'right',
                    render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` 
                  },
                  { title: 'Transaction Count', dataIndex: 'transactionCount', key: 'transactionCount', align: 'center' },
                  { 
                    title: 'Average Amount', 
                    dataIndex: 'averageAmount', 
                    key: 'averageAmount',
                    align: 'right',
                    render: (value) => `CNY ${Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` 
                  }
                ]}
                dataSource={Object.entries(reportData.byMonth).map(([name, data], index) => ({
                  key: `month-${index}`,
                  name: data.displayName || name,
                  totalAmount: data.totalAmount,
                  transactionCount: data.transactionCount,
                  averageAmount: data.averageAmount
                }))}
                pagination={false}
                size="small"
                bordered
              />
            </Card>
          )}

          {reportData.grandTotal && (
            <Card title="Summary Statistics" style={{ marginTop: 16 }}>
              <Row gutter={24}>
                <Col span={6}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4}>Total Transactions</Title>
                    <Title level={3}>{reportData.totalTransactionCount || 0}</Title>
                  </div>
                </Col>
                <Col span={6}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4}>Grand Total</Title>
                    <Title level={3}>CNY {Number(reportData.grandTotal || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                  </div>
                </Col>
                <Col span={6}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4}>Categories</Title>
                    <Title level={3}>{reportData.byCategory ? Object.keys(reportData.byCategory).length : 0}</Title>
                  </div>
                </Col>
                <Col span={6}>
                  <div style={{ textAlign: 'center', padding: 16 }}>
                    <Title level={4}>Departments</Title>
                    <Title level={3}>{reportData.byDepartment ? Object.keys(reportData.byDepartment).length : 0}</Title>
                  </div>
                </Col>
              </Row>
            </Card>
          )}
        </div>
      );
    } else {
      // Raw data display for debugging
      return (
        <div>
          <Title level={4}>Financial Grouping Report Preview</Title>
          <Alert 
            message="Checking Data Structure" 
            description={`Data keys found: ${Object.keys(reportData).join(', ')}`}
            type="info" 
            style={{ marginBottom: 16 }}
          />
          <Card title="Raw Report Data">
            <pre style={{ 
              background: '#f5f5f5', 
              padding: '16px', 
              overflow: 'auto', 
              borderRadius: 4,
              fontSize: '12px',
              lineHeight: '1.4'
            }}>
              {JSON.stringify(reportData, null, 2)}
            </pre>
          </Card>
        </div>
      );
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      {/* Backend Connection Status */}
      <Alert
        message={
          <Row justify="space-between" align="middle">
            <Col>
              <Space>
                <Text strong>Backend Connection Status:</Text>
                {getConnectionStatusTag()}
              </Space>
            </Col>
            <Col>
              <Button 
                size="small" 
                icon={<ReloadOutlined />}
                onClick={testBackendConnection}
                loading={testingConnection}
              >
                Test Connection
              </Button>
            </Col>
          </Row>
        }
        type={connectionStatus === 'connected' ? 'success' : connectionStatus === 'disconnected' ? 'error' : 'info'}
        style={{ marginBottom: 24 }}
        showIcon
      />

      <Card>
        <Title level={2}>
          <Space>
            <BarChartOutlined />
            Financial Reports Preview & Export
          </Space>
        </Title>
        
        {/* Parameter Selection */}
        <Card 
          title="Report Parameters" 
          style={{ marginBottom: '24px' }}
          size="small"
        >
          {renderParameterSelection()}
        </Card>
        
        {/* Report Content */}
        <Card title="Report Preview">
          {renderReportContent()}
        </Card>
      </Card>
    </div>
  );
}