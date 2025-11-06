package com.digitalhealth.dao.file;

import com.digitalhealth.dao.UserDao;
import com.digitalhealth.model.User;
import com.digitalhealth.model.UserRole;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based implementation of UserDao using serialization.
 */
public class FileUserDao implements UserDao {
    private final String filePath;
    private final Map<String, User> users;

    public FileUserDao(String filePath) {
        this.filePath = filePath;
        this.users = new ConcurrentHashMap<>();
        loadFromFile();
    }

    @Override
    public void save(User user) {
        users.put(user.getUserId(), user);
        saveToFile();
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void update(User user) {
        if (users.containsKey(user.getUserId())) {
            users.put(user.getUserId(), user);
            saveToFile();
        }
    }

    @Override
    public void delete(String userId) {
        users.remove(userId);
        saveToFile();
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.values().stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(filePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Map<String, User> loadedUsers = (Map<String, User>) ois.readObject();
                users.putAll(loadedUsers);
                System.out.println("Loaded " + users.size() + " users from " + filePath);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users from file: " + e.getMessage());
            }
        } else {
            // Create default admin user
            User admin = new User(
                "U0001",
                "admin",
                hashPassword("admin123"),
                UserRole.ADMIN,
                "admin"
            );
            users.put(admin.getUserId(), admin);
            saveToFile();
            System.out.println("Created default admin user (username: admin, password: admin123)");
        }
    }

    private void saveToFile() {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new HashMap<>(users));
            }
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Simple password hashing (in production, use bcrypt or similar)
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
