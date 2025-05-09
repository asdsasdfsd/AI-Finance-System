// frontend/src/views/Login.js
import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Tabs, Checkbox, Typography, Divider, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/authService';
import '../assets/styles/Login.css';
// Remove unused import
// import axios from 'axios';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Check if user is already logged in
  useEffect(() => {
    // Clear any stale tokens or errors from URL
    const urlParams = new URLSearchParams(window.location.search);
    const hasError = urlParams.has('error') || urlParams.has('error_description');
    
    if (hasError) {
      // If there are error parameters in the URL, clear them by navigating to clean login page
      navigate('/');
      return;
    }
    
    // Check for valid login
    const currentUser = AuthService.getCurrentUser();
    if (currentUser) {
      // If user already has valid token, redirect to dashboard
      navigate('/dashboard');
    }
  }, [navigate]);

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
      setLoading(true);
      
      // use AuthService to get SSO login URL
      const ssoUrl = await AuthService.getSsoLoginUrl();
      window.location.href = ssoUrl;
    } catch (error) {
      console.error('SSO Login Error:', error);
      
      // check error message
      const errorMsg = error.response?.data?.message || 
                     error.response?.data?.error || 
                     'Microsoft Login Init Failure';
                     
      message.error(errorMsg);
    } finally {
      setLoading(false);
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