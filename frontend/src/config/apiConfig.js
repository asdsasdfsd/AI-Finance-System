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
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (user.token) {
        config.headers.Authorization = `Bearer ${user.token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response interceptor for error handling
  client.interceptors.response.use(
    (response) => response,
    (error) => {
      console.error('API Error:', error);
      if (error.response?.status === 401) {
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return client;
};