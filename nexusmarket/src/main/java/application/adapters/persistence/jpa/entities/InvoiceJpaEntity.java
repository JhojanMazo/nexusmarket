package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceJpaEntity {

    @Id
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderJpaEntity order;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    public InvoiceJpaEntity(UUID invoiceId, OrderJpaEntity order, LocalDate issueDate,
                            BigDecimal subtotalAmount, BigDecimal taxAmount, BigDecimal totalAmount,
                            String currencyCode) {
        this.invoiceId = invoiceId;
        this.order = order;
        this.issueDate = issueDate;
        this.subtotalAmount = subtotalAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.currencyCode = currencyCode;
    }
}
