package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import org.springframework.stereotype.Component;

@Component
public class ReturnRequestJpaMapper {

    private final OrderJpaMapper orderJpaMapper;

    public ReturnRequestJpaMapper(OrderJpaMapper orderJpaMapper) {
        this.orderJpaMapper = orderJpaMapper;
    }

    public ReturnRequest toDomain(ReturnRequestJpaEntity entity) {
        if (entity == null) return null;
        Order order = orderJpaMapper.toDomain(entity.getOrder());
        return ReturnRequest.reconstitute(entity.getRequestId(), order, entity.getStatus());
    }

    public ReturnRequestJpaEntity toEntity(ReturnRequest domain,
                                           SpringDataJpaOrderRepository orderRepository,
                                           SpringDataJpaProductRepository productRepository) {
        if (domain == null) return null;
        OrderJpaEntity orderEntity = orderRepository != null && domain.getOrder() != null
                ? orderRepository.findById(domain.getOrder().getOrderId())
                    .orElseGet(() -> orderJpaMapper.toEntity(domain.getOrder(), productRepository))
                : (domain.getOrder() != null ? orderJpaMapper.toEntity(domain.getOrder(), productRepository) : null);
        return new ReturnRequestJpaEntity(domain.getRequestId(), orderEntity, domain.getStatus());
    }
}
