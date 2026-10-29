package application.domain.services.user;

import application.domain.models.Buyer;
import application.domain.valueobjects.Address;
import java.util.Objects;

/**
 * 2.3 Controls default/primary delivery location for buyers.
 */
public class PrimaryAddressManagementService {

    public void validatePrimaryAddress(Buyer buyer) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        if (buyer.getPrimaryAddress() == null) {
            throw new IllegalStateException("El comprador no tiene configurada una dirección principal");
        }
    }

    public void updatePrimaryAddress(Buyer buyer, Address newPrimaryAddress) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        Objects.requireNonNull(newPrimaryAddress, "Primary address must not be null");
        buyer.updatePrimaryAddress(newPrimaryAddress);
    }
}
