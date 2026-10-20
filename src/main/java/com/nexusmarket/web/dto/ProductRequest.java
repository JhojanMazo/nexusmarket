package com.nexusmarket.web.dto;

import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String variantAttribute,
        String variantValue,
        BigDecimal price,
        String currency,
        ProductStatus status,
        ProductType type
) {
}
