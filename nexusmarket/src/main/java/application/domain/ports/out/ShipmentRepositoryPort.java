package application.domain.ports.out;

import application.domain.models.Shipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepositoryPort {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(UUID shipmentId);
    Optional<Shipment> findByOrderId(UUID orderId);
    List<Shipment> findByStatus(Shipment.ShipmentStatus status);
    List<Shipment> findAll();
}
