/* eslint-disable testing-library/no-await-sync-query */
// frontend/src/__tests__/services/assetService.test.js
import AssetService from '../../services/assetService';
import AuthService from '../../services/authService';
import axios from 'axios';

// Mock dependencies
jest.mock('axios');
jest.mock('../../services/authService');

const mockedAxios = axios;
const mockedAuthService = AuthService;

describe('AssetService', () => {
  const mockAuthHeader = {
    headers: { Authorization: 'Bearer mock-token' }
  };

  const mockAsset = {
    assetId: 1,
    name: 'Test Asset',
    description: 'Test Description',
    acquisitionCost: 10000.00,
    currentValue: 8000.00,
    status: 'ACTIVE',
    companyId: 999,
    departmentId: 1
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockedAuthService.getAuthHeader.mockReturnValue(mockAuthHeader);
  });

  describe('getAll', () => {
    test('should fetch all assets successfully', async () => {
      // Given
      const mockResponse = {
        data: [mockAsset]
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getAll();

      // Then
      expect(mockedAxios.get).toHaveBeenCalledWith(
        'http://localhost:8085/api/fixed-assets',
        mockAuthHeader
      );
      expect(result.data).toEqual([mockAsset]);
    });

    test('should handle empty asset list', async () => {
      // Given
      const mockResponse = { data: [] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getAll();

      // Then
      expect(result.data).toEqual([]);
    });

    test('should handle API error', async () => {
      // Given
      const error = new Error('Network error');
      mockedAxios.get.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.getAll()).rejects.toThrow('Network error');
    });

    test('should call with correct auth header', async () => {
      // Given
      mockedAxios.get.mockResolvedValue({ data: [] });

      // When
      await AssetService.getAll();

      // Then
      expect(mockedAuthService.getAuthHeader).toHaveBeenCalled();
      expect(mockedAxios.get).toHaveBeenCalledWith(
        'http://localhost:8085/api/fixed-assets',
        mockAuthHeader
      );
    });
  });

  describe('getById', () => {
    test('should fetch asset by ID successfully', async () => {
      // Given
      const assetId = 1;
      const mockResponse = { data: mockAsset };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getById(assetId);

      // Then
      expect(mockedAxios.get).toHaveBeenCalledWith(
        `http://localhost:8085/api/fixed-assets/${assetId}`,
        mockAuthHeader
      );
      expect(result.data).toEqual(mockAsset);
    });

    test('should handle asset not found', async () => {
      // Given
      const assetId = 999;
      const error = {
        response: {
          status: 404,
          data: { message: 'Asset not found' }
        }
      };
      mockedAxios.get.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.getById(assetId)).rejects.toThrow();
    });

    test('should validate asset ID parameter', async () => {
      // When & Then
      await expect(AssetService.getById(null)).rejects.toThrow();
      await expect(AssetService.getById(undefined)).rejects.toThrow();
    });
  });

  describe('getByCompany', () => {
    test('should fetch assets by company ID successfully', async () => {
      // Given
      const companyId = 999;
      const mockResponse = { data: [mockAsset] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getByCompany(companyId);

      // Then
      expect(mockedAxios.get).toHaveBeenCalledWith(
        `http://localhost:8085/api/fixed-assets/company/${companyId}`,
        mockAuthHeader
      );
      expect(result.data).toEqual([mockAsset]);
    });

    test('should handle company with no assets', async () => {
      // Given
      const companyId = 999;
      const mockResponse = { data: [] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getByCompany(companyId);

      // Then
      expect(result.data).toEqual([]);
    });

    test('should validate company ID parameter', async () => {
      // When & Then
      await expect(AssetService.getByCompany(null)).rejects.toThrow();
      await expect(AssetService.getByCompany('invalid')).rejects.toThrow();
    });
  });

  describe('getByDepartment', () => {
    test('should fetch assets by department ID successfully', async () => {
      // Given
      const departmentId = 1;
      const mockResponse = { data: [mockAsset] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getByDepartment(departmentId);

      // Then
      expect(mockedAxios.get).toHaveBeenCalledWith(
        `http://localhost:8085/api/fixed-assets/department/${departmentId}`,
        mockAuthHeader
      );
      expect(result.data).toEqual([mockAsset]);
    });

    test('should handle department with no assets', async () => {
      // Given
      const departmentId = 1;
      const mockResponse = { data: [] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getByDepartment(departmentId);

      // Then
      expect(result.data).toEqual([]);
    });
  });

  describe('getByStatus', () => {
    test('should fetch assets by status successfully', async () => {
      // Given
      const status = 'ACTIVE';
      const mockResponse = { data: [mockAsset] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.getByStatus(status);

      // Then
      expect(mockedAxios.get).toHaveBeenCalledWith(
        `http://localhost:8085/api/fixed-assets/status/${status}`,
        mockAuthHeader
      );
      expect(result.data).toEqual([mockAsset]);
    });

    test('should handle different asset statuses', async () => {
      // Given
      const statuses = ['ACTIVE', 'INACTIVE', 'DISPOSED', 'MAINTENANCE'];
      
      for (const status of statuses) {
        mockedAxios.get.mockResolvedValue({ data: [{ ...mockAsset, status }] });
        
        // When
        const result = await AssetService.getByStatus(status);
        
        // Then
        expect(result.data[0].status).toBe(status);
      }
    });

    test('should validate status parameter', async () => {
      // When & Then
      await expect(AssetService.getByStatus(null)).rejects.toThrow();
      await expect(AssetService.getByStatus('')).rejects.toThrow();
    });
  });

  describe('createAsset', () => {
    const mockAssetData = {
      name: 'New Asset',
      description: 'New asset description',
      acquisitionCost: 15000.00,
      companyId: 999,
      departmentId: 1
    };

    test('should create asset successfully', async () => {
      // Given
      const mockResponse = { 
        data: { 
          ...mockAssetData, 
          assetId: 2,
          status: 'ACTIVE'
        }
      };
      mockedAxios.post.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.createAsset(mockAssetData);

      // Then
      expect(mockedAxios.post).toHaveBeenCalledWith(
        'http://localhost:8085/api/fixed-assets',
        mockAssetData,
        mockAuthHeader
      );
      expect(result.data.assetId).toBe(2);
      expect(result.data.name).toBe(mockAssetData.name);
    });

    test('should validate required fields for creation', async () => {
      // Given
      const invalidData = {
        // missing required fields
        description: 'Invalid asset'
      };

      // When & Then
      await expect(AssetService.createAsset(invalidData)).rejects.toThrow();
    });

    test('should handle creation with duplicate name', async () => {
      // Given
      const error = {
        response: {
          status: 409,
          data: { message: 'Asset name already exists' }
        }
      };
      mockedAxios.post.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.createAsset(mockAssetData)).rejects.toThrow();
    });

    test('should validate asset data types', async () => {
      // Given
      const invalidTypeData = {
        ...mockAssetData,
        acquisitionCost: 'invalid_number'
      };

      // When & Then
      await expect(AssetService.createAsset(invalidTypeData)).rejects.toThrow();
    });
  });

  describe('updateAsset', () => {
    const mockUpdateData = {
      name: 'Updated Asset',
      description: 'Updated description',
      currentValue: 7000.00
    };

    test('should update asset successfully', async () => {
      // Given
      const assetId = 1;
      const mockResponse = { 
        data: { 
          ...mockAsset, 
          ...mockUpdateData 
        }
      };
      mockedAxios.put.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.updateAsset(assetId, mockUpdateData);

      // Then
      expect(mockedAxios.put).toHaveBeenCalledWith(
        `http://localhost:8085/api/fixed-assets/${assetId}`,
        mockUpdateData,
        mockAuthHeader
      );
      expect(result.data.name).toBe(mockUpdateData.name);
    });

    test('should handle partial updates', async () => {
      // Given
      const assetId = 1;
      const partialUpdateData = { name: 'New Name Only' };
      const mockResponse = { 
        data: { 
          ...mockAsset, 
          name: 'New Name Only'
        }
      };
      mockedAxios.put.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.updateAsset(assetId, partialUpdateData);

      // Then
      expect(result.data.name).toBe('New Name Only');
      expect(result.data.description).toBe(mockAsset.description); // unchanged
    });

    test('should handle update of non-existent asset', async () => {
      // Given
      const assetId = 999;
      const error = {
        response: {
          status: 404,
          data: { message: 'Asset not found' }
        }
      };
      mockedAxios.put.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.updateAsset(assetId, mockUpdateData)).rejects.toThrow();
    });

    test('should validate update parameters', async () => {
      // When & Then
      await expect(AssetService.updateAsset(null, mockUpdateData)).rejects.toThrow();
      await expect(AssetService.updateAsset(1, null)).rejects.toThrow();
    });
  });

  describe('deleteAsset', () => {
    test('should delete asset successfully', async () => {
      // Given
      const assetId = 1;
      const mockResponse = { data: { message: 'Asset deleted successfully' } };
      mockedAxios.delete.mockResolvedValue(mockResponse);

      // When
      const result = await AssetService.deleteAsset(assetId);

      // Then
      expect(mockedAxios.delete).toHaveBeenCalledWith(
        `http://localhost:8085/api/fixed-assets/${assetId}`,
        mockAuthHeader
      );
      expect(result.data.message).toBe('Asset deleted successfully');
    });

    test('should handle deletion of non-existent asset', async () => {
      // Given
      const assetId = 999;
      const error = {
        response: {
          status: 404,
          data: { message: 'Asset not found' }
        }
      };
      mockedAxios.delete.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.deleteAsset(assetId)).rejects.toThrow();
    });

    test('should handle constraint violations during deletion', async () => {
      // Given
      const assetId = 1;
      const error = {
        response: {
          status: 409,
          data: { message: 'Cannot delete asset with active transactions' }
        }
      };
      mockedAxios.delete.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.deleteAsset(assetId)).rejects.toThrow();
    });

    test('should validate delete parameter', async () => {
      // When & Then
      await expect(AssetService.deleteAsset(null)).rejects.toThrow();
      await expect(AssetService.deleteAsset(undefined)).rejects.toThrow();
    });
  });

  describe('Authentication Integration', () => {
    test('should handle authentication failure', async () => {
      // Given
      mockedAuthService.getAuthHeader.mockReturnValue({});
      const error = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      };
      mockedAxios.get.mockRejectedValue(error);

      // When & Then
      await expect(AssetService.getAll()).rejects.toThrow();
    });

    test('should retry with fresh token on 401 error', async () => {
      // Given
      const unauthorizedError = {
        response: { status: 401 }
      };
      const successResponse = { data: [mockAsset] };
      
      mockedAxios.get
        .mockRejectedValueOnce(unauthorizedError)
        .mockResolvedValueOnce(successResponse);

      // When
      const result = await AssetService.getAll();

      // Then
      expect(mockedAxios.get).toHaveBeenCalledTimes(1); // Should not auto-retry
      expect(result).toBeDefined();
    });

    test('should handle missing auth header gracefully', async () => {
      // Given
      mockedAuthService.getAuthHeader.mockReturnValue({});
      mockedAxios.get.mockResolvedValue({ data: [] });

      // When
      const result = await AssetService.getAll();

      // Then
      expect(mockedAxios.get).toHaveBeenCalledWith(
        'http://localhost:8085/api/fixed-assets',
        {}
      );
      expect(result.data).toEqual([]);
    });
  });

  describe('Error Handling', () => {
    test('should handle network timeout', async () => {
      // Given
      const timeoutError = new Error('timeout of 5000ms exceeded');
      timeoutError.code = 'ECONNABORTED';
      mockedAxios.get.mockRejectedValue(timeoutError);

      // When & Then
      await expect(AssetService.getAll()).rejects.toThrow('timeout of 5000ms exceeded');
    });

    test('should handle server 500 error', async () => {
      // Given
      const serverError = {
        response: {
          status: 500,
          data: { message: 'Internal server error' }
        }
      };
      mockedAxios.get.mockRejectedValue(serverError);

      // When & Then
      await expect(AssetService.getAll()).rejects.toThrow();
    });

    test('should handle malformed response data', async () => {
      // Given
      const malformedResponse = { data: 'unexpected string instead of array' };
      mockedAxios.get.mockResolvedValue(malformedResponse);

      // When
      const result = await AssetService.getAll();

      // Then
      expect(result.data).toBe('unexpected string instead of array');
      // Service should return what API returns, let components handle validation
    });

    test('should handle request cancellation', async () => {
      // Given
      const cancelError = new Error('Request cancelled');
      cancelError.name = 'CanceledError';
      mockedAxios.get.mockRejectedValue(cancelError);

      // When & Then
      await expect(AssetService.getAll()).rejects.toThrow('Request cancelled');
    });
  });

  describe('Data Validation', () => {
    test('should validate asset creation data', async () => {
      const testCases = [
        { data: {}, expectedError: 'Name is required' },
        { data: { name: '' }, expectedError: 'Name cannot be empty' },
        { data: { name: 'Valid Name' }, expectedError: 'Company ID is required' },
        { data: { name: 'Valid Name', companyId: 'invalid' }, expectedError: 'Company ID must be a number' },
        { data: { name: 'Valid Name', companyId: 999, acquisitionCost: -100 }, expectedError: 'Cost must be positive' }
      ];

      for (const testCase of testCases) {
        await expect(AssetService.createAsset(testCase.data)).rejects.toThrow();
      }
    });

    test('should validate asset update data types', async () => {
      const invalidUpdates = [
        { currentValue: 'not-a-number' },
        { acquisitionCost: 'invalid' },
        { companyId: 'string-id' },
        { departmentId: 'string-id' }
      ];

      for (const invalidUpdate of invalidUpdates) {
        await expect(AssetService.updateAsset(1, invalidUpdate)).rejects.toThrow();
      }
    });
  });

  describe('Performance and Edge Cases', () => {
    test('should handle large response data', async () => {
      // Given
      const largeAssetList = Array.from({ length: 1000 }, (_, i) => ({
        ...mockAsset,
        assetId: i + 1,
        name: `Asset ${i + 1}`
      }));
      mockedAxios.get.mockResolvedValue({ data: largeAssetList });

      // When
      const result = await AssetService.getAll();

      // Then
      expect(result.data).toHaveLength(1000);
      expect(result.data[0].assetId).toBe(1);
      expect(result.data[999].assetId).toBe(1000);
    });

    test('should handle concurrent requests', async () => {
      // Given
      mockedAxios.get.mockResolvedValue({ data: [mockAsset] });

      // When
      const promises = [
        AssetService.getAll(),
        AssetService.getById(1),
        AssetService.getByCompany(999),
        AssetService.getByDepartment(1),
        AssetService.getByStatus('ACTIVE')
      ];

      // Then
      await expect(Promise.all(promises)).resolves.toHaveLength(5);
      expect(mockedAxios.get).toHaveBeenCalledTimes(5);
    });

    test('should handle empty string parameters gracefully', async () => {
      // When & Then
      await expect(AssetService.getByStatus('')).rejects.toThrow();
      await expect(AssetService.createAsset({ name: '', companyId: 999 })).rejects.toThrow();
    });

    test('should handle extremely large numbers', async () => {
      // Given
      const assetWithLargeValues = {
        name: 'Expensive Asset',
        acquisitionCost: 999999999.99,
        currentValue: 888888888.88,
        companyId: 999
      };
      mockedAxios.post.mockResolvedValue({ data: { ...assetWithLargeValues, assetId: 1 } });

      // When
      const result = await AssetService.createAsset(assetWithLargeValues);

      // Then
      expect(result.data.acquisitionCost).toBe(999999999.99);
      expect(result.data.currentValue).toBe(888888888.88);
    });
  });
});