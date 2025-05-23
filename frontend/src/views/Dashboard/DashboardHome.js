import React from 'react';
import { Row, Col, Card } from 'antd';
import { Column, Line, Pie } from '@ant-design/charts';
import "../../assets/styles/DashboardHome.css";

const DashboardHome = () => {
  const data = [
    { type: 'Jan', value: 30 },
    { type: 'Feb', value: 45 },
    { type: 'Mar', value: 60 },
    { type: 'Apr', value: 80 },
    { type: 'May', value: 55 },
    { type: 'Jun', value: 70 },
  ];

  const columnConfig = {
    data,
    xField: 'type',
    yField: 'value',
    height: 200,
    label: {
      position: 'top',
      style: {
        fill: '#000',
      },
    },
    meta: {
      type: { alias: 'Month' },
      value: { alias: 'Sales' },
    },
  };

  const lineConfig = {
    data,
    xField: 'type',
    yField: 'value',
    height: 200,
    label: {
      style: {
        fill: '#5B8FF9',
        fontWeight: 'bold',
      },
    },
    point: {
      size: 5,
      shape: 'diamond',
    },
    meta: {
      type: { alias: 'Month' },
      value: { alias: 'Revenue' },
    },
  };

  const pieData = [
    { type: 'Marketing', value: 27 },
    { type: 'Sales', value: 25 },
    { type: 'IT', value: 18 },
    { type: 'Finance', value: 15 },
    { type: 'Others', value: 15 },
  ];

  const pieConfig = {
    appendPadding: 10,
    data: pieData,
    angleField: 'value',
    colorField: 'type',
    radius: 1,
    label: {
      content: '{name} ({value})',
      style: {
        fontSize: 14,
      },
    },
    height: 400,
  };

  return (
    <div className="dashboard-home-container">
      <h2>📊 Welcome to the Dashboard Home</h2>
      <Row className="dashboard-row" gutter={16}>
        <Col span={16} className="dashboard-left">
          <Card title="Monthly Performance" bordered style={{ flex: 1 }}>
            <Column {...columnConfig} />
            <Line {...lineConfig} />
          </Card>
        </Col>
        <Col span={8} className="dashboard-right">
          <Card title="Category Distribution" bordered style={{ flex: 1 }}>
            <Pie {...pieConfig} />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default DashboardHome;



