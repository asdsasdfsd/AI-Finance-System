// frontend/src/config/apiConfig.js
import axios from 'axios';

export const API_CONFIG = {
  BASE_URL: 'http://localhost:8085',
  ENDPOINTS: {
    BALANCE_SHEET: '/api/balance-sheet',
    FINANCIAL_GROUPING: '/api/financial-grouping',
    INCOME_STATEMENT: '/api/income-statement',
    FINANCIAL_REPORT: '/api/financial-report'
  }
};

// Common axios configuration
export const createApiClient = () => {
  const client = axios.create({
    baseURL: API_CONFIG.BASE_URL,
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json'
    }
  });

  // Request interceptor for auth
  client.interceptors.request.use(
    (config) => {
      // Fix: Use 'auth' instead of 'user' to match AuthService
      const authData = JSON.parse(localStorage.getItem('auth') || '{}');
      
      // Support both token directly and nested user object
      const token = authData.token || authData.user?.token;
      
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.debug(`[API] Adding auth header for ${config.method?.toUpperCase()} ${config.url}`);
      } else {
        console.warn(`[API] No token found for ${config.method?.toUpperCase()} ${config.url}`);
        console.debug('[API] Available auth data:', authData);
      }
      
      return config;
    },
    (error) => {
      console.error('[API] Request interceptor error:', error);
      return Promise.reject(error);
    }
  );

  // Response interceptor for error handling
  client.interceptors.response.use(
    (response) => {
      console.debug(`[API] Response: ${response.status} ${response.config.method?.toUpperCase()} ${response.config.url}`);
      return response;
    },
    (error) => {
      console.error('[API] Response error:', error);
      
      if (error.response?.status === 401) {
        console.warn('[API] 401 Unauthorized - clearing auth data and redirecting to login');
        
        // Clear both possible auth keys
        localStorage.removeItem('auth');
        localStorage.removeItem('user');
        
        // Redirect to login if not already there
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      }
      
      return Promise.reject(error);
    }
  );

  return client;
};

// Utility function to get current auth token
export const getAuthToken = () => {
  const authData = JSON.parse(localStorage.getItem('auth') || '{}');
  return authData.token || authData.user?.token || null;
};

// Utility function to get auth headers
export const getAuthHeaders = () => {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};