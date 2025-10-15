function login() {
  const params = new URLSearchParams(window.location.search);
  const role = params.get("role"); // doctor, admin, or patient
  const user = document.getElementById("username").value.trim().toLowerCase();
  const pass = document.getElementById("password").value.trim();

  console.log("Role:", role, "User:", user, "Pass:", pass); // Debug log

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

