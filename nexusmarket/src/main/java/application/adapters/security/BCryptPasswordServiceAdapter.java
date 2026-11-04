package application.adapters.security;

import application.domain.ports.out.PasswordServicePort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Enterprise password hashing adapter using standard BCrypt (cost factor 10).
 * Rejects plaintext comparisons and supports constant-time verification.
 */
@Component
public class BCryptPasswordServiceAdapter implements PasswordServicePort {

    private static final String LEGACY_SALT_PREFIX = "$s256$";
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(10);

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Raw password cannot be null or blank");
        }
        return bcryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return bcryptEncoder.matches(rawPassword, encodedPassword);
        }
        if (encodedPassword.startsWith(LEGACY_SALT_PREFIX)) {
            String[] parts = encodedPassword.substring(LEGACY_SALT_PREFIX.length()).split("\\$");
            if (parts.length != 2) {
                return false;
            }
            try {
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                String expectedHash = parts[1];
                String actualHash = hashWithSalt(rawPassword, salt);
                return MessageDigest.isEqual(
                        expectedHash.getBytes(StandardCharsets.UTF_8),
                        actualHash.getBytes(StandardCharsets.UTF_8)
                );
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    private String hashWithSalt(String rawPassword, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
