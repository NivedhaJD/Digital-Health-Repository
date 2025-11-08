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
        
        String dbBackend = props.getProperty("db.backend", "file");
        String dbEnabled = props.getProperty("db.enabled", "false");
        String dbUrl = props.getProperty("db.url");
        
        System.out.println("DB Backend: " + dbBackend);
        System.out.println("DB Enabled: " + dbEnabled);
        System.out.println("DB URL: " + dbUrl);
        
        // Check if MySQL is explicitly enabled
        if ("mysql".equalsIgnoreCase(dbBackend) && "true".equalsIgnoreCase(dbEnabled)) {
            if (dbUrl != null && !dbUrl.isEmpty()) {
                if (DatabaseConnection.testConnection()) {
                    System.out.println("✓ Using MySQL database backend");
                    return createMySQLBackend();
                } else {
                    System.err.println("✗ Database connection failed, falling back to file-based backend");
                }
            } else {
                System.err.println("✗ Database URL not configured");
            }
        }
        
        // Fallback to file-based
        String dataDir = props.getProperty("data.directory", DEFAULT_DATA_DIR);
        System.out.println("✓ Using file-based backend (data directory: " + dataDir + ")");
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
        UserDao userDao = new MySQLUserDao();

        return createFacade(patientDao, doctorDao, appointmentDao, healthRecordDao, userDao);
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
        UserDao userDao = new FileUserDao(dataDirectory + "/users.dat");

        return createFacade(patientDao, doctorDao, appointmentDao, healthRecordDao, userDao);
    }

    private static BackendFacade createFacade(PatientDao patientDao, 
                                             DoctorDao doctorDao,
                                             AppointmentDao appointmentDao, 
                                             HealthRecordDao healthRecordDao,
                                             UserDao userDao) {
        // Initialize services
        PatientService patientService = new PatientService(patientDao);
        DoctorService doctorService = new DoctorService(doctorDao);
        AppointmentService appointmentService = new AppointmentService(
            appointmentDao, patientService, doctorService);
        HealthRecordService healthRecordService = new HealthRecordService(
            healthRecordDao, patientService, doctorService);
        ExportService exportService = new ExportService(
            patientService, healthRecordService, doctorService);
        AuthService authService = new AuthService(userDao);

        return new BackendFacade(
            patientService, 
            doctorService, 
            appointmentService, 
            healthRecordService, 
            exportService,
            authService
        );
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        
        // Try loading from current directory first
        File propFile = new File("application.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                props.load(input);
                System.out.println("Loaded properties from: " + propFile.getAbsolutePath());
                return props;
            } catch (IOException e) {
                System.err.println("Warning: Could not load application.properties from file: " + e.getMessage());
            }
        }
        
        // Try loading from classpath
        try (InputStream input = BackendFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                System.out.println("Loaded properties from classpath");
                return props;
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load application.properties from classpath: " + e.getMessage());
        }
        
        System.err.println("Warning: application.properties not found");
        return props;
    }
}
