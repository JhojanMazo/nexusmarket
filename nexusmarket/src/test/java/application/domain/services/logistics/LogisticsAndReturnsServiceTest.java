package application.domain.services.logistics;

import application.domain.services.user.*;
import application.domain.services.product.*;
import application.domain.services.order.*;
import application.domain.services.billing.*;
import application.domain.services.inventory.*;
import application.domain.services.logistics.*;
import application.domain.services.authorization.*;

import application.domain.enums.*;
import application.domain.models.*;
import application.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsAndReturnsServiceTest {

    private LogisticsShippingManagementService shippingService;
    private ReturnsAdministrationService returnsService;
    private RefundsManagementService refundsService;

    private User logisticsOperator;
    private Order order;
    private Buyer buyer;

    @BeforeEach
    void setUp() {
        shippingService = new LogisticsShippingManagementService();
        returnsService = new ReturnsAdministrationService();
        refundsService = new RefundsManagementService();

        logisticsOperator = User.createStaff(
                "Operador Logistico 1",
                new Email("logistics@nexusmarket.com"),
                new IdentityDocument("CC", "50607080"),
                UserRole.LOGISTICS_OPERATOR
        );

        buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Felipe Arango",
                new Email("felipe@example.com"),
                new IdentityDocument("CC", "12309845"),
                new Address("Calle 30", "Envigado", "Antioquia", "Colombia", "055420", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );

        Product product = new Product(
                UUID.randomUUID(),
                "Drone 4K",
                new ProductVariant("Color", "Gris"),
                new Money(new BigDecimal("600.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        order = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("600.00"), Currency.getInstance("USD")))),
                buyer.getPrimaryAddress(),
                OrderStatus.PAID,
                LocalDate.now()
        );
    }

    @Test
    void coordinateShipping_PaidOrder_DispatchesSuccessfully() {
        Shipment shipment = shippingService.coordinateShipping(logisticsOperator, order);

        assertNotNull(shipment);
        assertEquals(Shipment.ShipmentStatus.DISPATCHED, shipment.getStatus());
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void coordinateShipping_OrderNotPaid_ThrowsIllegalStateException() {
        Order unpaidOrder = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                order.getItems(),
                buyer.getPrimaryAddress(),
                OrderStatus.CART,
                LocalDate.now()
        );

        assertThrows(IllegalStateException.class, () -> shippingService.coordinateShipping(logisticsOperator, unpaidOrder));
    }

    @Test
    void initiateReturnAndProcessRefund_FullFlow_Success() {
        // Entregar la orden
        shippingService.coordinateShipping(logisticsOperator, order);
        order.advanceTo(OrderStatus.DELIVERED_COMPLETED);

        // Iniciar retorno
        ReturnRequest returnRequest = returnsService.initiateReturn(order);
        assertEquals(ReturnRequest.ReturnRequestStatus.REQUESTED, returnRequest.getStatus());

        // Aprobar retorno
        returnsService.approveReturn(returnRequest);
        assertEquals(ReturnRequest.ReturnRequestStatus.APPROVED, returnRequest.getStatus());

        // Procesar reembolso
        Refund refund = refundsService.processRefund(returnRequest, order.total());
        assertNotNull(refund);
        assertEquals(Refund.RefundStatus.PROCESSED, refund.getStatus());
        assertEquals(order.total(), refund.getAmount());
    }
}
