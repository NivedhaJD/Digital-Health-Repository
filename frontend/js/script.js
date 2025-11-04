const API = 'http://localhost:8080';

// ============ LOGIN ============
if (document.getElementById('login-btn')) {
  document.getElementById('login-btn').onclick = async () => {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;

    if (!email || !password) {
      document.getElementById('login-msg').innerText = 'Please enter email and password';
      return;
    }

    localStorage.setItem('role', role);
    localStorage.setItem('email', email);
    
    if (role === 'ADMIN') {
      window.location.href = '/html/admin.html';
    } else if (role === 'DOCTOR') {
      window.location.href = '/html/doctor.html';
    } else {
      window.location.href = '/html/patient.html';
    }
  };
}

// ============ PATIENT FUNCTIONS ============

// Register Patient
if (document.getElementById('register-patient')) {
  document.getElementById('register-patient').onclick = async () => {
    const name = document.getElementById('patientName').value;
    const age = document.getElementById('patientAge').value;
    const gender = document.getElementById('patientGender').value;
    const contact = document.getElementById('patientContact').value;

    if (!name || !age || !gender || !contact) {
      document.getElementById('register-msg').innerText = 'Please fill all fields';
      document.getElementById('register-msg').style.color = 'red';
      return;
    }

    try {
      const res = await fetch(`${API}/api/patients/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, age, gender, contact })
      });

      const data = await res.json();
      if (res.ok) {
        document.getElementById('register-msg').innerText = `Success! Your Patient ID: ${data.patientId}`;
        document.getElementById('register-msg').style.color = 'green';
        // Clear form
        document.getElementById('patientName').value = '';
        document.getElementById('patientAge').value = '';
        document.getElementById('patientGender').value = '';
        document.getElementById('patientContact').value = '';
      } else {
        document.getElementById('register-msg').innerText = data.error || 'Registration failed';
        document.getElementById('register-msg').style.color = 'red';
      }
    } catch (error) {
      document.getElementById('register-msg').innerText = 'Error: ' + error.message;
      document.getElementById('register-msg').style.color = 'red';
    }
  };
}

// Load doctors into dropdown
if (document.getElementById('bookDoctorId')) {
  async function loadDoctors() {
    try {
      const res = await fetch(`${API}/api/doctors`);
      const doctors = await res.json();
      const select = document.getElementById('bookDoctorId');
      doctors.forEach(doctor => {
        const option = document.createElement('option');
        option.value = doctor.doctorId;
        option.textContent = `${doctor.name} - ${doctor.specialty}`;
        select.appendChild(option);
      });
    } catch (error) {
      console.error('Error loading doctors:', error);
    }
  }
  loadDoctors();
}

// Book Appointment
if (document.getElementById('book-appointment')) {
  document.getElementById('book-appointment').onclick = async () => {
    const patientId = document.getElementById('bookPatientId').value;
    const doctorId = document.getElementById('bookDoctorId').value;
    const dateTime = document.getElementById('bookDateTime').value;

    if (!patientId || !doctorId || !dateTime) {
      document.getElementById('book-msg').innerText = 'Please fill all fields';
      document.getElementById('book-msg').style.color = 'red';
      return;
    }

    try {
      const res = await fetch(`${API}/api/appointments/book`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ patientId, doctorId, dateTime })
      });

      const data = await res.json();
      if (res.ok) {
        document.getElementById('book-msg').innerText = `Success! Appointment ID: ${data.appointmentId}`;
        document.getElementById('book-msg').style.color = 'green';
      } else {
        document.getElementById('book-msg').innerText = data.error || 'Booking failed';
        document.getElementById('book-msg').style.color = 'red';
      }
    } catch (error) {
      document.getElementById('book-msg').innerText = 'Error: ' + error.message;
      document.getElementById('book-msg').style.color = 'red';
    }
  };
}

// Load Patient Appointments
if (document.getElementById('load-appointments')) {
  document.getElementById('load-appointments').onclick = async () => {
    const patientId = document.getElementById('viewPatientId').value;
    
    if (!patientId) {
      document.getElementById('appointments').innerHTML = '<p style="color: red;">Please enter your Patient ID</p>';
      return;
    }

    try {
      const res = await fetch(`${API}/api/appointments?patientId=${patientId}`);
      const appointments = await res.json();
      
      const div = document.getElementById('appointments');
      div.innerHTML = '';
      
      if (appointments.length === 0) {
        div.innerHTML = '<p>No appointments found</p>';
        return;
      }
      
      appointments.forEach(apt => {
        const aptDiv = document.createElement('div');
        aptDiv.className = 'appointment-item';
        aptDiv.innerHTML = `
          <strong>Appointment ID:</strong> ${apt.appointmentId}<br>
          <strong>Doctor ID:</strong> ${apt.doctorId}<br>
          <strong>Date & Time:</strong> ${apt.appointmentDateTime}<br>
          <strong>Status:</strong> ${apt.status}<br>
          <hr>
        `;
        div.appendChild(aptDiv);
      });
    } catch (error) {
      document.getElementById('appointments').innerHTML = '<p style="color: red;">Error loading appointments</p>';
    }
  };
}

// ============ DOCTOR FUNCTIONS ============

// Register Doctor
if (document.getElementById('register-doctor')) {
  document.getElementById('register-doctor').onclick = async () => {
    const name = document.getElementById('doctorName').value;
    const specialty = document.getElementById('doctorSpecialty').value;

    if (!name || !specialty) {
      document.getElementById('doctor-register-msg').innerText = 'Please fill all fields';
      document.getElementById('doctor-register-msg').style.color = 'red';
      return;
    }

    try {
      const res = await fetch(`${API}/api/doctors/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, specialty })
      });

      const data = await res.json();
      if (res.ok) {
        document.getElementById('doctor-register-msg').innerText = `Success! Your Doctor ID: ${data.doctorId}`;
        document.getElementById('doctor-register-msg').style.color = 'green';
        // Clear form
        document.getElementById('doctorName').value = '';
        document.getElementById('doctorSpecialty').value = '';
      } else {
        document.getElementById('doctor-register-msg').innerText = data.error || 'Registration failed';
        document.getElementById('doctor-register-msg').style.color = 'red';
      }
    } catch (error) {
      document.getElementById('doctor-register-msg').innerText = 'Error: ' + error.message;
      document.getElementById('doctor-register-msg').style.color = 'red';
    }
  };
}

// Load Doctor Appointments
if (document.getElementById('view-appointments')) {
  document.getElementById('view-appointments').onclick = async () => {
    const doctorId = document.getElementById('doctorIdInput').value;
    
    if (!doctorId) {
      document.getElementById('appointment-list').innerHTML = '<p style="color: red;">Please enter your Doctor ID</p>';
      return;
    }

    try {
      const res = await fetch(`${API}/api/appointments?doctorId=${doctorId}`);
      const appointments = await res.json();
      
      const div = document.getElementById('appointment-list');
      div.innerHTML = '';
      
      if (appointments.length === 0) {
        div.innerHTML = '<p>No appointments found</p>';
        return;
      }
      
      appointments.forEach(apt => {
        const aptDiv = document.createElement('div');
        aptDiv.className = 'appointment-item';
        aptDiv.innerHTML = `
          <strong>Appointment ID:</strong> ${apt.appointmentId}<br>
          <strong>Patient ID:</strong> ${apt.patientId}<br>
          <strong>Date & Time:</strong> ${apt.appointmentDateTime}<br>
          <strong>Status:</strong> ${apt.status}<br>
          <hr>
        `;
        div.appendChild(aptDiv);
      });
    } catch (error) {
      document.getElementById('appointment-list').innerHTML = '<p style="color: red;">Error loading appointments</p>';
    }
  };
}

// Add Health Record
if (document.getElementById('add-health-record')) {
  document.getElementById('add-health-record').onclick = async () => {
    const patientId = document.getElementById('recordPatientId').value;
    const doctorId = document.getElementById('recordDoctorId').value;
    const symptoms = document.getElementById('symptoms').value;
    const diagnosis = document.getElementById('diagnosis').value;
    const prescription = document.getElementById('prescription').value;

    if (!patientId || !doctorId || !symptoms || !diagnosis) {
      document.getElementById('record-msg').innerText = 'Please fill required fields (Patient ID, Doctor ID, Symptoms, Diagnosis)';
      document.getElementById('record-msg').style.color = 'red';
      return;
    }

    try {
      const res = await fetch(`${API}/api/health-records/add`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ patientId, doctorId, symptoms, diagnosis, prescription })
      });

      const data = await res.json();
      if (res.ok) {
        document.getElementById('record-msg').innerText = `Success! Record ID: ${data.recordId}`;
        document.getElementById('record-msg').style.color = 'green';
        // Clear form
        document.getElementById('recordPatientId').value = '';
        document.getElementById('symptoms').value = '';
        document.getElementById('diagnosis').value = '';
        document.getElementById('prescription').value = '';
      } else {
        document.getElementById('record-msg').innerText = data.error || 'Failed to add record';
        document.getElementById('record-msg').style.color = 'red';
      }
    } catch (error) {
      document.getElementById('record-msg').innerText = 'Error: ' + error.message;
      document.getElementById('record-msg').style.color = 'red';
    }
  };
}

// ============ ADMIN FUNCTIONS ============

// Load all patients
if (document.getElementById('load-patients')) {
  document.getElementById('load-patients').onclick = async () => {
    try {
      const res = await fetch(`${API}/api/patients`);
      const patients = await res.json();
      
      const div = document.getElementById('patients-list');
      div.innerHTML = '';
      
      if (patients.length === 0) {
        div.innerHTML = '<p>No patients found</p>';
        return;
      }
      
      patients.forEach(patient => {
        const patDiv = document.createElement('div');
        patDiv.className = 'patient-item';
        patDiv.innerHTML = `
          <strong>ID:</strong> ${patient.patientId}<br>
          <strong>Name:</strong> ${patient.name}<br>
          <strong>Age:</strong> ${patient.age}<br>
          <strong>Gender:</strong> ${patient.gender}<br>
          <strong>Contact:</strong> ${patient.contact}<br>
          <hr>
        `;
        div.appendChild(patDiv);
      });
    } catch (error) {
      document.getElementById('patients-list').innerHTML = '<p style="color: red;">Error loading patients</p>';
    }
  };
}

// Load all doctors
if (document.getElementById('load-doctors')) {
  document.getElementById('load-doctors').onclick = async () => {
    try {
      const res = await fetch(`${API}/api/doctors`);
      const doctors = await res.json();
      
      const div = document.getElementById('doctors-list');
      div.innerHTML = '';
      
      if (doctors.length === 0) {
        div.innerHTML = '<p>No doctors found</p>';
        return;
      }
      
      doctors.forEach(doctor => {
        const docDiv = document.createElement('div');
        docDiv.className = 'doctor-item';
        docDiv.innerHTML = `
          <strong>ID:</strong> ${doctor.doctorId}<br>
          <strong>Name:</strong> ${doctor.name}<br>
          <strong>Specialty:</strong> ${doctor.specialty}<br>
          <hr>
        `;
        div.appendChild(docDiv);
      });
    } catch (error) {
      document.getElementById('doctors-list').innerHTML = '<p style="color: red;">Error loading doctors</p>';
    }
  };
}

// Load all appointments
if (document.getElementById('load-all-appointments')) {
  document.getElementById('load-all-appointments').onclick = async () => {
    try {
      const res = await fetch(`${API}/api/appointments`);
      const appointments = await res.json();
      
      const div = document.getElementById('all-appointments-list');
      div.innerHTML = '';
      
      if (appointments.length === 0) {
        div.innerHTML = '<p>No appointments found</p>';
        return;
      }
      
      appointments.forEach(apt => {
        const aptDiv = document.createElement('div');
        aptDiv.className = 'appointment-item';
        aptDiv.innerHTML = `
          <strong>ID:</strong> ${apt.appointmentId}<br>
          <strong>Patient ID:</strong> ${apt.patientId}<br>
          <strong>Doctor ID:</strong> ${apt.doctorId}<br>
          <strong>Date & Time:</strong> ${apt.appointmentDateTime}<br>
          <strong>Status:</strong> ${apt.status}<br>
          <hr>
        `;
        div.appendChild(aptDiv);
      });
    } catch (error) {
      document.getElementById('all-appointments-list').innerHTML = '<p style="color: red;">Error loading appointments</p>';
    }
  };
}
