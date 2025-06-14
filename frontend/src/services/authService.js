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
        // Store the full user data including companyId
        const userData = {
          token: response.data.token,
          user: response.data.user,
          companyId: response.data.user?.companyId || 1, // Fallback to hardcoded value
          userId: response.data.user?.userId || 1,
          ...response.data.user
        };
        
        localStorage.setItem('user', JSON.stringify(userData));
        return response.data;
      }
      
      throw new Error('Invalid response format');
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const userData = JSON.parse(userStr);
        
        // Ensure companyId is available (fallback for development)
        if (!userData.companyId) {
          userData.companyId = 1; // Hardcoded fallback as requested
        }
        
        if (!userData.userId) {
          userData.userId = 1; // Hardcoded fallback as requested
        }
        
        return userData;
      } catch (error) {
        console.error('Error parsing user data:', error);
        return null;
      }
    }
    return null;
  },

  isAuthenticated: () => {
    const user = AuthService.getCurrentUser();
    return user && user.token;
  },

  getAuthToken: () => {
    const user = AuthService.getCurrentUser();
    return user ? user.token : null;
  },

  // Development helper method to set hardcoded user
  setDevelopmentUser: () => {
    const devUser = {
      token: 'dev-token-123',
      userId: 1,
      companyId: 1,
      username: 'dev-user',
      user: {
        userId: 1,
        companyId: 1,
        username: 'dev-user',
        fullName: 'Development User',
        email: 'dev@example.com'
      }
    };
    localStorage.setItem('user', JSON.stringify(devUser));
    return devUser;
  }
};

export default AuthService;