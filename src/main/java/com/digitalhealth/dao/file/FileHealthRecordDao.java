package com.digitalhealth.dao.file;

import com.digitalhealth.dao.HealthRecordDao;
import com.digitalhealth.model.HealthRecord;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File-based implementation of HealthRecordDao using Java serialization.
 * Thread-safe with read-write locks.
 */
public class FileHealthRecordDao implements HealthRecordDao {
    private final String filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileHealthRecordDao(String filePath) {
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
                throw new RuntimeException("Failed to create health record data file", e);
            }
        }
    }

    @Override
    public void saveAll(Map<String, HealthRecord> records) {
        lock.writeLock().lock();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(records);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save health records", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, HealthRecord> loadAll() {
        lock.readLock().lock();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<String, HealthRecord>) ois.readObject();
        } catch (EOFException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load health records", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<HealthRecord> findById(String id) {
        Map<String, HealthRecord> records = loadAll();
        return Optional.ofNullable(records.get(id));
    }

    @Override
    public void save(HealthRecord record) {
        lock.writeLock().lock();
        try {
            Map<String, HealthRecord> records = loadAll();
            records.put(record.getRecordId(), record);
            saveAll(records);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
