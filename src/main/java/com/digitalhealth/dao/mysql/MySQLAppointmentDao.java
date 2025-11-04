package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.AppointmentDao;
import com.digitalhealth.model.Appointment;
import com.digitalhealth.model.AppointmentStatus;
import com.digitalhealth.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL implementation of AppointmentDao.
 */
public class MySQLAppointmentDao implements AppointmentDao {
    
    @Override
    public void save(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_id, patient_id, doctor_id, appointment_datetime, status) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE patient_id=?, doctor_id=?, appointment_datetime=?, status=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, appointment.getAppointmentId());
            pstmt.setString(2, appointment.getPatientId());
            pstmt.setString(3, appointment.getDoctorId());
            pstmt.setTimestamp(4, Timestamp.valueOf(appointment.getDateTime()));
            pstmt.setString(5, appointment.getStatus().name());
            // For UPDATE part
            pstmt.setString(6, appointment.getPatientId());
            pstmt.setString(7, appointment.getDoctorId());
            pstmt.setTimestamp(8, Timestamp.valueOf(appointment.getDateTime()));
            pstmt.setString(9, appointment.getStatus().name());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving appointment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void saveAll(Map<String, Appointment> appointments) {
        for (Appointment appointment : appointments.values()) {
            save(appointment);
        }
    }
    
    @Override
    public Optional<Appointment> findById(String appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Appointment appointment = new Appointment(
                    rs.getString("appointment_id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                    AppointmentStatus.valueOf(rs.getString("status"))
                );
                return Optional.of(appointment);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointment: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, Appointment> loadAll() {
        Map<String, Appointment> appointments = new HashMap<>();
        String sql = "SELECT * FROM appointments";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Appointment appointment = new Appointment(
                    rs.getString("appointment_id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                    AppointmentStatus.valueOf(rs.getString("status"))
                );
                appointments.put(appointment.getAppointmentId(), appointment);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error loading all appointments: " + e.getMessage(), e);
        }
        
        return appointments;
    }
    
    /**
     * Delete an appointment by ID (MySQL-specific method).
     */
    public void delete(String appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, appointmentId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting appointment: " + e.getMessage(), e);
        }
    }
}
