package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.RefundStatus;
import com.nexusmarket.domain.enums.ReturnStatus;
import com.nexusmarket.domain.valueobject.Money;

import java.util.Objects;

/** Financial compensation resulting from an approved ReturnRequest. */
public class Refund {

    private final String refundId;
    private final ReturnRequest returnRequest;
    private final Money amount;
    private RefundStatus status;

    public Refund(String refundId, ReturnRequest returnRequest, Money amount) {
        if (returnRequest.getStatus() != ReturnStatus.APPROVED
                && returnRequest.getStatus() != ReturnStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "A refund can only be created from an approved or completed return request.");
        }
        this.refundId = requireNotBlank(refundId, "refundId");
        this.returnRequest = returnRequest;
        this.amount = Objects.requireNonNull(amount, "amount is required");
        this.status = RefundStatus.PENDING;
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public void process() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING refund can be processed.");
        }
        status = RefundStatus.PROCESSED;
    }

    public void reject() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING refund can be rejected.");
        }
        status = RefundStatus.REJECTED;
    }

    public String getRefundId() { return refundId; }
    public ReturnRequest getReturnRequest() { return returnRequest; }
    public Money getAmount() { return amount; }
    public RefundStatus getStatus() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Refund)) return false;
        return refundId.equals(((Refund) o).refundId);
    }

    @Override
    public int hashCode() { return Objects.hash(refundId); }
}
