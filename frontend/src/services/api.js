// API Service for Digital Health System
const API_BASE = '/api';

// Get auth token from localStorage
const getAuthToken = () => localStorage.getItem('authToken');
const getUserData = () => {
  const userData = localStorage.getItem('userData');
  return userData ? JSON.parse(userData) : null;
};

// Generic request handler
async function apiRequest(endpoint, options = {}) {
  try {
    const token = getAuthToken();
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };
    
    // Add auth token if available
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      headers,
      ...options,
    });

    const data = await response.text().then(text => {
      try {
        return JSON.parse(text);
      } catch {
        return { error: text };
      }
    });

    if (!response.ok) {
      // Handle error messages from backend (can be in 'error' or 'message' field)
      const errorMessage = data.error || data.message || `HTTP ${response.status}: ${response.statusText}`;
      throw new Error(errorMessage);
    }

    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

// Patient API
export const patientAPI = {
  getAll: () => apiRequest('/patients'),
  
  getById: (id) => apiRequest(`/patients/${id}`),
  
  register: (patient) => apiRequest('/patients/register', {
    method: 'POST',
    body: JSON.stringify(patient),
  }),
  
  update: (id, patient) => apiRequest(`/patients/${id}`, {
    method: 'PUT',
    body: JSON.stringify(patient),
  }),

  delete: (id) => apiRequest(`/patients/${id}`, {
    method: 'DELETE',
  }),
};

// Doctor API
export const doctorAPI = {
  getAll: () => apiRequest('/doctors'),
  
  getById: (id) => apiRequest(`/doctors/${id}`),
  
  register: (doctor) => apiRequest('/doctors/register', {
    method: 'POST',
    body: JSON.stringify(doctor),
  }),
  
  update: (id, doctor) => apiRequest(`/doctors/${id}`, {
    method: 'PUT',
    body: JSON.stringify(doctor),
  }),

  delete: (id) => apiRequest(`/doctors/${id}`, {
    method: 'DELETE',
  }),
};

// Appointment API
export const appointmentAPI = {
  getAll: () => apiRequest('/appointments'),
  
  getById: (id) => apiRequest(`/appointments/${id}`),
  
  getByPatient: (patientId) => apiRequest(`/appointments?patientId=${patientId}`),
  
  getByDoctor: (doctorId) => apiRequest(`/appointments?doctorId=${doctorId}`),
  
  book: (appointment) => apiRequest('/appointments/book', {
    method: 'POST',
    body: JSON.stringify(appointment),
  }),
  
  cancel: (appointmentId) => apiRequest('/appointments/cancel', {
    method: 'POST',
    body: JSON.stringify({ appointmentId }),
  }),

  delete: (id) => apiRequest(`/appointments/${id}`, {
    method: 'DELETE',
  }),
};

// Health Record API
export const healthRecordAPI = {
  getAll: () => apiRequest('/health-records'),
  
  getById: (id) => apiRequest(`/health-records/${id}`),
  
  getByPatient: (patientId) => apiRequest(`/health-records?patientId=${patientId}`),
  
  add: (record) => apiRequest('/health-records/add', {
    method: 'POST',
    body: JSON.stringify(record),
  }),
  
  update: (id, record) => apiRequest(`/health-records/${id}`, {
    method: 'PUT',
    body: JSON.stringify(record),
  }),

  delete: (id) => apiRequest(`/health-records/${id}`, {
    method: 'DELETE',
  }),
};

// Authentication API
export const authAPI = {
  login: (credentials) => apiRequest('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  }),
  
  register: (userData) => apiRequest('/auth/register', {
    method: 'POST',
    body: JSON.stringify(userData),
  }),
  
  validate: (token) => apiRequest('/auth/validate', {
    method: 'POST',
    body: JSON.stringify({ token }),
  }),

  // Helper functions
  setAuth: (userData, token) => {
    localStorage.setItem('userData', JSON.stringify(userData));
    localStorage.setItem('authToken', token);
  },

  clearAuth: () => {
    localStorage.removeItem('userData');
    localStorage.removeItem('authToken');
  },

  isAuthenticated: () => {
    return !!getAuthToken();
  },

  getUserData: () => getUserData(),
};

export default {
  patients: patientAPI,
  doctors: doctorAPI,
  appointments: appointmentAPI,
  healthRecords: healthRecordAPI,
  auth: authAPI,
};
