package application.adapters.persistence.sql;
import application.adapters.persistence.jpa.*;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.valueobjects.*;
import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInvoiceRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceJpaAdapterTest {

    @Mock
    private SpringDataJpaInvoiceRepository jpaRepository;

    @Mock
    private SpringDataJpaOrderRepository orderJpaRepository;

    @Mock
    private SpringDataJpaProductRepository productJpaRepository;

    private InvoiceJpaMapper mapper;
    private OrderJpaMapper orderMapper;
    private InvoiceJpaAdapter adapter;

    private Invoice invoice;
    private InvoiceJpaEntity invoiceEntity;
    private OrderJpaEntity orderEntity;
    private UUID invoiceId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        UserJpaMapper userMapper = new UserJpaMapper();
        ProductJpaMapper productMapper = new ProductJpaMapper();
        orderMapper = new OrderJpaMapper(userMapper, productMapper);
        mapper = new InvoiceJpaMapper(orderMapper);
        adapter = new InvoiceJpaAdapter(jpaRepository, orderJpaRepository, productJpaRepository, mapper, orderMapper);

        Buyer buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Catalina Ruiz",
                new Email("catalina@example.com"),
                new IdentityDocument("CC", "32165498"),
                new Address("Calle 5", "Medellin", "Antioquia", "Colombia", "05001", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                application.domain.enums.UserStatus.ACTIVE
        );

        Product product = new Product(
                UUID.randomUUID(),
                "Silla Ergonomica",
                new ProductVariant("Color", "Negro"),
                new Money(new BigDecimal("250.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        orderId = UUID.randomUUID();
        Order order = Order.reconstitute(
                orderId,
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("250.00"), Currency.getInstance("USD")))),
                buyer.getPrimaryAddress(),
                OrderStatus.PAID,
                LocalDate.now()
        );

        ProductJpaEntity pEntity = productMapper.toEntity(product);
        when(productJpaRepository.findById(product.getProductId())).thenReturn(Optional.of(pEntity));
        orderEntity = orderMapper.toEntity(order, productJpaRepository);

        invoiceId = UUID.randomUUID();
        invoice = new Invoice(
                invoiceId,
                order,
                LocalDate.now(),
                new Money(new BigDecimal("250.00"), Currency.getInstance("USD")),
                new Money(new BigDecimal("47.50"), Currency.getInstance("USD"))
        );
        invoiceEntity = mapper.toEntity(invoice, orderEntity);
    }

    @Test
    void save_Success() {
        when(orderJpaRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(jpaRepository.save(any(InvoiceJpaEntity.class))).thenReturn(invoiceEntity);

        Invoice saved = adapter.save(invoice);

        assertNotNull(saved);
        assertEquals(invoiceId, saved.getInvoiceId());
        assertEquals(new BigDecimal("297.50"), saved.getTotal().amount());
        verify(jpaRepository, times(1)).save(any(InvoiceJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(invoiceId)).thenReturn(Optional.of(invoiceEntity));

        Optional<Invoice> found = adapter.findById(invoiceId);

        assertTrue(found.isPresent());
        assertEquals(invoiceId, found.get().getInvoiceId());
    }

    @Test
    void findByOrderId_Success() {
        when(jpaRepository.findByOrder_OrderId(orderId)).thenReturn(Optional.of(invoiceEntity));

        Optional<Invoice> found = adapter.findByOrderId(orderId);

        assertTrue(found.isPresent());
        assertEquals(invoiceId, found.get().getInvoiceId());
    }
}
