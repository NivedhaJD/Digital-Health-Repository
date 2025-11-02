package com.digitalhealth.facade;

import com.digitalhealth.dto.*;
import com.digitalhealth.exception.*;
import com.digitalhealth.service.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Main facade for GUI integration.
 * Provides a single entry point for all backend operations.
 * All methods are thread-safe and throw checked exceptions for error handling.
 * 
 * Usage Example:
 * <pre>
 * BackendFacade facade = BackendFactory.createFileBackend();
 * String patientId = facade.registerPatient(new PatientDTO("John Doe", 30, "M", "1234567890"));
 * List&lt;DoctorDTO&gt; doctors = facade.listDoctors();
 * AppointmentDTO appt = facade.bookAppointment(patientId, doctors.get(0).getDoctorId(), 
 *                                               LocalDateTime.of(2025,11,15,10,0));
 * </pre>
 */
public class BackendFacade {
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final HealthRecordService healthRecordService;
    private final ExportService exportService;

    public BackendFacade(PatientService patientService,
                        DoctorService doctorService,
                        AppointmentService appointmentService,
                        HealthRecordService healthRecordService,
                        ExportService exportService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.healthRecordService = healthRecordService;
        this.exportService = exportService;
    }

    // ========== Patient Operations ==========

    /**
     * Register a new patient with auto-generated ID.
     * 
     * @param dto PatientDTO with name, age, gender, contact (patientId will be auto-generated)
     * @return Generated patient ID (format: P####)
     * @throws ValidationException if input validation fails (e.g., invalid age, missing name)
     */
    public String registerPatient(PatientDTO dto) throws ValidationException {
        return patientService.registerPatient(dto);
    }

    /**
     * Get patient details by ID.
     * 
     * @param patientId Patient ID
     * @return PatientDTO with all patient information
     * @throws EntityNotFoundException if patient not found
     */
    public PatientDTO getPatient(String patientId) throws EntityNotFoundException {
        return patientService.getPatient(patientId);
    }

    /**
     * Update patient information.
     * 
     * @param dto PatientDTO with patientId and updated fields
     * @throws EntityNotFoundException if patient not found
     * @throws ValidationException if validation fails
     */
    public void updatePatient(PatientDTO dto) throws EntityNotFoundException, ValidationException {
        patientService.updatePatient(dto);
    }

    /**
     * List all registered patients.
     * 
     * @return List of PatientDTOs
     */
    public List<PatientDTO> listPatients() {
        return patientService.listAllPatients();
    }

    // ========== Doctor Operations ==========

    /**
     * Add a new doctor to the system.
     * 
     * @param dto DoctorDTO with doctorId, name, specialty, and optional availableSlots
     * @return Doctor ID
     * @throws ValidationException if validation fails
     */
    public String addDoctor(DoctorDTO dto) throws ValidationException {
        return doctorService.addDoctor(dto);
    }

    /**
     * Get doctor details by ID.
     * 
     * @param doctorId Doctor ID
     * @return DoctorDTO with all doctor information including available slots
     * @throws EntityNotFoundException if doctor not found
     */
    public DoctorDTO getDoctor(String doctorId) throws EntityNotFoundException {
        return doctorService.getDoctor(doctorId);
    }

    /**
     * List all doctors in the system.
     * 
     * @return List of DoctorDTOs
     */
    public List<DoctorDTO> listDoctors() {
        return doctorService.listAllDoctors();
    }

    /**
     * Add an available time slot for a doctor.
     * 
     * @param doctorId Doctor ID
     * @param slot DateTime slot
     * @throws EntityNotFoundException if doctor not found
     */
    public void addDoctorSlot(String doctorId, LocalDateTime slot) throws EntityNotFoundException {
        doctorService.addAvailableSlot(doctorId, slot);
    }

    // ========== Appointment Operations ==========

    /**
     * Book an appointment (atomic operation).
     * Checks slot availability and creates appointment in one transaction.
     * 
     * @param patientId Patient ID
     * @param doctorId Doctor ID
     * @param dateTime Appointment date and time (must be in the future and match doctor's available slot)
     * @return AppointmentDTO with generated appointmentId and status BOOKED
     * @throws EntityNotFoundException if patient or doctor not found
     * @throws SlotUnavailableException if the requested slot is not available
     * @throws ValidationException if validation fails (e.g., past date)
     */
    public AppointmentDTO bookAppointment(String patientId, String doctorId, LocalDateTime dateTime)
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        return appointmentService.bookAppointment(patientId, doctorId, dateTime);
    }

    /**
     * Cancel an appointment and restore the slot to doctor's availability.
     * 
     * @param appointmentId Appointment ID
     * @return true if cancelled successfully, false if already cancelled
     * @throws EntityNotFoundException if appointment not found
     */
    public boolean cancelAppointment(String appointmentId) throws EntityNotFoundException {
        return appointmentService.cancelAppointment(appointmentId);
    }

    /**
     * Reschedule an appointment to a new time (atomic operation).
     * 
     * @param appointmentId Appointment ID
     * @param newDateTime New appointment date and time
     * @return Updated AppointmentDTO
     * @throws EntityNotFoundException if appointment not found
     * @throws SlotUnavailableException if new slot not available
     * @throws ValidationException if validation fails
     */
    public AppointmentDTO rescheduleAppointment(String appointmentId, LocalDateTime newDateTime)
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        return appointmentService.rescheduleAppointment(appointmentId, newDateTime);
    }

    /**
     * Get appointment details by ID.
     * 
     * @param appointmentId Appointment ID
     * @return AppointmentDTO
     * @throws EntityNotFoundException if appointment not found
     */
    public AppointmentDTO getAppointment(String appointmentId) throws EntityNotFoundException {
        return appointmentService.getAppointment(appointmentId);
    }

    /**
     * Get all appointments for a patient.
     * 
     * @param patientId Patient ID
     * @return List of AppointmentDTOs sorted by date
     */
    public List<AppointmentDTO> getAppointmentsByPatient(String patientId) {
        return appointmentService.getAppointmentsByPatient(patientId);
    }

    /**
     * Get all appointments for a doctor.
     * 
     * @param doctorId Doctor ID
     * @return List of AppointmentDTOs sorted by date
     */
    public List<AppointmentDTO> getAppointmentsByDoctor(String doctorId) {
        return appointmentService.getAppointmentsByDoctor(doctorId);
    }

    /**
     * Get all appointments on a specific date.
     * 
     * @param date Date to query
     * @return List of AppointmentDTOs sorted by time
     */
    public List<AppointmentDTO> getAppointmentsByDate(LocalDate date) {
        return appointmentService.getAppointmentsByDate(date);
    }

    /**
     * List all appointments in the system.
     * 
     * @return List of AppointmentDTOs sorted by date
     */
    public List<AppointmentDTO> listAppointments() {
        return appointmentService.listAllAppointments();
    }

    /**
     * Mark appointment as completed.
     * 
     * @param appointmentId Appointment ID
     * @throws EntityNotFoundException if appointment not found
     */
    public void completeAppointment(String appointmentId) throws EntityNotFoundException {
        appointmentService.completeAppointment(appointmentId);
    }

    // ========== Health Record Operations ==========

    /**
     * Add a health record for a patient.
     * 
     * @param dto HealthRecordDTO with patientId, doctorId, symptoms, diagnosis, prescription
     * @return Generated record ID (format: R####)
     * @throws EntityNotFoundException if patient or doctor not found
     * @throws ValidationException if validation fails
     */
    public String addHealthRecord(HealthRecordDTO dto) 
            throws EntityNotFoundException, ValidationException {
        return healthRecordService.addHealthRecord(dto);
    }

    /**
     * Get health record by ID.
     * 
     * @param recordId Record ID
     * @return HealthRecordDTO
     * @throws EntityNotFoundException if record not found
     */
    public HealthRecordDTO getHealthRecord(String recordId) throws EntityNotFoundException {
        return healthRecordService.getHealthRecord(recordId);
    }

    /**
     * Get all health records for a patient, sorted by date.
     * 
     * @param patientId Patient ID
     * @return List of HealthRecordDTOs sorted by date (oldest first)
     */
    public List<HealthRecordDTO> getPatientHealthRecords(String patientId) {
        return healthRecordService.getRecordsByPatient(patientId);
    }

    /**
     * Get all health records created by a doctor.
     * 
     * @param doctorId Doctor ID
     * @return List of HealthRecordDTOs sorted by date
     */
    public List<HealthRecordDTO> getRecordsByDoctor(String doctorId) {
        return healthRecordService.getRecordsByDoctor(doctorId);
    }

    /**
     * List all health records.
     * 
     * @return List of HealthRecordDTOs sorted by date
     */
    public List<HealthRecordDTO> listAllHealthRecords() {
        return healthRecordService.listAllRecords();
    }

    // ========== Export Operations ==========

    /**
     * Export patient history to a text file.
     * Creates a human-readable report with demographics, all visits, diagnoses, and prescriptions.
     * 
     * @param patientId Patient ID
     * @param outputFilePath Output file path (e.g., "export/history_P1001.txt")
     * @throws EntityNotFoundException if patient not found
     * @throws IOException if file write fails
     */
    public void exportPatientHistory(String patientId, String outputFilePath) 
            throws EntityNotFoundException, IOException {
        exportService.exportPatientHistory(patientId, outputFilePath);
    }
}
