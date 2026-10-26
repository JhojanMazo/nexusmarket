package application.adapters.persistence.jpa.entities;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA representation of a Seller user.
 */
@Entity(name = "SellerEntity")
@DiscriminatorValue("SELLER")
@Getter
@Setter
@NoArgsConstructor
public class SellerEntity extends UserEntity {

    @Column(name = "onboarding_date")
    private LocalDate onboardingDate;

    public SellerEntity(UUID userId, String fullName, Email email, IdentityDocument identityDocument,
                        UserStatus status, LocalDate onboardingDate) {
        super(userId, fullName, email, identityDocument, UserRole.SELLER, status);
        this.onboardingDate = onboardingDate;
    }
}
