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

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

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

    public ShoppingCartJpaEntity updateEntity(ShoppingCartJpaEntity existing,
                                              ShoppingCart domain,
                                              SpringDataJpaProductRepository productJpaRepository) {
        if (existing == null || domain == null) return existing;

        List<CartItemJpaEntity> unmatchedRows = new ArrayList<>(existing.getItems());

        for (OrderItem item : domain.getItems()) {
            CartItemJpaEntity matchedRow = null;
            for (CartItemJpaEntity row : unmatchedRows) {
                if (row.getProduct() != null && row.getProduct().getProductId().equals(item.product().getProductId())) {
                    matchedRow = row;
                    break;
                }
            }

            if (matchedRow != null) {
                unmatchedRows.remove(matchedRow);
                matchedRow.setQuantity(item.quantity());
                matchedRow.setUnitPriceAmount(item.unitPrice().amount());
                matchedRow.setCurrencyCode(item.unitPrice().currency().getCurrencyCode());
            } else {
                ProductJpaEntity productEntity = productJpaRepository.findById(item.product().getProductId())
                        .orElseGet(() -> productJpaMapper.toEntity(item.product()));

                CartItemJpaEntity itemEntity = new CartItemJpaEntity(
                        productEntity,
                        item.quantity(),
                        item.unitPrice().amount(),
                        item.unitPrice().currency().getCurrencyCode()
                );
                existing.addItem(itemEntity);
            }
        }

        for (CartItemJpaEntity orphan : unmatchedRows) {
            existing.removeItem(orphan);
        }

        return existing;
    }
}
