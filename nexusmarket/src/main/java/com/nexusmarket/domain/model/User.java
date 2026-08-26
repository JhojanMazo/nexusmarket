package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;

import java.util.Objects;
import java.util.Set;

/**
 * Base entity for authentication and identification across the Marketplace.
 * Business rule (BR-02): every user has exactly one role in the system.
 *
 * <p><b>Design note (roles vs. entities):</b> only BUYER and SELLER carry
 * additional attributes and invariants in this specification — addresses and
 * commercial status for a buyer; trade name, onboarding date and warehouses
 * for a seller. Those two roles are therefore modeled as dedicated subclasses:
 * {@link Buyer} and {@link Seller}.
 *
 * ADMINISTRATOR, LOGISTICS_OPERATOR and SUPERVISOR add no extra state or
 * behavior beyond what already exists on User — they are simply a User
 * <i>playing that role</i>, not a distinct kind of thing in the domain. Modeling
 * them as three more subclasses would only add type-hierarchy weight without
 * expressing any additional business rule, so this class is concrete and
 * those three roles are created directly through {@link #createStaff}.
 */
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

    /**
     * Constructor for internal use by this class and by the Buyer/Seller
     * subclasses, which are responsible for supplying their fixed role.
     */
    protected User(String userId, String fullName, Email email, IdentityDocument identityDocument,
                    UserRole role, UserStatus status) {
        this.userId = requireNotBlank(userId, "userId");
        this.fullName = requireNotBlank(fullName, "fullName");
        this.email = Objects.requireNonNull(email, "email is required");
        this.identityDocument = Objects.requireNonNull(identityDocument, "identityDocument is required");
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");
    }

    /**
     * Creates a staff user — ADMINISTRATOR, LOGISTICS_OPERATOR or SUPERVISOR.
     * These roles have no dedicated subclass (see class-level note); use the
     * {@link Buyer} or {@link Seller} constructors for those roles instead.
     */
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
