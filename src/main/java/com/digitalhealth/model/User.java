package com.digitalhealth.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain model representing a user account for authentication.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String linkedEntityId; // patientId, doctorId, or "admin"
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean isActive;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public User(String userId, String username, String passwordHash, UserRole role, String linkedEntityId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.linkedEntityId = linkedEntityId;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getLinkedEntityId() {
        return linkedEntityId;
    }

    public void setLinkedEntityId(String linkedEntityId) {
        this.linkedEntityId = linkedEntityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", linkedEntityId='" + linkedEntityId + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
