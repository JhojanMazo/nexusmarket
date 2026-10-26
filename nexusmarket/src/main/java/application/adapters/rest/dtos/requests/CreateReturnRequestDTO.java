package application.adapters.rest.dtos.requests;

import java.util.UUID;

public record CreateReturnRequestDTO(
        UUID orderId,
        String reason
) {
}
