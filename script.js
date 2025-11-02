// Digital Health Repository - Full Frontend Integration with REST API
// API Base URL
const API_BASE = 'http://localhost:8080/api';

// Store current user session
let currentUser = {
  role: null,
  id: null,
  name: null
};

// ========== LOGIN FUNCTION ==========
function login() {
  const params = new URLSearchParams(window.location.search);
  const role = params.get("role");
  const user = document.getElementById("username").value.trim().toLowerCase();
  const pass = document.getElementById("password").value.trim();

  console.log("Role:", role, "User:", user, "Pass:", pass);

  // Simple authentication (for demo purposes)
  if (role === "doctor" && user === "doctor" && pass === "1234") {
    currentUser = { role: 'doctor', id: 'D001', name: 'Dr. Smith' };
    sessionStorage.setItem('currentUser', JSON.stringify(currentUser));
    alert("Doctor login successful!");
    window.location.href = "doctor.html";
  } else if (role === "admin" && user === "admin" && pass === "1234") {
    currentUser = { role: 'admin', id: 'A001', name: 'Admin' };
    sessionStorage.setItem('currentUser', JSON.stringify(currentUser));
    alert("Admin login successful!");
    window.location.href = "admin.html";
  } else if (role === "patient" && user === "patient" && pass === "1234") {
    currentUser = { role: 'patient', id: 'P1001', name: 'John Doe' };
    sessionStorage.setItem('currentUser', JSON.stringify(currentUser));
    alert("Patient login successful!");
    window.location.href = "patient.html";
  } else {
    alert("Invalid credentials!");
  }
}

// Load current user from session
function loadCurrentUser() {
  const stored = sessionStorage.getItem('currentUser');
  if (stored) {
    currentUser = JSON.parse(stored);
    return true;
  }
  return false;
}

// ========== PATIENT FUNCTIONS ==========

async function bookAppointment() {
  if (!loadCurrentUser() || currentUser.role !== 'patient') {
    alert('Please login as a patient first');
    window.location.href = 'index.html';
    return;
  }

  // Get list of doctors
  try {
    const response = await fetch(`${API_BASE}/doctors`);
    const doctors = await response.json();
    
    if (doctors.length === 0) {
      alert('No doctors available at the moment');
      return;
    }

    // Create a simple form
    let doctorOptions = doctors.map(d => `${d.doctorId}: ${d.name} (${d.specialty})`).join('\n');
    let doctorId = prompt(`Available Doctors:\n${doctorOptions}\n\nEnter Doctor ID:`);
    
    if (!doctorId) return;

    let dateTime = prompt('Enter appointment date and time (YYYY-MM-DD HH:MM):');
    if (!dateTime) return;

    // Book appointment
    const bookResponse = await fetch(`${API_BASE}/appointments/book`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        patientId: currentUser.id,
        doctorId: doctorId,
        dateTime: dateTime
      })
    });

    const result = await bookResponse.json();
    
    if (bookResponse.ok) {
      alert(`Appointment booked successfully!\nAppointment ID: ${result.appointmentId}`);
    } else {
      alert(`Error: ${result.error || 'Failed to book appointment'}`);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to book appointment. Is the server running?');
  }
}

async function viewHealthRecords() {
  if (!loadCurrentUser() || currentUser.role !== 'patient') {
    alert('Please login as a patient first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/health-records?patientId=${currentUser.id}`);
    const records = await response.json();
    
    if (records.length === 0) {
      alert('No health records found');
      return;
    }

    let recordText = 'Your Health Records:\n\n';
    records.forEach((record, index) => {
      recordText += `Record #${index + 1}:\n`;
      recordText += `Date: ${record.date || 'N/A'}\n`;
      recordText += `Doctor: ${record.doctorId}\n`;
      recordText += `Symptoms: ${record.symptoms}\n`;
      recordText += `Diagnosis: ${record.diagnosis}\n`;
      recordText += `Prescription: ${record.prescription}\n\n`;
    });

    alert(recordText);
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to fetch health records');
  }
}

async function viewHistory() {
  if (!loadCurrentUser() || currentUser.role !== 'patient') {
    alert('Please login as a patient first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/appointments?patientId=${currentUser.id}`);
    const appointments = await response.json();
    
    if (appointments.length === 0) {
      alert('No appointment history found');
      return;
    }

    let appointmentText = 'Your Appointments:\n\n';
    appointments.forEach((appt, index) => {
      appointmentText += `Appointment #${index + 1}:\n`;
      appointmentText += `ID: ${appt.appointmentId}\n`;
      appointmentText += `Doctor: ${appt.doctorId}\n`;
      appointmentText += `Date/Time: ${appt.dateTime}\n`;
      appointmentText += `Status: ${appt.status}\n\n`;
    });

    const cancel = confirm(appointmentText + '\nWould you like to cancel an appointment?');
    if (cancel) {
      const apptId = prompt('Enter Appointment ID to cancel:');
      if (apptId) {
        await cancelAppointment(apptId);
      }
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to fetch appointments');
  }
}

async function cancelAppointment(appointmentId) {
  try {
    const response = await fetch(`${API_BASE}/appointments/cancel`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ appointmentId })
    });

    const result = await response.json();
    
    if (response.ok) {
      alert('Appointment cancelled successfully!');
    } else {
      alert(`Error: ${result.error || 'Failed to cancel appointment'}`);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to cancel appointment');
  }
}

// ========== DOCTOR FUNCTIONS ==========

async function viewPatients() {
  if (!loadCurrentUser() || currentUser.role !== 'doctor') {
    alert('Please login as a doctor first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/patients`);
    const patients = await response.json();
    
    if (patients.length === 0) {
      alert('No patients registered');
      return;
    }

    let patientText = 'Registered Patients:\n\n';
    patients.forEach((patient, index) => {
      patientText += `${index + 1}. ${patient.name} (ID: ${patient.patientId})\n`;
      patientText += `   Age: ${patient.age}, Gender: ${patient.gender}\n`;
      patientText += `   Contact: ${patient.contact}\n\n`;
    });

    alert(patientText);
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to fetch patients');
  }
}

async function addHealthRecord() {
  if (!loadCurrentUser() || currentUser.role !== 'doctor') {
    alert('Please login as a doctor first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const patientId = prompt('Enter Patient ID:');
    if (!patientId) return;

    const symptoms = prompt('Enter Symptoms:');
    if (!symptoms) return;

    const diagnosis = prompt('Enter Diagnosis:');
    if (!diagnosis) return;

    const prescription = prompt('Enter Prescription:');
    if (!prescription) return;

    const response = await fetch(`${API_BASE}/health-records/add`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        patientId: patientId,
        doctorId: currentUser.id,
        symptoms: symptoms,
        diagnosis: diagnosis,
        prescription: prescription
      })
    });

    const result = await response.json();
    
    if (response.ok) {
      alert(`Health record added successfully!\nRecord ID: ${result.recordId}`);
    } else {
      alert(`Error: ${result.error || 'Failed to add health record'}`);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to add health record');
  }
}

async function viewAppointments() {
  if (!loadCurrentUser() || currentUser.role !== 'doctor') {
    alert('Please login as a doctor first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/appointments?doctorId=${currentUser.id}`);
    const appointments = await response.json();
    
    if (appointments.length === 0) {
      alert('No appointments scheduled');
      return;
    }

    let appointmentText = 'Your Appointments:\n\n';
    appointments.forEach((appt, index) => {
      appointmentText += `${index + 1}. Appointment ID: ${appt.appointmentId}\n`;
      appointmentText += `   Patient: ${appt.patientId}\n`;
      appointmentText += `   Date/Time: ${appt.dateTime}\n`;
      appointmentText += `   Status: ${appt.status}\n\n`;
    });

    alert(appointmentText);
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to fetch appointments');
  }
}

// ========== ADMIN FUNCTIONS ==========

async function manageDoctors() {
  if (!loadCurrentUser() || currentUser.role !== 'admin') {
    alert('Please login as an admin first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/doctors`);
    const doctors = await response.json();
    
    let doctorText = 'Registered Doctors:\n\n';
    if (doctors.length === 0) {
      doctorText = 'No doctors registered yet\n\n';
    } else {
      doctors.forEach((doctor, index) => {
        doctorText += `${index + 1}. ${doctor.name} (ID: ${doctor.doctorId})\n`;
        doctorText += `   Specialty: ${doctor.specialty}\n`;
        doctorText += `   Available Slots: ${doctor.availableSlots || 'N/A'}\n\n`;
      });
    }

    alert(doctorText);
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to fetch doctors');
  }
}

async function managePatients() {
  if (!loadCurrentUser() || currentUser.role !== 'admin') {
    alert('Please login as an admin first');
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/patients`);
    const patients = await response.json();
    
    let patientText = 'Registered Patients:\n\n';
    if (patients.length === 0) {
      patientText = 'No patients registered yet\n\n';
    } else {
      patients.forEach((patient, index) => {
        patientText += `${index + 1}. ${patient.name} (ID: ${patient.patientId})\n`;
        patientText += `   Age: ${patient.age}, Gender: ${patient.gender}\n`;
        patientText += `   Contact: ${patient.contact}\n\n`;
      });
    }

    const action = confirm(patientText + '\nWould you like to register a new patient?');
    if (action) {
      await registerNewPatient();
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to fetch patients');
  }
}

async function registerNewPatient() {
  const name = prompt('Enter Patient Name:');
  if (!name) return;

  const age = prompt('Enter Patient Age:');
  if (!age) return;

  const gender = prompt('Enter Patient Gender (Male/Female/Other):');
  if (!gender) return;

  const contact = prompt('Enter Patient Contact:');
  if (!contact) return;

  try {
    const response = await fetch(`${API_BASE}/patients/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: name,
        age: parseInt(age),
        gender: gender,
        contact: contact
      })
    });

    const result = await response.json();
    
    if (response.ok) {
      alert(`Patient registered successfully!\nPatient ID: ${result.patientId}`);
    } else {
      alert(`Error: ${result.error || 'Failed to register patient'}`);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to register patient');
  }
}

async function viewReports() {
  if (!loadCurrentUser() || currentUser.role !== 'admin') {
    alert('Please login as an admin first');
    window.location.href = 'index.html';
    return;
  }

  try {
    // Fetch all data
    const [patientsRes, doctorsRes, appointmentsRes] = await Promise.all([
      fetch(`${API_BASE}/patients`),
      fetch(`${API_BASE}/doctors`),
      fetch(`${API_BASE}/appointments`)
    ]);

    const patients = await patientsRes.json();
    const doctors = await doctorsRes.json();
    const appointments = await appointmentsRes.json();

    let report = '========== SYSTEM REPORT ==========\n\n';
    report += `Total Patients: ${patients.length}\n`;
    report += `Total Doctors: ${doctors.length}\n`;
    report += `Total Appointments: ${appointments.length}\n\n`;

    // Count appointments by status
    const statusCount = {};
    appointments.forEach(appt => {
      statusCount[appt.status] = (statusCount[appt.status] || 0) + 1;
    });

    report += 'Appointments by Status:\n';
    Object.keys(statusCount).forEach(status => {
      report += `  ${status}: ${statusCount[status]}\n`;
    });

    alert(report);
  } catch (error) {
    console.error('Error:', error);
    alert('Failed to generate report');
  }
}

// ========== LOGOUT FUNCTION ==========
function logout() {
  sessionStorage.removeItem('currentUser');
  currentUser = { role: null, id: null, name: null };
  window.location.href = 'index.html';
}

// ========== PAGE LOAD HANDLER ==========
window.addEventListener('DOMContentLoaded', () => {
  console.log('Digital Health Repository loaded');
  console.log('API Base:', API_BASE);
  
  // Load user if exists
  loadCurrentUser();
  
  // Update welcome message if on dashboard page
  const header = document.querySelector('main h2');
  if (header && currentUser.name) {
    header.textContent = `Welcome, ${currentUser.name}!`;
  }
});
