package application.adapters.rest;

import application.adapters.rest.controllers.SellerRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.ports.in.SellerPort;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SellerRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SellerPort sellerPort;

    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        SellerRestController controller = new SellerRestController(
                sellerPort,
                new ProductRestMapper(),
                new UserRestMapper(),
                new OrderRestMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createProduct_Success() throws Exception {
        Product product = new Product(productId, "Keyboard", new ProductVariant("Layout", "US"),
                new Money(BigDecimal.valueOf(80), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(sellerPort.createProduct(eq("Keyboard"), eq("Layout"), eq("US"), any(BigDecimal.class), eq("USD"), eq(ProductStatus.PUBLISHED), eq(ProductType.PHYSICAL)))
                .thenReturn(product);

        String json = """
                {
                    "name": "Keyboard",
                    "variantAttribute": "Layout",
                    "variantValue": "US",
                    "price": 80,
                    "currency": "USD",
                    "status": "PUBLISHED",
                    "type": "PHYSICAL"
                }
                """;

        mockMvc.perform(post("/api/sellers/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(80));
    }

    @Test
    void getProductById_Success() throws Exception {
        Product product = new Product(productId, "Keyboard", new ProductVariant("Layout", "US"),
                new Money(BigDecimal.valueOf(80), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(sellerPort.getProductById(productId)).thenReturn(product);

        mockMvc.perform(get("/api/sellers/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void updateProductPrice_Success() throws Exception {
        Product updated = new Product(productId, "Keyboard", new ProductVariant("Layout", "US"),
                new Money(BigDecimal.valueOf(95), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(sellerPort.updateProductPrice(eq(productId), any(BigDecimal.class), eq("USD")))
                .thenReturn(updated);

        String json = """
                {
                    "price": 95,
                    "currency": "USD"
                }
                """;

        mockMvc.perform(patch("/api/sellers/products/{productId}/price", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(95));
    }
}
