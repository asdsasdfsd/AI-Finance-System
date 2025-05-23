import React from 'react';
import { Card, Form, Switch, Select, Typography, Divider } from 'antd';
import '../../assets/styles/SystemSettings.css';

const { Title, Text } = Typography;
const { Option } = Select;

const SystemSettings = () => {
  return (
    <div className="system-settings-container">
      <Title level={3} className="system-settings-title">⚙️ System Settings</Title>
      <Text className="system-settings-description">Manage your frontend preferences</Text>

      <Divider />

      <Card title="Display Settings" bordered={false} className="system-settings-card">
        <Form layout="vertical">
          <Form.Item label="Dark Mode">
            <Switch defaultChecked />
          </Form.Item>

          <Form.Item label="Language">
            <Select defaultValue="en-US" style={{ width: 200 }}>
              <Option value="en-US">English</Option>
              <Option value="zh-CN">Chinese (Simplified)</Option>
              <Option value="ja-JP">Japanese</Option>
            </Select>
          </Form.Item>

          <Form.Item label="Layout Style">
            <Select defaultValue="side" style={{ width: 200 }}>
              <Option value="side">Sidebar Navigation</Option>
              <Option value="top">Top Navigation</Option>
              <Option value="mix">Mixed Layout</Option>
            </Select>
          </Form.Item>
        </Form>
      </Card>

      <Card title="Feature Toggles" bordered={false} className="system-settings-card">
        <Form layout="vertical">
          <Form.Item label="Enable Page Transitions">
            <Switch defaultChecked />
          </Form.Item>

          <Form.Item label="Enable Keyboard Shortcuts">
            <Switch />
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default SystemSettings;
