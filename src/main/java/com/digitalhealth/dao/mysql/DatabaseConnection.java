package com.digitalhealth.dao.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager for MySQL.
 * Thread-safe connection pooling would be recommended for production.
 */
public class DatabaseConnection {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection(Properties config) {
        this.url = config.getProperty("db.url", "jdbc:mysql://localhost:3306/digitalhealth");
        this.username = config.getProperty("db.username", "root");
        this.password = config.getProperty("db.password", "");
    }

    /**
     * Get a new database connection.
     * Caller is responsible for closing the connection.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(url, username, password);
    }
}
