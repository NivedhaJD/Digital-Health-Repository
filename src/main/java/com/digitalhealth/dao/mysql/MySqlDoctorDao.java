package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.DoctorDao;
import com.digitalhealth.model.Doctor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MySQL implementation of DoctorDao.
 * Handles doctors table and doctor_slots table.
 */
public class MySqlDoctorDao implements DoctorDao {
    private final DatabaseConnection dbConnection;

    public MySqlDoctorDao(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void saveAll(Map<String, Doctor> doctors) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Clear existing data
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM doctor_slots");
                    stmt.executeUpdate("DELETE FROM doctors");
                }
                
                // Insert all doctors
                String doctorSql = "INSERT INTO doctors (id, name, specialization) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(doctorSql)) {
                    for (Doctor doctor : doctors.values()) {
                        pstmt.setString(1, doctor.getDoctorId());
                        pstmt.setString(2, doctor.getName());
                        pstmt.setString(3, doctor.getSpecialty());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                
                // Insert all available slots
                String slotSql = "INSERT INTO doctor_slots (doctor_id, slot_datetime, is_available) VALUES (?, ?, TRUE)";
                try (PreparedStatement pstmt = conn.prepareStatement(slotSql)) {
                    for (Doctor doctor : doctors.values()) {
                        for (LocalDateTime slot : doctor.getAvailableSlots()) {
                            pstmt.setString(1, doctor.getDoctorId());
                            pstmt.setTimestamp(2, Timestamp.valueOf(slot));
                            pstmt.addBatch();
                        }
                    }
                    pstmt.executeBatch();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save doctors", e);
        }
    }

    @Override
    public Map<String, Doctor> loadAll() {
        Map<String, Doctor> doctors = new HashMap<>();
        
        // Load doctors
        String doctorSql = "SELECT id, name, specialization FROM doctors";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(doctorSql)) {
            
            while (rs.next()) {
                Doctor doctor = new Doctor(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("specialization")
                );
                doctors.put(doctor.getDoctorId(), doctor);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctors", e);
        }
        
        // Load available slots for each doctor
        String slotSql = "SELECT doctor_id, slot_datetime FROM doctor_slots WHERE is_available = TRUE ORDER BY slot_datetime";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(slotSql)) {
            
            while (rs.next()) {
                String doctorId = rs.getString("doctor_id");
                LocalDateTime slot = rs.getTimestamp("slot_datetime").toLocalDateTime();
                
                Doctor doctor = doctors.get(doctorId);
                if (doctor != null) {
                    doctor.addSlot(slot);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctor slots", e);
        }
        
        return doctors;
    }

    @Override
    public Optional<Doctor> findById(String id) {
        String doctorSql = "SELECT id, name, specialization FROM doctors WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(doctorSql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Doctor doctor = new Doctor(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("specialization")
                    );
                    
                    // Load slots for this doctor
                    String slotSql = "SELECT slot_datetime FROM doctor_slots WHERE doctor_id = ? AND is_available = TRUE ORDER BY slot_datetime";
                    try (PreparedStatement slotStmt = conn.prepareStatement(slotSql)) {
                        slotStmt.setString(1, id);
                        try (ResultSet slotRs = slotStmt.executeQuery()) {
                            while (slotRs.next()) {
                                doctor.addSlot(slotRs.getTimestamp("slot_datetime").toLocalDateTime());
                            }
                        }
                    }
                    
                    return Optional.of(doctor);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find doctor", e);
        }
        
        return Optional.empty();
    }

    @Override
    public void save(Doctor doctor) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Save or update doctor
                String doctorSql = "INSERT INTO doctors (id, name, specialization) " +
                                  "VALUES (?, ?, ?) " +
                                  "ON DUPLICATE KEY UPDATE name=?, specialization=?";
                try (PreparedStatement pstmt = conn.prepareStatement(doctorSql)) {
                    pstmt.setString(1, doctor.getDoctorId());
                    pstmt.setString(2, doctor.getName());
                    pstmt.setString(3, doctor.getSpecialty());
                    pstmt.setString(4, doctor.getName());
                    pstmt.setString(5, doctor.getSpecialty());
                    pstmt.executeUpdate();
                }
                
                // Delete existing slots and insert new ones
                String deleteSlotsSql = "DELETE FROM doctor_slots WHERE doctor_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSlotsSql)) {
                    pstmt.setString(1, doctor.getDoctorId());
                    pstmt.executeUpdate();
                }
                
                // Insert slots
                if (!doctor.getAvailableSlots().isEmpty()) {
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
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save doctor", e);
        }
    }
}
