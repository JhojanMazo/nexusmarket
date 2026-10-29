package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CreateSellerRequestDTO;
import application.adapters.rest.dtos.requests.ProductPriceUpdateRequestDTO;
import application.adapters.rest.dtos.requests.ProductRequestDTO;
import application.adapters.rest.dtos.requests.ProductStatusUpdateRequestDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.PageResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.in.SellerPort;
import application.domain.valueobjects.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sellers")
public class SellerRestController {

    private final SellerPort sellerPort;
    private final ProductRestMapper productRestMapper;
    private final UserRestMapper userRestMapper;
    private final OrderRestMapper orderRestMapper;
    private final WarehouseRestMapper warehouseRestMapper;
    private final InventoryRestMapper inventoryRestMapper;

    @Autowired
    public SellerRestController(SellerPort sellerPort,
                                ProductRestMapper productRestMapper,
                                UserRestMapper userRestMapper,
                                OrderRestMapper orderRestMapper,
                                WarehouseRestMapper warehouseRestMapper,
                                InventoryRestMapper inventoryRestMapper) {
        this.sellerPort = sellerPort;
        this.productRestMapper = productRestMapper;
        this.userRestMapper = userRestMapper;
        this.orderRestMapper = orderRestMapper;
        this.warehouseRestMapper = warehouseRestMapper;
        this.inventoryRestMapper = inventoryRestMapper;
    }

    public SellerRestController(SellerPort sellerPort,
                                ProductRestMapper productRestMapper,
                                UserRestMapper userRestMapper,
                                OrderRestMapper orderRestMapper) {
        this(sellerPort, productRestMapper, userRestMapper, orderRestMapper,
                new WarehouseRestMapper(), new InventoryRestMapper());
    }

    @PostMapping("/register")
    public ResponseEntity<SellerResponseDTO> registerSeller(
            @RequestParam(name = "adminUserId", required = false) UUID adminUserId,
            @RequestBody CreateSellerRequestDTO request) {
        UUID effectiveAdmin = adminUserId != null ? adminUserId : UUID.randomUUID();
        Seller seller = sellerPort.registerSeller(
                effectiveAdmin,
                request.fullName(),
                request.email(),
                request.documentType(),
                request.documentNumber(),
                request.onboardingDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestMapper.toSellerResponse(seller));
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerResponseDTO> getSellerProfile(@PathVariable("sellerId") UUID sellerId) {
        Seller seller = sellerPort.getSellerProfile(sellerId);
        return ResponseEntity.ok(userRestMapper.toSellerResponse(seller));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO request) {
        Product product = sellerPort.createProduct(
                request.name(),
                request.variantAttribute(),
                request.variantValue(),
                request.price(),
                request.currency(),
                request.status(),
                request.type()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(productRestMapper.toResponse(product));
    }

    @GetMapping("/products")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "direction", defaultValue = "ASC") String direction) {
        PageResult<Product> productsPage;
        if ("name".equals(sortBy) && "ASC".equalsIgnoreCase(direction)) {
            productsPage = sellerPort.getAllProducts(page, size);
        } else {
            productsPage = sellerPort.getAllProducts(page, size, sortBy, direction);
        }
        return ResponseEntity.ok(PageResponseDTO.from(productsPage, ProductRestMapper::toResponse));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable("productId") UUID productId) {
        Product product = sellerPort.getProductById(productId);
        return ResponseEntity.ok(productRestMapper.toResponse(product));
    }

    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<ProductResponseDTO> updateProductStatus(@PathVariable("productId") UUID productId,
                                                                 @RequestBody ProductStatusUpdateRequestDTO request) {
        Product updated = sellerPort.updateProductStatus(productId, request.status());
        return ResponseEntity.ok(productRestMapper.toResponse(updated));
    }

    @PatchMapping("/products/{productId}/price")
    public ResponseEntity<ProductResponseDTO> updateProductPrice(@PathVariable("productId") UUID productId,
                                                                @RequestBody ProductPriceUpdateRequestDTO request) {
        Product updated = sellerPort.updateProductPrice(productId, request.price(), request.currency());
        return ResponseEntity.ok(productRestMapper.toResponse(updated));
    }

    @GetMapping("/{sellerId}/orders")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getSellerOrders(
            @PathVariable("sellerId") UUID sellerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "orderDate") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction) {
        PageResult<Order> ordersPage = sellerPort.getSellerOrders(sellerId, page, size, sortBy, direction);
        return ResponseEntity.ok(PageResponseDTO.from(ordersPage, OrderRestMapper::toResponse));
    }

    @GetMapping("/{sellerId}/warehouses")
    public ResponseEntity<PageResponseDTO<WarehouseResponseDTO>> getSellerWarehouses(
            @PathVariable("sellerId") UUID sellerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<Warehouse> warehouses = sellerPort.getSellerWarehouses(sellerId);
        return ResponseEntity.ok(PageResponseDTO.of(warehouseRestMapper.toResponseList(warehouses), page, size));
    }

    @PostMapping("/{sellerId}/inventory/initial")
    public ResponseEntity<InventoryResponseDTO> registerInitialStock(
            @PathVariable("sellerId") UUID sellerId,
            @RequestBody Map<String, Object> body) {
        UUID productId = UUID.fromString((String) body.get("productId"));
        UUID warehouseId = UUID.fromString((String) body.get("warehouseId"));
        int quantity = ((Number) body.get("quantity")).intValue();

        Inventory inventory = sellerPort.registerInitialStock(sellerId, productId, warehouseId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryRestMapper.toResponse(inventory));
    }

    @PatchMapping("/{sellerId}/inventory/{inventoryId}/adjust")
    public ResponseEntity<InventoryResponseDTO> adjustStock(
            @PathVariable("sellerId") UUID sellerId,
            @PathVariable("inventoryId") UUID inventoryId,
            @RequestBody Map<String, Object> body) {
        int quantity = ((Number) body.get("quantity")).intValue();
        String reason = (String) body.getOrDefault("reason", "Ajuste de inventario");

        Inventory updated = sellerPort.adjustStock(sellerId, inventoryId, quantity, reason);
        return ResponseEntity.ok(inventoryRestMapper.toResponse(updated));
    }

    @GetMapping("/{sellerId}/inventory")
    public ResponseEntity<PageResponseDTO<InventoryResponseDTO>> getSellerInventory(
            @PathVariable("sellerId") UUID sellerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<Inventory> inventories = sellerPort.getSellerInventory(sellerId);
        return ResponseEntity.ok(PageResponseDTO.of(inventoryRestMapper.toResponseList(inventories), page, size));
    }
}
