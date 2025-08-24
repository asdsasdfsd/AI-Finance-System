// frontend/src/services/assetService.js
import axios from 'axios';
import { createApiClient, getAuthHeaders } from '../config/apiConfig';

// Create API client with unified auth handling
const apiClient = createApiClient();

const AssetService = {
  /**
   * Get all assets for current user's company (from JWT)
   * @returns {Promise<Array>}
   */
  getAllAssets: () => {
    console.log('[AssetService] Fetching all assets for current user\'s company');
    return apiClient.get('/api/fixed-assets');
  },

  /**
   * Get asset by ID (with company validation)
   * @param {number} assetId 
   * @returns {Promise<Object>}
   */
  getById: (assetId) => {
    console.log(`[AssetService] Fetching asset by ID: ${assetId}`);
    return apiClient.get(`/api/fixed-assets/${assetId}`);
  },

  /**
   * Get assets by company (from JWT)
   * @returns {Promise<Array>}
   */
  getByCompany: () => {
    console.log('[AssetService] Fetching assets by company (from JWT)');
    return apiClient.get('/api/fixed-assets/company');
  },

  /**
   * Get assets by department (within current user's company)
   * @param {number} departmentId 
   * @returns {Promise<Array>}
   */
  getByDepartment: (departmentId) => {
    console.log(`[AssetService] Fetching assets by department: ${departmentId}`);
    return apiClient.get(`/api/fixed-assets/department/${departmentId}`);
  },

  /**
   * Get assets by status (within current user's company)
   * @param {string} status - ACTIVE, DISPOSED, WRITTEN_OFF
   * @returns {Promise<Array>}
   */
  getByStatus: (status) => {
    console.log(`[AssetService] Fetching assets by status: ${status}`);
    return apiClient.get(`/api/fixed-assets/status/${status}`);
  },

  /**
   * Create a new asset (company auto-assigned from JWT)
   * @param {Object} assetData 
   * @returns {Promise<Object>}
   */
  createAsset: (assetData) => {
    console.log('[AssetService] Creating new asset:', assetData);
    // Clean data - remove company info as backend will auto-assign
    const cleanAssetData = {
      name: assetData.name,
      description: assetData.description,
      acquisitionDate: assetData.acquisitionDate,
      acquisitionCost: assetData.acquisitionCost || 0,
      currentValue: assetData.currentValue || assetData.acquisitionCost || 0,
      accumulatedDepreciation: assetData.accumulatedDepreciation || 0,
      location: assetData.location,
      serialNumber: assetData.serialNumber,
      status: assetData.status || 'ACTIVE',
      department: assetData.department // Department will be validated by backend
    };
    return apiClient.post('/api/fixed-assets', cleanAssetData);
  },

  /**
   * Update an existing asset (with company validation)
   * @param {number} assetId 
   * @param {Object} assetData 
   * @returns {Promise<Object>}
   */
  updateAsset: (assetId, assetData) => {
    console.log(`[AssetService] Updating asset ${assetId}:`, assetData);
    // Clean data - remove company info as backend will validate
    const cleanAssetData = {
      name: assetData.name,
      description: assetData.description,
      acquisitionDate: assetData.acquisitionDate,
      acquisitionCost: assetData.acquisitionCost,
      currentValue: assetData.currentValue,
      accumulatedDepreciation: assetData.accumulatedDepreciation,
      location: assetData.location,
      serialNumber: assetData.serialNumber,
      status: assetData.status,
      department: assetData.department
    };
    return apiClient.put(`/api/fixed-assets/${assetId}`, cleanAssetData);
  },

  /**
   * Delete an asset by ID (with company validation)
   * @param {number} assetId 
   * @returns {Promise<void>}
   */
  deleteAsset: (assetId) => {
    console.log(`[AssetService] Deleting asset ${assetId}`);
    return apiClient.delete(`/api/fixed-assets/${assetId}`);
  },

  // ========== Legacy Methods (Backward Compatibility) ==========

  /**
   * @deprecated Use getAllAssets() instead
   */
  getAll: () => {
    console.warn('[AssetService] getAll() method. Use getAllAssets() for clarity.');
    return AssetService.getAllAssets();
  },

  /**
   * @deprecated Use getByCompany() instead
   */
  getByCompany: (companyId) => {
    if (companyId) {
      console.warn('[AssetService] getByCompany(companyId) is deprecated. Use getByCompany() without parameters.');
      return apiClient.get(`/api/fixed-assets/company/${companyId}`);
    }
    return AssetService.getByCompany();
  }
};

export default AssetService;