# Digital Health Repository & Appointment System - Backend

A complete, production-ready Java 11 backend system for managing patients, doctors, appointments, and health records with file-based and MySQL persistence options.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [GUI Integration](#gui-integration)
- [Configuration](#configuration)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Build & Run](#build--run)

## ✨ Features

### Core Functionality

1. **Patient Management**
   - Register patients with auto-generated unique IDs
   - Update patient details
   - Fetch patient information
   - Input validation (age, contact format, required fields)

2. **Health Records**
   - Add health records with symptoms, diagnosis, prescription
   - List records sorted by date
   - Link records to patients and doctors

3. **Appointment System**
   - Thread-safe appointment booking
   - Doctor availability management
   - Atomic operations for booking/cancellation/rescheduling
   - Query appointments by patient, doctor, or date
   - Status tracking (BOOKED, CANCELLED, COMPLETED)

4. **Persistence**
   - **File-based**: Java serialization (default)
   - **MySQL**: JDBC with prepared statements (optional)
   - Thread-safe read-write locks for file operations

5. **Export**
   - Generate patient history reports in human-readable text format

## 🏗️ Architecture

The backend follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│                   GUI Layer                         │
│              (External HTML/JS GUI)                 │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│               BackendFacade                         │
│        (Single integration point for GUI)           │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│             Service Layer                           │
│  PatientService | DoctorService | AppointmentService│
│  HealthRecordService | ExportService                │
│  (Business logic, validation, coordination)         │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│              DAO Layer                              │
│  PatientDao | DoctorDao | AppointmentDao           │
│  HealthRecordDao                                    │
│  (Data access abstraction)                          │
└──────────────────┬──────────────────────────────────┘
                   │
          ┌────────┴────────┐
          ▼                 ▼
┌──────────────────┐ ┌──────────────────┐
│  File Storage    │ │   MySQL DB       │
│  (.dat files)    │ │   (JDBC)         │
└──────────────────┘ └──────────────────┘
```

### Package Structure

```
com.digitalhealth
├── model          # Domain entities (Patient, Doctor, Appointment, HealthRecord)
├── dto            # Data Transfer Objects for GUI communication
├── dao            # DAO interfaces
│   ├── file       # File-based implementations
│   └── mysql      # MySQL implementations
├── service        # Business logic layer
├── facade         # BackendFacade and BackendFactory
├── exception      # Custom exceptions
└── cli            # CLI demo application
```

### Design Patterns

- **Facade Pattern**: `BackendFacade` provides unified API
- **Factory Pattern**: `BackendFactory` creates backend instances
- **DAO Pattern**: Abstract data access
- **DTO Pattern**: Separate transfer objects from domain models
- **Dependency Injection**: Constructor-based (no frameworks)

## 📦 Prerequisites

- **JDK 11** or higher
- **Maven 3.6+** for building
- **MySQL 5.7+** (optional, for MySQL persistence)

## 🚀 Quick Start

### 1. Build the Project

```bash
mvn clean package
```

### 2. Run the CLI Demo

```bash
java -jar target/digital-health-backend-1.0.0.jar
```

Or with Maven:

```bash
mvn exec:java -Dexec.mainClass="com.digitalhealth.cli.CliApplication"
```

### 3. Run Tests

```bash
mvn test
```

## 📁 Project Structure

```
digital-health-backend/
├── pom.xml
├── application.properties
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/digitalhealth/
│   │   │   ├── model/           # Domain models
│   │   │   ├── dto/             # Transfer objects
│   │   │   ├── dao/             # Data access
│   │   │   ├── service/         # Business logic
│   │   │   ├── facade/          # Integration API
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── cli/             # CLI demo
│   │   └── resources/
│   │       └── schema.sql       # MySQL schema
│   └── test/
│       └── java/com/digitalhealth/
│           └── service/         # Unit tests
├── data/                        # File storage (created at runtime)
│   ├── patients.dat
│   ├── doctors.dat
│   ├── appointments.dat
│   └── records.dat
└── export/                      # Exported reports
    └── history_P1001.txt
```

## 🔌 GUI Integration

### Integration Example

The GUI can integrate with the backend by calling `BackendFacade` methods directly:

```java
import com.digitalhealth.facade.BackendFacade;
import com.digitalhealth.facade.BackendFactory;
import com.digitalhealth.dto.*;
import com.digitalhealth.exception.*;

import java.time.LocalDateTime;
import java.util.List;

public class GuiIntegrationExample {
    public static void main(String[] args) {
        // 1. Initialize backend (file-based)
        BackendFacade facade = BackendFactory.createFileBackend();
        
        // Or with custom data directory:
        // BackendFacade facade = BackendFactory.createFileBackend("data/myapp");
        
        try {
            // 2. Register a patient
            PatientDTO patientDto = new PatientDTO("Lea D.", 19, "F", "9999999999");
            String patientId = facade.registerPatient(patientDto);
            System.out.println("Patient registered: " + patientId);
            
            // 3. Get patient details
            PatientDTO patient = facade.getPatient(patientId);
            System.out.println("Patient name: " + patient.getName());
            
            // 4. List all doctors
            List<DoctorDTO> doctors = facade.listDoctors();
            if (!doctors.isEmpty()) {
                DoctorDTO doctor = doctors.get(0);
                System.out.println("Doctor: " + doctor.getName());
                
                // 5. Book appointment
                if (!doctor.getAvailableSlots().isEmpty()) {
                    LocalDateTime slot = doctor.getAvailableSlots().get(0);
                    AppointmentDTO appointment = facade.bookAppointment(
                        patientId, doctor.getDoctorId(), slot);
                    System.out.println("Appointment booked: " + appointment.getAppointmentId());
                }
            }
            
            // 6. Add health record
            HealthRecordDTO recordDto = new HealthRecordDTO(
                patientId, "D001", LocalDateTime.now(),
                "Fever, cough", "Viral infection", "Rest and hydration"
            );
            String recordId = facade.addHealthRecord(recordDto);
            System.out.println("Health record added: " + recordId);
            
            // 7. Export patient history
            facade.exportPatientHistory(patientId, "export/history_" + patientId + ".txt");
            System.out.println("History exported successfully");
            
        } catch (ValidationException e) {
            System.err.println("Validation error: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            System.err.println("Entity not found: " + e.getMessage());
        } catch (SlotUnavailableException e) {
            System.err.println("Slot unavailable: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### Exception Handling

All public API methods throw checked exceptions:

- **`ValidationException`**: Input validation failed (invalid age, missing fields, etc.)
- **`EntityNotFoundException`**: Requested entity (patient, doctor, appointment) not found
- **`SlotUnavailableException`**: Appointment slot not available
- **`IOException`**: File I/O error (export operations)

### Thread Safety

All `BackendFacade` methods are thread-safe:
- File-based DAOs use read-write locks
- Appointment booking uses synchronized blocks to prevent double-booking

## ⚙️ Configuration

Edit `application.properties`:

```properties
# Switch between file and mysql persistence
persistence=file

# File storage directory (for file persistence)
data.directory=data

# MySQL connection (for mysql persistence)
db.url=jdbc:mysql://localhost:3306/digitalhealth
db.username=root
db.password=yourpassword
```

### Using File Persistence (Default)

No configuration needed. Data files are created automatically in `data/` directory.

### Using MySQL Persistence

1. Install MySQL and create database:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

2. Update `application.properties`:

```properties
persistence=mysql
db.url=jdbc:mysql://localhost:3306/digitalhealth
db.username=root
db.password=yourpassword
```

3. Build with MySQL profile:

```bash
mvn clean package -P mysql
```

4. Run application:

```bash
java -Dpersistence=mysql -jar target/digital-health-backend-1.0.0.jar
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Test Coverage

The test suite includes:

- **Unit tests** for all service layer classes
- **Validation tests** (invalid inputs, edge cases)
- **Persistence tests** (round-trip data integrity)
- **Concurrency tests** (thread-safe appointment booking)
- **Integration tests** (service coordination)

### Sample Test Output

```
PatientServiceTest
  ✓ testRegisterPatient_Success
  ✓ testRegisterPatient_AutoIncrementId
  ✓ testRegisterPatient_MissingName
  ✓ testGetPatient_Success
  ✓ testPersistence_RoundTrip

AppointmentServiceTest
  ✓ testBookAppointment_Success
  ✓ testBookAppointment_SlotNotAvailable
  ✓ testCancelAppointment_Success
  ✓ testRescheduleAppointment_Success
  ✓ testConcurrentBooking_ThreadSafety

Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
```

## 📚 API Documentation

### Core DTO Classes

#### PatientDTO
```java
public class PatientDTO {
    private String patientId;      // Auto-generated (P####)
    private String name;           // Required
    private int age;               // 1-150
    private String gender;         // Required
    private String contact;        // 10 digits
}
```

#### DoctorDTO
```java
public class DoctorDTO {
    private String doctorId;       // Required (D###)
    private String name;
    private String specialty;
    private List<LocalDateTime> availableSlots;
}
```

#### AppointmentDTO
```java
public class AppointmentDTO {
    private String appointmentId;  // Auto-generated (A####)
    private String patientId;
    private String doctorId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;  // BOOKED, CANCELLED, COMPLETED
}
```

#### HealthRecordDTO
```java
public class HealthRecordDTO {
    private String recordId;       // Auto-generated (R####)
    private String patientId;
    private String doctorId;
    private LocalDateTime date;
    private String symptoms;
    private String diagnosis;
    private String prescription;
}
```

### BackendFacade Methods

| Method | Description | Returns | Exceptions |
|--------|-------------|---------|------------|
| `registerPatient(PatientDTO)` | Register new patient | `String` (patientId) | `ValidationException` |
| `getPatient(String)` | Get patient by ID | `PatientDTO` | `EntityNotFoundException` |
| `updatePatient(PatientDTO)` | Update patient | `void` | `EntityNotFoundException`, `ValidationException` |
| `listPatients()` | List all patients | `List<PatientDTO>` | - |
| `addDoctor(DoctorDTO)` | Add doctor | `String` (doctorId) | `ValidationException` |
| `getDoctor(String)` | Get doctor by ID | `DoctorDTO` | `EntityNotFoundException` |
| `listDoctors()` | List all doctors | `List<DoctorDTO>` | - |
| `addDoctorSlot(String, LocalDateTime)` | Add slot | `void` | `EntityNotFoundException` |
| `bookAppointment(String, String, LocalDateTime)` | Book appointment | `AppointmentDTO` | `EntityNotFoundException`, `SlotUnavailableException`, `ValidationException` |
| `cancelAppointment(String)` | Cancel appointment | `boolean` | `EntityNotFoundException` |
| `rescheduleAppointment(String, LocalDateTime)` | Reschedule | `AppointmentDTO` | `EntityNotFoundException`, `SlotUnavailableException`, `ValidationException` |
| `getAppointmentsByPatient(String)` | Query by patient | `List<AppointmentDTO>` | - |
| `getAppointmentsByDoctor(String)` | Query by doctor | `List<AppointmentDTO>` | - |
| `getAppointmentsByDate(LocalDate)` | Query by date | `List<AppointmentDTO>` | - |
| `addHealthRecord(HealthRecordDTO)` | Add record | `String` (recordId) | `EntityNotFoundException`, `ValidationException` |
| `getPatientHealthRecords(String)` | Get records | `List<HealthRecordDTO>` | - |
| `exportPatientHistory(String, String)` | Export history | `void` | `EntityNotFoundException`, `IOException` |

## 🗄️ Database Schema

### MySQL Tables

```sql
-- Patients
CREATE TABLE patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age > 0 AND age <= 150),
    gender VARCHAR(10) NOT NULL,
    contact VARCHAR(20) NOT NULL
);

-- Doctors
CREATE TABLE doctors (
    doctor_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL
);

-- Doctor Slots
CREATE TABLE doctor_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id VARCHAR(20) NOT NULL,
    slot_datetime DATETIME NOT NULL,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- Appointments
CREATE TABLE appointments (
    appointment_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    appointment_datetime DATETIME NOT NULL,
    status ENUM('BOOKED', 'CANCELLED', 'COMPLETED'),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- Health Records
CREATE TABLE health_records (
    record_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    record_date DATETIME NOT NULL,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    prescription TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);
```

## 🔨 Build & Run

### Build JAR

```bash
mvn clean package
```

### Run CLI Demo

```bash
java -jar target/digital-health-backend-1.0.0.jar
```

### Run with Custom Config

```bash
java -Dpersistence=file -Ddata.directory=mydata -jar target/digital-health-backend-1.0.0.jar
```

### Generate Javadoc

```bash
mvn javadoc:javadoc
```

Documentation will be in `target/site/apidocs/`.

## 📝 Sample Output Files

### Example: `export/history_P1001.txt`

```
========================================
    PATIENT HEALTH HISTORY REPORT
========================================

PATIENT INFORMATION:
--------------------
Patient ID: P1001
Name: Lea D.
Age: 19 years
Gender: F
Contact: 9999999999

MEDICAL HISTORY:
----------------
Total Visits: 1

VISIT #1
--------
Record ID: R3001
Date: 2025-11-15 10:30
Doctor: Dr. Sarah Johnson (ID: D001)
Symptoms: Fever, headache
Diagnosis: Common cold
Prescription: Rest and fluids, Paracetamol 500mg

========================================
        END OF REPORT
========================================
```

## ✅ Acceptance Criteria Checklist

- [x] **Patient lifecycle**: Register, update, fetch with auto-generated IDs
- [x] **Health records**: Add records with all fields, list sorted by date
- [x] **Appointments**: Book, cancel, reschedule (atomic), query by patient/doctor/date
- [x] **Persistence**: File-based (default) and MySQL implementations
- [x] **Export**: Patient history to text files
- [x] **Architecture**: Layered (model, DAO, service, facade)
- [x] **DTO pattern**: Separate DTOs for GUI communication
- [x] **BackendFacade**: Single integration point, well-documented
- [x] **Thread safety**: Read-write locks, synchronized booking
- [x] **Unit tests**: Service layer coverage, persistence, concurrency
- [x] **Documentation**: README, Javadoc, SQL schema, integration example
- [x] **Build system**: Maven with proper dependencies
- [x] **CLI demo**: Full scenario demonstration

## 👥 Authors

Digital Health Repository Backend Development Team

## 📄 License

This project is provided for educational and demonstration purposes.

---

**For GUI integration support, refer to the [GUI Integration](#gui-integration) section above.**
