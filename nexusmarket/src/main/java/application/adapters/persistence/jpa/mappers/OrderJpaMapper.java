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

import java.util.Currency;
import java.util.List;

@Component
public class OrderJpaMapper {

    private final UserJpaMapper userJpaMapper;
    private final ProductJpaMapper productJpaMapper;

    public OrderJpaMapper(UserJpaMapper userJpaMapper, ProductJpaMapper productJpaMapper) {
        this.userJpaMapper = userJpaMapper;
        this.productJpaMapper = productJpaMapper;
    }

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) return null;

        Buyer buyer = (Buyer) userJpaMapper.toDomain(entity.getBuyer());
        Address deliveryAddress = entity.getDeliveryAddress() != null
                ? entity.getDeliveryAddress().toDomain()
                : null;

        List<OrderItem> items = entity.getItems().stream()
                .map(itemEntity -> {
                    Product product = productJpaMapper.toDomain(itemEntity.getProduct());
                    Money unitPrice = new Money(
                            itemEntity.getUnitPriceAmount(),
                            Currency.getInstance(itemEntity.getCurrencyCode())
                    );
                    return new OrderItem(product, itemEntity.getQuantity(), unitPrice);
                })
                .toList();

        return Order.reconstitute(
                entity.getOrderId(),
                buyer,
                items,
                deliveryAddress,
                entity.getStatus(),
                entity.getCreationDate()
        );
    }

    public OrderJpaEntity toEntity(Order domain, SpringDataJpaProductRepository productJpaRepository) {
        if (domain == null) return null;

        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId(domain.getOrderId());
        entity.setBuyer((BuyerEntity) userJpaMapper.toEntity(domain.getBuyer()));
        entity.setDeliveryAddress(AddressEmbeddable.fromDomain(domain.getDeliveryAddress()));
        entity.setStatus(domain.getStatus());
        entity.setCreationDate(domain.getCreationDate());

        for (OrderItem item : domain.getItems()) {
            ProductJpaEntity productEntity = productJpaRepository.findById(item.product().getProductId())
                    .orElseGet(() -> productJpaMapper.toEntity(item.product()));

            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                    productEntity,
                    item.quantity(),
                    item.unitPrice().amount(),
                    item.unitPrice().currency().getCurrencyCode()
            );
            entity.addItem(itemEntity);
        }

        return entity;
    }
}
