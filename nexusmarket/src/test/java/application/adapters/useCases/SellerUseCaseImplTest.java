package application.adapters.useCases;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private UniqueIdentityValidationService uniqueIdentityValidationService;

    private SellerUseCaseImpl sellerUseCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sellerUseCase = new SellerUseCaseImpl(
                userRepository,
                productRepository,
                orderRepository,
                uniqueIdentityValidationService
        );
    }

    @Test
    void registerSeller_Success() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Seller seller = sellerUseCase.registerSeller(
                UUID.randomUUID(),
                "Seller Corp",
                "seller@nexus.com",
                "NIT",
                "900123",
                LocalDate.now()
        );

        assertNotNull(seller);
        assertEquals("Seller Corp", seller.getFullName());
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product product = sellerUseCase.createProduct(
                "Mechanical Keyboard",
                "Switch",
                "Cherry MX Red",
                BigDecimal.valueOf(120),
                "USD",
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        assertNotNull(product);
        assertEquals("Mechanical Keyboard", product.getName());
        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
    }

    @Test
    void updateProductPrice_Success() {
        Product existing = new Product(productId, "Keyboard", new ProductVariant("Switch", "Blue"),
                new Money(BigDecimal.valueOf(100), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product updated = sellerUseCase.updateProductPrice(productId, BigDecimal.valueOf(115), "USD");
        assertEquals(BigDecimal.valueOf(115), updated.getPrice().amount());
    }
}
