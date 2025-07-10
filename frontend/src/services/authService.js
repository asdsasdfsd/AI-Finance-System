// frontend/src/services/authService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085/api/auth';

const AuthService = {
  login: async (username, password) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, {
        username,
        password
      });

      if (response.data && response.data.token) {
        // Store the authentication response directly
        const authData = {
          token: response.data.token,
          tokenType: response.data.tokenType || 'Bearer',
          expiresIn: response.data.expiresIn,
          loginTime: Date.now(), // Add login time for expiration check
          user: response.data.user,
          // Extract key information for easy access
          userId: response.data.user?.userId,
          companyId: response.data.user?.companyId,
          username: response.data.user?.username,
          fullName: response.data.user?.fullName,
          email: response.data.user?.email,
          roles: response.data.user?.roles || []
        };
        
        localStorage.setItem('auth', JSON.stringify(authData));
        console.log('Login successful, stored auth data:', {
          userId: authData.userId,
          companyId: authData.companyId,
          username: authData.username,
          tokenPresent: !!authData.token
        });
        return response.data;
      }
      
      throw new Error('Invalid response format');
    } catch (error) {
      console.error('Login error:', error);
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      }
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('auth');
    localStorage.removeItem('user'); // Remove legacy storage
    console.log('User logged out');
  },

  getCurrentUser: () => {
    const authStr = localStorage.getItem('auth');
    if (authStr) {
      try {
        const authData = JSON.parse(authStr);
        
        // Check if token is expired (basic check)
        if (authData.expiresIn && authData.loginTime) {
          const now = Date.now();
          const expirationTime = authData.loginTime + authData.expiresIn;
          if (now > expirationTime) {
            // Token expired, remove it
            console.log('Token expired, logging out user');
            AuthService.logout();
            return null;
          }
        }
        
        return authData;
      } catch (error) {
        console.error('Error parsing auth data:', error);
        AuthService.logout(); // Clear corrupted data
        return null;
      }
    }
    
    // Check for legacy user data and migrate
    const legacyUserStr = localStorage.getItem('user');
    if (legacyUserStr) {
      try {
        const legacyData = JSON.parse(legacyUserStr);
        // Migrate to new format
        const migratedData = {
          token: legacyData.token,
          loginTime: Date.now(),
          user: legacyData.user || legacyData,
          userId: legacyData.userId || legacyData.user?.userId,
          companyId: legacyData.companyId || legacyData.user?.companyId,
          username: legacyData.username || legacyData.user?.username,
          fullName: legacyData.fullName || legacyData.user?.fullName,
          email: legacyData.email || legacyData.user?.email,
          roles: legacyData.roles || legacyData.user?.roles || []
        };
        
        localStorage.setItem('auth', JSON.stringify(migratedData));
        localStorage.removeItem('user'); // Remove legacy storage
        console.log('Migrated legacy auth data');
        return migratedData;
      } catch (error) {
        console.error('Error migrating legacy user data:', error);
        localStorage.removeItem('user');
        return null;
      }
    }
    
    return null;
  },

  isAuthenticated: () => {
    const authData = AuthService.getCurrentUser();
    const isAuth = authData && authData.token && authData.token !== 'dev-token-123';
    console.log('Authentication check:', {
      hasAuthData: !!authData,
      hasToken: !!(authData && authData.token),
      tokenType: authData?.token?.substring(0, 20) + '...',
      companyId: authData?.companyId,
      isAuthenticated: isAuth
    });
    return isAuth;
  },

  getAuthToken: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.token : null;
  },

  getCompanyId: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.companyId : null;
  },

  getUserId: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.userId : null;
  },

  getUsername: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.username : null;
  },

  getFullName: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.fullName : null;
  },

  getEmail: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.email : null;
  },

  getRoles: () => {
    const authData = AuthService.getCurrentUser();
    return authData ? authData.roles : [];
  },

  getSsoLoginUrl: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/sso/login-url`);
      return response.data.url;
    } catch (error) {
      console.error('Error getting SSO login URL:', error);
      throw error;
    }
  },

  // Utility method to refresh user data from server
  refreshUserData: async () => {
    const authData = AuthService.getCurrentUser();
    if (!authData || !authData.token) {
      return null;
    }

    try {
      const response = await axios.get(`${API_BASE_URL}/me`, {
        headers: { Authorization: `Bearer ${authData.token}` }
      });

      if (response.data && response.data.user) {
        const updatedAuthData = {
          ...authData,
          user: response.data.user,
          userId: response.data.user.userId,
          companyId: response.data.user.companyId,
          username: response.data.user.username,
          fullName: response.data.user.fullName,
          email: response.data.user.email,
          roles: response.data.user.roles || []
        };
        
        localStorage.setItem('auth', JSON.stringify(updatedAuthData));
        return updatedAuthData;
      }
      
      return authData;
    } catch (error) {
      console.error('Error refreshing user data:', error);
      // If token is invalid, logout user
      if (error.response && error.response.status === 401) {
        AuthService.logout();
        return null;
      }
      return authData; // Return existing data on other errors
    }
  },

  // Check if token is about to expire (within 5 minutes)
  isTokenExpiringSoon: () => {
    const authData = AuthService.getCurrentUser();
    if (!authData || !authData.expiresIn || !authData.loginTime) {
      return false;
    }

    const now = Date.now();
    const expirationTime = authData.loginTime + authData.expiresIn;
    const fiveMinutes = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    return (expirationTime - now) < fiveMinutes;
  },

  // Check if token is expired
  isTokenExpired: () => {
    const authData = AuthService.getCurrentUser();
    if (!authData || !authData.expiresIn || !authData.loginTime) {
      return false;
    }

    const now = Date.now();
    const expirationTime = authData.loginTime + authData.expiresIn;
    
    return now > expirationTime;
  },

  // Get user's company information
  getCompanyInfo: () => {
    const authData = AuthService.getCurrentUser();
    if (authData && authData.user) {
      return {
        companyId: authData.user.companyId,
        companyName: authData.user.companyName
      };
    }
    return null;
  },

  // Validate that user has valid authentication data
  validateAuth: () => {
    const authData = AuthService.getCurrentUser();
    if (!authData) {
      console.error('No authentication data found');
      return false;
    }
    
    if (!authData.token || authData.token === 'dev-token-123') {
      console.error('Invalid or development token found');
      return false;
    }
    
    if (!authData.companyId) {
      console.error('No company ID found in auth data');
      return false;
    }
    
    if (!authData.userId) {
      console.error('No user ID found in auth data');
      return false;
    }
    
    console.log('Auth validation passed:', {
      hasToken: true,
      companyId: authData.companyId,
      userId: authData.userId,
      username: authData.username
    });
    
    return true;
  }
};

export default AuthService;