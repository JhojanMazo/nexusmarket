package application.adapters.persistence.jpa.entities;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.adapters.persistence.jpa.entities.EmailConverter;
import application.adapters.persistence.jpa.entities.IdentityDocumentConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

/**
 * Base JPA entity for all user types. Uses single‑table inheritance with a discriminator based on {@link UserRole}.
 * Domain business rules (role immutability, validation) are enforced in domain services, not by the mapping.
 */
@Entity(name = "UserEntity")
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Convert(converter = EmailConverter.class)
    @Column(name = "email", nullable = false, unique = true)
    private Email email;

    @Convert(converter = IdentityDocumentConverter.class)
    @Column(name = "identity_document", nullable = false, unique = true)
    private IdentityDocument identityDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, insertable = false, updatable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "password_hash", nullable = true, length = 255)
    private String passwordHash;

    public UserEntity(UUID userId, String fullName, Email email, IdentityDocument identityDocument, UserRole role, UserStatus status) {
        this(userId, fullName, email, identityDocument, role, status, null);
    }
}
