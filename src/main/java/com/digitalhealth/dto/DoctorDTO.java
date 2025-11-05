package com.digitalhealth.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Doctor information.
 */
public class DoctorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String doctorId;
    private String name;
    private String specialty;
    private String contact;
    private String email;
    private String schedule;
    private List<LocalDateTime> availableSlots;

    public DoctorDTO() {
        this.availableSlots = new ArrayList<>();
    }

    public DoctorDTO(String doctorId, String name, String specialty) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.availableSlots = new ArrayList<>();
    }

    public DoctorDTO(String doctorId, String name, String specialty, String contact, String email, String schedule) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.contact = contact;
        this.email = email;
        this.schedule = schedule;
        this.availableSlots = new ArrayList<>();
    }

    public DoctorDTO(String doctorId, String name, String specialty, List<LocalDateTime> availableSlots) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.availableSlots = availableSlots;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public List<LocalDateTime> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<LocalDateTime> availableSlots) {
        this.availableSlots = availableSlots;
    }

    @Override
    public String toString() {
        return "DoctorDTO{" +
                "doctorId='" + doctorId + '\'' +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", schedule='" + schedule + '\'' +
                ", slotsAvailable=" + (availableSlots != null ? availableSlots.size() : 0) +
                '}';
    }
}
