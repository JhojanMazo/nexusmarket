package application.domain.services.billing;

import application.domain.models.Invoice;
import application.domain.ports.out.InvoiceRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInvoiceRepository implements InvoiceRepositoryPort {

    private final Map<UUID, Invoice> storage = new ConcurrentHashMap<>();

    @Override
    public Invoice save(Invoice invoice) {
        storage.put(invoice.getInvoiceId(), invoice);
        return invoice;
    }

    @Override
    public Optional<Invoice> findById(UUID invoiceId) {
        return Optional.ofNullable(storage.get(invoiceId));
    }

    @Override
    public Optional<Invoice> findByOrderId(UUID orderId) {
        return storage.values().stream()
                .filter(i -> i.getOrder() != null && i.getOrder().getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public List<Invoice> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}
