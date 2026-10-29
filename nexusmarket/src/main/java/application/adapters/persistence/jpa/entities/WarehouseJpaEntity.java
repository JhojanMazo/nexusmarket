package application.adapters.persistence.jpa.entities;

import application.domain.enums.WarehouseType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Embedded
    private AddressEmbeddable address;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private WarehouseType type;

    @Column(name = "seller_id")
    private UUID sellerId;

    @Column(name = "capacity", nullable = false)
    private double capacity;

    public WarehouseJpaEntity(UUID warehouseId, String name, AddressEmbeddable address,
                              WarehouseType type, UUID sellerId, double capacity) {
        this.warehouseId = warehouseId;
        this.name = name;
        this.address = address;
        this.type = type;
        this.sellerId = sellerId;
        this.capacity = capacity;
    }
}
