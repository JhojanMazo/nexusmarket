package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.ReturnStatus;

import java.time.LocalDate;
import java.util.Objects;

public class ReturnRequest {

    private final String returnId;
    private final Order order;
    private final String reason;
    private ReturnStatus status;
    private final LocalDate requestDate;

    public ReturnRequest(String returnId, Order order, String reason, LocalDate requestDate) {
        this.returnId = requireNotBlank(returnId, "returnId");
        this.order = Objects.requireNonNull(order, "order is required");
        this.reason = requireNotBlank(reason, "reason");
        this.requestDate = Objects.requireNonNull(requestDate, "requestDate is required");
        this.status = ReturnStatus.REQUESTED;
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public void approve() {
        if (status != ReturnStatus.REQUESTED) {
            throw new IllegalStateException("Only a REQUESTED return can be approved.");
        }
        status = ReturnStatus.APPROVED;
    }

    public void reject() {
        if (status != ReturnStatus.REQUESTED) {
            throw new IllegalStateException("Only a REQUESTED return can be rejected.");
        }
        status = ReturnStatus.REJECTED;
    }

    public void complete() {
        if (status != ReturnStatus.APPROVED) {
            throw new IllegalStateException("Only an APPROVED return can be completed.");
        }
        status = ReturnStatus.COMPLETED;
    }

    public String getReturnId() { return returnId; }
    public Order getOrder() { return order; }
    public String getReason() { return reason; }
    public ReturnStatus getStatus() { return status; }
    public LocalDate getRequestDate() { return requestDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnRequest)) return false;
        return returnId.equals(((ReturnRequest) o).returnId);
    }

    @Override
    public int hashCode() { return Objects.hash(returnId); }
}
