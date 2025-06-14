// frontend/src/views/Dashboard/ReportManagement.js
import React, { useState, useEffect } from 'react';
import {
  Card, Table, Button, Space, Tag, Select, DatePicker, Input,
  message, Modal, Tooltip, Progress, Typography, Row, Col, 
  Statistic, Descriptions, Drawer, Alert, Spin
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
 * Enhanced Report Management Component
 * 
 * Features:
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

  const reportTypes = ReportService.getReportTypes();
  const reportStatuses = ReportService.getReportStatuses();

  useEffect(() => {
    fetchReports();
    fetchStatistics();
  }, [filters]);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const filterParams = {
        ...filters,
        startDate: filters.dateRange?.[0]?.format('YYYY-MM-DD'),
        endDate: filters.dateRange?.[1]?.format('YYYY-MM-DD')
      };
      delete filterParams.dateRange;

      const response = await ReportService.getReports(filterParams);
      if (response.status === 'success') {
        setReports(response.data || []);
      }
    } catch (error) {
      console.error('Error fetching reports:', error);
      message.error('Failed to fetch reports: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchStatistics = async () => {
    try {
      const response = await ReportService.getReportStatistics();
      if (response.status === 'success') {
        setStatistics(response.data);
      }
    } catch (error) {
      console.error('Error fetching statistics:', error);
      // Don't show error message for statistics as it's not critical
    }
  };

  const handleViewDetails = async (report) => {
    try {
      // Fetch full report details
      const response = await ReportService.getReport(report.reportId);
      if (response.status === 'success') {
        setSelectedReport(response.data);
        setDetailsVisible(true);
      } else {
        throw new Error(response.message || 'Failed to fetch report details');
      }
    } catch (error) {
      console.error('Error fetching report details:', error);
      message.error('Failed to fetch report details: ' + error.message);
      
      // If API call fails, show basic details from table data
      setSelectedReport(report);
      setDetailsVisible(true);
    }
  };

  const handleDownload = async (report) => {
    try {
      if (!report.isDownloadable && report.status !== 'COMPLETED') {
        message.warning('This report is not ready for download yet');
        return;
      }

      // Fix the download function call
      const result = await ReportService.downloadReport(report.reportId, report.reportName);
      if (result.success) {
        message.success('Report downloaded successfully: ' + result.fileName);
      }
    } catch (error) {
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
      }
    });
  };

  const getStatusTag = (status) => {
    const statusConfig = reportStatuses.find(s => s.value === status);
    if (!statusConfig) return <Tag>{status}</Tag>;
    
    return (
      <Tag color={statusConfig.color} icon={getStatusIcon(status)}>
        {statusConfig.label}
      </Tag>
    );
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'GENERATING':
        return <ClockCircleOutlined />;
      case 'COMPLETED':
        return <CheckCircleOutlined />;
      case 'FAILED':
        return <ExclamationCircleOutlined />;
      default:
        return null;
    }
  };

  const getReportTypeIcon = (type) => {
    const reportType = reportTypes.find(rt => rt.value === type);
    return reportType ? reportType.icon : <FileTextOutlined />;
  };

  const columns = [
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      key: 'reportName',
      render: (text, record) => (
        <Space>
          {getReportTypeIcon(record.reportType)}
          <div>
            <div style={{ fontWeight: 500 }}>{text}</div>
            <Text type="secondary" style={{ fontSize: '12px' }}>
              ID: {record.reportId}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'reportType',
      key: 'reportType',
      render: (type) => {
        const reportType = reportTypes.find(rt => rt.value === type);
        return reportType ? reportType.label : type;
      },
      filters: reportTypes.map(type => ({ text: type.label, value: type.value })),
      onFilter: (value, record) => record.reportType === value,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
      filters: reportStatuses.map(status => ({ text: status.label, value: status.value })),
      onFilter: (value, record) => record.status === value,
    },
    {
      title: 'Period',
      key: 'period',
      render: (_, record) => (
        <div>
          <div>{dayjs(record.startDate).format('MMM DD, YYYY')}</div>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            to {dayjs(record.endDate).format('MMM DD, YYYY')}
          </Text>
        </div>
      ),
    },
    {
      title: 'File Size',
      dataIndex: 'fileSizeFormatted',
      key: 'fileSize',
      render: (size, record) => (
        record.status === 'COMPLETED' ? size || 'Unknown' : '-'
      ),
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date) => (
        <div>
          <div>{dayjs(date).format('MMM DD, YYYY')}</div>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {dayjs(date).format('HH:mm:ss')}
          </Text>
        </div>
      ),
      sorter: (a, b) => dayjs(a.createdAt).unix() - dayjs(b.createdAt).unix(),
      defaultSortOrder: 'descend',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Space size="small">
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
                icon={<AlertOutlined />} 
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
      )
    }
  ];

  const renderStatistics = () => {
    if (!statistics) return null;

    return (
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Statistic
              title="Total Reports"
              value={statistics.totalReports || 0}
              prefix={<FileTextOutlined />}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Completed"
              value={statistics.completedReports || 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Generating"
              value={statistics.generatingReports || 0}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Total Size"
              value={statistics.totalFileSizeFormatted || '0 B'}
              prefix={<FolderOutlined />}
            />
          </Col>
        </Row>
      </Card>
    );
  };

  const renderFilters = () => (
    <Card size="small" style={{ marginBottom: 16 }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Select
            placeholder="Filter by type"
            allowClear
            style={{ width: '100%' }}
            value={filters.reportType}
            onChange={(value) => setFilters({ ...filters, reportType: value })}
          >
            {reportTypes.map(type => (
              <Option key={type.value} value={type.value}>
                <Space>
                  {type.icon}
                  {type.label}
                </Space>
              </Option>
            ))}
          </Select>
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <Select
            placeholder="Filter by status"
            allowClear
            style={{ width: '100%' }}
            value={filters.status}
            onChange={(value) => setFilters({ ...filters, status: value })}
          >
            {reportStatuses.map(status => (
              <Option key={status.value} value={status.value}>
                <Tag color={status.color}>{status.label}</Tag>
              </Option>
            ))}
          </Select>
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <RangePicker
            placeholder={['Start date', 'End date']}
            style={{ width: '100%' }}
            value={filters.dateRange}
            onChange={(dates) => setFilters({ ...filters, dateRange: dates })}
          />
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <Search
            placeholder="Search reports..."
            allowClear
            value={filters.searchTerm}
            onChange={(e) => setFilters({ ...filters, searchTerm: e.target.value })}
            onSearch={() => fetchReports()}
          />
        </Col>
      </Row>
    </Card>
  );

  const renderReportDetails = () => {
    if (!selectedReport) return null;

    return (
      <Drawer
        title={
          <Space>
            <FileTextOutlined />
            Report Details
          </Space>
        }
        width={600}
        open={detailsVisible}
        onClose={() => {
          setDetailsVisible(false);
          setSelectedReport(null);
        }}
        extra={
          <Space>
            {selectedReport.status === 'COMPLETED' && (
              <Button
                type="primary"
                icon={<DownloadOutlined />}
                onClick={() => handleDownload(selectedReport)}
              >
                Download
              </Button>
            )}
          </Space>
        }
      >
        <Descriptions column={1} bordered>
          <Descriptions.Item label="Report ID">
            {selectedReport.reportId}
          </Descriptions.Item>
          
          <Descriptions.Item label="Report Name">
            {selectedReport.reportName}
          </Descriptions.Item>
          
          <Descriptions.Item label="Type">
            <Space>
              {getReportTypeIcon(selectedReport.reportType)}
              {reportTypes.find(rt => rt.value === selectedReport.reportType)?.label || selectedReport.reportType}
            </Space>
          </Descriptions.Item>
          
          <Descriptions.Item label="Status">
            {getStatusTag(selectedReport.status)}
          </Descriptions.Item>
          
          <Descriptions.Item label="Period">
            <Space direction="vertical" size={0}>
              <Text>
                <CalendarOutlined /> {dayjs(selectedReport.startDate).format('MMMM DD, YYYY')}
              </Text>
              <Text>
                <CalendarOutlined /> {dayjs(selectedReport.endDate).format('MMMM DD, YYYY')}
              </Text>
            </Space>
          </Descriptions.Item>
          
          <Descriptions.Item label="Created">
            <Space direction="vertical" size={0}>
              <Text>{dayjs(selectedReport.createdAt).format('MMMM DD, YYYY HH:mm:ss')}</Text>
              <Text type="secondary">
                <UserOutlined /> Created by user {selectedReport.createdBy || 'Unknown'}
              </Text>
            </Space>
          </Descriptions.Item>
          
          {selectedReport.completedAt && (
            <Descriptions.Item label="Completed">
              {dayjs(selectedReport.completedAt).format('MMMM DD, YYYY HH:mm:ss')}
            </Descriptions.Item>
          )}
          
          {selectedReport.status === 'COMPLETED' && (
            <Descriptions.Item label="File Information">
              <Space direction="vertical" size={0}>
                <Text>Format: {selectedReport.fileFormat || 'Excel (.xlsx)'}</Text>
                <Text>Size: {selectedReport.fileSizeFormatted || 'Unknown'}</Text>
                {selectedReport.filePath && (
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    Path: {selectedReport.filePath}
                  </Text>
                )}
              </Space>
            </Descriptions.Item>
          )}
          
          {selectedReport.aiAnalysisEnabled && (
            <Descriptions.Item label="AI Analysis">
              <Space direction="vertical" size={0}>
                <Text>
                  Enabled: {selectedReport.aiAnalysisEnabled ? 'Yes' : 'No'}
                </Text>
                {selectedReport.aiAnalysisStatus && (
                  <Text>Status: {selectedReport.aiAnalysisStatus}</Text>
                )}
              </Space>
            </Descriptions.Item>
          )}
          
          {selectedReport.errorMessage && selectedReport.status === 'FAILED' && (
            <Descriptions.Item label="Error Details">
              <Alert
                message="Report Generation Failed"
                description={selectedReport.errorMessage}
                type="error"
                showIcon
              />
            </Descriptions.Item>
          )}
        </Descriptions>

        {selectedReport.status === 'GENERATING' && (
          <div style={{ marginTop: 16 }}>
            <Alert
              message="Report Generation in Progress"
              description="This report is currently being generated. Please check back later or refresh the page for updates."
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
        rowKey={(record) => record.reportId}
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
            <Spin tip="Loading reports..." />
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