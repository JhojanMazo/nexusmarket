package application.adapters.persistence.sql;
import application.adapters.persistence.jpa.*;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.*;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.entities.ShoppingCartJpaEntity;
import application.adapters.persistence.jpa.mappers.CartJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaShoppingCartRepository;
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
class ShoppingCartJpaAdapterTest {

    @Mock
    private SpringDataJpaShoppingCartRepository jpaRepository;

    @Mock
    private SpringDataJpaProductRepository productJpaRepository;

    private CartJpaMapper mapper;
    private ShoppingCartJpaAdapter adapter;

    private ShoppingCart cart;
    private ShoppingCartJpaEntity cartEntity;
    private UUID cartId;
    private UUID buyerId;
    private Product product;

    @BeforeEach
    void setUp() {
        UserJpaMapper userMapper = new UserJpaMapper();
        ProductJpaMapper productMapper = new ProductJpaMapper();
        mapper = new CartJpaMapper(userMapper, productMapper);
        adapter = new ShoppingCartJpaAdapter(jpaRepository, productJpaRepository, mapper);

        buyerId = UUID.randomUUID();
        Buyer buyer = Buyer.reconstitute(
                buyerId,
                "Andres Castro",
                new Email("andres@example.com"),
                new IdentityDocument("CC", "71234567"),
                new Address("Calle 80", "Bogota", "Cundinamarca", "Colombia", "110111", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                application.domain.enums.UserStatus.ACTIVE
        );

        cartId = UUID.randomUUID();
        cart = new ShoppingCart(cartId, buyer);

        product = new Product(
                UUID.randomUUID(),
                "Camara Web HD",
                new ProductVariant("Resolucion", "1080p"),
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        cart.addItem(new OrderItem(product, 2, new Money(new BigDecimal("50.00"), Currency.getInstance("USD"))));

        ProductJpaEntity pEntity = productMapper.toEntity(product);
        when(productJpaRepository.findById(product.getProductId())).thenReturn(Optional.of(pEntity));
        cartEntity = mapper.toEntity(cart, productJpaRepository);
    }

    @Test
    void save_Success() {
        when(jpaRepository.save(any(ShoppingCartJpaEntity.class))).thenReturn(cartEntity);

        ShoppingCart saved = adapter.save(cart);

        assertNotNull(saved);
        assertEquals(cartId, saved.getCartId());
        assertEquals(1, saved.getItems().size());
        verify(jpaRepository, times(1)).save(any(ShoppingCartJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));

        Optional<ShoppingCart> found = adapter.findById(cartId);

        assertTrue(found.isPresent());
        assertEquals(cartId, found.get().getCartId());
    }

    @Test
    void deleteById_Success() {
        adapter.deleteById(cartId);
        verify(jpaRepository, times(1)).deleteById(cartId);
    }
}
