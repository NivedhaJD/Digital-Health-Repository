-- Digital Health Repository Database Schema
-- MySQL 5.7+ or 8.0+

CREATE DATABASE IF NOT EXISTS digitalhealth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE digitalhealth;

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age > 0 AND age <= 150),
    gender VARCHAR(10) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_contact (contact)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Doctors table
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_specialty (specialty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Doctor available slots
CREATE TABLE IF NOT EXISTS doctor_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id VARCHAR(20) NOT NULL,
    slot_datetime DATETIME NOT NULL,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_slot (doctor_id, slot_datetime),
    INDEX idx_doctor_slots (doctor_id, slot_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Appointments table
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    appointment_datetime DATETIME NOT NULL,
    status ENUM('BOOKED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    INDEX idx_patient_appointments (patient_id, appointment_datetime),
    INDEX idx_doctor_appointments (doctor_id, appointment_datetime),
    INDEX idx_date_appointments (appointment_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Health records table
CREATE TABLE IF NOT EXISTS health_records (
    record_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    record_date DATETIME NOT NULL,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    prescription TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    INDEX idx_patient_records (patient_id, record_date),
    INDEX idx_doctor_records (doctor_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data (optional)
INSERT INTO doctors (doctor_id, name, specialty) VALUES
    ('D001', 'Dr. Sarah Johnson', 'Cardiology'),
    ('D002', 'Dr. Michael Chen', 'General Medicine'),
    ('D003', 'Dr. Emily Rodriguez', 'Pediatrics')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO doctor_slots (doctor_id, slot_datetime) VALUES
    ('D001', '2025-11-15 10:00:00'),
    ('D001', '2025-11-15 14:00:00'),
    ('D001', '2025-11-16 09:00:00'),
    ('D002', '2025-11-15 11:00:00'),
    ('D002', '2025-11-15 15:00:00'),
    ('D002', '2025-11-16 10:00:00'),
    ('D003', '2025-11-15 13:00:00'),
    ('D003', '2025-11-16 11:00:00')
ON DUPLICATE KEY UPDATE slot_datetime=VALUES(slot_datetime);
