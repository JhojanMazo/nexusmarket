package application.domain.exceptions;

/**
 * Thrown when an operation cannot proceed due to the current operational status of a user (e.g., INACTIVE, SUSPENDED).
 */
public class InvalidUserStatusException extends BusinessRuleException {
    public InvalidUserStatusException(String message) {
        super(message);
    }
}
