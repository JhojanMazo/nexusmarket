package application.domain.models;

import java.util.Objects;
import java.util.UUID;
import application.domain.valueobjects.Money;

/**
 * Domain object representing a refund.
 *
 * FR-026: Refund can only be created from a ReturnRequest whose status is APPROVED or COMPLETED.
 */
public class Refund {
    public enum RefundStatus { PENDING, PROCESSED, REJECTED }

    private final UUID refundId;
    private final ReturnRequest returnRequest;
    private final Money amount;
    private RefundStatus status;

    private Refund(UUID refundId, ReturnRequest returnRequest, Money amount) {
        this.refundId = Objects.requireNonNull(refundId);
        this.returnRequest = Objects.requireNonNull(returnRequest);
        this.amount = Objects.requireNonNull(amount);
        // validate eligibility
        ReturnRequest.ReturnRequestStatus rrStatus = returnRequest.getStatus();
        if (rrStatus != ReturnRequest.ReturnRequestStatus.APPROVED &&
            rrStatus != ReturnRequest.ReturnRequestStatus.COMPLETED) {
            throw new IllegalArgumentException("Refund can only be created from APPROVED or COMPLETED ReturnRequest");
        }
        this.status = RefundStatus.PENDING;
    }

    public static Refund create(ReturnRequest returnRequest, Money amount) {
        Objects.requireNonNull(returnRequest);
        Objects.requireNonNull(amount);
        return new Refund(UUID.randomUUID(), returnRequest, amount);
    }

    public static Refund reconstitute(UUID refundId, ReturnRequest returnRequest, Money amount, RefundStatus status) {
        Refund refund = new Refund(refundId, returnRequest, amount);
        refund.status = Objects.requireNonNull(status);
        return refund;
    }

    public UUID getRefundId() { return refundId; }
    public ReturnRequest getReturnRequest() { return returnRequest; }
    public Money getAmount() { return amount; }
    public RefundStatus getStatus() { return status; }

    public void process() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Refund can only be processed from PENDING state");
        }
        this.status = RefundStatus.PROCESSED;
    }

    public void reject() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Refund can only be rejected from PENDING state");
        }
        this.status = RefundStatus.REJECTED;
    }
}
