// frontend/src/__tests__/services/authService.test.js
import AuthService from '../../services/authService';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
global.localStorage = localStorageMock;

describe('AuthService', () => {
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    localStorageMock.getItem.mockClear();
    localStorageMock.setItem.mockClear();
    localStorageMock.removeItem.mockClear();
  });

  describe('login', () => {
    const mockLoginData = {
      username: 'testuser',
      password: 'testpassword'
    };

    const mockResponse = {
      data: {
        token: 'mock-jwt-token',
        user: {
          userId: 1,
          username: 'testuser',
          email: 'test@example.com',
          fullName: 'Test User',
          companyId: 999
        }
      }
    };

    test('should login successfully with valid credentials', async () => {
      // Given
      mockedAxios.post.mockResolvedValue(mockResponse);

      // When
      const result = await AuthService.login(mockLoginData);

      // Then
      expect(mockedAxios.post).toHaveBeenCalledWith(
        'http://localhost:8085/api/auth/login',
        mockLoginData
      );
      expect(localStorageMock.setItem).toHaveBeenCalledWith(
        'user',
        JSON.stringify({
          token: mockResponse.data.token,
          ...mockResponse.data.user
        })
      );
      expect(result).toEqual({
        token: mockResponse.data.token,
        ...mockResponse.data.user
      });
    });

    test('should handle login failure with invalid credentials', async () => {
      // Given
      const errorResponse = {
        response: {
          status: 401,
          data: {
            message: 'Invalid username or password'
          }
        }
      };
      mockedAxios.post.mockRejectedValue(errorResponse);

      // When & Then
      await expect(AuthService.login(mockLoginData)).rejects.toThrow('Invalid username or password');
      expect(localStorageMock.setItem).not.toHaveBeenCalled();
    });

    test('should handle network error during login', async () => {
      // Given
      const networkError = {
        request: {}
      };
      mockedAxios.post.mockRejectedValue(networkError);

      // When & Then
      await expect(AuthService.login(mockLoginData)).rejects.toThrow(
        'Unable to connect to the server. Please check your connection.'
      );
    });

    test('should validate required login fields', async () => {
      // When & Then
      await expect(AuthService.login({})).rejects.toThrow();
      await expect(AuthService.login({ username: 'test' })).rejects.toThrow();
      await expect(AuthService.login({ password: 'test' })).rejects.toThrow();
    });

    test('should handle server error during login', async () => {
      // Given
      const serverError = {
        response: {
          status: 500,
          data: {
            message: 'Internal server error'
          }
        }
      };
      mockedAxios.post.mockRejectedValue(serverError);

      // When & Then
      await expect(AuthService.login(mockLoginData)).rejects.toThrow('Internal server error');
    });
  });

  describe('logout', () => {
    test('should logout and clear localStorage', () => {
      // When
      AuthService.logout();

      // Then
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
    });

    test('should handle logout when no user is stored', () => {
      // Given
      localStorageMock.getItem.mockReturnValue(null);

      // When & Then
      expect(() => AuthService.logout()).not.toThrow();
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
    });
  });

  describe('getCurrentUser', () => {
    test('should return current user from localStorage', () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser',
        token: 'mock-token',
        companyId: 999
      };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

      // When
      const result = AuthService.getCurrentUser();

      // Then
      expect(localStorageMock.getItem).toHaveBeenCalledWith('user');
      expect(result).toEqual(mockUser);
    });

    test('should return null when no user is stored', () => {
      // Given
      localStorageMock.getItem.mockReturnValue(null);

      // When
      const result = AuthService.getCurrentUser();

      // Then
      expect(result).toBeNull();
    });

    test('should handle invalid JSON in localStorage', () => {
      // Given
      localStorageMock.getItem.mockReturnValue('invalid-json');

      // When
      const result = AuthService.getCurrentUser();

      // Then
      expect(result).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    test('should return true when user has valid token', () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser',
        token: 'valid-token'
      };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

      // When
      const result = AuthService.isAuthenticated();

      // Then
      expect(result).toBe(true);
    });

    test('should return false when no user is stored', () => {
      // Given
      localStorageMock.getItem.mockReturnValue(null);

      // When
      const result = AuthService.isAuthenticated();

      // Then
      expect(result).toBe(false);
    });

    test('should return false when user has no token', () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser'
        // no token property
      };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

      // When
      const result = AuthService.isAuthenticated();

      // Then
      expect(result).toBe(false);
    });

    test('should return false when token is empty', () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser',
        token: ''
      };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

      // When
      const result = AuthService.isAuthenticated();

      // Then
      expect(result).toBe(false);
    });
  });

  describe('register', () => {
    const mockRegisterData = {
      username: 'newuser',
      email: 'newuser@example.com',
      password: 'newpassword',
      fullName: 'New User',
      companyId: 999
    };

    const mockRegisterResponse = {
      data: {
        message: 'User registered successfully',
        userId: 2
      }
    };

    test('should register new user successfully', async () => {
      // Given
      mockedAxios.post.mockResolvedValue(mockRegisterResponse);

      // When
      const result = await AuthService.register(mockRegisterData);

      // Then
      expect(mockedAxios.post).toHaveBeenCalledWith(
        'http://localhost:8085/api/auth/register',
        mockRegisterData
      );
      expect(result).toEqual(mockRegisterResponse.data);
    });

    test('should handle registration failure with existing username', async () => {
      // Given
      const errorResponse = {
        response: {
          status: 409,
          data: {
            message: 'Username already exists'
          }
        }
      };
      mockedAxios.post.mockRejectedValue(errorResponse);

      // When & Then
      await expect(AuthService.register(mockRegisterData)).rejects.toThrow('Username already exists');
    });

    test('should validate required registration fields', async () => {
      // When & Then
      await expect(AuthService.register({})).rejects.toThrow();
      await expect(AuthService.register({ username: 'test' })).rejects.toThrow();
    });
  });

  describe('refreshToken', () => {
    test('should refresh token successfully', async () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser',
        token: 'old-token'
      };
      const mockRefreshResponse = {
        data: {
          token: 'new-token',
          user: mockUser
        }
      };
      
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));
      mockedAxios.post.mockResolvedValue(mockRefreshResponse);

      // When
      const result = await AuthService.refreshToken();

      // Then
      expect(mockedAxios.post).toHaveBeenCalledWith(
        'http://localhost:8085/api/auth/refresh',
        {},
        {
          headers: {
            Authorization: 'Bearer old-token'
          }
        }
      );
      expect(localStorageMock.setItem).toHaveBeenCalledWith(
        'user',
        JSON.stringify({
          ...mockUser,
          token: 'new-token'
        })
      );
      expect(result).toEqual({
        ...mockUser,
        token: 'new-token'
      });
    });

    test('should handle refresh token failure', async () => {
      // Given
      localStorageMock.getItem.mockReturnValue(null);

      // When & Then
      await expect(AuthService.refreshToken()).rejects.toThrow('No user found');
    });
  });

  describe('getAuthHeader', () => {
    test('should return auth header with valid token', () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser',
        token: 'valid-token'
      };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

      // When
      const result = AuthService.getAuthHeader();

      // Then
      expect(result).toEqual({
        headers: {
          Authorization: 'Bearer valid-token'
        }
      });
    });

    test('should return empty object when no user', () => {
      // Given
      localStorageMock.getItem.mockReturnValue(null);

      // When
      const result = AuthService.getAuthHeader();

      // Then
      expect(result).toEqual({});
    });

    test('should return empty object when no token', () => {
      // Given
      const mockUser = {
        userId: 1,
        username: 'testuser'
        // no token
      };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser));

      // When
      const result = AuthService.getAuthHeader();

      // Then
      expect(result).toEqual({});
    });
  });

  describe('Edge Cases and Error Handling', () => {
    test('should handle malformed localStorage data', () => {
      // Given
      localStorageMock.getItem.mockReturnValue('{"incomplete": json');

      // When & Then
      expect(() => AuthService.getCurrentUser()).not.toThrow();
      expect(AuthService.getCurrentUser()).toBeNull();
      expect(AuthService.isAuthenticated()).toBe(false);
    });

    test('should handle localStorage quota exceeded', () => {
      // Given
      const mockUser = { userId: 1, token: 'token' };
      localStorageMock.setItem.mockImplementation(() => {
        throw new Error('QuotaExceededError');
      });

      // When & Then
      expect(() => {
        AuthService.login({ username: 'test', password: 'test' });
      }).not.toThrow();
    });

    test('should handle concurrent login attempts', async () => {
      // Given
      const loginData = { username: 'test', password: 'test' };
      const mockResponse = {
        data: {
          token: 'token',
          user: { userId: 1, username: 'test' }
        }
      };
      mockedAxios.post.mockResolvedValue(mockResponse);

      // When
      const promise1 = AuthService.login(loginData);
      const promise2 = AuthService.login(loginData);

      // Then
      await expect(Promise.all([promise1, promise2])).resolves.toHaveLength(2);
    });
  });
});