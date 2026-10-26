package application.domain.ports.out;

/**
 * Outbound port for secure password hashing and verification.
 */
public interface PasswordServicePort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
