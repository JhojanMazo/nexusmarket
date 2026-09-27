package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.User;
import com.nexusmarket.domain.repository.UserRepository;

public class AuthenticationAccessControlService {

    private final UserRepository userRepository;

    public AuthenticationAccessControlService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!user.isActive()) {
            throw new IllegalStateException("Access denied: Account is inactive.");
        }

        if (!verifyPassword(rawPassword, user.getEncryptedPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        return user;
    }

    private boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return rawPassword.equals(encryptedPassword);
    }
}