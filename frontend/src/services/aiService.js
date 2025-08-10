// frontend/src/services/aiService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = process.env.REACT_APP_API_URL ? 
  `${process.env.REACT_APP_API_URL}/api/ai` : 
  'http://localhost:8085/api/ai';

// Get auth header helper
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser();
  return user && user.token ? 
    { headers: { Authorization: `Bearer ${user.token}` } } : 
    {};
};

const AIService = {
  /**
   * Enhanced report insights with better error handling and response formatting
   */
  reportInsight: async (params) => {
    try {
      console.log('Requesting report insights with params:', params);
      
      const response = await axios.get(`${API_BASE_URL}/report-insights`, { 
        ...getAuthHeader(), 
        params 
      });
      
      console.log('Raw AI response:', response.data);
      
      // Handle different response formats
      if (response.data && typeof response.data === 'object') {
        // Already structured response
        return {
          success: true,
          data: response.data
        };
      } else {
        // Fallback for string responses
        return {
          success: true,
          data: {
            summary: 'Analysis completed',
            insights: [response.data || 'No insights available'],
            confidence: 'medium',
            analysisDate: new Date().toISOString(),
            status: 'completed'
          }
        };
      }
    } catch (error) {
      console.error('Report insight request failed:', error);
      
      // Return structured error response
      return {
        success: false,
        error: error.message,
        data: {
          summary: 'Analysis failed',
          insights: ['Unable to generate insights at this time'],
          recommendations: ['Please check your connection and try again'],
          confidence: 'low',
          analysisDate: new Date().toISOString(),
          status: 'error',
          error: true,
          errorMessage: error.message
        }
      };
    }
  },

  /**
   * Enhanced transaction classification
   */
  classifyTransaction: async (data) => {
    try {
      console.log('Classifying transaction:', data);
      
      const response = await axios.post(`${API_BASE_URL}/enhance-transaction`, data, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Transaction classification failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          enhancement: {
            description: data.description,
            confidence: 'low',
            error: true
          }
        }
      };
    }
  },

  /**
   * Enhanced financial Q&A
   */
  askFinancialQuestion: async (data) => {
    try {
      console.log('Asking financial question:', data);
      
      const response = await axios.post(`${API_BASE_URL}/ask-financial-question`, data, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Financial Q&A failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          answer: 'Unable to process your question at this time. Please try again later.',
          confidence: 'low'
        }
      };
    }
  },

  /**
   * Enhanced category suggestions
   */
  categorySuggestions: async (data) => {
    try {
      console.log('Getting category suggestions:', data);
      
      const response = await axios.post(`${API_BASE_URL}/category-suggestions`, data, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Category suggestions failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          suggestions: [{
            categoryCode: 'GENERAL',
            categoryName: 'General Expense',
            chineseName: '一般费用',
            confidence: 'low',
            reason: 'Default category due to service error',
            error: true
          }]
        }
      };
    }
  },

  /**
   * Enhanced single anomaly detection
   */
  detectAnomaly: async (data) => {
    try {
      console.log('Detecting anomaly:', data);
      
      const response = await axios.post(`${API_BASE_URL}/detect-anomaly`, data, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Anomaly detection failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          result: {
            anomalous: false,
            anomalyScore: 0.0,
            confidence: 'low',
            reason: 'Analysis failed: ' + error.message,
            error: true
          }
        }
      };
    }
  },

  /**
   * Enhanced batch anomaly detection
   */
  detectAnomalies: async (params) => {
    try {
      console.log('Detecting batch anomalies:', params);
      
      const response = await axios.get(`${API_BASE_URL}/detect-anomalies`, { 
        ...getAuthHeader(), 
        params 
      });
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Batch anomaly detection failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          anomalies: [],
          totalCount: 0
        }
      };
    }
  },

  /**
   * Enhanced financial analysis
   */
  analyze: async (data) => {
    try {
      console.log('Starting financial analysis:', data);
      
      const response = await axios.post(`${API_BASE_URL}/analyze`, data, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Financial analysis failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          summary: 'Analysis temporarily unavailable',
          insights: ['Please try again later']
        }
      };
    }
  },

  /**
   * Enhanced smart recommendations
   */
  recommend: async (data) => {
    try {
      console.log('Getting smart recommendations:', data);
      
      const response = await axios.post(`${API_BASE_URL}/recommend`, data, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Smart recommendations failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          recommendations: ['Service temporarily unavailable']
        }
      };
    }
  },

  /**
   * Health check
   */
  healthCheck: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/health`, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('AI health check failed:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'unhealthy',
          service: 'AI Analysis Service',
          error: error.message
        }
      };
    }
  },

  /**
   * AI provider info
   */
  providerName: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/provider`, getAuthHeader());
      
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Failed to get AI provider info:', error);
      return {
        success: false,
        error: error.message,
        data: {
          status: 'error',
          provider: {
            name: 'Unknown',
            version: 'Unknown',
            available: false
          }
        }
      };
    }
  }
};

/**
 * Enhanced AI response formatting for better user experience
 */
export function formatAIResult(result) {
  // Handle structured insights response
  if (result && typeof result === 'object') {
    // Report Insights with structured format
    if (result.insights && Array.isArray(result.insights)) {
      let formatted = `【AI Analysis Results】\n\n`;
      
      if (result.summary) {
        formatted += `📋 Summary:\n${result.summary}\n\n`;
      }
      
      if (result.insights.length > 0) {
        formatted += `💡 Key Insights:\n`;
        result.insights.forEach((insight, index) => {
          formatted += `${index + 1}. ${insight}\n`;
        });
        formatted += '\n';
      }
      
      if (result.anomalies && result.anomalies.length > 0) {
        formatted += `⚠️ Anomalies:\n`;
        result.anomalies.forEach((anomaly, index) => {
          formatted += `${index + 1}. ${anomaly}\n`;
        });
        formatted += '\n';
      }
      
      if (result.recommendations && result.recommendations.length > 0) {
        formatted += `🎯 Recommendations:\n`;
        result.recommendations.forEach((rec, index) => {
          formatted += `${index + 1}. ${rec}\n`;
        });
        formatted += '\n';
      }
      
      if (result.confidence) {
        formatted += `📊 Confidence: ${result.confidence.toUpperCase()}\n`;
      }
      
      if (result.analysisDate) {
        formatted += `🕒 Analysis Date: ${new Date(result.analysisDate).toLocaleString()}\n`;
      }
      
      return formatted;
    }
    
    // Single Anomaly Detection Result
    if (result.anomalous !== undefined || result.isAnomalous !== undefined) {
      const isAnomalous = result.anomalous || result.isAnomalous;
      let formatted = `【Anomaly Detection Result】\n`;
      formatted += `▶ Status: ${isAnomalous ? '⚠️ ANOMALOUS' : '✅ Normal'}\n`;
      formatted += `▶ Anomaly Score: ${result.anomalyScore || 'N/A'}\n`;
      formatted += `▶ Risk Level: ${result.riskLevel || (result.anomalyScore > 0.7 ? 'High' : result.anomalyScore > 0.4 ? 'Medium' : 'Low')}\n`;
      formatted += `▶ Type: ${result.anomalyType || 'General'}\n`;
      formatted += `▶ Confidence: ${result.confidence || 'Medium'}\n`;
      if (result.reason) {
        formatted += `▶ Reason: ${result.reason}\n`;
      }
      if (result.recommendations && result.recommendations.length > 0) {
        formatted += `▶ Recommendations: ${result.recommendations.join(', ')}\n`;
      }
      return formatted;
    }
    
    // Batch Anomaly Detection Results
    if (result.anomalies && Array.isArray(result.anomalies)) {
      let formatted = `【Batch Anomaly Detection Results】\n`;
      formatted += `▶ Total Analyzed: ${result.totalCount || result.anomalies.length}\n`;
      formatted += `▶ Anomalies Found: ${result.anomalies.filter(a => a.anomalous || a.isAnomalous).length}\n\n`;
      
      if (result.anomalies.length > 0) {
        formatted += `📋 Detected Anomalies:\n`;
        result.anomalies
          .filter(item => item.anomalous || item.isAnomalous)
          .forEach((item, index) => {
            formatted += `${index + 1}. ${item.description || 'Transaction'}\n`;
            formatted += `   Amount: ${item.amount || 'N/A'}\n`;
            formatted += `   Score: ${item.anomalyScore || 'N/A'}\n`;
            formatted += `   Type: ${item.anomalyType || 'General'}\n\n`;
          });
      } else {
        formatted += `✅ No anomalies detected.\n`;
      }
      
      return formatted;
    }
  }
  
  // Default: enhanced JSON formatting
  if (typeof result === 'object') {
    try {
      return `【AI Analysis Result】\n${JSON.stringify(result, null, 2)}`;
    } catch (e) {
      return `【AI Analysis Result】\n${String(result)}`;
    }
  }
  
  return String(result);
}

// Export all methods for compatibility
export const classifyTransaction = AIService.classifyTransaction;
export const askFinancialQuestion = AIService.askFinancialQuestion;
export const categorySuggestions = AIService.categorySuggestions;
export const detectAnomaly = AIService.detectAnomaly;
export const detectAnomalies = AIService.detectAnomalies;
export const reportInsight = AIService.reportInsight;
export const analyze = AIService.analyze;
export const recommend = AIService.recommend;
export const healthCheck = AIService.healthCheck;
export const providerName = AIService.providerName;

export default AIService;