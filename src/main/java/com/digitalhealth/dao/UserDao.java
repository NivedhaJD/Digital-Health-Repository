package com.digitalhealth.dao;

import com.digitalhealth.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity.
 */
public interface UserDao {
    void save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void update(User user);
    void delete(String userId);
    boolean existsByUsername(String username);
}
