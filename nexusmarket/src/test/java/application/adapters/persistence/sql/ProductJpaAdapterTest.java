package application.adapters.persistence.sql;
import application.adapters.persistence.jpa.*;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductJpaAdapterTest {

    @Mock
    private SpringDataJpaProductRepository jpaRepository;

    private ProductJpaMapper mapper;
    private ProductJpaAdapter adapter;

    private Product product;
    private ProductJpaEntity productEntity;
    private UUID productId;

    @BeforeEach
    void setUp() {
        mapper = new ProductJpaMapper();
        adapter = new ProductJpaAdapter(jpaRepository, mapper);

        productId = UUID.randomUUID();
        product = new Product(
                productId,
                "Auriculares Inalambricos",
                new ProductVariant("Color", "Blanco"),
                new Money(new BigDecimal("80.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        productEntity = mapper.toEntity(product);
    }

    @Test
    void save_Success() {
        when(jpaRepository.save(any(ProductJpaEntity.class))).thenReturn(productEntity);

        Product saved = adapter.save(product);

        assertNotNull(saved);
        assertEquals(productId, saved.getProductId());
        assertEquals("Auriculares Inalambricos", saved.getName());
        assertEquals(ProductStatus.PUBLISHED, saved.getStatus());
        verify(jpaRepository, times(1)).save(any(ProductJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        Optional<Product> found = adapter.findById(productId);

        assertTrue(found.isPresent());
        assertEquals(productId, found.get().getProductId());
        assertEquals("Auriculares Inalambricos", found.get().getName());
    }

    @Test
    void findAll_Success() {
        when(jpaRepository.findAll()).thenReturn(List.of(productEntity));

        List<Product> all = adapter.findAll();

        assertEquals(1, all.size());
        assertEquals(productId, all.get(0).getProductId());
    }
}
