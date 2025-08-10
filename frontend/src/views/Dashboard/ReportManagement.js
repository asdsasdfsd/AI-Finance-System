// frontend/src/views/Dashboard/ReportManagement.js

import React, { useState, useEffect } from 'react';
import {
  Card, Table, Button, Space, Tag, Select, DatePicker, Input,
  message, Modal, Tooltip, Progress, Typography, Row, Col, 
  Statistic, Descriptions, Drawer, Alert, Spin, Divider, Empty
} from 'antd';
import {
  FileTextOutlined, DownloadOutlined, DeleteOutlined, 
  AlertOutlined, ReloadOutlined, SearchOutlined,
  EyeOutlined, ClockCircleOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined, FileExcelOutlined, InfoCircleOutlined,
  CalendarOutlined, UserOutlined, FolderOutlined, PlusOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;
const { Option } = Select;
const { Search } = Input;

/**
 * Fixed Report Management Component - Fully functional
 * 
 * Features:
 * - Compatible with backend DDD API
 * - Proper error handling and loading states
 * - View report details with drawer
 * - Download reports with progress feedback
 * - Archive/Delete with confirmation
 * - Filter and search functionality
 * - Statistics dashboard with fallback
 */
const ReportManagement = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [statistics, setStatistics] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [detailsVisible, setDetailsVisible] = useState(false);
  const [serviceStatus, setServiceStatus] = useState('unknown');
  const [filters, setFilters] = useState({
    reportType: null,
    status: null,
    dateRange: null,
    searchTerm: ''
  });

  // Get static options from service
  const reportTypes = ReportService.getReportTypes();
  const reportStatuses = ReportService.getReportStatuses();

  useEffect(() => {
    checkServiceHealth();
    fetchReports();
    fetchStatistics();
  }, []);

  // Separate effect for filter changes to avoid infinite loops
  useEffect(() => {
    const delayedFetch = setTimeout(() => {
      fetchReports();
    }, 500);
    
    return () => clearTimeout(delayedFetch);
  }, [filters]);

  /**
   * Check if backend service is running
   */
  const checkServiceHealth = async () => {
    try {
      const health = await ReportService.healthCheck();
      setServiceStatus(health.status === 'success' ? 'healthy' : 'unhealthy');
    } catch (error) {
      setServiceStatus('unhealthy');
    }
  };

  /**
   * Fetch reports with filters
   */
  const fetchReports = async () => {
    setLoading(true);
    try {
      const filterParams = {
        page: 0,
        size: 100,
        ...filters,
        startDate: filters.dateRange?.[0]?.format('YYYY-MM-DD'),
        endDate: filters.dateRange?.[1]?.format('YYYY-MM-DD')
      };
      delete filterParams.dateRange; // Remove dateRange as we've converted it

      console.log('Fetching reports with params:', filterParams);
      
      const response = await ReportService.getReports(filterParams);
      console.log('Reports response:', response);
      
      // Handle different response formats from backend
      let reportData = [];
      if (response && response.status === 'success' && response.data) {
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
      }
      
    } catch (error) {
      console.error('Error fetching reports:', error);
      message.error('Failed to fetch reports: ' + error.message);
      setReports([]);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Fetch statistics with fallback
   */
  const fetchStatistics = async () => {
    try {
      const response = await ReportService.getReportStatistics();
      if (response && response.status === 'success' && response.data) {
        setStatistics(response.data);
      } else {
        // Generate basic statistics from current reports
        const stats = generateBasicStatistics(reports);
        setStatistics(stats);
      }
    } catch (error) {
      console.error('Error fetching statistics:', error);
      // Generate basic statistics from current reports
      const stats = generateBasicStatistics(reports);
      setStatistics(stats);
    }
  };

  /**
   * Generate basic statistics from reports data as fallback
   */
  const generateBasicStatistics = (reportsData) => {
    const total = reportsData.length;
    const completed = reportsData.filter(r => r.status === 'COMPLETED').length;
    const generating = reportsData.filter(r => r.status === 'GENERATING').length;
    const failed = reportsData.filter(r => r.status === 'FAILED').length;
    const archived = reportsData.filter(r => r.status === 'ARCHIVED').length;
    
    return {
      totalReports: total,
      completedReports: completed,
      generatingReports: generating,
      failedReports: failed,
      archivedReports: archived
    };
  };

  /**
   * Handle view report details
   */
  const handleViewDetails = async (report) => {
    try {
      setSelectedReport(report);
      setDetailsVisible(true);
      
      // Try to fetch full report details
      try {
        const response = await ReportService.getReport(report.reportId);
        if (response && response.status === 'success' && response.data) {
          setSelectedReport(response.data);
        }
      } catch (detailError) {
        console.warn('Could not fetch detailed report info:', detailError);
        // Continue with basic report data
      }
    } catch (error) {
      console.error('Error viewing report details:', error);
      message.error('Failed to view report details: ' + error.message);
    }
  };

  /**
   * Handle download report
   */
  const handleDownload = async (report) => {
    if (report.status !== 'COMPLETED') {
      message.warning('This report is not ready for download yet. Status: ' + report.status);
      return;
    }

    const loadingMessage = message.loading('Downloading report...', 0);
    
    try {
      const result = await ReportService.downloadReport(report.reportId, report.reportName);
      loadingMessage();
      
      if (result && result.success) {
        message.success('Report downloaded successfully: ' + result.fileName);
      } else {
        message.success('Report download completed');
      }
    } catch (error) {
      loadingMessage();
      console.error('Download error:', error);
      message.error('Failed to download report: ' + error.message);
    }
  };

  /**
   * Handle archive report
   */
  const handleArchive = async (reportId) => {
    try {
      const response = await ReportService.archiveReport(reportId);
      if (response && response.status === 'success') {
        message.success('Report archived successfully');
        fetchReports();
        fetchStatistics();
      } else {
        message.error('Failed to archive report');
      }
    } catch (error) {
      console.error('Archive error:', error);
      message.error('Failed to archive report: ' + error.message);
    }
  };

  /**
   * Handle delete report with confirmation
   */
  const handleDelete = (report) => {
    Modal.confirm({
      title: 'Delete Report',
      content: `Are you sure you want to delete "${report.reportName}"? This action cannot be undone.`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          const response = await ReportService.deleteReport(report.reportId);
          if (response && response.status === 'success') {
            message.success('Report deleted successfully');
            fetchReports();
            fetchStatistics();
          } else {
            message.error('Failed to delete report');
          }
        } catch (error) {
          console.error('Delete error:', error);
          message.error('Failed to delete report: ' + error.message);
        }
      },
    });
  };

  /**
   * Reset all filters
   */
  const handleResetFilters = () => {
    setFilters({
      reportType: null,
      status: null,
      dateRange: null,
      searchTerm: ''
    });
  };

  /**
   * Render service status alert
   */
  const renderServiceStatus = () => {
    if (serviceStatus === 'unhealthy') {
      return (
        <Alert
          message="Backend Service Unavailable"
          description="The report service is not responding. Please check if the backend server is running on localhost:8085."
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
          action={
            <Button size="small" onClick={checkServiceHealth}>
              <ReloadOutlined /> Retry
            </Button>
          }
        />
      );
    }
    return null;
  };

  /**
   * Render statistics cards
   */
  const renderStatistics = () => {
    const stats = statistics || generateBasicStatistics(reports);
    
    return (
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="Total Reports"
              value={stats.totalReports || 0}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="Completed"
              value={stats.completedReports || 0}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="Generating"
              value={stats.generatingReports || 0}
              prefix={<ClockCircleOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="Failed"
              value={stats.failedReports || 0}
              prefix={<ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>
    );
  };

  /**
   * Render filters
   */
  const renderFilters = () => (
    <Card size="small" style={{ marginBottom: 16 }}>
      <Row gutter={16} align="middle">
        <Col span={5}>
          <Select
            placeholder="Filter by Type"
            allowClear
            style={{ width: '100%' }}
            value={filters.reportType}
            onChange={(value) => setFilters(prev => ({ ...prev, reportType: value }))}
          >
            {reportTypes.map(type => (
              <Option key={type.value} value={type.value}>
                {type.label}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={5}>
          <Select
            placeholder="Filter by Status"
            allowClear
            style={{ width: '100%' }}
            value={filters.status}
            onChange={(value) => setFilters(prev => ({ ...prev, status: value }))}
          >
            {reportStatuses.map(status => (
              <Option key={status.value} value={status.value}>
                {status.label}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={6}>
          <RangePicker
            style={{ width: '100%' }}
            value={filters.dateRange}
            onChange={(dates) => setFilters(prev => ({ ...prev, dateRange: dates }))}
            placeholder={['Start Date', 'End Date']}
          />
        </Col>
        <Col span={5}>
          <Search
            placeholder="Search reports..."
            value={filters.searchTerm}
            onChange={(e) => setFilters(prev => ({ ...prev, searchTerm: e.target.value }))}
            onSearch={(value) => setFilters(prev => ({ ...prev, searchTerm: value }))}
          />
        </Col>
        <Col span={3}>
          <Space>
            <Button onClick={handleResetFilters}>
              Reset
            </Button>
            <Button type="primary" onClick={fetchReports} icon={<ReloadOutlined />}>
              Refresh
            </Button>
          </Space>
        </Col>
      </Row>
    </Card>
  );

  /**
   * Get status tag color
   */
  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return 'green';
      case 'GENERATING': return 'blue';
      case 'FAILED': return 'red';
      case 'ARCHIVED': return 'orange';
      case 'PENDING': return 'default';
      default: return 'default';
    }
  };

  /**
   * Format file size
   */
  const formatFileSize = (size) => {
    if (!size) return '-';
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  };

  /**
   * Table columns definition
   */
  const columns = [
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      key: 'reportName',
      width: 250,
      render: (text, record) => (
        <div>
          <div style={{ fontWeight: 500 }}>{text}</div>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            ID: {record.reportId}
          </Text>
        </div>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'reportType',
      key: 'reportType',
      width: 150,
      render: (type) => {
        const typeInfo = reportTypes.find(t => t.value === type);
        return (
          <Tag color="blue">
            {typeInfo ? typeInfo.label : type}
          </Tag>
        );
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => (
        <Tag color={getStatusColor(status)}>
          {status}
        </Tag>
      ),
    },
    {
      title: 'Period',
      key: 'period',
      width: 200,
      render: (_, record) => {
        if (record.startDate && record.endDate) {
          return `${record.startDate} to ${record.endDate}`;
        }
        if (record.endDate) {
          return `As of ${record.endDate}`;
        }
        return '-';
      },
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date) => date ? dayjs(date).format('MMM DD, HH:mm') : '-',
    },
    {
      title: 'File Size',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: formatFileSize,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 200,
      render: (_, record) => (
        <Space>
          <Tooltip title="View Details">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetails(record)}
            />
          </Tooltip>
          
          {record.status === 'COMPLETED' && (
            <Tooltip title="Download">
              <Button
                type="text"
                icon={<DownloadOutlined />}
                onClick={() => handleDownload(record)}
              />
            </Tooltip>
          )}
          
          {record.status === 'COMPLETED' && (
            <Tooltip title="Archive">
              <Button
                type="text"
                icon={<FolderOutlined />}
                onClick={() => handleArchive(record.reportId)}
              />
            </Tooltip>
          )}
          
          {(record.status === 'FAILED' || record.status === 'ARCHIVED') && (
            <Tooltip title="Delete">
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
                onClick={() => handleDelete(record)}
              />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  /**
   * Render report details drawer
   */
  const renderReportDetails = () => {
    if (!selectedReport) return null;

    return (
      <Drawer
        title="Report Details"
        open={detailsVisible}
        onClose={() => setDetailsVisible(false)}
        width={600}
      >
        <Descriptions column={1} bordered>
          <Descriptions.Item label="Report Name">
            {selectedReport.reportName || 'Unnamed Report'}
          </Descriptions.Item>
          <Descriptions.Item label="Report Type">
            <Tag color="blue">
              {reportTypes.find(t => t.value === selectedReport.reportType)?.label || selectedReport.reportType}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={getStatusColor(selectedReport.status)}>
              {selectedReport.status}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Report Period">
            {selectedReport.startDate && selectedReport.endDate
              ? `${selectedReport.startDate} to ${selectedReport.endDate}`
              : selectedReport.endDate 
              ? `As of ${selectedReport.endDate}`
              : 'Not specified'}
          </Descriptions.Item>
          <Descriptions.Item label="Created At">
            {selectedReport.createdAt 
              ? dayjs(selectedReport.createdAt).format('YYYY-MM-DD HH:mm:ss')
              : 'Unknown'}
          </Descriptions.Item>
          <Descriptions.Item label="File Path">
            {selectedReport.filePath || 'Not available'}
          </Descriptions.Item>
          <Descriptions.Item label="File Size">
            {formatFileSize(selectedReport.fileSize)}
          </Descriptions.Item>
          <Descriptions.Item label="AI Analysis">
            {selectedReport.aiAnalysisEnabled ? 'Enabled' : 'Disabled'}
          </Descriptions.Item>
        </Descriptions>
        
        {selectedReport.status === 'COMPLETED' && (
          <div style={{ marginTop: 16 }}>
            <Button 
              type="primary" 
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(selectedReport)}
            >
              Download Report
            </Button>
          </div>
        )}
      </Drawer>
    );
  };

  return (
    <div>
      <Title level={2}>Report Management</Title>
      
      {renderServiceStatus()}
      {renderStatistics()}
      {renderFilters()}
      
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
            showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} reports`,
          }}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description={
                  <span>
                    No reports found
                    <br />
                    <Button 
                      type="link" 
                      onClick={() => window.location.href = '/dashboard/financial-reports'}
                    >
                      Generate your first report
                    </Button>
                  </span>
                }
              />
            ),
          }}
        />
      </Card>
      
      {renderReportDetails()}
    </div>
  );
};

export default ReportManagement;