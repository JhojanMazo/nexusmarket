package application.adapters.rest.dtos.requests;

import application.domain.models.Shipment;

public record UpdateShipmentStatusRequestDTO(
        Shipment.ShipmentStatus status
) {
}
