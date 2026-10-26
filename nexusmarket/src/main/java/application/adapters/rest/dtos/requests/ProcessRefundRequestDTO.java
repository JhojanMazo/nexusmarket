package application.adapters.rest.dtos.requests;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessRefundRequestDTO(
        UUID returnRequestId,
        BigDecimal amount,
        String currencyCode,
        String reason
) {
}
