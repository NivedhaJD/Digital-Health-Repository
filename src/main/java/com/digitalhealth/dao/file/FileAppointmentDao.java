package com.digitalhealth.dao.file;

import com.digitalhealth.dao.AppointmentDao;
import com.digitalhealth.model.Appointment;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File-based implementation of AppointmentDao using Java serialization.
 * Thread-safe with read-write locks.
 */
public class FileAppointmentDao implements AppointmentDao {
    private final String filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileAppointmentDao(String filePath) {
        this.filePath = filePath;
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                saveAll(new HashMap<>());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create appointment data file", e);
            }
        }
    }

    @Override
    public void saveAll(Map<String, Appointment> appointments) {
        lock.writeLock().lock();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(appointments);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save appointments", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Appointment> loadAll() {
        lock.readLock().lock();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<String, Appointment>) ois.readObject();
        } catch (EOFException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load appointments", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Appointment> findById(String id) {
        Map<String, Appointment> appointments = loadAll();
        return Optional.ofNullable(appointments.get(id));
    }

    @Override
    public void save(Appointment appointment) {
        lock.writeLock().lock();
        try {
            Map<String, Appointment> appointments = loadAll();
            appointments.put(appointment.getAppointmentId(), appointment);
            saveAll(appointments);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(String appointmentId) {
        return findById(appointmentId).isPresent();
    }

    @Override
    public void delete(String appointmentId) {
        lock.writeLock().lock();
        try {
            Map<String, Appointment> appointments = loadAll();
            appointments.remove(appointmentId);
            saveAll(appointments);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
