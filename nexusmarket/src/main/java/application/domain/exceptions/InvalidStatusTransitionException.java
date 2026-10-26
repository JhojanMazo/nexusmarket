package application.domain.exceptions;

/**
 * Thrown when an illegal state or status transition is attempted on a domain entity.
 */
public class InvalidStatusTransitionException extends BusinessRuleException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
