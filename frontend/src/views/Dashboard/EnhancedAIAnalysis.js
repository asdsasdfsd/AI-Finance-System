// frontend/src/views/Dashboard/EnhancedAIAnalysis.js
import React, { useState } from 'react';
import {
  Card,
  Typography,
  Space,
  Button,
  DatePicker,
  Select,
  Form,
  Row,
  Col,
  Alert,
  Table,
  Checkbox,
  Tag,
  Spin,
  Progress,
  Descriptions,
  Input,
  message
} from 'antd';
import {
  RobotOutlined,
  FilterOutlined,
  AnalyticsOutlined,
  ExperimentOutlined,
  TrendingUpOutlined,
  SafetyOutlined,
  CompareOutlined,
  GlobalOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import AuthService from '../../services/authService';

const { Title, Text, Paragraph } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

export default function EnhancedAIAnalysis() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [dataLoading, setDataLoading] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [dataSummary, setDataSummary] = useState(null);
  const [availableTransactions, setAvailableTransactions] = useState([]);
  const [availableReports, setAvailableReports] = useState([]);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [selectedReports, setSelectedReports] = useState([]);
  const [analysisStep, setAnalysisStep] = useState('data_selection'); // data_selection, analysis_running, results
  
  const currentUser = AuthService.getCurrentUser();
  const companyId = currentUser?.companyId || 1;

  // Analysis type options
  const analysisTypes = [
    { value: 'FINANCIAL_INSIGHTS', label: 'Financial Insights', icon: <AnalyticsOutlined />, description: 'Get comprehensive financial health insights' },
    { value: 'TREND_ANALYSIS', label: 'Trend Analysis', icon: <TrendingUpOutlined />, description: 'Identify financial trends and patterns' },
    { value: 'ANOMALY_DETECTION', label: 'Anomaly Detection', icon: <SafetyOutlined />, description: 'Detect unusual transactions and potential risks' },
    { value: 'COMPARATIVE_ANALYSIS', label: 'Comparative Analysis', icon: <CompareOutlined />, description: 'Compare performance across periods and categories' },
    { value: 'COMPREHENSIVE', label: 'Comprehensive Analysis', icon: <GlobalOutlined />, description: 'Complete financial analysis with all insights' }
  ];

  // Load available data when filters change
  const loadAvailableData = async (values) => {
    if (!values.dateRange || values.dateRange.length !== 2) return;
    
    setDataLoading(true);
    try {
      const filterCommand = {
        companyId: companyId,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
        departmentId: values.departmentId || null
      };

      // Call backend API to get data summary
      const response = await fetch('/api/ai-analysis/data-summary', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${currentUser.token}`
        },
        body: JSON.stringify(filterCommand)
      });

      if (response.ok) {
        const summary = await response.json();
        setDataSummary(summary);
        
        // Load transactions and reports for selection
        await loadTransactionData(filterCommand);
        await loadReportData(filterCommand);
      } else {
        message.error('Failed to load available data');
      }
    } catch (error) {
      console.error('Error loading available data:', error);
      message.error('Error loading available data');
    } finally {
      setDataLoading(false);
    }
  };

  const loadTransactionData = async (filterCommand) => {
    try {
      const response = await fetch('/api/ai-analysis/transactions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${currentUser.token}`
        },
        body: JSON.stringify(filterCommand)
      });

      if (response.ok) {
        const transactions = await response.json();
        setAvailableTransactions(transactions);
      }
    } catch (error) {
      console.error('Error loading transaction data:', error);
    }
  };

  const loadReportData = async (filterCommand) => {
    try {
      const response = await fetch('/api/ai-analysis/reports', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${currentUser.token}`
        },
        body: JSON.stringify(filterCommand)
      });

      if (response.ok) {
        const reports = await response.json();
        setAvailableReports(reports);
      }
    } catch (error) {
      console.error('Error loading report data:', error);
    }
  };

  // Perform AI analysis
  const performAnalysis = async () => {
    const values = form.getFieldsValue();
    
    if (!values.dateRange || !values.analysisType) {
      message.error('Please select date range and analysis type');
      return;
    }

    if (!dataSummary?.dataSufficient) {
      message.error('Insufficient data for meaningful AI analysis');
      return;
    }

    setLoading(true);
    setAnalysisStep('analysis_running');
    
    try {
      const requestCommand = {
        companyId: companyId,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
        analysisType: values.analysisType,
        departmentId: values.departmentId || null,
        selectedTransactionIds: selectedTransactions,
        selectedReportIds: selectedReports,
        categoryFilter: values.categoryFilter || [],
        minAmount: values.minAmount,
        maxAmount: values.maxAmount
      };

      const response = await fetch('/api/ai-analysis/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${currentUser.token}`
        },
        body: JSON.stringify(requestCommand)
      });

      if (response.ok) {
        const result = await response.json();
        setAnalysisResult(result);
        setAnalysisStep('results');
        message.success('AI analysis completed successfully!');
      } else {
        const error = await response.text();
        message.error(`Analysis failed: ${error}`);
      }
    } catch (error) {
      console.error('Error performing AI analysis:', error);
      message.error('Error performing AI analysis');
    } finally {
      setLoading(false);
    }
  };

  // Transaction table columns
  const transactionColumns = [
    {
      title: 'Select',
      dataIndex: 'transactionId',
      width: 60,
      render: (id) => (
        <Checkbox
          checked={selectedTransactions.includes(id)}
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedTransactions([...selectedTransactions, id]);
            } else {
              setSelectedTransactions(selectedTransactions.filter(t => t !== id));
            }
          }}
        />
      )
    },
    {
      title: 'Date',
      dataIndex: 'transactionDate',
      render: (date) => dayjs(date).format('YYYY-MM-DD')
    },
    {
      title: 'Description',
      dataIndex: 'description',
      ellipsis: true
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      render: (amount) => `$${amount.toLocaleString()}`
    },
    {
      title: 'Type',
      dataIndex: 'transactionType',
      render: (type) => (
        <Tag color={type === 'INCOME' ? 'green' : 'red'}>
          {type}
        </Tag>
      )
    },
    {
      title: 'Category',
      dataIndex: 'category',
      ellipsis: true
    }
  ];

  // Report table columns
  const reportColumns = [
    {
      title: 'Select',
      dataIndex: 'reportId',
      width: 60,
      render: (id) => (
        <Checkbox
          checked={selectedReports.includes(id)}
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedReports([...selectedReports, id]);
            } else {
              setSelectedReports(selectedReports.filter(r => r !== id));
            }
          }}
        />
      )
    },
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      ellipsis: true
    },
    {
      title: 'Type',
      dataIndex: 'reportType',
      render: (type) => <Tag>{type.replace('_', ' ')}</Tag>
    },
    {
      title: 'Period',
      dataIndex: 'periodDescription'
    },
    {
      title: 'Content Size',
      dataIndex: 'contentSize',
      render: (size) => `${(size / 1024).toFixed(1)} KB`
    },
    {
      title: 'AI Ready',
      dataIndex: 'hasContent',
      render: (hasContent) => (
        <Tag color={hasContent ? 'green' : 'red'}>
          {hasContent ? 'Yes' : 'No'}
        </Tag>
      )
    }
  ];

  const resetAnalysis = () => {
    setAnalysisStep('data_selection');
    setAnalysisResult(null);
    setSelectedTransactions([]);
    setSelectedReports([]);
  };

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* Header */}
          <div>
            <Title level={3}>
              <RobotOutlined style={{ marginRight: 8, color: '#1890ff' }} />
              Enhanced AI Financial Analysis
            </Title>
            <Paragraph type="secondary">
              Select time period, transactions, and reports for intelligent financial analysis.
              Choose from multiple analysis types to get comprehensive insights.
            </Paragraph>
          </div>

          {/* Step 1: Data Selection */}
          {analysisStep === 'data_selection' && (
            <>
              <Card title={<><FilterOutlined /> Data Selection & Filters</>} size="small">
                <Form
                  form={form}
                  layout="vertical"
                  onValuesChange={(_, allValues) => {
                    if (allValues.dateRange && allValues.dateRange.length === 2) {
                      loadAvailableData(allValues);
                    }
                  }}
                >
                  <Row gutter={16}>
                    <Col span={8}>
                      <Form.Item
                        name="dateRange"
                        label="Analysis Period"
                        rules={[{ required: true, message: 'Please select date range' }]}
                      >
                        <RangePicker
                          style={{ width: '100%' }}
                          format="YYYY-MM-DD"
                          placeholder={['Start Date', 'End Date']}
                        />
                      </Form.Item>
                    </Col>
                    
                    <Col span={6}>
                      <Form.Item
                        name="departmentId"
                        label="Department Filter"
                      >
                        <Select placeholder="All Departments" allowClear>
                          <Option value={1}>Finance</Option>
                          <Option value={2}>Operations</Option>
                          <Option value={3}>Marketing</Option>
                          <Option value={4}>HR</Option>
                        </Select>
                      </Form.Item>
                    </Col>
                    
                    <Col span={5}>
                      <Form.Item
                        name="minAmount"
                        label="Min Amount"
                      >
                        <Input placeholder="0" type="number" prefix="$" />
                      </Form.Item>
                    </Col>
                    
                    <Col span={5}>
                      <Form.Item
                        name="maxAmount"
                        label="Max Amount"
                      >
                        <Input placeholder="No limit" type="number" prefix="$" />
                      </Form.Item>
                    </Col>
                  </Row>
                  
                  <Row gutter={16}>
                    <Col span={12}>
                      <Form.Item
                        name="analysisType"
                        label="Analysis Type"
                        rules={[{ required: true, message: 'Please select analysis type' }]}
                      >
                        <Select placeholder="Select analysis type">
                          {analysisTypes.map(type => (
                            <Option key={type.value} value={type.value}>
                              <Space>
                                {type.icon}
                                <span>{type.label}</span>
                              </Space>
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                    </Col>
                    
                    <Col span={12}>
                      <Form.Item
                        name="categoryFilter"
                        label="Category Filter"
                      >
                        <Select mode="multiple" placeholder="All Categories" allowClear>
                          <Option value="TRAVEL_EXPENSE">Travel</Option>
                          <Option value="FOOD_EXPENSE">Food & Dining</Option>
                          <Option value="OFFICE_SUPPLIES">Office Supplies</Option>
                          <Option value="MARKETING_EXPENSE">Marketing</Option>
                          <Option value="UTILITIES">Utilities</Option>
                          <Option value="RENT">Rent</Option>
                        </Select>
                      </Form.Item>
                    </Col>
                  </Row>
                </Form>
              </Card>

              {/* Data Summary */}
              {dataSummary && (
                <Card title="Available Data Summary" size="small" loading={dataLoading}>
                  <Row gutter={16}>
                    <Col span={6}>
                      <Descriptions column={1} size="small">
                        <Descriptions.Item label="Transactions">
                          <Text strong>{dataSummary.totalTransactions}</Text>
                        </Descriptions.Item>
                        <Descriptions.Item label="Reports">
                          <Text strong>{dataSummary.totalReports}</Text>
                        </Descriptions.Item>
                      </Descriptions>
                    </Col>
                    
                    <Col span={6}>
                      <Descriptions column={1} size="small">
                        <Descriptions.Item label="Total Income">
                          <Text style={{ color: '#52c41a' }}>
                            ${dataSummary.transactionSummary?.totalIncome?.toLocaleString() || 0}
                          </Text>
                        </Descriptions.Item>
                        <Descriptions.Item label="Total Expense">
                          <Text style={{ color: '#ff4d4f' }}>
                            ${dataSummary.transactionSummary?.totalExpense?.toLocaleString() || 0}
                          </Text>
                        </Descriptions.Item>
                      </Descriptions>
                    </Col>
                    
                    <Col span={6}>
                      <Descriptions column={1} size="small">
                        <Descriptions.Item label="Net Amount">
                          <Text strong style={{ 
                            color: dataSummary.transactionSummary?.netAmount >= 0 ? '#52c41a' : '#ff4d4f' 
                          }}>
                            ${dataSummary.transactionSummary?.netAmount?.toLocaleString() || 0}
                          </Text>
                        </Descriptions.Item>
                        <Descriptions.Item label="Analysis Complexity">
                          <Tag color="blue">{dataSummary.estimatedComplexity}</Tag>
                        </Descriptions.Item>
                      </Descriptions>
                    </Col>
                    
                    <Col span={6}>
                      <Descriptions column={1} size="small">
                        <Descriptions.Item label="Data Sufficient">
                          <Tag color={dataSummary.dataSufficient ? 'green' : 'red'}>
                            {dataSummary.dataSufficient ? 'Yes' : 'No'}
                          </Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="Available Analysis">
                          <Text>{dataSummary.availableAnalysisTypes?.length || 0} types</Text>
                        </Descriptions.Item>
                      </Descriptions>
                    </Col>
                  </Row>
                  
                  {!dataSummary.dataSufficient && (
                    <Alert
                      message="Insufficient Data"
                      description="The selected period doesn't have enough data for meaningful AI analysis. Please select a longer period or ensure there are more transactions."
                      type="warning"
                      showIcon
                      style={{ marginTop: 16 }}
                    />
                  )}
                </Card>
              )}

              {/* Transaction Selection */}
              {availableTransactions.length > 0 && (
                <Card 
                  title={
                    <Space>
                      <span>Available Transactions</span>
                      <Tag>{availableTransactions.length} total</Tag>
                      <Tag color="blue">{selectedTransactions.length} selected</Tag>
                    </Space>
                  }
                  size="small"
                  extra={
                    <Space>
                      <Button
                        size="small"
                        onClick={() => setSelectedTransactions(availableTransactions.map(t => t.transactionId))}
                      >
                        Select All
                      </Button>
                      <Button
                        size="small"
                        onClick={() => setSelectedTransactions([])}
                      >
                        Clear All
                      </Button>
                    </Space>
                  }
                >
                  <Table
                    dataSource={availableTransactions}
                    columns={transactionColumns}
                    rowKey="transactionId"
                    size="small"
                    pagination={{ pageSize: 10, showSizeChanger: true }}
                    scroll={{ x: 800 }}
                  />
                </Card>
              )}

              {/* Report Selection */}
              {availableReports.length > 0 && (
                <Card 
                  title={
                    <Space>
                      <span>Available Reports</span>
                      <Tag>{availableReports.length} total</Tag>
                      <Tag color="blue">{selectedReports.length} selected</Tag>
                    </Space>
                  }
                  size="small"
                  extra={
                    <Space>
                      <Button
                        size="small"
                        onClick={() => setSelectedReports(availableReports.map(r => r.reportId))}
                      >
                        Select All
                      </Button>
                      <Button
                        size="small"
                        onClick={() => setSelectedReports([])}
                      >
                        Clear All
                      </Button>
                    </Space>
                  }
                >
                  <Table
                    dataSource={availableReports}
                    columns={reportColumns}
                    rowKey="reportId"
                    size="small"
                    pagination={{ pageSize: 5, showSizeChanger: true }}
                    scroll={{ x: 800 }}
                  />
                </Card>
              )}

              {/* Analysis Actions */}
              {dataSummary && (
                <Card size="small">
                  <Space>
                    <Button
                      type="primary"
                      size="large"
                      icon={<ExperimentOutlined />}
                      onClick={performAnalysis}
                      disabled={!dataSummary.dataSufficient || loading}
                      loading={loading}
                    >
                      Start AI Analysis
                    </Button>
                    
                    <Button
                      size="large"
                      onClick={() => {
                        form.resetFields();
                        setDataSummary(null);
                        setAvailableTransactions([]);
                        setAvailableReports([]);
                        setSelectedTransactions([]);
                        setSelectedReports([]);
                      }}
                    >
                      Reset Filters
                    </Button>
                  </Space>
                </Card>
              )}
            </>
          )}

          {/* Step 2: Analysis Running */}
          {analysisStep === 'analysis_running' && (
            <Card>
              <div style={{ textAlign: 'center', padding: '40px 0' }}>
                <Spin size="large" />
                <Title level={4} style={{ marginTop: 16 }}>
                  AI Analysis in Progress
                </Title>
                <Paragraph type="secondary">
                  Analyzing {selectedTransactions.length} transactions and {selectedReports.length} reports...
                </Paragraph>
                <Progress percent={loading ? 30 : 100} status={loading ? 'active' : 'success'} />
              </div>
            </Card>
          )}

          {/* Step 3: Analysis Results */}
          {analysisStep === 'results' && analysisResult && (
            <>
              <Card 
                title={
                  <Space>
                    <AnalyticsOutlined />
                    <span>AI Analysis Results</span>
                    <Tag color="green">{analysisResult.analysisType.replace('_', ' ')}</Tag>
                  </Space>
                }
                extra={
                  <Space>
                    <Button onClick={resetAnalysis}>New Analysis</Button>
                    <Button type="primary" onClick={() => {
                      // Export results functionality
                      const dataStr = JSON.stringify(analysisResult, null, 2);
                      const dataBlob = new Blob([dataStr], {type: 'application/json'});
                      const url = URL.createObjectURL(dataBlob);
                      const link = document.createElement('a');
                      link.href = url;
                      link.download = `ai-analysis-${dayjs().format('YYYY-MM-DD')}.json`;
                      link.click();
                      URL.revokeObjectURL(url);
                    }}>
                      Export Results
                    </Button>
                  </Space>
                }
              >
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  {/* Analysis Summary */}
                  <Row gutter={16}>
                    <Col span={6}>
                      <Card size="small">
                        <div style={{ textAlign: 'center' }}>
                          <Title level={4} style={{ margin: 0 }}>
                            {analysisResult.confidence}
                          </Title>
                          <Text type="secondary">Confidence Level</Text>
                        </div>
                      </Card>
                    </Col>
                    
                    <Col span={6}>
                      <Card size="small">
                        <div style={{ textAlign: 'center' }}>
                          <Title level={4} style={{ margin: 0 }}>
                            {analysisResult.dataPoints}
                          </Title>
                          <Text type="secondary">Data Points Analyzed</Text>
                        </div>
                      </Card>
                    </Col>
                    
                    <Col span={6}>
                      <Card size="small">
                        <div style={{ textAlign: 'center' }}>
                          <Title level={4} style={{ margin: 0 }}>
                            {analysisResult.insights?.length || 0}
                          </Title>
                          <Text type="secondary">Insights Generated</Text>
                        </div>
                      </Card>
                    </Col>
                    
                    <Col span={6}>
                      <Card size="small">
                        <div style={{ textAlign: 'center' }}>
                          <Title level={4} style={{ margin: 0 }}>
                            {dayjs(analysisResult.generatedAt).format('HH:mm')}
                          </Title>
                          <Text type="secondary">Generated At</Text>
                        </div>
                      </Card>
                    </Col>
                  </Row>

                  {/* Analysis Summary */}
                  <Alert
                    message="Analysis Summary"
                    description={analysisResult.summary}
                    type="info"
                    showIcon
                  />

                  {/* Insights */}
                  <Card title="AI Insights" size="small">
                    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                      {analysisResult.insights?.map((insight, index) => (
                        <Card key={index} size="small" style={{ backgroundColor: '#fafafa' }}>
                          <Space>
                            <Tag color="blue">Insight #{index + 1}</Tag>
                            <Text>{insight}</Text>
                          </Space>
                        </Card>
                      )) || (
                        <Text type="secondary">No specific insights generated.</Text>
                      )}
                    </Space>
                  </Card>

                  {/* Raw Results (for debugging) */}
                  <Card title="Technical Details" size="small">
                    <Descriptions column={2} size="small">
                      <Descriptions.Item label="Analysis Type">
                        {analysisResult.analysisType}
                      </Descriptions.Item>
                      <Descriptions.Item label="Generated At">
                        {dayjs(analysisResult.generatedAt).format('YYYY-MM-DD HH:mm:ss')}
                      </Descriptions.Item>
                      <Descriptions.Item label="Confidence">
                        {analysisResult.confidence}
                      </Descriptions.Item>
                      <Descriptions.Item label="Data Points">
                        {analysisResult.dataPoints}
                      </Descriptions.Item>
                    </Descriptions>
                  </Card>
                </Space>
              </Card>
            </>
          )}
        </Space>
      </Card>
    </div>
  );
}