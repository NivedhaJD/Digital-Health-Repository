package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.HealthRecordDao;
import com.digitalhealth.model.HealthRecord;
import com.digitalhealth.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL implementation of HealthRecordDao.
 */
public class MySQLHealthRecordDao implements HealthRecordDao {
    
    @Override
    public void save(HealthRecord record) {
        String sql = "INSERT INTO health_records (record_id, patient_id, doctor_id, symptoms, diagnosis, prescription, record_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE patient_id=?, doctor_id=?, symptoms=?, diagnosis=?, prescription=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getRecordId());
            pstmt.setString(2, record.getPatientId());
            pstmt.setString(3, record.getDoctorId());
            pstmt.setString(4, record.getSymptoms());
            pstmt.setString(5, record.getDiagnosis());
            pstmt.setString(6, record.getPrescription());
            pstmt.setTimestamp(7, Timestamp.valueOf(record.getDate()));
            // For UPDATE part
            pstmt.setString(8, record.getPatientId());
            pstmt.setString(9, record.getDoctorId());
            pstmt.setString(10, record.getSymptoms());
            pstmt.setString(11, record.getDiagnosis());
            pstmt.setString(12, record.getPrescription());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving health record: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void saveAll(Map<String, HealthRecord> records) {
        for (HealthRecord record : records.values()) {
            save(record);
        }
    }
    
    @Override
    public Optional<HealthRecord> findById(String recordId) {
        String sql = "SELECT * FROM health_records WHERE record_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, recordId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Timestamp recordDate = rs.getTimestamp("record_date");
                HealthRecord record = new HealthRecord(
                    rs.getString("record_id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    recordDate != null ? recordDate.toLocalDateTime() : LocalDateTime.now(),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("prescription")
                );
                return Optional.of(record);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding health record: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, HealthRecord> loadAll() {
        Map<String, HealthRecord> records = new HashMap<>();
        String sql = "SELECT * FROM health_records";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Timestamp recordDate = rs.getTimestamp("record_date");
                HealthRecord record = new HealthRecord(
                    rs.getString("record_id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    recordDate != null ? recordDate.toLocalDateTime() : LocalDateTime.now(),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("prescription")
                );
                records.put(record.getRecordId(), record);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error loading all health records: " + e.getMessage(), e);
        }
        
        return records;
    }

    @Override
    public boolean exists(String recordId) {
        String sql = "SELECT COUNT(*) FROM health_records WHERE record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recordId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if health record exists: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void delete(String recordId) {
        String sql = "DELETE FROM health_records WHERE record_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, recordId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting health record: " + e.getMessage(), e);
        }
    }
}
