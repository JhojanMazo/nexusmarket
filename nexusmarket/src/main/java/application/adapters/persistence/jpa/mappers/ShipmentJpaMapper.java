package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import application.adapters.persistence.jpa.entities.UserEntity;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import org.springframework.stereotype.Component;

@Component
public class ShipmentJpaMapper {

    private final OrderJpaMapper orderJpaMapper;
    private final UserJpaMapper userJpaMapper;

    public ShipmentJpaMapper(OrderJpaMapper orderJpaMapper, UserJpaMapper userJpaMapper) {
        this.orderJpaMapper = orderJpaMapper;
        this.userJpaMapper = userJpaMapper;
    }

    public Shipment toDomain(ShipmentJpaEntity entity) {
        if (entity == null) return null;
        Order order = orderJpaMapper.toDomain(entity.getOrder());
        User operator = userJpaMapper.toDomain(entity.getLogisticsOperator());
        return Shipment.reconstitute(entity.getShipmentId(), order, operator, entity.getStatus());
    }

    public ShipmentJpaEntity toEntity(Shipment domain,
                                      SpringDataJpaOrderRepository orderRepository,
                                      SpringDataJpaProductRepository productRepository) {
        if (domain == null) return null;
        OrderJpaEntity orderEntity = orderRepository != null && domain.getOrder() != null
                ? orderRepository.findById(domain.getOrder().getOrderId())
                    .orElseGet(() -> orderJpaMapper.toEntity(domain.getOrder(), productRepository))
                : (domain.getOrder() != null ? orderJpaMapper.toEntity(domain.getOrder(), productRepository) : null);
        UserEntity operatorEntity = userJpaMapper.toEntity(domain.getLogisticsOperator());
        return new ShipmentJpaEntity(domain.getShipmentId(), orderEntity, operatorEntity, domain.getStatus());
    }
}
