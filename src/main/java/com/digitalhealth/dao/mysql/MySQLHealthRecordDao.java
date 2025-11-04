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
            pstmt.setTimestamp(7, Timestamp.valueOf(record.getRecordDate()));
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
    public Optional<HealthRecord> findById(String recordId) {
        String sql = "SELECT * FROM health_records WHERE record_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, recordId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                HealthRecord record = new HealthRecord(
                    rs.getString("record_id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("prescription")
                );
                Timestamp recordDate = rs.getTimestamp("record_date");
                if (recordDate != null) {
                    record.setRecordDate(recordDate.toLocalDateTime());
                }
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
                HealthRecord record = new HealthRecord(
                    rs.getString("record_id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("prescription")
                );
                Timestamp recordDate = rs.getTimestamp("record_date");
                if (recordDate != null) {
                    record.setRecordDate(recordDate.toLocalDateTime());
                }
                records.put(record.getRecordId(), record);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error loading all health records: " + e.getMessage(), e);
        }
        
        return records;
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
