package application.adapters.persistence.jpa;

import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.valueobjects.PageResult;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound adapter persisting the {@link Order} aggregate (header + items) in the relational store.
 *
 * <p>{@link #save(Order)} runs in a single transaction: it loads the managed order (if any),
 * reconciles its {@code order_items} with the domain items and lets JPA cascade the changes
 * (insert/update/orphan delete) to the {@code order_items} table.</p>
 */
@Primary
@Repository
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final SpringDataJpaOrderRepository jpaRepository;
    private final SpringDataJpaProductRepository productJpaRepository;
    private final OrderJpaMapper mapper;

    public OrderJpaAdapter(SpringDataJpaOrderRepository jpaRepository,
                           SpringDataJpaProductRepository productJpaRepository,
                           OrderJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.productJpaRepository = productJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = jpaRepository.findById(order.getOrderId())
                .map(existing -> mapper.updateJpaEntity(existing, order, this::resolveProduct))
                .orElseGet(() -> mapper.toJpaEntity(order, this::resolveProduct));
        OrderJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByBuyerId(UUID buyerId) {
        return jpaRepository.findByBuyer_UserId(buyerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> findAll(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<OrderJpaEntity> entityPage = jpaRepository.findAll(pageable);
        return toPageResult(entityPage, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> findByBuyerId(UUID buyerId, int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<OrderJpaEntity> entityPage = jpaRepository.findByBuyer_UserId(buyerId, pageable);
        return toPageResult(entityPage, page, size);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        String resolvedProperty = resolveSortProperty(sortBy);
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);
        return PageRequest.of(safePage, safeSize, Sort.by(dir, resolvedProperty));
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "creationDate";
        }
        return switch (sortBy.trim()) {
            case "orderDate", "creationDate" -> "creationDate";
            case "id", "orderId" -> "orderId";
            case "status" -> "status";
            case "createdAt" -> "createdAt";
            default -> "creationDate";
        };
    }

    private PageResult<Order> toPageResult(Page<OrderJpaEntity> entityPage, int fallbackPage, int fallbackSize) {
        if (entityPage == null) {
            return PageResult.of(List.of(), fallbackPage, fallbackSize);
        }
        List<Order> content = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new PageResult<>(
                content,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isLast()
        );
    }

    /**
     * An order item must reference an existing product row ({@code order_items.product_id} FK);
     * products are never created implicitly while saving an order.
     */
    private ProductJpaEntity resolveProduct(Product product) {
        return productJpaRepository.findById(product.getProductId())
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot persist order item: product " + product.getProductId() + " does not exist"));
    }
}
