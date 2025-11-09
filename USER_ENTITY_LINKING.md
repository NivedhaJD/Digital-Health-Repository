# User-Entity Linking Feature

## Overview
This feature prevents users from seeing patient/doctor registration forms if they have already registered one. Once a user registers a patient or doctor, their account is linked to that entity and they won't be asked to register again.

## How It Works

### Backend Changes

1. **AuthService.java** - Added `linkUserToEntity()` method:
   - Updates the `linkedEntityId` field in the users table
   - Links user accounts to their registered patient/doctor entities
   - Called after successful patient/doctor registration

2. **BackendFacade.java** - Added delegation method:
   - `linkUserToEntity(String userId, String entityId)` 
   - Delegates to AuthService for user-entity linking

3. **ApiServer.java** - Updated registration handlers:
   - `handleRegisterPatient()` - Extracts userId from request and links to new patient
   - `handleRegisterDoctor()` - Extracts userId from request and links to new doctor
   - Both call `facade.linkUserToEntity()` after successful registration

### Frontend Changes

1. **PatientPortal.jsx**:
   - Added `hasLinkedPatient` state to track if user has registered a patient
   - Added `checkLinkedPatient()` function that runs on component mount
   - Checks `localStorage.getItem('linkedEntityId')` for patient ID (starts with 'P')
   - If linked patient found, automatically loads patient data and shows dashboard
   - "Register Patient" button is hidden if user already has a linked patient
   - Registration form sends `userId` from localStorage
   - After successful registration, updates `linkedEntityId` in localStorage

2. **DoctorPortal.jsx**:
   - Added `hasLinkedDoctor` state to track if user has registered a doctor
   - Added `checkLinkedDoctor()` function that runs on component mount
   - Checks `localStorage.getItem('linkedEntityId')` for doctor ID (starts with 'D')
   - If linked doctor found, automatically loads doctor data and shows dashboard
   - "Register Doctor" button is hidden if user already has a linked doctor
   - Registration form sends `userId` from localStorage
   - After successful registration, updates `linkedEntityId` in localStorage

## User Experience Flow

### First Time User
1. User logs in
2. `linkedEntityId` is null in database
3. User sees "Register Patient" or "Register Doctor" button
4. User fills out registration form
5. Backend creates patient/doctor and links it to user account
6. `linkedEntityId` is updated in database and localStorage
7. Dashboard automatically loads with the registered entity

### Returning User
1. User logs in
2. Login response includes `linkedEntityId` (e.g., "P1001" or "D1001")
3. `linkedEntityId` is stored in localStorage
4. Portal component checks linkedEntityId on mount
5. Registration button is hidden
6. Dashboard loads automatically with existing entity
7. User can directly manage appointments, health records, etc.

## Database Schema
The `users` table includes the `linkedEntityId` field:
```sql
linkedEntityId VARCHAR(20) -- Stores patient/doctor ID (e.g., "P1001" or "D1001")
```

## Benefits
- ✅ Better user experience - no repeated registration requests
- ✅ Data integrity - one user account linked to one patient/doctor
- ✅ Cleaner UI - registration forms only shown when needed
- ✅ Automatic dashboard loading for returning users
- ✅ Consistent behavior across patient and doctor portals

## Files Modified
- `src/main/java/com/digitalhealth/service/AuthService.java`
- `src/main/java/com/digitalhealth/facade/BackendFacade.java`
- `src/main/java/com/digitalhealth/api/ApiServer.java`
- `frontend/src/pages/PatientPortal.jsx`
- `frontend/src/pages/DoctorPortal.jsx`

## Testing
To test this feature:
1. Start backend: `java -jar target/digital-health-server.jar`
2. Register a new user account
3. Login with the new account
4. Register a patient or doctor
5. Logout and login again
6. Verify registration button is hidden and dashboard loads automatically
