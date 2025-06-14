// frontend/src/views/Dashboard/FinancialReports.js
import React, { useState, useEffect } from 'react';
import {
  Card, Select, Button, Space, message, Typography, DatePicker,
  Row, Col, Spin, Alert, InputNumber, Table
} from 'antd';
import {
  FundProjectionScreenOutlined, DownloadOutlined, ReloadOutlined,
  BarChartOutlined, DollarCircleOutlined, PieChartOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';
import BalanceSheetService from '../../services/balanceSheetService';

const { Option } = Select;
const { Title, Text } = Typography;

/**
 * Enhanced Financial Reports Component with improved error handling
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
      description: 'Assets, Liabilities, and Equity at a specific date'
    },
    {
      value: 'INCOME_EXPENSE',
      label: 'Income vs Expense Report',
      icon: <PieChartOutlined />,
      description: 'Income and expense analysis over a period'
    },
    {
      value: 'INCOME_STATEMENT',
      label: 'Income Statement',
      icon: <DollarCircleOutlined />,
      description: 'Revenue and expenses over a period'
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
      let data = null;
      
      if (reportType === 'BALANCE_SHEET') {
        // Use existing balance sheet API
        const response = await fetch('/api/balance-sheet/json?' + 
          new URLSearchParams({
            companyId: companyId,
            asOfDate: asOfDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
          });
        
        if (!response.ok) {
          throw new Error('Failed to fetch balance sheet data');
        }
        data = await response.json();
      } else if (reportType === 'INCOME_EXPENSE') {
        // Use existing income expense API
        const response = await fetch('/api/financial-report/json?' + 
          new URLSearchParams({
            companyId: companyId,
            asOfDate: asOfDate.format('YYYY-MM-DD')
          }), {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
          });
        
        if (!response.ok) {
          throw new Error('Failed to fetch income expense data');
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
      if (reportType === 'BALANCE_SHEET') {
        await BalanceSheetService.exportBalanceSheet(
          companyId,
          asOfDate.format('YYYY-MM-DD')
        );
        message.success('Balance sheet exported successfully');
      } else if (reportType === 'INCOME_EXPENSE') {
        const response = await fetch('/api/financial-report/export?' + 
          new URLSearchParams({
            companyId: companyId,
            asOfDate: asOfDate.format('YYYY-MM-DD')
          }), {
            method: 'GET'
          });
        
        if (!response.ok) {
          throw new Error('Export failed');
        }
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `IncomeExpenseReport_${asOfDate.format('YYYY-MM-DD')}.xlsx`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        
        message.success('Income expense report exported successfully');
      }
    } catch (error) {
      console.error('Export error:', error);
      message.error('Failed to export report: ' + error.message);
    }
  };

  const renderParameterControls = () => {
    if (reportType === 'BALANCE_SHEET' || reportType === 'INCOME_EXPENSE') {
      return (
        <>
          <Col xs={24} sm={12} md={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Company ID:</Text>
              <InputNumber
                value={companyId}
                onChange={setCompanyId}
                min={1}
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>As of Date:</Text>
              <DatePicker
                value={asOfDate}
                onChange={setAsOfDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
        </>
      );
    } else {
      return (
        <>
          <Col xs={24} sm={12} md={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>Start Date:</Text>
              <DatePicker
                value={startDate}
                onChange={setStartDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Text strong>End Date:</Text>
              <DatePicker
                value={endDate}
                onChange={setEndDate}
                format="YYYY-MM-DD"
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
        </>
      );
    }
  };

  const renderReportContent = () => {
    if (loading) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <div style={{ marginTop: 16 }}>
            <Text>Loading report data...</Text>
          </div>
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
          style={{ margin: '20px 0' }}
        />
      );
    }

    if (!reportData) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Text>Select parameters and click "Generate Report" to view data</Text>
        </div>
      );
    }

    if (reportType === 'BALANCE_SHEET') {
      return renderBalanceSheet();
    } else if (reportType === 'INCOME_EXPENSE') {
      return renderIncomeExpenseReport();
    }

    return <div>Report content will be displayed here</div>;
  };

  const renderBalanceSheet = () => {
    if (!reportData) return null;

    console.log('Rendering balance sheet with data:', reportData);

    const renderSection = (sectionName, sectionData, total) => {
      // Safe check for undefined or null data
      if (!sectionData || typeof sectionData !== 'object') {
        console.warn(`Section data for ${sectionName} is invalid:`, sectionData);
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
            // Safe check for accounts array
            if (!Array.isArray(accounts)) {
              console.warn(`Accounts for category ${category} is not an array:`, accounts);
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
                      // Safe check for account object
                      if (!account || typeof account !== 'object') {
                        console.warn(`Invalid account data at index ${index}:`, account);
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

  return (
    <Card
      title={
        <Space>
          <FundProjectionScreenOutlined />
          Financial Reports
        </Space>
      }
      style={{ margin: 24 }}
    >
      {/* Controls */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Report Type:</Text>
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
          </Space>
        </Col>

        {renderParameterControls()}

        <Col xs={24} sm={12} md={6}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>Actions:</Text>
            <Space>
              <Button
                type="primary"
                icon={<ReloadOutlined />}
                onClick={loadReportData}
                loading={loading}
              >
                Generate
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={handleExport}
                disabled={!reportData}
              >
                Export
              </Button>
            </Space>
          </Space>
        </Col>
      </Row>

      {/* Report Type Description */}
      {currentReportConfig && (
        <Alert
          message={currentReportConfig.label}
          description={currentReportConfig.description}
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {/* Report Content */}
      <div style={{ marginTop: 16 }}>
        {renderReportContent()}
      </div>
    </Card>
  );
};

export default FinancialReports;