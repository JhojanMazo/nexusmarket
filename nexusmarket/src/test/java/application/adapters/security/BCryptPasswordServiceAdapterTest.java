package application.adapters.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordServiceAdapterTest {

    private BCryptPasswordServiceAdapter passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new BCryptPasswordServiceAdapter();
    }

    @Test
    void shouldEncodeAndMatchPassword() {
        String raw = "StrongPassword@2026";
        String encoded = passwordService.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(passwordService.matches(raw, encoded));
        assertFalse(passwordService.matches("WrongPassword", encoded));
    }

    @Test
    void shouldHandleNullGracefully() {
        assertFalse(passwordService.matches(null, "hash"));
        assertFalse(passwordService.matches("password", null));
        assertThrows(IllegalArgumentException.class, () -> passwordService.encode(null));
    }
}
