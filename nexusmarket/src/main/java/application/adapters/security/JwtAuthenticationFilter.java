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
import java.util.UUID;

/**
 * Intercepts HTTP requests, validates the Authorization Bearer token, and populates the SecurityUserContext.
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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (jwtServicePort.validateToken(token)) {
                try {
                    UUID userId = jwtServicePort.extractUserId(token);
                    String email = jwtServicePort.extractEmail(token);
                    String role = jwtServicePort.extractRole(token);
                    SecurityUserContext.set(new AuthenticatedUserPrincipal(userId, email, role));
                } catch (Exception ignored) {
                    // Invalid token structure
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityUserContext.clear();
        }
    }
}
