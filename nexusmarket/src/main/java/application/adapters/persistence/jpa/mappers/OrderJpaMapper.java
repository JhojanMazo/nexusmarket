package application.adapters.persistence.jpa.mappers;

import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.adapters.persistence.jpa.entities.*;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Translates between the {@link Order} aggregate (pure domain, framework-free) and its relational
 * representation ({@link OrderJpaEntity} + {@link OrderItemJpaEntity} rows in {@code order_items}).
 *
 * <p>Product rows are never created from here: the caller supplies a {@code productResolver}
 * that returns the {@link ProductJpaEntity} referenced by each item (typically a managed entity
 * loaded inside the current transaction).</p>
 */
@Component
public class OrderJpaMapper {

    private final UserJpaMapper userJpaMapper;
    private final ProductJpaMapper productJpaMapper;

    public OrderJpaMapper(UserJpaMapper userJpaMapper, ProductJpaMapper productJpaMapper) {
        this.userJpaMapper = userJpaMapper;
        this.productJpaMapper = productJpaMapper;
    }

    // ------------------------------------------------------------------ JPA -> domain

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) return null;

        Buyer buyer = (Buyer) userJpaMapper.toDomain(entity.getBuyer());
        Address deliveryAddress = entity.getDeliveryAddress() != null
                ? entity.getDeliveryAddress().toDomain()
                : null;

        List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomainItem)
                .toList();

        java.time.LocalDate creationDate = entity.getCreationDate() != null
                ? entity.getCreationDate()
                : (entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : java.time.LocalDate.now());

        return Order.reconstitute(
                entity.getOrderId(),
                buyer,
                items,
                deliveryAddress,
                entity.getStatus(),
                creationDate,
                entity.getCreatedAt()
        );
    }

    /** Maps one {@code order_items} row back to the domain value object (product, quantity, unit price). */
    private OrderItem toDomainItem(OrderItemJpaEntity itemEntity) {
        Product product = productJpaMapper.toDomain(itemEntity.getProduct());
        Money unitPrice = new Money(
                itemEntity.getUnitPriceAmount(),
                Currency.getInstance(itemEntity.getCurrencyCode())
        );
        return new OrderItem(product, itemEntity.getQuantity(), unitPrice);
    }

    // ------------------------------------------------------------------ domain -> JPA

    /**
     * Builds a brand-new (transient) entity graph for an order that does not exist yet in the
     * database. Every domain item becomes an {@link OrderItemJpaEntity} linked through
     * {@link OrderJpaEntity#addItem(OrderItemJpaEntity)}, so persisting the order cascades to
     * {@code order_items}.
     */
    public OrderJpaEntity toJpaEntity(Order domain, Function<Product, ProductJpaEntity> productResolver) {
        if (domain == null) return null;
        Objects.requireNonNull(productResolver, "productResolver must not be null");

        OrderJpaEntity orderEntity = new OrderJpaEntity();
        orderEntity.setOrderId(domain.getOrderId());
        copyOrderState(domain, orderEntity);

        for (OrderItem item : domain.getItems()) {
            orderEntity.addItem(toItemEntity(item, productResolver));
        }
        return orderEntity;
    }

    /**
     * Applies the current state of the domain aggregate onto an already persisted (managed)
     * entity. Items are reconciled instead of recreated:
     * <ul>
     *   <li>rows that still represent a domain item are kept (same {@code order_item_id});</li>
     *   <li>domain items without a matching row are added;</li>
     *   <li>rows without a matching domain item are removed (deleted via {@code orphanRemoval}).</li>
     * </ul>
     */
    public OrderJpaEntity updateJpaEntity(OrderJpaEntity target, Order domain,
                                          Function<Product, ProductJpaEntity> productResolver) {
        Objects.requireNonNull(target, "target entity must not be null");
        Objects.requireNonNull(domain, "domain order must not be null");
        Objects.requireNonNull(productResolver, "productResolver must not be null");
        if (!domain.getOrderId().equals(target.getOrderId())) {
            throw new IllegalArgumentException("Order id mismatch: entity " + target.getOrderId()
                    + " cannot be updated from order " + domain.getOrderId());
        }

        copyOrderState(domain, target);
        synchronizeItems(target, domain.getItems(), productResolver);
        return target;
    }

    /**
     * Convenience overload used by other aggregates' mappers (Invoice, Shipment, ReturnRequest)
     * that embed an order reference. Missing products fall back to a detached copy built from the
     * domain object; {@link application.adapters.persistence.jpa.OrderJpaAdapter} uses the strict
     * {@link #toJpaEntity(Order, Function)} variant instead.
     */
    public OrderJpaEntity toEntity(Order domain, SpringDataJpaProductRepository productJpaRepository) {
        return toJpaEntity(domain, product -> productJpaRepository.findById(product.getProductId())
                .orElseGet(() -> productJpaMapper.toEntity(product)));
    }

    // ------------------------------------------------------------------ internals

    private void copyOrderState(Order domain, OrderJpaEntity target) {
        // The buyer of an order never changes: keep the current (managed) reference when it matches.
        if (target.getBuyer() == null
                || !target.getBuyer().getUserId().equals(domain.getBuyer().getUserId())) {
            target.setBuyer((BuyerEntity) userJpaMapper.toEntity(domain.getBuyer()));
        }
        target.setDeliveryAddress(AddressEmbeddable.fromDomain(domain.getDeliveryAddress()));
        target.setStatus(domain.getStatus());
        target.setCreationDate(domain.getCreationDate());
    }

    private void synchronizeItems(OrderJpaEntity target, List<OrderItem> domainItems,
                                  Function<Product, ProductJpaEntity> productResolver) {
        List<OrderItemJpaEntity> unmatchedRows = new ArrayList<>(target.getItems());
        List<OrderItem> newItems = new ArrayList<>();

        for (OrderItem item : domainItems) {
            OrderItemJpaEntity row = takeMatchingRow(unmatchedRows, item);
            if (row == null) {
                newItems.add(item);
            }
        }

        unmatchedRows.forEach(target::removeItem);
        newItems.forEach(item -> target.addItem(toItemEntity(item, productResolver)));
    }

    /** Finds, removes from {@code candidates} and returns the first row equivalent to {@code item}. */
    private static OrderItemJpaEntity takeMatchingRow(List<OrderItemJpaEntity> candidates, OrderItem item) {
        Iterator<OrderItemJpaEntity> it = candidates.iterator();
        while (it.hasNext()) {
            OrderItemJpaEntity row = it.next();
            if (represents(row, item)) {
                it.remove();
                return row;
            }
        }
        return null;
    }

    /**
     * A row represents a domain item when product, quantity and unit price are equal. Amounts are
     * compared with {@code compareTo} because the column scale (4) differs from the domain scale.
     */
    private static boolean represents(OrderItemJpaEntity row, OrderItem item) {
        return row.getProduct() != null
                && Objects.equals(row.getProduct().getProductId(), item.product().getProductId())
                && row.getQuantity() == item.quantity()
                && row.getUnitPriceAmount() != null
                && row.getUnitPriceAmount().compareTo(item.unitPrice().amount()) == 0
                && Objects.equals(row.getCurrencyCode(), item.unitPrice().currency().getCurrencyCode());
    }

    private static OrderItemJpaEntity toItemEntity(OrderItem item,
                                                   Function<Product, ProductJpaEntity> productResolver) {
        ProductJpaEntity productEntity = Objects.requireNonNull(productResolver.apply(item.product()),
                () -> "No product entity resolved for product " + item.product().getProductId());
        return new OrderItemJpaEntity(
                productEntity,
                item.quantity(),
                item.unitPrice().amount(),
                item.unitPrice().currency().getCurrencyCode()
        );
    }
}
