package application.adapters.config;

import application.domain.ports.out.BusinessConfigurationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adapter providing enterprise business configuration and operational thresholds.
 */
@Component
public class DefaultBusinessConfigurationAdapter implements BusinessConfigurationPort {

    private final BigDecimal defaultTaxRate;
    private final String defaultCurrency;
    private final int criticalStockThreshold;

    public DefaultBusinessConfigurationAdapter(
            @Value("${nexusmarket.tax-rate:0.19}") BigDecimal defaultTaxRate,
            @Value("${nexusmarket.default-currency:COP}") String defaultCurrency,
            @Value("${nexusmarket.critical-stock-threshold:5}") int criticalStockThreshold) {
        this.defaultTaxRate = defaultTaxRate;
        this.defaultCurrency = defaultCurrency;
        this.criticalStockThreshold = criticalStockThreshold;
    }

    @Override
    public BigDecimal getDefaultTaxRate() {
        return defaultTaxRate;
    }

    @Override
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public int getCriticalStockThreshold() {
        return criticalStockThreshold;
    }
}
