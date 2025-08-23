// frontend/src/views/Dashboard/FinancialReports.js
// FIXED Financial Reports Component - Uses unified report service

import React, { useState, useEffect } from 'react';
import {
  Card, Select, Button, Space, message, Typography, DatePicker,
  Row, Col, Spin, Alert, InputNumber, Table, Tabs, Statistic
} from 'antd';
import {
  FundProjectionScreenOutlined, DownloadOutlined, ReloadOutlined,
  BarChartOutlined, DollarCircleOutlined, PieChartOutlined, FundOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import UnifiedReportService from '../../services/unifiedReportService';
import AuthService from '../../services/authService';
import { ReportPreview } from '../../components/ReportPreviewComponents';

const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;


/**
 * FIXED Financial Reports Component
 * 
 * Now uses UnifiedReportService for consistent API handling
 * All report types work with proper error handling and data conversion
 */
const FinancialReports = () => {
  const [reportType, setReportType] = useState('BALANCE_SHEET');
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState(null);
  const [originalResponse, setOriginalResponse] = useState(null);
  
  // FIXED: Set default dates to match available data
  const [companyId, setCompanyId] = useState(1);
  const [asOfDate, setAsOfDate] = useState(dayjs('2025-08-31')); // Month-end date with data
  
  // Income Statement specific states  
  const [startDate, setStartDate] = useState(dayjs('2025-07-01')); // Start of data range
  const [endDate, setEndDate] = useState(dayjs('2025-08-31')); // End of data range

  const reportConfigs = [
    {
      value: 'BALANCE_SHEET',
      label: 'Balance Sheet',
      icon: <BarChartOutlined />,
      description: 'Assets, Liabilities, and Equity at a specific date',
      useAsOfDate: true,
      canPreview: true
    },
    {
      value: 'INCOME_STATEMENT',
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and expenses over a period',
      useAsOfDate: false,
      canPreview: true
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Income and expense analysis over a period',
      useAsOfDate: true, // FIXED: Changed to match backend expectation
      canPreview: true
    },
    {
      value: 'FINANCIAL_GROUPING',
      label: 'Financial Grouping Report',
      icon: <FundOutlined />,
      description: 'Transactions grouped by various criteria',
      useAsOfDate: false,
      canPreview: true
    }
  ];

  const currentReportConfig = reportConfigs.find(config => config.value === reportType);

  useEffect(() => {
    // Auto-load report when component mounts or key parameters change
    if (currentReportConfig?.canPreview) {
      loadReportData();
    }
  }, [reportType, companyId, asOfDate, startDate, endDate]);

  /**
   * FIXED: Load report data using unified service
   */
  const loadReportData = async () => {
  setLoading(true);
  setError(null);
  
  try {
    // Check authentication
    const authData = AuthService.getCurrentUser();
    if (!authData || !authData.token) {
      throw new Error('Authentication required. Please login first.');
    }

    console.log(`[FinancialReports] Loading ${reportType} data...`);

    // Build parameters based on report configuration
    const params = {
      companyId: companyId
    };

    if (currentReportConfig.useAsOfDate) {
      params.asOfDate = asOfDate.format('YYYY-MM-DD');
    } else {
      params.startDate = startDate.format('YYYY-MM-DD');
      params.endDate = endDate.format('YYYY-MM-DD');
    }

    console.log(`[FinancialReports] Request params:`, params);

    // Use unified report service
    const response = await UnifiedReportService.generateReportPreview(reportType, params);
    
    // CRITICAL FIX: Store original response for Financial Grouping
    setOriginalResponse(response);
    
    // For FINANCIAL_GROUPING, we don't convert to table data since the component needs original structure
    let tableData;
    if (reportType === 'FINANCIAL_GROUPING') {
      // For Financial Grouping, we'll use the ReportPreview component which expects original data
      tableData = []; // Empty array to indicate we have data but it's handled specially
    } else {
      // Convert backend data to table format for other report types
      tableData = UnifiedReportService.convertToTableData(reportType, response);
    }
    
    console.log(`[FinancialReports] Converted table data:`, tableData);
    console.log(`[FinancialReports] Original response:`, response);
    setReportData(tableData);
    
    // Update success message logic
    if (reportType === 'FINANCIAL_GROUPING') {
      // For financial grouping, check if original response has data
      const hasData = response && (
        (response.categoryGrouping && response.categoryGrouping.length > 0) ||
        (response.departmentGrouping && response.departmentGrouping.length > 0) ||
        (response.transactionTypeGrouping && response.transactionTypeGrouping.length > 0) ||
        (response.monthlyTrend && response.monthlyTrend.length > 0)
      );
      
      if (!hasData) {
        message.warning('No data found for the selected criteria. Try adjusting the date range or company.');
      } else {
        const totalItems = (response.categoryGrouping?.length || 0) + 
                          (response.departmentGrouping?.length || 0) + 
                          (response.transactionTypeGrouping?.length || 0) + 
                          (response.monthlyTrend?.length || 0);
        message.success(`${currentReportConfig.label} loaded successfully with ${totalItems} entries.`);
      }
    } else {
      if (tableData.length === 0) {
        message.warning('No data found for the selected criteria. Try adjusting the date range or company.');
      } else {
        message.success(`${currentReportConfig.label} loaded successfully with ${tableData.length} entries.`);
      }
    }
    
  } catch (error) {
    console.error('[FinancialReports] Error loading report data:', error);
    setError(error.message);
    message.error(`Failed to load ${currentReportConfig.label}: ${error.message}`);
  } finally {
    setLoading(false);
  }
};

  /**
   * FIXED: Export report using unified service
   */
  const handleExport = async () => {
    setExporting(true);
    
    try {
      // Check authentication
      const authData = AuthService.getCurrentUser();
      if (!authData || !authData.token) {
        throw new Error('Authentication required. Please login first.');
      }

      console.log(`[FinancialReports] Exporting ${reportType}...`);

      // Build parameters
      const params = {
        companyId: companyId
      };

      if (currentReportConfig.useAsOfDate) {
        params.asOfDate = asOfDate.format('YYYY-MM-DD');
      } else {
        params.startDate = startDate.format('YYYY-MM-DD');
        params.endDate = endDate.format('YYYY-MM-DD');
      }

      // Use unified report service for export
      await UnifiedReportService.exportReport(reportType, params);
      message.success(`${currentReportConfig.label} exported successfully!`);
      
    } catch (error) {
      console.error('[FinancialReports] Export failed:', error);
      message.error(`Export failed: ${error.message}`);
    } finally {
      setExporting(false);
    }
  };

  /**
   * Get table columns based on report type
   */
  const getTableColumns = () => {
    const baseColumns = [
      {
        title: 'Account',
        dataIndex: 'Account',
        key: 'Account',
        width: 300,
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

    // Add additional columns based on report type
    switch (reportType) {
      case 'BALANCE_SHEET':
        return [
          ...baseColumns,
          {
            title: 'Type',
            dataIndex: 'Type',
            key: 'Type',
            width: 120,
            filters: [
              { text: 'Asset', value: 'Asset' },
              { text: 'Liability', value: 'Liability' },
              { text: 'Equity', value: 'Equity' }
            ],
            onFilter: (value, record) => record.Type === value,
            render: (type) => (
              <span className={`report-type report-type-${type?.toLowerCase()}`}>
                {type}
              </span>
            )
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
            render: (category) => (
              <span className={`report-category report-category-${category?.toLowerCase()}`}>
                {category}
              </span>
            )
          }
        ];
        
      case 'INCOME_EXPENSE':
        return [
          ...baseColumns,
          {
            title: 'Type',
            dataIndex: 'Type',
            key: 'Type',
            width: 100
          },
          {
            title: 'Category',
            dataIndex: 'Category',
            key: 'Category',
            width: 150
          }
        ];
        
      case 'FINANCIAL_GROUPING':
        return [
          {
            title: 'Category',
            dataIndex: 'Category',
            key: 'Category',
            width: 250
          },
          {
            title: 'Amount',
            dataIndex: 'Amount',
            key: 'Amount',
            align: 'right',
            width: 150,
            render: (value) => `¥${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`
          },
          {
            title: 'Count',
            dataIndex: 'Count',
            key: 'Count',
            align: 'center',
            width: 100
          },
          {
            title: 'Percentage',
            dataIndex: 'Percentage',
            key: 'Percentage',
            align: 'center',
            width: 120
          }
        ];
        
      default:
        return baseColumns;
    }
  };

  /**
   * Calculate summary statistics
   */
  const getSummaryStats = () => {
    if (!reportData || reportData.length === 0) return null;

    switch (reportType) {
      case 'BALANCE_SHEET':
        const assets = reportData.filter(item => item.Type === 'Asset');
        const liabilities = reportData.filter(item => item.Type === 'Liability');
        const equity = reportData.filter(item => item.Type === 'Equity');
        
        const totalAssets = assets.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
        const totalLiabilities = liabilities.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
        const totalEquity = equity.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
        
        return (
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={8}>
              <Statistic 
                title="Total Assets" 
                value={totalAssets} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: '#3f8600' }}
              />
            </Col>
            <Col span={8}>
              <Statistic 
                title="Total Liabilities" 
                value={totalLiabilities} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: '#cf1322' }}
              />
            </Col>
            <Col span={8}>
              <Statistic 
                title="Total Equity" 
                value={totalEquity} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: '#1890ff' }}
              />
            </Col>
          </Row>
        );

      case 'INCOME_STATEMENT':
        const revenues = reportData.filter(item => item.Category === 'Revenue');
        const expenses = reportData.filter(item => item.Category === 'Expense');
        
        const totalRevenue = revenues.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
        const totalExpenses = expenses.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0);
        const netIncome = totalRevenue - totalExpenses;
        
        return (
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={8}>
              <Statistic 
                title="Total Revenue" 
                value={totalRevenue} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: '#3f8600' }}
              />
            </Col>
            <Col span={8}>
              <Statistic 
                title="Total Expenses" 
                value={totalExpenses} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: '#cf1322' }}
              />
            </Col>
            <Col span={8}>
              <Statistic 
                title="Net Income" 
                value={netIncome} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: netIncome >= 0 ? '#3f8600' : '#cf1322' }}
              />
            </Col>
          </Row>
        );

      default:
        return (
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={12}>
              <Statistic 
                title="Total Entries" 
                value={reportData.length} 
                valueStyle={{ color: '#1890ff' }}
              />
            </Col>
            <Col span={12}>
              <Statistic 
                title="Total Amount" 
                value={reportData.reduce((sum, item) => sum + (Number(item.Amount) || 0), 0)} 
                prefix="¥" 
                precision={2}
                valueStyle={{ color: '#3f8600' }}
              />
            </Col>
          </Row>
        );
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Row gutter={24}>
          <Col span={24}>
            <Title level={3}>
              <FundProjectionScreenOutlined style={{ marginRight: 8 }} />
              Financial Reports
            </Title>
            <Text type="secondary">
              Generate and export comprehensive financial reports for analysis and compliance.
            </Text>
          </Col>
        </Row>

        <Row gutter={24} style={{ marginTop: 24 }}>
          {/* Report Configuration Panel */}
          <Col span={8}>
            <Card title="Report Configuration" size="small">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text strong>Report Type</Text>
                  <Select
                    style={{ width: '100%', marginTop: 8 }}
                    value={reportType}
                    onChange={setReportType}
                    placeholder="Select report type"
                  >
                    {reportConfigs.map(config => (
                      <Option key={config.value} value={config.value}>
                        {config.icon} {config.label}
                      </Option>
                    ))}
                  </Select>
                  {currentReportConfig && (
                    <Text type="secondary" style={{ fontSize: '12px', display: 'block', marginTop: 4 }}>
                      {currentReportConfig.description}
                    </Text>
                  )}
                </div>

                <div>
                  <Text strong>Company ID</Text>
                  <InputNumber
                    style={{ width: '100%', marginTop: 8 }}
                    value={companyId}
                    onChange={setCompanyId}
                    min={1}
                    placeholder="Enter company ID"
                  />
                </div>

                {currentReportConfig?.useAsOfDate ? (
                  <div>
                    <Text strong>As of Date</Text>
                    <DatePicker
                      style={{ width: '100%', marginTop: 8 }}
                      value={asOfDate}
                      onChange={setAsOfDate}
                      format="YYYY-MM-DD"
                    />
                  </div>
                ) : (
                  <>
                    <div>
                      <Text strong>Start Date</Text>
                      <DatePicker
                        style={{ width: '100%', marginTop: 8 }}
                        value={startDate}
                        onChange={setStartDate}
                        format="YYYY-MM-DD"
                      />
                    </div>
                    <div>
                      <Text strong>End Date</Text>
                      <DatePicker
                        style={{ width: '100%', marginTop: 8 }}
                        value={endDate}
                        onChange={setEndDate}
                        format="YYYY-MM-DD"
                      />
                    </div>
                  </>
                )}

                <Space style={{ width: '100%', marginTop: 16 }}>
                  <Button 
                    type="primary" 
                    icon={<ReloadOutlined />}
                    loading={loading}
                    onClick={loadReportData}
                    block
                  >
                    Generate Preview
                  </Button>
                </Space>

                <Button
                  icon={<DownloadOutlined />}
                  loading={exporting}
                  onClick={handleExport}
                  disabled={!reportData || reportData.length === 0}
                  block
                >
                  Export to Excel
                </Button>
              </Space>
            </Card>
          </Col>

          {/* Report Display Panel */}
          <Col span={16}>
            <Card 
              title={`${currentReportConfig?.label || 'Report'} Preview`}
              size="small"
              extra={
                reportData && (
                  <Text type="secondary">
                    {reportData.length} entries
                  </Text>
                )
              }
            >
              {loading && (
                <div style={{ textAlign: 'center', padding: '40px' }}>
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
                />
              )}

              {!loading && !error && (
  <>
    {reportType === 'FINANCIAL_GROUPING' ? (
      // CRITICAL FIX: Use original response data for Financial Grouping Preview
      originalResponse ? (
        <ReportPreview reportType={reportType} data={originalResponse} />
      ) : (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <Text type="secondary">
            No data available for the selected criteria.
            <br />
            <strong>💡 Try these dates with available data:</strong>
            <br />
            • Date Range: 2025-07-01 to 2025-08-31
            <br />
            • Companies: 1 (Tech Innovation), 2 (Green Energy), 3 (Finance Solutions)
          </Text>
        </div>
      )
    ) : (
      // Use regular table display for other report types
      reportData && reportData.length > 0 ? (
        <>
          {getSummaryStats()}
          <Table
            dataSource={reportData}
            columns={getTableColumns()}
            size="small"
            scroll={{ x: 800, y: 400 }}
            pagination={{
              pageSize: 20,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total, range) => 
                `${range[0]}-${range[1]} of ${total} entries`
            }}
          />
        </>
      ) : (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <Text type="secondary">
            No data available for the selected criteria.
            <br />
            <strong>💡 Try these dates with available data:</strong>
            <br />
            • Balance Sheet: 2025-07-31 or 2025-08-31
            <br />
            • Income Statement: 2025-07-01 to 2025-08-31
            <br />
            • Companies: 1 (Tech Innovation), 2 (Green Energy), 3 (Finance Solutions)
          </Text>
        </div>
      )
    )}
  </>
)}
            </Card>
          </Col>
        </Row>
      </Card>

      <style jsx>{`
        .report-type-asset { color: #52c41a; background: #f6ffed; padding: 2px 8px; border-radius: 4px; }
        .report-type-liability { color: #ff7875; background: #fff2f0; padding: 2px 8px; border-radius: 4px; }
        .report-type-equity { color: #1890ff; background: #e6f7ff; padding: 2px 8px; border-radius: 4px; }
        .report-category-revenue { color: #52c41a; background: #f6ffed; padding: 2px 8px; border-radius: 4px; }
        .report-category-expense { color: #ff7875; background: #fff2f0; padding: 2px 8px; border-radius: 4px; }
      `}</style>
    </div>
  );
};

export default FinancialReports;