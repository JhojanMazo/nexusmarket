package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponseDTO(
        UUID invoiceId,
        UUID orderId,
        LocalDate issueDate,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        String currency
) {
}
