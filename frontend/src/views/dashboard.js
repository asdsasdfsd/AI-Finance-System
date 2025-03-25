// src/pages/Dashboard.jsx
import React, { useState } from 'react';
import { Layout, Menu } from 'antd';
import {
  HomeOutlined,
  PieChartOutlined,
  UserOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import '../assets/styles/Dashboard.css';

// 引入内容组件
import DashboardHome from './Dashboard/DashboardHome';
import DataManagement from './Dashboard/DataManagement';
import AdminData from './Dashboard/AdminData';
import SystemSettings from './Dashboard/SystemSettings';

const { Header, Sider, Content } = Layout;

const Dashboard = () => {
  const [selectedKey, setSelectedKey] = useState('1');

  const renderContent = () => {
    switch (selectedKey) {
      case '1':
        return <DashboardHome />;
      case '2':
        return <DataManagement />;
      case '3':
        return <AdminData />;
      case '4':
        return <SystemSettings />;
      default:
        return <DashboardHome />;
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible>
        <div className="logo">账目管理系统</div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={['1']}
          onClick={(e) => setSelectedKey(e.key)}
        >
          <Menu.Item key="1" icon={<HomeOutlined />}>
            后台首页
          </Menu.Item>
          <Menu.Item key="2" icon={<PieChartOutlined />}>
            数据管理
          </Menu.Item>
          <Menu.Item key="3" icon={<UserOutlined />}>
            管理员数据
          </Menu.Item>
          <Menu.Item key="4" icon={<SettingOutlined />}>
            系统设置
          </Menu.Item>
        </Menu>
      </Sider>

      <Layout>
        <Header className="dashboard-header">您好，管理员 👋</Header>
        <Content className="dashboard-content">
          {renderContent()}
        </Content>
      </Layout>
    </Layout>
  );
};

export default Dashboard;
