package application.adapters.rest.dtos.responses;

import application.domain.models.Shipment;

import java.util.UUID;

public record ShipmentResponseDTO(
        UUID shipmentId,
        UUID orderId,
        UUID logisticsOperatorId,
        Shipment.ShipmentStatus status
) {
}
