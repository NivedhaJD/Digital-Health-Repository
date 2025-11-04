package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.PatientDao;
import com.digitalhealth.model.Patient;
import com.digitalhealth.util.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL implementation of PatientDao.
 */
public class MySQLPatientDao implements PatientDao {
    
    @Override
    public void save(Patient patient) {
        String sql = "INSERT INTO patients (patient_id, name, age, gender, contact) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=?, age=?, gender=?, contact=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patient.getPatientId());
            pstmt.setString(2, patient.getName());
            pstmt.setInt(3, patient.getAge());
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getContact());
            // For UPDATE part
            pstmt.setString(6, patient.getName());
            pstmt.setInt(7, patient.getAge());
            pstmt.setString(8, patient.getGender());
            pstmt.setString(9, patient.getContact());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving patient: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Patient> findById(String patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
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
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding patient: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, Patient> loadAll() {
        Map<String, Patient> patients = new HashMap<>();
        String sql = "SELECT * FROM patients";
        
        try (Connection conn = DatabaseConnection.getConnection();
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
            throw new RuntimeException("Error loading all patients: " + e.getMessage(), e);
        }
        
        return patients;
    }
    
    @Override
    public void delete(String patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting patient: " + e.getMessage(), e);
        }
    }
}
