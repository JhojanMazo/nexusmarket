package application.adapters.persistence.jpa.entities;

import application.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA aggregate root for the {@code orders} table.
 *
 * <p>Owns the {@code order_items} rows through a bidirectional one-to-many association:
 * every persist/merge/remove cascades to the items and any item detached from the collection
 * is deleted ({@code orphanRemoval}). The collection must only be modified through
 * {@link #addItem(OrderItemJpaEntity)} / {@link #removeItem(OrderItemJpaEntity)} so both sides
 * of the relationship (and therefore the {@code order_id} FK) stay in sync.</p>
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id", nullable = false)
    private BuyerEntity buyer;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "delivery_street", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_city", nullable = false)),
            @AttributeOverride(name = "stateOrProvince", column = @Column(name = "delivery_state_or_province", nullable = false)),
            @AttributeOverride(name = "country", column = @Column(name = "delivery_country", nullable = false)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "delivery_postal_code", nullable = false)),
            @AttributeOverride(name = "reference", column = @Column(name = "delivery_reference"))
    })
    private AddressEmbeddable deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    /*
     * EAGER is kept on purpose: other aggregates (Invoice, Shipment, ReturnRequest) map their
     * Order outside of a persistence context (open-in-view=false), so the items must be loaded
     * together with the order. No setter is exposed: replacing the collection instance would
     * break orphanRemoval tracking.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    /** Read-only view of the items; use {@link #addItem}/{@link #removeItem} to modify it. */
    public List<OrderItemJpaEntity> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Adds an item and sets the owning side of the relationship ({@code item.order = this}). */
    public void addItem(OrderItemJpaEntity item) {
        Objects.requireNonNull(item, "Order item must not be null");
        items.add(item);
        item.setOrder(this);
    }

    /** Removes an item and clears the owning side; {@code orphanRemoval} deletes the row on flush. */
    public void removeItem(OrderItemJpaEntity item) {
        Objects.requireNonNull(item, "Order item must not be null");
        if (items.remove(item)) {
            item.setOrder(null);
        }
    }
}
