package com.digitalhealth.util;

import com.digitalhealth.dao.UserDao;
import com.digitalhealth.dao.file.FileUserDao;
import com.digitalhealth.dao.mysql.MySQLUserDao;
import com.digitalhealth.model.User;

import java.util.List;

/**
 * Utility to migrate users from file-based storage to MySQL.
 */
public class UserMigrationUtil {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  User Migration: File to MySQL");
        System.out.println("========================================");
        
        // Initialize DAOs
        UserDao fileDao = new FileUserDao("data/users.dat");
        UserDao mysqlDao = new MySQLUserDao();
        
        // Load users from file
        List<User> users = fileDao.findAll();
        System.out.println("\nFound " + users.size() + " users in file storage");
        
        if (users.isEmpty()) {
            System.out.println("No users to migrate!");
            return;
        }
        
        // Migrate each user
        int successCount = 0;
        int failCount = 0;
        
        for (User user : users) {
            try {
                // Check if user already exists in MySQL
                if (mysqlDao.existsByUsername(user.getUsername())) {
                    System.out.println("⚠ User already exists in MySQL: " + user.getUsername());
                    continue;
                }
                
                // Save to MySQL
                mysqlDao.save(user);
                successCount++;
                System.out.println("✓ Migrated user: " + user.getUsername() + " (Role: " + user.getRole() + ")");
                
            } catch (Exception e) {
                failCount++;
                System.err.println("✗ Failed to migrate user: " + user.getUsername());
                System.err.println("  Error: " + e.getMessage());
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("Migration Summary:");
        System.out.println("  Total users found: " + users.size());
        System.out.println("  Successfully migrated: " + successCount);
        System.out.println("  Failed: " + failCount);
        System.out.println("========================================");
    }
}
