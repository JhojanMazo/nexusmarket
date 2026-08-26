package com.nexusmarket.domain.model;

import com.nexusmarket.domain.valueobject.Money;

import java.time.LocalDate;
import java.util.Objects;

/** Commercial record associated with a sale, generated from a paid Order. */
public class Invoice {

    private final String invoiceId;
    private final Order order;
    private final LocalDate issueDate;
    private final Money subtotal;
    private final Money tax;
    private final Money total;

    public Invoice(String invoiceId, Order order, LocalDate issueDate, Money subtotal, Money tax) {
        this.invoiceId = requireNotBlank(invoiceId, "invoiceId");
        this.order = Objects.requireNonNull(order, "order is required");
        this.issueDate = Objects.requireNonNull(issueDate, "issueDate is required");
        this.subtotal = Objects.requireNonNull(subtotal, "subtotal is required");
        this.tax = Objects.requireNonNull(tax, "tax is required");
        this.total = subtotal.add(tax);
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public String getInvoiceId() { return invoiceId; }
    public Order getOrder() { return order; }
    public LocalDate getIssueDate() { return issueDate; }
    public Money getSubtotal() { return subtotal; }
    public Money getTax() { return tax; }
    public Money getTotal() { return total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        return invoiceId.equals(((Invoice) o).invoiceId);
    }

    @Override
    public int hashCode() { return Objects.hash(invoiceId); }
}
