// frontend/src/views/Dashboard/AIHelper.js
import React, { useState, useEffect } from 'react';
import {
  Card,
  Typography,
  Space,
  Button,
  Divider,
  Alert,
  Modal,
  Table,
  Checkbox,
  DatePicker,
  Select,
  Form,
  Input,
  Tabs,
  Tag,
  Spin,
  message,
  Row,
  Col,
  Statistic,
  Timeline,
  List,
  Badge,
  Progress,
} from 'antd';
import {
  AppstoreOutlined,
  QuestionCircleOutlined,
  AlertOutlined,
  FileSearchOutlined,
  DatabaseOutlined,
  BarChartOutlined,
  DownloadOutlined,
  ExportOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  BulbOutlined,
  TrophyOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import TransactionService from '../../services/transactionService';
import ReportService from '../../services/reportService';
import AIService from '../../services/aiService';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;
const { RangePicker } = DatePicker;

export default function AIHelper() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('classify');
  
  // Data state
  const [transactions, setTransactions] = useState([]);
  const [reports, setReports] = useState([]);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [selectedReports, setSelectedReports] = useState([]);
  const [loadingData, setLoadingData] = useState(false);
  
  // Form instances
  const [classifyForm] = Form.useForm();
  const [questionForm] = Form.useForm();
  const [anomalyForm] = Form.useForm();
  const [insightForm] = Form.useForm();

  // Load data on component mount
  useEffect(() => {
    loadTransactionsAndReports();
  }, []);

  const loadTransactionsAndReports = async () => {
    setLoadingData(true);
    try {
      // Load recent transactions using the correct method
      const transactionData = await TransactionService.getAll();
      
      // Handle different response formats
      const txnList = transactionData.data || transactionData || [];
      setTransactions(Array.isArray(txnList) ? txnList : []);

      // Load available reports
      const reportData = await ReportService.getReports({
        page: 0,
        size: 50
      });
      
      // Handle different response formats from backend
      const reportList = reportData.data || reportData.content || reportData || [];
      setReports(Array.isArray(reportList) ? reportList : []);
      
    } catch (error) {
      console.error('Error loading data:', error);
      message.error('Failed to load transactions and reports: ' + error.message);
      // Set empty arrays as fallback
      setTransactions([]);
      setReports([]);
    } finally {
      setLoadingData(false);
    }
  };

  // Enhanced AI result display component
  const AIResultDisplay = ({ result }) => {
    if (!result) return null;

    // Handle different types of AI results
    const renderFinancialQA = (data) => (
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Title level={4}>
              <QuestionCircleOutlined style={{ color: '#1890ff' }} /> Financial Q&A Result
            </Title>
            <Paragraph>
              <Text strong>Question: </Text>
              <Text italic>"{data.question}"</Text>
            </Paragraph>
          </div>
          
          <Alert
            message="AI Answer"
            description={
              <div style={{ fontSize: '16px', lineHeight: '1.6' }}>
                {data.answer?.answer || data.answer || 'No answer available'}
              </div>
            }
            type={data.answer?.status === 'error' ? 'error' : 'info'}
            showIcon
          />
          
          <Row gutter={16}>
            <Col span={8}>
              <Statistic
                title="Confidence"
                value={data.answer?.confidence || 'Medium'}
                prefix={<TrophyOutlined />}
              />
            </Col>
            <Col span={8}>
              <Statistic
                title="Context Used"
                value={data.context}
                prefix={<DatabaseOutlined />}
              />
            </Col>
            <Col span={8}>
              <Statistic
                title="Response Time"
                value={dayjs(data.answer?.timestamp).format('HH:mm:ss')}
                prefix={<InfoCircleOutlined />}
              />
            </Col>
          </Row>
        </Space>
      </Card>
    );

    const renderClassification = (data) => (
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>
            <AppstoreOutlined style={{ color: '#52c41a' }} /> Transaction Classification
          </Title>
          
          {data.classification && (
            <div>
              <Row gutter={16}>
                <Col span={12}>
                  <Card size="small" title="Primary Category">
                    <Tag color="green" style={{ fontSize: '14px' }}>
                      {data.classification.category || 'GENERAL_EXPENSE'}
                    </Tag>
                    <br />
                    <Progress 
                      percent={Math.round((data.classification.confidence || 0.5) * 100)} 
                      size="small" 
                      style={{ marginTop: 8 }}
                    />
                    <Text type="secondary">Confidence: {data.classification.confidence || 0.5}</Text>
                  </Card>
                </Col>
                <Col span={12}>
                  <Card size="small" title="Review Required">
                    <Badge 
                      status={data.classification.requireReview ? 'error' : 'success'} 
                      text={data.classification.requireReview ? 'Yes' : 'No'}
                    />
                  </Card>
                </Col>
              </Row>
              
              {data.classification.reason && (
                <Alert
                  message="Classification Reason"
                  description={data.classification.reason}
                  type="info"
                  showIcon
                />
              )}
              
              {data.classification.alternativeCategories && (
                <div>
                  <Text strong>Alternative Categories: </Text>
                  {data.classification.alternativeCategories.map(cat => (
                    <Tag key={cat} color="blue">{cat}</Tag>
                  ))}
                </div>
              )}
            </div>
          )}
        </Space>
      </Card>
    );

    const renderAnomalyDetection = (data) => {
      const isArray = Array.isArray(data);
      const anomalies = isArray ? data : [data];
      
      return (
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Title level={4}>
              <AlertOutlined style={{ color: '#faad14' }} /> Anomaly Detection Results
            </Title>
            
            {isArray && (
              <Row gutter={16}>
                <Col span={8}>
                  <Statistic
                    title="Total Analyzed"
                    value={anomalies.length}
                    prefix={<DatabaseOutlined />}
                  />
                </Col>
                <Col span={8}>
                  <Statistic
                    title="Anomalies Found"
                    value={anomalies.filter(a => a.anomaly?.anomalous || a.anomaly?.isAnomalous).length}
                    prefix={<WarningOutlined />}
                    valueStyle={{ color: '#cf1322' }}
                  />
                </Col>
                <Col span={8}>
                  <Statistic
                    title="Normal Transactions"
                    value={anomalies.filter(a => !(a.anomaly?.anomalous || a.anomaly?.isAnomalous)).length}
                    prefix={<CheckCircleOutlined />}
                    valueStyle={{ color: '#3f8600' }}
                  />
                </Col>
              </Row>
            )}
            
            <List
              dataSource={anomalies}
              renderItem={(item, index) => {
                const anomaly = item.anomaly || item;
                const isAnomalous = anomaly.anomalous || anomaly.isAnomalous;
                
                return (
                  <List.Item>
                    <Card
                      size="small"
                      style={{ width: '100%' }}
                      title={
                        <Space>
                          {isAnomalous ? 
                            <WarningOutlined style={{ color: '#faad14' }} /> : 
                            <CheckCircleOutlined style={{ color: '#52c41a' }} />
                          }
                          <Text>{item.transaction || `Transaction ${index + 1}`}</Text>
                          <Tag color={isAnomalous ? 'red' : 'green'}>
                            {isAnomalous ? 'ANOMALY' : 'NORMAL'}
                          </Tag>
                        </Space>
                      }
                    >
                      <Row gutter={16}>
                        <Col span={6}>
                          <Text strong>Amount: </Text>
                          <Text>{item.amount || 'N/A'}</Text>
                        </Col>
                        <Col span={6}>
                          <Text strong>Score: </Text>
                          <Text>{anomaly.anomalyScore || 'N/A'}</Text>
                        </Col>
                        <Col span={6}>
                          <Text strong>Type: </Text>
                          <Text>{anomaly.anomalyType || 'General'}</Text>
                        </Col>
                        <Col span={6}>
                          <Progress 
                            percent={Math.round((anomaly.anomalyScore || 0) * 100)} 
                            size="small"
                            strokeColor={isAnomalous ? '#ff4d4f' : '#52c41a'}
                          />
                        </Col>
                      </Row>
                      
                      {anomaly.recommendations && anomaly.recommendations.length > 0 && (
                        <div style={{ marginTop: 8 }}>
                          <Text strong>Recommendations: </Text>
                          <ul>
                            {anomaly.recommendations.map((rec, i) => (
                              <li key={i}><Text type="secondary">{rec}</Text></li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </Card>
                  </List.Item>
                );
              }}
            />
          </Space>
        </Card>
      );
    };

    const renderReportInsights = (data) => (
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>
            <FileSearchOutlined style={{ color: '#722ed1' }} /> Report Insights
          </Title>
          
          {data.insights && (
            <>
              <Alert
                message="Executive Summary"
                description={data.summary || 'AI analysis completed successfully'}
                type="info"
                showIcon
              />
              
              <div>
                <Title level={5}>
                  <BulbOutlined /> Key Insights
                </Title>
                <Timeline>
                  {(data.insights.insights || data.insights || []).map((insight, index) => (
                    <Timeline.Item key={index} color="blue">
                      <Text>{insight}</Text>
                    </Timeline.Item>
                  ))}
                </Timeline>
              </div>
              
              {data.recommendations && data.recommendations.length > 0 && (
                <div>
                  <Title level={5}>
                    <TrophyOutlined /> Recommendations
                  </Title>
                  <List
                    dataSource={data.recommendations}
                    renderItem={item => (
                      <List.Item>
                        <List.Item.Meta
                          avatar={<BulbOutlined style={{ color: '#1890ff' }} />}
                          description={item}
                        />
                      </List.Item>
                    )}
                  />
                </div>
              )}
            </>
          )}
        </Space>
      </Card>
    );

    // Determine result type and render accordingly
    if (result.question) {
      return renderFinancialQA(result);
    } else if (result.classification) {
      return renderClassification(result);
    } else if (Array.isArray(result) || result.anomaly) {
      return renderAnomalyDetection(result);
    } else if (result.insights) {
      return renderReportInsights(result);
    } else {
      // Fallback to enhanced JSON display
      return (
        <Card>
          <Title level={4}>
            <InfoCircleOutlined /> AI Analysis Result
          </Title>
          <Alert
            message="Raw AI Response"
            description={
              <pre style={{ 
                background: '#f5f5f5', 
                padding: '12px', 
                borderRadius: '4px',
                maxHeight: '300px',
                overflow: 'auto'
              }}>
                {JSON.stringify(result, null, 2)}
              </pre>
            }
            type="info"
          />
        </Card>
      );
    }
  };

  // Handle AI action selection
  const handleAIAction = (action) => {
    setActiveTab(action);
    setModalVisible(true);
  };

  // Execute transaction classification
  const executeClassification = async (values) => {
    setLoading(true);
    try {
      const selectedTxns = transactions.filter(t => selectedTransactions.includes(t.transactionId));
      
      if (selectedTxns.length === 0) {
        message.warning('Please select at least one transaction');
        return;
      }

      const results = [];
      
      for (const txn of selectedTxns) {
        try {
          const res = await AIService.classifyTransaction({
            description: txn.description,
            amount: txn.amount,
            currency: txn.currency || 'CNY',
            transactionType: txn.transactionType,
          });
          
          results.push({
            transactionId: txn.transactionId,
            transaction: txn.description,
            classification: res.data || res
          });
        } catch (error) {
          console.error(`Classification failed for transaction ${txn.transactionId}:`, error);
          results.push({
            transactionId: txn.transactionId,
            transaction: txn.description,
            error: error.message
          });
        }
      }
      
      setResult(results);
      setModalVisible(false);
      message.success(`Classified ${selectedTxns.length} transactions`);
    } catch (error) {
      console.error('Transaction classification failed:', error);
      setResult({ error: 'Transaction classification failed. Please check the backend service.' });
    } finally {
      setLoading(false);
    }
  };

  // Execute financial Q&A with enhanced context
  const executeFinancialQA = async (values) => {
    setLoading(true);
    try {
      const selectedTxns = transactions.filter(t => selectedTransactions.includes(t.transactionId));
      
      // Enhanced context building
      let context = '';
      if (selectedTxns.length > 0) {
        context = selectedTxns.map(t => 
          `Date: ${t.transactionDate}, Description: ${t.description}, Amount: ${t.currency || 'CNY'} ${t.amount}, Type: ${t.transactionType}, Category: ${t.category || 'N/A'}`
        ).join('\n');
      } else {
        // If no transactions selected, build context from all available data
        context = `Total transactions available: ${transactions.length}. Recent transactions summary: ` +
          transactions.slice(0, 5).map(t => 
            `${t.description} (${t.amount})`
          ).join(', ');
      }

      const res = await AIService.askFinancialQuestion({
        question: values.question,
        context: context,
        companyId: 1, // TODO: Get from user context
      });
      
      const result = {
        question: values.question,
        context: selectedTxns.length > 0 ? `${selectedTxns.length} selected transactions` : `${transactions.length} total transactions`,
        answer: res.data || res
      };
      
      setResult(result);
      setModalVisible(false);
      message.success('Financial question answered');
    } catch (error) {
      console.error('Financial Q&A failed:', error);
      setResult({ 
        question: values.question,
        answer: { 
          status: 'error', 
          answer: 'Financial Q&A failed. Please check the backend service.',
          confidence: 'low'
        }
      });
    } finally {
      setLoading(false);
    }
  };

  // Execute anomaly detection
  const executeAnomalyDetection = async (values) => {
    setLoading(true);
    try {
      const selectedTxns = transactions.filter(t => selectedTransactions.includes(t.transactionId));
      
      if (selectedTxns.length === 0) {
        message.warning('Please select at least one transaction');
        return;
      }

      const results = [];
      let anomalyCount = 0;
      
      for (const txn of selectedTxns) {
        try {
          const res = await AIService.detectAnomaly({
            description: txn.description,
            amount: txn.amount,
            currency: txn.currency || 'CNY',
            transactionDate: txn.transactionDate,
            transactionType: txn.transactionType,
          });
          
          const anomalyResult = res.data || res;
          results.push({
            transactionId: txn.transactionId,
            transaction: txn.description,
            amount: txn.amount,
            anomaly: anomalyResult
          });
          
          if (anomalyResult.isAnomalous || anomalyResult.anomalous) {
            anomalyCount++;
          }
        } catch (error) {
          console.error(`Anomaly detection failed for transaction ${txn.transactionId}:`, error);
          results.push({
            transactionId: txn.transactionId,
            transaction: txn.description,
            error: error.message
          });
        }
      }
      
      setResult(results);
      setModalVisible(false);
      message.success(`Analyzed ${selectedTxns.length} transactions, found ${anomalyCount} anomalies`);
    } catch (error) {
      console.error('Anomaly detection failed:', error);
      setResult({ error: 'Anomaly detection failed. Please check the backend service.' });
    } finally {
      setLoading(false);
    }
  };

  // Execute report insights
  const executeReportInsights = async (values) => {
    setLoading(true);
    try {
      const selectedReportList = reports.filter(r => selectedReports.includes(r.reportId));
      
      if (selectedReportList.length === 0) {
        message.warning('Please select at least one report');
        return;
      }

      const results = [];
      
      for (const report of selectedReportList) {
        try {
          const res = await AIService.reportInsight({
            reportData: report.content || JSON.stringify(report),
            reportType: report.reportType || 'FINANCIAL_REPORT',
            reportId: report.reportId
          });
          
          results.push({
            reportId: report.reportId,
            reportName: report.reportName,
            insights: res.data || res
          });
        } catch (error) {
          console.error(`Report insight failed for report ${report.reportId}:`, error);
          results.push({
            reportId: report.reportId,
            reportName: report.reportName,
            error: error.message
          });
        }
      }
      
      setResult(results.length === 1 ? results[0] : results);
      setModalVisible(false);
      message.success(`Analyzed ${selectedReportList.length} reports`);
    } catch (error) {
      console.error('Report insights failed:', error);
      setResult({ error: 'Report insights failed. Please check the backend service.' });
    } finally {
      setLoading(false);
    }
  };

  // Export analysis results
  const exportAnalysisResults = (results, fileName) => {
    try {
      const jsonString = JSON.stringify(results, null, 2);
      const blob = new Blob([jsonString], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${fileName}_${dayjs().format('YYYYMMDD_HHmmss')}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message.success('Analysis results exported successfully');
    } catch (error) {
      console.error('Export error:', error);
      message.error('Failed to export analysis results');
    }
  };

  // Clear selections
  const clearSelections = () => {
    setSelectedTransactions([]);
    setSelectedReports([]);
  };

  // Transaction selection columns
  const transactionColumns = [
    {
      title: 'Select',
      dataIndex: 'transactionId',
      width: 60,
      render: (id, record) => (
        <Checkbox
          checked={selectedTransactions.includes(id)}
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedTransactions([...selectedTransactions, id]);
            } else {
              setSelectedTransactions(selectedTransactions.filter(tid => tid !== id));
            }
          }}
        />
      ),
    },
    {
      title: 'Date',
      dataIndex: 'transactionDate',
      width: 100,
      render: (date) => dayjs(date).format('MM-DD'),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      ellipsis: true,
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      width: 120,
      render: (amount, record) => `${record.currency || 'CNY'} ${amount}`,
    },
    {
      title: 'Type',
      dataIndex: 'transactionType',
      width: 80,
      render: (type) => (
        <Tag color={type === 'INCOME' ? 'green' : 'red'}>
          {type}
        </Tag>
      ),
    },
  ];

  // Report selection columns
  const reportColumns = [
    {
      title: 'Select',
      dataIndex: 'reportId',
      width: 60,
      render: (id, record) => (
        <Checkbox
          checked={selectedReports.includes(id)}
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedReports([...selectedReports, id]);
            } else {
              setSelectedReports(selectedReports.filter(rid => rid !== id));
            }
          }}
        />
      ),
    },
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      ellipsis: true,
    },
    {
      title: 'Type',
      dataIndex: 'reportType',
      width: 120,
      render: (type) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      width: 100,
      render: (date) => dayjs(date).format('MM-DD'),
    },
  ];

  return (
    <Card style={{ margin: 24 }}>
      <Title level={4}>🤖 AI Financial Assistant</Title>
      <Text type="secondary">
        Select transactions and reports to get AI-powered insights and analysis
      </Text>

      <Divider />

      <Space wrap size="large">
        <Button
          type="primary"
          icon={<AppstoreOutlined />}
          onClick={() => handleAIAction('classify')}
          size="large"
        >
          Classify Transactions
        </Button>
        <Button
          icon={<QuestionCircleOutlined />}
          onClick={() => handleAIAction('question')}
          size="large"
        >
          Financial Q&A
        </Button>
        <Button
          icon={<AlertOutlined />}
          onClick={() => handleAIAction('anomaly')}
          size="large"
        >
          Anomaly Detection
        </Button>
        <Button
          icon={<FileSearchOutlined />}
          onClick={() => handleAIAction('insight')}
          size="large"
        >
          Report Insights
        </Button>
        <Button
          icon={<DatabaseOutlined />}
          onClick={loadTransactionsAndReports}
          loading={loadingData}
        >
          Refresh Data
        </Button>
      </Space>

      <Divider />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={5} style={{ margin: 0 }}>Analysis Results</Title>
        {result && (
          <Space>
            <Button
              icon={<ExportOutlined />}
              onClick={() => exportAnalysisResults(result, 'ai_analysis')}
            >
              Export Results
            </Button>
            <Button onClick={() => setResult(null)}>Clear</Button>
          </Space>
        )}
      </div>

      {result ? (
        <AIResultDisplay result={result} />
      ) : (
        <Alert
          message="No Analysis Results"
          description="Click on the buttons above to start AI analysis"
          type="info"
          showIcon
        />
      )}

      {/* Modal for AI Actions */}
      <Modal
        title="AI Analysis Configuration"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={1000}
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab}>
          <TabPane tab="Classify Transactions" key="classify">
            <Form form={classifyForm} onFinish={executeClassification} layout="vertical">
              <div style={{ marginBottom: 16 }}>
                <Space>
                  <Title level={5} style={{ margin: 0 }}>
                    <DatabaseOutlined /> Select Transactions
                  </Title>
                  <Tag color="blue">Total: {transactions.length}</Tag>
                  <Button size="small" onClick={clearSelections}>Clear Selection</Button>
                </Space>
              </div>
              <Table
                dataSource={transactions}
                columns={transactionColumns}
                rowKey="transactionId"
                size="small"
                scroll={{ y: 300 }}
                pagination={{ pageSize: 10, simple: true }}
              />
              <div style={{ marginTop: 16, textAlign: 'right' }}>
                <Space>
                  <Text>Selected: {selectedTransactions.length} transactions</Text>
                  <Button 
                    type="primary" 
                    htmlType="submit" 
                    loading={loading}
                    disabled={selectedTransactions.length === 0}
                  >
                    Classify Selected
                  </Button>
                </Space>
              </div>
            </Form>
          </TabPane>

          <TabPane tab="Financial Q&A" key="question">
            <Form form={questionForm} onFinish={executeFinancialQA} layout="vertical">
              <Form.Item
                name="question"
                label="Ask a Financial Question"
                rules={[{ required: true, message: 'Please enter your question' }]}
              >
                <TextArea 
                  rows={3} 
                  placeholder="e.g., What were my highest expense categories this month? What's the total revenue this quarter?"
                />
              </Form.Item>
              
              <div style={{ marginBottom: 16 }}>
                <Space>
                  <Title level={5} style={{ margin: 0 }}>
                    <DatabaseOutlined /> Select Transaction Context (Optional)
                  </Title>
                  <Tag color="blue">Total: {transactions.length}</Tag>
                  <Button size="small" onClick={clearSelections}>Clear Selection</Button>
                </Space>
                <Text type="secondary" style={{ display: 'block', marginTop: 4 }}>
                  Leave empty to analyze all available data, or select specific transactions for focused analysis
                </Text>
              </div>
              <Table
                dataSource={transactions}
                columns={transactionColumns}
                rowKey="transactionId"
                size="small"
                scroll={{ y: 300 }}
                pagination={{ pageSize: 10, simple: true }}
              />
              <div style={{ marginTop: 16, textAlign: 'right' }}>
                <Space>
                  <Text>Context: {selectedTransactions.length > 0 ? `${selectedTransactions.length} selected transactions` : 'All available data'}</Text>
                  <Button 
                    type="primary" 
                    htmlType="submit" 
                    loading={loading}
                  >
                    Ask AI
                  </Button>
                </Space>
              </div>
            </Form>
          </TabPane>

          <TabPane tab="Anomaly Detection" key="anomaly">
            <Form form={anomalyForm} onFinish={executeAnomalyDetection} layout="vertical">
              <div style={{ marginBottom: 16 }}>
                <Space>
                  <Title level={5} style={{ margin: 0 }}>
                    <DatabaseOutlined /> Select Transactions to Analyze
                  </Title>
                  <Tag color="blue">Total: {transactions.length}</Tag>
                  <Button size="small" onClick={clearSelections}>Clear Selection</Button>
                </Space>
              </div>
              <Table
                dataSource={transactions}
                columns={transactionColumns}
                rowKey="transactionId"
                size="small"
                scroll={{ y: 300 }}
                pagination={{ pageSize: 10, simple: true }}
              />
              <div style={{ marginTop: 16, textAlign: 'right' }}>
                <Space>
                  <Text>Selected: {selectedTransactions.length} transactions</Text>
                  <Button 
                    type="primary" 
                    htmlType="submit" 
                    loading={loading}
                    disabled={selectedTransactions.length === 0}
                  >
                    Detect Anomalies
                  </Button>
                </Space>
              </div>
            </Form>
          </TabPane>

          <TabPane tab="Report Insights" key="insight">
            <Form form={insightForm} onFinish={executeReportInsights} layout="vertical">
              <div style={{ marginBottom: 16 }}>
                <Space>
                  <Title level={5} style={{ margin: 0 }}>
                    <FileSearchOutlined /> Select Reports to Analyze
                  </Title>
                  <Tag color="blue">Total: {reports.length}</Tag>
                  <Button size="small" onClick={clearSelections}>Clear Selection</Button>
                </Space>
              </div>
              <Table
                dataSource={reports}
                columns={reportColumns}
                rowKey="reportId"
                size="small"
                scroll={{ y: 300 }}
                pagination={{ pageSize: 10, simple: true }}
              />
              <div style={{ marginTop: 16, textAlign: 'right' }}>
                <Space>
                  <Text>Selected: {selectedReports.length} reports</Text>
                  <Button 
                    type="primary" 
                    htmlType="submit" 
                    loading={loading}
                    disabled={selectedReports.length === 0}
                  >
                    Generate Insights
                  </Button>
                </Space>
              </div>
            </Form>
          </TabPane>
        </Tabs>
      </Modal>
    </Card>
  );
}