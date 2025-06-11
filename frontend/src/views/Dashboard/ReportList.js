// frontend/src/views/Dashboard/ReportList.js
import React, { useState, useEffect } from 'react';
import {
  Card, Table, Button, Space, Tag, Select, DatePicker, Input,
  message, Modal, Tooltip, Progress, Typography, Row, Col, Statistic
} from 'antd';
import {
  FileTextOutlined, DownloadOutlined, DeleteOutlined, 
  AlertOutlined, ReloadOutlined, SearchOutlined,
  EyeOutlined, ClockCircleOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined, FileExcelOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import ReportService from '../../services/reportService';

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;
const { Option } = Select;
const { Search } = Input;

const ReportList = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [statistics, setStatistics] = useState(null);
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
      message.error('Failed to fetch reports');
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
    }
  };

  const handleDownload = async (report) => {
    try {
      await ReportService.downloadReport(report.reportId, report.reportName);
      message.success('Report downloaded successfully');
    } catch (error) {
      console.error('Download error:', error);
      message.error('Failed to download report');
    }
  };

  const handleArchive = async (reportId) => {
    try {
      await ReportService.archiveReport(reportId);
      message.success('Report archived successfully');
      fetchReports();
    } catch (error) {
      console.error('Archive error:', error);
      message.error('Failed to archive report');
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
        } catch (error) {
          console.error('Delete error:', error);
          message.error('Failed to delete report');
        }
      }
    });
  };

  const handleViewDetails = (report) => {
    Modal.info({
      title: 'Report Details',
      width: 600,
      content: (
        <div style={{ marginTop: 16 }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <div><Text strong>Report ID:</Text> {report.reportId}</div>
            <div><Text strong>Name:</Text> {report.reportName}</div>
            <div><Text strong>Type:</Text> {report.reportType}</div>
            <div><Text strong>Status:</Text> 
              <Tag color={reportStatuses.find(s => s.value === report.status)?.color}>
                {report.status}
              </Tag>
            </div>
            <div><Text strong>Period:</Text> {report.periodDescription}</div>
            <div><Text strong>Created:</Text> {dayjs(report.createdAt).format('YYYY-MM-DD HH:mm:ss')}</div>
            {report.completedAt && (
              <div><Text strong>Completed:</Text> {dayjs(report.completedAt).format('YYYY-MM-DD HH:mm:ss')}</div>
            )}
            <div><Text strong>File Size:</Text> {report.fileSizeFormatted || 'N/A'}</div>
            <div><Text strong>AI Analysis:</Text> {report.aiAnalysisEnabled ? 'Enabled' : 'Disabled'}</div>
            {report.errorMessage && (
              <div><Text strong>Error:</Text> <Text type="danger">{report.errorMessage}</Text></div>
            )}
          </Space>
        </div>
      )
    });
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'GENERATING':
        return <ClockCircleOutlined spin />;
      case 'COMPLETED':
        return <CheckCircleOutlined />;
      case 'FAILED':
        return <ExclamationCircleOutlined />;
      default:
        return <FileTextOutlined />;
    }
  };

  const columns = [
    {
      title: 'Report Name',
      dataIndex: 'reportName',
      key: 'reportName',
      ellipsis: true,
      render: (text, record) => (
        <Space>
          <FileExcelOutlined style={{ color: '#52c41a' }} />
          <Text strong>{text}</Text>
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
      dataIndex: 'periodDescription',
      key: 'period',
      width: 200
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
      render: (size) => size || '-'
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
          
          {record.isDownloadable && (
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

  return (
    <Card
      title={
        <>
          <FileTextOutlined style={{ marginRight: 8 }} />
          Report Management
        </>
      }
      style={{ margin: 24 }}
      extra={
        <Button 
          type="primary" 
          icon={<ReloadOutlined />} 
          onClick={() => { fetchReports(); fetchStatistics(); }}
          loading={loading}
        >
          Refresh
        </Button>
      }
    >
      {/* Statistics */}
      {statistics && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Row gutter={16}>
            <Col span={6}>
              <Statistic title="Total Reports" value={statistics.totalReports} />
            </Col>
            <Col span={6}>
              <Statistic 
                title="Completed" 
                value={statistics.completedReports} 
                valueStyle={{ color: '#3f8600' }}
              />
            </Col>
            <Col span={6}>
              <Statistic 
                title="Generating" 
                value={statistics.generatingReports} 
                valueStyle={{ color: '#1890ff' }}
              />
            </Col>
            <Col span={6}>
              <Statistic 
                title="Success Rate" 
                value={statistics.successRate} 
                precision={1}
                suffix="%" 
                valueStyle={{ color: '#3f8600' }}
              />
            </Col>
          </Row>
        </Card>
      )}

      {/* Filters */}
      <Card size="small" title="Filters" style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Select
              placeholder="Report Type"
              allowClear
              style={{ width: '100%' }}
              value={filters.reportType}
              onChange={(value) => setFilters({ ...filters, reportType: value })}
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
              placeholder="Status"
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
          
          <Col span={6}>
            <RangePicker
              style={{ width: '100%' }}
              value={filters.dateRange}
              onChange={(dateRange) => setFilters({ ...filters, dateRange })}
              placeholder={['Start Date', 'End Date']}
            />
          </Col>
          
          <Col span={6}>
            <Search
              placeholder="Search reports..."
              value={filters.searchTerm}
              onChange={(e) => setFilters({ ...filters, searchTerm: e.target.value })}
              onSearch={() => fetchReports()}
              enterButton={<SearchOutlined />}
            />
          </Col>
        </Row>
      </Card>

      {/* Reports Table */}
      <Table
        columns={columns}
        dataSource={reports}
        rowKey="reportId"
        loading={loading}
        pagination={{
          total: reports.length,
          pageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) => 
            `${range[0]}-${range[1]} of ${total} reports`
        }}
        scroll={{ x: 1200 }}
      />
    </Card>
  );
};

export default ReportList;