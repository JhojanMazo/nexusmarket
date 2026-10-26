package application.adapters.rest.dtos.requests;

import java.math.BigDecimal;
import java.util.UUID;

public record GenerateInvoiceRequestDTO(
        UUID orderId,
        BigDecimal taxAmount,
        String currency
) {
    public String currencyCode() {
        return currency;
    }
}
