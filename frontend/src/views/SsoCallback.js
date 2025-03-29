// frontend/src/views/SsoCallback.js
import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Spin, Typography, Card, message, Button, Alert } from 'antd';
import AuthService from '../services/authService';

const { Title, Text, Paragraph } = Typography;

/**
 * Component to handle Microsoft SSO callback
 * Processes the code returned by Microsoft and exchanges it for a token
 */
const SsoCallback = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newUserCreated, setNewUserCreated] = useState(false);
  const [newCompanyCreated, setNewCompanyCreated] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Clear any existing tokens first
    localStorage.removeItem('user');
    
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
        
        // Use AuthService to exchange code for token
        const response = await AuthService.processSsoLogin(code, state);
        
        // Check for user/company auto-provisioning flags
        if (response.newUserCreated) {
          setNewUserCreated(true);
        }
        
        if (response.newCompanyCreated) {
          setNewCompanyCreated(true);
        }
        
        message.success('SSO login successful');
        
        // Delayed navigation to show the user creation message if applicable
        if (newUserCreated || newCompanyCreated) {
          setTimeout(() => {
            navigate('/dashboard');
          }, 3000);
        } else {
          navigate('/dashboard');
        }
      } catch (err) {
        console.error('Authentication error:', err);
        // Extract error message from different possible locations
        const errorMsg = err.response?.data?.error?.message || 
                        err.response?.data?.message || 
                        err.message || 
                        'Unknown error occurred';
        setError(`Authentication failed: ${errorMsg}`);
        message.error('SSO authentication failed');
      } finally {
        setLoading(false);
      }
    };

    authenticateWithSso();
  }, []);

  // Show success message with info about auto-provisioning
  if (!loading && !error && (newUserCreated || newCompanyCreated)) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        background: '#f0f2f5'
      }}>
        <Card style={{ width: 500, textAlign: 'center', padding: '30px' }}>
          <Title level={3} style={{ color: '#52c41a' }}>
            Login Successful!
          </Title>
          
          {newUserCreated && (
            <Alert
              message="New Account Created"
              description="We've automatically created a new user account based on your Microsoft profile."
              type="success"
              showIcon
              style={{ marginBottom: '20px', textAlign: 'left' }}
            />
          )}
          
          {newCompanyCreated && (
            <Alert
              message="New Company Created"
              description="We've automatically created a company account based on your email domain. You can update company details in your profile settings."
              type="info"
              showIcon
              style={{ marginBottom: '20px', textAlign: 'left' }}
            />
          )}
          
          <Paragraph>
            You'll be redirected to the dashboard automatically...
          </Paragraph>
          
          <Button type="primary" onClick={() => navigate('/dashboard')} style={{ marginRight: '10px' }}>
            Go to Dashboard
          </Button>
          {(newUserCreated || newCompanyCreated) && (
            <Button type="default" onClick={() => navigate('/profile-completion')}>
              Complete Your Profile
            </Button>
          )}
        </Card>
      </div>
    );
  }

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
            Processing your Microsoft login...
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
        <Card style={{ width: 450, textAlign: 'center', padding: '30px' }}>
          <Title level={4} style={{ color: '#ff4d4f' }}>
            Authentication Error
          </Title>
          <Alert
            message="Login Failed"
            description={error}
            type="error"
            showIcon
            style={{ marginBottom: '20px', textAlign: 'left' }}
          />
          <div style={{ marginTop: 20 }}>
            <Button type="primary" onClick={() => navigate('/')}>
              Return to Login
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return null;
};

export default SsoCallback;