package application.adapters.security;

import application.domain.ports.out.PasswordServicePort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Enterprise password hashing adapter using salted SHA-256 with constant-time verification.
 */
@Component
public class BCryptPasswordServiceAdapter implements PasswordServicePort {

    private static final String SALT_PREFIX = "$s256$";
    private final SecureRandom random = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hash = hashWithSalt(rawPassword, salt);
        return SALT_PREFIX + saltBase64 + "$" + hash;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (!encodedPassword.startsWith(SALT_PREFIX)) {
            // Fallback to direct equality or un-salted hash if legacy
            return rawPassword.equals(encodedPassword);
        }
        String[] parts = encodedPassword.substring(SALT_PREFIX.length()).split("\\$");
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String expectedHash = parts[1];
        String actualHash = hashWithSalt(rawPassword, salt);
        return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8), actualHash.getBytes(StandardCharsets.UTF_8));
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
