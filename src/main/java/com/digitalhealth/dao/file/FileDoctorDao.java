package com.digitalhealth.dao.file;

import com.digitalhealth.dao.DoctorDao;
import com.digitalhealth.model.Doctor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File-based implementation of DoctorDao using Java serialization.
 * Thread-safe with read-write locks.
 */
public class FileDoctorDao implements DoctorDao {
    private final String filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileDoctorDao(String filePath) {
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
                throw new RuntimeException("Failed to create doctor data file", e);
            }
        }
    }

    @Override
    public void saveAll(Map<String, Doctor> doctors) {
        lock.writeLock().lock();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(doctors);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save doctors", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Doctor> loadAll() {
        lock.readLock().lock();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<String, Doctor>) ois.readObject();
        } catch (EOFException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load doctors", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Doctor> findById(String id) {
        Map<String, Doctor> doctors = loadAll();
        return Optional.ofNullable(doctors.get(id));
    }

    @Override
    public void save(Doctor doctor) {
        lock.writeLock().lock();
        try {
            Map<String, Doctor> doctors = loadAll();
            doctors.put(doctor.getDoctorId(), doctor);
            saveAll(doctors);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(String doctorId) {
        Map<String, Doctor> doctors = loadAll();
        return doctors.containsKey(doctorId);
    }

    @Override
    public void delete(String doctorId) {
        lock.writeLock().lock();
        try {
            Map<String, Doctor> doctors = loadAll();
            doctors.remove(doctorId);
            saveAll(doctors);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
