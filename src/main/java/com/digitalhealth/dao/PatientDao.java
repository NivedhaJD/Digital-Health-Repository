package com.digitalhealth.dao;

import com.digitalhealth.model.Patient;

import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for Patient entities.
 */
public interface PatientDao {
    /**
     * Save all patients to storage.
     * @param patients Map of patientId to Patient
     */
    void saveAll(Map<String, Patient> patients);

    /**
     * Load all patients from storage.
     * @return Map of patientId to Patient
     */
    Map<String, Patient> loadAll();

    /**
     * Find a patient by ID.
     * @param id Patient ID
     * @return Optional containing the patient if found
     */
    Optional<Patient> findById(String id);

    /**
     * Save or update a single patient.
     * @param patient Patient to save
     */
    void save(Patient patient);

    /**
     * Check if a patient exists.
     * @param patientId Patient ID
     * @return true if exists
     */
    boolean exists(String patientId);

    /**
     * Delete a patient by ID.
     * @param patientId Patient ID
     */
    void delete(String patientId);
}
