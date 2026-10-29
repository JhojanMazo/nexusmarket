package application.adapters.security;

import application.domain.ports.out.JwtServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Enterprise JWT provider using standard HMAC-SHA256 cryptographic signatures.
 */
@Component
public class JwtProvider implements JwtServicePort {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_KEY_BYTES = 32;
    private final byte[] secretKeyBytes;
    private final long expirationSeconds;

    public JwtProvider(
            @Value("${security.jwt.secret:NexusMarketDevOnlyJwtSecretKey2026LocalDevelopment32Bytes}") String secret,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret key (SECURITY_JWT_SECRET / security.jwt.secret) must not be blank");
        }
        byte[] keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_KEY_BYTES) {
            throw new IllegalStateException("JWT secret key must be at least 32 bytes (256 bits) for HMAC-SHA256");
        }
        this.secretKeyBytes = keyBytes;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateToken(UUID userId, String email, String role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expirationSeconds;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
                userId.toString(), escapeJson(email), escapeJson(role), now, exp
        );

        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String unsignedToken = encodedHeader + "." + encodedPayload;

        String signature = sign(unsignedToken);
        return unsignedToken + "." + signature;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        try {
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            long exp = extractLongField(payloadJson, "exp");
            return Instant.now().getEpochSecond() <= exp;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        String payloadJson = extractPayload(token);
        String sub = extractStringField(payloadJson, "sub");
        return UUID.fromString(sub);
    }

    @Override
    public String extractEmail(String token) {
        String payloadJson = extractPayload(token);
        return extractStringField(payloadJson, "email");
    }

    @Override
    public String extractRole(String token) {
        String payloadJson = extractPayload(token);
        return extractStringField(payloadJson, "role");
    }

    private String extractPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }
        return new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC signature", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }

    private String extractStringField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start == -1) {
            throw new IllegalArgumentException("Field not found: " + field);
        }
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end == -1) {
            throw new IllegalArgumentException("Malformed JSON for field: " + field);
        }
        return json.substring(start, end);
    }

    private long extractLongField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start == -1) {
            throw new IllegalArgumentException("Field not found: " + field);
        }
        start += key.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }
}
