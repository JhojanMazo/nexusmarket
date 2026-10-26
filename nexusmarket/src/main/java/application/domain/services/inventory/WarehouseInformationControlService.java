package application.domain.services.inventory;

import application.domain.models.Warehouse;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 3.1 Manages physical warehouse properties and parameters.
 */
@Service
public class WarehouseInformationControlService {

    public void updateWarehouseCapacity(Warehouse warehouse, double capacity) {
        Objects.requireNonNull(warehouse, "Warehouse must not be null");
        warehouse.updateCapacity(capacity);
    }
}
