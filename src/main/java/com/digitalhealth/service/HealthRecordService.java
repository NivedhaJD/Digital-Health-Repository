package com.digitalhealth.service;

import com.digitalhealth.dao.HealthRecordDao;
import com.digitalhealth.dto.HealthRecordDTO;
import com.digitalhealth.exception.EntityNotFoundException;
import com.digitalhealth.exception.ValidationException;
import com.digitalhealth.model.HealthRecord;
import com.digitalhealth.model.Patient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service layer for Health Record operations.
 */
public class HealthRecordService {
    private final HealthRecordDao healthRecordDao;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AtomicInteger idCounter = new AtomicInteger(3000);

    public HealthRecordService(HealthRecordDao healthRecordDao,
                              PatientService patientService,
                              DoctorService doctorService) {
        this.healthRecordDao = healthRecordDao;
        this.patientService = patientService;
        this.doctorService = doctorService;
        initializeIdCounter();
    }

    private void initializeIdCounter() {
        Map<String, HealthRecord> records = healthRecordDao.loadAll();
        if (!records.isEmpty()) {
            int maxId = records.keySet().stream()
                    .map(id -> id.replace("R", ""))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(3000);
            idCounter.set(maxId + 1);
        }
    }

    /**
     * Add a health record to a patient.
     * 
     * @param dto HealthRecordDTO
     * @return Generated record ID
     * @throws EntityNotFoundException if patient or doctor not found
     * @throws ValidationException if validation fails
     */
    public String addHealthRecord(HealthRecordDTO dto) 
            throws EntityNotFoundException, ValidationException {
        
        validateHealthRecordDTO(dto);

        // Verify patient and doctor exist
        Patient patient = patientService.getPatientEntity(dto.getPatientId());
        if (!doctorService.doctorExists(dto.getDoctorId())) {
            throw new EntityNotFoundException("Doctor not found: " + dto.getDoctorId());
        }

        String recordId = "R" + idCounter.getAndIncrement();
        HealthRecord record = new HealthRecord(
                recordId,
                dto.getPatientId(),
                dto.getDoctorId(),
                dto.getDate() != null ? dto.getDate() : LocalDateTime.now(),
                dto.getSymptoms(),
                dto.getDiagnosis(),
                dto.getPrescription()
        );

        healthRecordDao.save(record);
        
        // Add to patient's records
        patient.getHealthRecords().add(record);
        patientService.savePatientEntity(patient);

        return recordId;
    }

    /**
     * Get health record by ID.
     */
    public HealthRecordDTO getHealthRecord(String recordId) throws EntityNotFoundException {
        HealthRecord record = healthRecordDao.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Health record not found: " + recordId));
        return toDTO(record);
    }

    /**
     * Get all health records for a patient, sorted by date.
     * 
     * @param patientId Patient ID
     * @return List of HealthRecordDTOs sorted by date
     */
    public List<HealthRecordDTO> getRecordsByPatient(String patientId) {
        Map<String, HealthRecord> records = healthRecordDao.loadAll();
        return records.values().stream()
                .filter(r -> r.getPatientId().equals(patientId))
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get records by doctor.
     */
    public List<HealthRecordDTO> getRecordsByDoctor(String doctorId) {
        Map<String, HealthRecord> records = healthRecordDao.loadAll();
        return records.values().stream()
                .filter(r -> r.getDoctorId().equals(doctorId))
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * List all health records.
     */
    public List<HealthRecordDTO> listAllRecords() {
        Map<String, HealthRecord> records = healthRecordDao.loadAll();
        return records.values().stream()
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateHealthRecordDTO(HealthRecordDTO dto) throws ValidationException {
        if (dto.getPatientId() == null || dto.getPatientId().trim().isEmpty()) {
            throw new ValidationException("Patient ID is required");
        }
        if (dto.getDoctorId() == null || dto.getDoctorId().trim().isEmpty()) {
            throw new ValidationException("Doctor ID is required");
        }
        if (dto.getSymptoms() == null || dto.getSymptoms().trim().isEmpty()) {
            throw new ValidationException("Symptoms are required");
        }
        if (dto.getDiagnosis() == null || dto.getDiagnosis().trim().isEmpty()) {
            throw new ValidationException("Diagnosis is required");
        }
    }

    private HealthRecordDTO toDTO(HealthRecord record) {
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
}
