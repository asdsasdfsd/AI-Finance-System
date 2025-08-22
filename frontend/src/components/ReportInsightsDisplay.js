// frontend/src/components/ReportInsightsDisplay.js
import React, { useState } from 'react';
import {
  Card,
  Typography,
  Alert,
  Tag,
  Space,
  Divider,
  List,
  Timeline,
  Progress,
  Collapse,
  Badge,
  Tooltip,
  Button,
  Spin
} from 'antd';
import {
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  BulbOutlined,
  RiseOutlined,
  FallOutlined,
  MinusCircleOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;
const { Panel } = Collapse;

/**
 * Enhanced Report Insights Display Component
 * Renders structured AI analysis results with better formatting
 */
const ReportInsightsDisplay = ({ 
  insights, 
  loading = false, 
  onRefresh = null,
  reportName = '',
  reportType = '',
  reportData = null
}) => {
  const [activeKey, setActiveKey] = useState(['summary', 'insights']);

  // Handle different insight formats
  const formatInsights = (rawInsights, reportType, reportData) => {
    if (reportType === 'FINANCIAL_GROUPING' && reportData && !rawInsights) {
      return generateFinancialGroupingInsights(reportData);
    }

    // Handle already structured insights
    if (typeof rawInsights === 'object' && rawInsights.insights) {
      return rawInsights;
    }
    
    // Handle raw JSON string
    if (typeof rawInsights === 'string') {
      try {
        const parsed = JSON.parse(rawInsights);
        if (parsed.insights) return parsed;
      } catch (e) {
        // If not JSON, treat as plain text
        return {
          summary: rawInsights.substring(0, 200) + (rawInsights.length > 200 ? '...' : ''),
          insights: [rawInsights],
          confidence: 'medium',
          analysisDate: new Date().toISOString()
        };
      }
    }
    
    // Default fallback
    return {
      summary: 'Analysis completed',
      insights: rawInsights ? [rawInsights] : ['No insights available'],
      confidence: 'low',
      analysisDate: new Date().toISOString()
    };
  };

  const formattedInsights = formatInsights(insights, reportType, reportData);

  // Confidence level styling
  const getConfidenceColor = (confidence) => {
    switch (confidence) {
      case 'high': return 'green';
      case 'medium': return 'orange';
      case 'low': return 'red';
      default: return 'default';
    }
  };

  // Status icon
  const getStatusIcon = (status) => {
    switch (status) {
      case 'completed': return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'error': return <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />;
      default: return <InfoCircleOutlined style={{ color: '#1890ff' }} />;
    }
  };

  // Health status for specific report types
  const renderHealthStatus = () => {
    if (!formattedInsights.financialHealth && !formattedInsights.profitabilityAnalysis && !formattedInsights.cashFlowAnalysis) {
      return null;
    }

    return (
      <Card size="small" title={
        <Space>
          <RiseOutlined />
          <span>Financial Health Analysis</span>
        </Space>
      }>
        {formattedInsights.financialHealth && (
          <div style={{ marginBottom: 16 }}>
            <Text strong>Balance Sheet Health: </Text>
            <Tag color={formattedInsights.financialHealth.overall === 'good' ? 'green' : 
                       formattedInsights.financialHealth.overall === 'concerning' ? 'red' : 'orange'}>
              {formattedInsights.financialHealth.overall?.toUpperCase()}
            </Tag>
            <div style={{ marginTop: 8 }}>
              <Space>
                <Text type="secondary">Liquidity:</Text>
                <Tag size="small">{formattedInsights.financialHealth.liquidity}</Tag>
                <Text type="secondary">Solvency:</Text>
                <Tag size="small">{formattedInsights.financialHealth.solvency}</Tag>
              </Space>
            </div>
          </div>
        )}

        {formattedInsights.profitabilityAnalysis && (
          <div style={{ marginBottom: 16 }}>
            <Text strong>Profitability Trend: </Text>
            <Tag color={formattedInsights.profitabilityAnalysis.trend === 'improving' ? 'green' : 
                       formattedInsights.profitabilityAnalysis.trend === 'declining' ? 'red' : 'blue'}
                 icon={formattedInsights.profitabilityAnalysis.trend === 'improving' ? <RiseOutlined /> : 
                       formattedInsights.profitabilityAnalysis.trend === 'declining' ? <FallOutlined /> : <MinusCircleOutlined />}>
              {formattedInsights.profitabilityAnalysis.trend?.toUpperCase()}
            </Tag>
          </div>
        )}

        {formattedInsights.cashFlowAnalysis && (
          <div>
            <Text strong>Cash Flow Status: </Text>
            <Tag color={formattedInsights.cashFlowAnalysis.status === 'positive' ? 'green' : 
                       formattedInsights.cashFlowAnalysis.status === 'negative' ? 'red' : 'orange'}>
              {formattedInsights.cashFlowAnalysis.status?.toUpperCase()}
            </Tag>
          </div>
        )}
      </Card>
    );
  };

  if (loading) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '40px 0' }}>
          <Spin size="large" />
          <div style={{ marginTop: 16 }}>
            <Text>Analyzing report data...</Text>
          </div>
        </div>
      </Card>
    );
  }

  if (!insights) {
    return (
      <Alert
        message="No Analysis Available"
        description="Click 'Generate Insights' to analyze this report with AI."
        type="info"
        showIcon
      />
    );
  }

  return (
    <div style={{ maxWidth: '100%' }}>
      {/* Header Section */}
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>
              {getStatusIcon(formattedInsights.status)} AI Analysis Results
            </Title>
            {reportName && (
              <Text type="secondary">Report: {reportName}</Text>
            )}
          </div>
          <div>
            <Space>
              <Tooltip title="Analysis Confidence Level">
                <Tag color={getConfidenceColor(formattedInsights.confidence)}>
                  Confidence: {formattedInsights.confidence?.toUpperCase()}
                </Tag>
              </Tooltip>
              {formattedInsights.analysisDate && (
                <Text type="secondary">
                  {dayjs(formattedInsights.analysisDate).format('YYYY-MM-DD HH:mm')}
                </Text>
              )}
              {onRefresh && (
                <Button 
                  icon={<ReloadOutlined />} 
                  size="small" 
                  onClick={onRefresh}
                  title="Refresh Analysis"
                />
              )}
            </Space>
          </div>
        </div>
      </Card>

      {/* Error Handling */}
      {formattedInsights.error && (
        <Alert
          message="Analysis Warning"
          description={formattedInsights.errorMessage || "Some analysis features may not be available."}
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {/* Financial Health Status */}
      {renderHealthStatus()}

      {/* Main Analysis Content */}
      <Collapse 
        activeKey={activeKey} 
        onChange={setActiveKey}
        style={{ marginTop: 16 }}
      >
        {/* Summary Panel */}
        <Panel 
          header={
            <Space>
              <InfoCircleOutlined />
              <span>Executive Summary</span>
            </Space>
          } 
          key="summary"
        >
          <Paragraph>
            {formattedInsights.summary || 'No summary available'}
          </Paragraph>
        </Panel>

        {/* Key Insights Panel */}
        <Panel 
          header={
            <Space>
              <BulbOutlined />
              <span>Key Insights</span>
              <Badge count={formattedInsights.insights?.length || 0} />
            </Space>
          } 
          key="insights"
        >
          <List
            dataSource={formattedInsights.insights || []}
            renderItem={(insight, index) => (
              <List.Item>
                <Space align="start">
                  <Tag color="blue">{index + 1}</Tag>
                  <Text>{insight}</Text>
                </Space>
              </List.Item>
            )}
          />
        </Panel>

        {/* Anomalies Panel */}
        {formattedInsights.anomalies && formattedInsights.anomalies.length > 0 && (
          <Panel 
            header={
              <Space>
                <ExclamationCircleOutlined />
                <span>Anomalies & Risk Factors</span>
                <Badge count={formattedInsights.anomalies.length} />
              </Space>
            } 
            key="anomalies"
          >
            <Timeline>
              {formattedInsights.anomalies.map((anomaly, index) => (
                <Timeline.Item 
                  key={index}
                  color="red"
                  dot={<ExclamationCircleOutlined />}
                >
                  <Text>{anomaly}</Text>
                </Timeline.Item>
              ))}
            </Timeline>
          </Panel>
        )}

        {/* Recommendations Panel */}
        {formattedInsights.recommendations && formattedInsights.recommendations.length > 0 && (
          <Panel 
            header={
              <Space>
                <CheckCircleOutlined />
                <span>Recommendations</span>
                <Badge count={formattedInsights.recommendations.length} />
              </Space>
            } 
            key="recommendations"
          >
            <Timeline>
              {formattedInsights.recommendations.map((recommendation, index) => (
                <Timeline.Item 
                  key={index}
                  color="green"
                  dot={<CheckCircleOutlined />}
                >
                  <Text>{recommendation}</Text>
                </Timeline.Item>
              ))}
            </Timeline>
          </Panel>
        )}

        {/* Technical Details Panel */}
        <Panel 
          header={
            <Space>
              <InfoCircleOutlined />
              <span>Analysis Details</span>
            </Space>
          } 
          key="details"
        >
          <Space direction="vertical" style={{ width: '100%' }}>
            <div>
              <Text strong>Report Type: </Text>
              <Tag>{formattedInsights.reportType || reportType || 'Unknown'}</Tag>
            </div>
            <div>
              <Text strong>Analysis Status: </Text>
              <Tag color={formattedInsights.status === 'completed' ? 'green' : 'orange'}>
                {formattedInsights.status || 'Unknown'}
              </Tag>
            </div>
            <div>
              <Text strong>Confidence Level: </Text>
              <Progress 
                percent={
                  formattedInsights.confidence === 'high' ? 85 : 
                  formattedInsights.confidence === 'medium' ? 60 : 30
                }
                size="small"
                status={
                  formattedInsights.confidence === 'high' ? 'success' : 
                  formattedInsights.confidence === 'medium' ? 'active' : 'exception'
                }
              />
            </div>
            {formattedInsights.analysisDate && (
              <div>
                <Text strong>Generated: </Text>
                <Text code>{dayjs(formattedInsights.analysisDate).format('YYYY-MM-DD HH:mm:ss')}</Text>
              </div>
            )}
          </Space>
        </Panel>
      </Collapse>
    </div>
  );
};

const generateFinancialGroupingInsights = (data) => {
  if (!data || !data.summary) {
    return {
      summary: "No financial grouping data available for analysis.",
      insights: ["Unable to analyze financial grouping - insufficient data"],
      anomalies: [],
      recommendations: ["Please ensure financial grouping data is available"],
      confidence: "low"
    };
  }

  const insights = [];
  const anomalies = [];
  const recommendations = [];

  // Analyze income vs expenses
  const totalIncome = parseFloat(data.summary.totalIncome) || 0;
  const totalExpenses = parseFloat(data.summary.totalExpenses) || 0;
  const netIncome = parseFloat(data.summary.netIncome) || 0;
  const profitMargin = parseFloat(data.summary.profitMargin) || 0;

  // Revenue analysis
  if (totalIncome > 0) {
    insights.push(`Total income of ¥${totalIncome.toLocaleString()} indicates ${totalIncome > 1000000 ? 'strong' : 'moderate'} revenue generation`);
    
    if (profitMargin > 20) {
      insights.push(`Excellent profit margin of ${data.summary.profitMargin} demonstrates strong cost control`);
    } else if (profitMargin > 10) {
      insights.push(`Healthy profit margin of ${data.summary.profitMargin} shows good operational efficiency`);
    } else if (profitMargin > 0) {
      insights.push(`Profit margin of ${data.summary.profitMargin} suggests room for improvement in cost management`);
      recommendations.push("Consider reviewing expense categories to improve profit margins");
    } else {
      anomalies.push("Negative profit margin indicates expenses exceed income");
      recommendations.push("Urgent: Review and reduce expenses or increase revenue");
    }
  }

  // Expense analysis
  if (totalExpenses > 0) {
    const expenseRatio = parseFloat(data.summary.expenseRatio) || 0;
    
    if (expenseRatio > 80) {
      anomalies.push(`High expense ratio of ${data.summary.expenseRatio} may indicate cost control issues`);
      recommendations.push("Analyze major expense categories for potential cost reductions");
    } else if (expenseRatio > 60) {
      insights.push(`Expense ratio of ${data.summary.expenseRatio} is within acceptable range but could be optimized`);
    } else {
      insights.push(`Good expense control with ratio of ${data.summary.expenseRatio}`);
    }
  }

  // Category analysis
  if (data.categoryGrouping && Array.isArray(data.categoryGrouping)) {
    const categoryCount = data.categoryGrouping.length;
    insights.push(`Financial activity distributed across ${categoryCount} categories`);
    
    // Find top spending categories
    const sortedCategories = data.categoryGrouping
      .sort((a, b) => (parseFloat(b.totalAmount) || 0) - (parseFloat(a.totalAmount) || 0))
      .slice(0, 3);
    
    if (sortedCategories.length > 0) {
      const topCategory = sortedCategories[0];
      const topAmount = parseFloat(topCategory.totalAmount) || 0;
      const topPercentage = parseFloat(topCategory.percentage) || 0;
      
      insights.push(`Highest spending category: ${topCategory.category} (¥${topAmount.toLocaleString()}, ${topPercentage}%)`);
      
      if (topPercentage > 50) {
        anomalies.push(`Single category (${topCategory.category}) accounts for over 50% of total spending`);
        recommendations.push("Consider diversifying spending across categories to reduce concentration risk");
      }
    }
  }

  // Department analysis
  if (data.departmentGrouping && Array.isArray(data.departmentGrouping)) {
    const departmentCount = data.departmentGrouping.length;
    insights.push(`Budget allocation across ${departmentCount} departments`);
    
    // Analyze budget utilization
    const budgetAnalysis = data.departmentGrouping.map(dept => {
      const budgetAllocated = parseFloat(dept.budgetAllocated) || 0;
      const actualSpent = parseFloat(dept.actualSpent) || parseFloat(dept.totalAmount) || 0;
      const utilization = budgetAllocated > 0 ? (actualSpent / budgetAllocated) * 100 : 0;
      
      return {
        department: dept.department,
        utilization,
        budgetAllocated,
        actualSpent,
        variance: actualSpent - budgetAllocated
      };
    });
    
    const overBudgetDepts = budgetAnalysis.filter(d => d.utilization > 100);
    const underUtilizedDepts = budgetAnalysis.filter(d => d.utilization < 70);
    
    if (overBudgetDepts.length > 0) {
      anomalies.push(`${overBudgetDepts.length} department(s) exceeded budget: ${overBudgetDepts.map(d => d.department).join(', ')}`);
      recommendations.push("Review budget allocations for over-spending departments");
    }
    
    if (underUtilizedDepts.length > 0) {
      insights.push(`${underUtilizedDepts.length} department(s) under-utilized budget - potential for reallocation`);
      recommendations.push("Consider reallocating unused budget to high-priority areas");
    }
  }

  // Monthly trend analysis
  if (data.monthlyTrend && Array.isArray(data.monthlyTrend)) {
    const monthlyData = data.monthlyTrend.map(month => ({
      month: month.month,
      totalAmount: parseFloat(month.totalAmount) || 0,
      netIncome: parseFloat(month.netIncome) || 0
    }));
    
    if (monthlyData.length >= 2) {
      const currentMonth = monthlyData[monthlyData.length - 1];
      const previousMonth = monthlyData[monthlyData.length - 2];
      
      const growthRate = previousMonth.totalAmount > 0 
        ? ((currentMonth.totalAmount - previousMonth.totalAmount) / previousMonth.totalAmount) * 100
        : 0;
      
      if (growthRate > 10) {
        insights.push(`Strong month-over-month growth of ${growthRate.toFixed(1)}%`);
      } else if (growthRate > 0) {
        insights.push(`Positive month-over-month growth of ${growthRate.toFixed(1)}%`);
      } else if (growthRate < -10) {
        anomalies.push(`Significant month-over-month decline of ${Math.abs(growthRate).toFixed(1)}%`);
        recommendations.push("Investigate causes of declining financial activity");
      }
    }
  }

  // Transaction analysis
  const totalTransactions = data.summary.totalTransactions || 0;
  if (totalTransactions > 0) {
    const avgTransactionValue = totalIncome > 0 ? totalIncome / totalTransactions : 0;
    insights.push(`Average transaction value: ¥${avgTransactionValue.toLocaleString()}`);
    
    if (avgTransactionValue > 50000) {
      insights.push("High-value transaction pattern indicates significant financial operations");
    } else if (avgTransactionValue < 1000) {
      insights.push("High frequency, low-value transaction pattern");
      recommendations.push("Consider batch processing small transactions for efficiency");
    }
  }

  // Generate summary
  const performanceIndicator = netIncome >= 0 ? "profitable" : "loss-making";
  const riskLevel = anomalies.length > 2 ? "high" : anomalies.length > 0 ? "medium" : "low";
  
  const summary = `Financial grouping analysis shows ${performanceIndicator} operations with ${riskLevel} risk level. ` +
    `${insights.length} key insights identified across categories, departments, and trends.`;

  return {
    summary,
    insights,
    anomalies,
    recommendations,
    confidence: anomalies.length === 0 ? "high" : "medium",
    analysisDate: new Date().toISOString(),
    status: "success"
  };
};

export default ReportInsightsDisplay;