package com.digitalhealth.service;

import com.digitalhealth.dto.HealthRecordDTO;
import com.digitalhealth.dto.PatientDTO;
import com.digitalhealth.exception.EntityNotFoundException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting patient data to files.
 */
public class ExportService {
    private final PatientService patientService;
    private final HealthRecordService healthRecordService;
    private final DoctorService doctorService;

    public ExportService(PatientService patientService, 
                        HealthRecordService healthRecordService,
                        DoctorService doctorService) {
        this.patientService = patientService;
        this.healthRecordService = healthRecordService;
        this.doctorService = doctorService;
    }

    /**
     * Export patient history to a text file.
     * 
     * @param patientId Patient ID
     * @param outputPath Output file path
     * @throws EntityNotFoundException if patient not found
     * @throws IOException if file write fails
     */
    public void exportPatientHistory(String patientId, String outputPath) 
            throws EntityNotFoundException, IOException {
        
        PatientDTO patient = patientService.getPatient(patientId);
        List<HealthRecordDTO> records = healthRecordService.getRecordsByPatient(patientId);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("========================================\n");
            writer.write("    PATIENT HEALTH HISTORY REPORT\n");
            writer.write("========================================\n\n");

            // Patient Demographics
            writer.write("PATIENT INFORMATION:\n");
            writer.write("--------------------\n");
            writer.write(String.format("Patient ID: %s\n", patient.getPatientId()));
            writer.write(String.format("Name: %s\n", patient.getName()));
            writer.write(String.format("Age: %d years\n", patient.getAge()));
            writer.write(String.format("Gender: %s\n", patient.getGender()));
            writer.write(String.format("Contact: %s\n", patient.getContact()));
            writer.write("\n");

            // Medical History
            writer.write("MEDICAL HISTORY:\n");
            writer.write("----------------\n");
            writer.write(String.format("Total Visits: %d\n\n", records.size()));

            if (records.isEmpty()) {
                writer.write("No medical records found.\n");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                
                for (int i = 0; i < records.size(); i++) {
                    HealthRecordDTO record = records.get(i);
                    writer.write(String.format("VISIT #%d\n", i + 1));
                    writer.write("--------\n");
                    writer.write(String.format("Record ID: %s\n", record.getRecordId()));
                    writer.write(String.format("Date: %s\n", record.getDate().format(formatter)));
                    
                    try {
                        String doctorName = doctorService.getDoctor(record.getDoctorId()).getName();
                        writer.write(String.format("Doctor: %s (ID: %s)\n", doctorName, record.getDoctorId()));
                    } catch (EntityNotFoundException e) {
                        writer.write(String.format("Doctor ID: %s\n", record.getDoctorId()));
                    }
                    
                    writer.write(String.format("Symptoms: %s\n", record.getSymptoms()));
                    writer.write(String.format("Diagnosis: %s\n", record.getDiagnosis()));
                    writer.write(String.format("Prescription: %s\n", 
                        record.getPrescription() != null ? record.getPrescription() : "None"));
                    writer.write("\n");
                }
            }

            writer.write("========================================\n");
            writer.write("        END OF REPORT\n");
            writer.write("========================================\n");
        }
    }
}
