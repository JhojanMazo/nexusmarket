package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a Seller.
 *
 * FR‑005: A Seller must be created with a non‑null onboarding date.
 * FR‑006: Only a user with the ADMINISTRATOR role can onboard a Seller.
 */
public class Seller extends User {
    private final LocalDate onboardingDate;

    private Seller(UUID userId,
                   String fullName,
                   Email email,
                   IdentityDocument identityDocument,
                   LocalDate onboardingDate,
                   UserStatus status) {
        super(userId, fullName, email, identityDocument, UserRole.SELLER, status);
        this.onboardingDate = Objects.requireNonNull(onboardingDate);
    }

    /**
     * Factory method for onboarding a Seller.
     *
     * @param admin the administrator performing the onboarding
     * @param fullName seller's full name
     * @param email seller's email
     * @param idDoc seller's identity document
     * @param onboardingDate date of onboarding (must be non‑null)
     * @return a new Seller instance
     * @throws IllegalArgumentException if admin is not an ADMINISTRATOR or any argument is null
     */
    public static Seller onboard(User admin,
                                 String fullName,
                                 Email email,
                                 IdentityDocument idDoc,
                                 LocalDate onboardingDate) {
        Objects.requireNonNull(admin, "Admin user must not be null");
        if (admin.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Only ADMINISTRATOR can onboard a Seller");
        }
        Objects.requireNonNull(fullName, "Full name must not be null");
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(idDoc, "IdentityDocument must not be null");
        Objects.requireNonNull(onboardingDate, "Onboarding date must not be null");
        return new Seller(UUID.randomUUID(), fullName, email, idDoc, onboardingDate, UserStatus.ACTIVE);
    }

    public static Seller create(UUID adminUserId,
                                String fullName,
                                Email email,
                                IdentityDocument idDoc,
                                LocalDate onboardingDate) {
        Objects.requireNonNull(fullName, "Full name must not be null");
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(idDoc, "IdentityDocument must not be null");
        Objects.requireNonNull(onboardingDate, "Onboarding date must not be null");
        return new Seller(UUID.randomUUID(), fullName, email, idDoc, onboardingDate, UserStatus.ACTIVE);
    }

    public static Seller reconstitute(UUID userId,
                                      String fullName,
                                      Email email,
                                      IdentityDocument idDoc,
                                      LocalDate onboardingDate,
                                      UserStatus status) {
        return new Seller(userId, fullName, email, idDoc, onboardingDate, status);
    }

    public LocalDate getOnboardingDate() {
        return onboardingDate;
    }
}
