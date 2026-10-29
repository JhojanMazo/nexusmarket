package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Web security configuration providing CORS filtering, registering the JWT authentication filter,
 * enforcing role-based access control (RBAC) and resource ownership, and allowing public access
 * to authentication, catalog, and OpenAPI 3 documentation endpoints.
 */
@Configuration
public class SecurityConfig {

    public static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/auth/**",
            "/api/buyers/register",
            "/api/buyers/catalog",
            "/actuator/health",
            "/actuator/info"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${security.cors.allowed-origin-patterns:http://localhost:8080,http://localhost:3000,http://127.0.0.1:8080,http://127.0.0.1:3000}")
    private String allowedOriginPatternsProperty = "http://localhost:8080,http://localhost:3000,http://127.0.0.1:8080,http://127.0.0.1:3000";

    /**
     * Returns {@code true} when the given request path belongs to a public (unauthenticated) route.
     */
    public static boolean isPublicPath(String path) {
        if (path == null) {
            return false;
        }
        if (!path.startsWith("/api/") && !path.startsWith("/v3/api-docs") && !path.startsWith("/swagger-ui")) {
            return true;
        }
        return PUBLIC_PATHS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    /**
     * Evaluates role-based access control (RBAC) and actor ownership for protected {@code /api/**} routes.
     */
    public static boolean isAuthorizedForPath(String path, String method, AuthenticatedUserPrincipal principal) {
        if (path == null || principal == null || principal.role() == null) {
            return false;
        }
        String role = principal.role().trim().toUpperCase();

        if ("ADMINISTRATOR".equals(role)) {
            return true;
        }

        if (PATH_MATCHER.match("/api/admin/**", path)) {
            return false;
        }

        if (PATH_MATCHER.match("/api/supervisors/**", path)) {
            return "SUPERVISOR".equals(role);
        }

        if (PATH_MATCHER.match("/api/logistics/**", path)) {
            return "LOGISTICS_OPERATOR".equals(role);
        }

        if (PATH_MATCHER.match("/api/sellers/**", path)) {
            if (!"SELLER".equals(role)) {
                return false;
            }
            UUID pathSellerId = extractActorUuidSegment(path, "/api/sellers/");
            return pathSellerId == null || pathSellerId.equals(principal.userId());
        }

        if (PATH_MATCHER.match("/api/buyers/**", path)) {
            if (!"BUYER".equals(role)) {
                return false;
            }
            UUID pathBuyerId = extractActorUuidSegment(path, "/api/buyers/");
            return pathBuyerId == null || pathBuyerId.equals(principal.userId());
        }

        return true;
    }

    private static UUID extractActorUuidSegment(String path, String prefix) {
        if (!path.startsWith(prefix)) {
            return null;
        }
        String remainder = path.substring(prefix.length());
        int slashIdx = remainder.indexOf('/');
        String candidate = slashIdx >= 0 ? remainder.substring(0, slashIdx) : remainder;
        if (candidate.isBlank()
                || "products".equalsIgnoreCase(candidate)
                || "carts".equalsIgnoreCase(candidate)
                || "orders".equalsIgnoreCase(candidate)
                || "register".equalsIgnoreCase(candidate)
                || "catalog".equalsIgnoreCase(candidate)) {
            return null;
        }
        try {
            return UUID.fromString(candidate);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> origins = Arrays.stream(allowedOriginPatternsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOriginPatterns(origins.isEmpty() ? List.of("http://localhost:8080") : origins);
        config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
