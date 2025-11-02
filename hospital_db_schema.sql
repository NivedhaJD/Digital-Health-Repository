-- Digital Health Repository Database Schema
-- MySQL Database: hospital_db
-- Compatible with your existing structure

CREATE DATABASE IF NOT EXISTS hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_db;

-- Patients table (matches your structure: id, name, age, gender, contact)
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age > 0 AND age <= 150),
    gender VARCHAR(10) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_contact (contact)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Patient information';

-- Doctors table (matches your structure: id, name, specialization)
CREATE TABLE IF NOT EXISTS doctors (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_specialization (specialization)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Doctor information';

-- Doctor available slots (for appointment booking system)
CREATE TABLE IF NOT EXISTS doctor_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id VARCHAR(20) NOT NULL,
    slot_datetime DATETIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_slot (doctor_id, slot_datetime),
    INDEX idx_doctor_slots (doctor_id, slot_datetime),
    INDEX idx_available_slots (is_available, slot_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Doctor available time slots';

-- Appointments table (matches your structure: id, patient_id, doctor_id, date, time, status)
CREATE TABLE IF NOT EXISTS appointments (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    time TIME NOT NULL,
    status ENUM('BOOKED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    INDEX idx_patient_appointments (patient_id, date),
    INDEX idx_doctor_appointments (doctor_id, date),
    INDEX idx_date_appointments (date, time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Patient appointments with doctors';

-- Health records table (matches your structure: id, patient_id, symptoms, diagnosis, prescription, record_date)
CREATE TABLE IF NOT EXISTS health_records (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    prescription TEXT,
    record_date DATETIME NOT NULL,
    doctor_id VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL,
    INDEX idx_patient_records (patient_id, record_date),
    INDEX idx_doctor_records (doctor_id, record_date),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Patient health records';

-- Sample data for testing
INSERT INTO doctors (id, name, specialization) VALUES
    ('D001', 'Dr. Sarah Johnson', 'Cardiology'),
    ('D002', 'Dr. Michael Chen', 'General Medicine'),
    ('D003', 'Dr. Emily Rodriguez', 'Pediatrics')
ON DUPLICATE KEY UPDATE name=VALUES(name), specialization=VALUES(specialization);

-- Sample available slots for next 7 days
INSERT INTO doctor_slots (doctor_id, slot_datetime, is_available) VALUES
    ('D001', '2025-11-15 10:00:00', TRUE),
    ('D001', '2025-11-15 14:00:00', TRUE),
    ('D001', '2025-11-16 09:00:00', TRUE),
    ('D001', '2025-11-17 10:00:00', TRUE),
    ('D002', '2025-11-15 11:00:00', TRUE),
    ('D002', '2025-11-15 15:00:00', TRUE),
    ('D002', '2025-11-16 10:00:00', TRUE),
    ('D002', '2025-11-17 11:00:00', TRUE),
    ('D003', '2025-11-15 13:00:00', TRUE),
    ('D003', '2025-11-16 11:00:00', TRUE),
    ('D003', '2025-11-17 13:00:00', TRUE)
ON DUPLICATE KEY UPDATE is_available=VALUES(is_available);

-- Sample patient (optional - for testing)
INSERT INTO patients (id, name, age, gender, contact) VALUES
    ('P1001', 'John Doe', 35, 'M', '1234567890')
ON DUPLICATE KEY UPDATE name=VALUES(name), age=VALUES(age);

-- Sample appointment (optional - for testing)
INSERT INTO appointments (id, patient_id, doctor_id, date, time, status) VALUES
    ('A001', 'P1001', 'D002', '2025-11-15', '11:00:00', 'BOOKED')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Sample health record (optional - for testing)
INSERT INTO health_records (id, patient_id, symptoms, diagnosis, prescription, record_date, doctor_id) VALUES
    ('R001', 'P1001', 'Fever, headache', 'Common cold', 'Paracetamol 500mg twice daily', '2025-11-01 10:30:00', 'D002')
ON DUPLICATE KEY UPDATE symptoms=VALUES(symptoms);
