package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponseDTO(
        UUID paymentId,
        UUID orderId,
        String status,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        Instant processedAt,
        String transactionReference
) {
}
