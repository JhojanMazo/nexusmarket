package application.domain.services.billing;

import application.domain.models.Refund;
import application.domain.ports.out.RefundRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRefundRepository implements RefundRepositoryPort {

    private final Map<UUID, Refund> storage = new ConcurrentHashMap<>();

    @Override
    public Refund save(Refund refund) {
        storage.put(refund.getRefundId(), refund);
        return refund;
    }

    @Override
    public Optional<Refund> findById(UUID refundId) {
        return Optional.ofNullable(storage.get(refundId));
    }

    @Override
    public Optional<Refund> findByReturnRequestId(UUID returnRequestId) {
        return storage.values().stream()
                .filter(r -> r.getReturnRequest().getRequestId().equals(returnRequestId))
                .findFirst();
    }

    @Override
    public List<Refund> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}
