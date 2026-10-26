package application.domain.services.order;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.services.order.CompletedOrderImmutabilityService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompletedOrderImmutabilityServiceTest {

    private CompletedOrderImmutabilityService service;
    private Order activeOrder;
    private Order deliveredOrder;

    @BeforeEach
    void setUp() {
        service = new CompletedOrderImmutabilityService();
        Address addr = new Address("Calle 100", "Bogota", "Cundinamarca", "Colombia", "110111", null);
        Buyer buyer = Buyer.create("Test Buyer", new Email("buyer@test.com"), new IdentityDocument("CC", "998877"), addr, Collections.emptyList(), BuyerCommercialStatus.ENABLED);

        activeOrder = Order.reconstitute(UUID.randomUUID(), buyer, Collections.emptyList(), addr, OrderStatus.PAID, LocalDate.now());
        deliveredOrder = Order.reconstitute(UUID.randomUUID(), buyer, Collections.emptyList(), addr, OrderStatus.DELIVERED_COMPLETED, LocalDate.now());
    }

    @Test
    void shouldPassForActiveOrders() {
        assertDoesNotThrow(() -> service.ensureImmutability(activeOrder));
    }

    @Test
    void shouldThrowWhenOrderIsDeliveredAndCompleted() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.ensureImmutability(deliveredOrder)
        );
        assertTrue(ex.getMessage().contains("estrictamente inmutable"));
    }
}
