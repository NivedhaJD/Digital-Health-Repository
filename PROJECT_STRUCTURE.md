# Digital Health Repository - Project Structure

## 📁 Directory Organization

```
Digital-Health-Repository/
│
├── 📂 src/                          # Source code
│   ├── 📂 main/
│   │   ├── 📂 java/
│   │   │   └── 📂 com/
│   │   │       └── 📂 digitalhealth/
│   │   │           ├── 📂 api/              # REST API Server
│   │   │           │   └── ApiServer.java
│   │   │           │
│   │   │           ├── 📂 cli/              # Command Line Interface
│   │   │           │   └── CliApplication.java
│   │   │           │
│   │   │           ├── 📂 dao/              # Data Access Objects (Interfaces)
│   │   │           │   ├── AppointmentDao.java
│   │   │           │   ├── DoctorDao.java
│   │   │           │   ├── HealthRecordDao.java
│   │   │           │   ├── PatientDao.java
│   │   │           │   └── 📂 file/         # File-based Implementation
│   │   │           │       ├── FileAppointmentDao.java
│   │   │           │       ├── FileDoctorDao.java
│   │   │           │       ├── FileHealthRecordDao.java
│   │   │           │       └── FilePatientDao.java
│   │   │           │
│   │   │           ├── 📂 dto/              # Data Transfer Objects
│   │   │           │   ├── AppointmentDTO.java
│   │   │           │   ├── DoctorDTO.java
│   │   │           │   ├── HealthRecordDTO.java
│   │   │           │   └── PatientDTO.java
│   │   │           │
│   │   │           ├── 📂 exception/        # Custom Exceptions
│   │   │           │   ├── DuplicateEntityException.java
│   │   │           │   ├── EntityNotFoundException.java
│   │   │           │   ├── SlotUnavailableException.java
│   │   │           │   └── ValidationException.java
│   │   │           │
│   │   │           ├── 📂 facade/           # Facade Pattern Implementation
│   │   │           │   ├── BackendFacade.java
│   │   │           │   └── BackendFactory.java
│   │   │           │
│   │   │           ├── 📂 model/            # Domain Models
│   │   │           │   ├── Appointment.java
│   │   │           │   ├── AppointmentStatus.java
│   │   │           │   ├── Doctor.java
│   │   │           │   ├── HealthRecord.java
│   │   │           │   └── Patient.java
│   │   │           │
│   │   │           ├── 📂 service/          # Business Logic Layer
│   │   │           │   ├── AppointmentService.java
│   │   │           │   ├── DoctorService.java
│   │   │           │   ├── ExportService.java
│   │   │           │   ├── HealthRecordService.java
│   │   │           │   └── PatientService.java
│   │   │           │
│   │   │           └── 📂 util/             # Utility Classes
│   │   │               └── DtoMapper.java
│   │   │
│   │   └── 📂 resources/
│   │       └── application.properties       # Application Configuration
│   │
│   └── 📂 test/                             # Test Code
│       └── 📂 java/
│           └── 📂 com/
│               └── 📂 digitalhealth/
│                   └── 📂 service/
│                       ├── AppointmentServiceTest.java
│                       ├── HealthRecordServiceTest.java
│                       └── PatientServiceTest.java
│
├── 📂 frontend/                     # Frontend Application
│   ├── 📂 html/                     # HTML Pages
│   │   ├── index.html               # Landing page
│   │   ├── admin.html               # Admin interface
│   │   ├── doctor.html              # Doctor interface
│   │   ├── patient.html             # Patient interface
│   │   ├── login.html               # Login page
│   │   └── test-api.html            # API testing page
│   │
│   ├── 📂 css/                      # Stylesheets
│   │   └── style.css                # Main stylesheet
│   │
│   └── 📂 js/                       # JavaScript Files
│       ├── script.js                # Main application logic
│       └── script-api-example.js    # API usage examples
│
├── 📂 docs/                         # Documentation
│   ├── BACKEND_README.md            # Backend documentation
│   ├── FRONTEND_BACKEND_CONNECTION_GUIDE.md
│   ├── INTEGRATION_GUIDE.md
│   ├── PROJECT_SUMMARY.md
│   ├── QUICK_START.md
│   └── SYSTEM_READY.md
│
├── 📂 data/                         # Data Storage (File-based)
│   ├── patients.dat                 # Patient records
│   ├── doctors.dat                  # Doctor records
│   ├── appointments.dat             # Appointment records
│   └── records.dat                  # Health records
│
├── 📂 export/                       # Exported Data
│   └── history_P1001_sample.txt     # Sample export
│
├── 📂 target/                       # Maven Build Output
│   └── classes/                     # Compiled classes
│
├── pom.xml                          # Maven Project Configuration
├── README.md                        # Main README
├── PROJECT_STRUCTURE.md             # This file
├── application.properties           # Configuration file
└── .gitignore                       # Git ignore rules

```

## 🏗️ Architecture Layers

### 1. Presentation Layer
- **Location**: `frontend/`
- **Components**: HTML, CSS, JavaScript
- **Purpose**: User interface and client-side logic

### 2. API Layer
- **Location**: `src/main/java/com/digitalhealth/api/`
- **Components**: `ApiServer.java`
- **Purpose**: HTTP REST API endpoints

### 3. Service Layer
- **Location**: `src/main/java/com/digitalhealth/service/`
- **Components**: Business logic services
- **Purpose**: Core application logic and workflows

### 4. DAO Layer
- **Location**: `src/main/java/com/digitalhealth/dao/`
- **Components**: Data access interfaces and implementations
- **Purpose**: Abstract data persistence operations

### 5. Persistence Layer
- **Location**: `data/`
- **Components**: `.dat` files for data storage
- **Purpose**: File-based data persistence

## 🔑 Key Components

### Backend Core

| Component | Location | Description |
|-----------|----------|-------------|
| API Server | `api/ApiServer.java` | HTTP server handling REST requests |
| CLI App | `cli/CliApplication.java` | Interactive command-line interface |
| Facade | `facade/BackendFacade.java` | Unified backend interface |
| Factory | `facade/BackendFactory.java` | Creates appropriate DAO implementations |

### Data Access

| Component | Type | Location |
|-----------|------|----------|
| PatientDao | Interface | `dao/PatientDao.java` |
| DoctorDao | Interface | `dao/DoctorDao.java` |
| AppointmentDao | Interface | `dao/AppointmentDao.java` |
| HealthRecordDao | Interface | `dao/HealthRecordDao.java` |
| File Implementations | Concrete | `dao/file/*.java` |

### Business Logic

| Service | Purpose |
|---------|---------|
| PatientService | Patient CRUD operations |
| DoctorService | Doctor management and availability |
| AppointmentService | Appointment booking and management |
| HealthRecordService | Medical record management |
| ExportService | Data export functionality |

### Frontend Pages

| Page | Purpose | URL |
|------|---------|-----|
| index.html | Landing page | `/` |
| admin.html | Administrative tasks | `/admin.html` |
| doctor.html | Doctor interface | `/doctor.html` |
| patient.html | Patient portal | `/patient.html` |
| login.html | User authentication | `/login.html` |
| test-api.html | API testing | `/test-api.html` |

## 📦 Build Artifacts

```
target/
├── classes/                         # Compiled .class files
│   └── com/digitalhealth/...
├── test-classes/                    # Compiled test classes
└── digital-health-backend-1.0.0.jar # Executable JAR
```

## 🗂️ Data Files

All data is stored in the `data/` directory using file-based persistence:

- `patients.dat` - Patient records
- `doctors.dat` - Doctor profiles and schedules
- `appointments.dat` - Appointment bookings
- `records.dat` - Health/medical records

## 📝 Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven project configuration and dependencies |
| `application.properties` | Runtime application settings |
| `.gitignore` | Git version control exclusions |

## 🧪 Testing

Test files are located in `src/test/java/com/digitalhealth/service/`:
- Unit tests for service layer components
- JUnit 4 framework
- Run with: `mvn test`

## 🚀 Entry Points

1. **API Server**: `com.digitalhealth.api.ApiServer`
   - Starts HTTP server on port 8080
   - Serves REST API endpoints

2. **CLI Application**: `com.digitalhealth.cli.CliApplication`
   - Interactive menu-driven interface
   - Direct backend access

## 📊 Data Flow

```
User Interface (HTML/JS)
        ↓
   API Server (HTTP)
        ↓
   Service Layer (Business Logic)
        ↓
   DAO Layer (Data Access)
        ↓
File Storage (.dat files)
```

## 🛠️ Development Guidelines

1. **Source Code**: Place all Java code under `src/main/java/com/digitalhealth/`
2. **Tests**: Place all tests under `src/test/java/com/digitalhealth/`
3. **Frontend**: Keep HTML, CSS, and JS organized in `frontend/` subdirectories
4. **Documentation**: Add documentation to `docs/` directory
5. **Data**: Never commit `data/*.dat` files (excluded in `.gitignore`)

## 📚 Additional Resources

For more detailed information, see:
- `docs/BACKEND_README.md` - Backend architecture details
- `docs/INTEGRATION_GUIDE.md` - Frontend-backend integration
- `docs/QUICK_START.md` - Quick start guide
- `README.md` - Project overview
