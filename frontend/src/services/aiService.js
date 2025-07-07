import axios from 'axios';

const BASE_URL = '/api/ai'; 

export const classifyTransaction = async (data) => {
  return axios.post(`${BASE_URL}/classify`, data);
};

export const askFinancialQuestion = async (data) => {
  return axios.post(`${BASE_URL}/ask`, data);
};

export const detectAnomaly = async (data) => {
  return axios.post(`${BASE_URL}/detect`, data);
};

export const reportInsight = async (data) => {
  return axios.post(`${BASE_URL}/report`, data);
};
