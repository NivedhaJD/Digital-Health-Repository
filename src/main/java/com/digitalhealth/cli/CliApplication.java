package com.digitalhealth.cli;

import com.digitalhealth.dto.*;
import com.digitalhealth.exception.*;
import com.digitalhealth.facade.BackendFacade;
import com.digitalhealth.facade.BackendFactory;
import com.digitalhealth.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface for demonstrating the Digital Health Repository backend.
 * This CLI is for demonstration purposes only. The primary integration is through BackendFacade.
 */
public class CliApplication {
    private final BackendFacade facade;
    private final Scanner scanner;

    public CliApplication() {
        this.facade = BackendFactory.createFileBackend();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Digital Health Repository System");
        System.out.println("========================================\n");

        CliApplication app = new CliApplication();
        
        // Initialize with sample data if needed
        app.initializeSampleData();
        
        // Run demo
        app.runDemo();
        
        System.out.println("\nThank you for using the Digital Health Repository System!");
    }

    private void initializeSampleData() {
        try {
            List<DoctorDTO> doctors = facade.listDoctors();
            if (doctors.isEmpty()) {
                System.out.println("Initializing sample data...\n");
                
                // Add doctors
                DoctorDTO doc1 = new DoctorDTO("D001", "Dr. Sarah Johnson", "Cardiology");
                doc1.getAvailableSlots().add(LocalDateTime.of(2025, 11, 15, 10, 0));
                doc1.getAvailableSlots().add(LocalDateTime.of(2025, 11, 15, 14, 0));
                doc1.getAvailableSlots().add(LocalDateTime.of(2025, 11, 16, 9, 0));
                facade.addDoctor(doc1);
                
                DoctorDTO doc2 = new DoctorDTO("D002", "Dr. Michael Chen", "General Medicine");
                doc2.getAvailableSlots().add(LocalDateTime.of(2025, 11, 15, 11, 0));
                doc2.getAvailableSlots().add(LocalDateTime.of(2025, 11, 15, 15, 0));
                doc2.getAvailableSlots().add(LocalDateTime.of(2025, 11, 16, 10, 0));
                facade.addDoctor(doc2);
                
                DoctorDTO doc3 = new DoctorDTO("D003", "Dr. Emily Rodriguez", "Pediatrics");
                doc3.getAvailableSlots().add(LocalDateTime.of(2025, 11, 15, 13, 0));
                doc3.getAvailableSlots().add(LocalDateTime.of(2025, 11, 16, 11, 0));
                facade.addDoctor(doc3);
                
                System.out.println("Sample doctors added successfully.\n");
            }
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
        }
    }

    private void runDemo() {
        boolean running = true;
        
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            System.out.println();
            
            try {
                switch (choice) {
                    case "1":
                        registerPatientDemo();
                        break;
                    case "2":
                        viewPatientDemo();
                        break;
                    case "3":
                        listPatientsDemo();
                        break;
                    case "4":
                        listDoctorsDemo();
                        break;
                    case "5":
                        bookAppointmentDemo();
                        break;
                    case "6":
                        viewAppointmentsDemo();
                        break;
                    case "7":
                        cancelAppointmentDemo();
                        break;
                    case "8":
                        rescheduleAppointmentDemo();
                        break;
                    case "9":
                        addHealthRecordDemo();
                        break;
                    case "10":
                        viewHealthRecordsDemo();
                        break;
                    case "11":
                        exportPatientHistoryDemo();
                        break;
                    case "12":
                        runFullScenarioDemo();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========================================");
        System.out.println("              MAIN MENU");
        System.out.println("========================================");
        System.out.println("1.  Register Patient");
        System.out.println("2.  View Patient Details");
        System.out.println("3.  List All Patients");
        System.out.println("4.  List All Doctors");
        System.out.println("5.  Book Appointment");
        System.out.println("6.  View Appointments");
        System.out.println("7.  Cancel Appointment");
        System.out.println("8.  Reschedule Appointment");
        System.out.println("9.  Add Health Record");
        System.out.println("10. View Patient Health Records");
        System.out.println("11. Export Patient History");
        System.out.println("12. Run Full Scenario Demo");
        System.out.println("0.  Exit");
        System.out.println("========================================");
    }

    private void registerPatientDemo() throws ValidationException {
        System.out.println("=== Register New Patient ===");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Age: ");
        int age = Integer.parseInt(scanner.nextLine());
        System.out.print("Gender (M/F/Other): ");
        String gender = scanner.nextLine();
        System.out.print("Contact (10 digits): ");
        String contact = scanner.nextLine();

        PatientDTO dto = new PatientDTO(name, age, gender, contact);
        String patientId = facade.registerPatient(dto);
        
        System.out.println("\n✓ Patient registered successfully!");
        System.out.println("  Patient ID: " + patientId);
    }

    private void viewPatientDemo() throws EntityNotFoundException {
        System.out.println("=== View Patient Details ===");
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();

        PatientDTO patient = facade.getPatient(patientId);
        
        System.out.println("\n--- Patient Information ---");
        System.out.println("ID: " + patient.getPatientId());
        System.out.println("Name: " + patient.getName());
        System.out.println("Age: " + patient.getAge());
        System.out.println("Gender: " + patient.getGender());
        System.out.println("Contact: " + patient.getContact());
    }

    private void listPatientsDemo() {
        System.out.println("=== All Patients ===\n");
        List<PatientDTO> patients = facade.listPatients();
        
        if (patients.isEmpty()) {
            System.out.println("No patients registered.");
        } else {
            for (PatientDTO p : patients) {
                System.out.printf("%-10s %-30s Age: %-3d Gender: %-6s Contact: %s\n",
                    p.getPatientId(), p.getName(), p.getAge(), p.getGender(), p.getContact());
            }
            System.out.println("\nTotal patients: " + patients.size());
        }
    }

    private void listDoctorsDemo() {
        System.out.println("=== All Doctors ===\n");
        List<DoctorDTO> doctors = facade.listDoctors();
        
        if (doctors.isEmpty()) {
            System.out.println("No doctors available.");
        } else {
            for (DoctorDTO d : doctors) {
                System.out.printf("%-6s %-30s Specialty: %-20s Slots: %d\n",
                    d.getDoctorId(), d.getName(), d.getSpecialty(), d.getAvailableSlots().size());
                
                if (!d.getAvailableSlots().isEmpty()) {
                    System.out.println("       Available slots:");
                    for (LocalDateTime slot : d.getAvailableSlots()) {
                        System.out.println("       - " + slot);
                    }
                }
                System.out.println();
            }
        }
    }

    private void bookAppointmentDemo() 
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        System.out.println("=== Book Appointment ===");
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        System.out.print("Doctor ID: ");
        String doctorId = scanner.nextLine();
        
        // Show available slots
        DoctorDTO doctor = facade.getDoctor(doctorId);
        if (doctor.getAvailableSlots().isEmpty()) {
            System.out.println("No available slots for this doctor.");
            return;
        }
        
        System.out.println("\nAvailable slots:");
        for (int i = 0; i < doctor.getAvailableSlots().size(); i++) {
            System.out.printf("%d. %s\n", i + 1, doctor.getAvailableSlots().get(i));
        }
        
        System.out.print("\nSelect slot number: ");
        int slotIndex = Integer.parseInt(scanner.nextLine()) - 1;
        LocalDateTime slot = doctor.getAvailableSlots().get(slotIndex);

        AppointmentDTO appointment = facade.bookAppointment(patientId, doctorId, slot);
        
        System.out.println("\n✓ Appointment booked successfully!");
        System.out.println("  Appointment ID: " + appointment.getAppointmentId());
        System.out.println("  Date & Time: " + appointment.getDateTime());
    }

    private void viewAppointmentsDemo() {
        System.out.println("=== View Appointments ===");
        System.out.println("1. By Patient");
        System.out.println("2. By Doctor");
        System.out.println("3. All Appointments");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();

        List<AppointmentDTO> appointments = null;
        
        try {
            switch (choice) {
                case "1":
                    System.out.print("Patient ID: ");
                    String patientId = scanner.nextLine();
                    appointments = facade.getAppointmentsByPatient(patientId);
                    break;
                case "2":
                    System.out.print("Doctor ID: ");
                    String doctorId = scanner.nextLine();
                    appointments = facade.getAppointmentsByDoctor(doctorId);
                    break;
                case "3":
                    appointments = facade.listAppointments();
                    break;
            }

            if (appointments != null) {
                System.out.println("\n--- Appointments ---");
                if (appointments.isEmpty()) {
                    System.out.println("No appointments found.");
                } else {
                    for (AppointmentDTO a : appointments) {
                        System.out.printf("%-8s Patient: %-8s Doctor: %-6s Time: %-20s Status: %s\n",
                            a.getAppointmentId(), a.getPatientId(), a.getDoctorId(), 
                            a.getDateTime(), a.getStatus());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void cancelAppointmentDemo() throws EntityNotFoundException {
        System.out.println("=== Cancel Appointment ===");
        System.out.print("Appointment ID: ");
        String appointmentId = scanner.nextLine();

        boolean cancelled = facade.cancelAppointment(appointmentId);
        
        if (cancelled) {
            System.out.println("\n✓ Appointment cancelled successfully!");
            System.out.println("  The slot has been restored to doctor's availability.");
        } else {
            System.out.println("Appointment was already cancelled.");
        }
    }

    private void rescheduleAppointmentDemo() 
            throws EntityNotFoundException, SlotUnavailableException, ValidationException {
        System.out.println("=== Reschedule Appointment ===");
        System.out.print("Appointment ID: ");
        String appointmentId = scanner.nextLine();
        
        AppointmentDTO current = facade.getAppointment(appointmentId);
        System.out.println("\nCurrent appointment: " + current.getDateTime());
        
        // Show available slots for the doctor
        DoctorDTO doctor = facade.getDoctor(current.getDoctorId());
        System.out.println("\nAvailable slots:");
        for (int i = 0; i < doctor.getAvailableSlots().size(); i++) {
            System.out.printf("%d. %s\n", i + 1, doctor.getAvailableSlots().get(i));
        }
        
        System.out.print("\nSelect new slot number: ");
        int slotIndex = Integer.parseInt(scanner.nextLine()) - 1;
        LocalDateTime newSlot = doctor.getAvailableSlots().get(slotIndex);

        AppointmentDTO updated = facade.rescheduleAppointment(appointmentId, newSlot);
        
        System.out.println("\n✓ Appointment rescheduled successfully!");
        System.out.println("  New Date & Time: " + updated.getDateTime());
    }

    private void addHealthRecordDemo() 
            throws EntityNotFoundException, ValidationException {
        System.out.println("=== Add Health Record ===");
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        System.out.print("Doctor ID: ");
        String doctorId = scanner.nextLine();
        System.out.print("Symptoms: ");
        String symptoms = scanner.nextLine();
        System.out.print("Diagnosis: ");
        String diagnosis = scanner.nextLine();
        System.out.print("Prescription: ");
        String prescription = scanner.nextLine();

        HealthRecordDTO dto = new HealthRecordDTO(
            patientId, doctorId, LocalDateTime.now(), symptoms, diagnosis, prescription);
        
        String recordId = facade.addHealthRecord(dto);
        
        System.out.println("\n✓ Health record added successfully!");
        System.out.println("  Record ID: " + recordId);
    }

    private void viewHealthRecordsDemo() {
        System.out.println("=== View Health Records ===");
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();

        List<HealthRecordDTO> records = facade.getPatientHealthRecords(patientId);
        
        if (records.isEmpty()) {
            System.out.println("\nNo health records found for this patient.");
        } else {
            System.out.println("\n--- Health Records (sorted by date) ---\n");
            for (int i = 0; i < records.size(); i++) {
                HealthRecordDTO r = records.get(i);
                System.out.printf("Record #%d\n", i + 1);
                System.out.println("  ID: " + r.getRecordId());
                System.out.println("  Date: " + r.getDate());
                System.out.println("  Doctor: " + r.getDoctorId());
                System.out.println("  Symptoms: " + r.getSymptoms());
                System.out.println("  Diagnosis: " + r.getDiagnosis());
                System.out.println("  Prescription: " + r.getPrescription());
                System.out.println();
            }
        }
    }

    private void exportPatientHistoryDemo() throws EntityNotFoundException {
        System.out.println("=== Export Patient History ===");
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        
        String fileName = "export/history_" + patientId + ".txt";
        
        try {
            new java.io.File("export").mkdirs();
            facade.exportPatientHistory(patientId, fileName);
            System.out.println("\n✓ Patient history exported successfully!");
            System.out.println("  File: " + fileName);
        } catch (Exception e) {
            System.err.println("Error exporting history: " + e.getMessage());
        }
    }

    private void runFullScenarioDemo() {
        System.out.println("=== Running Full Scenario Demo ===\n");
        
        try {
            // Register a patient
            System.out.println("1. Registering patient...");
            PatientDTO patientDto = new PatientDTO("Lea D.", 19, "F", "9999999999");
            String patientId = facade.registerPatient(patientDto);
            System.out.println("   ✓ Patient registered: " + patientId);

            // List doctors
            System.out.println("\n2. Listing available doctors...");
            List<DoctorDTO> doctors = facade.listDoctors();
            for (DoctorDTO d : doctors) {
                System.out.println("   - " + d.getDoctorId() + ": " + d.getName() + 
                                 " (" + d.getSpecialty() + ")");
            }

            if (!doctors.isEmpty() && !doctors.get(0).getAvailableSlots().isEmpty()) {
                // Book appointment
                System.out.println("\n3. Booking appointment...");
                DoctorDTO doctor = doctors.get(0);
                LocalDateTime slot = doctor.getAvailableSlots().get(0);
                AppointmentDTO appointment = facade.bookAppointment(patientId, doctor.getDoctorId(), slot);
                System.out.println("   ✓ Appointment booked: " + appointment.getAppointmentId());
                System.out.println("     Time: " + appointment.getDateTime());

                // Add health record
                System.out.println("\n4. Adding health record...");
                HealthRecordDTO recordDto = new HealthRecordDTO(
                    patientId, doctor.getDoctorId(), LocalDateTime.now(),
                    "Fever, headache", "Common cold", "Rest and fluids, Paracetamol 500mg"
                );
                String recordId = facade.addHealthRecord(recordDto);
                System.out.println("   ✓ Health record added: " + recordId);

                // View appointments
                System.out.println("\n5. Viewing patient appointments...");
                List<AppointmentDTO> appointments = facade.getAppointmentsByPatient(patientId);
                System.out.println("   Total appointments: " + appointments.size());

                // Export history
                System.out.println("\n6. Exporting patient history...");
                new java.io.File("export").mkdirs();
                String exportFile = "export/history_" + patientId + ".txt";
                facade.exportPatientHistory(patientId, exportFile);
                System.out.println("   ✓ History exported to: " + exportFile);
            }

            System.out.println("\n=== Demo completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Demo error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
