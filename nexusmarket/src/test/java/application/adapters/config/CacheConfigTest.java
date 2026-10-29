package application.adapters.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CacheConfigTest {

    @Test
    void cacheManager_ConfiguresIsolatedCaches() {
        CacheConfig config = new CacheConfig();
        CacheManager cacheManager = config.cacheManager();

        assertNotNull(cacheManager);

        Cache publicCatalogCache = cacheManager.getCache(CacheConfig.PRODUCTS_CATALOG_CACHE);
        Cache sellerProductsCache = cacheManager.getCache(CacheConfig.SELLER_PRODUCTS_CACHE);

        assertNotNull(publicCatalogCache, "products_catalog cache must exist");
        assertNotNull(sellerProductsCache, "seller_products cache must exist");

        // Verify segregation: key stored in public cache does not exist in seller cache
        publicCatalogCache.put("catalog-key-1", "published_item");
        sellerProductsCache.put("seller-key-1", "seller_item");

        assertEquals("published_item", publicCatalogCache.get("catalog-key-1", String.class));
        assertNull(publicCatalogCache.get("seller-key-1", String.class));

        assertEquals("seller_item", sellerProductsCache.get("seller-key-1", String.class));
        assertNull(sellerProductsCache.get("catalog-key-1", String.class));
    }
}
