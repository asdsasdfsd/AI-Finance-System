// frontend/src/routes/AppRoutes.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from '../views/Login';
import Register from '../views/Register';
import RegisterCompany from '../views/RegisterCompany';
import Dashboard from '../views/dashboard';
import SsoCallback from '../views/SsoCallback';
import ProfileCompletion from '../views/ProfileCompletion';
import AuthService from '../services/authService';

// Protected route component
const ProtectedRoute = ({ children }) => {
  const isAuthenticated = AuthService.getCurrentUser() !== null;
  
  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

const AppRoutes = () => (
  <Router>
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/register-company" element={<RegisterCompany />} />
      <Route path="/api/auth/sso/callback" element={<SsoCallback />} />
      <Route path="/api/sso/callback" element={<SsoCallback />} />
      <Route path="/profile-completion" element={
        <ProtectedRoute>
          <ProfileCompletion />
        </ProtectedRoute>
      } />
      <Route 
        path="/dashboard/*" 
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        } 
      />
      <Route path="*" element={<Navigate to="/" replace />} />
      
    </Routes>
  </Router>
);

export default AppRoutes;