package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.RefundJpaEntity;
import application.adapters.persistence.jpa.mappers.RefundJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaRefundRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import application.domain.models.Refund;
import application.domain.ports.out.RefundRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class RefundJpaAdapter implements RefundRepositoryPort {

    private final SpringDataJpaRefundRepository jpaRepository;
    private final SpringDataJpaReturnRequestRepository returnRequestRepository;
    private final SpringDataJpaOrderRepository orderRepository;
    private final SpringDataJpaProductRepository productRepository;
    private final RefundJpaMapper mapper;

    public RefundJpaAdapter(SpringDataJpaRefundRepository jpaRepository,
                           SpringDataJpaReturnRequestRepository returnRequestRepository,
                           SpringDataJpaOrderRepository orderRepository,
                           SpringDataJpaProductRepository productRepository,
                           RefundJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public Refund save(Refund refund) {
        RefundJpaEntity entity = mapper.toEntity(refund, returnRequestRepository, orderRepository, productRepository);
        RefundJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Refund> findById(UUID refundId) {
        return jpaRepository.findById(refundId).map(mapper::toDomain);
    }

    @Override
    public Optional<Refund> findByReturnRequestId(UUID returnRequestId) {
        return jpaRepository.findByReturnRequestRequestId(returnRequestId).map(mapper::toDomain);
    }

    @Override
    public List<Refund> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
