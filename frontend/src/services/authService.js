// frontend/src/services/authService.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth/';

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
    const response = await axios.post(API_URL + 'login', {
      username,
      password,
      rememberMe
    });
    
    if (response.data.token) {
      localStorage.setItem('user', JSON.stringify(response.data));
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
   */
  logout: () => {
    // We could also call the logout API endpoint here if needed
    localStorage.removeItem('user');
  },
  
  /**
   * Get the current authenticated user from local storage
   * @returns {Object|null} Current user or null if not logged in
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
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
   * Set up the authentication header for axios requests
   * @param {string} token - JWT token
   */
  setupAxiosInterceptors: (token) => {
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
  }
};

export default AuthService;