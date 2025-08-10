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
  reportType = ''
}) => {
  const [activeKey, setActiveKey] = useState(['summary', 'insights']);

  // Handle different insight formats
  const formatInsights = (rawInsights) => {
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
      insights: ['No specific insights available'],
      confidence: 'low',
      analysisDate: new Date().toISOString()
    };
  };

  const formattedInsights = formatInsights(insights);

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

export default ReportInsightsDisplay;