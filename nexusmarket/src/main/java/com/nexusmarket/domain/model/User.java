package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;

import java.util.Objects;
import java.util.Set;

public class User {

    private static final Set<UserRole> STAFF_ROLES = Set.of(
            UserRole.ADMINISTRATOR, UserRole.LOGISTICS_OPERATOR, UserRole.SUPERVISOR
    );

    private final String userId;
    private String fullName;
    private Email email;
    private IdentityDocument identityDocument;
    private final UserRole role;
    private UserStatus status;

    protected User(String userId, String fullName, Email email, IdentityDocument identityDocument,
                    UserRole role, UserStatus status) {
        this.userId = requireNotBlank(userId, "userId");
        this.fullName = requireNotBlank(fullName, "fullName");
        this.email = Objects.requireNonNull(email, "email is required");
        this.identityDocument = Objects.requireNonNull(identityDocument, "identityDocument is required");
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public static User createStaff(String userId, String fullName, Email email,
                                    IdentityDocument identityDocument, UserRole role, UserStatus status) {
        Objects.requireNonNull(role, "role is required");
        if (!STAFF_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "createStaff only accepts ADMINISTRATOR, LOGISTICS_OPERATOR or SUPERVISOR; "
                            + role + " requires its dedicated subclass (Buyer/Seller).");
        }
        return new User(userId, fullName, email, identityDocument, role, status);
    }

    protected static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public Email getEmail() { return email; }
    public IdentityDocument getIdentityDocument() { return identityDocument; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }

    public void setFullName(String fullName) {
        this.fullName = requireNotBlank(fullName, "fullName");
    }

    public void setEmail(Email email) {
        this.email = Objects.requireNonNull(email, "email is required");
    }

    public void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return userId.equals(((User) o).userId);
    }

    @Override
    public int hashCode() { return Objects.hash(userId); }
}
