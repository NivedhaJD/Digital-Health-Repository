// ===== LOGIN HANDLER =====
function login() {
  const params = new URLSearchParams(window.location.search);
  const role = params.get("role");
  const user = document.getElementById("username").value;
  const pass = document.getElementById("password").value;

  if (user === "doctor" && pass === "1234" && role === "doctor") {
    window.location.href = "doctor.html";
  } else if (user === "admin" && pass === "1234" && role === "admin") {
    window.location.href = "admin.html";
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
