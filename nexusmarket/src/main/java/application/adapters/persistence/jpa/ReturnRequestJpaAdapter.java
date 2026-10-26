package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import application.adapters.persistence.jpa.mappers.ReturnRequestJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import application.domain.models.ReturnRequest;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class ReturnRequestJpaAdapter implements ReturnRequestRepositoryPort {

    private final SpringDataJpaReturnRequestRepository jpaRepository;
    private final SpringDataJpaOrderRepository orderRepository;
    private final SpringDataJpaProductRepository productRepository;
    private final ReturnRequestJpaMapper mapper;

    public ReturnRequestJpaAdapter(SpringDataJpaReturnRequestRepository jpaRepository,
                                  SpringDataJpaOrderRepository orderRepository,
                                  SpringDataJpaProductRepository productRepository,
                                  ReturnRequestJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        ReturnRequestJpaEntity entity = mapper.toEntity(returnRequest, orderRepository, productRepository);
        ReturnRequestJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ReturnRequest> findById(UUID returnRequestId) {
        return jpaRepository.findById(returnRequestId).map(mapper::toDomain);
    }

    @Override
    public Optional<ReturnRequest> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public List<ReturnRequest> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
