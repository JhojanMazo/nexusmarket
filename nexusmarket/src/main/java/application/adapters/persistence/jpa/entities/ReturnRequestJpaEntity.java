package application.adapters.persistence.jpa.entities;

import application.domain.models.ReturnRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequestJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReturnRequest.ReturnRequestStatus status;

    public ReturnRequestJpaEntity(UUID requestId, OrderJpaEntity order, ReturnRequest.ReturnRequestStatus status) {
        this.requestId = requestId;
        this.order = order;
        this.status = status;
    }
}
