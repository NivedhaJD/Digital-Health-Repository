package com.digitalhealth.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility class.
 * Manages MySQL database connections using configuration from application.properties
 */
public class DatabaseConnection {
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                System.out.println("Unable to find application.properties");
                DB_URL = null; // No database configured
                return;
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            DB_URL = prop.getProperty("db.url");
            
            // If db.url is not set, database is disabled
            if (DB_URL == null || DB_URL.trim().isEmpty()) {
                System.out.println("Database not configured in application.properties");
                return;
            }
            
            DB_USERNAME = prop.getProperty("db.username", "root");
            DB_PASSWORD = prop.getProperty("db.password", "root");
            DB_DRIVER = prop.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
            // Load JDBC driver
            Class.forName(DB_DRIVER);
            
            System.out.println("Database configuration loaded successfully");
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            DB_URL = null; // Disable database on error
        }
    }
    
    /**
     * Get a database connection.
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
    
    /**
     * Close database connection.
     * 
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test database connection.
     * 
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        // If DB_URL is null, database is not configured
        if (DB_URL == null || DB_URL.trim().isEmpty()) {
            System.out.println("Database not configured - using file-based storage");
            return false;
        }
        
        try (Connection conn = getConnection()) {
            boolean isConnected = conn != null && !conn.isClosed();
            if (isConnected) {
                System.out.println("Successfully connected to database: " + DB_URL);
            }
            return isConnected;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get database URL for information purposes.
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }
}
