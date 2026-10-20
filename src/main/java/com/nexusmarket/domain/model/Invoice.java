package com.nexusmarket.domain.model;

import com.nexusmarket.domain.valueobject.Money;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable invoice for an {@link Order}.
 *
 * FR‑020: total = subtotal + tax.
 */
public final class Invoice {
    private final UUID invoiceId;
    private final Order order;
    private final LocalDate issueDate;
    private final Money subtotal;
    private final Money tax;
    private final Money total;

    public Invoice(UUID invoiceId, Order order, LocalDate issueDate, Money subtotal, Money tax) {
        this.invoiceId = Objects.requireNonNull(invoiceId);
        this.order = Objects.requireNonNull(order);
        this.issueDate = Objects.requireNonNull(issueDate);
        this.subtotal = Objects.requireNonNull(subtotal);
        this.tax = Objects.requireNonNull(tax);
        // total must be sum of subtotal and tax, same currency
        this.total = subtotal.add(tax);
    }

    public UUID getInvoiceId() { return invoiceId; }
    public Order getOrder() { return order; }
    public LocalDate getIssueDate() { return issueDate; }
    public Money getSubtotal() { return subtotal; }
    public Money getTax() { return tax; }
    public Money getTotal() { return total; }
}
