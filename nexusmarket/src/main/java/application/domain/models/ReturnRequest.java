package application.domain.models;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain object representing a return request.
 *
 * FR-023: Initialized in REQUESTED state.
 * FR-024: approve()/reject() only allowed from REQUESTED.
 * FR-025: complete() only allowed from APPROVED.
 */
public class ReturnRequest {
    public enum ReturnRequestStatus { REQUESTED, APPROVED, REJECTED, COMPLETED }

    private final UUID requestId;
    private final Order order;
    private final String reason;
    private ReturnRequestStatus status;

    private ReturnRequest(UUID requestId, Order order, String reason) {
        this.requestId = Objects.requireNonNull(requestId);
        this.order = Objects.requireNonNull(order);
        this.reason = (reason != null && !reason.isBlank()) ? reason.trim() : "Customer requested return";
        this.status = ReturnRequestStatus.REQUESTED;
    }

    public static ReturnRequest create(Order order) {
        return create(order, "Customer requested return");
    }

    public static ReturnRequest create(Order order, String reason) {
        Objects.requireNonNull(order);
        return new ReturnRequest(UUID.randomUUID(), order, reason);
    }

    public static ReturnRequest reconstitute(UUID requestId, Order order, ReturnRequestStatus status) {
        return reconstitute(requestId, order, status, "Customer requested return");
    }

    public static ReturnRequest reconstitute(UUID requestId, Order order, ReturnRequestStatus status, String reason) {
        ReturnRequest request = new ReturnRequest(requestId, order, reason);
        request.status = Objects.requireNonNull(status);
        return request;
    }

    public UUID getRequestId() { return requestId; }
    public Order getOrder() { return order; }
    public String getReason() { return reason; }
    public ReturnRequestStatus getStatus() { return status; }

    public void approve() {
        if (status != ReturnRequestStatus.REQUESTED) {
            throw new IllegalStateException("Can only approve from REQUESTED state");
        }
        this.status = ReturnRequestStatus.APPROVED;
    }

    public void reject() {
        if (status != ReturnRequestStatus.REQUESTED) {
            throw new IllegalStateException("Can only reject from REQUESTED state");
        }
        this.status = ReturnRequestStatus.REJECTED;
    }

    public void complete() {
        if (status != ReturnRequestStatus.APPROVED) {
            throw new IllegalStateException("Can only complete from APPROVED state");
        }
        this.status = ReturnRequestStatus.COMPLETED;
    }
}
