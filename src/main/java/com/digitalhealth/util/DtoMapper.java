package com.digitalhealth.util;

import com.digitalhealth.dto.*;
import com.digitalhealth.model.*;

/**
 * Utility class for mapping between domain objects and DTOs.
 * Centralizes DTO conversion logic.
 */
public class DtoMapper {

    /**
     * Convert Patient domain object to PatientDTO.
     */
    public static PatientDTO toDTO(Patient patient) {
        if (patient == null) return null;
        return new PatientDTO(
            patient.getPatientId(),
            patient.getName(),
            patient.getAge(),
            patient.getGender(),
            patient.getContact()
        );
    }

    /**
     * Convert PatientDTO to Patient domain object.
     * Note: Collections (healthRecords, appointments) are initialized empty.
     */
    public static Patient toEntity(PatientDTO dto) {
        if (dto == null) return null;
        return new Patient(
            dto.getPatientId(),
            dto.getName(),
            dto.getAge(),
            dto.getGender(),
            dto.getContact()
        );
    }

    /**
     * Convert Doctor domain object to DoctorDTO.
     */
    public static DoctorDTO toDTO(Doctor doctor) {
        if (doctor == null) return null;
        return new DoctorDTO(
            doctor.getDoctorId(),
            doctor.getName(),
            doctor.getSpecialty(),
            doctor.getAvailableSlots()
        );
    }

    /**
     * Convert DoctorDTO to Doctor domain object.
     */
    public static Doctor toEntity(DoctorDTO dto) {
        if (dto == null) return null;
        return new Doctor(
            dto.getDoctorId(),
            dto.getName(),
            dto.getSpecialty(),
            dto.getAvailableSlots()
        );
    }

    /**
     * Convert Appointment domain object to AppointmentDTO.
     */
    public static AppointmentDTO toDTO(Appointment appointment) {
        if (appointment == null) return null;
        return new AppointmentDTO(
            appointment.getAppointmentId(),
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getDateTime(),
            appointment.getStatus()
        );
    }

    /**
     * Convert AppointmentDTO to Appointment domain object.
     */
    public static Appointment toEntity(AppointmentDTO dto) {
        if (dto == null) return null;
        return new Appointment(
            dto.getAppointmentId(),
            dto.getPatientId(),
            dto.getDoctorId(),
            dto.getDateTime(),
            dto.getStatus()
        );
    }

    /**
     * Convert HealthRecord domain object to HealthRecordDTO.
     */
    public static HealthRecordDTO toDTO(HealthRecord record) {
        if (record == null) return null;
        HealthRecordDTO dto = new HealthRecordDTO(
            record.getPatientId(),
            record.getDoctorId(),
            record.getDate(),
            record.getSymptoms(),
            record.getDiagnosis(),
            record.getPrescription()
        );
        dto.setRecordId(record.getRecordId());
        return dto;
    }

    /**
     * Convert HealthRecordDTO to HealthRecord domain object.
     */
    public static HealthRecord toEntity(HealthRecordDTO dto) {
        if (dto == null) return null;
        return new HealthRecord(
            dto.getRecordId(),
            dto.getPatientId(),
            dto.getDoctorId(),
            dto.getDate(),
            dto.getSymptoms(),
            dto.getDiagnosis(),
            dto.getPrescription()
        );
    }
}
