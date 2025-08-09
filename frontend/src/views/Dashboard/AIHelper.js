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
  Upload,
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
} from '@ant-design/icons';
import dayjs from 'dayjs';
import TransactionService from '../../services/transactionService';
import ReportService from '../../services/reportService';
import AIService from '../../services/aiService';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;
const { RangePicker } = DatePicker;

export default function AIHelper() {
  const [result, setResult] = useState('');
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
      title: 'Period',
      width: 120,
      render: (_, record) => {
        if (record.startDate && record.endDate) {
          return `${dayjs(record.startDate).format('MM/DD')} - ${dayjs(record.endDate).format('MM/DD')}`;
        }
        return '-';
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      width: 100,
      render: (status) => (
        <Tag color={status === 'COMPLETED' ? 'green' : 'orange'}>
          {status}
        </Tag>
      ),
    },
  ];

  // Handle AI analysis actions
  const handleAIAction = (type) => {
    setActiveTab(type);
    setModalVisible(true);
  };

  // Execute classification analysis
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
          });
          results.push({
            transactionId: txn.transactionId,
            transaction: txn.description,
            amount: txn.amount,
            originalType: txn.transactionType,
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
      
      setResult(JSON.stringify(results, null, 2));
      setModalVisible(false);
      message.success(`Classified ${results.length} transactions`);
    } catch (error) {
      console.error('Classification failed:', error);
      setResult('Classification failed. Please check the backend service.');
    } finally {
      setLoading(false);
    }
  };

  // Execute financial Q&A
  const executeFinancialQA = async (values) => {
    setLoading(true);
    try {
      const selectedTxns = transactions.filter(t => selectedTransactions.includes(t.transactionId));
      const context = selectedTxns.map(t => 
        `${t.transactionDate}: ${t.description} - ${t.currency || 'CNY'} ${t.amount} (${t.transactionType})`
      ).join('\n');

      const res = await AIService.askFinancialQuestion({
        question: values.question,
        context: context,
        companyId: 1, // TODO: Get from user context
      });
      
      const result = {
        question: values.question,
        context: `${selectedTxns.length} transactions`,
        answer: res.data || res
      };
      
      setResult(JSON.stringify(result, null, 2));
      setModalVisible(false);
      message.success('Financial question answered');
    } catch (error) {
      console.error('Financial Q&A failed:', error);
      setResult('Financial Q&A failed. Please check the backend service.');
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
      
      setResult(JSON.stringify(results, null, 2));
      setModalVisible(false);
      message.success(`Analyzed ${selectedTxns.length} transactions, found ${anomalyCount} anomalies`);
    } catch (error) {
      console.error('Anomaly detection failed:', error);
      setResult('Anomaly detection failed. Please check the backend service.');
    } finally {
      setLoading(false);
    }
  };

  // Execute report insights
  const executeReportInsights = async (values) => {
    setLoading(true);
    try {
      const selectedReportData = reports.filter(r => selectedReports.includes(r.reportId));
      
      if (selectedReportData.length === 0) {
        message.warning('Please select at least one report');
        return;
      }

      const results = [];
      for (const report of selectedReportData) {
        try {
          // Create report content summary for AI analysis
          const reportContent = `Report: ${report.reportName}
Type: ${report.reportType}
Period: ${report.startDate} to ${report.endDate}
Status: ${report.status}
File Size: ${report.fileSize || 'Unknown'}`;
          
          const res = await AIService.reportInsight({
            reportData: reportContent,
            reportType: report.reportType,
          });
          
          results.push({
            reportId: report.reportId,
            reportName: report.reportName,
            reportType: report.reportType,
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
      
      setResult(JSON.stringify(results, null, 2));
      setModalVisible(false);
      message.success(`Generated insights for ${results.length} reports`);
    } catch (error) {
      console.error('Report insights failed:', error);
      setResult('Report insights failed. Please check the backend service.');
    } finally {
      setLoading(false);
    }
  };

  // Clear selections
  const clearSelections = () => {
    setSelectedTransactions([]);
    setSelectedReports([]);
  };

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
              onClick={() => exportAnalysisResults(JSON.parse(result), 'ai_analysis')}
            >
              Export Results
            </Button>
            <Button onClick={() => setResult('')}>Clear</Button>
          </Space>
        )}
      </div>

      {result ? (
        <Card>
          <pre style={{
            background: '#f6f6f6',
            padding: 16,
            borderRadius: 4,
            maxHeight: 500,
            overflow: 'auto',
            whiteSpace: 'pre-wrap',
            fontSize: '12px',
          }}>
            {result}
          </pre>
        </Card>
      ) : (
        <Alert 
          message="Select an AI analysis type above to begin" 
          type="info" 
          showIcon 
        />
      )}

      {/* AI Analysis Modal */}
      <Modal
        title={`AI Analysis - ${activeTab}`}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={1000}
        footer={null}
        destroyOnClose
      >
        <Spin spinning={loadingData}>
          <Tabs 
            activeKey={activeTab} 
            onChange={setActiveTab}
            type="card"
            items={[
              {
                key: 'classify',
                label: 'Classify Transactions',
                children: (
                  <Form form={classifyForm} onFinish={executeClassification}>
                    <div style={{ marginBottom: 16 }}>
                      <Space>
                        <Title level={5} style={{ margin: 0 }}>
                          <DatabaseOutlined /> Select Transactions to Classify
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
                        <Text>Selected: {selectedTransactions.length}</Text>
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
                )
              },
              {
                key: 'question',
                label: 'Financial Q&A',
                children: (
                  <Form form={questionForm} onFinish={executeFinancialQA} layout="vertical">
                    <Form.Item
                      name="question"
                      label="Ask a Financial Question"
                      rules={[{ required: true, message: 'Please enter your question' }]}
                    >
                      <TextArea 
                        rows={3} 
                        placeholder="e.g., What were my highest expense categories this month?"
                      />
                    </Form.Item>
                    
                    <div style={{ marginBottom: 16 }}>
                      <Space>
                        <Title level={5} style={{ margin: 0 }}>
                          <DatabaseOutlined /> Select Transaction Context
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
                        <Text>Context: {selectedTransactions.length} transactions</Text>
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
                )
              },
              {
                key: 'anomaly',
                label: 'Anomaly Detection',
                children: (
                  <Form form={anomalyForm} onFinish={executeAnomalyDetection}>
                    <div style={{ marginBottom: 16 }}>
                      <Space>
                        <Title level={5} style={{ margin: 0 }}>
                          <AlertOutlined /> Select Transactions to Analyze
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
                        <Text>Selected: {selectedTransactions.length}</Text>
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
                )
              },
              {
                key: 'insight',
                label: 'Report Insights',
                children: (
                  <Form form={insightForm} onFinish={executeReportInsights}>
                    <div style={{ marginBottom: 16 }}>
                      <Space>
                        <Title level={5} style={{ margin: 0 }}>
                          <BarChartOutlined /> Select Reports to Analyze
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
                        <Text>Selected: {selectedReports.length}</Text>
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
                )
              }
            ]}
          />
        </Spin>
      </Modal>
    </Card>
  );
}