// frontend/src/views/Dashboard/ReportManagement.js
// FIXED Report Management with working delete functionality

import React, { useState, useEffect } from 'react';
import {
  Card, Table, Button, Space, message, Tag, Typography, Row, Col, 
  Select, DatePicker, Input, Statistic, Popconfirm, Modal, Alert, Spin
} from 'antd';
import {
  FileTextOutlined, DownloadOutlined, DeleteOutlined, EyeOutlined,
  CheckCircleOutlined, ClockCircleOutlined, ExclamationCircleOutlined,
  ReloadOutlined, SearchOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

const ReportManagement = () => {
  // State management
  const [reports, setReports] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(null); // Track which report is being deleted
  const [filters, setFilters] = useState({
    reportType: null,
    status: null,
    dateRange: null,
    searchTerm: ''
  });

  // Report types
  const reportTypes = [
    { value: 'BALANCE_SHEET', label: 'Balance Sheet' },
    { value: 'INCOME_STATEMENT', label: 'Income Statement' },
    { value: 'INCOME_EXPENSE', label: 'Income vs Expense' },
    { value: 'FINANCIAL_GROUPING', label: 'Financial Grouping' }
  ];

  // Status options
  const statusOptions = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'GENERATING', label: 'Generating' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'FAILED', label: 'Failed' }
  ];

  // Load data on component mount
  useEffect(() => {
    fetchReports();
    fetchStatistics();
  }, [filters]);

  // Fetch reports with applied filters
  const fetchReports = async () => {
    setLoading(true);
    try {
      console.log('[ReportManagement] Fetching reports with filters:', filters);
      
      // Build filter parameters
      const filterParams = {
        page: 0,
        size: 100,
        reportType: filters.reportType,
        status: filters.status,
        searchTerm: filters.searchTerm || undefined,
        startDate: filters.dateRange?.[0]?.format('YYYY-MM-DD'),
        endDate: filters.dateRange?.[1]?.format('YYYY-MM-DD')
      };
      delete filterParams.dateRange; // Remove dateRange as we've converted it

      console.log('Fetching reports with params:', filterParams);
      
      const response = await ReportService.getReports(filterParams);
      console.log('Reports response:', response);
      
      // Handle different response formats from backend
      let reportData = [];
      if (response && response.data) {
        // Response format: {status: 'success', data: [...]}
        reportData = Array.isArray(response.data) ? response.data : [];
      } else if (Array.isArray(response)) {
        // Direct array response
        reportData = response;
      } else if (response && Array.isArray(response.content)) {
        // Paginated response: {content: [...], totalElements: ...}
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
      setReports([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  };

  // Fetch statistics
  const fetchStatistics = async () => {
    try {
      // Try to fetch statistics, but don't fail if not available
      const response = await ReportService.getReportStatistics?.();
      if (response && response.data) {
        setStatistics(response.data);
      } else {
        // Generate basic statistics from reports data
        setStatistics(generateBasicStatistics(reports));
      }
    } catch (error) {
      console.log('Statistics not available, generating from reports data');
      setStatistics(generateBasicStatistics(reports));
    }
  };

  // Generate basic statistics from reports data
  const generateBasicStatistics = (reportsData) => {
    if (!Array.isArray(reportsData)) return null;
    
    return {
      totalReports: reportsData.length,
      completedReports: reportsData.filter(r => r.status === 'COMPLETED').length,
      generatingReports: reportsData.filter(r => r.status === 'GENERATING' || r.status === 'PENDING').length,
      failedReports: reportsData.filter(r => r.status === 'FAILED').length,
      totalFileSize: reportsData.reduce((sum, r) => sum + (r.fileSize || 0), 0),
      totalFileSizeFormatted: formatFileSize(reportsData.reduce((sum, r) => sum + (r.fileSize || 0), 0))
    };
  };

  // Format file size
  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // FIXED: Handle report deletion with proper error handling
  const handleDeleteReport = async (report) => {
    if (!report || !report.reportId) {
      message.error('Invalid report data');
      return;
    }

    Modal.confirm({
      title: 'Delete Report',
      content: `Are you sure you want to delete "${report.reportName}"?\n\nThis action cannot be undone.`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        setDeleting(report.reportId);
        try {
          console.log(`[ReportManagement] Deleting report ${report.reportId}:`, report.reportName);
          
          // Call delete API
          await ReportService.deleteReport(report.reportId);
          
          message.success('Report deleted successfully');
          
          // Refresh data
          await fetchReports();
          await fetchStatistics();
          
        } catch (error) {
          console.error('[ReportManagement] Delete error:', error);
          
          // Provide specific error messages
          if (error.message.includes('404')) {
            message.error('Report not found (may have already been deleted)');
          } else if (error.message.includes('403')) {
            message.error('You do not have permission to delete this report');
          } else if (error.message.includes('500')) {
            message.error('Server error occurred while deleting report');
          } else {
            message.error('Failed to delete report: ' + error.message);
          }
          
          // Still refresh in case the report was actually deleted
          fetchReports();
        } finally {
          setDeleting(null);
        }
      },
    });
  };

  // Handle download report
  const handleDownloadReport = async (report) => {
    try {
      console.log(`[ReportManagement] Downloading report ${report.reportId}:`, report.reportName);
      
      const response = await ReportService.downloadReport(report.reportId);
      
      // Create download link
      const url = window.URL.createObjectURL(response);
      const link = document.createElement('a');
      link.href = url;
      link.download = report.fileName || `${report.reportName}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      message.success('Report downloaded successfully');
      
    } catch (error) {
      console.error('[ReportManagement] Download error:', error);
      message.error('Failed to download report: ' + error.message);
    }
  };

  // Handle view report details
  const handleViewReport = (report) => {
    Modal.info({
      title: 'Report Details',
      width: 600,
      content: (
        <div>
          <p><strong>Report Name:</strong> {report.reportName}</p>
          <p><strong>Type:</strong> {report.reportType}</p>
          <p><strong>Status:</strong> <Tag color={getStatusColor(report.status)}>{report.status}</Tag></p>
          <p><strong>Created:</strong> {dayjs(report.createdAt).format('YYYY-MM-DD HH:mm:ss')}</p>
          <p><strong>Period:</strong> {report.startDate} to {report.endDate}</p>
          {report.fileSize && <p><strong>File Size:</strong> {formatFileSize(report.fileSize)}</p>}
          {report.fileName && <p><strong>File Name:</strong> {report.fileName}</p>}
          {report.errorMessage && (
            <Alert
              message="Error Details"
              description={report.errorMessage}
              type="error"
              showIcon
              style={{ marginTop: 16 }}
            />
          )}
        </div>
      ),
    });
  };

  // Get status color
  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'GENERATING': 
      case 'PENDING': return 'processing';
      case 'FAILED': return 'error';
      default: return 'default';
    }
  };

  // FIXED: Table columns with proper delete button logic
  const columns = [
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      key: 'reportName',
      ellipsis: true,
      render: (text, record) => (
        <div>
          <div style={{ fontWeight: 'bold' }}>{text}</div>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {record.reportType}
          </Text>
        </div>
      ),
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
      filters: statusOptions.map(option => ({ text: option.label, value: option.value })),
      onFilter: (value, record) => record.status === value,
    },
    {
      title: 'Period',
      key: 'period',
      width: 200,
      render: (_, record) => (
        <div>
          <div>{record.startDate}</div>
          <Text type="secondary">to {record.endDate}</Text>
        </div>
      ),
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (date) => dayjs(date).format('MMM DD, HH:mm'),
      sorter: (a, b) => dayjs(a.createdAt).unix() - dayjs(b.createdAt).unix(),
    },
    {
      title: 'File Size',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size) => size ? formatFileSize(size) : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handleViewReport(record)}
            size="small"
          >
            View
          </Button>
          
          {record.status === 'COMPLETED' && record.fileName && (
            <Button
              type="link"
              icon={<DownloadOutlined />}
              onClick={() => handleDownloadReport(record)}
              size="small"
            >
              Download
            </Button>
          )}
          
          {/* FIXED: Show delete button for all reports, with loading state */}
          <Popconfirm
            title="Delete Report"
            description="Are you sure you want to delete this report?"
            onConfirm={() => handleDeleteReport(record)}
            okText="Delete"
            cancelText="Cancel"
            okType="danger"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
              size="small"
              loading={deleting === record.reportId}
              disabled={deleting === record.reportId}
            >
              Delete
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // Render statistics cards
  const renderStatistics = () => {
    const stats = statistics || generateBasicStatistics(reports);
    
    return (
      <Row gutter={16} style={{ marginBottom: 24 }}>
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

  // Render filters
  const renderFilters = () => (
    <Card size="small" style={{ marginBottom: 16 }}>
      <Row gutter={16}>
        <Col span={6}>
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
        <Col span={6}>
          <Select
            placeholder="Filter by Status"
            allowClear
            style={{ width: '100%' }}
            value={filters.status}
            onChange={(value) => setFilters(prev => ({ ...prev, status: value }))}
          >
            {statusOptions.map(status => (
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
            format="YYYY-MM-DD"
          />
        </Col>
        <Col span={6}>
          <Input
            placeholder="Search reports..."
            prefix={<SearchOutlined />}
            value={filters.searchTerm}
            onChange={(e) => setFilters(prev => ({ ...prev, searchTerm: e.target.value }))}
            allowClear
          />
        </Col>
      </Row>
    </Card>
  );

  return (
    <div style={{ padding: '24px' }}>
      {/* Header */}
      <Card>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3}>
              <FileTextOutlined /> Report Management
            </Title>
            <Text type="secondary">
              Manage and download your generated financial reports
            </Text>
          </Col>
          <Col>
            <Button
              type="primary"
              icon={<ReloadOutlined />}
              onClick={fetchReports}
              loading={loading}
            >
              Refresh
            </Button>
          </Col>
        </Row>
      </Card>

      {/* Statistics */}
      {renderStatistics()}

      {/* Filters */}
      {renderFilters()}

      {/* Reports Table */}
      <Card title="Reports">
        <Table
          dataSource={reports}
          columns={columns}
          rowKey="reportId"
          loading={loading}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} reports`,
          }}
          scroll={{ x: 1000 }}
          locale={{
            emptyText: loading ? <Spin /> : 'No reports found. Generate some reports first!'
          }}
        />
      </Card>
    </div>
  );
};

export default ReportManagement;
