// frontend/src/views/SsoCallback.js
import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Spin, Typography, Card, message } from 'antd';
import axios from 'axios';
import AuthService from '../services/authService';

const { Title, Text } = Typography;

/**
 * Component to handle Microsoft SSO callback
 * Processes the code returned by Microsoft and exchanges it for a token
 */
const SsoCallback = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const authenticateWithSso = async () => {
      try {
        // Get code and state from URL query parameters
        const urlParams = new URLSearchParams(location.search);
        const code = urlParams.get('code');
        const state = urlParams.get('state');
        
        if (!code) {
          setError('Authorization code is missing');
          setLoading(false);
          return;
        }
        
        // Exchange code for token
        const response = await axios.post('/api/auth/sso/login', null, {
          params: { code, state }
        });
        
        // Save authentication data
        if (response.data.token) {
          localStorage.setItem('user', JSON.stringify(response.data));
          message.success('SSO login successful');
          navigate('/dashboard');
        } else {
          setError('Authentication failed: Invalid response');
        }
      } catch (err) {
        setError(`Authentication failed: ${err.response?.data?.message || err.message}`);
        message.error('SSO authentication failed');
      } finally {
        setLoading(false);
      }
    };

    authenticateWithSso();
  }, [location, navigate]);

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        background: '#f0f2f5'
      }}>
        <Card style={{ width: 400, textAlign: 'center', padding: '30px' }}>
          <Spin size="large" />
          <Title level={4} style={{ marginTop: 20 }}>
            Processing your SSO login...
          </Title>
          <Text type="secondary">Please wait while we authenticate you</Text>
        </Card>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        background: '#f0f2f5'
      }}>
        <Card style={{ width: 400, textAlign: 'center', padding: '30px' }}>
          <Title level={4} style={{ color: '#ff4d4f' }}>
            Authentication Error
          </Title>
          <Text type="danger">{error}</Text>
          <div style={{ marginTop: 20 }}>
            <a href="/">Return to login</a>
          </div>
        </Card>
      </div>
    );
  }

  return null;
};

export default SsoCallback;