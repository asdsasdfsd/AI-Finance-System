// frontend/src/services/aiService.js
import axios from 'axios';
import AuthService from './authService';

const API_BASE_URL = 'http://localhost:8085/api/ai';

// Get authentication headers (optional, supports both login/no-login scenarios)
const getAuthHeader = () => {
  const user = AuthService.getCurrentUser && AuthService.getCurrentUser();
  return user && user.token
    ? { headers: { Authorization: `Bearer ${user.token}` } }
    : {};
};

const AIService = {
  // Enhanced transaction
  classifyTransaction: async (data) => {
    return (await axios.post(`${API_BASE_URL}/enhance-transaction`, data, getAuthHeader())).data;
  },
  // Financial Q&A
  askFinancialQuestion: async (data) => {
    return (await axios.post(`${API_BASE_URL}/ask-financial-question`, data, getAuthHeader())).data;
  },
  // Category suggestions
  categorySuggestions: async (data) => {
    return (await axios.post(`${API_BASE_URL}/category-suggestions`, data, getAuthHeader())).data;
  },
  // Single anomaly detection - FIXED: Added missing function
  detectAnomaly: async (data) => {
    return (await axios.post(`${API_BASE_URL}/detect-anomaly`, data, getAuthHeader())).data;
  },
  // Batch anomaly detection
  detectAnomalies: async (params) => {
    return (await axios.get(`${API_BASE_URL}/detect-anomalies`, { ...getAuthHeader(), params })).data;
  },
  // Smart report insights
  reportInsight: async (params) => {
    return (await axios.get(`${API_BASE_URL}/report-insights`, { ...getAuthHeader(), params })).data;
  },
  // Financial analysis
  analyze: async (data) => {
    return (await axios.post(`${API_BASE_URL}/analyze`, data, getAuthHeader())).data;
  },
  // Smart recommendations
  recommend: async (data) => {
    return (await axios.post(`${API_BASE_URL}/recommend`, data, getAuthHeader())).data;
  },
  // Health check
  healthCheck: async () => {
    return (await axios.get(`${API_BASE_URL}/health`, getAuthHeader())).data;
  },
  // AI provider info
  providerName: async () => {
    return (await axios.get(`${API_BASE_URL}/provider`, getAuthHeader())).data;
  }
};

// Export all methods
export const classifyTransaction = AIService.classifyTransaction;
export const askFinancialQuestion = AIService.askFinancialQuestion;
export const categorySuggestions = AIService.categorySuggestions;
export const detectAnomaly = AIService.detectAnomaly;  // FIXED: Added missing export
export const detectAnomalies = AIService.detectAnomalies;
export const reportInsight = AIService.reportInsight;
export const analyze = AIService.analyze;
export const recommend = AIService.recommend;
export const healthCheck = AIService.healthCheck;
export const providerName = AIService.providerName;

// Enhanced AI response formatting for better user experience
export function formatAIResult(result) {
  // Single Anomaly Detection Result - NEW: Better formatting
  if (result.anomalous !== undefined || result.isAnomalous !== undefined) {
    const isAnomalous = result.anomalous || result.isAnomalous;
    return `【Anomaly Detection Result】
▶ Status: ${isAnomalous ? '⚠️ ANOMALOUS' : '✅ Normal'}
▶ Anomaly Score: ${result.anomalyScore || 'N/A'}
▶ Risk Level: ${result.riskLevel || (result.anomalyScore > 0.7 ? 'High' : result.anomalyScore > 0.4 ? 'Medium' : 'Low')}
▶ Type: ${result.anomalyType || 'General'}
▶ Confidence: ${result.confidence || 'Medium'}
${result.reason ? `▶ Reason: ${result.reason}` : ''}
${result.recommendations && result.recommendations.length > 0 ? 
  `▶ Recommendations:\n${result.recommendations.map(r => '  • ' + r).join('\n')}` : ''}`;
  }

  // Enhanced Transaction
  if (result.aiClassification && result.anomalyDetection) {
    const c = result.aiClassification;
    const a = result.anomalyDetection;
    return `【AI Enhanced Transaction Result】
▶ Category: ${c.category} (Confidence: ${c.confidence})
▶ Reason: ${c.reason}
▶ Alternative Categories: ${(c.alternativeCategories || []).join(', ')}
▶ Require Review: ${c.requireReview ? 'Yes' : 'No'}

▶ Anomaly Detection: ${a.anomalous ? '⚠️ Abnormal' : '✅ Normal'}
▶ Anomaly Score: ${a.anomalyScore}
▶ Type: ${a.anomalyType}
▶ Recommendations: ${(a.recommendations || []).join(', ')}

AI Enhancement Time: ${result.enhancementTimestamp}`;
  }

  // Financial Analysis
  if (result.summary && result.highlights && result.suggestions) {
    return `【AI Financial Analysis】
▶ Summary:
${result.summary}

▶ Key Highlights:
${(result.highlights || []).map(h => '✓ ' + h).join('\n')}

▶ Risk Factors:
${(result.risks || []).map(r => '⚠️ ' + r).join('\n')}

▶ Recommendations:
${(result.suggestions || []).map(s => '💡 ' + s).join('\n')}

▶ Analysis Confidence: ${result.confidence || 'Medium'}`;
  }

  // Q&A
  if (result.answer && result.confidence) {
    return `【AI Financial Q&A】
▶ Answer: ${result.answer}
▶ Confidence: ${result.confidence}
▶ Has Numeric Data: ${result.hasNumericData ? 'Yes' : 'No'}
▶ Data Sources: ${(result.dataSources || []).join(', ')}
${result.relatedData ? '▶ Related Data: ' + JSON.stringify(result.relatedData, null, 2) : ''}`;
  }

  // Recommendations
  if (result.recommendations && result.reasoning !== undefined) {
    return `【AI Recommendations】
${(result.recommendations || []).map(r => '💡 ' + r).join('\n')}

▶ Reasoning: ${result.reasoning}
▶ Confidence: ${result.confidence}`;
  }

  // Category Suggestions
  if (Array.isArray(result) && result[0] && result[0].categoryCode) {
    return result.map(
      item =>
        `【Category Suggestion】
▶ Code: ${item.categoryCode}
▶ Name: ${item.categoryName}
▶ Chinese Name: ${item.chineseName}
▶ Confidence: ${item.confidence}
▶ Reason: ${item.reason}\n`
    ).join('\n');
  }

  // Batch Anomaly Detection
  if (Array.isArray(result) && result[0] && result[0].description && result[0].anomalyScore !== undefined) {
    return result.map(
      item =>
        `【Transaction Anomaly Analysis】
▶ Description: ${item.description}
▶ Amount: ${item.amount}
▶ Date: ${item.transactionDate}
▶ Category: ${item.category}
▶ Status: ${item.anomalyScore > 0.5 ? '⚠️ Suspicious' : '✅ Normal'}
▶ Anomaly Score: ${item.anomalyScore}
▶ Type: ${item.anomalyType}
▶ Recommendations: ${(item.recommendations || []).join(', ')}\n`
    ).join('\n');
  }

  // Report Insights
  if (result.insights && Array.isArray(result.insights)) {
    return `【Report Insights】
${result.insights.map(insight => '📊 ' + insight).join('\n')}

${result.summary ? `▶ Summary: ${result.summary}` : ''}
${result.trends ? `▶ Trends: ${result.trends.join(', ')}` : ''}
${result.recommendations ? `▶ Recommendations:\n${result.recommendations.map(r => '💡 ' + r).join('\n')}` : ''}`;
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

export default AIService;