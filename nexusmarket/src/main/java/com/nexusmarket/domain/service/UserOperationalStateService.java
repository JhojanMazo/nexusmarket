package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.User;
import com.nexusmarket.domain.enums.UserStatus;

public class UserOperationalStateService {

    public void blockUser(User user, String reason) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("User is already blocked.");
        }
        user.setStatus(UserStatus.BLOCKED);
        user.setBlockReason(reason);
    }

    public void activateUser(User user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active.");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setBlockReason(null);
    }
}