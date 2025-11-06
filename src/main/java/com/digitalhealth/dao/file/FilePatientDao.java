package com.digitalhealth.dao.file;

import com.digitalhealth.dao.PatientDao;
import com.digitalhealth.model.Patient;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File-based implementation of PatientDao using Java serialization.
 * Thread-safe with read-write locks.
 */
public class FilePatientDao implements PatientDao {
    private final String filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FilePatientDao(String filePath) {
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
                throw new RuntimeException("Failed to create patient data file", e);
            }
        }
    }

    @Override
    public void saveAll(Map<String, Patient> patients) {
        lock.writeLock().lock();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(patients);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save patients", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Patient> loadAll() {
        lock.readLock().lock();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<String, Patient>) ois.readObject();
        } catch (EOFException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load patients", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Patient> findById(String id) {
        Map<String, Patient> patients = loadAll();
        return Optional.ofNullable(patients.get(id));
    }

    @Override
    public void save(Patient patient) {
        lock.writeLock().lock();
        try {
            Map<String, Patient> patients = loadAll();
            patients.put(patient.getPatientId(), patient);
            saveAll(patients);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(String patientId) {
        Map<String, Patient> patients = loadAll();
        return patients.containsKey(patientId);
    }

    @Override
    public void delete(String patientId) {
        lock.writeLock().lock();
        try {
            Map<String, Patient> patients = loadAll();
            patients.remove(patientId);
            saveAll(patients);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
