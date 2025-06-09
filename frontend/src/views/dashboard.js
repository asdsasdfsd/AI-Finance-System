import React, { useState, useEffect } from 'react';
import { Layout, Menu, Dropdown, Avatar } from 'antd';
import {
  HomeOutlined,
  PieChartOutlined,
  UserOutlined,
  SettingOutlined,
  TeamOutlined,
  BankOutlined,
  ApartmentOutlined,
  LogoutOutlined,
  FundOutlined,
  AppstoreOutlined,
  DollarCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/authService';
import '../assets/styles/Dashboard.css';

// Import content components
import DashboardHome from './Dashboard/DashboardHome';
import DataManagement from './Dashboard/DataManagement';
import SystemSettings from './Dashboard/SystemSettings';
import CompanyManagement from './Dashboard/CompanyManagement';
import UserManagement from './Dashboard/UserManagement';
import DepartmentManagement from './Dashboard/DepartmentManagement';
import FundManagement from './Dashboard/FundManagement';
import AssetManagement from './Dashboard/AssetManagement';
import TransactionManagement from './Dashboard/TransactionManagement';
import BalanceSheet from './Dashboard/BalanceSheet';

const { Header, Sider, Content } = Layout;

const Dashboard = () => {
  const [selectedKey, setSelectedKey] = useState('1');
  const [currentUser, setCurrentUser] = useState(null);
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    } else {
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
      case '1': return <DashboardHome />;
      case '2': return <DataManagement />;
      case '3': return <BalanceSheet />;
      case '4': return <CompanyManagement />;
      case '5': return <UserManagement />;
      case '6': return <DepartmentManagement />;
      case '7': return <TransactionManagement />;
      case '8': return <FundManagement />;
      case '9': return <AssetManagement />;
      case '10': return <SystemSettings />;
      default: return <DashboardHome />;
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div className="logo">Finance Management System</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]} // ✅ 修复：绑定状态
          onClick={(e) => setSelectedKey(e.key)}
        >
          <Menu.Item key="1" icon={<HomeOutlined />}>
            Dashboard Home
          </Menu.Item>
          <Menu.Item key="2" icon={<PieChartOutlined />}>
            Data Management
          </Menu.Item>
          <Menu.Item key="3" icon={<PieChartOutlined />}>
            Balance Sheet
          </Menu.Item>
          <Menu.SubMenu key="organization" icon={<TeamOutlined />} title="Organization Management">
            <Menu.Item key="4" icon={<BankOutlined />}>
              Company Management
            </Menu.Item>
            <Menu.Item key="5" icon={<UserOutlined />}>
              User Management
            </Menu.Item>
            <Menu.Item key="6" icon={<ApartmentOutlined />}>
              Department Management
            </Menu.Item>
          </Menu.SubMenu>
          <Menu.Item key="7" icon={<SettingOutlined />}>
            Transaction Management
          </Menu.Item>
          <Menu.Item key="8" icon={<FundOutlined />}>
            Fund Management
          </Menu.Item>
          <Menu.Item key="9" icon={<AppstoreOutlined />}>
            Asset Management
          </Menu.Item>
          <Menu.Item key="10" icon={<DollarCircleOutlined />}>
            System Settings
          </Menu.Item>
        </Menu>
      </Sider>

      <Layout>
        <Header className="dashboard-header">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', height: '100%' }}>
            <span style={{ display: 'flex', alignItems: 'center', fontSize: '18px', fontWeight: '500' }}>
              <HomeOutlined style={{ marginRight: '8px', color: '#1890ff' }} />
              Dashboard
            </span>
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
