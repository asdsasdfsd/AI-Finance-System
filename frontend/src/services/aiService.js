// frontend/src/services/aiService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/ai';

// Get authentication headers
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

const AIService = {
  // Classify transaction using AI
  classifyTransaction: async (data) => {
    console.log('🤖 AI: Classifying transaction...', data);
    try {
      const response = await axios.post(`${API_BASE_URL}/classify`, data, getAuthHeader());
      console.log('✅ AI: Transaction classified successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ AI: Error classifying transaction:', error);
      throw error;
    }
  },

  // Ask financial question to AI
  askFinancialQuestion: async (data) => {
    console.log('🤖 AI: Asking financial question...', data);
    try {
      const response = await axios.post(`${API_BASE_URL}/ask`, data, getAuthHeader());
      console.log('✅ AI: Financial question answered successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ AI: Error answering financial question:', error);
      throw error;
    }
  },

  // Detect anomaly using AI
  detectAnomaly: async (data) => {
    console.log('🤖 AI: Detecting anomaly...', data);
    try {
      const response = await axios.post(`${API_BASE_URL}/detect`, data, getAuthHeader());
      console.log('✅ AI: Anomaly detection completed:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ AI: Error detecting anomaly:', error);
      throw error;
    }
  },

  // Get report insights from AI
  reportInsight: async (data) => {
    console.log('🤖 AI: Generating report insights...', data);
    try {
      const response = await axios.post(`${API_BASE_URL}/report`, data, getAuthHeader());
      console.log('✅ AI: Report insights generated successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ AI: Error generating report insights:', error);
      throw error;
    }
  },

  // Get AI analysis for financial data
  analyzeFinancialData: async (data) => {
    console.log('🤖 AI: Analyzing financial data...', data);
    try {
      const response = await axios.post(`${API_BASE_URL}/analyze`, data, getAuthHeader());
      console.log('✅ AI: Financial data analyzed successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ AI: Error analyzing financial data:', error);
      throw error;
    }
  },

  // Get AI recommendations
  getRecommendations: async (data) => {
    console.log('🤖 AI: Getting recommendations...', data);
    try {
      const response = await axios.post(`${API_BASE_URL}/recommend`, data, getAuthHeader());
      console.log('✅ AI: Recommendations generated successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ AI: Error getting recommendations:', error);
      throw error;
    }
  }
};

// Export individual functions for backward compatibility
export const classifyTransaction = AIService.classifyTransaction;
export const askFinancialQuestion = AIService.askFinancialQuestion;
export const detectAnomaly = AIService.detectAnomaly;
export const reportInsight = AIService.reportInsight;

export default AIService;