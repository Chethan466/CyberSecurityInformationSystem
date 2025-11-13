package com.cybersecurity.cybersecurity_info_system.service;

import com.cybersecurity.cybersecurity_info_system.entity.User;
import com.cybersecurity.cybersecurity_info_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Registers a new user.
     * - Blocks ADMIN role (only Admin can promote)
     * - Defaults to VIEWER if role is null
     * - Hashes password with BCrypt
     * @param user User object from form
     * @return true if registered, false if username/email taken or invalid role
     */
    public boolean registerUser(User user) {

        // === 1. Validate username & email ===
        if (user == null || user.getUsername() == null || user.getEmail() == null) {
            return false;
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            return false; // Username already exists
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return false; // Email already exists
        }

        // === 2. Block ADMIN role in public registration ===
        if (user.getRole() == User.Role.ADMIN) {
            return false; // Only Admin can assign ADMIN role
        }

        // === 3. Set default role if null (safety) ===
        if (user.getRole() == null) {
            user.setRole(User.Role.VIEWER);
        }

        // === 4. Hash password (null-safe) ===
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            return false; // Password required
        }

        // === 5. Save to database ===
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            // Log in production
            return false;
        }
    }
}