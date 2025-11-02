package com.digitalhealth.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a doctor in the system.
 */
public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    private String doctorId;
    private String name;
    private String specialty;
    private List<LocalDateTime> availableSlots;

    public Doctor() {
        this.availableSlots = new ArrayList<>();
    }

    public Doctor(String doctorId, String name, String specialty) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.availableSlots = new ArrayList<>();
    }

    public Doctor(String doctorId, String name, String specialty, List<LocalDateTime> availableSlots) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.availableSlots = availableSlots != null ? new ArrayList<>(availableSlots) : new ArrayList<>();
    }

    // Getters and setters
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public List<LocalDateTime> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<LocalDateTime> availableSlots) {
        this.availableSlots = availableSlots;
    }

    /**
     * Check if a specific slot is available.
     */
    public boolean hasSlot(LocalDateTime dateTime) {
        return availableSlots.contains(dateTime);
    }

    /**
     * Remove a slot from availability.
     */
    public boolean removeSlot(LocalDateTime dateTime) {
        return availableSlots.remove(dateTime);
    }

    /**
     * Add a slot to availability.
     */
    public void addSlot(LocalDateTime dateTime) {
        if (!availableSlots.contains(dateTime)) {
            availableSlots.add(dateTime);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(doctorId, doctor.doctorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId='" + doctorId + '\'' +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", availableSlots=" + availableSlots.size() +
                '}';
    }
}
