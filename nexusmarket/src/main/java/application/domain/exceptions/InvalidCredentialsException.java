package application.domain.exceptions;

/**
 * Thrown when user credentials provided for authentication are invalid.
 */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
