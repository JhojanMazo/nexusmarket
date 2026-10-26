package application.domain.services.billing;

import application.domain.enums.OrderStatus;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.valueobjects.Money;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 6.1 Manages tax and commercial invoicing (FR-020).
 */
@Service
public class BillingAdministrationService {

    public Invoice generateInvoice(Order order, Money tax) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(tax, "Tax must not be null");

        if (order.getStatus() == OrderStatus.CART) {
            throw new IllegalStateException("No se puede emitir factura para una orden en estado CART");
        }

        return new Invoice(
                UUID.randomUUID(),
                order,
                LocalDate.now(),
                order.subtotal(),
                tax
        );
    }
}
