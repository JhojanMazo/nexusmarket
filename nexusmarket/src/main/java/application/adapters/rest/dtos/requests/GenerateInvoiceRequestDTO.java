package application.adapters.rest.dtos.requests;

import java.math.BigDecimal;
import java.util.UUID;

public record GenerateInvoiceRequestDTO(
        UUID orderId,
        BigDecimal taxAmount,
        String currency,
        String currencyCode
) {
    public GenerateInvoiceRequestDTO(UUID orderId, BigDecimal taxAmount, String currency) {
        this(orderId, taxAmount, currency, currency);
    }

    @Override
    public String currencyCode() {
        return currencyCode != null && !currencyCode.isBlank() ? currencyCode : currency;
    }
}
