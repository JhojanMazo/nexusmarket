package com.nexusmarket.application.service;

import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.model.Product;
import com.nexusmarket.domain.repository.ProductRepository;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.ProductVariant;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(
            String name,
            String variantAttribute,
            String variantValue,
            BigDecimal price,
            String currencyCode,
            ProductStatus status,
            ProductType type) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (price == null || status == null || type == null) {
            throw new IllegalArgumentException("Precio, estado y tipo son obligatorios.");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("La moneda es obligatoria.");
        }

        String normalizedName = name.trim();
        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede superar 150 caracteres.");
        }

        String normalizedCurrency = currencyCode.trim().toUpperCase(Locale.ROOT);
        Currency currency;
        try {
            currency = Currency.getInstance(normalizedCurrency);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Código de moneda inválido. Ejemplo: COP o USD.");
        }

        Product product = new Product(
                UUID.randomUUID(),
                normalizedName,
                new ProductVariant(variantAttribute, variantValue),
                new Money(price, currency),
                status,
                type
        );

        return productRepository.save(product);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(UUID id) {
            super("No se encontró el producto con ID: " + id);
        }
    }
}
