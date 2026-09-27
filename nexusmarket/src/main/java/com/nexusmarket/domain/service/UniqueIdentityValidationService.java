package com.nexusmarket.domain.service;

import com.nexusmarket.domain.repository.UserRepository;

public class UniqueIdentityValidationService {

    private final UserRepository userRepository;

    public UniqueIdentityValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateUniqueIdentity(String identityDocument, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Integrity violation: Email address is already registered.");
        }

        if (userRepository.existsByIdentityDocument(identityDocument)) {
            throw new IllegalStateException("Integrity violation: Identity document is already registered to another account.");
        }
    }
}