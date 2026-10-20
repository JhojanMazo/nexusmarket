package com.nexusmarket.infrastructure.jpa.adapter;

import com.nexusmarket.domain.model.Product;
import com.nexusmarket.domain.repository.ProductRepository;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.ProductVariant;
import com.nexusmarket.infrastructure.jpa.entity.ProductJpaEntity;
import com.nexusmarket.infrastructure.jpa.repository.ProductJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId(product.getProductId());
        entity.setName(product.getName());
        entity.setVariantAttribute(product.getVariant().attribute());
        entity.setVariantValue(product.getVariant().value());
        entity.setPriceAmount(product.getPrice().amount());
        entity.setCurrencyCode(product.getPrice().currency().getCurrencyCode());
        entity.setStatus(product.getStatus());
        entity.setType(product.getType());

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return jpaRepository.findById(productId).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Product toDomain(ProductJpaEntity entity) {
        return new Product(
                entity.getProductId(),
                entity.getName(),
                new ProductVariant(
                        entity.getVariantAttribute(),
                        entity.getVariantValue()),
                new Money(
                        entity.getPriceAmount(),
                        Currency.getInstance(entity.getCurrencyCode())),
                entity.getStatus(),
                entity.getType()
        );
    }
}
