package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CreateBuyerRequestDTO;
import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.adapters.rest.dtos.responses.PageResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.ports.in.PublicAccessPort;
import application.domain.valueobjects.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;
    private final UserRestMapper userRestMapper;
    private final ProductRestMapper productRestMapper;

    @Autowired
    public AuthRestController(PublicAccessPort publicAccessPort,
                              UserRestMapper userRestMapper,
                              ProductRestMapper productRestMapper) {
        this.publicAccessPort = publicAccessPort;
        this.userRestMapper = userRestMapper;
        this.productRestMapper = productRestMapper;
    }

    public AuthRestController(PublicAccessPort publicAccessPort, UserRestMapper userRestMapper) {
        this(publicAccessPort, userRestMapper, new ProductRestMapper());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        AuthenticationResult result = publicAccessPort.authenticate(request.email(), request.password());
        LoginResponseDTO response = LoginResponseDTO.bearer(
                result.token(),
                result.userId(),
                result.email(),
                result.role()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : "";
        publicAccessPort.logout(token);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
    }

    @PostMapping("/register")
    public ResponseEntity<BuyerResponseDTO> register(@RequestBody CreateBuyerRequestDTO request) {
        Buyer buyer;
        if (request.password() != null && !request.password().isBlank()) {
            buyer = publicAccessPort.registerBuyer(
                    request.fullName(),
                    request.email(),
                    request.documentType(),
                    request.documentNumber(),
                    userRestMapper.toAddress(request.primaryAddress()),
                    request.password()
            );
        } else {
            buyer = publicAccessPort.registerBuyer(
                    request.fullName(),
                    request.email(),
                    request.documentType(),
                    request.documentNumber(),
                    userRestMapper.toAddress(request.primaryAddress())
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestMapper.toBuyerResponse(buyer));
    }

    @GetMapping("/catalog")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getPublishedCatalog(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice) {
        boolean hasFilters = (search != null && !search.isBlank())
                || (type != null && !type.isBlank())
                || minPrice != null
                || maxPrice != null;

        PageResult<Product> catalogPage;
        if (hasFilters) {
            catalogPage = publicAccessPort.searchPublishedCatalog(
                    search,
                    type,
                    minPrice,
                    maxPrice,
                    page,
                    size,
                    sortBy != null ? sortBy : "name",
                    direction != null ? direction : "ASC"
            );
        } else if (sortBy != null || direction != null) {
            catalogPage = publicAccessPort.getPublishedCatalog(
                    page,
                    size,
                    sortBy != null ? sortBy : "name",
                    direction != null ? direction : "ASC"
            );
        } else {
            catalogPage = publicAccessPort.getPublishedCatalog(page, size);
        }
        return ResponseEntity.ok(PageResponseDTO.from(catalogPage, ProductRestMapper::toResponse));
    }

    @GetMapping("/catalog/{productId}")
    public ResponseEntity<ProductResponseDTO> getPublishedProduct(@PathVariable("productId") UUID productId) {
        Product product = publicAccessPort.getPublishedProductById(productId);
        return ResponseEntity.ok(productRestMapper.toResponse(product));
    }

    @GetMapping("/catalog/{productId}/stock")
    public ResponseEntity<Map<String, Object>> getPublishedProductStock(@PathVariable("productId") UUID productId) {
        Product product = publicAccessPort.getPublishedProductById(productId);
        int available = publicAccessPort.getAvailableStockForProduct(productId);
        return ResponseEntity.ok(Map.of(
                "productId", product.getProductId(),
                "productType", product.getType().name(),
                "availableQuantity", available,
                "inStock", available > 0
        ));
    }
}
