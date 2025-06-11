// frontend/src/views/Dashboard/ReportGeneration.js
import React, { useState } from 'react';
import {
  Card, Form, Select, Input, DatePicker, Switch, Button, 
  message, Space, Typography, Divider, Alert
} from 'antd';
import {
  FileTextOutlined, DownloadOutlined, RocketOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;
const { Option } = Select;

const ReportGeneration = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [generatedReport, setGeneratedReport] = useState(null);

  const reportTypes = ReportService.getReportTypes();

  const handleSubmit = async (values) => {
    setLoading(true);
    setGeneratedReport(null);

    try {
      // Format the dates
      const [startDate, endDate] = values.dateRange;
      const reportRequest = {
        reportType: values.reportType,
        reportName: values.reportName,
        startDate: startDate.format('YYYY-MM-DD'),
        endDate: endDate.format('YYYY-MM-DD'),
        aiAnalysisEnabled: values.aiAnalysisEnabled || false
      };

      // Validate the request
      const validation = ReportService.validateReportRequest(reportRequest);
      if (!validation.isValid) {
        message.error(`Validation failed: ${validation.errors.join(', ')}`);
        return;
      }

      // Generate the report
      const response = await ReportService.generateReport(reportRequest);
      
      if (response.status === 'success') {
        message.success('Report generation started successfully!');
        setGeneratedReport({
          reportId: response.reportId,
          ...reportRequest
        });
      } else {
        message.error(response.message || 'Failed to start report generation');
      }

    } catch (error) {
      console.error('Report generation error:', error);
      message.error(error.message || 'Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    form.resetFields();
    setGeneratedReport(null);
  };

  const generateDefaultName = (reportType, dateRange) => {
    if (!reportType || !dateRange || dateRange.length !== 2) {
      return '';
    }

    const reportTypeLabel = reportTypes.find(type => type.value === reportType)?.label || reportType;
    const period = ReportService.formatReportPeriod(
      dateRange[0].format('YYYY-MM-DD'),
      dateRange[1].format('YYYY-MM-DD')
    );
    
    return `${reportTypeLabel} - ${period}`;
  };

  const onReportTypeChange = (reportType) => {
    const dateRange = form.getFieldValue('dateRange');
    if (reportType && dateRange) {
      const defaultName = generateDefaultName(reportType, dateRange);
      form.setFieldValue('reportName', defaultName);
    }
  };

  const onDateRangeChange = (dateRange) => {
    const reportType = form.getFieldValue('reportType');
    if (reportType && dateRange) {
      const defaultName = generateDefaultName(reportType, dateRange);
      form.setFieldValue('reportName', defaultName);
    }
  };

  return (
    <Card
      title={
        <>
          <FileTextOutlined style={{ marginRight: 8 }} />
          Report Generation
        </>
      }
      style={{ margin: 24 }}
      extra={
        <Button onClick={handleReset} disabled={loading}>
          Reset Form
        </Button>
      }
    >
      <Alert
        message="Financial Report Generator"
        description="Generate comprehensive financial reports including Balance Sheets, Income Statements, and detailed analysis reports using our advanced DDD-based system."
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
          dateRange: [dayjs().startOf('month'), dayjs()]
        }}
      >
        <Space size="large" style={{ width: '100%' }} direction="vertical">
          
          {/* Report Type Selection */}
          <Card size="small" title="Report Configuration">
            <Form.Item
              name="reportType"
              label="Report Type"
              rules={[{ required: true, message: 'Please select a report type' }]}
            >
              <Select 
                placeholder="Select report type"
                onChange={onReportTypeChange}
                size="large"
              >
                {reportTypes.map(type => (
                  <Option key={type.value} value={type.value}>
                    <div>
                      <Text strong>{type.label}</Text>
                      <br />
                      <Text type="secondary" style={{ fontSize: '12px' }}>
                        {type.description}
                      </Text>
                    </div>
                  </Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              name="reportName"
              label="Report Name"
              rules={[{ required: true, message: 'Please enter a report name' }]}
            >
              <Input 
                placeholder="Enter report name"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="dateRange"
              label="Report Period"
              rules={[{ required: true, message: 'Please select date range' }]}
            >
              <RangePicker 
                style={{ width: '100%' }}
                size="large"
                onChange={onDateRangeChange}
                disabledDate={(current) => current && current > dayjs().endOf('day')}
                ranges={{
                  'This Month': [dayjs().startOf('month'), dayjs()],
                  'Last Month': [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')],
                  'This Quarter': [dayjs().startOf('quarter'), dayjs()],
                  'Last Quarter': [dayjs().subtract(1, 'quarter').startOf('quarter'), dayjs().subtract(1, 'quarter').endOf('quarter')],
                  'This Year': [dayjs().startOf('year'), dayjs()],
                  'Last Year': [dayjs().subtract(1, 'year').startOf('year'), dayjs().subtract(1, 'year').endOf('year')]
                }}
              />
            </Form.Item>
          </Card>

          {/* Advanced Options */}
          <Card size="small" title="Advanced Options">
            <Form.Item
              name="aiAnalysisEnabled"
              valuePropName="checked"
              label="Enable AI Analysis"
            >
              <Switch 
                checkedChildren="ON" 
                unCheckedChildren="OFF"
              />
            </Form.Item>
            <Text type="secondary" style={{ fontSize: '12px', marginTop: -16, display: 'block' }}>
              AI analysis provides insights and recommendations based on your financial data
            </Text>
          </Card>

          {/* Action Buttons */}
          <Card size="small">
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                icon={<RocketOutlined />}
                size="large"
              >
                {loading ? 'Generating Report...' : 'Generate Report'}
              </Button>
              
              <Button
                type="default"
                onClick={handleReset}
                disabled={loading}
                size="large"
              >
                Clear Form
              </Button>
            </Space>
          </Card>

          {/* Generation Result */}
          {generatedReport && (
            <Card 
              size="small" 
              title="Report Generation Started"
              style={{ backgroundColor: '#f6ffed', border: '1px solid #b7eb8f' }}
            >
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text>
                  <Text strong>Report ID:</Text> {generatedReport.reportId}
                </Text>
                <Text>
                  <Text strong>Report Type:</Text> {generatedReport.reportType}
                </Text>
                <Text>
                  <Text strong>Period:</Text> {ReportService.formatReportPeriod(
                    generatedReport.startDate, 
                    generatedReport.endDate
                  )}
                </Text>
                <Text>
                  <Text strong>AI Analysis:</Text> {generatedReport.aiAnalysisEnabled ? 'Enabled' : 'Disabled'}
                </Text>
                
                <Alert
                  message="Report generation has been started"
                  description="You can monitor the progress and download the report from the Report List page once it's completed."
                  type="success"
                  showIcon
                  action={
                    <Button size="small" type="primary" ghost>
                      Go to Report List
                    </Button>
                  }
                />
              </Space>
            </Card>
          )}

          {/* Help Information */}
          <Card size="small" title="Report Types Information">
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px' }}>
              {reportTypes.map(type => (
                <div key={type.value} style={{ padding: '12px', border: '1px solid #f0f0f0', borderRadius: '6px' }}>
                  <Text strong style={{ color: '#1890ff' }}>{type.label}</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    {type.description}
                  </Text>
                </div>
              ))}
            </div>
          </Card>
          
        </Space>
      </Form>
    </Card>
  );
};

export default ReportGeneration;