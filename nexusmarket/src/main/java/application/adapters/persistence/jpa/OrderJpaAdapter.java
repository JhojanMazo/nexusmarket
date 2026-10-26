package application.adapters.persistence.jpa;

import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toEntity(order, productJpaRepository);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId).map(mapper::toDomain);
    }

    @Override
    public List<Order> findByBuyerId(UUID buyerId) {
        return jpaRepository.findByBuyer_UserId(buyerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
