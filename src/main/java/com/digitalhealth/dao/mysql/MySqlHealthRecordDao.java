package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.HealthRecordDao;
import com.digitalhealth.model.HealthRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MySQL implementation of HealthRecordDao.
 * Handles health_records table.
 */
public class MySqlHealthRecordDao implements HealthRecordDao {
    private final DatabaseConnection dbConnection;

    public MySqlHealthRecordDao(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void saveAll(Map<String, HealthRecord> records) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Clear existing data
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM health_records");
                }
                
                // Insert all records
                String sql = "INSERT INTO health_records (id, patient_id, symptoms, diagnosis, prescription, record_date, doctor_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (HealthRecord record : records.values()) {
                        pstmt.setString(1, record.getRecordId());
                        pstmt.setString(2, record.getPatientId());
                        pstmt.setString(3, record.getSymptoms());
                        pstmt.setString(4, record.getDiagnosis());
                        pstmt.setString(5, record.getPrescription());
                        pstmt.setTimestamp(6, Timestamp.valueOf(record.getRecordDate()));
                        pstmt.setString(7, record.getDoctorId());
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
            throw new RuntimeException("Failed to save health records", e);
        }
    }

    @Override
    public Map<String, HealthRecord> loadAll() {
        Map<String, HealthRecord> records = new HashMap<>();
        String sql = "SELECT id, patient_id, symptoms, diagnosis, prescription, record_date, doctor_id FROM health_records";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                HealthRecord record = new HealthRecord(
                    rs.getString("id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    rs.getTimestamp("record_date").toLocalDateTime(),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("prescription")
                );
                records.put(record.getRecordId(), record);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load health records", e);
        }
        
        return records;
    }

    @Override
    public Optional<HealthRecord> findById(String id) {
        String sql = "SELECT id, patient_id, symptoms, diagnosis, prescription, record_date, doctor_id FROM health_records WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    HealthRecord record = new HealthRecord(
                        rs.getString("id"),
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("record_date").toLocalDateTime(),
                        rs.getString("symptoms"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription")
                    );
                    return Optional.of(record);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find health record", e);
        }
        
        return Optional.empty();
    }

    @Override
    public void save(HealthRecord record) {
        String sql = "INSERT INTO health_records (id, patient_id, symptoms, diagnosis, prescription, record_date, doctor_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE patient_id=?, symptoms=?, diagnosis=?, prescription=?, record_date=?, doctor_id=?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getRecordId());
            pstmt.setString(2, record.getPatientId());
            pstmt.setString(3, record.getSymptoms());
            pstmt.setString(4, record.getDiagnosis());
            pstmt.setString(5, record.getPrescription());
            pstmt.setTimestamp(6, Timestamp.valueOf(record.getRecordDate()));
            pstmt.setString(7, record.getDoctorId());
            // For update clause
            pstmt.setString(8, record.getPatientId());
            pstmt.setString(9, record.getSymptoms());
            pstmt.setString(10, record.getDiagnosis());
            pstmt.setString(11, record.getPrescription());
            pstmt.setTimestamp(12, Timestamp.valueOf(record.getRecordDate()));
            pstmt.setString(13, record.getDoctorId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save health record", e);
        }
    }
}
