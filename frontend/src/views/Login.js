// frontend/src/views/Login.js
import React, { useState } from 'react';
import { Form, Input, Button, Tabs, Checkbox, Typography, Divider, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/authService';
import '../assets/styles/Login.css';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    try {
      setLoading(true);
      await AuthService.login(values.username, values.password, values.remember);
      message.success('Login successful');
      navigate('/dashboard');
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Login failed. Please check your credentials.';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleSSOLogin = async () => {
    try {
      const ssoUrl = await AuthService.getSsoLoginUrl();
      window.location.href = ssoUrl;
    } catch (error) {
      message.error('Failed to initiate SSO login');
    }
  };

  const handleRegisterClick = () => {
    navigate('/register');
  };

  const handleCompanyRegisterClick = () => {
    navigate('/register-company');
  };

  return (
    <div className="login-container">
      <div className="login-form-box">
        <Title level={3} style={{ textAlign: 'center', marginBottom: 32 }}>
          Welcome to AI Financial Management System
        </Title>

        <Tabs defaultActiveKey="1" centered>
          <TabPane tab="Login with Username" key="1">
            <Form name="login" onFinish={onFinish} layout="vertical">
              <Form.Item name="username" label="Username" rules={[{ required: true, message: 'Please enter your username' }]}>
                <Input placeholder="Username" />
              </Form.Item>
              <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please enter your password' }]}>
                <Input.Password placeholder="Password" />
              </Form.Item>
              <Form.Item name="remember" valuePropName="checked">
                <Checkbox>Remember me</Checkbox>
                <a style={{ float: 'right' }} href="/forgot-password">
                  Forgot password
                </a>
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" block loading={loading}>
                  Login
                </Button>
              </Form.Item>
            </Form>
          </TabPane>

          <TabPane tab="Login with Email" key="2">
            <Form name="email-login" layout="vertical">
              <Form.Item name="email" label="Email" rules={[
                { required: true, message: 'Please enter your email' },
                { type: 'email', message: 'Please enter a valid email' }
              ]}>
                <Input placeholder="Email" />
              </Form.Item>
              <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please enter your password' }]}>
                <Input.Password placeholder="Password" />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" block>
                  Login
                </Button>
              </Form.Item>
            </Form>
          </TabPane>
        </Tabs>

        <Divider plain className="custom-divider">Or</Divider>

        <Button 
          type="default" 
          block 
          onClick={handleSSOLogin}
          style={{ marginBottom: '10px' }}
        >
          Login with Microsoft SSO
        </Button>
        
        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          <Text>Don't have an account?</Text>
          <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'space-between' }}>
            <Button type="link" onClick={handleRegisterClick}>
              Register as User
            </Button>
            <Button type="link" onClick={handleCompanyRegisterClick}>
              Register Company
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;