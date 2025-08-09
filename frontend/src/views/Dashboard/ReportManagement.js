// frontend/src/views/Dashboard/ReportManagement.js
import React, { useState, useEffect } from 'react';
import {
  Card, Table, Button, Space, Tag, Select, DatePicker, Input,
  message, Modal, Tooltip, Progress, Typography, Row, Col, 
  Statistic, Descriptions, Drawer, Alert, Spin, Divider
} from 'antd';
import {
  FileTextOutlined, DownloadOutlined, DeleteOutlined, 
  AlertOutlined, ReloadOutlined, SearchOutlined,
  EyeOutlined, ClockCircleOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined, FileExcelOutlined, InfoCircleOutlined,
  CalendarOutlined, UserOutlined, FolderOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;
const { Option } = Select;
const { Search } = Input;

/**
 * Fixed Report Management Component
 * 
 * Features:
 * - Properly handle backend API responses
 * - View report details
 * - Download reports
 * - Archive/Delete reports
 * - Filter and search
 * - Statistics dashboard
 */
const ReportManagement = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [statistics, setStatistics] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [detailsVisible, setDetailsVisible] = useState(false);
  const [filters, setFilters] = useState({
    reportType: null,
    status: null,
    dateRange: null,
    searchTerm: ''
  });

  // Static options - fallback if backend doesn't provide them
  const reportTypes = [
    { value: 'BALANCE_SHEET', label: 'Balance Sheet' },
    { value: 'INCOME_STATEMENT', label: 'Income Statement' },
    { value: 'INCOME_EXPENSE', label: 'Income & Expense' },
    { value: 'FINANCIAL_GROUPING', label: 'Financial Grouping' },
  ];

  const reportStatuses = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'GENERATING', label: 'Generating' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'FAILED', label: 'Failed' },
    { value: 'ARCHIVED', label: 'Archived' },
  ];

  useEffect(() => {
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

  const fetchStatistics = async () => {
    try {
      // Try to fetch statistics, but don't fail if not available
      const response = await ReportService.getReportStatistics?.();
      if (response && response.data) {
        setStatistics(response.data);
      }
    } catch (error) {
      console.error('Error fetching statistics:', error);
      // Don't show error message for statistics as it's not critical
      // Generate basic statistics from current reports
      const stats = generateBasicStatistics(reports);
      setStatistics(stats);
    }
  };

  // Generate basic statistics from reports data
  const generateBasicStatistics = (reportsData) => {
    const total = reportsData.length;
    const completed = reportsData.filter(r => r.status === 'COMPLETED').length;
    const generating = reportsData.filter(r => r.status === 'GENERATING').length;
    const failed = reportsData.filter(r => r.status === 'FAILED').length;
    
    return {
      totalReports: total,
      completedReports: completed,
      generatingReports: generating,
      failedReports: failed
    };
  };

  const handleViewDetails = async (report) => {
    try {
      setSelectedReport(report);
      setDetailsVisible(true);
      
      // Try to fetch full report details
      try {
        const response = await ReportService.getReport(report.reportId);
        if (response && response.data) {
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

  const handleDownload = async (report) => {
    try {
      if (!report.isDownloadable && report.status !== 'COMPLETED') {
        message.warning('This report is not ready for download yet');
        return;
      }

      message.loading('Downloading report...', 0);
      const result = await ReportService.downloadReport(report.reportId, report.reportName);
      message.destroy(); // Clear loading message
      
      if (result && result.success) {
        message.success('Report downloaded successfully: ' + result.fileName);
      } else {
        message.success('Report download started');
      }
    } catch (error) {
      message.destroy(); // Clear loading message
      console.error('Download error:', error);
      message.error('Failed to download report: ' + error.message);
    }
  };

  const handleArchive = async (reportId) => {
    try {
      await ReportService.archiveReport(reportId);
      message.success('Report archived successfully');
      fetchReports();
      fetchStatistics();
    } catch (error) {
      console.error('Archive error:', error);
      message.error('Failed to archive report: ' + error.message);
    }
  };

  const handleDelete = (report) => {
    Modal.confirm({
      title: 'Delete Report',
      content: `Are you sure you want to delete "${report.reportName}"? This action cannot be undone.`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await ReportService.deleteReport(report.reportId);
          message.success('Report deleted successfully');
          fetchReports();
          fetchStatistics();
        } catch (error) {
          console.error('Delete error:', error);
          message.error('Failed to delete report: ' + error.message);
        }
      },
    });
  };

  // Render statistics cards
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
        <Col span={6}>
          <Search
            placeholder="Search reports..."
            allowClear
            value={filters.searchTerm}
            onChange={(e) => setFilters(prev => ({ ...prev, searchTerm: e.target.value }))}
            onSearch={() => fetchReports()}
          />
        </Col>
      </Row>
    </Card>
  );

  // Table columns
  const columns = [
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      key: 'reportName',
      ellipsis: true,
      render: (text, record) => (
        <Space>
          <FileTextOutlined />
          <span>{text || 'Unnamed Report'}</span>
        </Space>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'reportType',
      key: 'reportType',
      width: 150,
      render: (type) => {
        const typeObj = reportTypes.find(t => t.value === type);
        return (
          <Tag color="blue">
            {typeObj ? typeObj.label : type}
          </Tag>
        );
      },
    },
    {
      title: 'Period',
      key: 'period',
      width: 150,
      render: (_, record) => {
        if (record.startDate && record.endDate) {
          return (
            <span>
              {dayjs(record.startDate).format('MMM DD')} - {dayjs(record.endDate).format('MMM DD, YYYY')}
            </span>
          );
        }
        return '-';
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => {
        let color = 'default';
        let icon = <InfoCircleOutlined />;
        
        switch (status) {
          case 'COMPLETED':
            color = 'green';
            icon = <CheckCircleOutlined />;
            break;
          case 'GENERATING':
            color = 'blue';
            icon = <ClockCircleOutlined />;
            break;
          case 'FAILED':
            color = 'red';
            icon = <ExclamationCircleOutlined />;
            break;
          case 'PENDING':
            color = 'orange';
            icon = <ClockCircleOutlined />;
            break;
          default:
            break;
        }
        
        return (
          <Tag color={color} icon={icon}>
            {status}
          </Tag>
        );
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
      render: (size) => {
        if (!size) return '-';
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
        return `${(size / (1024 * 1024)).toFixed(1)} MB`;
      },
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

  // Render report details drawer
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
            <Tag color={selectedReport.status === 'COMPLETED' ? 'green' : 'orange'}>
              {selectedReport.status}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Period">
            {selectedReport.startDate && selectedReport.endDate
              ? `${dayjs(selectedReport.startDate).format('YYYY-MM-DD')} to ${dayjs(selectedReport.endDate).format('YYYY-MM-DD')}`
              : 'N/A'
            }
          </Descriptions.Item>
          <Descriptions.Item label="Created At">
            {selectedReport.createdAt ? dayjs(selectedReport.createdAt).format('YYYY-MM-DD HH:mm:ss') : 'N/A'}
          </Descriptions.Item>
          <Descriptions.Item label="File Size">
            {selectedReport.fileSize ? `${(selectedReport.fileSize / 1024).toFixed(1)} KB` : 'N/A'}
          </Descriptions.Item>
          <Descriptions.Item label="File Path">
            {selectedReport.filePath || 'N/A'}
          </Descriptions.Item>
          {selectedReport.errorMessage && (
            <Descriptions.Item label="Error Message">
              <Text type="danger">{selectedReport.errorMessage}</Text>
            </Descriptions.Item>
          )}
        </Descriptions>

        {selectedReport.status === 'GENERATING' && (
          <div style={{ marginTop: 16 }}>
            <Alert
              message="Report is being generated"
              description="Please check back later or refresh the page for updates."
              type="info"
              showIcon
              icon={<ClockCircleOutlined />}
            />
            <Progress 
              percent={undefined} 
              status="active" 
              style={{ marginTop: 12 }}
            />
          </div>
        )}

        {selectedReport.status === 'COMPLETED' && (
          <div style={{ marginTop: 16 }}>
            <Button
              type="primary"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(selectedReport)}
              block
            >
              Download Report
            </Button>
          </div>
        )}
      </Drawer>
    );
  };

  return (
    <Card
      title={
        <Space>
          <FileTextOutlined />
          Report Management
        </Space>
      }
      style={{ margin: 24 }}
      extra={
        <Button 
          type="primary" 
          icon={<ReloadOutlined />} 
          onClick={() => { 
            fetchReports(); 
            fetchStatistics(); 
          }}
          loading={loading}
        >
          Refresh
        </Button>
      }
    >
      {/* Statistics */}
      {renderStatistics()}

      {/* Filters */}
      {renderFilters()}

      {/* Reports Table */}
      <Table
        columns={columns}
        dataSource={reports}
        rowKey={(record) => record.reportId || record.id}
        loading={loading}
        pagination={{
          total: reports.length,
          pageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) => 
            `${range[0]}-${range[1]} of ${total} reports`,
        }}
        scroll={{ x: 1200 }}
        locale={{
          emptyText: loading ? (
            <div style={{ textAlign: 'center', padding: '20px' }}>
              <Spin size="large" />
              <div style={{ marginTop: '16px' }}>Loading reports...</div>
            </div>
          ) : (
            <div style={{ padding: '20px', textAlign: 'center' }}>
              <FileTextOutlined style={{ fontSize: '48px', color: '#d9d9d9' }} />
              <div style={{ marginTop: '16px' }}>
                <Text type="secondary">No reports found</Text>
              </div>
              <div style={{ marginTop: '8px' }}>
                <Text type="secondary" style={{ fontSize: '12px' }}>
                  Generate your first report to see it here
                </Text>
              </div>
              <div style={{ marginTop: '16px' }}>
                <Button 
                  type="primary" 
                  onClick={fetchReports}
                  icon={<ReloadOutlined />}
                >
                  Refresh
                </Button>
              </div>
            </div>
          )
        }}
      />

      {/* Report Details Drawer */}
      {renderReportDetails()}
    </Card>
  );
};

export default ReportManagement;