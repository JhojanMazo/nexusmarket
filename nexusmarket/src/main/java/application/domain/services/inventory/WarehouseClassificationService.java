package application.domain.services.inventory;

import application.domain.enums.WarehouseType;
import application.domain.models.Warehouse;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 3.2 Segregates storage facilities by ownership model.
 */
@Service
public class WarehouseClassificationService {

    public boolean isMarketplaceOwned(Warehouse warehouse) {
        Objects.requireNonNull(warehouse, "Warehouse must not be null");
        return warehouse.getType() == WarehouseType.MARKETPLACE;
    }

    public boolean isSellerOwned(Warehouse warehouse) {
        Objects.requireNonNull(warehouse, "Warehouse must not be null");
        return warehouse.getType() == WarehouseType.SELLER;
    }
}
