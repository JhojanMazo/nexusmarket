package application.domain.services.billing;

import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.valueobjects.Money;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 6.4 Governs financial compensation upon return receipt (FR-026).
 */
@Service
public class RefundsManagementService {

    public Refund processRefund(ReturnRequest returnRequest, Money amount) {
        Objects.requireNonNull(returnRequest, "ReturnRequest must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");

        Refund refund = Refund.create(returnRequest, amount);
        refund.process();
        return refund;
    }
}
