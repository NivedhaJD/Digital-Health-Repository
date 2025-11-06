package com.digitalhealth.service;

import com.digitalhealth.dao.UserDao;
import com.digitalhealth.dto.LoginRequestDTO;
import com.digitalhealth.dto.LoginResponseDTO;
import com.digitalhealth.dto.UserDTO;
import com.digitalhealth.exception.ValidationException;
import com.digitalhealth.model.User;
import com.digitalhealth.model.UserRole;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for authentication and user management.
 */
public class AuthService {
    private final UserDao userDao;
    private int userCounter = 1;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
        initializeUserCounter();
    }

    private void initializeUserCounter() {
        userDao.findAll().stream()
                .map(User::getUserId)
                .filter(id -> id.startsWith("U"))
                .map(id -> id.substring(1))
                .mapToInt(Integer::parseInt)
                .max()
                .ifPresent(max -> userCounter = max + 1);
    }

    /**
     * Register a new user.
     */
    public String register(UserDTO userDTO) throws ValidationException {
        // Validate input
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (userDTO.getRole() == null) {
            throw new ValidationException("Role is required");
        }

        // Check if username already exists
        if (userDao.existsByUsername(userDTO.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        // Create new user
        String userId = generateUserId();
        User user = new User(
                userId,
                userDTO.getUsername(),
                hashPassword(userDTO.getPassword()),
                UserRole.valueOf(userDTO.getRole().toUpperCase()),
                userDTO.getLinkedEntityId()
        );

        userDao.save(user);
        return userId;
    }

    /**
     * Login user and return token.
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) throws ValidationException {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new ValidationException("Username and password are required");
        }

        Optional<User> userOpt = userDao.findByUsername(loginRequest.getUsername());
        if (!userOpt.isPresent()) {
            throw new ValidationException("Invalid username or password");
        }

        User user = userOpt.get();
        String hashedPassword = hashPassword(loginRequest.getPassword());
        
        if (!user.getPasswordHash().equals(hashedPassword)) {
            throw new ValidationException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new ValidationException("Account is inactive");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userDao.update(user);

        // Generate token
        String token = generateToken(user);

        return new LoginResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRole().toString(),
                token,
                user.getLinkedEntityId()
        );
    }

    /**
     * Validate token and return user info.
     */
    public LoginResponseDTO validateToken(String token) throws ValidationException {
        if (token == null || token.isEmpty()) {
            throw new ValidationException("Token is required");
        }

        // Simple token format: userId:timestamp:uuid
        String[] parts = token.split(":");
        if (parts.length != 3) {
            throw new ValidationException("Invalid token format");
        }

        String userId = parts[0];
        Optional<User> userOpt = userDao.findById(userId);
        
        if (!userOpt.isPresent()) {
            throw new ValidationException("Invalid token");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            throw new ValidationException("Account is inactive");
        }

        return new LoginResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRole().toString(),
                token,
                user.getLinkedEntityId()
        );
    }

    private String generateUserId() {
        return String.format("U%04d", userCounter++);
    }

    private String generateToken(User user) {
        return user.getUserId() + ":" + System.currentTimeMillis() + ":" + UUID.randomUUID().toString();
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
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
