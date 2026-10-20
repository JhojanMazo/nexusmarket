package com.nexusmarket.web.dto;

import com.nexusmarket.domain.model.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String variantAttribute,
        String variantValue,
        BigDecimal price,
        String currency,
        String status,
        String type
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getVariant().attribute(),
                product.getVariant().value(),
                product.getPrice().amount(),
                product.getPrice().currency().getCurrencyCode(),
                product.getStatus().name(),
                product.getType().name()
        );
    }
}
