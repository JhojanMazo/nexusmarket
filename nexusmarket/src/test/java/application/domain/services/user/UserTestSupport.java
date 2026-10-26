package application.domain.services.user;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

public final class UserTestSupport {

    private UserTestSupport() {
    }

    public static Buyer createTestBuyer(String email) {
        Address address = new Address("Calle 10", "Medellin", "Antioquia", "Colombia", "05001", "Apt 101");
        return Buyer.create(
                "Test Buyer",
                new Email(email != null ? email : "buyer@test.com"),
                new IdentityDocument("CC", "10203040"),
                address,
                Collections.emptyList(),
                BuyerCommercialStatus.ENABLED
        );
    }

    public static User createTestStaff(UserRole role) {
        return User.createStaff(
                "Test Staff",
                new Email("staff_" + UUID.randomUUID().toString().substring(0, 6) + "@test.com"),
                new IdentityDocument("CC", "998877"),
                role
        );
    }

    public static Seller createTestSeller() {
        return Seller.create(
                UUID.randomUUID(),
                "Test Seller Co",
                new Email("seller@test.com"),
                new IdentityDocument("NIT", "900998877"),
                LocalDate.now()
        );
    }
}
