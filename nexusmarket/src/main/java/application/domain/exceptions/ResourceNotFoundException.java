package application.domain.exceptions;

/**
 * Exception thrown when a requested domain entity cannot be found.
 */
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s no encontrado con el identificador: %s", resourceName, id));
    }
}
