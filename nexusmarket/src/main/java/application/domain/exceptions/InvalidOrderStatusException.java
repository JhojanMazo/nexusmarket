package application.domain.exceptions;

/**
 * Thrown when an order operation violates order status lifecycle rules.
 */
public class InvalidOrderStatusException extends BusinessRuleException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
