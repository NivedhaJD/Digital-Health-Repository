package com.digitalhealth.dao;

import com.digitalhealth.model.Doctor;

import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for Doctor entities.
 */
public interface DoctorDao {
    /**
     * Save all doctors to storage.
     * @param doctors Map of doctorId to Doctor
     */
    void saveAll(Map<String, Doctor> doctors);

    /**
     * Load all doctors from storage.
     * @return Map of doctorId to Doctor
     */
    Map<String, Doctor> loadAll();

    /**
     * Find a doctor by ID.
     * @param id Doctor ID
     * @return Optional containing the doctor if found
     */
    Optional<Doctor> findById(String id);

    /**
     * Save or update a single doctor.
     * @param doctor Doctor to save
     */
    void save(Doctor doctor);
}
