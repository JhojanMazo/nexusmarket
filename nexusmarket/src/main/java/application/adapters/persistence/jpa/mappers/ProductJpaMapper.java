package application.adapters.persistence.jpa.mappers;

import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class ProductJpaMapper {

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) return null;
        return new Product(
                entity.getProductId(),
                entity.getName(),
                new ProductVariant(entity.getVariantAttribute(), entity.getVariantValue()),
                new Money(entity.getPriceAmount(), Currency.getInstance(entity.getCurrencyCode())),
                entity.getStatus(),
                entity.getType()
        );
    }

    public ProductJpaEntity toEntity(Product domain) {
        if (domain == null) return null;
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId(domain.getProductId());
        entity.setName(domain.getName());
        entity.setVariantAttribute(domain.getVariant().attribute());
        entity.setVariantValue(domain.getVariant().value());
        entity.setPriceAmount(domain.getPrice().amount());
        entity.setCurrencyCode(domain.getPrice().currency().getCurrencyCode());
        entity.setStatus(domain.getStatus());
        entity.setType(domain.getType());
        return entity;
    }
}
