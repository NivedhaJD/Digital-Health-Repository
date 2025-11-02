package com.digitalhealth.service;

import com.digitalhealth.dao.PatientDao;
import com.digitalhealth.dao.file.FilePatientDao;
import com.digitalhealth.dto.PatientDTO;
import com.digitalhealth.exception.EntityNotFoundException;
import com.digitalhealth.exception.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for PatientService.
 */
public class PatientServiceTest {
    private PatientService patientService;
    private String testDataFile = "test-data/test-patients.dat";

    @Before
    public void setUp() {
        new File("test-data").mkdirs();
        PatientDao patientDao = new FilePatientDao(testDataFile);
        patientService = new PatientService(patientDao);
    }

    @After
    public void tearDown() {
        new File(testDataFile).delete();
        new File("test-data").delete();
    }

    @Test
    public void testRegisterPatient_Success() throws ValidationException {
        PatientDTO dto = new PatientDTO("John Doe", 30, "M", "1234567890");
        String patientId = patientService.registerPatient(dto);

        assertNotNull(patientId);
        assertTrue(patientId.startsWith("P"));
        assertTrue(patientService.patientExists(patientId));
    }

    @Test
    public void testRegisterPatient_AutoIncrementId() throws ValidationException {
        PatientDTO dto1 = new PatientDTO("John Doe", 30, "M", "1234567890");
        PatientDTO dto2 = new PatientDTO("Jane Smith", 25, "F", "0987654321");

        String id1 = patientService.registerPatient(dto1);
        String id2 = patientService.registerPatient(dto2);

        assertNotEquals(id1, id2);
        int num1 = Integer.parseInt(id1.substring(1));
        int num2 = Integer.parseInt(id2.substring(1));
        assertEquals(1, num2 - num1);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterPatient_MissingName() throws ValidationException {
        PatientDTO dto = new PatientDTO("", 30, "M", "1234567890");
        patientService.registerPatient(dto);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterPatient_InvalidAge() throws ValidationException {
        PatientDTO dto = new PatientDTO("John Doe", -5, "M", "1234567890");
        patientService.registerPatient(dto);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterPatient_InvalidContact() throws ValidationException {
        PatientDTO dto = new PatientDTO("John Doe", 30, "M", "123"); // Too short
        patientService.registerPatient(dto);
    }

    @Test
    public void testGetPatient_Success() throws ValidationException, EntityNotFoundException {
        PatientDTO dto = new PatientDTO("John Doe", 30, "M", "1234567890");
        String patientId = patientService.registerPatient(dto);

        PatientDTO retrieved = patientService.getPatient(patientId);

        assertNotNull(retrieved);
        assertEquals(patientId, retrieved.getPatientId());
        assertEquals("John Doe", retrieved.getName());
        assertEquals(30, retrieved.getAge());
        assertEquals("M", retrieved.getGender());
        assertEquals("1234567890", retrieved.getContact());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetPatient_NotFound() throws EntityNotFoundException {
        patientService.getPatient("P9999");
    }

    @Test
    public void testUpdatePatient_Success() 
            throws ValidationException, EntityNotFoundException {
        PatientDTO dto = new PatientDTO("John Doe", 30, "M", "1234567890");
        String patientId = patientService.registerPatient(dto);

        PatientDTO updateDto = new PatientDTO(patientId, "John Updated", 31, "M", "9876543210");
        patientService.updatePatient(updateDto);

        PatientDTO retrieved = patientService.getPatient(patientId);
        assertEquals("John Updated", retrieved.getName());
        assertEquals(31, retrieved.getAge());
        assertEquals("9876543210", retrieved.getContact());
    }

    @Test
    public void testListAllPatients() throws ValidationException {
        PatientDTO dto1 = new PatientDTO("John Doe", 30, "M", "1234567890");
        PatientDTO dto2 = new PatientDTO("Jane Smith", 25, "F", "0987654321");

        patientService.registerPatient(dto1);
        patientService.registerPatient(dto2);

        List<PatientDTO> patients = patientService.listAllPatients();

        assertEquals(2, patients.size());
    }

    @Test
    public void testPersistence_RoundTrip() throws ValidationException, EntityNotFoundException {
        // Register patient
        PatientDTO dto = new PatientDTO("John Doe", 30, "M", "1234567890");
        String patientId = patientService.registerPatient(dto);

        // Create new service instance (simulates app restart)
        PatientDao newDao = new FilePatientDao(testDataFile);
        PatientService newService = new PatientService(newDao);

        // Verify data persisted
        PatientDTO retrieved = newService.getPatient(patientId);
        assertEquals("John Doe", retrieved.getName());
        assertEquals(30, retrieved.getAge());
    }
}
