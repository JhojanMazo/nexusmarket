package application.domain.services.logistics;

import application.domain.models.ReturnRequest;
import application.domain.ports.out.ReturnRequestRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReturnRequestRepository implements ReturnRequestRepositoryPort {

    private final Map<UUID, ReturnRequest> storage = new ConcurrentHashMap<>();

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        storage.put(returnRequest.getRequestId(), returnRequest);
        return returnRequest;
    }

    @Override
    public Optional<ReturnRequest> findById(UUID returnRequestId) {
        return Optional.ofNullable(storage.get(returnRequestId));
    }

    @Override
    public Optional<ReturnRequest> findByOrderId(UUID orderId) {
        return storage.values().stream()
                .filter(r -> r.getOrder().getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public List<ReturnRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}
