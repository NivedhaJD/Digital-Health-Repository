# 🔐 Authentication & Admin System Guide

## System Overview

The Digital Health Repository now has a complete authentication system with role-based access control and admin delete functionality.

## 🚀 Quick Start

### 1. Start the Backend API Server
```bash
# Using JDK 25
"C:\Program Files\Java\jdk-25\bin\java.exe" -jar target\digital-health-server.jar
```
**Backend runs on:** `http://localhost:8080`

### 2. Start the Frontend React App
```bash
cd frontend
npm run dev
```
**Frontend runs on:** `http://localhost:3002`

## 👥 User Roles & Access

### Default Admin Account
- **Username:** `admin`
- **Password:** `admin123`
- **Access:** Full admin panel with delete capabilities

### User Roles
1. **ADMIN** - Full system access, can delete any record
2. **PATIENT** - Access to patient portal
3. **DOCTOR** - Access to doctor portal

## 🌐 Available Pages

### Public Pages
- **Home:** `http://localhost:3002/`
- **Login:** `http://localhost:3002/login`
- **Register:** `http://localhost:3002/register`

### Protected Pages (After Login)
- **Admin Dashboard:** `http://localhost:3002/admin`
- **Patient Portal:** `http://localhost:3002/patient`
- **Doctor Portal:** `http://localhost:3002/doctor`

### Test Pages (Direct API Testing)
- **Auth Test:** `http://localhost:8080/test-auth.html`
- **Admin Test:** `http://localhost:8080/test-admin.html`

## 🔑 Authentication Features

### Registration
- Create new accounts for Admin, Patient, or Doctor roles
- Link accounts to existing patient/doctor records (optional)
- Minimum 6-character password requirement
- Unique username validation
- SHA-256 password hashing

### Login
- Username/password authentication
- Token-based session management
- Automatic redirect based on user role
- Token stored in localStorage

### Security
- Password hashing with SHA-256
- Token format: `userId:timestamp:uuid`
- Token validation on protected routes
- Automatic logout functionality

## 🛡️ Admin Panel Features

### Overview Tab
- Total patients count
- Total doctors count
- Total appointments count
- Total health records count

### Patients Management
- View all patients
- Delete patient records
- Confirmation dialog before deletion

### Doctors Management
- View all doctors
- Delete doctor records (removes all slots)
- Confirmation dialog before deletion

### Appointments Management
- View all appointments
- Delete appointments
- Returns doctor slots if appointment not completed/cancelled
- Confirmation dialog before deletion

### Health Records Management
- View all health records
- Delete health records
- Confirmation dialog before deletion

## 📡 API Endpoints

### Authentication
```
POST /api/auth/register    - Register new user
POST /api/auth/login       - User login
POST /api/auth/validate    - Validate token
```

### Delete Operations (Admin Only)
```
DELETE /api/patients/{id}        - Delete patient
DELETE /api/doctors/{id}         - Delete doctor
DELETE /api/appointments/{id}    - Delete appointment
DELETE /api/health-records/{id}  - Delete health record
```

### Existing Endpoints
```
GET    /api/patients              - List all patients
POST   /api/patients/register     - Register patient
GET    /api/doctors               - List all doctors
POST   /api/doctors/register      - Register doctor
GET    /api/appointments          - List appointments
POST   /api/appointments/book     - Book appointment
POST   /api/appointments/cancel   - Cancel appointment
GET    /api/health-records        - List health records
POST   /api/health-records/add    - Add health record
```

## 🎨 Frontend Components

### New Components
- **Login.jsx** - Login form with role-based redirect
- **Register.jsx** - Registration form with role selection
- **AdminPortal.jsx** - Updated with delete functionality

### Updated Components
- **App.jsx** - Added authentication routes
- **Home.jsx** - Redirect authenticated users to their portal
- **api.js** - Added authentication service methods

### New Styles
- **Login.css** - Login page styling
- **Register.css** - Register page styling
- **AdminPortal.css** - Enhanced admin panel styling

## 💾 Data Storage

All data is stored in `.dat` files in the `data/` directory:
- `data/users.dat` - User accounts (encrypted passwords)
- `data/patients.dat` - Patient records
- `data/doctors.dat` - Doctor records
- `data/appointments.dat` - Appointments
- `data/records.dat` - Health records

## 🔄 Workflow Examples

### 1. Admin Login & Delete a Patient
1. Go to `http://localhost:3002/login`
2. Login with `admin` / `admin123`
3. Redirected to Admin Dashboard
4. Click "Patients" tab
5. Click delete button next to any patient
6. Confirm deletion
7. Patient removed from system

### 2. Register New Patient User
1. Go to `http://localhost:3002/register`
2. Enter username and password
3. Select "Patient" role
4. Optionally link to existing patient ID (e.g., P1001)
5. Click Register
6. Login with new credentials
7. Access Patient Portal

### 3. Register New Admin
1. Use existing admin account to navigate to admin panel
2. OR use the register page to create a new admin user
3. Select "Admin" role during registration
4. New admin can access full admin panel

## 🧪 Testing

### Test Authentication
1. Open `http://localhost:8080/test-auth.html`
2. Test login with default admin
3. Test registration of new users
4. Test token validation

### Test Admin Functions
1. Open `http://localhost:8080/test-admin.html`
2. View all records
3. Test delete operations
4. Verify data refresh after deletion

### Test Frontend Integration
1. Open `http://localhost:3002`
2. Test login/logout flow
3. Test role-based redirects
4. Test admin delete functionality
5. Test responsive design

## 📊 Database Schema (File-based)

### Users
```
userId: String (U1001, U1002, ...)
username: String (unique)
passwordHash: String (SHA-256)
role: ADMIN | PATIENT | DOCTOR
linkedEntityId: String (optional - P1001, D1001)
createdAt: LocalDateTime
lastLogin: LocalDateTime
isActive: Boolean
```

## 🔧 Troubleshooting

### Issue: "Port already in use"
- Backend uses port 8080
- Frontend uses port 3002 (or next available)
- Kill existing processes or change ports

### Issue: "Java version mismatch"
- Use JDK 11 or higher
- Specify full path to JDK 25: `"C:\Program Files\Java\jdk-25\bin\java.exe"`

### Issue: "Token invalid"
- Tokens expire after browser close (localStorage)
- Login again to get new token

### Issue: "Cannot delete record"
- Check if you're logged in as admin
- Verify record ID exists
- Check backend console for errors

## 🚀 Next Steps

### Potential Enhancements
1. Token expiration with refresh mechanism
2. Password reset functionality
3. Email verification
4. Audit logging for delete operations
5. Batch delete operations
6. Export data before deletion
7. Soft delete with recovery option
8. Role-based permissions granularity

## 📝 Notes

- All passwords are hashed with SHA-256
- Default admin user is created automatically on first run
- Delete operations are permanent (no soft delete)
- Doctor deletion removes all associated slots
- Appointment deletion returns slots to doctor if not completed/cancelled

---

**System Ready!** 🎉

Access the application at: **http://localhost:3002**
