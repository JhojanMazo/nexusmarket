package application.adapters.rest.dtos.responses;

import application.domain.models.ReturnRequest;

import java.util.UUID;

public record ReturnRequestResponseDTO(
        UUID requestId,
        UUID orderId,
        ReturnRequest.ReturnRequestStatus status,
        String reason
) {
    public ReturnRequestResponseDTO(UUID requestId, UUID orderId, ReturnRequest.ReturnRequestStatus status) {
        this(requestId, orderId, status, "Customer requested return");
    }
}
