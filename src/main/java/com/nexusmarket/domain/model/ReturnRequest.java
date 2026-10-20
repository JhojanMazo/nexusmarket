package com.nexusmarket.domain.model;

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
    private ReturnRequestStatus status;

    private ReturnRequest(UUID requestId, Order order) {
        this.requestId = Objects.requireNonNull(requestId);
        this.order = Objects.requireNonNull(order);
        this.status = ReturnRequestStatus.REQUESTED;
    }

    public static ReturnRequest create(Order order) {
        Objects.requireNonNull(order);
        return new ReturnRequest(UUID.randomUUID(), order);
    }

    public UUID getRequestId() { return requestId; }
    public Order getOrder() { return order; }
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
