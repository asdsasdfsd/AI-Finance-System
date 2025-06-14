// frontend/src/views/Dashboard/FinancialReportsUnified.js
import React, { useState, useEffect } from 'react';
import {
  Card, Select, Button, Space, message, Row, Col, Typography, 
  DatePicker, InputNumber, Tabs, Form, Input, Switch, 
  Alert, Spin, Divider, Tag, Tooltip, Table
} from 'antd';
import {
  FundProjectionScreenOutlined, EyeOutlined, PlayCircleOutlined,
  DownloadOutlined, ReloadOutlined, InfoCircleOutlined,
  BarChartOutlined, DollarCircleOutlined, PieChartOutlined,
  FundOutlined, CheckCircleOutlined, LoadingOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';
import BalanceSheetService from '../../services/balanceSheetService';

const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;
const { RangePicker } = DatePicker;

/**
 * Unified Financial Reports Component
 * 
 * Combines report preview and DDD-based report generation
 */
const FinancialReportsUnified = () => {
  // Form instances
  const [previewForm] = Form.useForm();
  const [generateForm] = Form.useForm();
  
  // State management
  const [activeTab, setActiveTab] = useState('preview');
  const [previewLoading, setPreviewLoading] = useState(false);
  const [generateLoading, setGenerateLoading] = useState(false);
  const [reportData, setReportData] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState('unknown');
  const [testingConnection, setTestingConnection] = useState(false);

  // Preview form states
  const [previewReportType, setPreviewReportType] = useState('BALANCE_SHEET');
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [startDate, setStartDate] = useState(dayjs().subtract(1, 'month'));
  const [endDate, setEndDate] = useState(dayjs());

  // Generate form states
  const [selectedReportType, setSelectedReportType] = useState(null);

  // Report configurations
  const reportTypes = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      icon: <BarChartOutlined />,
      description: 'Assets, Liabilities, and Equity at a specific date',
      useAsOfDate: true,
      canPreview: true,
      previewApi: '/api/balance-sheet/json',
      exportApi: '/api/balance-sheet/export'
    },
    {
      value: 'INCOME_STATEMENT',
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and Expenses over a period',
      useAsOfDate: false,
      canPreview: true,
      previewApi: '/api/income-statement/json',
      exportApi: '/api/income-statement/export'
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Detailed Income and Expense Analysis',
      useAsOfDate: true,
      canPreview: true,
      previewApi: '/api/financial-report/json',
      exportApi: '/api/financial-report/export'
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Report',
      icon: <FundOutlined />,
      description: 'Transactions grouped by various criteria',
      useAsOfDate: false,
      canPreview: true,
      previewApi: '/api/financial-grouping/json',
      exportApi: '/api/financial-grouping/export'
    }
  ];

  // Test backend connection on component mount
  useEffect(() => {
    testBackendConnection();
  }, []);

  const testBackendConnection = async () => {
    setTestingConnection(true);
    try {
      const result = await ReportService.testConnection();
      setConnectionStatus(result.success ? 'connected' : 'disconnected');
      if (!result.success) {
        console.error('Backend connection failed:', result.error);
      }
    } catch (error) {
      console.error('Connection test error:', error);
      setConnectionStatus('disconnected');
    } finally {
      setTestingConnection(false);
    }
  };

  // Preview Functions
  const handlePreview = async () => {
    setPreviewLoading(true);
    try {
      let data = null;
      const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
      
      if (!currentConfig?.previewApi) {
        throw new Error('Preview not available for this report type');
      }

      // Build parameters based on report type
      const params = new URLSearchParams({
        companyId: companyId,
      });

      if (currentConfig.useAsOfDate) {
        params.append('asOfDate', asOfDate.format('YYYY-MM-DD'));
      } else {
        params.append('startDate', startDate.format('YYYY-MM-DD'));
        params.append('endDate', endDate.format('YYYY-MM-DD'));
      }

      const response = await fetch(`${currentConfig.previewApi}?${params.toString()}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch ${currentConfig.label}: ${response.status} ${response.statusText}`);
      }
      
      data = await response.json();
      setReportData(data);
      message.success(`${currentConfig.label} preview loaded successfully`);
    } catch (error) {
      console.error('Error fetching report data:', error);
      message.error('Failed to load report preview: ' + error.message);
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
      
      if (!currentConfig?.exportApi) {
        throw new Error('Export not available for this report type');
      }

      // Build parameters for export
      const params = new URLSearchParams({
        companyId: companyId,
      });

      if (currentConfig.useAsOfDate) {
        params.append('asOfDate', asOfDate.format('YYYY-MM-DD'));
      } else {
        params.append('startDate', startDate.format('YYYY-MM-DD'));
        params.append('endDate', endDate.format('YYYY-MM-DD'));
      }

      const response = await fetch(`${currentConfig.exportApi}?${params.toString()}`, {
        method: 'GET'
      });
      
      if (!response.ok) {
        throw new Error('Export failed');
      }
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
      // Generate filename
      const dateStr = currentConfig.useAsOfDate 
        ? asOfDate.format('YYYY-MM-DD')
        : `${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}`;
      const filename = `${currentConfig.label.replace(/\s+/g, '_')}_${dateStr}.xlsx`;
      
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      message.success(`${currentConfig.label} exported successfully`);
    } catch (error) {
      console.error('Export error:', error);
      message.error('Failed to export report: ' + error.message);
    }
  };

  // Generate Functions
  const handleGenerate = async (values) => {
    setGenerateLoading(true);
    try {
      // Validate form data
      const validation = ReportService.validateReportRequest(values);
      if (!validation.isValid) {
        message.error(validation.errors.join(', '));
        return;
      }

      // Format dates and prepare request
      const reportRequest = {
        ...values,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
        aiAnalysisEnabled: values.aiAnalysisEnabled || false
      };
      delete reportRequest.dateRange;

      // Generate report
      const response = await ReportService.generateReport(reportRequest);
      
      if (response && (response.status === 'success' || response.reportId)) {
        message.success('Report generation started successfully!');
        generateForm.resetFields();
        setSelectedReportType(null);
        
        // Show success message with report ID
        const reportId = response.reportId || response.id;
        if (reportId) {
          message.info(`Report ID: ${reportId}. Check Report Management for progress.`);
        }
      } else {
        throw new Error(response?.message || 'Unexpected response format');
      }
    } catch (error) {
      console.error('Report generation error:', error);
      
      let errorMessage = 'Failed to generate report';
      if (error.message) {
        errorMessage += ': ' + error.message;
      }
      
      message.error(errorMessage);
      
      if (error.message.includes('connect') || error.message.includes('network')) {
        message.warning('Please check if the backend server is running on localhost:8085');
      }
    } finally {
      setGenerateLoading(false);
    }
  };

  const handleReportTypeChange = (value) => {
    setSelectedReportType(value);
    const reportType = reportTypes.find(rt => rt.value === value);
    
    if (reportType) {
      const currentDate = dayjs().format('YYYY-MM-DD');
      generateForm.setFieldsValue({
        reportName: `${reportType.label} - ${currentDate}`
      });
    }
  };

  // Utility Functions
  const getReportTypeIcon = (type) => {
    const reportType = reportTypes.find(rt => rt.value === type);
    return reportType ? reportType.icon : <FundProjectionScreenOutlined />;
  };

  const getConnectionStatusTag = () => {
    if (testingConnection) {
      return <Tag icon={<LoadingOutlined />} color="processing">Testing...</Tag>;
    }
    
    switch (connectionStatus) {
      case 'connected':
        return <Tag icon={<CheckCircleOutlined />} color="success">Backend Connected</Tag>;
      case 'disconnected':
        return <Tag icon={<ExclamationCircleOutlined />} color="error">Backend Disconnected</Tag>;
      default:
        return <Tag color="default">Connection Unknown</Tag>;
    }
  };

  const getCurrentReportType = () => {
    return reportTypes.find(rt => rt.value === selectedReportType);
  };

  // Render Functions
  const renderPreviewControls = () => {
    const currentConfig = reportTypes.find(rt => rt.value === previewReportType);
    
    return (
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Report Type:</Text>
            <Select
              value={previewReportType}
              onChange={setPreviewReportType}
              style={{ width: '100%' }}
              size="large"
            >
              {reportTypes.filter(type => type.canPreview).map(type => (
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

        <Col xs={24} sm={12} md={6}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Company ID:</Text>
            <InputNumber
              value={companyId}
              onChange={setCompanyId}
              min={1}
              style={{ width: '100%' }}
              size="large"
            />
          </Space>
        </Col>

        {currentConfig?.useAsOfDate ? (
          <Col xs={24} sm={12} md={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>As of Date:</Text>
              <DatePicker
                value={asOfDate}
                onChange={setAsOfDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
                size="large"
              />
            </Space>
          </Col>
        ) : (
          <>
            <Col xs={24} sm={12} md={3}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text strong>Start Date:</Text>
                <DatePicker
                  value={startDate}
                  onChange={setStartDate}
                  format="YYYY-MM-DD"
                  style={{ width: '100%' }}
                  size="large"
                />
              </Space>
            </Col>
            <Col xs={24} sm={12} md={3}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text strong>End Date:</Text>
                <DatePicker
                  value={endDate}
                  onChange={setEndDate}
                  format="YYYY-MM-DD"
                  style={{ width: '100%' }}
                  size="large"
                />
              </Space>
            </Col>
          </>
        )}

        <Col xs={24} sm={12} md={6}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Actions:</Text>
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
                disabled={!reportData}
                size="large"
              >
                Export
              </Button>
            </Space>
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
    if (!reportData) return null;

    const renderSection = (sectionName, sectionData, total) => {
      if (!sectionData || typeof sectionData !== 'object') {
        return (
          <div style={{ marginBottom: 24 }}>
            <Title level={4}>{sectionName}</Title>
            <Alert
              message={`No data available for ${sectionName}`}
              type="warning"
              style={{ margin: '10px 0' }}
            />
          </div>
        );
      }

      const sectionEntries = Object.entries(sectionData);
      
      if (sectionEntries.length === 0) {
        return (
          <div style={{ marginBottom: 24 }}>
            <Title level={4}>{sectionName}</Title>
            <Alert
              message={`No accounts found in ${sectionName}`}
              type="info"
              style={{ margin: '10px 0' }}
            />
          </div>
        );
      }

      return (
        <div style={{ marginBottom: 24 }}>
          <Title level={4}>{sectionName}</Title>
          {sectionEntries.map(([category, accounts]) => {
            if (!Array.isArray(accounts)) {
              return (
                <div key={category} style={{ marginBottom: 16 }}>
                  <Text strong style={{ fontSize: '16px' }}>{category}</Text>
                  <Alert
                    message={`Invalid data format for category: ${category}`}
                    type="warning"
                    size="small"
                    style={{ marginTop: 8 }}
                  />
                </div>
              );
            }

            return (
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
                    {accounts.map((account, index) => {
                      if (!account || typeof account !== 'object') {
                        return (
                          <tr key={index}>
                            <td colSpan={4} style={{ padding: '8px', border: '1px solid #d9d9d9', textAlign: 'center' }}>
                              <Text type="warning">Invalid account data</Text>
                            </td>
                          </tr>
                        );
                      }

                      return (
                        <tr key={index}>
                          <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>
                            {account.accountName || 'Unknown Account'}
                          </td>
                          <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                            {typeof account.currentMonth === 'number' 
                              ? account.currentMonth.toLocaleString(undefined, { minimumFractionDigits: 2 })
                              : '0.00'
                            }
                          </td>
                          <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                            {typeof account.previousMonth === 'number'
                              ? account.previousMonth.toLocaleString(undefined, { minimumFractionDigits: 2 })
                              : '0.00'
                            }
                          </td>
                          <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                            {typeof account.lastYearEnd === 'number'
                              ? account.lastYearEnd.toLocaleString(undefined, { minimumFractionDigits: 2 })
                              : '0.00'
                            }
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            );
          })}
          <div style={{ textAlign: 'right', marginTop: 16 }}>
            <Text strong style={{ fontSize: '18px' }}>
              TOTAL {sectionName.toUpperCase()}: {
                typeof total === 'number' 
                  ? total.toLocaleString(undefined, { minimumFractionDigits: 2 })
                  : '0.00'
              }
            </Text>
          </div>
        </div>
      );
    };

    return (
      <div>
        <Title level={3}>
          Balance Sheet as at {reportData.asOfDate || asOfDate.format('YYYY-MM-DD')}
        </Title>
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

  const renderIncomeStatement = () => {
    if (!reportData) return null;

    // Handle different data structures that might be returned
    const revenues = reportData.revenues || reportData.revenue || [];
    const operatingExpenses = reportData.operatingExpenses || reportData.expenses || [];
    const netIncome = reportData.netIncome || 0;

    const renderSection = (title, items, isExpense = false) => (
      <div style={{ marginBottom: 24 }}>
        <Title level={4}>{title}</Title>
        <table style={{ width: '100%', border: '1px solid #d9d9d9' }}>
          <thead>
            <tr style={{ backgroundColor: '#fafafa' }}>
              <th style={{ padding: '8px', textAlign: 'left', border: '1px solid #d9d9d9' }}>Account</th>
              <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Amount</th>
            </tr>
          </thead>
          <tbody>
            {(Array.isArray(items) ? items : []).map((item, index) => (
              <tr key={index}>
                <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>
                  {item.name || item.category || item.description || 'Unknown'}
                </td>
                <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                  {typeof item.amount === 'number'
                    ? item.amount.toLocaleString(undefined, { minimumFractionDigits: 2 })
                    : '0.00'
                  }
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );

    return (
      <div>
        <Title level={3}>
          Income Statement for {startDate.format('MMMM DD, YYYY')} to {endDate.format('MMMM DD, YYYY')}
        </Title>
        {renderSection('Revenue', revenues)}
        {renderSection('Operating Expenses', operatingExpenses, true)}
        <div style={{ textAlign: 'center', marginTop: 24, padding: '16px', backgroundColor: '#f0f2f5' }}>
          <Text strong style={{ fontSize: '20px' }}>
            NET INCOME: {typeof netIncome === 'number' 
              ? netIncome.toLocaleString(undefined, { minimumFractionDigits: 2 })
              : '0.00'
            }
          </Text>
        </div>
      </div>
    );
  };

  const renderIncomeExpenseReport = () => {
    if (!reportData || !Array.isArray(reportData)) {
      return (
        <Alert
          message="Invalid Income Expense Data"
          description="The income expense data format is not as expected."
          type="warning"
          style={{ margin: '20px 0' }}
        />
      );
    }

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
            {reportData.map((row, index) => {
              if (!row || typeof row !== 'object') {
                return (
                  <tr key={index}>
                    <td colSpan={6} style={{ padding: '8px', border: '1px solid #d9d9d9', textAlign: 'center' }}>
                      <Text type="warning">Invalid row data</Text>
                    </td>
                  </tr>
                );
              }

              return (
                <tr key={index}>
                  <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>
                    {row.type || 'Unknown'}
                  </td>
                  <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>
                    {row.category || 'Unknown'}
                  </td>
                  <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>
                    {row.description || 'Unknown'}
                  </td>
                  <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                    {typeof row.currentMonth === 'number'
                      ? row.currentMonth.toLocaleString(undefined, { minimumFractionDigits: 2 })
                      : '0.00'
                    }
                  </td>
                  <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                    {typeof row.previousMonth === 'number'
                      ? row.previousMonth.toLocaleString(undefined, { minimumFractionDigits: 2 })
                      : '0.00'
                    }
                  </td>
                  <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                    {typeof row.ytd === 'number'
                      ? row.ytd.toLocaleString(undefined, { minimumFractionDigits: 2 })
                      : '0.00'
                    }
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  };

  const renderFinancialGroupingReport = () => {
    if (!reportData) return null;

    // Handle different possible data structures
    const byCategory = reportData.byCategory || reportData.categories || {};
    const byDepartment = reportData.byDepartment || reportData.departments || {};
    const summary = reportData.summary || {};

    const renderGrouping = (title, groupData) => {
      if (!groupData || typeof groupData !== 'object') {
        return (
          <div style={{ marginBottom: 24 }}>
            <Title level={4}>{title}</Title>
            <Alert message={`No data available for ${title}`} type="info" />
          </div>
        );
      }

      const entries = Object.entries(groupData);
      if (entries.length === 0) {
        return (
          <div style={{ marginBottom: 24 }}>
            <Title level={4}>{title}</Title>
            <Alert message={`No data found for ${title}`} type="info" />
          </div>
        );
      }

      return (
        <div style={{ marginBottom: 24 }}>
          <Title level={4}>{title}</Title>
          <table style={{ width: '100%', border: '1px solid #d9d9d9' }}>
            <thead>
              <tr style={{ backgroundColor: '#fafafa' }}>
                <th style={{ padding: '8px', textAlign: 'left', border: '1px solid #d9d9d9' }}>Name</th>
                <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Amount</th>
                <th style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>Count</th>
              </tr>
            </thead>
            <tbody>
              {entries.map(([name, data], index) => (
                <tr key={index}>
                  <td style={{ padding: '8px', border: '1px solid #d9d9d9' }}>{name}</td>
                  <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                    {typeof data?.amount === 'number'
                      ? data.amount.toLocaleString(undefined, { minimumFractionDigits: 2 })
                      : typeof data === 'number'
                      ? data.toLocaleString(undefined, { minimumFractionDigits: 2 })
                      : '0.00'
                    }
                  </td>
                  <td style={{ padding: '8px', textAlign: 'right', border: '1px solid #d9d9d9' }}>
                    {data?.count || data?.transactionCount || '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    };

    return (
      <div>
        <Title level={3}>
          Financial Grouping Report for {startDate.format('MMMM DD, YYYY')} to {endDate.format('MMMM DD, YYYY')}
        </Title>
        {renderGrouping('By Category', byCategory)}
        {renderGrouping('By Department', byDepartment)}
        
        {summary && Object.keys(summary).length > 0 && (
          <div style={{ marginTop: 24, padding: '16px', backgroundColor: '#f0f2f5' }}>
            <Title level={4}>Summary</Title>
            <Row gutter={16}>
              {Object.entries(summary).map(([key, value], index) => (
                <Col span={6} key={index}>
                  <Text strong>{key}:</Text>
                  <br />
                  <Text>{typeof value === 'number' ? value.toLocaleString() : value}</Text>
                </Col>
              ))}
            </Row>
          </div>
        )}
      </div>
    );
  };

  return (
    <Card
      title={
        <Space>
          <FundProjectionScreenOutlined />
          Financial Reports - Preview & Generate
        </Space>
      }
      style={{ margin: 24 }}
    >
      {/* Connection Status */}
      <Card 
        size="small" 
        style={{ marginBottom: 16, borderLeft: '4px solid #1890ff' }}
      >
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <InfoCircleOutlined />
              <Text strong>Backend Connection Status:</Text>
              {getConnectionStatusTag()}
            </Space>
          </Col>
          <Col>
            <Button 
              size="small" 
              onClick={testBackendConnection}
              loading={testingConnection}
            >
              Test Connection
            </Button>
          </Col>
        </Row>
        {connectionStatus === 'disconnected' && (
          <Alert
            style={{ marginTop: 8 }}
            message="Backend server appears to be offline"
            description="Please ensure the Spring Boot backend is running on localhost:8085"
            type="warning"
            showIcon
          />
        )}
      </Card>

      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        {/* Preview Tab */}
        <TabPane 
          tab={
            <Space>
              <EyeOutlined />
              Preview Reports
            </Space>
          } 
          key="preview"
        >
          <Alert
            message="Financial Report Preview"
            description="Preview all financial reports using current APIs. Supports Balance Sheet, Income Statement, Income vs Expense, and Financial Grouping reports."
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />

          {renderPreviewControls()}

          <div style={{ marginTop: 16 }}>
            {renderReportContent()}
          </div>
        </TabPane>

        {/* Generate Tab */}
        <TabPane 
          tab={
            <Space>
              <PlayCircleOutlined />
              Generate Reports (DDD)
            </Space>
          } 
          key="generate"
        >
          <Alert
            message="DDD-Based Report Generation"
            description="Generate reports using the Domain-Driven Design architecture. Reports will be processed asynchronously and can be monitored in Report Management."
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />

          <Form
            form={generateForm}
            layout="vertical"
            onFinish={handleGenerate}
            initialValues={{
              aiAnalysisEnabled: false,
              dateRange: [dayjs().subtract(1, 'month'), dayjs()]
            }}
          >
            <Row gutter={[16, 16]}>
              {/* Report Type Selection */}
              <Col xs={24} md={12}>
                <Form.Item
                  name="reportType"
                  label="Report Type"
                  rules={[{ required: true, message: 'Please select a report type' }]}
                >
                  <Select
                    placeholder="Select report type"
                    onChange={handleReportTypeChange}
                    size="large"
                  >
                    {reportTypes.map(type => (
                      <Option key={type.value} value={type.value}>
                        <Space>
                          {type.icon}
                          <div>
                            <div>{type.label}</div>
                            <Text type="secondary" style={{ fontSize: '12px' }}>
                              {type.description}
                            </Text>
                          </div>
                        </Space>
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>

              {/* Report Name */}
              <Col xs={24} md={12}>
                <Form.Item
                  name="reportName"
                  label="Report Name"
                  rules={[
                    { required: true, message: 'Please enter a report name' },
                    { min: 3, message: 'Report name must be at least 3 characters' }
                  ]}
                >
                  <Input 
                    placeholder="Enter report name"
                    size="large"
                    maxLength={100}
                  />
                </Form.Item>
              </Col>

              {/* Date Range */}
              <Col xs={24} md={12}>
                <Form.Item
                  name="dateRange"
                  label="Report Period"
                  rules={[{ required: true, message: 'Please select a date range' }]}
                >
                  <RangePicker
                    style={{ width: '100%' }}
                    size="large"
                    format="YYYY-MM-DD"
                    placeholder={['Start Date', 'End Date']}
                  />
                </Form.Item>
              </Col>

              {/* AI Analysis Toggle */}
              <Col xs={24} md={12}>
                <Form.Item
                  name="aiAnalysisEnabled"
                  label="AI Analysis"
                  valuePropName="checked"
                >
                  <div>
                    <Switch />
                    <Tooltip title="Enable AI-powered analysis of the financial report">
                      <InfoCircleOutlined style={{ marginLeft: 8, color: '#1890ff' }} />
                    </Tooltip>
                    <div style={{ marginTop: 4 }}>
                      <Text type="secondary" style={{ fontSize: '12px' }}>
                        Include intelligent insights and recommendations
                      </Text>
                    </div>
                  </div>
                </Form.Item>
              </Col>
            </Row>

            {/* Report Type Details */}
            {selectedReportType && (
              <Card
                size="small"
                style={{ 
                  marginTop: 16,
                  backgroundColor: '#f9f9f9',
                  border: '1px dashed #d9d9d9'
                }}
              >
                <Row align="top">
                  <Col span={2}>
                    {getReportTypeIcon(selectedReportType)}
                  </Col>
                  <Col span={22}>
                    <Title level={5} style={{ margin: 0 }}>
                      {getCurrentReportType()?.label}
                    </Title>
                    <Text style={{ margin: '4px 0 0 0', fontSize: '13px' }}>
                      {getCurrentReportType()?.description}
                    </Text>
                  </Col>
                </Row>
              </Card>
            )}

            <Divider />

            {/* Submit Button */}
            <Form.Item style={{ marginBottom: 0 }}>
              <Button
                type="primary"
                htmlType="submit"
                loading={generateLoading}
                disabled={connectionStatus === 'disconnected'}
                icon={<PlayCircleOutlined />}
                size="large"
                style={{ minWidth: 160 }}
              >
                {generateLoading ? 'Generating Report...' : 'Generate Report'}
              </Button>
              
              {connectionStatus === 'disconnected' && (
                <div style={{ marginTop: 8 }}>
                  <Text type="warning" style={{ fontSize: '12px' }}>
                    Please ensure backend connection before generating reports
                  </Text>
                </div>
              )}
            </Form.Item>
          </Form>
        </TabPane>
      </Tabs>
    </Card>
  );
};

export default FinancialReportsUnified;