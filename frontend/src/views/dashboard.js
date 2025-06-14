// frontend/src/views/dashboard.js
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
  FileTextOutlined,
  BarChartOutlined,
  FundProjectionScreenOutlined,
  EyeOutlined,
  PlayCircleOutlined,
  UnorderedListOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/authService';
import '../assets/styles/Dashboard.css';

// Import existing content components
import DashboardHome from './Dashboard/DashboardHome';
import DataManagement from './Dashboard/DataManagement';
import SystemSettings from './Dashboard/SystemSettings';
import CompanyManagement from './Dashboard/CompanyManagement';
import UserManagement from './Dashboard/UserManagement';
import DepartmentManagement from './Dashboard/DepartmentManagement';
import FundManagement from './Dashboard/FundManagement';
import AssetManagement from './Dashboard/AssetManagement';
import TransactionManagement from './Dashboard/TransactionManagement';

// Import new unified financial reports components
import FinancialReportsUnified from './Dashboard/FinancialReportsUnified';
import ReportManagement from './Dashboard/ReportManagement';

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
      case '1':
        return <DashboardHome />;
      case '2':
        return <DataManagement />;
      case '3':
        return <SystemSettings />;
      case '4':
        return <CompanyManagement />;
      case '5':
        return <UserManagement />;
      case '6':
        return <DepartmentManagement />;
      case '7':
        return <FundManagement />;
      case '8':
        return <AssetManagement />;
      case '9':
        return <TransactionManagement />;
      
      // Updated Financial Reports Structure
      case '10': // Preview & Generate Reports
        return <FinancialReportsUnified />;
      case '11': // Report Management
        return <ReportManagement />;
        
      default:
        return <DashboardHome />;
    }
  };

  const menuItems = [
    {
      key: '1',
      icon: <HomeOutlined />,
      label: 'Dashboard Home'
    },
    {
      key: 'data',
      icon: <AppstoreOutlined />,
      label: 'Data Management',
      children: [
        {
          key: '2',
          icon: <PieChartOutlined />,
          label: 'General Data'
        },
        {
          key: '4',
          icon: <BankOutlined />,
          label: 'Company Management'
        },
        {
          key: '5',
          icon: <TeamOutlined />,
          label: 'User Management'
        },
        {
          key: '6',
          icon: <ApartmentOutlined />,
          label: 'Department Management'
        },
        {
          key: '7',
          icon: <FundOutlined />,
          label: 'Fund Management'
        },
        {
          key: '8',
          icon: <DollarCircleOutlined />,
          label: 'Asset Management'
        },
        {
          key: '9',
          icon: <BarChartOutlined />,
          label: 'Transaction Management'
        }
      ]
    },
    {
      key: 'reports',
      icon: <FileTextOutlined />,
      label: 'Financial Reports',
      children: [
        {
          key: '10',
          icon: <EyeOutlined />,
          label: 'Preview & Generate'
        },
        {
          key: '11',
          icon: <UnorderedListOutlined />,
          label: 'Report Management'
        }
      ]
    },
    {
      key: '3',
      icon: <SettingOutlined />,
      label: 'System Settings'
    }
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        collapsible 
        collapsed={collapsed} 
        onCollapse={setCollapsed}
        theme="dark"
        width={250}
      >
        <div className="logo" style={{ 
          height: 32, 
          margin: 16, 
          background: 'rgba(255, 255, 255, 0.3)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontWeight: 'bold'
        }}>
          {collapsed ? 'AI财务' : 'AI Financial Management'}
        </div>
        
        <Menu
          theme="dark"
          selectedKeys={[selectedKey]}
          mode="inline"
          items={menuItems}
          onClick={({ key }) => setSelectedKey(key)}
          style={{ borderRight: 0 }}
        />
      </Sider>
      
      <Layout>
        <Header style={{ 
          background: '#fff', 
          padding: '0 24px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 1px 4px rgba(0,21,41,.08)'
        }}>
          <div style={{ fontSize: '18px', fontWeight: '500' }}>
            {(() => {
              const getMenuLabel = (items, key) => {
                for (const item of items) {
                  if (item.key === key) {
                    return item.label;
                  }
                  if (item.children) {
                    const found = getMenuLabel(item.children, key);
                    if (found) return found;
                  }
                }
                return 'Dashboard';
              };
              return getMenuLabel(menuItems, selectedKey);
            })()}
          </div>
          
          <Dropdown overlay={userMenu} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
              <Avatar icon={<UserOutlined />} style={{ marginRight: 8 }} />
              <span>{currentUser?.username || 'User'}</span>
            </div>
          </Dropdown>
        </Header>
        
        <Content style={{ 
          margin: 0, 
          minHeight: 280,
          background: '#f0f2f5'
        }}>
          {renderContent()}
        </Content>
      </Layout>
    </Layout>
  );
};

export default Dashboard;