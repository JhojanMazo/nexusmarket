package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CreateBuyerRequestDTO;
import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.ports.in.PublicAccessPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
        Buyer buyer = publicAccessPort.registerBuyer(
                request.fullName(),
                request.email(),
                request.documentType(),
                request.documentNumber(),
                userRestMapper.toAddress(request.primaryAddress())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestMapper.toBuyerResponse(buyer));
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<ProductResponseDTO>> getPublishedCatalog() {
        List<Product> products = publicAccessPort.getPublishedCatalog();
        return ResponseEntity.ok(productRestMapper.toResponseList(products));
    }

    @GetMapping("/catalog/{productId}")
    public ResponseEntity<ProductResponseDTO> getPublishedProduct(@PathVariable("productId") UUID productId) {
        Product product = publicAccessPort.getPublishedProductById(productId);
        return ResponseEntity.ok(productRestMapper.toResponse(product));
    }
}
