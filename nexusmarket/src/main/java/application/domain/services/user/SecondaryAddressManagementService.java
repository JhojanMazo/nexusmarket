package application.domain.services.user;

import application.domain.models.Buyer;
import application.domain.valueobjects.Address;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 2.4 Manages auxiliary delivery addresses.
 */
@Service
public class SecondaryAddressManagementService {

    public void addSecondaryAddress(Buyer buyer, Address address) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        Objects.requireNonNull(address, "Address must not be null");
        buyer.addAdditionalAddress(address);
    }

    public void updateSecondaryAddress(Buyer buyer, int index, Address newAddress) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        Objects.requireNonNull(newAddress, "Address must not be null");
        buyer.updateAdditionalAddress(index, newAddress);
    }

    public void removeSecondaryAddress(Buyer buyer, int index) {
        Objects.requireNonNull(buyer, "Buyer must not be null");
        buyer.removeAdditionalAddress(index);
    }
}
