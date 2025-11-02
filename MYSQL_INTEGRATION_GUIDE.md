# MySQL Integration Guide - Digital Health Repository

## 🎯 Overview

This guide will help you integrate the Java backend with your MySQL database (`hospital_db`). The backend now supports both **file-based storage** (default) and **MySQL database** storage.

---

## 📋 Prerequisites

### 1. MySQL Server
- MySQL 5.7+ or 8.0+ installed and running
- Default port: `3306`
- MySQL Workbench (recommended) or Command Line Client

### 2. Java Backend
- Java 11 or higher
- Maven 3.6+ 
- MySQL Connector/J dependency (already in `pom.xml`)

---

## 🗄️ Database Setup

### Step 1: Import the Database Schema

Two options provided:

#### Option A: Use Your Existing Database
If you already created `hospital_db` with your tables, you're all set! Just make sure the tables match this structure:

```sql
-- Tables required:
- patients (id, name, age, gender, contact)
- doctors (id, name, specialization)
- appointments (id, patient_id, doctor_id, date, time, status)
- health_records (id, patient_id, symptoms, diagnosis, prescription, record_date, doctor_id)
- doctor_slots (slot_id, doctor_id, slot_datetime, is_available)
```

#### Option B: Use the Provided Schema
Import the enhanced schema I created for you:

**File**: `hospital_db_schema.sql` (in your project root)

**Via MySQL Workbench:**
1. Open MySQL Workbench
2. Connect to your local MySQL instance
3. Go to **Server → Data Import**
4. Select **Import from Self-Contained File**
5. Browse and select: `hospital_db_schema.sql`
6. Under **Default Target Schema**, select: `hospital_db`
7. Click **Start Import**

**Via Command Line:**
```bash
mysql -u root -p < hospital_db_schema.sql
```
Enter password: `yourpassword`

### Step 2: Verify Database Creation

```sql
USE hospital_db;
SHOW TABLES;
```

You should see:
- `patients`
- `doctors`
- `appointments`
- `health_records`
- `doctor_slots`

---

## ⚙️ Backend Configuration

### Step 1: Update `application.properties`

The configuration file has been updated for you. Verify these settings:

**File**: `application.properties` (in your project root)

```properties
# Switch to MySQL persistence
persistence=mysql

# MySQL database configuration
db.url=jdbc:mysql://localhost:3306/hospital_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=yourpassword

# Application settings
app.name=Digital Health Repository
app.version=1.0.0
```

**Important**: 
- Change `db.password=yourpassword` to your actual MySQL root password
- If using a different user, update `db.username` as well
- If MySQL is on a different host/port, update `db.url`

### Step 2: Switch Back to File Storage (Optional)

To use file-based storage instead of MySQL:

```properties
# Use file-based persistence
persistence=file

# File storage location
data.directory=data
```

---

## 🔧 MySQL DAO Implementation Details

### Updated Files

The following MySQL DAO classes have been created/updated to match your `hospital_db` schema:

1. **MySqlPatientDao.java** ✅
   - Maps to `patients` table with `id` column
   - CRUD operations with prepared statements
   - Thread-safe with transactions

2. **MySqlDoctorDao.java** ✅ (NEW)
   - Maps to `doctors` table (id, name, specialization)
   - Handles `doctor_slots` for availability
   - Automatic slot management

3. **MySqlAppointmentDao.java** ✅ (NEW)
   - Maps to `appointments` table
   - Separate `date` and `time` columns (as per your schema)
   - Status enum mapping (BOOKED, CANCELLED, COMPLETED)

4. **MySqlHealthRecordDao.java** ✅ (NEW)
   - Maps to `health_records` table
   - Full CRUD with symptoms, diagnosis, prescription
   - Sorted by record_date

5. **DatabaseConnection.java** ✅
   - Reads configuration from `application.properties`
   - Provides JDBC connections
   - Auto-loads MySQL Connector/J driver

6. **BackendFactory.java** ✅ (UPDATED)
   - Now creates MySQL backend properly
   - Auto-detects persistence type from config
   - No more "not implemented" exception!

---

## 🚀 Running with MySQL

### Step 1: Rebuild the Project

```bash
mvn clean compile
```

### Step 2: Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.digitalhealth.cli.CliApplication"
```

Or build JAR and run:
```bash
mvn clean package -DskipTests
java -jar target/digital-health-backend-1.0.0.jar
```

### Step 3: Verify MySQL Connection

When the application starts, it will:
1. Read `application.properties`
2. See `persistence=mysql`
3. Connect to `hospital_db`
4. Load/save all data to MySQL instead of files

**Success indicators:**
- No connection errors
- Sample doctors loaded from database
- Can register patients (saved to `patients` table)
- Can view data retrieved from database

---

## 🧪 Testing MySQL Integration

### Test 1: Register a Patient
1. Run the application
2. Choose option `1. Register Patient`
3. Enter patient details
4. Check MySQL:
   ```sql
   USE hospital_db;
   SELECT * FROM patients;
   ```

### Test 2: View Doctors
1. Choose option `5. View All Doctors`
2. Should see doctors from database
3. Verify in MySQL:
   ```sql
   SELECT * FROM doctors;
   ```

### Test 3: Book Appointment
1. Register a patient first (e.g., gets ID P1000)
2. View available doctors and their slots
3. Book appointment (option 7)
4. Check MySQL:
   ```sql
   SELECT * FROM appointments;
   SELECT * FROM doctor_slots WHERE is_available = FALSE;
   ```

### Test 4: Health Records
1. Add health record for patient
2. Verify in MySQL:
   ```sql
   SELECT * FROM health_records;
   ```

---

## 🔍 Troubleshooting

### Issue 1: "Access denied for user 'root'@'localhost'"

**Solution**: Update password in `application.properties`

```properties
db.password=YOUR_ACTUAL_PASSWORD
```

### Issue 2: "Unknown database 'hospital_db'"

**Solution**: Create the database
```sql
CREATE DATABASE hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then import the schema as described in Step 1.

### Issue 3: "No suitable driver found"

**Solution**: Rebuild project to download MySQL Connector
```bash
mvn clean install
```

The MySQL connector is in your `pom.xml`:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### Issue 4: "Table doesn't exist"

**Solution**: Run the schema script
```bash
mysql -u root -p hospital_db < hospital_db_schema.sql
```

### Issue 5: Connection hangs or times out

**Solution**: Check if MySQL server is running
```bash
# Windows (Command Prompt as Administrator)
net start MySQL80

# Or check MySQL Workbench server status
```

---

## 📊 Database Schema Details

### Table: `patients`
```sql
id VARCHAR(20) PRIMARY KEY        -- Auto-generated (P1000, P1001, ...)
name VARCHAR(100) NOT NULL
age INT NOT NULL
gender VARCHAR(10) NOT NULL       -- M, F, Other
contact VARCHAR(20) NOT NULL
created_at TIMESTAMP
updated_at TIMESTAMP
```

### Table: `doctors`
```sql
id VARCHAR(20) PRIMARY KEY        -- D001, D002, ...
name VARCHAR(100) NOT NULL
specialization VARCHAR(100)       -- Cardiology, General Medicine, etc.
created_at TIMESTAMP
```

### Table: `doctor_slots`
```sql
slot_id INT AUTO_INCREMENT PRIMARY KEY
doctor_id VARCHAR(20)             -- Foreign key to doctors.id
slot_datetime DATETIME             -- Available appointment time
is_available BOOLEAN               -- TRUE = available, FALSE = booked
```

### Table: `appointments`
```sql
id VARCHAR(20) PRIMARY KEY        -- A001, A002, ...
patient_id VARCHAR(20)            -- Foreign key to patients.id
doctor_id VARCHAR(20)             -- Foreign key to doctors.id
date DATE                         -- Appointment date
time TIME                         -- Appointment time
status ENUM('BOOKED', 'CANCELLED', 'COMPLETED')
created_at TIMESTAMP
updated_at TIMESTAMP
```

### Table: `health_records`
```sql
id VARCHAR(20) PRIMARY KEY        -- R001, R002, ...
patient_id VARCHAR(20)            -- Foreign key to patients.id
symptoms TEXT NOT NULL
diagnosis TEXT NOT NULL
prescription TEXT
record_date DATETIME
doctor_id VARCHAR(20)             -- Foreign key to doctors.id
created_at TIMESTAMP
```

---

## 🎨 Integration with Your GUI

The GUI (HTML/JS files) connects through `BackendFacade` - the same API works for both file and MySQL storage!

### Example: Fetch Patient from JavaScript

```javascript
// Backend automatically uses MySQL when persistence=mysql in properties

// Register patient (calls BackendFacade.registerPatient)
fetch('/api/patients', {
    method: 'POST',
    body: JSON.stringify({
        name: 'John Doe',
        age: 35,
        gender: 'M',
        contact: '1234567890'
    })
})
.then(res => res.json())
.then(patient => {
    console.log('Patient ID:', patient.patientId); // e.g., P1002
});

// Get patient (calls BackendFacade.getPatient)
fetch('/api/patients/P1002')
    .then(res => res.json())
    .then(patient => {
        console.log(patient); // Data from MySQL!
    });
```

**No code changes needed in GUI** - just switch `persistence=mysql` in backend config!

---

## 📝 Quick Reference: Configuration Toggle

### Use MySQL Database
```properties
persistence=mysql
db.url=jdbc:mysql://localhost:3306/hospital_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=yourpassword
```

### Use File Storage
```properties
persistence=file
data.directory=data
```

---

## ✅ Checklist

- [ ] MySQL Server installed and running
- [ ] Database `hospital_db` created
- [ ] Schema imported (`hospital_db_schema.sql`)
- [ ] `application.properties` updated with correct password
- [ ] `persistence=mysql` set in properties
- [ ] Project rebuilt: `mvn clean compile`
- [ ] Application runs without connection errors
- [ ] Can register patient and see in MySQL
- [ ] Can book appointment and see in database

---

## 🎉 Success!

You've successfully connected your Java backend to MySQL! The system now:

✅ Stores all data in `hospital_db`  
✅ Uses proper indexes for fast queries  
✅ Maintains foreign key relationships  
✅ Supports concurrent access with transactions  
✅ Auto-generates IDs (P1000, D001, A001, R001...)  
✅ Handles all CRUD operations atomically  

**Next Steps:**
1. Test all features (register, view, book, records)
2. Integrate with your HTML/JS GUI
3. Deploy to production server
4. Optional: Set up connection pooling for better performance

Need help? Check the main `BACKEND_README.md` for API documentation!
