package application.adapters.persistence.sql;

import application.adapters.persistence.jpa.RefundJpaAdapter;
import application.adapters.persistence.jpa.ReturnRequestJpaAdapter;
import application.adapters.persistence.jpa.entities.RefundJpaEntity;
import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.RefundJpaMapper;
import application.adapters.persistence.jpa.mappers.ReturnRequestJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaRefundRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnRefundJpaAdapterTest {

    @Mock
    private SpringDataJpaReturnRequestRepository returnRequestRepository;

    @Mock
    private SpringDataJpaRefundRepository refundRepository;

    @Mock
    private SpringDataJpaOrderRepository orderRepository;

    @Mock
    private SpringDataJpaProductRepository productRepository;

    private ReturnRequestJpaAdapter returnRequestAdapter;
    private RefundJpaAdapter refundAdapter;

    private ReturnRequest returnRequest;
    private ReturnRequestJpaEntity returnEntity;
    private Refund refund;
    private RefundJpaEntity refundEntity;

    private UUID requestId;
    private UUID refundId;

    @BeforeEach
    void setUp() {
        UserJpaMapper userJpaMapper = new UserJpaMapper();
        ProductJpaMapper productJpaMapper = new ProductJpaMapper();
        OrderJpaMapper orderJpaMapper = new OrderJpaMapper(userJpaMapper, productJpaMapper);
        ReturnRequestJpaMapper returnRequestMapper = new ReturnRequestJpaMapper(orderJpaMapper);
        RefundJpaMapper refundMapper = new RefundJpaMapper(returnRequestMapper);

        returnRequestAdapter = new ReturnRequestJpaAdapter(returnRequestRepository, orderRepository, productRepository, returnRequestMapper);
        refundAdapter = new RefundJpaAdapter(refundRepository, returnRequestRepository, orderRepository, productRepository, refundMapper);

        Address address = new Address("Street", "City", "State", "Country", "11111", null);
        Buyer buyer = Buyer.create("John Buyer", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        Product product = new Product(UUID.randomUUID(), "Book", new ProductVariant("Edition", "1"),
                new Money(BigDecimal.TEN, Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        cart.addItem(new OrderItem(product, 2, new Money(BigDecimal.TEN, Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);
        order.advanceTo(OrderStatus.SHIPPED);
        order.advanceTo(OrderStatus.DELIVERED_COMPLETED);

        returnRequest = ReturnRequest.create(order);
        returnRequest.approve();
        requestId = returnRequest.getRequestId();
        returnEntity = returnRequestMapper.toEntity(returnRequest, orderRepository, productRepository);

        refund = Refund.create(returnRequest, new Money(new BigDecimal("20.00"), Currency.getInstance("USD")));
        refundId = refund.getRefundId();
        refundEntity = refundMapper.toEntity(refund, returnRequestRepository, orderRepository, productRepository);
    }

    @Test
    void saveReturnRequest_Success() {
        when(returnRequestRepository.save(any(ReturnRequestJpaEntity.class))).thenReturn(returnEntity);

        ReturnRequest saved = returnRequestAdapter.save(returnRequest);

        assertNotNull(saved);
        assertEquals(requestId, saved.getRequestId());
        assertEquals(ReturnRequest.ReturnRequestStatus.APPROVED, saved.getStatus());
        verify(returnRequestRepository, times(1)).save(any(ReturnRequestJpaEntity.class));
    }

    @Test
    void findReturnRequestById_Success() {
        when(returnRequestRepository.findById(requestId)).thenReturn(Optional.of(returnEntity));

        Optional<ReturnRequest> found = returnRequestAdapter.findById(requestId);

        assertTrue(found.isPresent());
        assertEquals(requestId, found.get().getRequestId());
    }

    @Test
    void saveRefund_Success() {
        when(refundRepository.save(any(RefundJpaEntity.class))).thenReturn(refundEntity);

        Refund saved = refundAdapter.save(refund);

        assertNotNull(saved);
        assertEquals(refundId, saved.getRefundId());
        assertEquals(Refund.RefundStatus.PENDING, saved.getStatus());
        verify(refundRepository, times(1)).save(any(RefundJpaEntity.class));
    }

    @Test
    void findRefundById_Success() {
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(refundEntity));

        Optional<Refund> found = refundAdapter.findById(refundId);

        assertTrue(found.isPresent());
        assertEquals(refundId, found.get().getRefundId());
    }
}
