package application.adapters.persistence.jpa.entities;

import application.domain.models.Shipment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentJpaEntity {

    @Id
    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderJpaEntity order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", nullable = false)
    private UserEntity logisticsOperator;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Shipment.ShipmentStatus status;

    public ShipmentJpaEntity(UUID shipmentId, OrderJpaEntity order,
                             UserEntity logisticsOperator, Shipment.ShipmentStatus status) {
        this.shipmentId = shipmentId;
        this.order = order;
        this.logisticsOperator = logisticsOperator;
        this.status = status;
    }
}
