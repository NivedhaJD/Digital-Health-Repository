package com.digitalhealth.facade;

import com.digitalhealth.dao.*;
import com.digitalhealth.dao.file.*;
import com.digitalhealth.dao.mysql.*;
import com.digitalhealth.service.*;
import com.digitalhealth.util.DatabaseConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Factory for creating BackendFacade instances with file-based or MySQL persistence.
 * 
 * Usage:
 * <pre>
 * // MySQL backend
 * BackendFacade facade = BackendFactory.createMySQLBackend();
 * 
 * // File-based backend (default)
 * BackendFacade facade = BackendFactory.createFileBackend();
 * 
 * // File-based with custom data directory
 * BackendFacade facade = BackendFactory.createFileBackend("data/myapp");
 * 
 * // Auto-detect from application.properties
 * BackendFacade facade = BackendFactory.create();
 * </pre>
 */
public class BackendFactory {
    private static final String DEFAULT_DATA_DIR = "data";

    /**
     * Create backend based on application.properties.
     * Checks if database is configured, otherwise defaults to file-based persistence.
     */
    public static BackendFacade create() {
        Properties props = loadProperties();
        String dbUrl = props.getProperty("db.url");
        
        // If database is configured, try MySQL first
        if (dbUrl != null && !dbUrl.isEmpty()) {
            if (DatabaseConnection.testConnection()) {
                System.out.println("Using MySQL database backend");
                return createMySQLBackend();
            } else {
                System.out.println("Database connection failed, falling back to file-based backend");
            }
        }
        
        // Fallback to file-based
        String dataDir = props.getProperty("data.directory", DEFAULT_DATA_DIR);
        System.out.println("Using file-based backend");
        return createFileBackend(dataDir);
    }

    /**
     * Create MySQL-based backend.
     */
    public static BackendFacade createMySQLBackend() {
        // Initialize MySQL DAOs
        PatientDao patientDao = new MySQLPatientDao();
        DoctorDao doctorDao = new MySQLDoctorDao();
        AppointmentDao appointmentDao = new MySQLAppointmentDao();
        HealthRecordDao healthRecordDao = new MySQLHealthRecordDao();

        return createFacade(patientDao, doctorDao, appointmentDao, healthRecordDao);
    }

    /**
     * Create file-based backend with default data directory.
     */
    public static BackendFacade createFileBackend() {
        return createFileBackend(DEFAULT_DATA_DIR);
    }

    /**
     * Create file-based backend with custom data directory.
     * 
     * @param dataDirectory Directory for data files
     */
    public static BackendFacade createFileBackend(String dataDirectory) {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Initialize DAOs
        PatientDao patientDao = new FilePatientDao(dataDirectory + "/patients.dat");
        DoctorDao doctorDao = new FileDoctorDao(dataDirectory + "/doctors.dat");
        AppointmentDao appointmentDao = new FileAppointmentDao(dataDirectory + "/appointments.dat");
        HealthRecordDao healthRecordDao = new FileHealthRecordDao(dataDirectory + "/records.dat");

        return createFacade(patientDao, doctorDao, appointmentDao, healthRecordDao);
    }

    private static BackendFacade createFacade(PatientDao patientDao, 
                                             DoctorDao doctorDao,
                                             AppointmentDao appointmentDao, 
                                             HealthRecordDao healthRecordDao) {
        // Initialize services
        PatientService patientService = new PatientService(patientDao);
        DoctorService doctorService = new DoctorService(doctorDao);
        AppointmentService appointmentService = new AppointmentService(
            appointmentDao, patientService, doctorService);
        HealthRecordService healthRecordService = new HealthRecordService(
            healthRecordDao, patientService, doctorService);
        ExportService exportService = new ExportService(
            patientService, healthRecordService, doctorService);

        return new BackendFacade(
            patientService, 
            doctorService, 
            appointmentService, 
            healthRecordService, 
            exportService
        );
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        File propFile = new File("application.properties");
        
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Warning: Could not load application.properties: " + e.getMessage());
            }
        }
        
        return props;
    }
}
