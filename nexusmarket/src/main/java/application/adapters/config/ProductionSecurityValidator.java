package application.adapters.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Validador fail-fast de seguridad para el perfil de produccion (prod).
 * Impide que el microservicio inicie con secretos vacios, debiles, por defecto
 * o con configuraciones destructivas de esquema JPA.
 */
@Component
@Profile("prod")
public class ProductionSecurityValidator {

    private static final int MIN_JWT_SECRET_BYTES = 32;

    private static final Set<String> FORBIDDEN_SECRETS = Set.of(
            "ChangeMe_StrongSqlPass2026!",
            "ChangeMe_StrongMongoPass2026!",
            "ChangeMe_ReplaceWith256BitCryptographicSecretKeyForProduction",
            "NexusMarketSuperSecretKeyForJwtSigning2026EnterpriseSecurity",
            "NexusMarketDevOnlyJwtSecretKey2026LocalDevelopment32Bytes",
            "NexusMarketTestOnlyJwtSecretKey2026AutomatedSuite32Bytes!",
            "password",
            "123456",
            "admin",
            "sa"
    );

    private final String datasourcePassword;
    private final String jwtSecret;
    private final String mongoUri;
    private final String ddlAuto;

    public ProductionSecurityValidator(
            @Value("${spring.datasource.password:}") String datasourcePassword,
            @Value("${security.jwt.secret:}") String jwtSecret,
            @Value("${spring.data.mongodb.uri:}") String mongoUri,
            @Value("${spring.jpa.hibernate.ddl-auto:none}") String ddlAuto) {
        this.datasourcePassword = datasourcePassword;
        this.jwtSecret = jwtSecret;
        this.mongoUri = mongoUri;
        this.ddlAuto = ddlAuto;
    }

    @PostConstruct
    public void validate() {
        validateDatasourcePassword();
        validateJwtSecret();
        validateMongoUri();
        validateDdlAuto();
    }

    private void validateDatasourcePassword() {
        if (isBlankOrPlaceholder(datasourcePassword)) {
            throw new IllegalStateException(
                    "Seguridad PROD: La contrasena de base de datos relacional (SPRING_DATASOURCE_PASSWORD / DB_PASSWORD) "
                            + "es obligatoria en produccion y no puede usar valores por defecto ni plantillas.");
        }
    }

    private void validateJwtSecret() {
        if (isBlankOrPlaceholder(jwtSecret)) {
            throw new IllegalStateException(
                    "Seguridad PROD: El secreto JWT (SECURITY_JWT_SECRET) es obligatorio en produccion "
                            + "y no puede usar claves de desarrollo ni valores de ejemplo.");
        }
        int byteLength = jwtSecret.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength < MIN_JWT_SECRET_BYTES) {
            throw new IllegalStateException(
                    "Seguridad PROD: SECURITY_JWT_SECRET debe tener al menos " + MIN_JWT_SECRET_BYTES
                            + " bytes (256 bits) para firmas HMAC-SHA256 seguras.");
        }
    }

    private void validateMongoUri() {
        if (isBlankOrPlaceholder(mongoUri) || !mongoUri.startsWith("mongodb")) {
            throw new IllegalStateException(
                    "Seguridad PROD: SPRING_DATA_MONGODB_URI es obligatorio en produccion "
                            + "y no puede contener credenciales por defecto ni plantillas.");
        }
    }

    private void validateDdlAuto() {
        if (ddlAuto != null) {
            String normalized = ddlAuto.trim().toLowerCase();
            if ("create".equals(normalized) || "create-drop".equals(normalized)) {
                throw new IllegalStateException(
                        "Seguridad PROD: spring.jpa.hibernate.ddl-auto='" + ddlAuto
                                + "' esta prohibido en produccion para evitar perdida de datos.");
            }
        }
    }

    private boolean isBlankOrPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String trimmed = value.trim();
        if (FORBIDDEN_SECRETS.contains(trimmed)) {
            return true;
        }
        String lower = trimmed.toLowerCase();
        return lower.contains("changeme")
                || lower.contains("<replace_with_")
                || lower.contains("devonly")
                || lower.contains("testonly");
    }
}
