package application.domain.models;

import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import java.util.Objects;
import java.util.UUID;

/**
 * Base user entity. Represents a system user with a fixed role.
 * Immutable identifier; mutable fullName, email, status.
 */
public abstract class User {
    private final UUID userId;
    private String fullName;
    private Email email;
    private final IdentityDocument identityDocument;
    private final UserRole role;
    private UserStatus status;
    private String passwordHash;

    protected User(UUID userId, String fullName, Email email, IdentityDocument identityDocument, UserRole role, UserStatus status, String passwordHash) {
        if (userId == null || fullName == null || email == null || identityDocument == null || role == null || status == null) {
            throw new IllegalArgumentException("User fields must not be null");
        }
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.identityDocument = identityDocument;
        this.role = role;
        this.status = status;
        this.passwordHash = passwordHash;
    }

    protected User(UUID userId, String fullName, Email email, IdentityDocument identityDocument, UserRole role, UserStatus status) {
        this(userId, fullName, email, identityDocument, role, status, null);
    }

    /**
     * Factory for staff users (ADMINISTRATOR, LOGISTICS_OPERATOR, SUPERVISOR).
     * FR‑001: exactly one UserRole per user, fixed at creation.
     * FR‑002: creation of staff with role BUYER or SELLER is prohibited.
     */
    public static User createStaff(String fullName, Email email, IdentityDocument idDoc, UserRole role, String passwordHash) {
        if (role == UserRole.BUYER || role == UserRole.SELLER) {
            throw new IllegalArgumentException("Staff users cannot have BUYER or SELLER role");
        }
        return new StaffUser(UUID.randomUUID(), fullName, email, idDoc, role, UserStatus.ACTIVE, passwordHash);
    }

    public static User createStaff(String fullName, Email email, IdentityDocument idDoc, UserRole role) {
        return createStaff(fullName, email, idDoc, role, null);
    }

    public static User reconstituteStaff(UUID userId, String fullName, Email email, IdentityDocument idDoc, UserRole role, UserStatus status, String passwordHash) {
        return new StaffUser(userId, fullName, email, idDoc, role, status, passwordHash);
    }

    public static User reconstituteStaff(UUID userId, String fullName, Email email, IdentityDocument idDoc, UserRole role, UserStatus status) {
        return reconstituteStaff(userId, fullName, email, idDoc, role, status, null);
    }

    public UUID getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = Objects.requireNonNull(fullName); }
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = Objects.requireNonNull(email); }
    public IdentityDocument getIdentityDocument() { return identityDocument; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = Objects.requireNonNull(status); }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // Internal concrete class for staff users; not exported.
    private static class StaffUser extends User {
        private StaffUser(UUID userId, String fullName, Email email, IdentityDocument idDoc, UserRole role, UserStatus status, String passwordHash) {
            super(userId, fullName, email, idDoc, role, status, passwordHash);
        }
    }
}
