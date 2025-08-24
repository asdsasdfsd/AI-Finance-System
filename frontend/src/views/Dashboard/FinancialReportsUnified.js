// frontend/src/views/Dashboard/FinancialReportsUnified.js
// FIXED FinancialReportsUnified Component with correct date ranges

import React, { useState, useEffect } from 'react';
import {
  Card, Button, Select, DatePicker, Row, Col, Table, Space, Alert, Modal, Form, Input,
  message, Spin, Divider, Typography, Tag, InputNumber, Switch
} from 'antd';
import {
  EyeOutlined, DownloadOutlined, SaveOutlined, FundProjectionScreenOutlined,
  ReloadOutlined, BarChartOutlined, DollarCircleOutlined, PieChartOutlined, FundOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import AuthService from '../../services/authService';
import ReportService from '../../services/reportService';
import UnifiedReportService from '../../services/unifiedReportService';
import { ReportPreview } from '../../components/ReportPreviewComponents';

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

const FinancialReportsUnified = () => {
  // State management
  const [reportType, setReportType] = useState('BALANCE_SHEET');
  // FIXED: Set default dates to match available data
  const [asOfDate, setAsOfDate] = useState(dayjs('2025-08-31')); // Month-end date with data
  const [startDate, setStartDate] = useState(dayjs('2025-07-01')); // Start of data range
  const [endDate, setEndDate] = useState(dayjs('2025-08-31')); // End of data range
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [saveModalVisible, setSaveModalVisible] = useState(false);
  const [originalResponse, setOriginalResponse] = useState(null);

  // Form and user context
  const [saveForm] = Form.useForm();
  const currentUser = AuthService.getCurrentUser();
  const [companyId, setCompanyId] = useState(currentUser?.companyId || 1);

  // ENHANCED: Report configurations with industry-standard formats
  const reportConfigs = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      useAsOfDate: true,
      description: 'Assets, Liabilities, and Equity as of a specific date',
      icon: '📊',
      dateHelper: 'Use month-end dates: 2025-07-31 or 2025-08-31'
    },
    {
      value: 'INCOME_STATEMENT', 
      label: 'Income Statement',
      useAsOfDate: false,
      description: 'Revenue and Expenses over a period',
      icon: '💰',
      dateHelper: 'Available data: July 1 - August 31, 2025'
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      useAsOfDate: true, // FIXED: Changed to true to match backend expectation
      description: 'Detailed income and expense analysis',
      icon: '📈',
      dateHelper: 'Use month-end dates: 2025-07-31 or 2025-08-31'
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Analysis',
      useAsOfDate: false,
      description: 'Grouped financial data by department, category, etc.',
      icon: '🔗',
      dateHelper: 'Available data: July 1 - August 31, 2025'
    }
  ];

  const currentReportConfig = reportConfigs.find(config => config.value === reportType);

  // Auto-load report when parameters change
  useEffect(() => {
    if (currentReportConfig) {
      loadReportData();
    }
  }, [reportType, companyId, asOfDate, startDate, endDate]);

  // ENHANCED: Generate report preview with proper data structure
const loadReportData = async () => {
  setLoading(true);
  setError(null);
  
  try {
    const config = currentReportConfig;
    
    // Check authentication
    if (!currentUser || !currentUser.token) {
      throw new Error('Authentication required. Please login first.');
    }

    // Build parameters based on report type
    const params = {
      companyId: companyId
    };

    if (config.useAsOfDate) {
      params.asOfDate = asOfDate.format('YYYY-MM-DD');
    } else {
      params.startDate = startDate.format('YYYY-MM-DD');
      params.endDate = endDate.format('YYYY-MM-DD');
    }

    console.log(`[FinancialReportsUnified] Loading ${reportType} with params:`, params);

    // FIXED: Use UnifiedReportService for consistent API handling
    const response = await UnifiedReportService.generateReportPreview(reportType, params);
    
    // 🔥 CRITICAL FIX: Store original response for Financial Grouping
    setOriginalResponse(response);
    
    // 🔥 CRITICAL FIX: Handle Financial Grouping differently
    let tableData;
    if (reportType === 'FINANCIAL_GROUPING') {
      // For Financial Grouping, don't convert to table data
      // The ReportPreview component needs original structure
      tableData = []; // Empty array to indicate we have data but handle it specially
    } else {
      // Convert backend data to table format for other reports
      tableData = UnifiedReportService.convertToTableData(reportType, response);
    }
    
    console.log(`[FinancialReportsUnified] Converted table data:`, tableData);
    console.log(`[FinancialReportsUnified] Original response:`, response);
    setReportData(tableData);
    
    // 🔥 CRITICAL FIX: Update success message logic for Financial Grouping
    if (reportType === 'FINANCIAL_GROUPING') {
      // Check if original response has data
      const hasData = response && (
        (response.categoryGrouping && response.categoryGrouping.length > 0) ||
        (response.departmentGrouping && response.departmentGrouping.length > 0) ||
        (response.transactionTypeGrouping && response.transactionTypeGrouping.length > 0) ||
        (response.monthlyTrend && response.monthlyTrend.length > 0)
      );
      
      if (!hasData) {
        message.warning(`No data found for ${config.label}. Try different dates:\n${config.dateHelper}`);
        setError(`No data available for the selected criteria.\n\n💡 Suggestion: ${config.dateHelper}`);
      } else {
        const totalItems = (response.categoryGrouping?.length || 0) + 
                          (response.departmentGrouping?.length || 0) + 
                          (response.transactionTypeGrouping?.length || 0) + 
                          (response.monthlyTrend?.length || 0);
        message.success(`${config.label} loaded successfully with ${totalItems} entries.`);
      }
    } else {
      // Handle other report types
      if (!tableData || tableData.length === 0) {
        message.warning(`No data found for ${config.label}. Try different dates:\n${config.dateHelper}`);
        setError(`No data available for the selected criteria.\n\n💡 Suggestion: ${config.dateHelper}`);
      } else {
        message.success(`${config.label} loaded successfully with ${tableData.length} entries.`);
      }
    }
    
  } catch (error) {
    console.error('[FinancialReportsUnified] Error loading report data:', error);
    const errorMessage = error.message || 'Failed to load report data';
    setError(errorMessage);
    
    // Provide specific guidance based on error type
    if (errorMessage.includes('404') || errorMessage.includes('not found')) {
      message.error(`${currentReportConfig.label} endpoint not available. Please check backend configuration.`);
    } else if (errorMessage.includes('401') || errorMessage.includes('Authentication')) {
      message.error('Please login again to access reports.');
    } else if (errorMessage.includes('connect') || errorMessage.includes('network')) {
      message.error('Cannot connect to backend server. Please check if it\'s running on port 8085.');
    } else {
      message.error(`Failed to load ${currentReportConfig.label}: ${errorMessage}`);
    }
  } finally {
    setLoading(false);
  }
};

  // ENHANCED: Export to Excel using unified backend generators
  const handleExport = async () => {
    setExporting(true);
    
    try {
      // 🔥 CRITICAL FIX: Update availability check for Financial Grouping
      if (reportType === 'FINANCIAL_GROUPING') {
        if (!originalResponse) {
          throw new Error('No report data to export. Please generate a preview first.');
        }
      } else {
        if (!reportData || reportData.length === 0) {
          throw new Error('No report data to export. Please generate a preview first.');
        }
      }

      const config = currentReportConfig;
      const params = {
        companyId: companyId
      };

      if (config.useAsOfDate) {
        params.asOfDate = asOfDate.format('YYYY-MM-DD');
      } else {
        params.startDate = startDate.format('YYYY-MM-DD');
        params.endDate = endDate.format('YYYY-MM-DD');
      }

      console.log(`[FinancialReportsUnified] Exporting ${reportType} with params:`, params);

      // FIXED: Use UnifiedReportService for export
      await UnifiedReportService.exportReport(reportType, params);
      message.success(`${config.label} exported successfully!`);
      
    } catch (error) {
      console.error('[FinancialReportsUnified] Export failed:', error);
      message.error(`Export failed: ${error.message}`);
    } finally {
      setExporting(false);
    }
  };

  const debugSaveButton = () => {
  const saveButton = document.querySelector('button:has(.anticon-save)');
  console.log('🔍 [DEBUG] Save Button Analysis:');
  console.log('Button element:', saveButton);
  console.log('Button disabled:', saveButton?.disabled);
  console.log('Button className:', saveButton?.className);
  console.log('reportData:', reportData);
  console.log('reportData length:', reportData?.length);
  console.log('Disable condition result:', !reportData || reportData.length === 0);
  
  return {
    element: saveButton,
    disabled: saveButton?.disabled,
    reportDataExists: !!reportData,
    reportDataLength: reportData?.length,
    shouldBeDisabled: !reportData || reportData.length === 0
  };
};

// 将调试函数暴露到window对象
window.debugSaveButton = debugSaveButton;

  // ENHANCED: Save report with unified backend generators
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
      
      console.log('[FinancialReportsUnified] Saving report with unified backend:', command);
      
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
      console.error('[FinancialReportsUnified] Save report failed:', error);
      message.error(`Failed to save report: ${error.message}`);
    } finally {
      setSaving(false);
    }
  };

  // Quick date shortcuts for available data
  const getDateShortcuts = () => {
    return [
      {
        text: 'July 2025',
        value: [dayjs('2025-07-01'), dayjs('2025-07-31')]
      },
      {
        text: 'August 2025',
        value: [dayjs('2025-08-01'), dayjs('2025-08-31')]
      },
      {
        text: 'Jul-Aug 2025',
        value: [dayjs('2025-07-01'), dayjs('2025-08-31')]
      },
      {
        text: 'Last Month (July)',
        value: [dayjs('2025-07-01'), dayjs('2025-07-31')]
      }
    ];
  };

  // Get table columns based on report type
  const getTableColumns = () => {
    const baseColumns = [
      {
        title: 'Account',
        dataIndex: 'Account',
        key: 'Account',
        width: 250,
        ellipsis: true
      },
      {
        title: 'Amount',
        dataIndex: 'Amount',
        key: 'Amount',
        align: 'right',
        width: 150,
        render: (value) => {
          const numValue = Number(value) || 0;
          return (
            <span style={{ 
              color: numValue >= 0 ? '#52c41a' : '#ff4d4f',
              fontWeight: 'bold'
            }}>
              ¥{numValue.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
            </span>
          );
        }
      }
    ];

    // Add type-specific columns
    switch (reportType) {
      case 'BALANCE_SHEET':
        return [
          ...baseColumns,
          {
            title: 'Type',
            dataIndex: 'Type',
            key: 'Type',
            width: 100,
            filters: [
              { text: 'Asset', value: 'Asset' },
              { text: 'Liability', value: 'Liability' },
              { text: 'Equity', value: 'Equity' }
            ],
            onFilter: (value, record) => record.Type === value,
            render: (type) => {
              const colors = {
                'Asset': 'green',
                'Liability': 'red', 
                'Equity': 'blue'
              };
              return <Tag color={colors[type] || 'default'}>{type}</Tag>;
            }
          }
        ];
        
      case 'INCOME_STATEMENT':
        return [
          ...baseColumns,
          {
            title: 'Category',
            dataIndex: 'Category',
            key: 'Category',
            width: 120,
            filters: [
              { text: 'Revenue', value: 'Revenue' },
              { text: 'Expense', value: 'Expense' }
            ],
            onFilter: (value, record) => record.Category === value,
            render: (category) => {
              const color = category === 'Revenue' ? 'green' : 'red';
              return <Tag color={color}>{category}</Tag>;
            }
          }
        ];
        
      case 'INCOME_EXPENSE':
        return [
          ...baseColumns,
          {
            title: 'Type',
            dataIndex: 'Type',
            key: 'Type',
            width: 100,
            render: (type) => {
              const color = type === 'INCOME' ? 'green' : 'red';
              return <Tag color={color}>{type}</Tag>;
            }
          },
          {
            title: 'Category',
            dataIndex: 'Category',
            key: 'Category',
            width: 150,
            ellipsis: true
          }
        ];
        
      case 'FINANCIAL_GROUPING':
        return [
          {
            title: 'Category',
            dataIndex: 'Category',
            key: 'Category',
            width: 200
          },
          {
            title: 'Amount',
            dataIndex: 'Amount',
            key: 'Amount',
            align: 'right',
            width: 120,
            render: (value) => `¥${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`
          },
          {
            title: 'Count',
            dataIndex: 'Count',
            key: 'Count',
            align: 'center',
            width: 80
          },
          {
            title: 'Percentage',
            dataIndex: 'Percentage',
            key: 'Percentage',
            align: 'center',
            width: 100
          }
        ];
        
      default:
        return baseColumns;
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      {/* Header Card */}
      <Card>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3}>
              <FundProjectionScreenOutlined /> Financial Reports (Unified)
            </Title>
            <Text type="secondary">
              Generate industry-standard financial reports with unified Excel formatting
            </Text>
          </Col>
          <Col>
            <Tag color="blue">Data Available: Jul-Aug 2025</Tag>
          </Col>
        </Row>
      </Card>

      {/* Controls Card */}
      <Card style={{ marginTop: 16 }} title="Report Configuration">
        <Row gutter={[16, 16]}>
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

          <Col span={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Company ID</Text>
              <InputNumber
                value={companyId}
                onChange={setCompanyId}
                style={{ width: '100%' }}
                size="large"
                min={1}
                max={3}
                placeholder="1, 2, or 3"
              />
              <Text type="secondary" style={{ fontSize: '12px' }}>
                Companies: 1=Tech Innovation, 2=Green Energy, 3=Finance Solutions
              </Text>
            </Space>
          </Col>

          <Col span={12}>
            {currentReportConfig?.useAsOfDate ? (
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text strong>As of Date</Text>
                <DatePicker
                  value={asOfDate}
                  onChange={setAsOfDate}
                  style={{ width: '100%' }}
                  size="large"
                  format="YYYY-MM-DD"
                  placeholder="Select as-of date"
                />
                <Text type="secondary" style={{ fontSize: '12px' }}>
                  💡 {currentReportConfig.dateHelper}
                </Text>
              </Space>
            ) : (
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text strong>Date Range</Text>
                <RangePicker
                  value={[startDate, endDate]}
                  onChange={([start, end]) => {
                    setStartDate(start);
                    setEndDate(end);
                  }}
                  style={{ width: '100%' }}
                  size="large"
                  format="YYYY-MM-DD"
                  presets={getDateShortcuts()}
                />
                <Text type="secondary" style={{ fontSize: '12px' }}>
                  💡 {currentReportConfig.dateHelper}
                </Text>
              </Space>
            )}
          </Col>
        </Row>

        <Divider />

        <Row gutter={16}>
          <Col>
            <Button 
              type="primary" 
              icon={<EyeOutlined />}
              size="large"
              loading={loading}
              onClick={loadReportData}
            >
              Generate Preview
            </Button>
          </Col>
          <Col>
            <Button
              icon={<DownloadOutlined />}
              size="large"
              loading={exporting}
              onClick={handleExport}
              disabled={
                (() => {
                  if (reportType === 'FINANCIAL_GROUPING') {
                    // FINANCIAL_GROUPING允许保存，即使转换后数组为空
                    return !reportData;
                  }
                  // 其他报表需要有数组数据
                  return !reportData || reportData.length === 0;
                })()
              }
            >
              Export to Excel
            </Button>
          </Col>
          <Col>
            <Button
              icon={<SaveOutlined />}
              size="large"
              loading={saving}
              onClick={handleShowSaveModal}
              disabled={(() => {
                  if (reportType === 'FINANCIAL_GROUPING') {
                    // FINANCIAL_GROUPING允许保存，即使转换后数组为空
                    return !reportData;
                  }
                  // 其他报表需要有数组数据
                  return !reportData || reportData.length === 0;
                })()}
            >
              Save Report
            </Button>
          </Col>
          <Col>
            <Button
              icon={<ReloadOutlined />}
              size="large"
              onClick={loadReportData}
              disabled={loading}
            >
              Refresh
            </Button>
          </Col>
        </Row>
      </Card>

      {/* Report Display Card */}
      <Card 
        style={{ marginTop: 16 }}
        title={`${currentReportConfig?.label || 'Report'} Preview`}
        extra={reportData && <Text type="secondary">{reportData.length} entries</Text>}
      >
        {loading && (
          <div style={{ textAlign: 'center', padding: '60px' }}>
            <Spin size="large" />
            <div style={{ marginTop: 16 }}>
              <Text>Loading {currentReportConfig?.label}...</Text>
            </div>
          </div>
        )}

        {error && (
          <Alert
            message="Error Loading Report"
            description={error}
            type="error"
            showIcon
            closable
            onClose={() => setError(null)}
            style={{ marginBottom: 16 }}
            action={
              <Button size="small" onClick={loadReportData}>
                Retry
              </Button>
            }
          />
        )}

        {!loading && !error && reportData && reportData.length > 0 && (
          <Table
            dataSource={reportData}
            columns={getTableColumns()}
            size="small"
            scroll={{ x: 800, y: 500 }}
            pagination={{
              pageSize: 50,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} entries`
            }}
            summary={(pageData) => {
              if (reportType === 'BALANCE_SHEET') {
                const assets = pageData.filter(item => item.Type === 'Asset');
                const liabilities = pageData.filter(item => item.Type === 'Liability');
                const equity = pageData.filter(item => item.Type === 'Equity');
                
                const totalAssets = assets.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
                const totalLiabilities = liabilities.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
                const totalEquity = equity.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
                
                return (
                  <>
                    <Table.Summary.Row>
                      <Table.Summary.Cell><strong>Total Assets</strong></Table.Summary.Cell>
                      <Table.Summary.Cell align="right">
                        <strong style={{ color: '#52c41a' }}>
                          ¥{totalAssets.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                        </strong>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell>
                        <Tag color="green">Assets</Tag>
                      </Table.Summary.Cell>
                    </Table.Summary.Row>
                    <Table.Summary.Row>
                      <Table.Summary.Cell><strong>Total Liabilities + Equity</strong></Table.Summary.Cell>
                      <Table.Summary.Cell align="right">
                        <strong style={{ color: '#1890ff' }}>
                          ¥{(totalLiabilities + totalEquity).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                        </strong>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell>
                        <Tag color={Math.abs(totalAssets - (totalLiabilities + totalEquity)) < 0.01 ? 'green' : 'red'}>
                          {Math.abs(totalAssets - (totalLiabilities + totalEquity)) < 0.01 ? 'Balanced' : 'Unbalanced'}
                        </Tag>
                      </Table.Summary.Cell>
                    </Table.Summary.Row>
                  </>
                );
              }
              
              return null;
            }}
          />
        )}

        {!loading && !error && (
          <>
            {/* 处理 FINANCIAL_GROUPING 的特殊情况 */}
            {reportType === 'FINANCIAL_GROUPING' ? (
              originalResponse ? (
                <ReportPreview reportType={reportType} data={originalResponse} />
              ) : (
                <div style={{ textAlign: 'center', padding: '60px' }}>
                  <Text type="secondary">
                    No data available for the selected criteria.
                    <br />
                    <strong>💡 {currentReportConfig?.dateHelper}</strong>
                  </Text>
                </div>
              )
            ) : (
              /* 处理其他报表类型的无数据情况 */
              (!reportData || reportData.length === 0) && (
                <div style={{ textAlign: 'center', padding: '60px' }}>
                  <Text type="secondary" style={{ fontSize: '16px' }}>
                    No data available for the selected criteria.
                  </Text>
                  <br />
                  <Text type="secondary">
                    💡 Try using the suggested dates: {currentReportConfig?.dateHelper}
                  </Text>
                </div>
              )
            )}
          </>
        )}
      </Card>

      {/* Save Report Modal */}
      <Modal
        title="Save Report"
        visible={saveModalVisible}
        onCancel={() => setSaveModalVisible(false)}
        footer={null}
      >
        <Form
          form={saveForm}
          layout="vertical"
          onFinish={handleSaveReport}
        >
          <Form.Item
            label="Report Name"
            name="reportName"
            rules={[{ required: true, message: 'Please enter report name' }]}
          >
            <Input placeholder="Enter report name" />
          </Form.Item>

          <Form.Item
            label="Report Type"
            name="reportType"
          >
            <Select disabled>
              <Option value={reportType}>{currentReportConfig?.label}</Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="AI Analysis"
            name="aiAnalysisEnabled"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={saving}>
                Save Report
              </Button>
              <Button onClick={() => setSaveModalVisible(false)}>
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default FinancialReportsUnified;