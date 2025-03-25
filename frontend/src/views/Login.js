import React from 'react';
import { Form, Input, Button, Tabs, Checkbox, Typography, Divider } from 'antd';
import { useNavigate } from 'react-router-dom';
import '../assets/styles/Login.css';

const { Title, Text } = Typography;

const Login = () => {

  const navigate = useNavigate();

  const onFinish = (values) => {
    console.log('登录信息:', values);
    //如果验证成功
    navigate('/dashboard');
  };

  const handleSSOLogin = () => {
    window.location.href = 'https://sso.example.com/login?redirect_uri=http://localhost:3000/dashboard';
  };

  return (
    <div className="login-container">
      <div className="login-form-box">
        <Title level={3} style={{ textAlign: 'center', marginBottom: 32 }}>
          欢迎登录 AI 财务管理系统
        </Title>

        <Tabs defaultActiveKey="1" centered>
          <Tabs.TabPane tab="账号密码登录" key="1">
          <Text type="secondary">（这下面是表单样例，可以替换）</Text>
            <Form name="login" onFinish={onFinish} layout="vertical">
              <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
                <Input placeholder="请输入用户名" />
              </Form.Item>
              <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
                <Input.Password placeholder="请输入密码" />
              </Form.Item>
              <Form.Item>
                <Checkbox>自动登录</Checkbox>
                <a style={{ float: 'right' }}>忘记密码</a>
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" block>
                  登录
                </Button>
              </Form.Item>
            </Form>
          </Tabs.TabPane>

          <Tabs.TabPane tab="手机号登录" key="2">
            <Text type="secondary">（这里你可以添加手机号 + 验证码的表单）</Text>
          </Tabs.TabPane>
        </Tabs>

        <Divider plain className="custom-divider"></Divider>

        <Button type="default" block onClick={handleSSOLogin}>
          使用公司账号（SSO）登录
        </Button>
      </div>
    </div>
  );
};

export default Login;
