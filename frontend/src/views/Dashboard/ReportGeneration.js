// frontend/src/views/Dashboard/ReportGeneration.js
import React, { useState, useEffect } from 'react';
import {
  Card, Form, Select, Input, DatePicker, Switch, Button, 
  Space, message, Row, Col, Typography, Divider, Alert, 
  Spin, Tooltip, Tag
} from 'antd';
import {
  FundProjectionScreenOutlined, PlayCircleOutlined,
  InfoCircleOutlined, BarChartOutlined, DollarCircleOutlined,
  PieChartOutlined, FundOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined, LoadingOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { Option } = Select;
const { Title, Text, Paragraph } = Typography;
const { RangePicker } = DatePicker;

/**
 * Enhanced Report Generation Component
 * 
 * Features:
 * - Connection testing
 * - Better error handling
 * - Enhanced UI feedback
 * - Debugging information
 */
const ReportGeneration = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [selectedReportType, setSelectedReportType] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState('unknown');
  const [testingConnection, setTestingConnection] = useState(false);

  const reportTypes = ReportService.getReportTypes();

  // Test backend connection on component mount
  useEffect(() => {
    testBackendConnection();
  }, []);

  const testBackendConnection = async () => {
    setTestingConnection(true);
    try {
      const result = await ReportService.testConnection();
      setConnectionStatus(result.success ? 'connected' : 'disconnected');
      if (!result.success) {
        console.error('Backend connection failed:', result.error);
      }
    } catch (error) {
      console.error('Connection test error:', error);
      setConnectionStatus('disconnected');
    } finally {
      setTestingConnection(false);
    }
  };

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      console.log('Form values:', values);

      // Validate form data
      const validation = ReportService.validateReportRequest(values);
      if (!validation.isValid) {
        message.error(validation.errors.join(', '));
        return;
      }

      // Format dates and prepare request
      const reportRequest = {
        ...values,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
        aiAnalysisEnabled: values.aiAnalysisEnabled || false
      };
      delete reportRequest.dateRange;

      console.log('Sending report request:', reportRequest);

      // Generate report
      const response = await ReportService.generateReport(reportRequest);
      console.log('Report generation response:', response);
      
      if (response && (response.status === 'success' || response.reportId)) {
        message.success('Report generation started successfully!');
        form.resetFields();
        setSelectedReportType(null);
        
        // Show success message with report ID
        const reportId = response.reportId || response.id;
        if (reportId) {
          message.info(`Report ID: ${reportId}. Check Report Management for progress.`);
        }
      } else {
        throw new Error(response?.message || 'Unexpected response format');
      }
    } catch (error) {
      console.error('Report generation error:', error);
      
      // Enhanced error message handling
      let errorMessage = 'Failed to generate report';
      if (error.message) {
        errorMessage += ': ' + error.message;
      }
      
      message.error(errorMessage);
      
      // If connection error, suggest testing connection
      if (error.message.includes('connect') || error.message.includes('network')) {
        message.warning('Please check if the backend server is running on localhost:8085');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleReportTypeChange = (value) => {
    setSelectedReportType(value);
    const reportType = reportTypes.find(rt => rt.value === value);
    
    // Auto-fill report name based on type and current date
    if (reportType) {
      const currentDate = dayjs().format('YYYY-MM-DD');
      form.setFieldsValue({
        reportName: `${reportType.label} - ${currentDate}`
      });
    }
  };

  const getReportTypeIcon = (type) => {
    switch (type) {
      case 'BALANCE_SHEET':
        return <BarChartOutlined />;
      case 'INCOME_STATEMENT':
        return <DollarCircleOutlined />;
      case 'INCOME_EXPENSE':
        return <PieChartOutlined />;
      case 'FINANCIAL_GROUPING':
        return <FundOutlined />;
      default:
        return <FundProjectionScreenOutlined />;
    }
  };

  const getCurrentReportType = () => {
    return reportTypes.find(rt => rt.value === selectedReportType);
  };

  const getConnectionStatusTag = () => {
    if (testingConnection) {
      return <Tag icon={<LoadingOutlined />} color="processing">Testing...</Tag>;
    }
    
    switch (connectionStatus) {
      case 'connected':
        return <Tag icon={<CheckCircleOutlined />} color="success">Backend Connected</Tag>;
      case 'disconnected':
        return <Tag icon={<ExclamationCircleOutlined />} color="error">Backend Disconnected</Tag>;
      default:
        return <Tag color="default">Connection Unknown</Tag>;
    }
  };

  return (
    <div>
      {/* Connection Status Card */}
      <Card 
        size="small" 
        style={{ margin: '24px 24px 16px 24px', borderLeft: '4px solid #1890ff' }}
      >
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <InfoCircleOutlined />
              <Text strong>Backend Connection Status:</Text>
              {getConnectionStatusTag()}
            </Space>
          </Col>
          <Col>
            <Button 
              size="small" 
              onClick={testBackendConnection}
              loading={testingConnection}
            >
              Test Connection
            </Button>
          </Col>
        </Row>
        {connectionStatus === 'disconnected' && (
          <Alert
            style={{ marginTop: 8 }}
            message="Backend server appears to be offline"
            description="Please ensure the Spring Boot backend is running on localhost:8085"
            type="warning"
            showIcon
          />
        )}
      </Card>

      {/* Main Report Generation Card */}
      <Card
        title={
          <Space>
            <FundProjectionScreenOutlined />
            Generate Financial Reports
          </Space>
        }
        style={{ margin: 24 }}
      >
        {/* Information Alert */}
        <Alert
          message="DDD-Based Report Generation"
          description="This interface generates reports using the Domain-Driven Design architecture. Reports will be processed asynchronously and can be monitored in Report Management."
          type="info"
          showIcon
          style={{ marginBottom: 24 }}
        />

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            aiAnalysisEnabled: false,
            dateRange: [dayjs().subtract(1, 'month'), dayjs()]
          }}
        >
          <Row gutter={[16, 16]}>
            {/* Report Type Selection */}
            <Col xs={24} md={12}>
              <Form.Item
                name="reportType"
                label="Report Type"
                rules={[{ required: true, message: 'Please select a report type' }]}
              >
                <Select
                  placeholder="Select report type"
                  onChange={handleReportTypeChange}
                  size="large"
                >
                  {reportTypes.map(type => (
                    <Option key={type.value} value={type.value}>
                      <Space>
                        {getReportTypeIcon(type.value)}
                        <div>
                          <div>{type.label}</div>
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            {type.description}
                          </Text>
                        </div>
                      </Space>
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>

            {/* Report Name */}
            <Col xs={24} md={12}>
              <Form.Item
                name="reportName"
                label="Report Name"
                rules={[
                  { required: true, message: 'Please enter a report name' },
                  { min: 3, message: 'Report name must be at least 3 characters' }
                ]}
              >
                <Input 
                  placeholder="Enter report name"
                  size="large"
                  maxLength={100}
                />
              </Form.Item>
            </Col>

            {/* Date Range */}
            <Col xs={24} md={12}>
              <Form.Item
                name="dateRange"
                label="Report Period"
                rules={[{ required: true, message: 'Please select a date range' }]}
              >
                <RangePicker
                  style={{ width: '100%' }}
                  size="large"
                  format="YYYY-MM-DD"
                  placeholder={['Start Date', 'End Date']}
                />
              </Form.Item>
            </Col>

            {/* AI Analysis Toggle */}
            <Col xs={24} md={12}>
              <Form.Item
                name="aiAnalysisEnabled"
                label="AI Analysis"
                valuePropName="checked"
              >
                <div>
                  <Switch />
                  <Tooltip title="Enable AI-powered analysis of the financial report">
                    <InfoCircleOutlined style={{ marginLeft: 8, color: '#1890ff' }} />
                  </Tooltip>
                  <div style={{ marginTop: 4 }}>
                    <Text type="secondary" style={{ fontSize: '12px' }}>
                      Include intelligent insights and recommendations
                    </Text>
                  </div>
                </div>
              </Form.Item>
            </Col>
          </Row>

          {/* Report Type Details */}
          {selectedReportType && (
            <Card
              size="small"
              style={{ 
                marginTop: 16, 
                backgroundColor: '#f9f9f9',
                border: '1px dashed #d9d9d9'
              }}
            >
              <Row align="top">
                <Col span={2}>
                  {getReportTypeIcon(selectedReportType)}
                </Col>
                <Col span={22}>
                  <Title level={5} style={{ margin: 0 }}>
                    {getCurrentReportType()?.label}
                  </Title>
                  <Paragraph style={{ margin: '4px 0 0 0', fontSize: '13px' }}>
                    {getCurrentReportType()?.description}
                  </Paragraph>
                </Col>
              </Row>
            </Card>
          )}

          <Divider />

          {/* Submit Button */}
          <Form.Item style={{ marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              disabled={connectionStatus === 'disconnected'}
              icon={<PlayCircleOutlined />}
              size="large"
              style={{ minWidth: 160 }}
            >
              {loading ? 'Generating Report...' : 'Generate Report'}
            </Button>
            
            {connectionStatus === 'disconnected' && (
              <div style={{ marginTop: 8 }}>
                <Text type="warning" style={{ fontSize: '12px' }}>
                  Please ensure backend connection before generating reports
                </Text>
              </div>
            )}
          </Form.Item>
        </Form>

        {/* Debug Information */}
        <Divider orientation="left">Debug Information</Divider>
        <Row gutter={16}>
          <Col span={8}>
            <Text type="secondary">Backend URL:</Text>
            <br />
            <Text code>http://localhost:8085/api/reports</Text>
          </Col>
          <Col span={8}>
            <Text type="secondary">Connection Status:</Text>
            <br />
            {getConnectionStatusTag()}
          </Col>
          <Col span={8}>
            <Text type="secondary">Report Types Available:</Text>
            <br />
            <Text>{reportTypes.length} types configured</Text>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default ReportGeneration;