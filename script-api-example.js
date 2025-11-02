// Example API integration for Digital Health Repository
// Replace the contents of script.js with API calls

// API Base URL
const API_BASE = 'http://localhost:8080/api';

// Example: Login function that calls backend
function login() {
  const params = new URLSearchParams(window.location.search);
  const role = params.get("role");
  const user = document.getElementById("username").value.trim().toLowerCase();
  const pass = document.getElementById("password").value.trim();

  // For now, using simple authentication
  // In production, call /api/auth/login endpoint
  if (role === "doctor" && user === "doctor" && pass === "1234") {
    alert("Doctor login successful!");
    window.location.href = "doctor.html";
  } else if (role === "admin" && user === "admin" && pass === "1234") {
    alert("Admin login successful!");
    window.location.href = "admin.html";
  } else if (role === "patient" && user === "patient" && pass === "1234") {
    alert("Patient login successful!");
    window.location.href = "patient.html";
  } else {
    alert("Invalid credentials!");
  }
}

// ========== Patient Functions ==========

// Register a new patient
async function registerPatient(name, age, gender, contact) {
  try {
    const response = await fetch(`${API_BASE}/patients/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ name, age, gender, contact })
    });
    
    const data = await response.json();
    
    if (response.ok) {
      alert(`Patient registered successfully! ID: ${data.patientId}`);
      return data.patientId;
    } else {
      alert(`Error: ${data.error}`);
      return null;
    }
  } catch (error) {
    console.error('Error registering patient:', error);
    alert('Failed to register patient. Is the server running?');
    return null;
  }
}

// Get all patients
async function getAllPatients() {
  try {
    const response = await fetch(`${API_BASE}/patients`);
    const patients = await response.json();
    return patients;
  } catch (error) {
    console.error('Error fetching patients:', error);
    return [];
  }
}

// ========== Doctor Functions ==========

// Get all doctors
async function getAllDoctors() {
  try {
    const response = await fetch(`${API_BASE}/doctors`);
    const doctors = await response.json();
    return doctors;
  } catch (error) {
    console.error('Error fetching doctors:', error);
    return [];
  }
}

// Display doctors in a table
async function displayDoctors() {
  const doctors = await getAllDoctors();
  const tableBody = document.getElementById('doctorsTableBody');
  
  if (!tableBody) return;
  
  tableBody.innerHTML = '';
  
  doctors.forEach(doctor => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${doctor.doctorId}</td>
      <td>${doctor.name}</td>
      <td>${doctor.specialty}</td>
      <td>${doctor.availableSlots} slots</td>
    `;
    tableBody.appendChild(row);
  });
}

// ========== Appointment Functions ==========

// Book an appointment
async function bookAppointment(patientId, doctorId, dateTime) {
  try {
    const response = await fetch(`${API_BASE}/appointments/book`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ patientId, doctorId, dateTime })
    });
    
    const data = await response.json();
    
    if (response.ok) {
      alert(`Appointment booked successfully! ID: ${data.appointmentId}`);
      return data.appointmentId;
    } else {
      alert(`Error: ${data.error}`);
      return null;
    }
  } catch (error) {
    console.error('Error booking appointment:', error);
    alert('Failed to book appointment.');
    return null;
  }
}

// Get patient appointments
async function getPatientAppointments(patientId) {
  try {
    const response = await fetch(`${API_BASE}/appointments?patientId=${patientId}`);
    const appointments = await response.json();
    return appointments;
  } catch (error) {
    console.error('Error fetching appointments:', error);
    return [];
  }
}

// Cancel appointment
async function cancelAppointment(appointmentId) {
  try {
    const response = await fetch(`${API_BASE}/appointments/cancel`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ appointmentId })
    });
    
    const data = await response.json();
    
    if (response.ok) {
      alert('Appointment cancelled successfully!');
      return true;
    } else {
      alert(`Error: ${data.error}`);
      return false;
    }
  } catch (error) {
    console.error('Error cancelling appointment:', error);
    return false;
  }
}

// ========== Health Record Functions ==========

// Add health record
async function addHealthRecord(patientId, doctorId, symptoms, diagnosis, prescription) {
  try {
    const response = await fetch(`${API_BASE}/health-records/add`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ patientId, doctorId, symptoms, diagnosis, prescription })
    });
    
    const data = await response.json();
    
    if (response.ok) {
      alert(`Health record added successfully! ID: ${data.recordId}`);
      return data.recordId;
    } else {
      alert(`Error: ${data.error}`);
      return null;
    }
  } catch (error) {
    console.error('Error adding health record:', error);
    return null;
  }
}

// Get patient health records
async function getPatientHealthRecords(patientId) {
  try {
    const response = await fetch(`${API_BASE}/health-records?patientId=${patientId}`);
    const records = await response.json();
    return records;
  } catch (error) {
    console.error('Error fetching health records:', error);
    return [];
  }
}

// ========== Example Usage ==========

// Call this when page loads to populate data
window.addEventListener('DOMContentLoaded', async () => {
  console.log('Page loaded. Connecting to backend...');
  
  // Example: Load doctors on doctor list page
  if (document.getElementById('doctorsTableBody')) {
    await displayDoctors();
  }
  
  // Example: Load patients on admin dashboard
  if (document.getElementById('patientsTableBody')) {
    const patients = await getAllPatients();
    console.log('Loaded patients:', patients);
    // Display in table...
  }
});

// Example form submission handler
// Add this to your HTML forms
function handlePatientRegistration(event) {
  event.preventDefault();
  
  const name = document.getElementById('patientName').value;
  const age = parseInt(document.getElementById('patientAge').value);
  const gender = document.getElementById('patientGender').value;
  const contact = document.getElementById('patientContact').value;
  
  registerPatient(name, age, gender, contact);
}
