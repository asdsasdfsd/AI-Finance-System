// frontend/src/routes/AppRoutes.js
import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import Login from '../views/Login';
import Register from '../views/Register';
import RegisterCompany from '../views/RegisterCompany';
import Dashboard from '../views/dashboard';
import SsoCallback from '../views/SsoCallback';
import ProfileCompletion from '../views/ProfileCompletion';
import AuthService from '../services/authService';

/**
 * Protected route component
 * Checks authentication status and redirects to login if not authenticated
 * Also checks token expiration and logs out if token is expired
 */
const ProtectedRoute = ({ children }) => {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(true);
  
  useEffect(() => {
    // Check token expiration on component mount
    const checkAuth = () => {
      const user = AuthService.getCurrentUser();
      if (!user || AuthService.isTokenExpired()) {
        // If no user or token expired, clean up and set not authenticated
        AuthService.logout();
        setIsAuthenticated(false);
      }
    };
    
    checkAuth();
    
    // Set up periodic check for token expiration
    const interval = setInterval(checkAuth, 60000); // Check every minute
    
    return () => clearInterval(interval);
  }, [navigate]);
  
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