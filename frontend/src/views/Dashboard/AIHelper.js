import React, { useState } from 'react';
import {
  Card,
  Typography,
  Space,
  Button,
  Divider,
  Alert,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  DatePicker,
} from 'antd';
import {
  AppstoreOutlined,
  QuestionCircleOutlined,
  AlertOutlined,
  FileSearchOutlined,
  BulbOutlined,
  SafetyCertificateOutlined,
  RobotOutlined,
  ContainerOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  classifyTransaction,
  askFinancialQuestion,
  categorySuggestions,
  detectAnomalies,
  reportInsight,
  analyze,
  recommend,
  healthCheck,
  providerName,
  formatAIResult,
} from '../../services/aiService';

const { Title } = Typography;
const { Option } = Select;

const MODULES = [
  {
    key: 'classify',
    icon: <AppstoreOutlined />,
    title: 'Enhanced Transaction / AI Classification',
    color: 'blue',
  },
  {
    key: 'category',
    icon: <BulbOutlined />,
    title: 'Get Category Suggestions',
    color: 'orange',
  },
  {
    key: 'ask',
    icon: <QuestionCircleOutlined />,
    title: 'Financial Q&A',
    color: 'green',
  },
  {
    key: 'analyze',
    icon: <FileSearchOutlined />,
    title: 'Financial Analysis',
    color: 'geekblue',
  },
  {
    key: 'detect',
    icon: <AlertOutlined />,
    title: 'Batch Anomaly Detection',
    color: 'volcano',
  },
  {
    key: 'report',
    icon: <FileSearchOutlined />,
    title: 'Smart Report Insights',
    color: 'purple',
  },
  {
    key: 'recommend',
    icon: <RobotOutlined />,
    title: 'AI Recommendations',
    color: 'magenta',
  },
];

export default function AIHelper() {
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [openModal, setOpenModal] = useState(null);

  // Form instances for each feature
  const [form] = Form.useForm();
  const [formCat] = Form.useForm();
  const [formAsk] = Form.useForm();
  const [formAnalyze] = Form.useForm();
  const [formDetect] = Form.useForm();
  const [formReport] = Form.useForm();
  const [formRecommend] = Form.useForm();

  // Open modal and clear result
  const handleOpen = (key) => {
    setResult('');
    setOpenModal(key);
  };

  // All submit handlers
  const submitClassify = async (values) => {
    setLoading(true);
    try {
      const res = await classifyTransaction(values);
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  const submitCategory = async (values) => {
    setLoading(true);
    try {
      const res = await categorySuggestions(values);
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  const submitAsk = async (values) => {
    setLoading(true);
    try {
      const res = await askFinancialQuestion(values);
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  const submitAnalyze = async (values) => {
    setLoading(true);
    try {
      const params = {
        ...values,
        startDate: values.startDate && dayjs(values.startDate).format('YYYY-MM-DD'),
        endDate: values.endDate && dayjs(values.endDate).format('YYYY-MM-DD'),
      };
      const res = await analyze(params);
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  const submitDetect = async (values) => {
    setLoading(true);
    try {
      const params = {
        ...values,
        startDate: values.startDate && dayjs(values.startDate).format('YYYY-MM-DD'),
        endDate: values.endDate && dayjs(values.endDate).format('YYYY-MM-DD'),
      };
      const res = await detectAnomalies(params);
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  const submitReport = async (values) => {
    setLoading(true);
    try {
      const res = await reportInsight({
        reportData: values.reportData,
        reportType: values.reportType,
      });
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  const submitRecommend = async (values) => {
    setLoading(true);
    try {
      const res = await recommend(values);
      setResult(formatAIResult(res));
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  // Health/Provider
  const handleHealth = async () => {
    setLoading(true);
    try {
      const res = await healthCheck();
      setResult(res ? 'AI service is available' : 'AI service is not available');
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };
  const handleProvider = async () => {
    setLoading(true);
    try {
      const res = await providerName();
      setResult('Current AI Provider: ' + res);
    } catch {
      setResult('Request failed. Please check if the backend service is running.');
    }
    setLoading(false);
  };

  // Modal rendering content
  const renderModalForm = () => {
    switch (openModal) {
      case 'classify':
        return (
          <Form form={form} layout="vertical" onFinish={submitClassify}>
            <Form.Item label="Transaction Description" name="description" rules={[{ required: true, message: 'Please enter a description' }]}>
              <Input placeholder="e.g. Hotel cost for business trip" />
            </Form.Item>
            <Form.Item label="Amount" name="amount" rules={[{ required: true, message: 'Please enter the amount' }]}>
              <InputNumber placeholder="e.g. 1200" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Currency" name="currency" rules={[{ required: true, message: 'Please enter currency' }]}>
              <Input placeholder="e.g. CNY" />
            </Form.Item>
            <Form.Item label="Payment Method" name="paymentMethod">
              <Input placeholder="e.g. Credit Card" />
            </Form.Item>
            <Form.Item label="Company ID" name="companyId">
              <InputNumber placeholder="e.g. 1" style={{ width: '100%' }} />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>Submit</Button>
          </Form>
        );
      case 'category':
        return (
          <Form form={formCat} layout="vertical" onFinish={submitCategory}>
            <Form.Item label="Transaction Description" name="description" rules={[{ required: true, message: 'Please enter a description' }]}>
              <Input placeholder="e.g. Team dinner reimbursement" />
            </Form.Item>
            <Form.Item label="Amount" name="amount">
              <InputNumber placeholder="e.g. 500" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Currency" name="currency">
              <Input placeholder="e.g. CNY" />
            </Form.Item>
            <Form.Item label="Type" name="transactionType">
              <Select placeholder="Please select">
                <Option value="EXPENSE">Expense</Option>
                <Option value="INCOME">Income</Option>
              </Select>
            </Form.Item>
            <Form.Item label="Company ID" name="companyId">
              <InputNumber placeholder="e.g. 1" style={{ width: '100%' }} />
            </Form.Item>
            <Button htmlType="submit" loading={loading} block type="dashed">Submit</Button>
          </Form>
        );
      case 'ask':
        return (
          <Form form={formAsk} layout="vertical" onFinish={submitAsk}>
            <Form.Item label="Financial Question" name="question" rules={[{ required: true, message: 'Please enter your question' }]}>
              <Input placeholder="e.g. What is the company’s profit this month?" />
            </Form.Item>
            <Form.Item label="Company ID" name="companyId">
              <InputNumber placeholder="e.g. 1" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Language" name="language">
              <Input placeholder="e.g. en" />
            </Form.Item>
            <Form.Item label="Start Date" name="startDate">
              <DatePicker placeholder="e.g. 2024-07-01" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="End Date" name="endDate">
              <DatePicker placeholder="e.g. 2024-07-31" style={{ width: '100%' }} />
            </Form.Item>
            <Button htmlType="submit" loading={loading} block>Submit</Button>
          </Form>
        );
      case 'analyze':
        return (
          <Form form={formAnalyze} layout="vertical" onFinish={submitAnalyze}>
            <Form.Item label="Company ID" name="companyId">
              <InputNumber placeholder="e.g. 1" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Report Type" name="reportType">
              <Input placeholder="e.g. Income Statement" />
            </Form.Item>
            <Form.Item label="Start Date" name="startDate">
              <DatePicker placeholder="e.g. 2024-07-01" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="End Date" name="endDate">
              <DatePicker placeholder="e.g. 2024-07-31" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Raw Data" name="rawData">
              <Input.TextArea placeholder="e.g. July 2024: Income 15000, Expenses 8000, Net Profit 7000" rows={2} />
            </Form.Item>
            <Button htmlType="submit" loading={loading} block>Submit</Button>
          </Form>
        );
      case 'detect':
        return (
          <Form form={formDetect} layout="vertical" onFinish={submitDetect}>
            <Form.Item label="Company ID" name="companyId">
              <InputNumber placeholder="e.g. 1" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Start Date" name="startDate">
              <DatePicker placeholder="e.g. 2024-07-01" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="End Date" name="endDate">
              <DatePicker placeholder="e.g. 2024-07-31" style={{ width: '100%' }} />
            </Form.Item>
            <Button htmlType="submit" loading={loading} block>Submit</Button>
          </Form>
        );
      case 'report':
        return (
          <Form form={formReport} layout="vertical" onFinish={submitReport}>
            <Form.Item label="Report Data" name="reportData" rules={[{ required: true, message: 'Please enter report data' }]}>
              <Input.TextArea placeholder="e.g. July 2024: Income 15000, Expenses 8000, Net Profit 7000" rows={2} />
            </Form.Item>
            <Form.Item label="Report Type" name="reportType">
              <Input placeholder="e.g. Income Statement" />
            </Form.Item>
            <Button htmlType="submit" loading={loading} block>Submit</Button>
          </Form>
        );
      case 'recommend':
        return (
          <Form form={formRecommend} layout="vertical" onFinish={submitRecommend}>
            <Form.Item label="Company ID" name="companyId">
              <InputNumber placeholder="e.g. 1" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label="Scenario" name="scenario">
              <Input placeholder="e.g. Cost Optimization, Investment Decision" />
            </Form.Item>
            <Form.Item label="Target Object" name="targetObject">
              <Input placeholder="e.g. Operations Department" />
            </Form.Item>
            <Form.Item label="Additional Data" name="data">
              <Input.TextArea placeholder="e.g. 2024 operations department expense details…" rows={2} />
            </Form.Item>
            <Button htmlType="submit" loading={loading} block>Submit</Button>
          </Form>
        );
      default:
        return null;
    }
  };

  return (
    <Card style={{ margin: 24 }}>
      <Title level={4} style={{ marginBottom: 8 }}>🤖 AI Finance Assistant DEMO</Title>
      <Space wrap>
        {MODULES.map(m => (
          <Button
            key={m.key}
            type="primary"
            icon={m.icon}
            style={{ margin: '4px 8px' }}
            onClick={() => handleOpen(m.key)}
          >
            {m.title}
          </Button>
        ))}
        <Button icon={<SafetyCertificateOutlined />} onClick={handleHealth}>Health Check</Button>
        <Button icon={<ContainerOutlined />} onClick={handleProvider}>Current AI Provider</Button>
      </Space>
      <Divider />
      <Title level={5}>Response Result</Title>
      {result ? (
        <pre style={{
          background: '#f6f6f6',
          padding: 12,
          borderRadius: 4,
          maxHeight: 400,
          overflow: 'auto',
          whiteSpace: 'pre-wrap'
        }}>
          {result}
        </pre>
      ) : (
        <Alert message="Click any button above, fill the form in the popup, and the AI response will be displayed here." type="info" showIcon />
      )}
      <Modal
        open={!!openModal}
        title={MODULES.find(m => m.key === openModal)?.title}
        onCancel={() => setOpenModal(null)}
        footer={null}
        destroyOnClose
        width={500}
      >
        {renderModalForm()}
      </Modal>
    </Card>
  );
}
