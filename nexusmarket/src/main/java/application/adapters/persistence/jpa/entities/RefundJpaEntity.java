package application.adapters.persistence.jpa.entities;

import application.domain.models.Refund;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
public class RefundJpaEntity {

    @Id
    @Column(name = "refund_id", nullable = false)
    private UUID refundId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "return_request_id", nullable = false, unique = true)
    private ReturnRequestJpaEntity returnRequest;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Refund.RefundStatus status;

    public RefundJpaEntity(UUID refundId, ReturnRequestJpaEntity returnRequest,
                           BigDecimal amount, String currencyCode, Refund.RefundStatus status) {
        this.refundId = refundId;
        this.returnRequest = returnRequest;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.status = status;
    }
}
