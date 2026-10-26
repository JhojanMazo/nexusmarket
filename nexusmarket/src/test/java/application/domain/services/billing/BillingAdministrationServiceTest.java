package application.domain.services.billing;

import application.domain.services.user.*;
import application.domain.services.product.*;
import application.domain.services.order.*;
import application.domain.services.billing.*;
import application.domain.services.inventory.*;
import application.domain.services.logistics.*;
import application.domain.services.authorization.*;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BillingAdministrationServiceTest {

    private BillingAdministrationService service;
    private Order paidOrder;
    private Order cartOrder;

    @BeforeEach
    void setUp() {
        service = new BillingAdministrationService();

        Buyer buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Valeria Mejia",
                new Email("valeria@example.com"),
                new IdentityDocument("CC", "11223344"),
                new Address("Transversal 5", "Medellin", "Antioquia", "Colombia", "05001", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );

        Product product = new Product(
                UUID.randomUUID(),
                "Televisor 55 OLED",
                new ProductVariant("Resolucion", "4K"),
                new Money(new BigDecimal("1000.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        paidOrder = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("1000.00"), Currency.getInstance("USD")))),
                buyer.getPrimaryAddress(),
                OrderStatus.PAID,
                LocalDate.now()
        );

        cartOrder = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("1000.00"), Currency.getInstance("USD")))),
                buyer.getPrimaryAddress(),
                OrderStatus.CART,
                LocalDate.now()
        );
    }

    @Test
    void generateInvoice_PaidOrder_CalculatesSubtotalTaxAndTotal() {
        Money tax = new Money(new BigDecimal("190.00"), Currency.getInstance("USD"));

        Invoice invoice = service.generateInvoice(paidOrder, tax);

        assertNotNull(invoice);
        assertEquals(paidOrder, invoice.getOrder());
        assertEquals(new BigDecimal("1000.00"), invoice.getSubtotal().amount());
        assertEquals(new BigDecimal("190.00"), invoice.getTax().amount());
        assertEquals(new BigDecimal("1190.00"), invoice.getTotal().amount());
    }

    @Test
    void generateInvoice_CartOrder_ThrowsIllegalStateException() {
        Money tax = new Money(new BigDecimal("190.00"), Currency.getInstance("USD"));

        assertThrows(IllegalStateException.class, () -> service.generateInvoice(cartOrder, tax));
    }
}
