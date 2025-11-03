const API = 'http://localhost:8080';

// LOGIN
if (document.getElementById('login-btn')) {
  document.getElementById('login-btn').onclick = async () => {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;

    const res = await fetch(`${API}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, role })
    });

    const data = await res.json();
    if (data.token) {
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', role);
      if (role === 'ADMIN') window.location.href = 'admin.html';
      else if (role === 'DOCTOR') window.location.href = 'doctor.html';
      else window.location.href = 'patient.html';
    } else {
      document.getElementById('login-msg').innerText = data.error || 'Login failed!';
    }
  };
}

// PATIENT: Book Appointment
if (document.getElementById('book-appointment')) {
  document.getElementById('book-appointment').onclick = async () => {
    const doctorId = document.getElementById('doctorId').value;
    const date = document.getElementById('date').value;
    const token = localStorage.getItem('token');

    const res = await fetch(`${API}/api/patient/book`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
      body: JSON.stringify({ doctorId, date })
    });

    const msg = await res.json();
    document.getElementById('book-msg').innerText = msg.status || msg.error;
  };
}

// PATIENT: View Appointments
if (document.getElementById('load-appointments')) {
  document.getElementById('load-appointments').onclick = async () => {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API}/api/patient/appointments`, {
      headers: { Authorization: 'Bearer ' + token }
    });
    const list = await res.json();
    const div = document.getElementById('appointments');
    div.innerHTML = '';
    list.forEach(a => {
      div.innerHTML += `<div class="mini">Doctor ID: ${a.doctorId} | Date: ${a.date}</div>`;
    });
  };
}

// DOCTOR: View Appointments
if (document.getElementById('view-appointments')) {
  document.getElementById('view-appointments').onclick = async () => {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API}/api/doctor/appointments`, {
      headers: { Authorization: 'Bearer ' + token }
    });
    const data = await res.json();
    const div = document.getElementById('appointment-list');
    div.innerHTML = '';
    data.forEach(a => {
      div.innerHTML += `<div class="mini">Patient: ${a.patientName} | Date: ${a.date}</div>`;
    });
  };
}
