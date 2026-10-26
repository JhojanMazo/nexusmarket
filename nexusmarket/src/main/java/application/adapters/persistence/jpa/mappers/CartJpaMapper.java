package application.adapters.persistence.jpa.mappers;

import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.adapters.persistence.jpa.entities.BuyerEntity;
import application.adapters.persistence.jpa.entities.CartItemJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.entities.ShoppingCartJpaEntity;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CartJpaMapper {

    private final UserJpaMapper userJpaMapper;
    private final ProductJpaMapper productJpaMapper;

    public CartJpaMapper(UserJpaMapper userJpaMapper, ProductJpaMapper productJpaMapper) {
        this.userJpaMapper = userJpaMapper;
        this.productJpaMapper = productJpaMapper;
    }

    public ShoppingCart toDomain(ShoppingCartJpaEntity entity) {
        if (entity == null) return null;

        Buyer buyer = (Buyer) userJpaMapper.toDomain(entity.getBuyer());
        ShoppingCart cart = new ShoppingCart(entity.getCartId(), buyer);

        if (entity.getItems() != null) {
            for (CartItemJpaEntity itemEntity : entity.getItems()) {
                Product product = productJpaMapper.toDomain(itemEntity.getProduct());
                Money unitPrice = new Money(
                        itemEntity.getUnitPriceAmount(),
                        Currency.getInstance(itemEntity.getCurrencyCode())
                );
                cart.addItem(new OrderItem(product, itemEntity.getQuantity(), unitPrice));
            }
        }

        return cart;
    }

    public ShoppingCartJpaEntity toEntity(ShoppingCart domain, SpringDataJpaProductRepository productJpaRepository) {
        if (domain == null) return null;

        ShoppingCartJpaEntity entity = new ShoppingCartJpaEntity(
                domain.getCartId(),
                (BuyerEntity) userJpaMapper.toEntity(domain.getBuyer())
        );

        for (OrderItem item : domain.getItems()) {
            ProductJpaEntity productEntity = productJpaRepository.findById(item.product().getProductId())
                    .orElseGet(() -> productJpaMapper.toEntity(item.product()));

            CartItemJpaEntity itemEntity = new CartItemJpaEntity(
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
