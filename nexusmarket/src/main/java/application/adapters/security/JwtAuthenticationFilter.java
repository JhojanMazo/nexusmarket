package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.ports.out.JwtServicePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Intercepts HTTP requests on protected API routes, validates the Authorization Bearer token,
 * enforces role-based access control (RBAC) and actor ownership, and populates SecurityUserContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtServicePort jwtServicePort;

    public JwtAuthenticationFilter(JwtServicePort jwtServicePort) {
        this.jwtServicePort = jwtServicePort;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = resolvePath(request);
        return SecurityConfig.isPublicPath(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = resolvePath(request);
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                    "Se requiere autenticación mediante token Bearer JWT para acceder a este recurso", path);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty() || !jwtServicePort.validateToken(token)) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                    "El token JWT proporcionado es inválido o ha expirado", path);
            return;
        }

        AuthenticatedUserPrincipal principal;
        try {
            UUID userId = jwtServicePort.extractUserId(token);
            String email = jwtServicePort.extractEmail(token);
            String role = jwtServicePort.extractRole(token);
            principal = new AuthenticatedUserPrincipal(userId, email, role);
        } catch (Exception ex) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                    "Estructura de token JWT inválida", path);
            return;
        }

        if (!SecurityConfig.isAuthorizedForPath(path, request.getMethod(), principal)) {
            writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                    "No tiene permisos suficientes para ejecutar esta operación o acceder a este recurso", path);
            return;
        }

        SecurityUserContext.set(principal);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityUserContext.clear();
        }
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        return path != null ? path : "";
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    int status,
                                    String error,
                                    String message,
                                    String path) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                Instant.now().toString(),
                status,
                escapeJson(error),
                escapeJson(message),
                escapeJson(path)
        );
        response.getWriter().write(json);
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
