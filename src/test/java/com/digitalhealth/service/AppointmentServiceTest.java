package com.digitalhealth.service;

import com.digitalhealth.dao.*;
import com.digitalhealth.dao.file.*;
import com.digitalhealth.dto.AppointmentDTO;
import com.digitalhealth.dto.DoctorDTO;
import com.digitalhealth.dto.PatientDTO;
import com.digitalhealth.exception.*;
import com.digitalhealth.model.AppointmentStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for AppointmentService.
 */
public class AppointmentServiceTest {
    private AppointmentService appointmentService;
    private PatientService patientService;
    private DoctorService doctorService;
    private String testDir = "test-data-appt";

    private String patientId;
    private String doctorId;
    private LocalDateTime slot1;
    private LocalDateTime slot2;

    @Before
    public void setUp() throws Exception {
        new File(testDir).mkdirs();

        PatientDao patientDao = new FilePatientDao(testDir + "/patients.dat");
        DoctorDao doctorDao = new FileDoctorDao(testDir + "/doctors.dat");
        AppointmentDao appointmentDao = new FileAppointmentDao(testDir + "/appointments.dat");

        patientService = new PatientService(patientDao);
        doctorService = new DoctorService(doctorDao);
        appointmentService = new AppointmentService(appointmentDao, patientService, doctorService);

        // Setup test data
        PatientDTO patientDto = new PatientDTO("Test Patient", 30, "M", "1234567890");
        patientId = patientService.registerPatient(patientDto);

        slot1 = LocalDateTime.now().plusDays(1);
        slot2 = LocalDateTime.now().plusDays(2);

        DoctorDTO doctorDto = new DoctorDTO("D001", "Dr. Test", "General");
        doctorDto.getAvailableSlots().add(slot1);
        doctorDto.getAvailableSlots().add(slot2);
        doctorId = doctorService.addDoctor(doctorDto);
    }

    @After
    public void tearDown() {
        new File(testDir + "/patients.dat").delete();
        new File(testDir + "/doctors.dat").delete();
        new File(testDir + "/appointments.dat").delete();
        new File(testDir).delete();
    }

    @Test
    public void testBookAppointment_Success() throws Exception {
        AppointmentDTO appointment = appointmentService.bookAppointment(patientId, doctorId, slot1);

        assertNotNull(appointment);
        assertNotNull(appointment.getAppointmentId());
        assertTrue(appointment.getAppointmentId().startsWith("A"));
        assertEquals(patientId, appointment.getPatientId());
        assertEquals(doctorId, appointment.getDoctorId());
        assertEquals(slot1, appointment.getDateTime());
        assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());

        // Verify slot removed from doctor
        DoctorDTO doctor = doctorService.getDoctor(doctorId);
        assertFalse(doctor.getAvailableSlots().contains(slot1));
        assertTrue(doctor.getAvailableSlots().contains(slot2));
    }

    @Test(expected = SlotUnavailableException.class)
    public void testBookAppointment_SlotNotAvailable() throws Exception {
        LocalDateTime unavailableSlot = LocalDateTime.now().plusDays(10);
        appointmentService.bookAppointment(patientId, doctorId, unavailableSlot);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testBookAppointment_InvalidPatient() throws Exception {
        appointmentService.bookAppointment("P9999", doctorId, slot1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testBookAppointment_InvalidDoctor() throws Exception {
        appointmentService.bookAppointment(patientId, "D9999", slot1);
    }

    @Test(expected = ValidationException.class)
    public void testBookAppointment_PastDate() throws Exception {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        DoctorDTO doctor = doctorService.getDoctor(doctorId);
        doctor.getAvailableSlots().add(pastDate);
        doctorService.addDoctor(doctor);

        appointmentService.bookAppointment(patientId, doctorId, pastDate);
    }

    @Test
    public void testCancelAppointment_Success() throws Exception {
        AppointmentDTO appointment = appointmentService.bookAppointment(patientId, doctorId, slot1);

        boolean cancelled = appointmentService.cancelAppointment(appointment.getAppointmentId());

        assertTrue(cancelled);

        // Verify appointment status changed
        AppointmentDTO retrieved = appointmentService.getAppointment(appointment.getAppointmentId());
        assertEquals(AppointmentStatus.CANCELLED, retrieved.getStatus());

        // Verify slot restored to doctor
        DoctorDTO doctor = doctorService.getDoctor(doctorId);
        assertTrue(doctor.getAvailableSlots().contains(slot1));
    }

    @Test
    public void testCancelAppointment_AlreadyCancelled() throws Exception {
        AppointmentDTO appointment = appointmentService.bookAppointment(patientId, doctorId, slot1);

        appointmentService.cancelAppointment(appointment.getAppointmentId());
        boolean secondCancel = appointmentService.cancelAppointment(appointment.getAppointmentId());

        assertFalse(secondCancel);
    }

    @Test
    public void testRescheduleAppointment_Success() throws Exception {
        AppointmentDTO appointment = appointmentService.bookAppointment(patientId, doctorId, slot1);

        AppointmentDTO rescheduled = appointmentService.rescheduleAppointment(
            appointment.getAppointmentId(), slot2);

        assertNotNull(rescheduled);
        assertEquals(slot2, rescheduled.getDateTime());
        assertEquals(AppointmentStatus.BOOKED, rescheduled.getStatus());

        // Verify old slot restored and new slot removed
        DoctorDTO doctor = doctorService.getDoctor(doctorId);
        assertTrue(doctor.getAvailableSlots().contains(slot1)); // Old slot restored
        assertFalse(doctor.getAvailableSlots().contains(slot2)); // New slot taken
    }

    @Test(expected = SlotUnavailableException.class)
    public void testRescheduleAppointment_NewSlotUnavailable() throws Exception {
        AppointmentDTO appointment = appointmentService.bookAppointment(patientId, doctorId, slot1);
        LocalDateTime unavailableSlot = LocalDateTime.now().plusDays(10);

        appointmentService.rescheduleAppointment(appointment.getAppointmentId(), unavailableSlot);
    }

    @Test
    public void testGetAppointmentsByPatient() throws Exception {
        appointmentService.bookAppointment(patientId, doctorId, slot1);
        appointmentService.bookAppointment(patientId, doctorId, slot2);

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);

        assertEquals(2, appointments.size());
        // Verify sorted by date
        assertTrue(appointments.get(0).getDateTime().isBefore(appointments.get(1).getDateTime()));
    }

    @Test
    public void testGetAppointmentsByDoctor() throws Exception {
        appointmentService.bookAppointment(patientId, doctorId, slot1);

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctor(doctorId);

        assertEquals(1, appointments.size());
        assertEquals(doctorId, appointments.get(0).getDoctorId());
    }

    @Test
    public void testCompleteAppointment() throws Exception {
        AppointmentDTO appointment = appointmentService.bookAppointment(patientId, doctorId, slot1);

        appointmentService.completeAppointment(appointment.getAppointmentId());

        AppointmentDTO retrieved = appointmentService.getAppointment(appointment.getAppointmentId());
        assertEquals(AppointmentStatus.COMPLETED, retrieved.getStatus());
    }

    @Test
    public void testConcurrentBooking_ThreadSafety() throws Exception {
        // This test verifies that concurrent booking attempts are handled correctly
        final boolean[] success1 = {false};
        final boolean[] success2 = {false};
        final Exception[] exception1 = {null};
        final Exception[] exception2 = {null};

        Thread t1 = new Thread(() -> {
            try {
                appointmentService.bookAppointment(patientId, doctorId, slot1);
                success1[0] = true;
            } catch (Exception e) {
                exception1[0] = e;
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                appointmentService.bookAppointment(patientId, doctorId, slot1);
                success2[0] = true;
            } catch (Exception e) {
                exception2[0] = e;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Only one should succeed
        assertTrue(success1[0] ^ success2[0]); // XOR: exactly one is true
        
        if (!success1[0]) {
            assertTrue(exception1[0] instanceof SlotUnavailableException);
        }
        if (!success2[0]) {
            assertTrue(exception2[0] instanceof SlotUnavailableException);
        }
    }
}
