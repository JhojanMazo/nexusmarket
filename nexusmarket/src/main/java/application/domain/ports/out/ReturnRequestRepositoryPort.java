package application.domain.ports.out;

import application.domain.models.ReturnRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRequestRepositoryPort {
    ReturnRequest save(ReturnRequest returnRequest);
    Optional<ReturnRequest> findById(UUID returnRequestId);
    Optional<ReturnRequest> findByOrderId(UUID orderId);
    List<ReturnRequest> findAll();
}
