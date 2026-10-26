package application.domain.exceptions;

/**
 * Thrown when an inventory allocation or cart reservation exceeds available stock.
 */
public class InsufficientStockException extends BusinessRuleException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
