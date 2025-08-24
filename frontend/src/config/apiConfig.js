// frontend/src/config/apiConfig.js
import axios from 'axios';

export const API_CONFIG = {
  BASE_URL: 'http://localhost:8085',
  TIMEOUT: 30000,
  ENDPOINTS: {
    // Authentication endpoints
    AUTH: '/api/auth',
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    SSO: '/api/auth/sso',
    
    // Business endpoints
    FUNDS: '/api/funds',
    FIXED_ASSETS: '/api/fixed-assets',
    TRANSACTIONS: '/api/transactions',
    COMPANIES: '/api/companies',
    USERS: '/api/users',
    
    // Report endpoints
    BALANCE_SHEET: '/api/balance-sheet',
    FINANCIAL_GROUPING: '/api/financial-grouping',
    INCOME_STATEMENT: '/api/income-statement',
    FINANCIAL_REPORT: '/api/financial-report'
  }
};

// Utility function to get current auth token with unified key handling
export const getAuthToken = () => {
  try {
    // Try the unified 'auth' key first (new standard)
    let authData = localStorage.getItem('auth');
    if (authData) {
      const parsed = JSON.parse(authData);
      if (parsed.token) {
        return parsed.token;
      }
    }

    // Fallback to legacy 'user' key for backward compatibility
    const userData = localStorage.getItem('user');
    if (userData) {
      const parsed = JSON.parse(userData);
      if (parsed.token) {
        console.warn('[API] Using legacy user token. Consider updating to auth key.');
        return parsed.token;
      }
      if (parsed.user?.token) {
        console.warn('[API] Using nested legacy user token. Consider updating to auth key.');
        return parsed.user.token;
      }
    }
  } catch (error) {
    console.error('[API] Error reading auth token from localStorage:', error);
  }

  return null;
};

// Utility function to get auth headers
export const getAuthHeaders = () => {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// Utility function to check if user is authenticated
export const isAuthenticated = () => {
  const token = getAuthToken();
  if (!token) return false;

  // Basic token validation (check if it looks like a JWT)
  if (token === 'dev-token-123') {
    console.warn('[API] Development token detected');
    return false;
  }

  // Check if token has basic JWT structure
  const parts = token.split('.');
  if (parts.length !== 3) {
    console.warn('[API] Invalid token format');
    return false;
  }

  return true;
};

// Create unified API client with interceptors
export const createApiClient = () => {
  const client = axios.create({
    baseURL: API_CONFIG.BASE_URL,
    timeout: API_CONFIG.TIMEOUT,
    headers: {
      'Content-Type': 'application/json'
    }
  });

  // Request interceptor for auth and logging
  client.interceptors.request.use(
    (config) => {
      // Add authentication header
      const token = getAuthToken();
      
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.debug(`[API] Request: ${config.method?.toUpperCase()} ${config.url} (authenticated)`);
      } else {
        console.debug(`[API] Request: ${config.method?.toUpperCase()} ${config.url} (no auth)`);
        
        // For protected routes, warn about missing auth
        const protectedRoutes = ['/api/funds', '/api/fixed-assets', '/api/transactions', '/api/companies'];
        const isProtectedRoute = protectedRoutes.some(route => config.url?.includes(route));
        
        if (isProtectedRoute) {
          console.warn(`[API] Making request to protected route without authentication: ${config.url}`);
        }
      }
      
      return config;
    },
    (error) => {
      console.error('[API] Request interceptor error:', error);
      return Promise.reject(error);
    }
  );

  // Response interceptor for error handling and auth cleanup
  client.interceptors.response.use(
    (response) => {
      const duration = response.config.metadata?.startTime 
        ? Date.now() - response.config.metadata.startTime 
        : 0;
      
      console.debug(`[API] Response: ${response.status} ${response.config.method?.toUpperCase()} ${response.config.url} (${duration}ms)`);
      return response;
    },
    (error) => {
      const config = error.config || {};
      const status = error.response?.status;
      const url = config.url || 'unknown';
      const method = config.method?.toUpperCase() || 'unknown';
      
      console.error(`[API] Error: ${status} ${method} ${url}`, {
        status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message
      });
      
      // Handle authentication errors
      if (status === 401) {
        console.warn('[API] 401 Unauthorized - clearing auth data and redirecting to login');
        
        // Clear all possible auth keys
        localStorage.removeItem('auth');
        localStorage.removeItem('user');
        
        // Only redirect if not already on login page
        if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/')) {
          console.log('[API] Redirecting to login page');
          window.location.href = '/login';
        }
      }
      
      // Handle forbidden errors
      if (status === 403) {
        console.warn('[API] 403 Forbidden - user does not have permission');
      }
      
      // Handle server errors
      if (status >= 500) {
        console.error('[API] Server error detected:', error.response?.data);
      }
      
      return Promise.reject(error);
    }
  );

  // Add request timing metadata
  client.interceptors.request.use(
    (config) => {
      config.metadata = { startTime: Date.now() };
      return config;
    }
  );

  return client;
};

// Create a default API client instance
export const apiClient = createApiClient();

// Utility function to handle API errors consistently
export const handleApiError = (error, context = 'API Request') => {
  console.error(`[${context}] Error:`, error);

  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        return {
          type: 'validation',
          message: data?.message || 'Invalid request data',
          details: data?.details
        };
      case 401:
        return {
          type: 'auth',
          message: 'Authentication required. Please login again.',
          shouldRedirect: true
        };
      case 403:
        return {
          type: 'permission',
          message: 'Access denied. You don\'t have permission for this action.',
        };
      case 404:
        return {
          type: 'notfound',
          message: 'Requested resource not found.',
        };
      case 409:
        return {
          type: 'conflict',
          message: data?.message || 'Conflict with existing data',
        };
      case 422:
        return {
          type: 'validation',
          message: data?.message || 'Data validation failed',
          details: data?.errors
        };
      case 429:
        return {
          type: 'ratelimit',
          message: 'Too many requests. Please try again later.',
        };
      case 500:
      case 502:
      case 503:
      case 504:
        return {
          type: 'server',
          message: 'Server error. Please try again later.',
        };
      default:
        return {
          type: 'unknown',
          message: data?.message || `Request failed with status ${status}`,
        };
    }
  } else if (error.request) {
    // Request was made but no response received
    return {
      type: 'network',
      message: 'Network error. Please check your connection.',
    };
  } else {
    // Something else happened
    return {
      type: 'unknown',
      message: error.message || 'An unexpected error occurred',
    };
  }
};

// Utility to make authenticated requests with error handling
export const makeAuthenticatedRequest = async (requestFn, context = 'Request') => {
  try {
    if (!isAuthenticated()) {
      throw new Error('Authentication required');
    }
    
    return await requestFn();
  } catch (error) {
    const errorInfo = handleApiError(error, context);
    throw { ...error, errorInfo };
  }
};

// Export commonly used endpoints
export const ENDPOINTS = API_CONFIG.ENDPOINTS;

// Development helper - log API configuration
if (process.env.NODE_ENV === 'development') {
  console.log('[API Config] Initialized with:', {
    baseURL: API_CONFIG.BASE_URL,
    timeout: API_CONFIG.TIMEOUT,
    authenticated: isAuthenticated(),
    endpoints: Object.keys(ENDPOINTS).length
  });
}