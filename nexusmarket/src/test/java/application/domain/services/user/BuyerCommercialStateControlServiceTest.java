package application.domain.services.user;

import application.domain.services.user.*;
import application.domain.services.product.*;
import application.domain.services.order.*;
import application.domain.services.billing.*;
import application.domain.services.inventory.*;
import application.domain.services.logistics.*;
import application.domain.services.authorization.*;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BuyerCommercialStateControlServiceTest {

    private BuyerCommercialStateControlService service;
    private Buyer buyer;

    @BeforeEach
    void setUp() {
        service = new BuyerCommercialStateControlService();
        buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Laura Sanchez",
                new Email("laura@example.com"),
                new IdentityDocument("CC", "99887766"),
                new Address("Calle 100", "Medellin", "Antioquia", "Colombia", "05001", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );
    }

    @Test
    void canTransact_ActiveAndEnabled_ReturnsTrue() {
        assertTrue(service.canTransact(buyer));
    }

    @Test
    void canTransact_BlockedUser_ReturnsFalse() {
        buyer.setStatus(UserStatus.BLOCKED);
        assertFalse(service.canTransact(buyer));
    }

    @Test
    void canTransact_SuspendedCommercialStatus_ReturnsFalse() {
        buyer.setCommercialStatus(BuyerCommercialStatus.SUSPENDED);
        assertFalse(service.canTransact(buyer));
    }

    @Test
    void verifyAndEnableTransactions_Success() {
        buyer.setCommercialStatus(BuyerCommercialStatus.SUSPENDED);
        service.verifyAndEnableTransactions(buyer);
        assertEquals(BuyerCommercialStatus.ENABLED, buyer.getCommercialStatus());
    }
}
