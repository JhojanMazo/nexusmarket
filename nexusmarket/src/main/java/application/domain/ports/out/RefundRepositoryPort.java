package application.domain.ports.out;

import application.domain.models.Refund;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepositoryPort {
    Refund save(Refund refund);
    Optional<Refund> findById(UUID refundId);
    Optional<Refund> findByReturnRequestId(UUID returnRequestId);
    List<Refund> findAll();
}
