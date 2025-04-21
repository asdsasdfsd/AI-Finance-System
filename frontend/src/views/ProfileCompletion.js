// frontend/src/views/ProfileCompletion.js
import React from 'react';
import { Card, Typography, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

const { Title } = Typography;

/**
 * Simple Profile Completion component
 * This is a simplified version that we can expand later
 */
const ProfileCompletion = () => {
  const navigate = useNavigate();
  
  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh',
      background: '#f0f2f5'
    }}>
      <Card style={{ width: 600, padding: '20px' }}>
        <Title level={2} style={{ textAlign: 'center' }}>
          Complete Your Profile
        </Title>
        <p style={{ textAlign: 'center', marginBottom: 24 }}>
          Thanks for logging in with Microsoft! This page will allow you to complete your profile 
          and company information to get the most out of our platform.
        </p>
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <Button type="primary" onClick={() => navigate('/dashboard')} style={{ marginRight: 16 }}>
            Continue to Dashboard
          </Button>
          <Button onClick={() => navigate('/')}>
            Log Out
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default ProfileCompletion;