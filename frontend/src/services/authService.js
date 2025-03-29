// frontend/src/services/authService.js
import axios from 'axios';

// configure backend API url
const API_BASE_URL = 'http://localhost:8085';
const API_URL = `${API_BASE_URL}/api/auth/`;

/**
 * Service for handling authentication-related API requests
 */
const AuthService = {
  /**
   * Authenticate user with username and password
   * @param {string} username - User's username
   * @param {string} password - User's password
   * @param {boolean} rememberMe - Whether to remember the user
   * @returns {Promise<Object>} Authentication response containing token and user details
   */
  login: async (username, password, rememberMe = false) => {
    // Clear any existing tokens first
    localStorage.removeItem('user');
    
    const response = await axios.post(API_URL + 'login', {
      username,
      password,
      rememberMe
    });
    
    if (response.data.token) {
      // Store login timestamp for token expiration check
      const userData = {
        ...response.data,
        issuedAt: new Date().toISOString(), // Store token issue time
      };
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Set up axios with authentication header
      AuthService.setupAxiosInterceptors(response.data.token);
    }
    
    return response.data;
  },
  
  /**
   * Register a new user
   * @param {Object} userData - User registration data
   * @returns {Promise<Object>} Registered user data
   */
  register: async (userData) => {
    const response = await axios.post(API_URL + 'register', userData);
    return response.data;
  },
  
  /**
   * Register a new company and admin user
   * @param {Object} companyData - Company and admin registration data
   * @returns {Promise<Object>} Registered user data
   */
  registerCompany: async (companyData) => {
    const response = await axios.post(API_URL + 'company/register', companyData);
    return response.data;
  },
  
  /**
   * Log out the current user
   * Makes API call to invalidate token server-side and removes user from localStorage
   */
  logout: async () => {
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        if (user.token) {
          // Send logout request to server to invalidate token
          await axios.post(API_URL + 'logout', {}, {
            headers: {
              Authorization: `Bearer ${user.token}`
            }
          });
        }
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Remove user from localStorage regardless of API success
      localStorage.removeItem('user');
    }
  },
  
  /**
   * Check if token is expired
   * @returns {boolean} True if expired, false otherwise
   */
  isTokenExpired: () => {
    const user = AuthService.getCurrentUser();
    if (!user || !user.token) return true;
    
    const tokenExpTime = user.expiresIn * 1000; // Convert to milliseconds
    const issuedAt = new Date(user.issuedAt || Date.now());
    const expirationTime = new Date(issuedAt.getTime() + tokenExpTime);
    
    return Date.now() > expirationTime;
  },
  
  /**
   * Get the current authenticated user from local storage
   * @returns {Object|null} Current user or null if not logged in or token expired
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    
    const user = JSON.parse(userStr);
    
    // Check if token is expired
    const tokenExpTime = user.expiresIn * 1000; // Convert to milliseconds
    const issuedAt = new Date(user.issuedAt || Date.now());
    const expirationTime = new Date(issuedAt.getTime() + tokenExpTime);
    
    if (Date.now() > expirationTime) {
      // Token expired, clean up localStorage
      localStorage.removeItem('user');
      return null;
    }
    
    return user;
  },
  
  /**
   * Get Microsoft SSO login URL
   * @returns {Promise<string>} SSO login URL
   */
  getSsoLoginUrl: async () => {
    const response = await axios.get(API_URL + 'sso/login-url');
    return response.data.url;
  },
  
  /**
   * Process SSO authentication with code received from Microsoft
   * @param {string} code - Authorization code from Microsoft
   * @param {string} state - State parameter for security validation
   * @returns {Promise<Object>} Authentication response with token, user details and provisioning flags
   */
  processSsoLogin: async (code, state) => {
    // Clear any existing tokens before making the request
    localStorage.removeItem('user');
    
    try {
      console.log('Processing SSO login with code:', code, 'state:', state);
      const response = await axios.post(API_URL + 'sso/login', null, {
        params: { code, state }
      });
      
      console.log('SSO login response:', response.data);
      if (response.data.token) {
        // Store login timestamp for token expiration check
        const userData = {
          ...response.data,
          issuedAt: new Date().toISOString(),
        };
        localStorage.setItem('user', JSON.stringify(userData));
        
        // Set up axios with authentication header
        AuthService.setupAxiosInterceptors(response.data.token);
      }
      
      return response.data;
    } catch (error) {
      console.error('SSO Login Error:', error);
      // If there's an error, make sure we don't have any stale auth data
      localStorage.removeItem('user');
      throw error;
    }
  },
  
  /**
   * Set up the authentication header for axios requests
   * @param {string} token - JWT token
   */
  setupAxiosInterceptors: (token) => {
    // Clear any existing interceptors
    axios.interceptors.request.handlers = [];
    axios.interceptors.response.handlers = [];
    
    axios.interceptors.request.use(
      (config) => {
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );
    
    // Add response interceptor to handle 401 Unauthorized errors
    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response && error.response.status === 401) {
          // If we receive a 401, clear the user session
          localStorage.removeItem('user');
          window.location.href = '/'; // Redirect to login page
        }
        return Promise.reject(error);
      }
    );
  }
};

// Setup axios interceptor with the token from localStorage
(() => {
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      if (user && user.token) {
        // Check if token is expired before setting up interceptor
        const tokenExpTime = user.expiresIn * 1000;
        const issuedAt = new Date(user.issuedAt || Date.now());
        const expirationTime = new Date(issuedAt.getTime() + tokenExpTime);
        
        if (Date.now() < expirationTime) {
          AuthService.setupAxiosInterceptors(user.token);
        } else {
          // Token expired, clean up localStorage
          localStorage.removeItem('user');
        }
      }
    }
  } catch (error) {
    console.error('Error setting up auth interceptors:', error);
    localStorage.removeItem('user');
  }
})();

export default AuthService;