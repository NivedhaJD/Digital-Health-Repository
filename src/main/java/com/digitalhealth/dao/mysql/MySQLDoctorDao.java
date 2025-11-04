package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.DoctorDao;
import com.digitalhealth.model.Doctor;
import com.digitalhealth.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL implementation of DoctorDao.
 */
public class MySQLDoctorDao implements DoctorDao {
    
    @Override
    public void save(Doctor doctor) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Save doctor basic info
            String doctorSql = "INSERT INTO doctors (doctor_id, name, specialty) " +
                              "VALUES (?, ?, ?) " +
                              "ON DUPLICATE KEY UPDATE name=?, specialty=?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(doctorSql)) {
                pstmt.setString(1, doctor.getDoctorId());
                pstmt.setString(2, doctor.getName());
                pstmt.setString(3, doctor.getSpecialty());
                pstmt.setString(4, doctor.getName());
                pstmt.setString(5, doctor.getSpecialty());
                pstmt.executeUpdate();
            }
            
            // Delete existing slots
            String deleteSql = "DELETE FROM doctor_slots WHERE doctor_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setString(1, doctor.getDoctorId());
                pstmt.executeUpdate();
            }
            
            // Save available slots
            if (doctor.getAvailableSlots() != null && !doctor.getAvailableSlots().isEmpty()) {
                String slotSql = "INSERT INTO doctor_slots (doctor_id, slot_datetime, is_available) VALUES (?, ?, TRUE)";
                try (PreparedStatement pstmt = conn.prepareStatement(slotSql)) {
                    for (LocalDateTime slot : doctor.getAvailableSlots()) {
                        pstmt.setString(1, doctor.getDoctorId());
                        pstmt.setTimestamp(2, Timestamp.valueOf(slot));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignore
                }
            }
            throw new RuntimeException("Error saving doctor: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public void saveAll(Map<String, Doctor> doctors) {
        for (Doctor doctor : doctors.values()) {
            save(doctor);
        }
    }
    
    @Override
    public Optional<Doctor> findById(String doctorId) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Doctor doctor = new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialty")
                );
                
                // Load available slots
                List<LocalDateTime> slots = loadSlotsForDoctor(doctorId);
                doctor.setAvailableSlots(slots);
                
                return Optional.of(doctor);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding doctor: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, Doctor> loadAll() {
        Map<String, Doctor> doctors = new HashMap<>();
        String sql = "SELECT * FROM doctors";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String doctorId = rs.getString("doctor_id");
                Doctor doctor = new Doctor(
                    doctorId,
                    rs.getString("name"),
                    rs.getString("specialty")
                );
                
                // Load available slots
                List<LocalDateTime> slots = loadSlotsForDoctor(doctorId);
                doctor.setAvailableSlots(slots);
                
                doctors.put(doctorId, doctor);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error loading all doctors: " + e.getMessage(), e);
        }
        
        return doctors;
    }
    
    /**
     * Delete a doctor by ID (MySQL-specific method).
     */
    public void delete(String doctorId) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting doctor: " + e.getMessage(), e);
        }
    }
    
    private List<LocalDateTime> loadSlotsForDoctor(String doctorId) {
        List<LocalDateTime> slots = new ArrayList<>();
        String sql = "SELECT slot_datetime FROM doctor_slots WHERE doctor_id = ? AND is_available = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(rs.getTimestamp("slot_datetime").toLocalDateTime());
            }
            
        } catch (SQLException e) {
            // Return empty list on error
        }
        
        return slots;
    }
}
