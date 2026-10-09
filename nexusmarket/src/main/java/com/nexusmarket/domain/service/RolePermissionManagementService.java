package com.nexusmarket.domain.service;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.model.User;

public class RolePermissionManagementService {

    public void assignRoleToUser(User user, com.nexusmarket.domain.enums.UserRole newRole) {
        if (user == null || newRole == null) {
            throw new IllegalArgumentException("User and role are required.");
        }
        // Cannot set role directly on User. Or we can add setRole. Wait, User has final UserRole. Let's check.
    }

    public void validateActionPermission(User user, String requiredAction) {
        if (user.getRole() == null || !user.getRole().hasPermission(requiredAction)) {
            throw new SecurityException("Insufficient permissions for action: " + requiredAction);
        }
    }
}