package application.adapters.persistence.jpa.entities;

import application.domain.enums.InventoryMovementType;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementJpaEntity {

    @Id
    @Column(name = "movement_id", nullable = false)
    private UUID movementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryJpaEntity inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private InventoryMovementType type;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "reference", length = 255)
    private String reference;

    public InventoryMovementJpaEntity(UUID movementId, InventoryJpaEntity inventory,
                                      InventoryMovementType type, int quantity,
                                      Instant date, String reference) {
        this.movementId = movementId;
        this.inventory = inventory;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.reference = reference;
    }
}
