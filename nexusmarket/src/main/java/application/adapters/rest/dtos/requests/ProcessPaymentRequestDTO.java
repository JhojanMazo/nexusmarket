package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequestDTO(
        UUID orderId,
        String paymentMethod,
        BigDecimal amount,
        String currency
) {
}
