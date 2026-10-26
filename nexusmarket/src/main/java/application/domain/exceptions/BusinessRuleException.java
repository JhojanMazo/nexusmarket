package application.domain.exceptions;

/**
 * Exception thrown when a business invariant or domain rule is violated.
 */
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
