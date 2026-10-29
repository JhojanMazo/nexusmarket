package application.adapters.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger UI configuration with HTTP Bearer (JWT) security scheme
 * so protected endpoints can be authorized and tested interactively.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI nexusMarketOpenAPI() {
        SecurityScheme bearerSecurityScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingrese el token JWT Bearer obtenido en /api/auth/login (sin el prefijo 'Bearer ')");

        return new OpenAPI()
                .info(new Info()
                        .title("NexusMarket Enterprise API")
                        .version("1.0.0")
                        .description("API REST empresarial de NexusMarket bajo Arquitectura Hexagonal y DDD")
                        .contact(new Contact().name("NexusMarket Architecture Team")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerSecurityScheme));
    }
}
