package com.digitalhealth.dao;

import com.digitalhealth.model.Appointment;

import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for Appointment entities.
 */
public interface AppointmentDao {
    /**
     * Save all appointments to storage.
     * @param appointments Map of appointmentId to Appointment
     */
    void saveAll(Map<String, Appointment> appointments);

    /**
     * Load all appointments from storage.
     * @return Map of appointmentId to Appointment
     */
    Map<String, Appointment> loadAll();

    /**
     * Find an appointment by ID.
     * @param id Appointment ID
     * @return Optional containing the appointment if found
     */
    Optional<Appointment> findById(String id);

    /**
     * Save or update a single appointment.
     * @param appointment Appointment to save
     */
    void save(Appointment appointment);
}
