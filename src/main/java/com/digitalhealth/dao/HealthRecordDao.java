package com.digitalhealth.dao;

import com.digitalhealth.model.HealthRecord;

import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for HealthRecord entities.
 */
public interface HealthRecordDao {
    /**
     * Save all health records to storage.
     * @param records Map of recordId to HealthRecord
     */
    void saveAll(Map<String, HealthRecord> records);

    /**
     * Load all health records from storage.
     * @return Map of recordId to HealthRecord
     */
    Map<String, HealthRecord> loadAll();

    /**
     * Find a health record by ID.
     * @param id Record ID
     * @return Optional containing the record if found
     */
    Optional<HealthRecord> findById(String id);

    /**
     * Save or update a single health record.
     * @param record HealthRecord to save
     */
    void save(HealthRecord record);
}
