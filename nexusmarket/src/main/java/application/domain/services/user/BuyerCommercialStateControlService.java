package application.domain.services.user;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import java.util.Objects;

/**
 * 2.5 Evaluates commercial transaction eligibility (FR-003).
 */
public class BuyerCommercialStateControlService {

    public boolean canTransact(Buyer buyer) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        return buyer.getStatus() == UserStatus.ACTIVE &&
               buyer.getCommercialStatus() == BuyerCommercialStatus.ENABLED &&
               buyer.getPrimaryAddress() != null;
    }

    public void verifyAndEnableTransactions(Buyer buyer) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        if (buyer.getPrimaryAddress() == null) {
            throw new IllegalStateException("No se puede habilitar transacciones sin una dirección principal");
        }
        buyer.setCommercialStatus(BuyerCommercialStatus.ENABLED);
    }
}
