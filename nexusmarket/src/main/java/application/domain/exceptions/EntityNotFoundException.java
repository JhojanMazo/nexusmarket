package application.domain.exceptions;

/**
 * Thrown when a required domain aggregate or entity is not found by its identifier.
 */
public class EntityNotFoundException extends ResourceNotFoundException {
    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " no encontrado con el identificador: " + id);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
