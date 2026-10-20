package com.nexusmarket.infrastructure.jpa.entity;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;
import com.nexusmarket.infrastructure.jpa.converter.EmailConverter;
import com.nexusmarket.infrastructure.jpa.converter.IdentityDocumentConverter;
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
public abstract class UserEntity {

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
}
