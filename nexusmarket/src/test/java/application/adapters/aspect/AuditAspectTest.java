package application.adapters.aspect;

import application.adapters.persistence.mongodb.AuditLogMongoAdapter;
import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.adapters.security.SecurityUserContext;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.adapters.useCases.SellerUseCaseImpl;
import application.domain.enums.AuditSeverity;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.ports.in.SellerPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.user.UniqueIdentityValidationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests verifying that {@link AuditAspect} intercepts critical operations via Spring AOP,
 * enriches audit entries with {@link SecurityUserContext}, and persists {@link AuditLogDocument}
 * records into MongoDB through {@link RegisterAuditLogService} and {@link AuditLogMongoAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogMongoRepository mongoRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private UniqueIdentityValidationService uniqueIdentityValidationService;

    private SellerPort proxiedSellerUseCase;

    @BeforeEach
    void setUp() {
        AuditLogMongoAdapter mongoAdapter = new AuditLogMongoAdapter(mongoRepository);
        RegisterAuditLogService registerAuditLogService = new RegisterAuditLogService(mongoAdapter);
        AuditAspect auditAspect = new AuditAspect(registerAuditLogService);

        SellerUseCaseImpl target = new SellerUseCaseImpl(
                userRepository,
                productRepository,
                orderRepository,
                uniqueIdentityValidationService
        );

        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(auditAspect);
        proxiedSellerUseCase = proxyFactory.getProxy();
    }

    @AfterEach
    void tearDown() {
        SecurityUserContext.clear();
    }

    @Test
    void createProduct_automaticallyAuditsOperationToMongoDbWithSecurityContextUser() {
        UUID sellerUserId = UUID.randomUUID();
        SecurityUserContext.set(new AuthenticatedUserPrincipal(sellerUserId, "seller@nexusmarket.com", "SELLER"));

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mongoRepository.save(any(AuditLogDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        Product created = proxiedSellerUseCase.createProduct(
                "Teclado Mecánico",
                "Switch",
                "Blue",
                new BigDecimal("110.00"),
                "USD",
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        assertNotNull(created);
        ArgumentCaptor<AuditLogDocument> docCaptor = ArgumentCaptor.forClass(AuditLogDocument.class);
        verify(mongoRepository).save(docCaptor.capture());

        AuditLogDocument savedDoc = docCaptor.getValue();
        assertEquals("PRODUCT_CREATED", savedDoc.getAction());
        assertEquals("Product", savedDoc.getResource());
        assertEquals(created.getProductId().toString(), savedDoc.getResourceId());
        assertEquals(sellerUserId.toString(), savedDoc.getUserId());
        assertEquals("seller@nexusmarket.com", savedDoc.getUserEmail());
        assertEquals(AuditSeverity.MEDIUM.name(), savedDoc.getSeverity());
        assertTrue(savedDoc.getDetails().contains("createProduct"));
    }

    @Test
    void failedCriticalOperation_recordsAuditWithHighSeverityAndRethrowsException() {
        UUID sellerUserId = UUID.randomUUID();
        UUID missingProductId = UUID.randomUUID();
        SecurityUserContext.set(new AuthenticatedUserPrincipal(sellerUserId, "seller@nexusmarket.com", "SELLER"));

        when(productRepository.findById(missingProductId)).thenReturn(Optional.empty());
        when(mongoRepository.save(any(AuditLogDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(RuntimeException.class, () ->
                proxiedSellerUseCase.updateProductPrice(missingProductId, new BigDecimal("200.00"), "USD"));

        ArgumentCaptor<AuditLogDocument> docCaptor = ArgumentCaptor.forClass(AuditLogDocument.class);
        verify(mongoRepository).save(docCaptor.capture());

        AuditLogDocument savedDoc = docCaptor.getValue();
        assertEquals("PRODUCT_PRICE_UPDATED", savedDoc.getAction());
        assertEquals("Product", savedDoc.getResource());
        assertEquals(missingProductId.toString(), savedDoc.getResourceId());
        assertEquals(AuditSeverity.HIGH.name(), savedDoc.getSeverity());
        assertTrue(savedDoc.getDetails().contains("Error en operación"));
    }

    @Test
    void readOnlyQueryMethods_areExcludedFromAutomaticAudit() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> proxiedSellerUseCase.getProductById(productId));
        verify(mongoRepository, never()).save(any());
    }
}
