const API_BASE = "http://localhost:8080/api";

// ---------------- LOGIN ----------------
document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (res.ok) {
    const data = await res.json();
    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);

    if (data.role === "doctor") location.href = "doctor.html";
    else if (data.role === "admin") location.href = "admin.html";
    else location.href = "patient.html";
  } else {
    alert("Invalid login!");
  }
});

// --------------- DOCTOR ---------------
async function loadDoctorData() {
  const token = localStorage.getItem("token");
  const res = await fetch(`${API_BASE}/doctor/profile`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const doc = await res.json();
  document.getElementById("doctorInfo").innerHTML =
    `<p>Name: ${doc.name}</p><p>Specialization: ${doc.specialization}</p>`;
}

async function loadHealthRecords() {
  const token = localStorage.getItem("token");
  const res = await fetch(`${API_BASE}/doctor/records`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const records = await res.json();
  document.getElementById("records").innerHTML =
    records.map(r => `<div>${r.patientName} - ${r.diagnosis}</div>`).join("");
}

// --------------- PATIENT ---------------
async function loadPatientDashboard() {
  const token = localStorage.getItem("token");
  const res = await fetch(`${API_BASE}/patient/profile`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const pat = await res.json();
  document.getElementById("patientName").innerText = pat.name;
  loadAppointments();
}

document.getElementById("appointmentForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const token = localStorage.getItem("token");
  const body = {
    doctorId: document.getElementById("doctorId").value,
    appointmentDate: document.getElementById("appointmentDate").value,
    appointmentTime: document.getElementById("appointmentTime").value
  };
  await fetch(`${API_BASE}/appointments/book`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });
  alert("Appointment booked!");
  loadAppointments();
});

async function loadAppointments() {
  const token = localStorage.getItem("token");
  const res = await fetch(`${API_BASE}/appointments/my`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const list = await res.json();
  document.getElementById("appointments").innerHTML =
    list.map(a => `<div>${a.appointmentDate} - ${a.status}</div>`).join("");
}

// --------------- ADMIN ---------------
async function loadAdminDashboard() {
  const token = localStorage.getItem("token");
  const usersRes = await fetch(`${API_BASE}/admin/users`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const apptRes = await fetch(`${API_BASE}/admin/appointments`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  const users = await usersRes.json();
  const appointments = await apptRes.json();

  document.getElementById("usersList").innerHTML =
    users.map(u => `<div>${u.username} - ${u.role}</div>`).join("");

  document.getElementById("allAppointments").innerHTML =
    appointments.map(a => `<div>${a.patientName} → ${a.doctorName} on ${a.appointmentDate}</div>`).join("");
}
