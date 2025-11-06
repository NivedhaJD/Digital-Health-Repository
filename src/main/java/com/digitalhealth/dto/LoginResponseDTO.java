package com.digitalhealth.dto;

/**
 * DTO for login response containing user info and token.
 */
public class LoginResponseDTO {
    private String userId;
    private String username;
    private String role;
    private String token;
    private String linkedEntityId;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String userId, String username, String role, String token, String linkedEntityId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.token = token;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLinkedEntityId() {
        return linkedEntityId;
    }

    public void setLinkedEntityId(String linkedEntityId) {
        this.linkedEntityId = linkedEntityId;
    }
}
