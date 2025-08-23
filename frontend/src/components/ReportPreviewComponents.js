// frontend/src/components/ReportPreviewComponents.js
import React, { useState } from 'react';
import { 
  Tabs, Table, Typography, Row, Col, Statistic, Tag, Space, 
  Progress, Card, Collapse, Tooltip, Alert
} from 'antd';
import {
  FundOutlined, BankOutlined, BarChartOutlined, CalendarOutlined,
  InfoCircleOutlined, DollarCircleOutlined
} from '@ant-design/icons';

const { Text, Title } = Typography;
const { TabPane } = Tabs;
const { Panel } = Collapse;

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
  const [activeTab, setActiveTab] = useState('category');
  
  if (!data) {
    return (
      <Alert
        message="No Data Available"
        description="No financial grouping data found for the selected criteria."
        type="info"
        showIcon
      />
    );
  }

  // Format currency helper
  const formatCurrency = (value) => {
    const num = parseFloat(value) || 0;
    return `¥${num.toLocaleString()}`;
  };

  // Get category type color
  const getCategoryTypeColor = (type) => {
    const colorMap = {
      'INCOME': '#52c41a',
      'EXPENSE': '#ff4d4f',
      'ASSET': '#1890ff',
      'LIABILITY': '#722ed1',
      'EQUITY': '#fa8c16'
    };
    return colorMap[type] || '#666';
  };

  // Process categoryGrouping data from backend
  const processCategoryData = () => {
    if (!data.categoryGrouping || !Array.isArray(data.categoryGrouping)) {
      return [];
    }
    
    return data.categoryGrouping.map((item, index) => ({
      key: `category_${index}`,
      name: item.category || 'Unknown Category',
      type: item.type || 'Unknown',
      amount: parseFloat(item.totalAmount) || 0,
      count: item.transactionCount || 0,
      percentage: item.percentage || '0%',
      subcategories: item.subcategories || []
    }));
  };

  // Process departmentGrouping data
  const processDepartmentData = () => {
    if (!data.departmentGrouping || !Array.isArray(data.departmentGrouping)) {
      return [];
    }
    
    return data.departmentGrouping.map((item, index) => ({
      key: `department_${index}`,
      name: item.department || 'Unknown Department',
      budgetAllocated: parseFloat(item.budgetAllocated) || 0,
      actualSpent: parseFloat(item.actualSpent) || parseFloat(item.totalAmount) || 0,
      utilization: item.budgetUtilization || '0%',
      count: item.transactionCount || 0,
      avgTransactionSize: parseFloat(item.averageTransactionSize) || 0
    }));
  };

  // Process transactionTypeGrouping data
  const processTransactionTypeData = () => {
    if (!data.transactionTypeGrouping || !Array.isArray(data.transactionTypeGrouping)) {
      return [];
    }
    
    return data.transactionTypeGrouping.map((item, index) => ({
      key: `type_${index}`,
      name: item.transactionType || 'Unknown Type',
      amount: parseFloat(item.totalAmount) || 0,
      count: item.transactionCount || 0,
      percentage: item.percentage || '0%',
      avgAmount: parseFloat(item.averageAmount) || 0
    }));
  };

  // Process monthlyTrend data
  const processMonthlyData = () => {
    if (!data.monthlyTrend || !Array.isArray(data.monthlyTrend)) {
      return [];
    }
    
    return data.monthlyTrend.map((item, index) => ({
      key: `month_${index}`,
      name: item.month || 'Unknown Month',
      totalAmount: (parseFloat(item.income) || 0) + (parseFloat(item.expenses) || 0), // Calculate total
      totalIncome: parseFloat(item.income) || 0,     // backend uses 'income'
      totalExpenses: parseFloat(item.expenses) || 0, // backend uses 'expenses'  
      netIncome: parseFloat(item.netIncome) || 0,    // backend uses 'netIncome'
      count: item.transactionCount || 0
    }));
  };

  // Get processed data
  const categoryData = processCategoryData();
  const departmentData = processDepartmentData();
  const transactionTypeData = processTransactionTypeData();
  const monthlyData = processMonthlyData();

  // Category columns with enhanced rendering
  const categoryColumns = [
    {
      title: 'Category',
      dataIndex: 'name',
      key: 'name',
      width: '30%',
      render: (name, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{name}</Text>
          <Tag color={getCategoryTypeColor(record.type)} size="small">
            {record.type}
          </Tag>
        </Space>
      )
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      width: '25%',
      align: 'right',
      render: (value) => (
        <Text strong style={{ fontSize: '14px' }}>
          {formatCurrency(value)}
        </Text>
      ),
      sorter: (a, b) => a.amount - b.amount
    },
    {
      title: 'Count',
      dataIndex: 'count',
      key: 'count',
      width: '15%',
      align: 'center',
      render: (count) => <Tag color="blue">{count}</Tag>
    },
    {
      title: 'Percentage',
      dataIndex: 'percentage',
      key: 'percentage',
      width: '20%',
      render: (percentage) => {
        const percent = parseFloat(percentage) || 0;
        return (
          <Progress 
            percent={percent} 
            size="small" 
            format={(percent) => `${percent}%`}
            strokeColor={percent > 20 ? '#52c41a' : percent > 10 ? '#faad14' : '#ff4d4f'}
          />
        );
      }
    },
    {
      title: 'Avg/Transaction',
      key: 'average',
      width: '10%',
      align: 'right',
      render: (_, record) => {
        const avg = record.count > 0 ? record.amount / record.count : 0;
        return <Text type="secondary">{formatCurrency(avg)}</Text>;
      }
    }
  ];

  // Department columns
  const departmentColumns = [
    {
      title: 'Department',
      dataIndex: 'name',
      key: 'name',
      width: '25%',
      render: (name) => (
        <Space>
          <BankOutlined />
          <Text strong>{name}</Text>
        </Space>
      )
    },
    {
      title: 'Budget Allocated',
      dataIndex: 'budgetAllocated',
      key: 'budgetAllocated',
      width: '20%',
      align: 'right',
      render: (value) => <Text>{formatCurrency(value)}</Text>
    },
    {
      title: 'Actual Spent',
      dataIndex: 'actualSpent',
      key: 'actualSpent',
      width: '20%',
      align: 'right',
      render: (value) => <Text strong>{formatCurrency(value)}</Text>
    },
    {
      title: 'Utilization',
      dataIndex: 'utilization',
      key: 'utilization',
      width: '15%',
      align: 'center',
      render: (utilization) => {
        const percent = parseFloat(utilization) || 0;
        const color = percent > 100 ? 'red' : percent > 80 ? 'orange' : 'green';
        return <Tag color={color}>{utilization}</Tag>;
      }
    },
    {
      title: 'Transactions',
      dataIndex: 'count',
      key: 'count',
      width: '10%',
      align: 'center',
      render: (count) => <Tag color="blue">{count}</Tag>
    },
    {
      title: 'Variance',
      key: 'variance',
      width: '10%',
      align: 'right',
      render: (_, record) => {
        const variance = record.actualSpent - record.budgetAllocated;
        return (
          <Text style={{ color: variance > 0 ? '#ff4d4f' : '#52c41a' }}>
            {formatCurrency(variance)}
          </Text>
        );
      }
    }
  ];

  // Transaction type columns
  const transactionTypeColumns = [
    {
      title: 'Transaction Type',
      dataIndex: 'name',
      key: 'name',
      width: '35%',
      render: (name) => (
        <Space>
          <BarChartOutlined />
          <Text strong>{name}</Text>
        </Space>
      )
    },
    {
      title: 'Total Amount',
      dataIndex: 'amount',
      key: 'amount',
      width: '25%',
      align: 'right',
      render: (value) => <Text strong>{formatCurrency(value)}</Text>
    },
    {
      title: 'Count',
      dataIndex: 'count',
      key: 'count',
      width: '15%',
      align: 'center',
      render: (count) => <Tag color="blue">{count}</Tag>
    },
    {
      title: 'Percentage',
      dataIndex: 'percentage',
      key: 'percentage',
      width: '15%',
      align: 'center',
      render: (percentage) => <Tag color="green">{percentage}</Tag>
    },
    {
      title: 'Average',
      dataIndex: 'avgAmount',
      key: 'avgAmount',
      width: '10%',
      align: 'right',
      render: (value) => <Text type="secondary">{formatCurrency(value)}</Text>
    }
  ];

  // Monthly trend columns
  const monthlyColumns = [
    {
      title: 'Month',
      dataIndex: 'name',
      key: 'name',
      width: '20%',
      render: (name) => (
        <Space>
          <CalendarOutlined />
          <Text strong>{name}</Text>
        </Space>
      )
    },
    {
      title: 'Total',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      width: '20%',
      align: 'right',
      render: (value) => <Text strong>{formatCurrency(value)}</Text>
    },
    {
      title: 'Income',
      dataIndex: 'totalIncome',
      key: 'totalIncome',
      width: '15%',
      align: 'right',
      render: (value) => <Text style={{ color: '#52c41a' }}>{formatCurrency(value)}</Text>
    },
    {
      title: 'Expenses',
      dataIndex: 'totalExpenses',
      key: 'totalExpenses',
      width: '15%',
      align: 'right',
      render: (value) => <Text style={{ color: '#ff4d4f' }}>{formatCurrency(value)}</Text>
    },
    {
      title: 'Net Income',
      dataIndex: 'netIncome',
      key: 'netIncome',
      width: '15%',
      align: 'right',
      render: (value) => {
        const isPositive = value >= 0;
        return (
          <Text strong style={{ color: isPositive ? '#52c41a' : '#ff4d4f' }}>
            {formatCurrency(value)}
          </Text>
        );
      }
    },
    {
      title: 'Transactions',
      dataIndex: 'count',
      key: 'count',
      width: '15%',
      align: 'center',
      render: (count) => <Tag color="blue">{count}</Tag>
    }
  ];

  // Summary section
  const renderSummary = () => {
    if (!data.summary) return null;

    return (
      <Card style={{ marginBottom: 16 }} size="small">
        <Row gutter={16}>
          <Col span={6}>
            <Statistic
              title="Total Income"
              value={data.summary.totalIncome || 0}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#52c41a' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Total Expenses"
              value={data.summary.totalExpenses || 0}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Net Income"
              value={data.summary.netIncome || 0}
              precision={2}
              prefix="¥"
              valueStyle={{ color: (data.summary.netIncome || 0) >= 0 ? '#52c41a' : '#ff4d4f' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Total Transactions"
              value={data.summary.totalTransactions || 0}
              valueStyle={{ color: '#1890ff' }}
            />
          </Col>
        </Row>
        <Row gutter={16} style={{ marginTop: 16 }}>
          <Col span={12}>
            <Statistic
              title="Profit Margin"
              value={data.summary.profitMargin || '0%'}
              valueStyle={{ color: '#722ed1' }}
            />
          </Col>
          <Col span={12}>
            <Statistic
              title="Expense Ratio"
              value={data.summary.expenseRatio || '0%'}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Col>
        </Row>
      </Card>
    );
  };

  // Expandable row render for categories with subcategories
  const expandedRowRender = (record) => {
    if (!record.subcategories || record.subcategories.length === 0) {
      return <Text type="secondary">No subcategory details available</Text>;
    }

    const subColumns = [
      { title: 'Subcategory', dataIndex: 'name', key: 'name' },
      { 
        title: 'Amount', 
        dataIndex: 'amount', 
        key: 'amount',
        render: (value) => formatCurrency(value),
        align: 'right'
      },
      { title: 'Transactions', dataIndex: 'transactionCount', key: 'count', align: 'center' },
      { title: 'Percentage', dataIndex: 'percentage', key: 'percentage', align: 'center' }
    ];

    return (
      <Table
        columns={subColumns}
        dataSource={record.subcategories}
        pagination={false}
        size="small"
        showHeader={false}
        rowKey="id"
      />
    );
  };

  return (
    <div>
      {renderSummary()}
      
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane 
          tab={
            <Space>
              <FundOutlined />
              By Category ({categoryData.length})
            </Space>
          } 
          key="category"
        >
          {categoryData.length > 0 ? (
            <Table
              dataSource={categoryData}
              columns={categoryColumns}
              pagination={{ pageSize: 10, showSizeChanger: true }}
              size="small"
              expandable={{
                expandedRowRender,
                rowExpandable: (record) => record.subcategories && record.subcategories.length > 0,
              }}
              summary={(pageData) => {
                const total = pageData.reduce((sum, item) => sum + item.amount, 0);
                const totalCount = pageData.reduce((sum, item) => sum + item.count, 0);
                return (
                  <Table.Summary.Row>
                    <Table.Summary.Cell><Text strong>Total</Text></Table.Summary.Cell>
                    <Table.Summary.Cell align="right">
                      <Text strong>{formatCurrency(total)}</Text>
                    </Table.Summary.Cell>
                    <Table.Summary.Cell align="center">
                      <Text strong>{totalCount}</Text>
                    </Table.Summary.Cell>
                    <Table.Summary.Cell>100%</Table.Summary.Cell>
                    <Table.Summary.Cell align="right">
                      <Text strong>{formatCurrency(totalCount > 0 ? total / totalCount : 0)}</Text>
                    </Table.Summary.Cell>
                  </Table.Summary.Row>
                );
              }}
            />
          ) : (
            <Text type="secondary">No category grouping data available</Text>
          )}
        </TabPane>

        <TabPane 
          tab={
            <Space>
              <BankOutlined />
              By Department ({departmentData.length})
            </Space>
          } 
          key="department"
        >
          {departmentData.length > 0 ? (
            <Table
              dataSource={departmentData}
              columns={departmentColumns}
              pagination={{ pageSize: 10, showSizeChanger: true }}
              size="small"
            />
          ) : (
            <Text type="secondary">No department grouping data available</Text>
          )}
        </TabPane>

        <TabPane 
          tab={
            <Space>
              <BarChartOutlined />
              By Transaction Type ({transactionTypeData.length})
            </Space>
          } 
          key="type"
        >
          {transactionTypeData.length > 0 ? (
            <Table
              dataSource={transactionTypeData}
              columns={transactionTypeColumns}
              pagination={{ pageSize: 10, showSizeChanger: true }}
              size="small"
            />
          ) : (
            <Text type="secondary">No transaction type grouping data available</Text>
          )}
        </TabPane>

        <TabPane 
          tab={
            <Space>
              <CalendarOutlined />
              Monthly Trend ({monthlyData.length})
            </Space>
          } 
          key="monthly"
        >
          {monthlyData.length > 0 ? (
            <Table
              dataSource={monthlyData}
              columns={monthlyColumns}
              pagination={{ pageSize: 12, showSizeChanger: true }}
              size="small"
            />
          ) : (
            <Text type="secondary">No monthly trend data available</Text>
          )}
        </TabPane>
      </Tabs>
    </div>
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