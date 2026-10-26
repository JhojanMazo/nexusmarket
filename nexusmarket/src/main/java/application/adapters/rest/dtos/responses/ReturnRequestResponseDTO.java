package application.adapters.rest.dtos.responses;

import application.domain.models.ReturnRequest;

import java.util.UUID;

public record ReturnRequestResponseDTO(
        UUID requestId,
        UUID orderId,
        ReturnRequest.ReturnRequestStatus status
) {
}
