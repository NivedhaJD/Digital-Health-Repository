package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.PatientDao;
import com.digitalhealth.model.Patient;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL implementation of PatientDao using JDBC with prepared statements.
 */
public class MySqlPatientDao implements PatientDao {
    private final DatabaseConnection dbConnection;

    public MySqlPatientDao(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void saveAll(Map<String, Patient> patients) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Clear existing data
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM patients");
                }
                
                // Insert all patients
                String sql = "INSERT INTO patients (patient_id, name, age, gender, contact) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (Patient patient : patients.values()) {
                        pstmt.setString(1, patient.getPatientId());
                        pstmt.setString(2, patient.getName());
                        pstmt.setInt(3, patient.getAge());
                        pstmt.setString(4, patient.getGender());
                        pstmt.setString(5, patient.getContact());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patients", e);
        }
    }

    @Override
    public Map<String, Patient> loadAll() {
        Map<String, Patient> patients = new HashMap<>();
        String sql = "SELECT patient_id, name, age, gender, contact FROM patients";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getString("patient_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact")
                );
                patients.put(patient.getPatientId(), patient);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load patients", e);
        }
        
        return patients;
    }

    @Override
    public Optional<Patient> findById(String id) {
        String sql = "SELECT patient_id, name, age, gender, contact FROM patients WHERE patient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Patient patient = new Patient(
                        rs.getString("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("contact")
                    );
                    return Optional.of(patient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find patient", e);
        }
        
        return Optional.empty();
    }

    @Override
    public void save(Patient patient) {
        String sql = "INSERT INTO patients (patient_id, name, age, gender, contact) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name=?, age=?, gender=?, contact=?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patient.getPatientId());
            pstmt.setString(2, patient.getName());
            pstmt.setInt(3, patient.getAge());
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getContact());
            // For update clause
            pstmt.setString(6, patient.getName());
            pstmt.setInt(7, patient.getAge());
            pstmt.setString(8, patient.getGender());
            pstmt.setString(9, patient.getContact());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patient", e);
        }
    }
}
