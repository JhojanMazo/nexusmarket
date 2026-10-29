package application.adapters.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache configuration for high-speed catalog lookups.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTS_CATALOG_CACHE = "products_catalog";
    public static final String SELLER_PRODUCTS_CACHE = "seller_products";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(PRODUCTS_CATALOG_CACHE, SELLER_PRODUCTS_CACHE);
    }
}
