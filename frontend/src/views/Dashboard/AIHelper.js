import React, { useState } from 'react';
import {
  Card,
  Typography,
  Space,
  Button,
  Divider,
  Alert,
} from 'antd';
import {
  AppstoreOutlined,
  QuestionCircleOutlined,
  AlertOutlined,
  FileSearchOutlined,
} from '@ant-design/icons';
import {
  classifyTransaction,
  askFinancialQuestion,
  detectAnomaly,
  reportInsight,
} from '../../services/aiService';

const { Title } = Typography;

export default function AIHelper() {
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAction = async (type) => {
    setLoading(true);
    try {
      let res;
      if (type === 'classify') {
        res = await classifyTransaction({
          description: 'Purchased an office chair',
          amount: 1200,
          currency: 'SGD',
        });
      } else if (type === 'ask') {
        res = await askFinancialQuestion({
          question: 'What was my highest spending category this month?',
          companyId: 1,
        });
      } else if (type === 'detect') {
        res = await detectAnomaly({
          description: 'Paid office rent',
          amount: 100000,
          currency: 'SGD',
        });
      } else if (type === 'report') {
        res = await reportInsight({
          reportData: 'Income: 10000, Expense: 8000, Profit: 2000',
          reportType: 'Income',
        });
      }
      setResult(JSON.stringify(res.data, null, 2));
    } catch (err) {
      setResult('Request failed. Please check if the backend service is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card style={{ margin: 24 }}>
      <Title level={4}>🤖 AI Assistant Feature Test</Title>
      <Space wrap>
        <Button
          type="primary"
          icon={<AppstoreOutlined />}
          onClick={() => handleAction('classify')}
          loading={loading}
        >
          Classify Transaction
        </Button>
        <Button
          icon={<QuestionCircleOutlined />}
          onClick={() => handleAction('ask')}
          loading={loading}
        >
          Financial Q&A
        </Button>
        <Button
          icon={<AlertOutlined />}
          onClick={() => handleAction('detect')}
          loading={loading}
        >
          Anomaly Detection
        </Button>
        <Button
          icon={<FileSearchOutlined />}
          onClick={() => handleAction('report')}
          loading={loading}
        >
          Report Insight
        </Button>
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
        <Alert message="Click any button above to test the AI features." type="info" showIcon />
      )}
    </Card>
  );
}
