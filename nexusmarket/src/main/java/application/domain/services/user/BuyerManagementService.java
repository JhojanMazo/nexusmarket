package application.domain.services.user;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.models.Buyer;
import java.util.Objects;

/**
 * 2.2 Manages buyer commercial records and profiles.
 */
public class BuyerManagementService {

    public void updateCommercialStatus(Buyer buyer, BuyerCommercialStatus newStatus) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        Objects.requireNonNull(newStatus, "Commercial status must not be null");
        buyer.setCommercialStatus(newStatus);
    }
}
