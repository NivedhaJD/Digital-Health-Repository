package com.digitalhealth.service;

import com.digitalhealth.dao.*;
import com.digitalhealth.dao.file.*;
import com.digitalhealth.dto.DoctorDTO;
import com.digitalhealth.dto.HealthRecordDTO;
import com.digitalhealth.dto.PatientDTO;
import com.digitalhealth.exception.EntityNotFoundException;
import com.digitalhealth.exception.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for HealthRecordService.
 */
public class HealthRecordServiceTest {
    private HealthRecordService healthRecordService;
    private PatientService patientService;
    private DoctorService doctorService;
    private String testDir = "test-data-record";

    private String patientId;
    private String doctorId;

    @Before
    public void setUp() throws Exception {
        new File(testDir).mkdirs();

        PatientDao patientDao = new FilePatientDao(testDir + "/patients.dat");
        DoctorDao doctorDao = new FileDoctorDao(testDir + "/doctors.dat");
        HealthRecordDao recordDao = new FileHealthRecordDao(testDir + "/records.dat");

        patientService = new PatientService(patientDao);
        doctorService = new DoctorService(doctorDao);
        healthRecordService = new HealthRecordService(recordDao, patientService, doctorService);

        // Setup test data
        PatientDTO patientDto = new PatientDTO("Test Patient", 30, "M", "1234567890");
        patientId = patientService.registerPatient(patientDto);

        DoctorDTO doctorDto = new DoctorDTO("D001", "Dr. Test", "General");
        doctorId = doctorService.addDoctor(doctorDto);
    }

    @After
    public void tearDown() {
        new File(testDir + "/patients.dat").delete();
        new File(testDir + "/doctors.dat").delete();
        new File(testDir + "/records.dat").delete();
        new File(testDir).delete();
    }

    @Test
    public void testAddHealthRecord_Success() throws Exception {
        HealthRecordDTO dto = new HealthRecordDTO(
            patientId, doctorId, LocalDateTime.now(),
            "Fever", "Common cold", "Rest and fluids"
        );

        String recordId = healthRecordService.addHealthRecord(dto);

        assertNotNull(recordId);
        assertTrue(recordId.startsWith("R"));
    }

    @Test(expected = ValidationException.class)
    public void testAddHealthRecord_MissingSymptoms() throws Exception {
        HealthRecordDTO dto = new HealthRecordDTO(
            patientId, doctorId, LocalDateTime.now(),
            "", "Common cold", "Rest"
        );

        healthRecordService.addHealthRecord(dto);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddHealthRecord_InvalidPatient() throws Exception {
        HealthRecordDTO dto = new HealthRecordDTO(
            "P9999", doctorId, LocalDateTime.now(),
            "Fever", "Cold", "Rest"
        );

        healthRecordService.addHealthRecord(dto);
    }

    @Test
    public void testGetRecordsByPatient_Sorted() throws Exception {
        LocalDateTime time1 = LocalDateTime.now().minusDays(2);
        LocalDateTime time2 = LocalDateTime.now().minusDays(1);
        LocalDateTime time3 = LocalDateTime.now();

        HealthRecordDTO dto1 = new HealthRecordDTO(
            patientId, doctorId, time2, "Symptoms2", "Diagnosis2", "Rx2");
        HealthRecordDTO dto2 = new HealthRecordDTO(
            patientId, doctorId, time1, "Symptoms1", "Diagnosis1", "Rx1");
        HealthRecordDTO dto3 = new HealthRecordDTO(
            patientId, doctorId, time3, "Symptoms3", "Diagnosis3", "Rx3");

        healthRecordService.addHealthRecord(dto1);
        healthRecordService.addHealthRecord(dto2);
        healthRecordService.addHealthRecord(dto3);

        List<HealthRecordDTO> records = healthRecordService.getRecordsByPatient(patientId);

        assertEquals(3, records.size());
        // Verify sorted by date (oldest first)
        assertEquals("Symptoms1", records.get(0).getSymptoms());
        assertEquals("Symptoms2", records.get(1).getSymptoms());
        assertEquals("Symptoms3", records.get(2).getSymptoms());
    }

    @Test
    public void testGetRecordsByDoctor() throws Exception {
        HealthRecordDTO dto = new HealthRecordDTO(
            patientId, doctorId, LocalDateTime.now(),
            "Fever", "Cold", "Rest"
        );

        healthRecordService.addHealthRecord(dto);

        List<HealthRecordDTO> records = healthRecordService.getRecordsByDoctor(doctorId);

        assertEquals(1, records.size());
        assertEquals(doctorId, records.get(0).getDoctorId());
    }
}
