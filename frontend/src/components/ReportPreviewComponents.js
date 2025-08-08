// frontend/src/components/ReportPreviewComponents.js
import React from 'react';
import { Card, Table, Tabs, Typography, Row, Col, Statistic } from 'antd';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

/**
 * Unified Report Preview Components
 * 
 * This file contains standardized preview components for all financial reports
 * to ensure consistent styling and layout across FinancialReports.js and FinancialReportsUnified.js
 */

// Balance Sheet Preview Component
export const BalanceSheetPreview = ({ data }) => {
  if (!data) return null;

  const columns = [
    { title: 'Account', dataIndex: 'accountName', key: 'accountName' },
    { 
      title: 'Amount', 
      dataIndex: 'amount', 
      key: 'amount',
      render: (value) => `$${Number(value || 0).toLocaleString()}`,
      align: 'right'
    }
  ];

  return (
    <div>
      {/* Summary Statistics */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Statistic 
            title="Total Assets" 
            value={data.totalAssets || 0} 
            prefix="$" 
            precision={2}
          />
        </Col>
        <Col span={8}>
          <Statistic 
            title="Total Liabilities" 
            value={data.totalLiabilities || 0} 
            prefix="$" 
            precision={2}
          />
        </Col>
        <Col span={8}>
          <Statistic 
            title="Total Equity" 
            value={data.totalEquity || 0} 
            prefix="$" 
            precision={2}
            valueStyle={{ color: data.isBalanced ? '#52c41a' : '#ff4d4f' }}
          />
        </Col>
      </Row>

      {/* Detailed Breakdown */}
      <Tabs defaultActiveKey="assets">
        <TabPane tab="Assets" key="assets">
          {data.assets && Object.entries(data.assets).map(([category, accounts]) => (
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
          {data.liabilities && Object.entries(data.liabilities).map(([category, accounts]) => (
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
          {data.equity && Object.entries(data.equity).map(([category, accounts]) => (
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
    </div>
  );
};

// Income Statement Preview Component
export const IncomeStatementPreview = ({ data }) => {
  if (!data) return null;

  const sections = [
    {
      title: 'I. Operating Revenue',
      items: data.revenues || [],
      total: data.totalRevenue,
      isRevenue: true
    },
    {
      title: 'II. Operating Expenses', 
      items: data.operatingExpenses || [],
      total: data.totalOperatingExpenses,
      isRevenue: false
    },
    {
      title: 'III. Administrative Expenses',
      items: data.administrativeExpenses || [],
      total: data.totalAdministrativeExpenses,
      isRevenue: false
    },
    {
      title: 'IV. Financial Expenses',
      items: data.financialExpenses || [],
      total: data.totalFinancialExpenses,
      isRevenue: false
    }
  ];

  // Add other income section if exists
  if (data.otherIncomes && data.otherIncomes.length > 0) {
    sections.push({
      title: 'V. Other Income',
      items: data.otherIncomes,
      total: data.totalOtherIncomes,
      isRevenue: true
    });
  }

  // Add other expenses section if exists
  if (data.otherExpenses && data.otherExpenses.length > 0) {
    sections.push({
      title: 'VI. Other Expenses',
      items: data.otherExpenses,
      total: data.totalOtherExpenses,
      isRevenue: false
    });
  }

  return (
    <div>
      {/* Summary Statistics */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Statistic 
            title="Total Revenue" 
            value={data.totalRevenue || 0} 
            prefix="$" 
            precision={2}
            valueStyle={{ color: '#52c41a' }}
          />
        </Col>
        <Col span={8}>
          <Statistic 
            title="Total Expenses" 
            value={data.totalExpenses || 0} 
            prefix="$" 
            precision={2}
            valueStyle={{ color: '#ff4d4f' }}
          />
        </Col>
        <Col span={8}>
          <Statistic 
            title="Net Income" 
            value={data.netIncome || 0} 
            prefix="$" 
            precision={2}
            valueStyle={{ color: (data.netIncome || 0) >= 0 ? '#52c41a' : '#ff4d4f' }}
          />
        </Col>
      </Row>

      {/* Detailed Breakdown */}
      <div style={{ background: '#fafafa', padding: '16px', borderRadius: '6px' }}>
        {sections.map((section, index) => (
          <div key={index} style={{ marginBottom: 16 }}>
            <Title level={4} style={{ color: section.isRevenue ? '#52c41a' : '#ff4d4f' }}>
              {section.title}
            </Title>
            
            {section.items && section.items.length > 0 ? (
              <Table
                dataSource={section.items}
                columns={[
                  { title: 'Account', dataIndex: 'accountName', key: 'accountName' },
                  { 
                    title: 'Amount', 
                    dataIndex: 'amount', 
                    key: 'amount',
                    render: (value) => `$${Number(value || 0).toLocaleString()}`,
                    align: 'right'
                  }
                ]}
                pagination={false}
                size="small"
                rowKey="accountName"
                style={{ marginBottom: 8 }}
              />
            ) : (
              <Text type="secondary" style={{ marginLeft: 16 }}>No data available</Text>
            )}
            
            {section.total !== undefined && (
              <div style={{ textAlign: 'right', fontWeight: 'bold', marginTop: 8 }}>
                <Text strong style={{ color: section.isRevenue ? '#52c41a' : '#ff4d4f' }}>
                  Total: ${Number(section.total || 0).toLocaleString()}
                </Text>
              </div>
            )}
          </div>
        ))}
        
        {/* Final Net Income */}
        <div style={{ 
          borderTop: '2px solid #d9d9d9', 
          paddingTop: 16, 
          textAlign: 'right' 
        }}>
          <Title level={3} style={{ 
            color: (data.netIncome || 0) >= 0 ? '#52c41a' : '#ff4d4f',
            margin: 0 
          }}>
            Net Income: ${Number(data.netIncome || 0).toLocaleString()}
          </Title>
        </div>
      </div>
    </div>
  );
};

// Income vs Expense Preview Component
export const IncomeExpensePreview = ({ data }) => {
  if (!data) return null;

  return (
    <div>
      {/* Summary Statistics */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card>
            <Statistic 
              title="Total Income (YTD)" 
              value={data.totalIncomeYTD || 0} 
              prefix="$" 
              precision={2}
              valueStyle={{ color: '#52c41a' }}
            />
            <Statistic 
              title="Total Income (Month)" 
              value={data.totalIncomeMonth || 0} 
              prefix="$" 
              precision={2}
              valueStyle={{ color: '#52c41a', fontSize: '14px' }}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <Statistic 
              title="Total Expense (YTD)" 
              value={data.totalExpenseYTD || 0} 
              prefix="$" 
              precision={2}
              valueStyle={{ color: '#ff4d4f' }}
            />
            <Statistic 
              title="Total Expense (Month)" 
              value={data.totalExpenseMonth || 0} 
              prefix="$" 
              precision={2}
              valueStyle={{ color: '#ff4d4f', fontSize: '14px' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Net Income Summary */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <Text type="secondary">Net Income (YTD)</Text>
              <div style={{ 
                fontSize: '24px', 
                fontWeight: 'bold',
                color: (data.netIncomeYTD || 0) >= 0 ? '#52c41a' : '#ff4d4f',
                marginTop: 8 
              }}>
                ${Number(data.netIncomeYTD || 0).toLocaleString()}
              </div>
            </div>
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <Text type="secondary">Net Income (Month)</Text>
              <div style={{ 
                fontSize: '24px', 
                fontWeight: 'bold',
                color: (data.netIncomeMonth || 0) >= 0 ? '#52c41a' : '#ff4d4f',
                marginTop: 8 
              }}>
                ${Number(data.netIncomeMonth || 0).toLocaleString()}
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* Detailed Breakdown */}
      <Tabs defaultActiveKey="income">
        <TabPane tab="Income Items" key="income">
          {data.incomeRows && data.incomeRows.length > 0 ? (
            <Table
              dataSource={data.incomeRows}
              columns={[
                { title: 'Category', dataIndex: 'category', key: 'category' },
                { title: 'Description', dataIndex: 'description', key: 'description' },
                { 
                  title: 'YTD Amount', 
                  dataIndex: 'ytdAmount', 
                  key: 'ytdAmount',
                  render: (value) => `$${Number(value || 0).toLocaleString()}`,
                  align: 'right'
                },
                { 
                  title: 'Month Amount', 
                  dataIndex: 'monthAmount', 
                  key: 'monthAmount',
                  render: (value) => `$${Number(value || 0).toLocaleString()}`,
                  align: 'right'
                }
              ]}
              pagination={false}
              size="small"
              rowKey={(record, index) => index}
            />
          ) : (
            <Text type="secondary">No income data available</Text>
          )}
        </TabPane>
        <TabPane tab="Expense Items" key="expense">
          {data.expenseRows && data.expenseRows.length > 0 ? (
            <Table
              dataSource={data.expenseRows}
              columns={[
                { title: 'Category', dataIndex: 'category', key: 'category' },
                { title: 'Description', dataIndex: 'description', key: 'description' },
                { 
                  title: 'YTD Amount', 
                  dataIndex: 'ytdAmount', 
                  key: 'ytdAmount',
                  render: (value) => `$${Number(value || 0).toLocaleString()}`,
                  align: 'right'
                },
                { 
                  title: 'Month Amount', 
                  dataIndex: 'monthAmount', 
                  key: 'monthAmount',
                  render: (value) => `$${Number(value || 0).toLocaleString()}`,
                  align: 'right'
                }
              ]}
              pagination={false}
              size="small"
              rowKey={(record, index) => index}
            />
          ) : (
            <Text type="secondary">No expense data available</Text>
          )}
        </TabPane>
      </Tabs>
    </div>
  );
};

// Financial Grouping Preview Component
export const FinancialGroupingPreview = ({ data }) => {
  if (!data) return null;

  return (
    <Tabs defaultActiveKey="category">
      <TabPane tab="By Category" key="category">
        {data.categoryGrouping && Object.keys(data.categoryGrouping).length > 0 ? (
          <Table 
            dataSource={Object.entries(data.categoryGrouping).map(([key, value]) => ({ key, value }))}
            columns={[
              { title: 'Category', dataIndex: 'key', key: 'key' },
              { 
                title: 'Amount', 
                dataIndex: 'value', 
                key: 'value', 
                render: (value) => `$${Number(value || 0).toLocaleString()}`,
                align: 'right'
              }
            ]}
            pagination={false}
            size="small"
            rowKey="key"
          />
        ) : (
          <Text type="secondary">No category grouping data available</Text>
        )}
      </TabPane>
      <TabPane tab="By Department" key="department">
        {data.departmentGrouping && Object.keys(data.departmentGrouping).length > 0 ? (
          <Table 
            dataSource={Object.entries(data.departmentGrouping).map(([key, value]) => ({ key, value }))}
            columns={[
              { title: 'Department', dataIndex: 'key', key: 'key' },
              { 
                title: 'Amount', 
                dataIndex: 'value', 
                key: 'value', 
                render: (value) => `$${Number(value || 0).toLocaleString()}`,
                align: 'right'
              }
            ]}
            pagination={false}
            size="small"
            rowKey="key"
          />
        ) : (
          <Text type="secondary">No department grouping data available</Text>
        )}
      </TabPane>
      <TabPane tab="By Type" key="type">
        {data.typeGrouping && Object.keys(data.typeGrouping).length > 0 ? (
          <Table 
            dataSource={Object.entries(data.typeGrouping).map(([key, value]) => ({ key, value }))}
            columns={[
              { title: 'Type', dataIndex: 'key', key: 'key' },
              { 
                title: 'Amount', 
                dataIndex: 'value', 
                key: 'value', 
                render: (value) => `$${Number(value || 0).toLocaleString()}`,
                align: 'right'
              }
            ]}
            pagination={false}
            size="small"
            rowKey="key"
          />
        ) : (
          <Text type="secondary">No type grouping data available</Text>
        )}
      </TabPane>
    </Tabs>
  );
};

// Generic Report Preview Selector
export const ReportPreview = ({ reportType, data }) => {
  switch (reportType) {
    case 'BALANCE_SHEET':
      return <BalanceSheetPreview data={data} />;
    case 'INCOME_STATEMENT':
      return <IncomeStatementPreview data={data} />;
    case 'INCOME_EXPENSE':
      return <IncomeExpensePreview data={data} />;
    case 'FINANCIAL_GROUPING':
      return <FinancialGroupingPreview data={data} />;
    default:
      return <Text>Preview not available for this report type</Text>;
  }
};