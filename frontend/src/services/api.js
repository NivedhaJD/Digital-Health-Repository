// API Service for Digital Health System
const API_BASE = '/api';

// Generic request handler
async function apiRequest(endpoint, options = {}) {
  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
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
};

// Appointment API
export const appointmentAPI = {
  getAll: () => apiRequest('/appointments'),
  
  getById: (id) => apiRequest(`/appointments/${id}`),
  
  getByPatient: (patientId) => apiRequest(`/appointments/patient/${patientId}`),
  
  getByDoctor: (doctorId) => apiRequest(`/appointments/doctor/${doctorId}`),
  
  book: (appointment) => apiRequest('/appointments/book', {
    method: 'POST',
    body: JSON.stringify(appointment),
  }),
  
  updateStatus: (id, status) => apiRequest(`/appointments/${id}/status?status=${status}`, {
    method: 'PUT',
  }),
  
  cancel: (id) => apiRequest(`/appointments/${id}`, {
    method: 'DELETE',
  }),
};

// Health Record API
export const healthRecordAPI = {
  getAll: () => apiRequest('/health-records'),
  
  getById: (id) => apiRequest(`/health-records/${id}`),
  
  getByPatient: (patientId) => apiRequest(`/health-records/patient/${patientId}`),
  
  add: (record) => apiRequest('/health-records/add', {
    method: 'POST',
    body: JSON.stringify(record),
  }),
  
  update: (id, record) => apiRequest(`/health-records/${id}`, {
    method: 'PUT',
    body: JSON.stringify(record),
  }),
};

export default {
  patients: patientAPI,
  doctors: doctorAPI,
  appointments: appointmentAPI,
  healthRecords: healthRecordAPI,
};
