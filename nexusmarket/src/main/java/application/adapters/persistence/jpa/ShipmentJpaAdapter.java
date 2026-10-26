package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import application.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaShipmentRepository;
import application.domain.models.Shipment;
import application.domain.ports.out.ShipmentRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class ShipmentJpaAdapter implements ShipmentRepositoryPort {

    private final SpringDataJpaShipmentRepository jpaRepository;
    private final SpringDataJpaOrderRepository orderRepository;
    private final SpringDataJpaProductRepository productRepository;
    private final ShipmentJpaMapper mapper;

    public ShipmentJpaAdapter(SpringDataJpaShipmentRepository jpaRepository,
                              SpringDataJpaOrderRepository orderRepository,
                              SpringDataJpaProductRepository productRepository,
                              ShipmentJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentJpaEntity entity = mapper.toEntity(shipment, orderRepository, productRepository);
        ShipmentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Shipment> findById(UUID shipmentId) {
        return jpaRepository.findById(shipmentId).map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findByStatus(Shipment.ShipmentStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Shipment> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
