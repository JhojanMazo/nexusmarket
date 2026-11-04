package application.adapters.useCases;

import application.adapters.aspect.Auditable;
import application.adapters.security.BCryptPasswordServiceAdapter;
import application.adapters.security.SecurityUserContext;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.enums.AuditSeverity;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.UserStatus;
import application.domain.events.OrderCancelledEvent;
import application.domain.events.OrderPaidEvent;
import application.domain.events.OrderPlacedEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.out.EventPublisherPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.ShoppingCartRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.inventory.CriticalStockValidationService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.logistics.ReturnsAdministrationService;
import application.domain.services.product.ProductTypeDifferentiationService;
import application.domain.services.user.PrimaryAddressManagementService;
import application.domain.services.user.SecondaryAddressManagementService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BuyerUseCaseImpl implements BuyerPort {

    private final UserRepositoryPort userRepository;
    private final ShoppingCartRepositoryPort cartRepository;
    private final ProductRepositoryPort productRepository;
    private final OrderRepositoryPort orderRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final ReturnRequestRepositoryPort returnRequestRepository;
    private final UniqueIdentityValidationService uniqueIdentityValidationService;
    private final ReturnsAdministrationService returnsAdministrationService;
    private final InventoryReservationService inventoryReservationService;
    private final CriticalStockValidationService criticalStockValidationService;
    private final ProductTypeDifferentiationService productTypeDifferentiationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;
    private final PrimaryAddressManagementService primaryAddressManagementService;
    private final SecondaryAddressManagementService secondaryAddressManagementService;
    private final EventPublisherPort eventPublisherPort;
    private final PasswordServicePort passwordServicePort = new BCryptPasswordServiceAdapter();

    @Autowired
    public BuyerUseCaseImpl(UserRepositoryPort userRepository,
                            ShoppingCartRepositoryPort cartRepository,
                            ProductRepositoryPort productRepository,
                            OrderRepositoryPort orderRepository,
                            InventoryRepositoryPort inventoryRepository,
                            ReturnRequestRepositoryPort returnRequestRepository,
                            UniqueIdentityValidationService uniqueIdentityValidationService,
                            ReturnsAdministrationService returnsAdministrationService,
                            InventoryReservationService inventoryReservationService,
                            CriticalStockValidationService criticalStockValidationService,
                            ProductTypeDifferentiationService productTypeDifferentiationService,
                            ValidateBuyerOwnershipService validateBuyerOwnershipService,
                            PrimaryAddressManagementService primaryAddressManagementService,
                            SecondaryAddressManagementService secondaryAddressManagementService,
                            EventPublisherPort eventPublisherPort) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.uniqueIdentityValidationService = uniqueIdentityValidationService;
        this.returnsAdministrationService = returnsAdministrationService;
        this.inventoryReservationService = inventoryReservationService;
        this.criticalStockValidationService = criticalStockValidationService;
        this.productTypeDifferentiationService = productTypeDifferentiationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
        this.primaryAddressManagementService = primaryAddressManagementService;
        this.secondaryAddressManagementService = secondaryAddressManagementService;
        this.eventPublisherPort = eventPublisherPort;
    }

    public BuyerUseCaseImpl(UserRepositoryPort userRepository,
                            ShoppingCartRepositoryPort cartRepository,
                            ProductRepositoryPort productRepository,
                            OrderRepositoryPort orderRepository,
                            InventoryRepositoryPort inventoryRepository,
                            ReturnRequestRepositoryPort returnRequestRepository,
                            UniqueIdentityValidationService uniqueIdentityValidationService,
                            ReturnsAdministrationService returnsAdministrationService,
                            InventoryReservationService inventoryReservationService,
                            CriticalStockValidationService criticalStockValidationService,
                            ProductTypeDifferentiationService productTypeDifferentiationService,
                            ValidateBuyerOwnershipService validateBuyerOwnershipService,
                            PrimaryAddressManagementService primaryAddressManagementService,
                            SecondaryAddressManagementService secondaryAddressManagementService) {
        this(userRepository, cartRepository, productRepository, orderRepository, inventoryRepository,
                returnRequestRepository, uniqueIdentityValidationService, returnsAdministrationService,
                inventoryReservationService, criticalStockValidationService, productTypeDifferentiationService,
                validateBuyerOwnershipService, primaryAddressManagementService, secondaryAddressManagementService, null);
    }

    public BuyerUseCaseImpl(UserRepositoryPort userRepository,
                            ShoppingCartRepositoryPort cartRepository,
                            ProductRepositoryPort productRepository,
                            OrderRepositoryPort orderRepository,
                            ReturnRequestRepositoryPort returnRequestRepository,
                            UniqueIdentityValidationService uniqueIdentityValidationService,
                            ReturnsAdministrationService returnsAdministrationService) {
        this(userRepository, cartRepository, productRepository, orderRepository, null, returnRequestRepository,
                uniqueIdentityValidationService, returnsAdministrationService,
                new InventoryReservationService(), new CriticalStockValidationService(),
                new ProductTypeDifferentiationService(), new ValidateBuyerOwnershipService(),
                new PrimaryAddressManagementService(), new SecondaryAddressManagementService());
    }

    public BuyerUseCaseImpl(UserRepositoryPort userRepository,
                            ShoppingCartRepositoryPort cartRepository,
                            ProductRepositoryPort productRepository,
                            OrderRepositoryPort orderRepository,
                            UniqueIdentityValidationService uniqueIdentityValidationService) {
        this(userRepository, cartRepository, productRepository, orderRepository, null,
                uniqueIdentityValidationService, new ReturnsAdministrationService());
    }

    @Override
    @Transactional
    @Auditable(action = "BUYER_REGISTERED", resource = "Buyer", severity = AuditSeverity.LOW)
    public Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress) {
        String generatedCompliantPassword = "Nx" + UUID.randomUUID().toString().replace("-", "") + "9a!";
        return registerBuyer(fullName, email, docType, docNumber, primaryAddress, generatedCompliantPassword);
    }

    @Override
    @Transactional
    @Auditable(action = "BUYER_REGISTERED", resource = "Buyer", severity = AuditSeverity.LOW)
    public Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress, String rawPassword) {
        uniqueIdentityValidationService.validateUniqueIdentity(userRepository, email, docType, docNumber);

        String effectivePassword;
        if (rawPassword != null && !rawPassword.isBlank()) {
            PublicAccessUseCaseImpl.validatePasswordPolicy(rawPassword);
            effectivePassword = rawPassword;
        } else {
            effectivePassword = "Nx" + UUID.randomUUID().toString().replace("-", "") + "9a!";
        }
        String passwordHash = passwordServicePort.encode(effectivePassword);

        Buyer buyer = Buyer.create(
                fullName,
                new Email(email),
                new IdentityDocument(docType, docNumber),
                primaryAddress,
                passwordHash
        );
        User saved = userRepository.save(buyer);
        return (Buyer) saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Buyer getBuyerProfile(UUID buyerId) {
        assertAuthenticatedBuyerMatches(buyerId);
        User user = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Comprador", buyerId));
        if (!(user instanceof Buyer buyer)) {
            throw new IllegalArgumentException("El usuario con ID " + buyerId + " no es un comprador");
        }
        return buyer;
    }

    @Override
    @Transactional
    public Buyer addAdditionalAddress(UUID buyerId, Address address) {
        return addAdditionalAddress(buyerId, buyerId, address);
    }

    @Override
    @Transactional
    public Buyer addAdditionalAddress(UUID authenticatedBuyerId, UUID buyerId, Address address) {
        UUID effectiveAuthId = authenticatedBuyerId != null ? authenticatedBuyerId : buyerId;
        validateBuyerOwnershipService.validateOwnership(effectiveAuthId, buyerId);
        Buyer buyer = getBuyerProfile(buyerId);
        secondaryAddressManagementService.addSecondaryAddress(buyer, address);
        return (Buyer) userRepository.save(buyer);
    }

    @Override
    @Transactional
    public Buyer updateAdditionalAddress(UUID authenticatedBuyerId, UUID buyerId, int index, Address newAddress) {
        UUID effectiveAuthId = authenticatedBuyerId != null ? authenticatedBuyerId : buyerId;
        validateBuyerOwnershipService.validateOwnership(effectiveAuthId, buyerId);
        Buyer buyer = getBuyerProfile(buyerId);
        secondaryAddressManagementService.updateSecondaryAddress(buyer, index, newAddress);
        return (Buyer) userRepository.save(buyer);
    }

    @Override
    @Transactional
    public Buyer removeAdditionalAddress(UUID authenticatedBuyerId, UUID buyerId, int index) {
        UUID effectiveAuthId = authenticatedBuyerId != null ? authenticatedBuyerId : buyerId;
        validateBuyerOwnershipService.validateOwnership(effectiveAuthId, buyerId);
        Buyer buyer = getBuyerProfile(buyerId);
        secondaryAddressManagementService.removeSecondaryAddress(buyer, index);
        return (Buyer) userRepository.save(buyer);
    }

    @Override
    @Transactional
    public Buyer updateAddresses(UUID buyerId, Address primaryAddress, List<Address> additionalAddresses) {
        return updateAddresses(buyerId, buyerId, primaryAddress, additionalAddresses);
    }

    @Override
    @Transactional
    public Buyer updateAddresses(UUID authenticatedBuyerId, UUID buyerId, Address primaryAddress, List<Address> additionalAddresses) {
        UUID effectiveAuthId = authenticatedBuyerId != null ? authenticatedBuyerId : buyerId;
        validateBuyerOwnershipService.validateOwnership(effectiveAuthId, buyerId);
        Buyer buyer = getBuyerProfile(buyerId);
        if (primaryAddress != null) {
            primaryAddressManagementService.updatePrimaryAddress(buyer, primaryAddress);
        }
        if (additionalAddresses != null) {
            buyer.replaceAdditionalAddresses(additionalAddresses);
        }
        return (Buyer) userRepository.save(buyer);
    }

    @Override
    @Transactional
    public ShoppingCart createCart(UUID buyerId) {
        assertAuthenticatedBuyerMatches(buyerId);
        User user = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Comprador", buyerId));

        if (!(user instanceof Buyer buyer)) {
            throw new IllegalArgumentException("El usuario especificado no tiene rol de COMPRADOR");
        }

        if (buyer.getStatus() != UserStatus.ACTIVE || buyer.getCommercialStatus() != BuyerCommercialStatus.ENABLED) {
            throw new BusinessRuleException("El comprador no está habilitado comercialmente para realizar compras");
        }

        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingCart getCartById(UUID cartId) {
        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito de compras", cartId));
        assertAuthenticatedBuyerMatches(cart.getBuyer().getUserId());
        return cart;
    }

    @Override
    @Transactional
    public ShoppingCart getCartByBuyerId(UUID buyerId) {
        assertAuthenticatedBuyerMatches(buyerId);
        return cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> createCart(buyerId));
    }

    @Override
    @Transactional
    public ShoppingCart addItem(UUID cartId, UUID productId, int quantity, BigDecimal unitPrice, String currencyCode) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        ShoppingCart cart = getCartById(cartId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));

        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BusinessRuleException("Solo se pueden agregar productos con estado PUBLICADO al carrito");
        }

        if (currencyCode != null && !currencyCode.isBlank()) {
            Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT));
        }

        Money authoritativePrice = product.getPrice();

        OrderItem existingItem = cart.getItems().stream()
                .filter(i -> i.product().getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int combinedQuantity = existingItem.quantity() + quantity;
            cart.removeItem(existingItem);
            cart.addItem(new OrderItem(product, combinedQuantity, authoritativePrice));
        } else {
            OrderItem item = new OrderItem(product, quantity, authoritativePrice);
            cart.addItem(item);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public ShoppingCart updateItemQuantity(UUID cartId, UUID productId, int quantity) {
        if (quantity <= 0) {
            return removeItem(cartId, productId);
        }
        ShoppingCart cart = getCartById(cartId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));

        OrderItem existingItem = cart.getItems().stream()
                .filter(i -> i.product().getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        Money price = existingItem != null ? existingItem.unitPrice() : product.getPrice();
        if (existingItem != null) {
            cart.removeItem(existingItem);
        }
        cart.addItem(new OrderItem(product, quantity, price));
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public ShoppingCart removeItem(UUID cartId, UUID productId) {
        ShoppingCart cart = getCartById(cartId);
        cart.getItems().stream()
                .filter(item -> item.product().getProductId().equals(productId))
                .findFirst()
                .ifPresent(cart::removeItem);

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(UUID cartId) {
        getCartById(cartId);
        cartRepository.deleteById(cartId);
    }

    @Override
    @Transactional
    @Auditable(action = "ORDER_CREATED", resource = "Order", severity = AuditSeverity.MEDIUM)
    public Order createOrderFromCart(UUID cartId, Address deliveryAddress) {
        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito de compras", cartId));
        assertAuthenticatedBuyerMatches(cart.getBuyer().getUserId());

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("No es posible generar una orden a partir de un carrito vacío");
        }

        Address finalAddress = deliveryAddress != null ? deliveryAddress : cart.getBuyer().getPrimaryAddress();
        if (finalAddress == null) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria para procesar la orden");
        }

        Order order = Order.createFromCart(cart, finalAddress);
        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteById(cartId);

        if (eventPublisherPort != null) {
            eventPublisherPort.publish(new OrderPlacedEvent(
                    savedOrder.getOrderId(),
                    savedOrder.getBuyer().getUserId(),
                    savedOrder.total(),
                    savedOrder.getItems(),
                    Instant.now()
            ));
        }

        return savedOrder;
    }

    @Transactional
    public Order createOrder(UUID cartId, Address deliveryAddress) {
        return createOrderFromCart(cartId, deliveryAddress);
    }

    @Override
    @Transactional
    @Auditable(action = "ORDER_CANCELLED", resource = "Order", severity = AuditSeverity.HIGH)
    public Order cancelOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        order.cancel();
        if (inventoryRepository != null && previousStatus == OrderStatus.PENDING_PAYMENT) {
            for (OrderItem item : order.getItems()) {
                List<Inventory> inventories = inventoryRepository.findByProductId(item.product().getProductId());
                for (Inventory inv : inventories) {
                    if (inv.getReservedQuantity() >= item.quantity()) {
                        inventoryReservationService.releaseReservation(inv, item.quantity());
                        inventoryRepository.save(inv);
                        break;
                    }
                }
            }
        }
        Order savedOrder = orderRepository.save(order);
        if (eventPublisherPort != null) {
            eventPublisherPort.publish(new OrderCancelledEvent(
                    savedOrder.getOrderId(),
                    "Orden cancelada por el comprador",
                    savedOrder.getItems(),
                    Instant.now()
            ));
        }
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        if (order.getBuyer() != null) {
            assertAuthenticatedBuyerMatches(order.getBuyer().getUserId());
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByBuyerId(UUID buyerId) {
        assertAuthenticatedBuyerMatches(buyerId);
        return orderRepository.findByBuyerId(buyerId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> getOrdersByBuyerId(UUID buyerId, int page, int size, String sortBy, String direction) {
        assertAuthenticatedBuyerMatches(buyerId);
        return orderRepository.findByBuyerId(buyerId, page, size, sortBy, direction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products_catalog", key = "#page + '-' + #size")
    public PageResult<Product> getProductsCatalog(int page, int size) {
        return productRepository.findByStatus(ProductStatus.PUBLISHED, page, size, "name", "ASC");
    }

    @Override
    @Transactional
    public Order updateDeliveryAddress(UUID orderId, Address newAddress) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED_COMPLETED) {
            throw new IllegalStateException("No se puede modificar la dirección de una orden que ya fue enviada o completada");
        }
        order.setDeliveryAddress(newAddress);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    @Auditable(action = "ORDER_PAYMENT_PROCESSED", resource = "Order", severity = AuditSeverity.HIGH)
    public Order processPayment(UUID orderId, BigDecimal amount, String paymentMethod) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED_COMPLETED) {
            throw new IllegalStateException("La orden ya se encuentra pagada o en proceso de despacho (estado actual: " + order.getStatus() + ")");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("No se puede procesar el pago de una orden cancelada");
        }
        if (order.getStatus() != OrderStatus.CART && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Estado inválido para procesar pago: " + order.getStatus());
        }

        BigDecimal expectedTotal = order.total().amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de pago debe ser mayor a 0");
        }
        if (amount.compareTo(expectedTotal) != 0) {
            throw new IllegalArgumentException("El monto pagado (" + amount + ") no coincide con el total real de la orden (" + expectedTotal + ")");
        }
        if (paymentMethod != null && ("DECLINED".equalsIgnoreCase(paymentMethod.trim())
                || "FAILED".equalsIgnoreCase(paymentMethod.trim())
                || "REJECTED".equalsIgnoreCase(paymentMethod.trim()))) {
            throw new BusinessRuleException("El pago fue rechazado por la pasarela de pagos");
        }

        for (OrderItem item : order.getItems()) {
            if (productTypeDifferentiationService.requiresShipping(item.product()) && inventoryRepository != null) {
                List<Inventory> productInventories = inventoryRepository.findByProductId(item.product().getProductId());
                if (productInventories.isEmpty()) {
                    throw new IllegalStateException("No existen lotes de inventario disponibles para el producto físico: " + item.product().getName());
                }
                Inventory selectedLot = productInventories.stream()
                        .filter(inv -> criticalStockValidationService.validateStock(inv, item.quantity()))
                        .findFirst()
                        .orElseGet(() -> productInventories.get(0));
                criticalStockValidationService.assertValidActiveLotAndNonNegativeBalance(selectedLot, item.quantity());
                inventoryReservationService.reserve(selectedLot, item.quantity());
                inventoryRepository.save(selectedLot);
            }
        }

        if (order.getStatus() == OrderStatus.CART) {
            order.advanceTo(OrderStatus.PENDING_PAYMENT);
        }
        order.advanceTo(OrderStatus.PAID);

        boolean allDigital = order.isDigitalOnly() || (!order.getItems().isEmpty() && order.getItems().stream()
                .allMatch(item -> productTypeDifferentiationService.isDigital(item.product())));

        if (allDigital && order.getStatus() == OrderStatus.PAID) {
            order.advanceTo(OrderStatus.DELIVERED_COMPLETED);
        }

        Order savedOrder = orderRepository.save(order);

        if (eventPublisherPort != null) {
            String paymentReference = (paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "PAYMENT")
                    + "-" + savedOrder.getOrderId();
            eventPublisherPort.publish(new OrderPaidEvent(
                    savedOrder.getOrderId(),
                    savedOrder.getBuyer().getUserId(),
                    paymentReference,
                    Instant.now()
            ));
        }

        return savedOrder;
    }

    @Override
    @Transactional
    @Auditable(action = "RETURN_REQUESTED", resource = "ReturnRequest", severity = AuditSeverity.MEDIUM)
    public ReturnRequest requestReturn(UUID orderId, String reason) {
        Order order = getOrderById(orderId);
        ReturnRequest returnRequest = returnsAdministrationService.initiateReturn(order, reason);
        return returnRequestRepository.save(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> getBuyerReturns(UUID buyerId) {
        getBuyerProfile(buyerId);
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        return orders.stream()
                .flatMap(o -> returnRequestRepository.findByOrderId(o.getOrderId()).stream())
                .toList();
    }

    private void assertAuthenticatedBuyerMatches(UUID targetBuyerId) {
        AuthenticatedUserPrincipal principal = SecurityUserContext.get().orElse(null);
        if (principal != null && "BUYER".equalsIgnoreCase(principal.role()) && targetBuyerId != null) {
            validateBuyerOwnershipService.validateOwnership(principal.userId(), targetBuyerId);
        }
    }
}
