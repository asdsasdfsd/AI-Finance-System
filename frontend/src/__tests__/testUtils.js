/* eslint-disable testing-library/no-node-access */
// frontend/src/__tests__/testUtils.js
import React from 'react';
import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import enUS from 'antd/locale/en_US';

/**
 * Custom render function with providers
 * Wraps components with necessary providers for testing
 */
export const renderWithProviders = (ui, options = {}) => {
  const { initialEntries = ['/'], ...renderOptions } = options;

  const Wrapper = ({ children }) => (
    <BrowserRouter>
      <ConfigProvider locale={enUS}>
        {children}
      </ConfigProvider>
    </BrowserRouter>
  );

  return render(ui, { wrapper: Wrapper, ...renderOptions });
};

/**
 * Custom render function with router and initial route
 */
export const renderWithRouter = (ui, { route = '/' } = {}) => {
  window.history.pushState({}, 'Test page', route);
  return renderWithProviders(ui);
};

/**
 * Mock user data for authentication tests
 */
export const mockAuthUser = {
  userId: 1,
  username: 'testuser',
  email: 'test@example.com',
  fullName: 'Test User',
  token: 'mock-jwt-token',
  companyId: 999
};

/**
 * Mock company data
 */
export const mockCompany = {
  companyId: 999,
  companyName: 'Test Company',
  registrationNumber: 'TEST999',
  address: 'Test Address',
  contactEmail: 'test@company.com',
  phoneNumber: '123-456-7890',
  website: 'www.testcompany.com',
  isActive: true
};

/**
 * Mock department data
 */
export const mockDepartment = {
  departmentId: 1,
  name: 'Test Department',
  code: 'DEPT001',
  budget: 100000.00,
  isActive: true,
  company: {
    companyId: 999,
    companyName: 'Test Company'
  },
  manager: {
    userId: 1,
    fullName: 'Test Manager',
    username: 'testmanager'
  }
};

/**
 * Mock transaction data
 */
export const mockTransaction = {
  transactionId: 1,
  amount: 1000.00,
  currency: 'CNY',
  description: 'Test transaction',
  transactionType: 'INCOME',
  transactionDate: '2024-06-15',
  status: 'APPROVED',
  categoryId: 1,
  departmentId: 1,
  createdBy: 1,
  createdAt: '2024-06-15T10:00:00',
  updatedAt: '2024-06-15T10:00:00'
};

/**
 * Mock asset data
 */
export const mockAsset = {
  assetId: 1,
  name: 'Test Asset',
  description: 'Test asset description',
  acquisitionDate: '2024-01-01',
  acquisitionCost: 10000.00,
  currentValue: 8000.00,
  accumulatedDepreciation: 2000.00,
  location: 'Office A',
  serialNumber: 'ASSET001',
  status: 'ACTIVE',
  companyId: 999,
  departmentId: 1
};

/**
 * Mock financial report data
 */
export const mockFinancialReport = {
  reportId: 1,
  reportType: 'INCOME_STATEMENT',
  title: 'Test Income Statement',
  startDate: '2024-01-01',
  endDate: '2024-12-31',
  companyId: 999,
  data: {
    totalRevenue: 50000.00,
    totalExpenses: 30000.00,
    netIncome: 20000.00
  },
  status: 'COMPLETED',
  createdAt: '2024-06-15T10:00:00'
};

/**
 * Wait for async operations to complete
 */
export const waitForAsync = () => new Promise(resolve => setTimeout(resolve, 0));

/**
 * Mock localStorage for testing
 */
export const mockLocalStorage = (() => {
  let store = {};

  return {
    getItem: jest.fn((key) => store[key] || null),
    setItem: jest.fn((key, value) => {
      store[key] = value.toString();
    }),
    removeItem: jest.fn((key) => {
      delete store[key];
    }),
    clear: jest.fn(() => {
      store = {};
    }),
    get length() {
      return Object.keys(store).length;
    },
    key: jest.fn((index) => Object.keys(store)[index] || null)
  };
})();

/**
 * Mock axios responses for different scenarios
 */
export const mockAxiosResponses = {
  success: (data = {}) => Promise.resolve({ data }),
  error: (status = 500, message = 'Server error') => 
    Promise.reject({
      response: {
        status,
        data: { message }
      }
    }),
  networkError: () => 
    Promise.reject({
      request: {},
      message: 'Network Error'
    }),
  timeout: () => 
    Promise.reject({
      code: 'ECONNABORTED',
      message: 'timeout of 5000ms exceeded'
    })
};

/**
 * Create mock API response with pagination
 */
export const createMockPaginatedResponse = (items, page = 0, size = 10) => ({
  content: items.slice(page * size, (page + 1) * size),
  pageable: {
    pageNumber: page,
    pageSize: size
  },
  totalElements: items.length,
  totalPages: Math.ceil(items.length / size),
  first: page === 0,
  last: page === Math.ceil(items.length / size) - 1,
  numberOfElements: Math.min(size, items.length - page * size)
});

/**
 * Mock form validation helpers
 */
export const mockFormValidation = {
  required: {
    validator: (_, value) => {
      if (!value || (typeof value === 'string' && value.trim() === '')) {
        return Promise.reject(new Error('This field is required'));
      }
      return Promise.resolve();
    }
  },
  email: {
    validator: (_, value) => {
      if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        return Promise.reject(new Error('Please enter a valid email'));
      }
      return Promise.resolve();
    }
  },
  positiveNumber: {
    validator: (_, value) => {
      if (value && (isNaN(value) || Number(value) <= 0)) {
        return Promise.reject(new Error('Please enter a positive number'));
      }
      return Promise.resolve();
    }
  }
};

/**
 * Mock Ant Design message service
 */
export const mockMessage = {
  success: jest.fn(),
  error: jest.fn(),
  warning: jest.fn(),
  info: jest.fn(),
  loading: jest.fn(() => jest.fn()), // Returns a function to hide the loading
  destroy: jest.fn()
};

/**
 * Mock Ant Design notification service
 */
export const mockNotification = {
  success: jest.fn(),
  error: jest.fn(),
  warning: jest.fn(),
  info: jest.fn(),
  open: jest.fn(),
  close: jest.fn(),
  destroy: jest.fn()
};

/**
 * Helper to simulate user interactions
 */
export const userInteractions = {
  type: (input, value) => {
    input.focus();
    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.blur();
  },
  click: (element) => {
    element.dispatchEvent(new MouseEvent('click', { bubbles: true }));
  },
  select: (select, value) => {
    select.focus();
    select.value = value;
    select.dispatchEvent(new Event('change', { bubbles: true }));
  }
};

/**
 * Helper to create mock API error with different status codes
 */
export const createMockApiError = (status, message, data = {}) => ({
  response: {
    status,
    data: {
      message,
      ...data
    }
  }
});

/**
 * Helper to create mock successful API response
 */
export const createMockApiResponse = (data, status = 200) => ({
  data,
  status,
  statusText: 'OK',
  headers: {},
  config: {}
});

/**
 * Mock date helper for consistent testing
 */
export const mockDates = {
  today: '2024-06-15',
  yesterday: '2024-06-14',
  tomorrow: '2024-06-16',
  startOfYear: '2024-01-01',
  endOfYear: '2024-12-31',
  startOfMonth: '2024-06-01',
  endOfMonth: '2024-06-30'
};

/**
 * Helper to mock file upload
 */
export const createMockFile = (name = 'test.xlsx', type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') => {
  const file = new File([''], name, { type });
  Object.defineProperty(file, 'size', { value: 1024 });
  return file;
};

/**
 * Helper to mock window.URL.createObjectURL
 */
export const mockCreateObjectURL = jest.fn(() => 'mock-url');

/**
 * Helper to mock window.URL.revokeObjectURL
 */
export const mockRevokeObjectURL = jest.fn();

/**
 * Setup common mocks for all tests
 */
export const setupTestMocks = () => {
  // Mock localStorage
  Object.defineProperty(window, 'localStorage', {
    value: mockLocalStorage
  });

  // Mock URL methods
  Object.defineProperty(window.URL, 'createObjectURL', {
    value: mockCreateObjectURL
  });
  
  Object.defineProperty(window.URL, 'revokeObjectURL', {
    value: mockRevokeObjectURL
  });

  // Mock console methods to reduce noise in tests
  global.console = {
    ...console,
    warn: jest.fn(),
    error: jest.fn()
  };

  // Mock IntersectionObserver
  global.IntersectionObserver = class IntersectionObserver {
    constructor() {}
    disconnect() {}
    observe() {}
    unobserve() {}
  };

  // Mock ResizeObserver
  global.ResizeObserver = class ResizeObserver {
    constructor() {}
    disconnect() {}
    observe() {}
    unobserve() {}
  };
};

/**
 * Cleanup mocks after tests
 */
export const cleanupTestMocks = () => {
  jest.clearAllMocks();
  mockLocalStorage.clear();
};

/**
 * Helper to wait for component to update
 */
export const waitForComponentUpdate = async () => {
  await new Promise(resolve => setTimeout(resolve, 0));
};

/**
 * Helper to generate test data arrays
 */
export const generateTestData = (count, generator) => {
  return Array.from({ length: count }, (_, index) => generator(index));
};

/**
 * Mock environment variables for testing
 */
export const mockEnv = {
  REACT_APP_API_BASE_URL: 'http://localhost:8085',
  REACT_APP_VERSION: '1.0.0-test',
  NODE_ENV: 'test'
};

/**
 * Helper to assert loading states
 */
export const expectLoadingState = (container, isLoading = true) => {
  const loadingElement = container.querySelector('.ant-spin');
  if (isLoading) {
    expect(loadingElement).toBeInTheDocument();
  } else {
    expect(loadingElement).not.toBeInTheDocument();
  }
};

/**
 * Helper to assert error states
 */
export const expectErrorState = (container, hasError = true) => {
  const errorElement = container.querySelector('.ant-alert-error, .error-message');
  if (hasError) {
    expect(errorElement).toBeInTheDocument();
  } else {
    expect(errorElement).not.toBeInTheDocument();
  }
};

export default {
  renderWithProviders,
  renderWithRouter,
  mockAuthUser,
  mockCompany,
  mockDepartment,
  mockTransaction,
  mockAsset,
  mockFinancialReport,
  waitForAsync,
  mockLocalStorage,
  mockAxiosResponses,
  createMockPaginatedResponse,
  mockFormValidation,
  mockMessage,
  mockNotification,
  userInteractions,
  createMockApiError,
  createMockApiResponse,
  mockDates,
  createMockFile,
  setupTestMocks,
  cleanupTestMocks,
  waitForComponentUpdate,
  generateTestData,
  mockEnv,
  expectLoadingState,
  expectErrorState
};