// frontend/src/views/RegisterCompany.js
import React, { useState } from 'react';
import { Form, Input, Button, Typography, Card, message, Steps } from 'antd';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/authService';

const { Title } = Typography;
const { Step } = Steps;

const RegisterCompany = () => {
  const [current, setCurrent] = useState(0);
  const [loading, setLoading] = useState(false);
  const [companyData, setCompanyData] = useState({});
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onCompanyInfoFinish = (values) => {
    setCompanyData({ ...values });
    setCurrent(1);
  };

  const onAdminInfoFinish = async (values) => {
    try {
      setLoading(true);
      const registrationData = {
        ...companyData,
        ...values
      };
      
      await AuthService.registerCompany(registrationData);
      message.success('Company registration successful! Please login.');
      navigate('/');
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Registration failed. Please try again.';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const prev = () => {
    setCurrent(current - 1);
  };

  const steps = [
    {
      title: 'Company Information',
      content: (
        <Form
          name="company-info"
          layout="vertical"
          onFinish={onCompanyInfoFinish}
          initialValues={companyData}
          form={form}
        >
          <Form.Item
            name="companyName"
            label="Company Name"
            rules={[{ required: true, message: 'Please input company name!' }]}
          >
            <Input placeholder="Company Name" />
          </Form.Item>

          <Form.Item
            name="email"
            label="Company Email"
            rules={[
              { required: true, message: 'Please input company email!' },
              { type: 'email', message: 'Please enter a valid email!' }
            ]}
          >
            <Input placeholder="company@example.com" />
          </Form.Item>

          <Form.Item
            name="address"
            label="Address"
            rules={[{ required: true, message: 'Please input company address!' }]}
          >
            <Input placeholder="Address" />
          </Form.Item>

          <Form.Item
            name="city"
            label="City"
            rules={[{ required: true, message: 'Please input city!' }]}
          >
            <Input placeholder="City" />
          </Form.Item>

          <Form.Item
            name="stateProvince"
            label="State/Province"
            rules={[{ required: true, message: 'Please input state or province!' }]}
          >
            <Input placeholder="State/Province" />
          </Form.Item>

          <Form.Item
            name="postalCode"
            label="Postal Code"
            rules={[{ required: true, message: 'Please input postal code!' }]}
          >
            <Input placeholder="Postal Code" />
          </Form.Item>

          <Form.Item
            name="registrationNumber"
            label="Registration Number"
            rules={[{ required: true, message: 'Please input registration number!' }]}
          >
            <Input placeholder="Registration Number" />
          </Form.Item>

          <Form.Item
            name="taxId"
            label="Tax ID"
            rules={[{ required: true, message: 'Please input tax ID!' }]}
          >
            <Input placeholder="Tax ID" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              Next
            </Button>
          </Form.Item>
        </Form>
      )
    },
    {
      title: 'Admin Account',
      content: (
        <Form
          name="admin-info"
          layout="vertical"
          onFinish={onAdminInfoFinish}
        >
          <Form.Item
            name="fullName"
            label="Admin Full Name"
            rules={[{ required: true, message: 'Please input admin name!' }]}
          >
            <Input placeholder="Full Name" />
          </Form.Item>

          <Form.Item
            name="username"
            label="Admin Username"
            rules={[
              { required: true, message: 'Please input username!' },
              { min: 4, message: 'Username must be at least 4 characters!' }
            ]}
          >
            <Input placeholder="Username" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Password"
            rules={[
              { required: true, message: 'Please input password!' },
              { min: 8, message: 'Password must be at least 8 characters!' }
            ]}
            hasFeedback
          >
            <Input.Password placeholder="Password" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Confirm Password"
            dependencies={['password']}
            hasFeedback
            rules={[
              { required: true, message: 'Please confirm your password!' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('The two passwords do not match!'));
                },
              }),
            ]}
          >
            <Input.Password placeholder="Confirm Password" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} style={{ marginRight: '10px' }}>
              Register
            </Button>
            <Button onClick={prev}>
              Previous
            </Button>
          </Form.Item>
        </Form>
      )
    }
  ];

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 600, padding: '20px' }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>
          Company Registration
        </Title>

        <Steps current={current} style={{ marginBottom: 30 }}>
          {steps.map(item => (
            <Step key={item.title} title={item.title} />
          ))}
        </Steps>

        <div>{steps[current].content}</div>
        
        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          Already have a company account? <a href="/">Login</a>
        </div>
      </Card>
    </div>
  );
};

export default RegisterCompany;