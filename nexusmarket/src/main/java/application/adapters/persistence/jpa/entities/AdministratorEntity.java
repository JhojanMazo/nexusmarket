package application.adapters.persistence.jpa.entities;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "AdministratorEntity")
@DiscriminatorValue("ADMINISTRATOR")
@NoArgsConstructor
public class AdministratorEntity extends UserEntity {

    public AdministratorEntity(UUID userId, String fullName, Email email, IdentityDocument identityDocument, UserStatus status) {
        super(userId, fullName, email, identityDocument, UserRole.ADMINISTRATOR, status);
    }
}
