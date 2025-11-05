package com.digitalhealth.service;

import com.digitalhealth.dao.AppointmentDao;
import com.digitalhealth.dto.AppointmentDTO;
import com.digitalhealth.exception.EntityNotFoundException;
import com.digitalhealth.exception.SlotUnavailableException;
import com.digitalhealth.exception.ValidationException;
import com.digitalhealth.model.Appointment;
import com.digitalhealth.model.AppointmentStatus;
import com.digitalhealth.model.Doctor;
import com.digitalhealth.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service layer for Appointment operations.
 * Handles booking, cancellation, rescheduling with thread-safe slot management.
 */
public class AppointmentService {
    private final AppointmentDao appointmentDao;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AtomicInteger idCounter = new AtomicInteger(5000);
    private final Object lockObject = new Object();

    public AppointmentService(AppointmentDao appointmentDao, 
                             PatientService patientService,
                             DoctorService doctorService) {
        this.appointmentDao = appointmentDao;
        this.patientService = patientService;
        this.doctorService = doctorService;
        initializeIdCounter();
    }

    private void initializeIdCounter() {
        Map<String, Appointment> appointments = appointmentDao.loadAll();
        if (!appointments.isEmpty()) {
            int maxId = appointments.keySet().stream()
                    .map(id -> id.replace("A", ""))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(5000);
            idCounter.set(maxId + 1);
        }
    }

    /**
     * Book an appointment atomically.
     * Checks slot availability and creates appointment in one transaction.
     * 
     * @param patientId Patient ID
     * @param doctorId Doctor ID
     * @param dateTime Appointment date and time
     * @return AppointmentDTO with generated appointment ID
     * @throws EntityNotFoundException if patient or doctor not found
     * @throws SlotUnavailableException if slot is not available
     */
    public AppointmentDTO bookAppointment(String patientId, String doctorId, LocalDateTime dateTime) 
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        
        validateBookingInput(patientId, doctorId, dateTime);

        synchronized (lockObject) {
            // Verify entities exist
            Patient patient = patientService.getPatientEntity(patientId);
            Doctor doctor = doctorService.getDoctorEntity(doctorId);

            // Check slot availability
            if (!doctor.hasSlot(dateTime)) {
                throw new SlotUnavailableException(
                    "Slot not available for doctor " + doctorId + " at " + dateTime);
            }

            // Create appointment
            String appointmentId = "A" + idCounter.getAndIncrement();
            Appointment appointment = new Appointment(
                appointmentId, patientId, doctorId, dateTime, AppointmentStatus.BOOKED);

            // Remove slot from doctor's availability
            doctor.removeSlot(dateTime);

            // Save changes
            appointmentDao.save(appointment);
            doctorService.saveDoctorEntity(doctor);
            
            // Add appointment to patient
            patient.getAppointments().add(appointment);
            patientService.savePatientEntity(patient);

            return toDTO(appointment);
        }
    }

    /**
     * Book an appointment with a reason for the visit.
     * 
     * @param patientId ID of the patient
     * @param doctorId ID of the doctor
     * @param dateTime Desired appointment time
     * @param reason Reason for the appointment
     * @return AppointmentDTO of the newly created appointment
     * @throws EntityNotFoundException if patient or doctor not found
     * @throws SlotUnavailableException if slot is not available
     */
    public AppointmentDTO bookAppointment(String patientId, String doctorId, LocalDateTime dateTime, String reason) 
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        
        validateBookingInput(patientId, doctorId, dateTime);

        synchronized (lockObject) {
            // Verify entities exist
            Patient patient = patientService.getPatientEntity(patientId);
            Doctor doctor = doctorService.getDoctorEntity(doctorId);

            // Auto-generate slots if doctor has none (for backward compatibility)
            if (doctor.getAvailableSlots() == null || doctor.getAvailableSlots().isEmpty()) {
                doctor.setAvailableSlots(doctorService.generateDefaultSlotsForDoctor());
                doctorService.saveDoctorEntity(doctor);
            }

            // Check slot availability
            if (!doctor.hasSlot(dateTime)) {
                throw new SlotUnavailableException(
                    "Slot not available for doctor " + doctorId + " at " + dateTime);
            }

            // Create appointment with reason
            String appointmentId = "A" + idCounter.getAndIncrement();
            Appointment appointment = new Appointment(
                appointmentId, patientId, doctorId, dateTime, AppointmentStatus.BOOKED, reason);

            // Remove slot from doctor's availability
            doctor.removeSlot(dateTime);

            // Save changes
            appointmentDao.save(appointment);
            doctorService.saveDoctorEntity(doctor);
            
            // Add appointment to patient
            patient.getAppointments().add(appointment);
            patientService.savePatientEntity(patient);

            return toDTO(appointment);
        }
    }

    /**
     * Cancel an appointment and restore the slot.
     * 
     * @param appointmentId Appointment ID
     * @return true if cancelled successfully
     * @throws EntityNotFoundException if appointment not found
     */
    public boolean cancelAppointment(String appointmentId) throws EntityNotFoundException {
        synchronized (lockObject) {
            Appointment appointment = appointmentDao.findById(appointmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));

            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                return false; // Already cancelled
            }

            // Update status
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentDao.save(appointment);

            // Restore slot to doctor
            Doctor doctor = doctorService.getDoctorEntity(appointment.getDoctorId());
            doctor.addSlot(appointment.getDateTime());
            doctorService.saveDoctorEntity(doctor);

            return true;
        }
    }

    /**
     * Reschedule an appointment atomically.
     * 
     * @param appointmentId Appointment ID
     * @param newDateTime New date and time
     * @return Updated AppointmentDTO
     * @throws EntityNotFoundException if appointment not found
     * @throws SlotUnavailableException if new slot not available
     */
    public AppointmentDTO rescheduleAppointment(String appointmentId, LocalDateTime newDateTime) 
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        
        if (newDateTime == null) {
            throw new ValidationException("New date time is required");
        }

        synchronized (lockObject) {
            Appointment appointment = appointmentDao.findById(appointmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));

            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                throw new ValidationException("Cannot reschedule a cancelled appointment");
            }

            Doctor doctor = doctorService.getDoctorEntity(appointment.getDoctorId());

            // Check if new slot is available
            if (!doctor.hasSlot(newDateTime)) {
                throw new SlotUnavailableException(
                    "New slot not available for doctor " + appointment.getDoctorId() + " at " + newDateTime);
            }

            LocalDateTime oldDateTime = appointment.getDateTime();

            // Update appointment
            appointment.setDateTime(newDateTime);
            appointmentDao.save(appointment);

            // Restore old slot and remove new slot
            doctor.addSlot(oldDateTime);
            doctor.removeSlot(newDateTime);
            doctorService.saveDoctorEntity(doctor);

            return toDTO(appointment);
        }
    }

    /**
     * Get appointment by ID.
     */
    public AppointmentDTO getAppointment(String appointmentId) throws EntityNotFoundException {
        Appointment appointment = appointmentDao.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));
        return toDTO(appointment);
    }

    /**
     * Get appointments for a patient.
     */
    public List<AppointmentDTO> getAppointmentsByPatient(String patientId) {
        Map<String, Appointment> appointments = appointmentDao.loadAll();
        return appointments.values().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments for a doctor.
     */
    public List<AppointmentDTO> getAppointmentsByDoctor(String doctorId) {
        Map<String, Appointment> appointments = appointmentDao.loadAll();
        return appointments.values().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by date.
     */
    public List<AppointmentDTO> getAppointmentsByDate(LocalDate date) {
        Map<String, Appointment> appointments = appointmentDao.loadAll();
        return appointments.values().stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(date))
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * List all appointments.
     */
    public List<AppointmentDTO> listAllAppointments() {
        Map<String, Appointment> appointments = appointmentDao.loadAll();
        return appointments.values().stream()
                .sorted()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mark appointment as completed.
     */
    public void completeAppointment(String appointmentId) throws EntityNotFoundException {
        Appointment appointment = appointmentDao.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentDao.save(appointment);
    }

    private void validateBookingInput(String patientId, String doctorId, LocalDateTime dateTime) 
            throws ValidationException {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new ValidationException("Patient ID is required");
        }
        if (doctorId == null || doctorId.trim().isEmpty()) {
            throw new ValidationException("Doctor ID is required");
        }
        if (dateTime == null) {
            throw new ValidationException("Date time is required");
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot book appointment in the past");
        }
    }

    private AppointmentDTO toDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getAppointmentId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime(),
                appointment.getStatus()
        );
    }
}
