package com.digitalhealth.dao.mysql;

import com.digitalhealth.dao.AppointmentDao;
import com.digitalhealth.model.Appointment;
import com.digitalhealth.model.AppointmentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * MySQL implementation of AppointmentDao.
 * Handles appointments table with separate date and time columns.
 */
public class MySqlAppointmentDao implements AppointmentDao {
    private final DatabaseConnection dbConnection;

    public MySqlAppointmentDao(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void saveAll(Map<String, Appointment> appointments) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Clear existing data
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM appointments");
                }
                
                // Insert all appointments
                String sql = "INSERT INTO appointments (id, patient_id, doctor_id, date, time, status) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (Appointment appointment : appointments.values()) {
                        LocalDateTime dateTime = appointment.getAppointmentDateTime();
                        pstmt.setString(1, appointment.getAppointmentId());
                        pstmt.setString(2, appointment.getPatientId());
                        pstmt.setString(3, appointment.getDoctorId());
                        pstmt.setDate(4, Date.valueOf(dateTime.toLocalDate()));
                        pstmt.setTime(5, Time.valueOf(dateTime.toLocalTime()));
                        pstmt.setString(6, appointment.getStatus().name());
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
            throw new RuntimeException("Failed to save appointments", e);
        }
    }

    @Override
    public Map<String, Appointment> loadAll() {
        Map<String, Appointment> appointments = new HashMap<>();
        String sql = "SELECT id, patient_id, doctor_id, date, time, status FROM appointments";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                LocalTime time = rs.getTime("time").toLocalTime();
                LocalDateTime dateTime = LocalDateTime.of(date, time);
                
                Appointment appointment = new Appointment(
                    rs.getString("id"),
                    rs.getString("patient_id"),
                    rs.getString("doctor_id"),
                    dateTime,
                    AppointmentStatus.valueOf(rs.getString("status"))
                );
                appointments.put(appointment.getAppointmentId(), appointment);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appointments", e);
        }
        
        return appointments;
    }

    @Override
    public Optional<Appointment> findById(String id) {
        String sql = "SELECT id, patient_id, doctor_id, date, time, status FROM appointments WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    LocalTime time = rs.getTime("time").toLocalTime();
                    LocalDateTime dateTime = LocalDateTime.of(date, time);
                    
                    Appointment appointment = new Appointment(
                        rs.getString("id"),
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        dateTime,
                        AppointmentStatus.valueOf(rs.getString("status"))
                    );
                    return Optional.of(appointment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find appointment", e);
        }
        
        return Optional.empty();
    }

    @Override
    public void save(Appointment appointment) {
        String sql = "INSERT INTO appointments (id, patient_id, doctor_id, date, time, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE patient_id=?, doctor_id=?, date=?, time=?, status=?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            LocalDateTime dateTime = appointment.getAppointmentDateTime();
            Date sqlDate = Date.valueOf(dateTime.toLocalDate());
            Time sqlTime = Time.valueOf(dateTime.toLocalTime());
            
            pstmt.setString(1, appointment.getAppointmentId());
            pstmt.setString(2, appointment.getPatientId());
            pstmt.setString(3, appointment.getDoctorId());
            pstmt.setDate(4, sqlDate);
            pstmt.setTime(5, sqlTime);
            pstmt.setString(6, appointment.getStatus().name());
            // For update clause
            pstmt.setString(7, appointment.getPatientId());
            pstmt.setString(8, appointment.getDoctorId());
            pstmt.setDate(9, sqlDate);
            pstmt.setTime(10, sqlTime);
            pstmt.setString(11, appointment.getStatus().name());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save appointment", e);
        }
    }
}
