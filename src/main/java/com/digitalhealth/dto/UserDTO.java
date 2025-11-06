package com.digitalhealth.dto;

/**
 * DTO for user registration and data transfer.
 */
public class UserDTO {
    private String userId;
    private String username;
    private String password;
    private String role;
    private String linkedEntityId;

    public UserDTO() {}

    public UserDTO(String userId, String username, String password, String role, String linkedEntityId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.linkedEntityId = linkedEntityId;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLinkedEntityId() {
        return linkedEntityId;
    }

    public void setLinkedEntityId(String linkedEntityId) {
        this.linkedEntityId = linkedEntityId;
    }
}
