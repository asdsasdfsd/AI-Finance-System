// frontend/src/views/dashboard.js
import React, { useState, useEffect } from 'react';
import { Layout, Menu, Button, Dropdown, Avatar } from 'antd';
import {
  HomeOutlined,
  PieChartOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/authService';
import '../assets/styles/Dashboard.css';

// Import content components
import DashboardHome from './Dashboard/DashboardHome';
import DataManagement from './Dashboard/DataManagement';
import AdminData from './Dashboard/AdminData';
import SystemSettings from './Dashboard/SystemSettings';

const { Header, Sider, Content } = Layout;

const Dashboard = () => {
  const [selectedKey, setSelectedKey] = useState('1');
  const [currentUser, setCurrentUser] = useState(null);
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Get current user from local storage
    const user = AuthService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    } else {
      // If no user found, redirect to login
      navigate('/');
    }
  }, [navigate]);

  const handleLogout = async () => {
    await AuthService.logout();
    navigate('/');
  };

  const userMenu = (
    <Menu>
      <Menu.Item key="profile" icon={<UserOutlined />}>
        Profile
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
        Logout
      </Menu.Item>
    </Menu>
  );

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
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div className="logo">财务管理系统</div>
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
        <Header className="dashboard-header">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', height: '100%' }}>
            <span>Dashboard</span>
            {currentUser && currentUser.user && (
              <Dropdown overlay={userMenu} trigger={['click']}>
                <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
                  <Avatar icon={<UserOutlined />} style={{ marginRight: '8px', backgroundColor: '#1890ff' }} />
                  <span style={{ marginRight: '8px', color: '#333' }}>
                    Hi! {currentUser.user.username || currentUser.user.fullName}
                  </span>
                </div>
              </Dropdown>
            )}
          </div>
        </Header>
        <Content className="dashboard-content">
          {renderContent()}
        </Content>
      </Layout>
    </Layout>
  );
};

export default Dashboard;