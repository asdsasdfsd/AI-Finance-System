// frontend/src/views/Dashboard/ReportManagement.js
import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Tooltip,
  message,
  Modal,
  Drawer,
  Typography,
  Progress,
  Alert,
  Spin,
  Empty,
  Row,
  Col,
  Statistic
} from 'antd';
import {
  EyeOutlined,
  DownloadOutlined,
  DeleteOutlined,
  BulbOutlined,
  FileTextOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';
import AIService from '../../services/aiService';
import AuthService from '../../services/authService';
import ReportInsightsDisplay from '../../components/ReportInsightsDisplay';

const { Title, Text } = Typography;
const { confirm } = Modal;

/**
 * Enhanced Report Management with AI Insights Integration
 */
const ReportManagement = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [insightsLoading, setInsightsLoading] = useState(false);
  const [statistics, setStatistics] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [reportInsights, setReportInsights] = useState({});
  const [insightsDrawerVisible, setInsightsDrawerVisible] = useState(false);
  const [previewDrawerVisible, setPreviewDrawerVisible] = useState(false);
  
  const currentUser = AuthService.getCurrentUser();

  useEffect(() => {
    fetchReports();
    fetchStatistics();
  }, []);

  // Fetch reports list
  const fetchReports = async () => {
    setLoading(true);
    try {
      console.log('Fetching reports...');
      
      const response = await ReportService.getReports({});
      console.log('Reports response:', response);
      
      // Handle different response formats from backend
      let reportData = [];
      if (response && response.data) {
        reportData = Array.isArray(response.data) ? response.data : [];
      } else if (Array.isArray(response)) {
        reportData = response;
      } else if (response && Array.isArray(response.content)) {
        reportData = response.content;
      }
      
      console.log('Processed report data:', reportData);
      setReports(reportData);
      
      if (reportData.length === 0) {
        console.log('No reports found - this may be normal if no reports have been generated yet');
      } else {
        message.success(`Found ${reportData.length} reports`);
      }
      
    } catch (error) {
      console.error('Error fetching reports:', error);
      message.error('Failed to fetch reports: ' + error.message);
      setReports([]);
    } finally {
      setLoading(false);
    }
  };

  // Fetch statistics
  const fetchStatistics = async () => {
    try {
      const stats = await ReportService.getReportStatistics?.();
      setStatistics(stats);
    } catch (error) {
      console.log('Statistics not available:', error.message);
    }
  };

  // Generate AI insights for a report
  const generateInsights = async (report) => {
    setInsightsLoading(true);
    try {
      console.log('Generating insights for report:', report);
      
      // Create report content summary for AI analysis
      const reportContent = `Report Name: ${report.reportName}
Report Type: ${report.reportType}
Period: ${report.startDate || 'N/A'} to ${report.endDate || 'N/A'}
Status: ${report.status}
File Size: ${report.fileSize || 'Unknown'}
Created: ${report.createdAt}
Company ID: ${report.companyId}`;

      const result = await AIService.reportInsight({
        reportData: reportContent,
        reportType: report.reportType
      });

      console.log('AI insights result:', result);

      if (result.success) {
        setReportInsights(prev => ({
          ...prev,
          [report.reportId]: result.data
        }));
        
        setSelectedReport(report);
        setInsightsDrawerVisible(true);
        
        message.success('AI insights generated successfully');
      } else {
        throw new Error(result.error || 'Failed to generate insights');
      }
      
    } catch (error) {
      console.error('Failed to generate insights:', error);
      message.error('Failed to generate insights: ' + error.message);
      
      // Show error insights
      setReportInsights(prev => ({
        ...prev,
        [report.reportId]: {
          summary: 'Analysis failed',
          insights: ['Unable to generate insights at this time'],
          recommendations: ['Please check the report data and try again'],
          confidence: 'low',
          analysisDate: new Date().toISOString(),
          status: 'error',
          error: true,
          errorMessage: error.message
        }
      }));
      
      setSelectedReport(report);
      setInsightsDrawerVisible(true);
    } finally {
      setInsightsLoading(false);
    }
  };

  // View report details
  const viewReport = async (report) => {
    try {
      console.log('Viewing report:', report);
      setSelectedReport(report);
      setPreviewDrawerVisible(true);
    } catch (error) {
      console.error('Failed to load report:', error);
      message.error('Failed to load report details');
    }
  };

  // Download report
  const downloadReport = async (report) => {
    try {
      console.log('Downloading report:', report);
      
      const response = await ReportService.downloadReport(report.reportId);
      
      // Create download link
      const url = window.URL.createObjectURL(new Blob([response]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${report.reportName}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      message.success('Report downloaded successfully');
    } catch (error) {
      console.error('Download failed:', error);
      message.error('Failed to download report: ' + error.message);
    }
  };

  // Delete report
  const deleteReport = (report) => {
    confirm({
      title: 'Delete Report',
      icon: <ExclamationCircleOutlined />,
      content: `Are you sure you want to delete "${report.reportName}"?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await ReportService.deleteReport(report.reportId);
          message.success('Report deleted successfully');
          fetchReports(); // Refresh list
        } catch (error) {
          console.error('Delete failed:', error);
          message.error('Failed to delete report: ' + error.message);
        }
      }
    });
  };

  // Report status configuration
  const reportStatuses = [
    { value: 'GENERATING', label: 'Generating', color: 'processing' },
    { value: 'COMPLETED', label: 'Completed', color: 'success' },
    { value: 'FAILED', label: 'Failed', color: 'error' },
    { value: 'ARCHIVED', label: 'Archived', color: 'default' }
  ];

  const getStatusIcon = (status) => {
    switch (status) {
      case 'GENERATING': return <Spin size="small" />;
      case 'COMPLETED': return <FileTextOutlined style={{ color: '#52c41a' }} />;
      case 'FAILED': return <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />;
      case 'ARCHIVED': return <FileTextOutlined style={{ color: '#d9d9d9' }} />;
      default: return <FileTextOutlined />;
    }
  };

  // Report type configuration
  const reportTypes = [
    { value: 'BALANCE_SHEET', label: 'Balance Sheet' },
    { value: 'INCOME_STATEMENT', label: 'Income Statement' },
    { value: 'INCOME_EXPENSE', label: 'Income vs Expense' },
    { value: 'CASH_FLOW', label: 'Cash Flow' },
    { value: 'FINANCIAL_GROUPING', label: 'Financial Grouping' }
  ];

  // Table columns
  const columns = [
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      key: 'reportName',
      width: 300,
      render: (name, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{name}</Text>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            ID: {record.reportId}
          </Text>
        </Space>
      )
    },
    {
      title: 'Type',
      dataIndex: 'reportType',
      key: 'reportType',
      width: 150,
      render: (type) => {
        const reportType = reportTypes.find(t => t.value === type);
        return reportType ? reportType.label : type;
      }
    },
    {
      title: 'Period',
      key: 'period',
      width: 200,
      render: (_, record) => {
        if (record.startDate && record.endDate) {
          return `${dayjs(record.startDate).format('MM-DD')} to ${dayjs(record.endDate).format('MM-DD')}`;
        } else if (record.endDate) {
          return dayjs(record.endDate).format('YYYY-MM-DD');
        }
        return '-';
      }
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => {
        const statusConfig = reportStatuses.find(s => s.value === status);
        return (
          <Space>
            {getStatusIcon(status)}
            <Tag color={statusConfig?.color}>
              {statusConfig?.label || status}
            </Tag>
          </Space>
        );
      }
    },
    {
      title: 'Progress',
      key: 'progress',
      width: 100,
      render: (_, record) => {
        if (record.status === 'GENERATING') {
          return <Progress percent={50} size="small" status="active" showInfo={false} />;
        } else if (record.status === 'COMPLETED') {
          return <Progress percent={100} size="small" status="success" showInfo={false} />;
        } else if (record.status === 'FAILED') {
          return <Progress percent={100} size="small" status="exception" showInfo={false} />;
        }
        return '-';
      }
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date) => dayjs(date).format('MM-DD HH:mm')
    },
    {
      title: 'Size',
      dataIndex: 'fileSizeFormatted',
      key: 'fileSize',
      width: 80,
      render: (size, record) => size || (record.fileSize ? `${Math.round(record.fileSize / 1024)}KB` : '-')
    },
    {
      title: 'AI Insights',
      key: 'insights',
      width: 120,
      render: (_, record) => {
        const hasInsights = reportInsights[record.reportId];
        return (
          <Space>
            <Button
              type={hasInsights ? "default" : "primary"}
              icon={<BulbOutlined />}
              size="small"
              loading={insightsLoading}
              onClick={() => generateInsights(record)}
              disabled={record.status !== 'COMPLETED'}
            >
              {hasInsights ? 'View' : 'Generate'}
            </Button>
          </Space>
        );
      }
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 200,
      render: (_, record) => (
        <Space>
          <Tooltip title="View Details">
            <Button 
              icon={<EyeOutlined />} 
              size="small"
              onClick={() => viewReport(record)}
            />
          </Tooltip>
          <Tooltip title="Download">
            <Button 
              icon={<DownloadOutlined />} 
              size="small"
              onClick={() => downloadReport(record)}
              disabled={record.status !== 'COMPLETED'}
            />
          </Tooltip>
          <Tooltip title="Delete">
            <Button 
              icon={<DeleteOutlined />} 
              size="small" 
              danger
              onClick={() => deleteReport(record)}
            />
          </Tooltip>
        </Space>
      )
    }
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* Header */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={2}>Report Management</Title>
          <Space>
            <Button 
              icon={<ReloadOutlined />} 
              onClick={fetchReports}
              loading={loading}
            >
              Refresh
            </Button>
          </Space>
        </div>
      </div>

      {/* Statistics Cards */}
      {statistics && (
        <Row gutter={16} style={{ marginBottom: '24px' }}>
          <Col span={6}>
            <Card>
              <Statistic 
                title="Total Reports" 
                value={statistics.totalReports || 0} 
                prefix={<FileTextOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic 
                title="Completed" 
                value={statistics.completedReports || 0} 
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic 
                title="Processing" 
                value={statistics.processingReports || 0} 
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic 
                title="With AI Insights" 
                value={Object.keys(reportInsights).length} 
                valueStyle={{ color: '#722ed1' }}
                prefix={<BulbOutlined />}
              />
            </Card>
          </Col>
        </Row>
      )}

      {/* Reports Table */}
      <Card>
        <Table
          columns={columns}
          dataSource={reports}
          rowKey="reportId"
          loading={loading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `Total ${total} reports`
          }}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="No reports found"
              />
            )
          }}
        />
      </Card>

      {/* AI Insights Drawer */}
      <Drawer
        title={
          <Space>
            <BulbOutlined />
            <span>AI Analysis Insights</span>
            {selectedReport && (
              <Tag color="blue">{selectedReport.reportType}</Tag>
            )}
          </Space>
        }
        width={800}
        open={insightsDrawerVisible}
        onClose={() => setInsightsDrawerVisible(false)}
        extra={
          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={() => selectedReport && generateInsights(selectedReport)}
              loading={insightsLoading}
            >
              Refresh
            </Button>
          </Space>
        }
      >
        {selectedReport && (
          <>
            {/* Report Info */}
            <Alert
              message={`Report: ${selectedReport.reportName}`}
              description={`Type: ${selectedReport.reportType} | Status: ${selectedReport.status} | Created: ${dayjs(selectedReport.createdAt).format('YYYY-MM-DD HH:mm')}`}
              type="info"
              style={{ marginBottom: 16 }}
            />
            
            {/* AI Insights Display */}
            <ReportInsightsDisplay
              insights={reportInsights[selectedReport.reportId]}
              loading={insightsLoading}
              onRefresh={() => generateInsights(selectedReport)}
              reportName={selectedReport.reportName}
              reportType={selectedReport.reportType}
            />
          </>
        )}
      </Drawer>

      {/* Report Preview Drawer */}
      <Drawer
        title={
          <Space>
            <FileTextOutlined />
            <span>Report Details</span>
          </Space>
        }
        width={600}
        open={previewDrawerVisible}
        onClose={() => setPreviewDrawerVisible(false)}
      >
        {selectedReport && (
          <div>
            <Space direction="vertical" style={{ width: '100%' }} size="middle">
              <Card title="Basic Information" size="small">
                <Row gutter={16}>
                  <Col span={12}>
                    <Text strong>Report Name:</Text>
                    <div>{selectedReport.reportName}</div>
                  </Col>
                  <Col span={12}>
                    <Text strong>Type:</Text>
                    <div>
                      <Tag color="blue">
                        {reportTypes.find(t => t.value === selectedReport.reportType)?.label || selectedReport.reportType}
                      </Tag>
                    </div>
                  </Col>
                  <Col span={12}>
                    <Text strong>Status:</Text>
                    <div>
                      <Space>
                        {getStatusIcon(selectedReport.status)}
                        <Tag color={reportStatuses.find(s => s.value === selectedReport.status)?.color}>
                          {reportStatuses.find(s => s.value === selectedReport.status)?.label || selectedReport.status}
                        </Tag>
                      </Space>
                    </div>
                  </Col>
                  <Col span={12}>
                    <Text strong>File Size:</Text>
                    <div>{selectedReport.fileSizeFormatted || (selectedReport.fileSize ? `${Math.round(selectedReport.fileSize / 1024)}KB` : 'Unknown')}</div>
                  </Col>
                </Row>
              </Card>

              <Card title="Time Information" size="small">
                <Row gutter={16}>
                  <Col span={12}>
                    <Text strong>Created At:</Text>
                    <div>{dayjs(selectedReport.createdAt).format('YYYY-MM-DD HH:mm:ss')}</div>
                  </Col>
                  <Col span={12}>
                    <Text strong>Period:</Text>
                    <div>
                      {selectedReport.startDate && selectedReport.endDate
                        ? `${dayjs(selectedReport.startDate).format('YYYY-MM-DD')} to ${dayjs(selectedReport.endDate).format('YYYY-MM-DD')}`
                        : selectedReport.endDate
                        ? dayjs(selectedReport.endDate).format('YYYY-MM-DD')
                        : 'Not specified'}
                    </div>
                  </Col>
                </Row>
              </Card>

              <Card title="Actions" size="small">
                <Space>
                  <Button
                    type="primary"
                    icon={<BulbOutlined />}
                    onClick={() => {
                      setPreviewDrawerVisible(false);
                      generateInsights(selectedReport);
                    }}
                    disabled={selectedReport.status !== 'COMPLETED'}
                  >
                    Generate AI Insights
                  </Button>
                  <Button
                    icon={<DownloadOutlined />}
                    onClick={() => downloadReport(selectedReport)}
                    disabled={selectedReport.status !== 'COMPLETED'}
                  >
                    Download Report
                  </Button>
                </Space>
              </Card>
            </Space>
          </div>
        )}
      </Drawer>
    </div>
  );
};

export default ReportManagement;