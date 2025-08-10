// frontend/src/views/Dashboard/FinancialReportsUnified.js
import React, { useState, useEffect } from 'react';
import {
  Card, Button, Select, DatePicker, Row, Col, Table, Space, Alert, Modal, Form, Input,
  message, Spin, Divider, Typography, Tag
} from 'antd';
import {
  EyeOutlined, DownloadOutlined, SaveOutlined, FundProjectionScreenOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import AuthService from '../../services/authService';
import ReportService from '../../services/reportService';
import { formatAIResult } from '../../services/aiService';

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

const FinancialReportsUnified = () => {
  // State management
  const [reportType, setReportType] = useState('BALANCE_SHEET');
  const [asOfDate, setAsOfDate] = useState(dayjs());
  const [startDate, setStartDate] = useState(dayjs().subtract(1, 'month'));
  const [endDate, setEndDate] = useState(dayjs());
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [saveModalVisible, setSaveModalVisible] = useState(false);

  // Form and user context
  const [saveForm] = Form.useForm();
  const currentUser = AuthService.getCurrentUser();
  const companyId = currentUser?.companyId || 1;

  // ENHANCED: Report configurations with industry-standard formats
  const reportConfigs = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      useAsOfDate: true,
      description: 'Assets, Liabilities, and Equity as of a specific date',
      icon: '📊'
    },
    {
      value: 'INCOME_STATEMENT', 
      label: 'Income Statement',
      useAsOfDate: false,
      description: 'Revenue and Expenses over a period',
      icon: '💰'
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      useAsOfDate: false,
      description: 'Detailed income and expense analysis',
      icon: '📈'
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Analysis',
      useAsOfDate: false,
      description: 'Grouped financial data by department, category, etc.',
      icon: '🔗'
    }
  ];

  const currentReportConfig = reportConfigs.find(config => config.value === reportType);

  // ENHANCED: Generate report preview with proper data structure
  const handlePreview = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const config = currentReportConfig;
      let apiEndpoint = '';
      let params = {
        companyId: companyId
      };

      // Set parameters based on report type
      if (config.useAsOfDate) {
        params.asOfDate = asOfDate.format('YYYY-MM-DD');
        apiEndpoint = getReportApiEndpoint(reportType, 'preview');
      } else {
        params.startDate = startDate.format('YYYY-MM-DD');
        params.endDate = endDate.format('YYYY-MM-DD');
        apiEndpoint = getReportApiEndpoint(reportType, 'preview');
      }

      console.log('Generating preview for:', reportType, 'with params:', params);

      // FIXED: Call actual backend API instead of mock data
      const response = await fetch(apiEndpoint + '?' + new URLSearchParams(params), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to generate ${config.label}: ${response.status} ${response.statusText}`);
      }

      const responseData = await response.json();
      
      // ENHANCED: Convert backend data to table format
      const tableData = convertBackendDataToTable(reportType, responseData);
      setReportData(tableData);
      
      message.success(`${config.label} preview generated successfully!`);
      
    } catch (error) {
      console.error('Preview generation failed:', error);
      setError(error.message);
      
      // FALLBACK: Generate demo data if API fails (for development)
      if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
        console.log('API unavailable, generating demo data...');
        const demoData = generateDemoData(currentReportConfig);
        setReportData(demoData);
        message.warning('Using demo data - backend API unavailable');
      } else {
        message.error(`Preview failed: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  // ENHANCED: Export to Excel using unified backend generators
  const handleExport = async () => {
    setExporting(true);
    
    try {
      if (!reportData || reportData.length === 0) {
        throw new Error('No report data to export. Please generate a preview first.');
      }

      const config = currentReportConfig;
      let exportEndpoint = getReportApiEndpoint(reportType, 'export');
      let params = { companyId: companyId };

      if (config.useAsOfDate) {
        params.asOfDate = asOfDate.format('YYYY-MM-DD');
      } else {
        params.startDate = startDate.format('YYYY-MM-DD');
        params.endDate = endDate.format('YYYY-MM-DD');
      }

      console.log('Exporting:', reportType, 'with params:', params);

      // FIXED: Use actual backend export endpoints
      const response = await fetch(exportEndpoint + '?' + new URLSearchParams(params), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Export failed: ${response.status} ${response.statusText}`);
      }

      // Handle Excel file download
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
      const dateStr = config.useAsOfDate 
        ? asOfDate.format('YYYY-MM-DD')
        : `${startDate.format('YYYY-MM-DD')}_to_${endDate.format('YYYY-MM-DD')}`;
      
      link.download = `${config.label.replace(/\s+/g, '_')}_${dateStr}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      message.success(`${config.label} exported successfully as Excel!`);
      
    } catch (error) {
      console.error('Export failed:', error);
      message.error(`Export failed: ${error.message}`);
    } finally {
      setExporting(false);
    }
  };

  // ENHANCED: Save report using unified backend with proper Excel format
  const handleShowSaveModal = () => {
    const config = currentReportConfig;
    const defaultName = config.useAsOfDate 
      ? `${config.label} - ${asOfDate.format('YYYY-MM-DD')}`
      : `${config.label} - ${startDate.format('YYYY-MM-DD')} to ${endDate.format('YYYY-MM-DD')}`;
    
    saveForm.setFieldsValue({
      reportName: defaultName,
      reportType: reportType,
      aiAnalysisEnabled: false
    });
    
    setSaveModalVisible(true);
  };

  // ENHANCED: Save report with unified backend generators
  const handleSaveReport = async (values) => {
    setSaving(true);
    
    try {
      const config = currentReportConfig;
      
      // FIXED: Use unified report generation command
      const command = {
        reportType: reportType,
        reportName: values.reportName,
        tenantId: companyId,
        createdBy: currentUser?.userId || 1,
        aiAnalysisEnabled: values.aiAnalysisEnabled || false
      };
      
      // Set date range based on report type
      if (config.useAsOfDate) {
        command.startDate = asOfDate.format('YYYY-MM-DD');
        command.endDate = asOfDate.format('YYYY-MM-DD');
      } else {
        command.startDate = startDate.format('YYYY-MM-DD');
        command.endDate = endDate.format('YYYY-MM-DD');
      }
      
      console.log('Saving report with unified backend:', command);
      
      // FIXED: Call unified report generation API
      const response = await ReportService.generateReport(command);
      
      if (response.status === 'success') {
        message.success(`Report "${values.reportName}" saved successfully! You can view it in Report Management.`);
        setSaveModalVisible(false);
        saveForm.resetFields();
      } else {
        throw new Error(response.message || 'Failed to save report');
      }
      
    } catch (error) {
      console.error('Save report failed:', error);
      message.error(`Failed to save report: ${error.message}`);
    } finally {
      setSaving(false);
    }
  };

  // Helper functions

  const getReportApiEndpoint = (reportType, operation) => {
    const baseEndpoints = {
      'BALANCE_SHEET': '/api/balance-sheet',
      'INCOME_STATEMENT': '/api/income-statement', 
      'INCOME_EXPENSE': '/api/income-expense',
      'FINANCIAL_GROUPING': '/api/financial-grouping'
    };
    
    const base = baseEndpoints[reportType] || '/api/reports';
    return operation === 'export' ? `${base}/export` : `${base}/generate`;
  };

  // ENHANCED: Convert backend response to table format
  const convertBackendDataToTable = (reportType, backendData) => {
    try {
      switch (reportType) {
        case 'BALANCE_SHEET':
          return convertBalanceSheetData(backendData);
        case 'INCOME_STATEMENT':
          return convertIncomeStatementData(backendData);
        case 'INCOME_EXPENSE':
          return convertIncomeExpenseData(backendData);
        case 'FINANCIAL_GROUPING':
          return convertFinancialGroupingData(backendData);
        default:
          return [];
      }
    } catch (error) {
      console.error('Data conversion failed:', error);
      return generateDemoData(currentReportConfig);
    }
  };

  const convertBalanceSheetData = (data) => {
    const result = [];
    
    // Assets section
    if (data.assets) {
      data.assets.forEach(asset => {
        result.push({
          key: `asset_${asset.accountId}`,
          Account: asset.accountName,
          Amount: asset.balance,
          Type: 'Asset'
        });
      });
    }
    
    // Liabilities section  
    if (data.liabilities) {
      data.liabilities.forEach(liability => {
        result.push({
          key: `liability_${liability.accountId}`,
          Account: liability.accountName,
          Amount: liability.balance,
          Type: 'Liability'
        });
      });
    }
    
    // Equity section
    if (data.equity) {
      data.equity.forEach(equity => {
        result.push({
          key: `equity_${equity.accountId}`,
          Account: equity.accountName,
          Amount: equity.balance,
          Type: 'Equity'
        });
      });
    }
    
    return result;
  };

  const convertIncomeStatementData = (data) => {
    const result = [];
    
    // Revenue section
    if (data.revenues) {
      data.revenues.forEach(revenue => {
        result.push({
          key: `revenue_${revenue.name}`,
          Account: revenue.name,
          Amount: revenue.amount,
          Category: 'Income'
        });
      });
    }
    
    // Expenses section
    if (data.expenses) {
      data.expenses.forEach(expense => {
        result.push({
          key: `expense_${expense.name}`,
          Account: expense.name,
          Amount: expense.amount,
          Category: 'Expense'
        });
      });
    }
    
    // Net income
    if (data.netProfit !== undefined) {
      result.push({
        key: 'net_income',
        Account: 'Net Income',
        Amount: data.netProfit,
        Category: 'Net'
      });
    }
    
    return result;
  };

  const convertIncomeExpenseData = (data) => {
    if (Array.isArray(data)) {
      return data.map((item, index) => ({
        key: `ie_${index}`,
        Description: item.description || item.Description,
        Amount: item.amount || item.Amount,
        Type: item.type || item.Type,
        Date: item.transactionDate || item.Date,
        Category: item.category || item.Category
      }));
    }
    return [];
  };

  const convertFinancialGroupingData = (data) => {
    const result = [];
    
    if (data.departmentGroups) {
      data.departmentGroups.forEach(group => {
        result.push({
          key: `dept_${group.departmentId}`,
          Department: group.departmentName,
          Total_Amount: group.totalAmount,
          Transaction_Count: group.transactionCount
        });
      });
    }
    
    return result;
  };

  // ENHANCED: Industry-standard demo data matching Excel format
  const generateDemoData = (config) => {
    const dateStr = config.useAsOfDate ? 
      asOfDate.format('YYYY-MM-DD') : 
      `${startDate.format('YYYY-MM-DD')} to ${endDate.format('YYYY-MM-DD')}`;
    
    switch (config.value) {
      case 'BALANCE_SHEET':
        return [
          { key: '1', Account: 'Cash and Cash Equivalents', Amount: 152000, Type: 'Current Asset' },
          { key: '2', Account: 'Accounts Receivable', Amount: 89000, Type: 'Current Asset' },
          { key: '3', Account: 'Inventory', Amount: 45000, Type: 'Current Asset' },
          { key: '4', Account: 'Property, Plant & Equipment', Amount: 285000, Type: 'Fixed Asset' },
          { key: '5', Account: 'Accounts Payable', Amount: 65000, Type: 'Current Liability' },
          { key: '6', Account: 'Long-term Debt', Amount: 120000, Type: 'Long-term Liability' },
          { key: '7', Account: 'Common Stock', Amount: 200000, Type: 'Equity' },
          { key: '8', Account: 'Retained Earnings', Amount: 186000, Type: 'Equity' }
        ];
      case 'INCOME_STATEMENT':
        return [
          { key: '1', Account: 'Sales Revenue', Amount: 485000, Category: 'Revenue' },
          { key: '2', Account: 'Service Revenue', Amount: 125000, Category: 'Revenue' },
          { key: '3', Account: 'Cost of Goods Sold', Amount: 195000, Category: 'Cost of Sales' },
          { key: '4', Account: 'Salaries and Wages', Amount: 158000, Category: 'Operating Expense' },
          { key: '5', Account: 'Rent Expense', Amount: 48000, Category: 'Operating Expense' },
          { key: '6', Account: 'Utilities', Amount: 12000, Category: 'Operating Expense' },
          { key: '7', Account: 'Net Income', Amount: 197000, Category: 'Net Income' }
        ];
      case 'INCOME_EXPENSE':
        return [
          { key: '1', Description: 'Product Sales Q1', Amount: 275000, Type: 'INCOME', Date: '2024-03-31', Category: 'Sales' },
          { key: '2', Description: 'Consulting Services', Amount: 85000, Type: 'INCOME', Date: '2024-03-25', Category: 'Services' },
          { key: '3', Description: 'Office Rent Payment', Amount: 18000, Type: 'EXPENSE', Date: '2024-03-01', Category: 'Facilities' },
          { key: '4', Description: 'Marketing Campaign', Amount: 25000, Type: 'EXPENSE', Date: '2024-03-15', Category: 'Marketing' },
          { key: '5', Description: 'Software Licenses', Amount: 8500, Type: 'EXPENSE', Date: '2024-03-10', Category: 'Technology' }
        ];
      case 'FINANCIAL_GROUPING':
        return [
          { key: '1', Department: 'Sales & Marketing', Total_Amount: 485000, Transaction_Count: 28 },
          { key: '2', Department: 'Operations', Total_Amount: 165000, Transaction_Count: 15 },
          { key: '3', Department: 'Technology', Total_Amount: 125000, Transaction_Count: 22 },
          { key: '4', Department: 'Finance & Admin', Total_Amount: 95000, Transaction_Count: 18 },
          { key: '5', Department: 'Human Resources', Total_Amount: 75000, Transaction_Count: 12 }
        ];
      default:
        return [];
    }
  };

  // Reset data when report type changes
  useEffect(() => {
    setReportData(null);
    setError(null);
  }, [reportType]);

  // ENHANCED: Render report table with industry-standard formatting
  const renderReportTable = () => {
    if (!reportData || !Array.isArray(reportData) || reportData.length === 0) {
      return (
        <Alert 
          message="No data available" 
          description="Generate a preview to see report data."
          type="info" 
          showIcon 
        />
      );
    }

    // Dynamic columns based on report type
    const getColumns = () => {
      const baseColumns = Object.keys(reportData[0])
        .filter(key => key !== 'key')
        .map(key => ({
          title: key.replace(/_/g, ' '),
          dataIndex: key,
          key: key,
          render: (text, record) => {
            // Format currency amounts
            if (key === 'Amount' || key === 'Total_Amount') {
              const amount = parseFloat(text) || 0;
              return (
                <span style={{ 
                  fontFamily: 'monospace',
                  color: amount < 0 ? '#ff4d4f' : '#000000',
                  fontWeight: Math.abs(amount) > 100000 ? 'bold' : 'normal'
                }}>
                  {new Intl.NumberFormat('en-US', {
                    style: 'currency',
                    currency: 'USD'
                  }).format(amount)}
                </span>
              );
            }
            
            // Format transaction counts
            if (key === 'Transaction_Count') {
              return <Tag color="blue">{text}</Tag>;
            }
            
            // Format types/categories with color coding
            if (key === 'Type' || key === 'Category') {
              const colors = {
                'Asset': 'green',
                'Current Asset': 'green',
                'Fixed Asset': 'cyan',
                'Liability': 'orange', 
                'Current Liability': 'orange',
                'Long-term Liability': 'red',
                'Equity': 'blue',
                'Income': 'green',
                'Revenue': 'green',
                'Expense': 'red',
                'Operating Expense': 'red',
                'Cost of Sales': 'magenta',
                'Net': 'purple',
                'Net Income': 'purple',
                'INCOME': 'green',
                'EXPENSE': 'red'
              };
              return <Tag color={colors[text] || 'default'}>{text}</Tag>;
            }
            
            return text;
          }
        }));

      return baseColumns;
    };

    return (
      <Table
        dataSource={reportData}
        columns={getColumns()}
        pagination={{
          pageSize: 50,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) => 
            `${range[0]}-${range[1]} of ${total} items`
        }}
        size="small"
        bordered
        scroll={{ x: 800 }}
        summary={(pageData) => {
          // Calculate totals for financial reports
          if (reportType === 'BALANCE_SHEET') {
            const assets = pageData.filter(item => item.Type?.includes('Asset'));
            const liabilities = pageData.filter(item => item.Type?.includes('Liability'));
            const equity = pageData.filter(item => item.Type?.includes('Equity'));
            
            const totalAssets = assets.reduce((sum, item) => sum + (parseFloat(item.Amount) || 0), 0);
            const totalLiabilities = liabilities.reduce((sum, item) => sum + (parseFloat(item.Amount) || 0), 0);
            const totalEquity = equity.reduce((sum, item) => sum + (parseFloat(item.Amount) || 0), 0);
            
            return (
              <>
                <Table.Summary.Row style={{ backgroundColor: '#f0f2f5', fontWeight: 'bold' }}>
                  <Table.Summary.Cell>Total Assets</Table.Summary.Cell>
                  <Table.Summary.Cell>
                    <Text strong style={{ color: '#1890ff' }}>
                      {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(totalAssets)}
                    </Text>
                  </Table.Summary.Cell>
                  <Table.Summary.Cell>Assets</Table.Summary.Cell>
                </Table.Summary.Row>
                <Table.Summary.Row style={{ backgroundColor: '#fff2e8', fontWeight: 'bold' }}>
                  <Table.Summary.Cell>Total Liabilities + Equity</Table.Summary.Cell>
                  <Table.Summary.Cell>
                    <Text strong style={{ color: totalAssets === (totalLiabilities + totalEquity) ? '#52c41a' : '#ff4d4f' }}>
                      {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(totalLiabilities + totalEquity)}
                    </Text>
                  </Table.Summary.Cell>
                  <Table.Summary.Cell>
                    {totalAssets === (totalLiabilities + totalEquity) ? 
                      <Tag color="green">Balanced ✓</Tag> : 
                      <Tag color="red">Unbalanced ⚠️</Tag>
                    }
                  </Table.Summary.Cell>
                </Table.Summary.Row>
              </>
            );
          }
          
          if (reportType === 'INCOME_STATEMENT') {
            const revenues = pageData.filter(item => item.Category?.includes('Revenue') || item.Category?.includes('Income'));
            const expenses = pageData.filter(item => item.Category?.includes('Expense') || item.Category?.includes('Cost'));
            
            const totalRevenue = revenues.reduce((sum, item) => sum + (parseFloat(item.Amount) || 0), 0);
            const totalExpenses = expenses.reduce((sum, item) => sum + (parseFloat(item.Amount) || 0), 0);
            const netIncome = totalRevenue - totalExpenses;
            
            return (
              <Table.Summary.Row style={{ backgroundColor: '#f6ffed', fontWeight: 'bold' }}>
                <Table.Summary.Cell>Net Income</Table.Summary.Cell>
                <Table.Summary.Cell>
                  <Text strong style={{ color: netIncome >= 0 ? '#52c41a' : '#ff4d4f' }}>
                    {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(netIncome)}
                  </Text>
                </Table.Summary.Cell>
                <Table.Summary.Cell>
                  {netIncome >= 0 ? <Tag color="green">Profit</Tag> : <Tag color="red">Loss</Tag>}
                </Table.Summary.Cell>
              </Table.Summary.Row>
            );
          }
          
          return null;
        }}
      />
    );
  };

  return (
    <div>
      {/* Header Card */}
      <Card>
        <Title level={3}>
          <FundProjectionScreenOutlined /> Financial Reports (Unified)
        </Title>
        <Text type="secondary">
          Generate industry-standard financial reports with unified Excel formatting
        </Text>
      </Card>

      {/* Controls Card */}
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
                      <span>{config.icon}</span>
                      <span>{config.label}</span>
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
              <Text strong>Company</Text>
              <Input 
                value={`Company ${companyId}`} 
                disabled 
                size="large"
              />
            </Space>
          </Col>

          <Col span={8}>
            {currentReportConfig?.useAsOfDate ? (
              <>
                <Title level={5}>As of Date</Title>
                <DatePicker 
                  value={asOfDate} 
                  onChange={setAsOfDate}
                  style={{ width: '100%' }}
                  format="YYYY-MM-DD"
                  size="large"
                />
              </>
            ) : (
              <>
                <Title level={5}>Date Range</Title>
                <RangePicker 
                  value={[startDate, endDate]} 
                  onChange={([start, end]) => {
                    setStartDate(start);
                    setEndDate(end);
                  }}
                  style={{ width: '100%' }}
                  format="YYYY-MM-DD"
                  size="large"
                />
              </>
            )}
          </Col>
        </Row>

        {/* Action Buttons */}
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col span={24}>
            <Space size="middle">
              <Button 
                type="primary" 
                icon={<EyeOutlined />}
                onClick={handlePreview}
                loading={loading}
                size="large"
              >
                Generate Preview
              </Button>
              
              <Button 
                icon={<DownloadOutlined />}
                onClick={handleExport}
                loading={exporting}
                disabled={!reportData}
                size="large"
                type="default"
              >
                Export to Excel
              </Button>
              
              <Button 
                icon={<SaveOutlined />}
                onClick={handleShowSaveModal}
                disabled={!reportData}
                size="large"
                type="dashed"
              >
                Save Report
              </Button>
            </Space>
          </Col>
        </Row>

        <Divider />

        {/* Report Preview */}
        <div style={{ marginTop: 24 }}>
          <Title level={4}>
            {currentReportConfig?.icon} {currentReportConfig?.label} Preview
          </Title>
          
          {loading && (
            <div style={{ textAlign: 'center', padding: 48 }}>
              <Spin size="large" />
              <div style={{ marginTop: 16 }}>Generating preview...</div>
            </div>
          )}
          
          {error && (
            <Alert 
              message="Preview Error" 
              description={error}
              type="error" 
              showIcon 
              closable
              onClose={() => setError(null)}
              style={{ marginBottom: 16 }}
            />
          )}
          
          {!loading && !error && renderReportTable()}
        </div>
      </Card>

      {/* Save Report Modal */}
      <Modal
        title={
          <Space>
            <SaveOutlined />
            <span>Save Report to Management</span>
          </Space>
        }
        open={saveModalVisible}
        onCancel={() => setSaveModalVisible(false)}
        onOk={() => saveForm.submit()}
        confirmLoading={saving}
        width={600}
      >
        <Form
          form={saveForm}
          layout="vertical"
          onFinish={handleSaveReport}
        >
          <Form.Item
            name="reportName"
            label="Report Name"
            rules={[{ required: true, message: 'Please enter report name' }]}
          >
            <Input placeholder="Enter a descriptive name for this report" />
          </Form.Item>
          
          <Form.Item
            name="reportType"
            label="Report Type"
          >
            <Select disabled>
              <Option value={reportType}>
                {currentReportConfig?.icon} {currentReportConfig?.label}
              </Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name="aiAnalysisEnabled"
            label="Enable AI Analysis"
            valuePropName="checked"
          >
            <input type="checkbox" />
            <span style={{ marginLeft: 8 }}>
              Generate AI insights for this report (experimental)
            </span>
          </Form.Item>
          
          <Alert
            message="Unified Format"
            description={`This will save the ${currentReportConfig?.label} using the unified Excel format with industry-standard styling. The saved report will match the export format exactly.`}
            type="info"
            showIcon
            style={{ marginTop: 16 }}
          />
        </Form>
      </Modal>
    </div>
  );
};

export default FinancialReportsUnified;