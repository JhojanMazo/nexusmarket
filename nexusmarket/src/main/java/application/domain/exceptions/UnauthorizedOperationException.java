package application.domain.exceptions;

/**
 * Thrown when an actor attempts an action for which they lack authorization or role permissions.
 */
public class UnauthorizedOperationException extends DomainException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
