package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.User;
import com.nexusmarket.domain.repository.UserRepository;

public class UserInformationManagementService {

    private final UserRepository userRepository;
    private final UniqueIdentityValidationService identityValidationService;

    public UserInformationManagementService(UserRepository userRepository, UniqueIdentityValidationService identityValidationService) {
        this.userRepository = userRepository;
        this.identityValidationService = identityValidationService;
    }

    public User registerNewUser(User user) {
        identityValidationService.validateUniqueIdentity(user.getIdentityDocument(), user.getEmail());
        return userRepository.save(user);
    }

    public User updateUserInfo(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Invalid user for update.");
        }
        return userRepository.save(user);
    }
}