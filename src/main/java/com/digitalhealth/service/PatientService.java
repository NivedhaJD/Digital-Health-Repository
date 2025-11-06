package com.digitalhealth.service;

import com.digitalhealth.dao.PatientDao;
import com.digitalhealth.dto.PatientDTO;
import com.digitalhealth.exception.DuplicateEntityException;
import com.digitalhealth.exception.EntityNotFoundException;
import com.digitalhealth.exception.ValidationException;
import com.digitalhealth.model.Patient;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for Patient operations.
 * Handles business logic, validation, and coordinates with DAO layer.
 */
public class PatientService {
    private final PatientDao patientDao;
    private final AtomicInteger idCounter = new AtomicInteger(1000);

    public PatientService(PatientDao patientDao) {
        this.patientDao = patientDao;
        initializeIdCounter();
    }

    private void initializeIdCounter() {
        Map<String, Patient> patients = patientDao.loadAll();
        if (!patients.isEmpty()) {
            int maxId = patients.keySet().stream()
                    .map(id -> id.replace("P", ""))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(1000);
            idCounter.set(maxId + 1);
        }
    }

    /**
     * Register a new patient with auto-generated ID.
     * 
     * @param dto PatientDTO containing patient information
     * @return Generated patient ID
     * @throws ValidationException if input validation fails
     */
    public String registerPatient(PatientDTO dto) throws ValidationException {
        validatePatientDTO(dto);

        String patientId = "P" + idCounter.getAndIncrement();
        Patient patient = new Patient(patientId, dto.getName(), dto.getAge(), 
                                     dto.getGender(), dto.getContact());

        patientDao.save(patient);
        return patientId;
    }

    /**
     * Get patient by ID.
     * 
     * @param patientId Patient ID
     * @return PatientDTO
     * @throws EntityNotFoundException if patient not found
     */
    public PatientDTO getPatient(String patientId) throws EntityNotFoundException {
        Patient patient = patientDao.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId));
        return toDTO(patient);
    }

    /**
     * Update patient details.
     * 
     * @param dto PatientDTO with updated information (must include patientId)
     * @throws EntityNotFoundException if patient not found
     * @throws ValidationException if validation fails
     */
    public void updatePatient(PatientDTO dto) throws EntityNotFoundException, ValidationException {
        if (dto.getPatientId() == null || dto.getPatientId().isEmpty()) {
            throw new ValidationException("Patient ID is required for update");
        }

        Patient patient = patientDao.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + dto.getPatientId()));

        validatePatientDTO(dto);

        patient.setName(dto.getName());
        patient.setAge(dto.getAge());
        patient.setGender(dto.getGender());
        patient.setContact(dto.getContact());

        patientDao.save(patient);
    }

    /**
     * List all patients.
     * 
     * @return List of PatientDTOs
     */
    public List<PatientDTO> listAllPatients() {
        Map<String, Patient> patients = patientDao.loadAll();
        List<PatientDTO> result = new ArrayList<>();
        for (Patient patient : patients.values()) {
            result.add(toDTO(patient));
        }
        return result;
    }

    /**
     * Check if a patient exists.
     */
    public boolean patientExists(String patientId) {
        return patientDao.findById(patientId).isPresent();
    }

    /**
     * Get the Patient entity (for internal use by other services).
     */
    Patient getPatientEntity(String patientId) throws EntityNotFoundException {
        return patientDao.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId));
    }

    /**
     * Save patient entity (for internal use).
     */
    void savePatientEntity(Patient patient) {
        patientDao.save(patient);
    }

    private void validatePatientDTO(PatientDTO dto) throws ValidationException {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Patient name is required");
        }
        if (dto.getAge() <= 0 || dto.getAge() > 150) {
            throw new ValidationException("Invalid age: must be between 1 and 150");
        }
        if (dto.getGender() == null || dto.getGender().trim().isEmpty()) {
            throw new ValidationException("Gender is required");
        }
        if (dto.getContact() == null || dto.getContact().trim().isEmpty()) {
            throw new ValidationException("Contact is required");
        }
        // Basic phone validation (10 digits)
        if (!dto.getContact().matches("\\d{10}")) {
            throw new ValidationException("Contact must be a 10-digit phone number");
        }
    }

    /**
     * Delete a patient by ID.
     * 
     * @param patientId ID of the patient to delete
     * @throws EntityNotFoundException if patient doesn't exist
     */
    public void deletePatient(String patientId) throws EntityNotFoundException {
        if (!patientDao.exists(patientId)) {
            throw new EntityNotFoundException("Patient not found with ID: " + patientId);
        }
        patientDao.delete(patientId);
    }

    private PatientDTO toDTO(Patient patient) {
        return new PatientDTO(
                patient.getPatientId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getContact()
        );
    }
}
