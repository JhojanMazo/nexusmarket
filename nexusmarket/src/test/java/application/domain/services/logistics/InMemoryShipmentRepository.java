package application.domain.services.logistics;

import application.domain.models.Shipment;
import application.domain.ports.out.ShipmentRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryShipmentRepository implements ShipmentRepositoryPort {

    private final Map<UUID, Shipment> storage = new ConcurrentHashMap<>();

    @Override
    public Shipment save(Shipment shipment) {
        storage.put(shipment.getShipmentId(), shipment);
        return shipment;
    }

    @Override
    public Optional<Shipment> findById(UUID shipmentId) {
        return Optional.ofNullable(storage.get(shipmentId));
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return storage.values().stream()
                .filter(s -> s.getOrder().getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public List<Shipment> findByStatus(Shipment.ShipmentStatus status) {
        return storage.values().stream()
                .filter(s -> s.getStatus() == status)
                .toList();
    }

    @Override
    public List<Shipment> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}
