// frontend/src/views/Dashboard/ReportGeneration.js
import React, { useState } from 'react';
import {
  Card, Form, Select, Input, DatePicker, Switch, Button, 
  Space, message, Row, Col, Typography, Divider, Alert
} from 'antd';
import {
  FundProjectionScreenOutlined, PlayCircleOutlined,
  InfoCircleOutlined, BarChartOutlined, DollarCircleOutlined,
  PieChartOutlined, FundOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { Option } = Select;
const { Title, Text, Paragraph } = Typography;
const { RangePicker } = DatePicker;

/**
 * Report Generation Component
 * 
 * Handles DDD-based report generation requests
 */
const ReportGeneration = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [selectedReportType, setSelectedReportType] = useState(null);

  const reportTypes = ReportService.getReportTypes();

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      // Validate form data
      const validation = ReportService.validateReportRequest(values);
      if (!validation.isValid) {
        message.error(validation.errors.join(', '));
        return;
      }

      // Format dates
      const reportRequest = {
        ...values,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
        aiAnalysisEnabled: values.aiAnalysisEnabled || false
      };
      delete reportRequest.dateRange;

      // Generate report
      const response = await ReportService.generateReport(reportRequest);
      
      if (response.status === 'success') {
        message.success('Report generation started successfully!');
        form.resetFields();
        setSelectedReportType(null);
        
        // Show success message with report ID
        message.info(`Report ID: ${response.reportId}. Check Report Management for progress.`);
      } else {
        throw new Error(response.message || 'Unknown error occurred');
      }
    } catch (error) {
      console.error('Report generation error:', error);
      message.error('Failed to generate report: ' + error.message);
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

  return (
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
        icon={<InfoCircleOutlined />}
        showIcon
        style={{ marginBottom: 24 }}
      />

      <Row gutter={24}>
        {/* Report Generation Form */}
        <Col span={14}>
          <Card title="Report Configuration" size="small">
            <Form
              form={form}
              layout="vertical"
              onFinish={handleSubmit}
              initialValues={{
                dateRange: [dayjs().subtract(1, 'month'), dayjs()],
                aiAnalysisEnabled: false
              }}
            >
              <Row gutter={16}>
                <Col span={12}>
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
                            {type.label}
                          </Space>
                        </Option>
                      ))}
                    </Select>
                  </Form.Item>
                </Col>

                <Col span={12}>
                  <Form.Item
                    name="reportName"
                    label="Report Name"
                    rules={[{ required: true, message: 'Please enter a report name' }]}
                  >
                    <Input
                      placeholder="Enter a descriptive name for this report"
                      size="large"
                    />
                  </Form.Item>
                </Col>
              </Row>

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

              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    name="aiAnalysisEnabled"
                    label="AI Analysis"
                    valuePropName="checked"
                  >
                    <Switch
                      checkedChildren="Enabled"
                      unCheckedChildren="Disabled"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Divider />

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  size="large"
                  icon={<PlayCircleOutlined />}
                  block
                >
                  Generate Report
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>

        {/* Report Type Information */}
        <Col span={10}>
          <Card title="Report Information" size="small">
            {selectedReportType ? (
              <div>
                <Title level={4}>
                  <Space>
                    {getReportTypeIcon(selectedReportType)}
                    {getCurrentReportType()?.label}
                  </Space>
                </Title>
                <Paragraph>
                  {getCurrentReportType()?.description}
                </Paragraph>

                <Divider />

                <Title level={5}>Features:</Title>
                <ul>
                  <li>Asynchronous processing</li>
                  <li>Excel export format</li>
                  <li>Multi-tenant data isolation</li>
                  {selectedReportType === 'FINANCIAL_GROUPING' && (
                    <>
                      <li>Group by category, department, fund</li>
                      <li>Monthly trend analysis</li>
                    </>
                  )}
                  {selectedReportType === 'INCOME_STATEMENT' && (
                    <>
                      <li>Revenue and expense categorization</li>
                      <li>Net profit calculation</li>
                    </>
                  )}
                  {selectedReportType === 'BALANCE_SHEET' && (
                    <>
                      <li>Assets, liabilities, equity</li>
                      <li>Balance verification</li>
                    </>
                  )}
                  {selectedReportType === 'INCOME_EXPENSE' && (
                    <>
                      <li>Detailed income vs expense analysis</li>
                      <li>Variance reporting</li>
                    </>
                  )}
                </ul>

                <Divider />

                <Text type="secondary">
                  Report generation typically takes 30-60 seconds depending on data volume.
                  You'll be notified when the report is ready for download.
                </Text>
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 0' }}>
                <FundProjectionScreenOutlined 
                  style={{ fontSize: '48px', color: '#d9d9d9', marginBottom: '16px' }} 
                />
                <Paragraph type="secondary">
                  Select a report type to see detailed information
                </Paragraph>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      {/* Quick Actions */}
      <Card title="Quick Actions" size="small" style={{ marginTop: 16 }}>
        <Space wrap>
          <Button
            onClick={() => {
              form.setFieldsValue({
                reportType: 'BALANCE_SHEET',
                reportName: `Balance Sheet - ${dayjs().format('YYYY-MM-DD')}`,
                dateRange: [dayjs(), dayjs()]
              });
              setSelectedReportType('BALANCE_SHEET');
            }}
          >
            <BarChartOutlined />
            Current Balance Sheet
          </Button>

          <Button
            onClick={() => {
              form.setFieldsValue({
                reportType: 'INCOME_STATEMENT',
                reportName: `Income Statement - ${dayjs().format('YYYY-MM')}`,
                dateRange: [dayjs().startOf('month'), dayjs().endOf('month')]
              });
              setSelectedReportType('INCOME_STATEMENT');
            }}
          >
            <DollarCircleOutlined />
            Monthly Income Statement
          </Button>

          <Button
            onClick={() => {
              form.setFieldsValue({
                reportType: 'FINANCIAL_GROUPING',
                reportName: `Financial Grouping - Q${dayjs().quarter()} ${dayjs().year()}`,
                dateRange: [dayjs().startOf('quarter'), dayjs().endOf('quarter')]
              });
              setSelectedReportType('FINANCIAL_GROUPING');
            }}
          >
            <FundOutlined />
            Quarterly Analysis
          </Button>
        </Space>
      </Card>
    </Card>
  );
};

export default ReportGeneration;