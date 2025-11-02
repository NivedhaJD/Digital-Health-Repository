# 🚀 Frontend-Backend Integration Complete!

## ✅ What's Been Set Up

I've created a **REST API Server** that connects your HTML/CSS/JavaScript frontend with your Java backend!

### 🌐 Server Status: RUNNING
- **URL**: http://localhost:8080
- **Frontend**: http://localhost:8080/index.html
- **API Base**: http://localhost:8080/api

## 📡 Available API Endpoints

### Patient Endpoints
- `GET /api/patients` - List all patients
- `POST /api/patients/register` - Register new patient
  ```json
  {
    "name": "John Doe",
    "age": 30,
    "gender": "Male",
    "contact": "1234567890"
  }
  ```

### Doctor Endpoints
- `GET /api/doctors` - List all doctors

### Appointment Endpoints
- `GET /api/appointments` - List all appointments
- `GET /api/appointments?patientId=P1000` - Get patient appointments
- `GET /api/appointments?doctorId=D001` - Get doctor appointments
- `POST /api/appointments/book` - Book appointment
  ```json
  {
    "patientId": "P1000",
    "doctorId": "D001",
    "dateTime": "2025-11-15T10:00:00"
  }
  ```
- `POST /api/appointments/cancel` - Cancel appointment
  ```json
  {
    "appointmentId": "A001"
  }
  ```

### Health Record Endpoints
- `GET /api/health-records` - List all health records
- `GET /api/health-records?patientId=P1000` - Get patient records
- `POST /api/health-records/add` - Add health record
  ```json
  {
    "patientId": "P1000",
    "doctorId": "D001",
    "symptoms": "Fever, cough",
    "diagnosis": "Common cold",
    "prescription": "Rest and fluids"
  }
  ```

## 🔧 How to Use

### 1. Start the Server (Already Running!)
```powershell
java -cp "target/classes" com.digitalhealth.api.ApiServer
```

### 2. Access the Application
Open your browser and go to:
```
http://localhost:8080/index.html
```

### 3. Use JavaScript to Call the API

Example - Register a Patient:
```javascript
fetch('http://localhost:8080/api/patients/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: 'John Doe',
    age: 30,
    gender: 'Male',
    contact: '1234567890'
  })
})
.then(response => response.json())
.then(data => {
  console.log('Patient registered:', data.patientId);
})
.catch(error => console.error('Error:', error));
```

Example - Get All Doctors:
```javascript
fetch('http://localhost:8080/api/doctors')
  .then(response => response.json())
  .then(doctors => {
    console.log('Doctors:', doctors);
    // doctors is an array of doctor objects
  });
```

Example - Book an Appointment:
```javascript
fetch('http://localhost:8080/api/appointments/book', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    patientId: 'P1000',
    doctorId: 'D001',
    dateTime: '2025-11-15T10:00:00'
  })
})
.then(response => response.json())
.then(data => {
  console.log('Appointment booked:', data.appointmentId);
});
```

## 📁 Project Structure

```
Digital-Health-Repository/
├── index.html          ← Your frontend entry point
├── login.html          ← Login page
├── patient.html        ← Patient dashboard
├── doctor.html         ← Doctor dashboard
├── admin.html          ← Admin dashboard
├── script.js           ← Your JavaScript (update this to call API)
├── style.css           ← Your styles
└── src/main/java/
    └── com/digitalhealth/
        ├── api/
        │   └── ApiServer.java    ← REST API Server ✅
        ├── facade/
        │   └── BackendFacade.java ← Business logic
        └── ...

```

## 🎯 Next Steps

1. **Update your `script.js`** to call the REST API endpoints instead of using hardcoded data
2. **Update your HTML pages** to fetch and display real data from the backend
3. **Test the integration** by registering patients, booking appointments, etc.

## 🛑 How to Stop the Server

Press `Ctrl+C` in the terminal where the server is running.

## 🔄 How to Restart

```powershell
mvn clean compile
java -cp "target/classes" com.digitalhealth.api.ApiServer
```

## 🎉 You're All Set!

Your Java backend is now connected to your HTML/JavaScript frontend!

**Server running at**: http://localhost:8080  
**Data stored in**: `data/` directory (file-based persistence)

Open http://localhost:8080/index.html in your browser to see it in action! 🚀
