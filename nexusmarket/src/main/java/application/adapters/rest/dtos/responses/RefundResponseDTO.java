package application.adapters.rest.dtos.responses;

import application.domain.models.Refund;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundResponseDTO(
        UUID refundId,
        UUID returnRequestId,
        BigDecimal amount,
        String currencyCode,
        Refund.RefundStatus status
) {
}
