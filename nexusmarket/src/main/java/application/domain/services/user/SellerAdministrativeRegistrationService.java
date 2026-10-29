package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 2.1 Manages administrative onboarding of Marketplace Sellers.
 */
public class SellerAdministrativeRegistrationService {

    public Seller registerSeller(User admin, String fullName, Email email, IdentityDocument idDoc, LocalDate onboardingDate) {
        Objects.requireNonNull(admin, "Admin user must not be null");
        if (admin.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Solo un usuario con rol ADMINISTRATOR puede registrar vendedores");
        }
        LocalDate date = onboardingDate != null ? onboardingDate : LocalDate.now();
        return Seller.onboard(admin, fullName, email, idDoc, date);
    }
}
