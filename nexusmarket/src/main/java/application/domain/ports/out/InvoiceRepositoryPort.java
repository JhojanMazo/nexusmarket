package application.domain.ports.out;

import application.domain.models.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Invoice aggregates.
 */
public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID invoiceId);
    Optional<Invoice> findByOrderId(UUID orderId);
    List<Invoice> findAll();
}
