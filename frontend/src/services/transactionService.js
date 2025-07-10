// frontend/src/services/transactionService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/transactions';

// Get auth headers with debugging
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  
  if (!user || !user.token) {
    console.error('No valid authentication token found');
    throw new Error('Authentication required. Please login first.');
  }
  
  if (user.token === 'dev-token-123') {
    console.error('Development token detected. Please login with real credentials.');
    throw new Error('Development token not allowed. Please login with real credentials.');
  }
  
  console.log('Using auth token for request:', {
    tokenPresent: !!user.token,
    tokenPrefix: user.token.substring(0, 20) + '...',
    companyId: user.companyId,
    userId: user.userId,
    username: user.username
  });
  
  return user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

// Get company ID from current user (JWT will be used by backend to extract company ID)
const getCompanyId = () => {
  const user = AuthService.getCurrentUser();
  return user?.companyId || user?.user?.companyId || null;
};

// Get user ID from current user
const getUserId = () => {
  const user = AuthService.getCurrentUser();
  return user?.userId || user?.user?.userId || null;
};

const TransactionService = {
  // Get all transactions (backend will use JWT to determine company)
  getAll: async () => {
    try {
      console.log('Fetching all transactions...');
      
      // Validate authentication before making request
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      const authHeaders = getAuthHeader();
      console.log('Making request to:', API_BASE_URL);
      console.log('Auth headers:', authHeaders);
      
      const response = await axios.get(API_BASE_URL, authHeaders);
      console.log('Transaction fetch successful:', {
        status: response.status,
        dataLength: response.data?.length || 0
      });
      
      return response;
    } catch (error) {
      console.error('Transaction fetch error:', {
        message: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        config: {
          url: error.config?.url,
          method: error.config?.method,
          headers: error.config?.headers
        }
      });
      throw error;
    }
  },

  // Get transaction by ID (backend will validate company access via JWT)
  getById: async (id) => {
    try {
      console.log(`Fetching transaction by ID: ${id}`);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      return await axios.get(`${API_BASE_URL}/${id}`, getAuthHeader());
    } catch (error) {
      console.error(`Error fetching transaction ${id}:`, error.response?.data || error.message);
      throw error;
    }
  },

  // Get transactions by type (backend will use JWT for company context)
  getByType: async (type) => {
    try {
      console.log(`Fetching transactions by type: ${type}`);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      return await axios.get(`${API_BASE_URL}/type/${type}`, getAuthHeader());
    } catch (error) {
      console.error(`Error fetching transactions by type ${type}:`, error.response?.data || error.message);
      throw error;
    }
  },

  // Get transactions by date range (backend will use JWT for company context)
  getByDateRange: async (startDate, endDate) => {
    try {
      console.log(`Fetching transactions by date range: ${startDate} to ${endDate}`);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      return await axios.get(`${API_BASE_URL}/date-range`, {
        ...getAuthHeader(),
        params: { startDate, endDate }
      });
    } catch (error) {
      console.error(`Error fetching transactions by date range:`, error.response?.data || error.message);
      throw error;
    }
  },

  // Get transaction sum by type (backend will use JWT for company context)
  getSumByType: async (type) => {
    try {
      console.log(`Fetching transaction sum by type: ${type}`);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      return await axios.get(`${API_BASE_URL}/sum/type/${type}`, getAuthHeader());
    } catch (error) {
      console.error(`Error fetching sum by type ${type}:`, error.response?.data || error.message);
      throw error;
    }
  },

  // Create new transaction
  createTransaction: async (data) => {
    try {
      console.log('Creating new transaction:', data);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      // Remove explicit companyId and userId as backend will extract from JWT
      const transactionData = {
        amount: data.amount,
        description: data.description,
        transactionDate: data.transactionDate,
        category: data.category,
        paymentMethod: data.paymentMethod,
        reference: data.reference,
        notes: data.notes
      };
      
      return await axios.post(API_BASE_URL, transactionData, getAuthHeader());
    } catch (error) {
      console.error('Error creating transaction:', error.response?.data || error.message);
      throw error;
    }
  },

  // Update existing transaction
  updateTransaction: async (id, data) => {
    try {
      console.log(`Updating transaction ${id}:`, data);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      // Remove explicit companyId and userId as backend will extract from JWT
      const transactionData = {
        amount: data.amount,
        description: data.description,
        transactionDate: data.transactionDate,
        category: data.category,
        paymentMethod: data.paymentMethod,
        reference: data.reference,
        notes: data.notes
      };
      
      return await axios.put(`${API_BASE_URL}/${id}`, transactionData, getAuthHeader());
    } catch (error) {
      console.error(`Error updating transaction ${id}:`, error.response?.data || error.message);
      throw error;
    }
  },

  // Delete transaction (backend will validate company access via JWT)
  deleteTransaction: async (id) => {
    try {
      console.log(`Deleting transaction: ${id}`);
      if (!AuthService.validateAuth()) {
        throw new Error('Authentication validation failed');
      }
      
      return await axios.delete(`${API_BASE_URL}/${id}`, getAuthHeader());
    } catch (error) {
      console.error(`Error deleting transaction ${id}:`, error.response?.data || error.message);
      throw error;
    }
  },

  // Legacy methods for backward compatibility (will be deprecated)
  getByCompanyAndType: (companyId, type) => {
    console.warn('getByCompanyAndType is deprecated. Use getByType instead.');
    return TransactionService.getByType(type);
  },

  getByUserAndType: (userId, type) => {
    console.warn('getByUserAndType is deprecated. Use getByType instead.');
    return TransactionService.getByType(type);
  }
};

export default TransactionService;