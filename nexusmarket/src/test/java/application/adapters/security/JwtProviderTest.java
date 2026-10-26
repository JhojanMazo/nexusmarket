package application.adapters.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider("TestSecretKeyForEnterpriseJwtOperationsOnlyNexusMarket2026", 3600);
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        UUID userId = UUID.randomUUID();
        String email = "shopper@nexusmarket.com";
        String role = "BUYER";

        String token = jwtProvider.generateToken(userId, email, role);
        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));

        assertEquals(userId, jwtProvider.extractUserId(token));
        assertEquals(email, jwtProvider.extractEmail(token));
        assertEquals(role, jwtProvider.extractRole(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtProvider.generateToken(userId, "admin@nexusmarket.com", "ADMINISTRATOR");

        String tampered = token.substring(0, token.length() - 5) + "abcde";
        assertFalse(jwtProvider.validateToken(tampered));
    }

    @Test
    void shouldRejectNullOrBlankToken() {
        assertFalse(jwtProvider.validateToken(null));
        assertFalse(jwtProvider.validateToken(""));
        assertFalse(jwtProvider.validateToken("not.a.valid.jwt"));
    }
}
