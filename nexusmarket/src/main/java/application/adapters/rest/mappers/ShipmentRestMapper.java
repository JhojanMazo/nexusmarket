package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.ShipmentResponseDTO;
import application.domain.models.Shipment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ShipmentRestMapper {

    public ShipmentResponseDTO toResponse(Shipment shipment) {
        if (shipment == null) return null;
        return new ShipmentResponseDTO(
                shipment.getShipmentId(),
                shipment.getOrder() != null ? shipment.getOrder().getOrderId() : null,
                shipment.getLogisticsOperator() != null ? shipment.getLogisticsOperator().getUserId() : null,
                shipment.getStatus()
        );
    }

    public List<ShipmentResponseDTO> toResponseList(List<Shipment> shipments) {
        if (shipments == null) return Collections.emptyList();
        return shipments.stream().map(this::toResponse).toList();
    }
}
