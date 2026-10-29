package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.AddCartItemRequestDTO;
import application.adapters.rest.dtos.requests.AddressDTO;
import application.adapters.rest.dtos.requests.CreateBuyerRequestDTO;
import application.adapters.rest.dtos.requests.CreateCartRequestDTO;
import application.adapters.rest.dtos.requests.CreateOrderRequestDTO;
import application.adapters.rest.dtos.requests.ProcessPaymentRequestDTO;
import application.adapters.rest.dtos.requests.UpdateBuyerAddressesRequestDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.PageResponseDTO;
import application.adapters.rest.dtos.responses.PaymentResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.ShoppingCartResponseDTO;
import application.adapters.rest.mappers.CartRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.ports.in.BuyerPort;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/buyers")
public class BuyerRestController {

    private final BuyerPort buyerPort;
    private final UserRestMapper userRestMapper;
    private final CartRestMapper cartRestMapper;
    private final OrderRestMapper orderRestMapper;
    private final ReturnRequestRestMapper returnRequestRestMapper;
    private final ProductRestMapper productRestMapper;

    @Autowired
    public BuyerRestController(BuyerPort buyerPort,
                               UserRestMapper userRestMapper,
                               CartRestMapper cartRestMapper,
                               OrderRestMapper orderRestMapper,
                               ReturnRequestRestMapper returnRequestRestMapper,
                               ProductRestMapper productRestMapper) {
        this.buyerPort = buyerPort;
        this.userRestMapper = userRestMapper;
        this.cartRestMapper = cartRestMapper;
        this.orderRestMapper = orderRestMapper;
        this.returnRequestRestMapper = returnRequestRestMapper;
        this.productRestMapper = productRestMapper;
    }

    public BuyerRestController(BuyerPort buyerPort,
                               UserRestMapper userRestMapper,
                               CartRestMapper cartRestMapper,
                               OrderRestMapper orderRestMapper,
                               ReturnRequestRestMapper returnRequestRestMapper) {
        this(buyerPort, userRestMapper, cartRestMapper, orderRestMapper,
                returnRequestRestMapper, new ProductRestMapper());
    }

    public BuyerRestController(BuyerPort buyerPort,
                               UserRestMapper userRestMapper,
                               CartRestMapper cartRestMapper,
                               OrderRestMapper orderRestMapper) {
        this(buyerPort, userRestMapper, cartRestMapper, orderRestMapper,
                new ReturnRequestRestMapper(), new ProductRestMapper());
    }

    @PostMapping("/register")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@RequestBody CreateBuyerRequestDTO request) {
        Buyer buyer = buyerPort.registerBuyer(
                request.fullName(),
                request.email(),
                request.documentType(),
                request.documentNumber(),
                userRestMapper.toAddress(request.primaryAddress())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestMapper.toBuyerResponse(buyer));
    }

    @GetMapping("/catalog")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getCatalog(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PageResult<Product> catalogPage = buyerPort.getProductsCatalog(page, size);
        return ResponseEntity.ok(PageResponseDTO.from(catalogPage, ProductRestMapper::toResponse));
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<BuyerResponseDTO> getBuyerProfile(@PathVariable("buyerId") UUID buyerId) {
        Buyer buyer = buyerPort.getBuyerProfile(buyerId);
        return ResponseEntity.ok(userRestMapper.toBuyerResponse(buyer));
    }

    @PostMapping("/{buyerId}/addresses")
    public ResponseEntity<BuyerResponseDTO> addAdditionalAddress(
            @PathVariable("buyerId") UUID buyerId,
            @RequestParam(name = "authenticatedBuyerId", required = false) UUID authenticatedBuyerId,
            @RequestBody AddressDTO addressDto) {
        Buyer buyer = authenticatedBuyerId != null
                ? buyerPort.addAdditionalAddress(authenticatedBuyerId, buyerId, userRestMapper.toAddress(addressDto))
                : buyerPort.addAdditionalAddress(buyerId, userRestMapper.toAddress(addressDto));
        return ResponseEntity.ok(userRestMapper.toBuyerResponse(buyer));
    }

    @PutMapping("/{buyerId}/addresses/{index}")
    public ResponseEntity<BuyerResponseDTO> updateAdditionalAddress(
            @PathVariable("buyerId") UUID buyerId,
            @PathVariable("index") int index,
            @RequestParam(name = "authenticatedBuyerId", required = false) UUID authenticatedBuyerId,
            @RequestBody AddressDTO addressDto) {
        Buyer buyer = buyerPort.updateAdditionalAddress(
                authenticatedBuyerId != null ? authenticatedBuyerId : buyerId,
                buyerId,
                index,
                userRestMapper.toAddress(addressDto)
        );
        return ResponseEntity.ok(userRestMapper.toBuyerResponse(buyer));
    }

    @DeleteMapping("/{buyerId}/addresses/{index}")
    public ResponseEntity<BuyerResponseDTO> removeAdditionalAddress(
            @PathVariable("buyerId") UUID buyerId,
            @PathVariable("index") int index,
            @RequestParam(name = "authenticatedBuyerId", required = false) UUID authenticatedBuyerId) {
        Buyer buyer = buyerPort.removeAdditionalAddress(
                authenticatedBuyerId != null ? authenticatedBuyerId : buyerId,
                buyerId,
                index
        );
        return ResponseEntity.ok(userRestMapper.toBuyerResponse(buyer));
    }

    @PutMapping("/{buyerId}/addresses")
    public ResponseEntity<BuyerResponseDTO> updateAddresses(
            @PathVariable("buyerId") UUID buyerId,
            @RequestParam(name = "authenticatedBuyerId", required = false) UUID authenticatedBuyerId,
            @RequestBody UpdateBuyerAddressesRequestDTO request) {
        Address primary = request.primaryAddress() != null ? userRestMapper.toAddress(request.primaryAddress()) : null;
        List<Address> additional = request.additionalAddresses() != null
                ? request.additionalAddresses().stream().map(userRestMapper::toAddress).toList()
                : null;
        Buyer buyer = authenticatedBuyerId != null
                ? buyerPort.updateAddresses(authenticatedBuyerId, buyerId, primary, additional)
                : buyerPort.updateAddresses(buyerId, primary, additional);
        return ResponseEntity.ok(userRestMapper.toBuyerResponse(buyer));
    }

    @PostMapping("/{buyerId}/cart")
    public ResponseEntity<ShoppingCartResponseDTO> createCart(@PathVariable("buyerId") UUID buyerId) {
        ShoppingCart cart = buyerPort.createCart(buyerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartRestMapper.toResponse(cart));
    }

    @PostMapping("/carts")
    public ResponseEntity<ShoppingCartResponseDTO> createCartByBody(@RequestBody CreateCartRequestDTO request) {
        ShoppingCart cart = buyerPort.createCart(request.buyerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartRestMapper.toResponse(cart));
    }

    @GetMapping("/{buyerId}/cart")
    public ResponseEntity<ShoppingCartResponseDTO> getCartByBuyerId(@PathVariable("buyerId") UUID buyerId) {
        ShoppingCart cart = buyerPort.getCartByBuyerId(buyerId);
        return ResponseEntity.ok(cartRestMapper.toResponse(cart));
    }

    @GetMapping("/carts/{cartId}")
    public ResponseEntity<ShoppingCartResponseDTO> getCartById(@PathVariable("cartId") UUID cartId) {
        ShoppingCart cart = buyerPort.getCartById(cartId);
        return ResponseEntity.ok(cartRestMapper.toResponse(cart));
    }

    @PostMapping("/carts/{cartId}/items")
    public ResponseEntity<ShoppingCartResponseDTO> addItem(@PathVariable("cartId") UUID cartId,
                                                          @RequestBody AddCartItemRequestDTO request) {
        ShoppingCart cart = buyerPort.addItem(
                cartId,
                request.productId(),
                request.quantity(),
                request.unitPrice(),
                request.currency()
        );
        return ResponseEntity.ok(cartRestMapper.toResponse(cart));
    }

    @PutMapping("/carts/{cartId}/items/{productId}")
    public ResponseEntity<ShoppingCartResponseDTO> updateItemQuantity(@PathVariable("cartId") UUID cartId,
                                                                      @PathVariable("productId") UUID productId,
                                                                      @RequestBody Map<String, Integer> body) {
        int quantity = body != null && body.get("quantity") != null ? body.get("quantity") : 1;
        ShoppingCart cart = buyerPort.updateItemQuantity(cartId, productId, quantity);
        return ResponseEntity.ok(cartRestMapper.toResponse(cart));
    }

    @DeleteMapping("/carts/{cartId}/items/{productId}")
    public ResponseEntity<ShoppingCartResponseDTO> removeItem(@PathVariable("cartId") UUID cartId,
                                                             @PathVariable("productId") UUID productId) {
        ShoppingCart cart = buyerPort.removeItem(cartId, productId);
        return ResponseEntity.ok(cartRestMapper.toResponse(cart));
    }

    @DeleteMapping("/carts/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable("cartId") UUID cartId) {
        buyerPort.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody CreateOrderRequestDTO request) {
        Order order = buyerPort.createOrderFromCart(
                request.cartId(),
                userRestMapper.toAddress(request.deliveryAddress())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(orderRestMapper.toResponse(order));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("orderId") UUID orderId) {
        Order order = buyerPort.getOrderById(orderId);
        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    @GetMapping("/{buyerId}/orders")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getOrdersByBuyerId(
            @PathVariable("buyerId") UUID buyerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "orderDate") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction) {
        PageResult<Order> ordersPage = buyerPort.getOrdersByBuyerId(buyerId, page, size, sortBy, direction);
        return ResponseEntity.ok(PageResponseDTO.from(ordersPage, OrderRestMapper::toResponse));
    }

    @PutMapping("/orders/{orderId}/delivery-address")
    public ResponseEntity<OrderResponseDTO> updateDeliveryAddress(@PathVariable("orderId") UUID orderId,
                                                                 @RequestBody AddressDTO newAddress) {
        Order order = buyerPort.updateDeliveryAddress(orderId, userRestMapper.toAddress(newAddress));
        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<PaymentResponseDTO> payOrder(@PathVariable("orderId") UUID orderId,
                                                      @RequestBody ProcessPaymentRequestDTO request) {
        Order order = buyerPort.processPayment(
                orderId,
                request.amount(),
                request.paymentMethod()
        );
        UUID paymentId = UUID.randomUUID();
        PaymentResponseDTO response = new PaymentResponseDTO(
                paymentId,
                order.getOrderId(),
                "APPROVED",
                request.amount() != null ? request.amount() : order.total().amount(),
                request.currency() != null ? request.currency() : order.total().currency().getCurrencyCode(),
                request.paymentMethod() != null ? request.paymentMethod() : "CREDIT_CARD",
                Instant.now(),
                "TXN-" + paymentId.toString().substring(0, 8).toUpperCase()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable("orderId") UUID orderId) {
        Order cancelled = buyerPort.cancelOrder(orderId);
        return ResponseEntity.ok(orderRestMapper.toResponse(cancelled));
    }

    @PostMapping("/orders/{orderId}/return")
    public ResponseEntity<Map<String, Object>> requestReturn(@PathVariable("orderId") UUID orderId,
                                                            @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "Customer requested return");
        ReturnRequest returnRequest = buyerPort.requestReturn(orderId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "requestId", returnRequest.getRequestId(),
                "orderId", orderId,
                "status", returnRequest.getStatus().name()
        ));
    }

    @GetMapping("/{buyerId}/returns")
    public ResponseEntity<PageResponseDTO<ReturnRequestResponseDTO>> getBuyerReturns(
            @PathVariable("buyerId") UUID buyerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<ReturnRequest> returns = buyerPort.getBuyerReturns(buyerId);
        return ResponseEntity.ok(PageResponseDTO.of(returnRequestRestMapper.toResponseList(returns), page, size));
    }
}
