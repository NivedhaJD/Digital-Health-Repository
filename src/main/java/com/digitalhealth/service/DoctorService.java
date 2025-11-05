package com.digitalhealth.service;

import com.digitalhealth.dao.DoctorDao;
import com.digitalhealth.dto.DoctorDTO;
import com.digitalhealth.exception.EntityNotFoundException;
import com.digitalhealth.exception.ValidationException;
import com.digitalhealth.model.Doctor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service layer for Doctor operations.
 */
public class DoctorService {
    private final DoctorDao doctorDao;

    public DoctorService(DoctorDao doctorDao) {
        this.doctorDao = doctorDao;
    }

    /**
     * Register a new doctor (auto-generates ID).
     * 
     * @param dto DoctorDTO (without ID)
     * @return Generated Doctor ID
     * @throws ValidationException if validation fails
     */
    public String registerDoctor(DoctorDTO dto) throws ValidationException {
        validateDoctorRegistrationDTO(dto);

        // Auto-generate doctor ID
        String doctorId = generateDoctorId();
        
        Doctor doctor = new Doctor(doctorId, dto.getName(), dto.getSpecialty(), 
                                   dto.getContact(), dto.getEmail(), dto.getSchedule());
        if (dto.getAvailableSlots() != null) {
            doctor.setAvailableSlots(new ArrayList<>(dto.getAvailableSlots()));
        } else {
            // Generate default available slots for the next 30 days
            doctor.setAvailableSlots(generateDefaultSlots());
        }

        doctorDao.save(doctor);
        return doctor.getDoctorId();
    }

    /**
     * Add a new doctor.
     * 
     * @param dto DoctorDTO
     * @return Doctor ID
     * @throws ValidationException if validation fails
     */
    public String addDoctor(DoctorDTO dto) throws ValidationException {
        validateDoctorDTO(dto);

        Doctor doctor = new Doctor(dto.getDoctorId(), dto.getName(), dto.getSpecialty());
        if (dto.getAvailableSlots() != null) {
            doctor.setAvailableSlots(new ArrayList<>(dto.getAvailableSlots()));
        }

        doctorDao.save(doctor);
        return doctor.getDoctorId();
    }

    /**
     * Get doctor by ID.
     * 
     * @param doctorId Doctor ID
     * @return DoctorDTO
     * @throws EntityNotFoundException if doctor not found
     */
    public DoctorDTO getDoctor(String doctorId) throws EntityNotFoundException {
        Doctor doctor = doctorDao.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId));
        return toDTO(doctor);
    }

    /**
     * List all doctors.
     * 
     * @return List of DoctorDTOs
     */
    public List<DoctorDTO> listAllDoctors() {
        Map<String, Doctor> doctors = doctorDao.loadAll();
        List<DoctorDTO> result = new ArrayList<>();
        for (Doctor doctor : doctors.values()) {
            result.add(toDTO(doctor));
        }
        return result;
    }

    /**
     * Add available slot for a doctor.
     * 
     * @param doctorId Doctor ID
     * @param slot DateTime slot
     * @throws EntityNotFoundException if doctor not found
     */
    public void addAvailableSlot(String doctorId, LocalDateTime slot) throws EntityNotFoundException {
        Doctor doctor = getDoctorEntity(doctorId);
        doctor.addSlot(slot);
        doctorDao.save(doctor);
    }

    /**
     * Check if doctor exists.
     */
    public boolean doctorExists(String doctorId) {
        return doctorDao.findById(doctorId).isPresent();
    }

    /**
     * Get Doctor entity (for internal use by other services).
     */
    Doctor getDoctorEntity(String doctorId) throws EntityNotFoundException {
        return doctorDao.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId));
    }

    /**
     * Save doctor entity (for internal use).
     */
    void saveDoctorEntity(Doctor doctor) {
        doctorDao.save(doctor);
    }

    private void validateDoctorDTO(DoctorDTO dto) throws ValidationException {
        if (dto.getDoctorId() == null || dto.getDoctorId().trim().isEmpty()) {
            throw new ValidationException("Doctor ID is required");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Doctor name is required");
        }
        if (dto.getSpecialty() == null || dto.getSpecialty().trim().isEmpty()) {
            throw new ValidationException("Specialty is required");
        }
    }

    private void validateDoctorRegistrationDTO(DoctorDTO dto) throws ValidationException {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Doctor name is required");
        }
        if (dto.getSpecialty() == null || dto.getSpecialty().trim().isEmpty()) {
            throw new ValidationException("Specialty is required");
        }
    }

    private String generateDoctorId() {
        Map<String, Doctor> allDoctors = doctorDao.loadAll();
        int maxNum = 0;
        for (String id : allDoctors.keySet()) {
            if (id.startsWith("D")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException e) {
                    // Skip non-numeric IDs
                }
            }
        }
        return String.format("D%04d", maxNum + 1);
    }

    private DoctorDTO toDTO(Doctor doctor) {
        return new DoctorDTO(
                doctor.getDoctorId(),
                doctor.getName(),
                doctor.getSpecialty(),
                new ArrayList<>(doctor.getAvailableSlots())
        );
    }

    /**
     * Generate default available slots for a new doctor.
     * Creates slots for the next 30 days, 9 AM to 5 PM, every hour.
     */
    public List<LocalDateTime> generateDefaultSlotsForDoctor() {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Generate slots for the next 30 days
        for (int day = 0; day < 30; day++) {
            LocalDateTime date = now.plusDays(day);
            // For each day, create slots from 9 AM to 5 PM (every hour)
            for (int hour = 9; hour <= 17; hour++) {
                slots.add(date.withHour(hour).withMinute(0).withSecond(0).withNano(0));
            }
        }
        
        return slots;
    }

    /**
     * Private wrapper for internal use.
     */
    private List<LocalDateTime> generateDefaultSlots() {
        return generateDefaultSlotsForDoctor();
    }
}
