// ===== LOGIN HANDLER =====
function login() {
  const params = new URLSearchParams(window.location.search);
  const role = params.get("role");
  const user = document.getElementById("username").value;
  const pass = document.getElementById("password").value;

  if (role === "doctor" && user === "doctor" && pass === "1234") {
    window.location.href = "doctor.html";
  } else if (role === "admin" && user === "admin" && pass === "1234") {
    window.location.href = "admin.html";
  } else if (role === "patient" && user === "patient" && pass === "1234") {
    window.location.href = "patient.html";
  } else {
    alert("Invalid credentials!");
  }
}

window.onload = () => {
  const role = new URLSearchParams(window.location.search).get("role");
  const title = document.getElementById("role-title");
  if (title && role) {
    title.textContent = `${role.charAt(0).toUpperCase() + role.slice(1)} Login`;
  }
};

// ===== DOCTOR PAGE =====
const records = [];

function addRecord() {
  const patient = document.getElementById("patientName").value;
  const symptoms = document.getElementById("symptoms").value;
  const diagnosis = document.getElementById("diagnosis").value;
  const prescription = document.getElementById("prescription").value;

  const record = { patient, symptoms, diagnosis, prescription };
  records.push(record);

  document.getElementById("records").innerHTML += 
    `<p><b>${patient}</b> - ${symptoms}, ${diagnosis}, ${prescription}</p>`;
}

// ===== ADMIN PAGE =====
const appointments = [];

function addAppointment() {
  const info = document.getElementById("appointment").value;
  appointments.push(info);
  document.getElementById("appointments").innerHTML += `<p>${info}</p>`;
}

// ===== PATIENT PAGE =====
const myAppointments = [];

function bookAppointment() {
  const doctorName = document.getElementById("doctorName").value;
  const date = document.getElementById("date").value;
  const timeSlot = document.getElementById("timeSlot").value;

  const appt = { doctorName, date, timeSlot };
  myAppointments.push(appt);

  document.getElementById("myAppointments").innerHTML += 
    `<p><b>Dr. ${doctorName}</b> - ${date} at ${timeSlot}</p>`;
}
