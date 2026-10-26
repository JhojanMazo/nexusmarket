package application.domain.ports.out;

import java.math.BigDecimal;

/**
 * Outbound port for accessing business configuration and global policy parameters.
 */
public interface BusinessConfigurationPort {
    BigDecimal getDefaultTaxRate();
    String getDefaultCurrency();
    int getCriticalStockThreshold();
}
