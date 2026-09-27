package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Role;
import com.nexusmarket.domain.entity.User;

public class RolePermissionManagementService {

    public void assignRoleToUser(User user, Role newRole) {
        if (user == null || newRole == null) {
            throw new IllegalArgumentException("User and role are required.");
        }
        user.setRole(newRole);
    }

    public void validateActionPermission(User user, String requiredAction) {
        if (user.getRole() == null || !user.getRole().hasPermission(requiredAction)) {
            throw new SecurityException("Insufficient permissions for action: " + requiredAction);
        }
    }
}