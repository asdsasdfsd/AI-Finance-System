// frontend/src/services/authService.js
import axios from 'axios';
import { API_CONFIG, handleApiError } from '../config/apiConfig';

const API_BASE_URL = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.AUTH;

const AuthService = {
  /**
   * Login user with username and password
   * @param {string} username 
   * @param {string} password 
   * @returns {Promise<Object>} Authentication response
   */
  login: async (username, password) => {
    try {
      console.log(`[AuthService] Attempting login for user: ${username}`);
      
      const response = await axios.post(`${API_BASE_URL}/login`, {
        username,
        password
      });

      if (response.data && response.data.token) {
        const authData = AuthService.processAuthResponse(response.data);
        AuthService.storeAuthData(authData);
        
        console.log('[AuthService] Login successful:', {
          userId: authData.userId,
          companyId: authData.companyId,
          username: authData.username,
          fullName: authData.fullName
        });
        
        return authData;
      } else {
        throw new Error('Invalid response format: missing token');
      }
    } catch (error) {
      console.error('[AuthService] Login failed:', error);
      throw error;
    }
  },

  /**
   * Process authentication response and normalize data structure
   * @param {Object} responseData - Raw response from backend
   * @returns {Object} Normalized auth data
   */
  processAuthResponse: (responseData) => {
    return {
      // Token information
      token: responseData.token,
      tokenType: responseData.tokenType || 'Bearer',
      expiresIn: responseData.expiresIn,
      loginTime: Date.now(),
      
      // User information (flatten nested structure)
      user: responseData.user,
      userId: responseData.user?.userId,
      companyId: responseData.user?.companyId || responseData.user?.tenantId,
      username: responseData.user?.username,
      fullName: responseData.user?.fullName,
      email: responseData.user?.email,
      roles: responseData.user?.roles || [],
      
      // Additional metadata
      companyName: responseData.user?.companyName,
      permissions: responseData.user?.permissions || [],
      lastLogin: responseData.user?.lastLogin,
      
      // SSO flags (if applicable)
      newUserCreated: responseData.newUserCreated || false,
      newCompanyCreated: responseData.newCompanyCreated || false
    };
  },

  /**
   * Store authentication data in localStorage with unified key
   * @param {Object} authData - Processed auth data
   */
  storeAuthData: (authData) => {
    try {
      // Store with new unified key
      localStorage.setItem('auth', JSON.stringify(authData));
      
      // Remove any legacy keys to avoid confusion
      localStorage.removeItem('user');
      
      console.log('[AuthService] Auth data stored successfully');
    } catch (error) {
      console.error('[AuthService] Failed to store auth data:', error);
      throw new Error('Failed to store authentication data');
    }
  },

  /**
   * Get current user data from storage
   * @returns {Object|null} Current user data or null if not authenticated
   */
  getCurrentUser: () => {
    try {
      // Try new unified key first
      const authData = localStorage.getItem('auth');
      if (authData) {
        const parsed = JSON.parse(authData);
        
        // Validate required fields
        if (parsed.token && parsed.userId && parsed.companyId) {
          return parsed;
        }
      }

      // Fallback to legacy key for backward compatibility
      const userData = localStorage.getItem('user');
      if (userData) {
        console.warn('[AuthService] Using legacy user data. Consider re-logging in.');
        const parsed = JSON.parse(userData);
        
        // Try to normalize legacy format
        return AuthService.normalizeLegacyUserData(parsed);
      }
      
      return null;
    } catch (error) {
      console.error('[AuthService] Error reading user data:', error);
      return null;
    }
  },

  /**
   * Normalize legacy user data format to current standard
   * @param {Object} legacyData - Legacy user data
   * @returns {Object} Normalized data
   */
  normalizeLegacyUserData: (legacyData) => {
    return {
      token: legacyData.token || legacyData.user?.token,
      tokenType: legacyData.tokenType || 'Bearer',
      loginTime: legacyData.loginTime || Date.now(),
      
      user: legacyData.user || legacyData,
      userId: legacyData.userId || legacyData.user?.userId,
      companyId: legacyData.companyId || legacyData.user?.companyId,
      username: legacyData.username || legacyData.user?.username,
      fullName: legacyData.fullName || legacyData.user?.fullName,
      email: legacyData.email || legacyData.user?.email,
      roles: legacyData.roles || legacyData.user?.roles || []
    };
  },

  /**
   * Validate current authentication status
   * @returns {boolean} True if user is properly authenticated
   */
  validateAuth: () => {
    const user = AuthService.getCurrentUser();
    
    if (!user) {
      console.warn('[AuthService] No authentication data found');
      return false;
    }

    if (!user.token) {
      console.warn('[AuthService] No token found in auth data');
      return false;
    }

    if (user.token === 'dev-token-123') {
      console.warn('[AuthService] Development token detected');
      return false;
    }

    if (!user.companyId) {
      console.warn('[AuthService] No company ID found in auth data');
      return false;
    }

    if (!user.userId) {
      console.warn('[AuthService] No user ID found in auth data');
      return false;
    }

    // Check token expiration if available
    if (user.expiresIn && user.loginTime) {
      const expirationTime = user.loginTime + (user.expiresIn * 1000);
      if (Date.now() > expirationTime) {
        console.warn('[AuthService] Token has expired');
        AuthService.logout();
        return false;
      }
    }

    return true;
  },

  /**
   * Check if user has specific role
   * @param {string} role - Role to check
   * @returns {boolean} True if user has the role
   */
  hasRole: (role) => {
    const user = AuthService.getCurrentUser();
    return user?.roles?.includes(role) || false;
  },

  /**
   * Check if user has any of the specified roles
   * @param {string[]} roles - Roles to check
   * @returns {boolean} True if user has any of the roles
   */
  hasAnyRole: (roles) => {
    const user = AuthService.getCurrentUser();
    if (!user?.roles) return false;
    return roles.some(role => user.roles.includes(role));
  },

  /**
   * Get user's company ID
   * @returns {number|null} Company ID or null
   */
  getCompanyId: () => {
    const user = AuthService.getCurrentUser();
    return user?.companyId || null;
  },

  /**
   * Get user's ID
   * @returns {number|null} User ID or null
   */
  getUserId: () => {
    const user = AuthService.getCurrentUser();
    return user?.userId || null;
  },

  /**
   * Logout user and clean up storage
   */
  logout: () => {
    console.log('[AuthService] Logging out user');
    
    // Clear all possible auth keys
    localStorage.removeItem('auth');
    localStorage.removeItem('user');
    
    // Clear any other app-specific data if needed
    // localStorage.removeItem('preferences');
    
    console.log('[AuthService] User logged out successfully');
  },

  /**
   * Register new user
   * @param {Object} userData - User registration data
   * @returns {Promise<Object>} Registration response
   */
  register: async (userData) => {
    try {
      console.log('[AuthService] Registering new user:', userData.username);
      
      const response = await axios.post(`${API_BASE_URL}/register`, userData);
      
      if (response.data && response.data.token) {
        const authData = AuthService.processAuthResponse(response.data);
        AuthService.storeAuthData(authData);
        return authData;
      }
      
      return response.data;
    } catch (error) {
      console.error('[AuthService] Registration failed:', error);
      throw error;
    }
  },

  /**
   * Handle SSO authentication
   * @param {string} code - Authorization code
   * @param {string} state - State parameter
   * @returns {Promise<Object>} SSO authentication response
   */
  ssoLogin: async (code, state) => {
    try {
      console.log('[AuthService] Processing SSO login');
      
      const response = await axios.post(`${API_BASE_URL}/sso/callback`, {
        code,
        state
      });

      if (response.data && response.data.token) {
        const authData = AuthService.processAuthResponse(response.data);
        AuthService.storeAuthData(authData);
        
        console.log('[AuthService] SSO login successful:', {
          userId: authData.userId,
          companyId: authData.companyId,
          newUser: authData.newUserCreated,
          newCompany: authData.newCompanyCreated
        });
        
        return authData;
      }
      
      throw new Error('Invalid SSO response format');
    } catch (error) {
      console.error('[AuthService] SSO login failed:', error);
      throw error;
    }
  },

  /**
   * Refresh authentication token
   * @returns {Promise<Object>} Refreshed auth data
   */
  refreshToken: async () => {
    try {
      const currentUser = AuthService.getCurrentUser();
      if (!currentUser?.token) {
        throw new Error('No token to refresh');
      }

      const response = await axios.post(`${API_BASE_URL}/refresh`, {}, {
        headers: {
          Authorization: `Bearer ${currentUser.token}`
        }
      });

      if (response.data && response.data.token) {
        const authData = AuthService.processAuthResponse(response.data);
        AuthService.storeAuthData(authData);
        return authData;
      }

      throw new Error('Invalid refresh response');
    } catch (error) {
      console.error('[AuthService] Token refresh failed:', error);
      AuthService.logout(); // Force logout on refresh failure
      throw error;
    }
  }
};

export default AuthService;