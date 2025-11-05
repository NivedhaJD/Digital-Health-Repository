package com.digitalhealth.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Health Record information.
 */
public class HealthRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recordId;
    private String patientId;
    private String doctorId;
    private LocalDateTime date;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String prescription;

    public HealthRecordDTO() {
    }

    public HealthRecordDTO(String patientId, String doctorId, LocalDateTime date, 
                          String symptoms, String diagnosis, String prescription) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public HealthRecordDTO(String patientId, String doctorId, LocalDateTime date, 
                          String symptoms, String diagnosis, String treatment, String prescription) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
    }

    // Getters and setters
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    @Override
    public String toString() {
        return "HealthRecordDTO{" +
                "recordId='" + recordId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", date=" + date +
                ", diagnosis='" + diagnosis + '\'' +
                '}';
    }
}
