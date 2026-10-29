package application.adapters.config;

import application.adapters.rest.dtos.responses.ApiErrorResponseDTO;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.security.JwtAuthenticationFilter;
import application.adapters.security.JwtProvider;
import application.adapters.security.SecurityConfig;
import application.domain.ports.out.JwtServicePort;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class OpenApiAndSecurityConfigTest {

    @Test
    void openApiConfig_definesHttpBearerJwtSecurityScheme() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.nexusMarketOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getComponents());
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME);
        assertNotNull(scheme, "bearerAuth security scheme must be registered");
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
        assertFalse(openAPI.getSecurity().isEmpty(), "Global security requirement must be configured");
        assertTrue(openAPI.getSecurity().get(0).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME));
    }

    @Test
    void securityConfig_allowsPublicAccessToSwaggerAndOpenApiRoutes() throws Exception {
        assertTrue(SecurityConfig.isPublicPath("/v3/api-docs"));
        assertTrue(SecurityConfig.isPublicPath("/v3/api-docs/swagger-config"));
        assertTrue(SecurityConfig.isPublicPath("/swagger-ui/index.html"));
        assertTrue(SecurityConfig.isPublicPath("/swagger-ui/swagger-initializer.js"));
        assertTrue(SecurityConfig.isPublicPath("/swagger-ui.html"));
        assertFalse(SecurityConfig.isPublicPath("/api/buyers/orders"));

        JwtServicePort jwtServicePort = mock(JwtServicePort.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtServicePort);

        for (String publicRoute : new String[]{"/v3/api-docs", "/v3/api-docs/swagger-config", "/swagger-ui/index.html", "/swagger-ui.html"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", publicRoute);
            request.setServletPath(publicRoute);
            request.addHeader("Authorization", "Bearer any-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);
            verifyNoInteractions(jwtServicePort);
        }
    }

    @Test
    void globalExceptionHandler_mapsOptimisticLockingFailureToHttp409Conflict() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/sellers/products/123/price");
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Row was updated or deleted by another transaction");

        ResponseEntity<ApiErrorResponseDTO> response = handler.handleOptimisticLockingFailure(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("/api/sellers/products/123/price", response.getBody().path());
        assertTrue(response.getBody().message().toLowerCase().contains("concurrentemente"));
    }

    @Test
    void productionSecurityValidator_rejectsMissingWeakOrPlaceholderSecretsAndDestructiveDdl() {
        String strongDbPass = "Pr0d$SqlS3rv3r#987654321!";
        String strongJwtSecret = "Pr0d$JwtHmacSha256$SecretKey$WithMoreThan32BytesLength2026!";
        String strongMongoUri = "mongodb://nexus_admin:Pr0d$M0ng0#987654!@nexusmarket-mongo:27017/nexusmarket_audit?authSource=admin";

        // 1. Blank DB password fails
        ProductionSecurityValidator blankDbValidator = new ProductionSecurityValidator(
                "", strongJwtSecret, strongMongoUri, "none");
        assertThrows(IllegalStateException.class, blankDbValidator::validate);

        // 2. Placeholder DB password fails
        ProductionSecurityValidator placeholderDbValidator = new ProductionSecurityValidator(
                "ChangeMe_StrongSqlPass2026!", strongJwtSecret, strongMongoUri, "none");
        assertThrows(IllegalStateException.class, placeholderDbValidator::validate);

        // 3. Default dev JWT secret fails in prod
        ProductionSecurityValidator devJwtValidator = new ProductionSecurityValidator(
                strongDbPass, "NexusMarketDevOnlyJwtSecretKey2026LocalDevelopment32Bytes", strongMongoUri, "none");
        assertThrows(IllegalStateException.class, devJwtValidator::validate);

        // 4. Short JWT secret (< 32 bytes) fails
        ProductionSecurityValidator shortJwtValidator = new ProductionSecurityValidator(
                strongDbPass, "TooShortSecret123!", strongMongoUri, "none");
        assertThrows(IllegalStateException.class, shortJwtValidator::validate);

        // 5. Placeholder MongoDB URI fails
        ProductionSecurityValidator placeholderMongoValidator = new ProductionSecurityValidator(
                strongDbPass, strongJwtSecret,
                "mongodb://nexus_admin:<REPLACE_WITH_STRONG_MONGODB_PASSWORD>@nexusmarket-mongo:27017/nexusmarket_audit",
                "none");
        assertThrows(IllegalStateException.class, placeholderMongoValidator::validate);

        // 6. Destructive ddl-auto=create or create-drop fails in prod
        ProductionSecurityValidator destructiveDdlValidator = new ProductionSecurityValidator(
                strongDbPass, strongJwtSecret, strongMongoUri, "create-drop");
        assertThrows(IllegalStateException.class, destructiveDdlValidator::validate);

        // 7. Strong production configuration with ddl-auto=none succeeds
        ProductionSecurityValidator validProdValidator = new ProductionSecurityValidator(
                strongDbPass, strongJwtSecret, strongMongoUri, "none");
        assertDoesNotThrow(validProdValidator::validate);
    }

    @Test
    void jwtProvider_enforcesMinimum256BitSecretLength() {
        assertThrows(IllegalStateException.class, () -> new JwtProvider("", 3600));
        assertThrows(IllegalStateException.class, () -> new JwtProvider("short-secret-key", 3600));
        assertDoesNotThrow(() -> new JwtProvider("ValidCryptographicSecretKeyWithAtLeast32Bytes!", 3600));
    }
}
