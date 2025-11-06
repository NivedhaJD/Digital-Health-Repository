package com.digitalhealth.dto;

import com.digitalhealth.model.AppointmentStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Appointment information.
 */
public class AppointmentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private String reason;

    public AppointmentDTO() {
    }

    public AppointmentDTO(String patientId, String doctorId, LocalDateTime dateTime) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.status = AppointmentStatus.BOOKED;
    }

    public AppointmentDTO(String appointmentId, String patientId, String doctorId, 
                         LocalDateTime dateTime, AppointmentStatus status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.status = status;
    }

    public AppointmentDTO(String appointmentId, String patientId, String doctorId, 
                         LocalDateTime dateTime, AppointmentStatus status, String reason) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.status = status;
        this.reason = reason;
    }

    // Getters and setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "AppointmentDTO{" +
                "appointmentId='" + appointmentId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", dateTime=" + dateTime +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
