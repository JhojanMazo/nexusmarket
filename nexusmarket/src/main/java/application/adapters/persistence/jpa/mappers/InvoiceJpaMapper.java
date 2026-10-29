package application.adapters.persistence.jpa.mappers;

import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.valueobjects.Money;
import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class InvoiceJpaMapper {

    private final OrderJpaMapper orderJpaMapper;

    public InvoiceJpaMapper(OrderJpaMapper orderJpaMapper) {
        this.orderJpaMapper = orderJpaMapper;
    }

    public Invoice toDomain(InvoiceJpaEntity entity) {
        if (entity == null) return null;

        Order order = orderJpaMapper.toDomain(entity.getOrder());
        Currency currency = Currency.getInstance(entity.getCurrencyCode());
        Money subtotal = new Money(entity.getSubtotalAmount(), currency);
        Money tax = new Money(entity.getTaxAmount(), currency);

        java.time.LocalDate issueDate = entity.getIssueDate() != null
                ? entity.getIssueDate()
                : (entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : java.time.LocalDate.now());

        return new Invoice(
                entity.getInvoiceId(),
                order,
                issueDate,
                subtotal,
                tax
        );
    }

    public InvoiceJpaEntity toEntity(Invoice domain, OrderJpaEntity orderEntity) {
        if (domain == null) return null;

        return new InvoiceJpaEntity(
                domain.getInvoiceId(),
                orderEntity,
                domain.getIssueDate(),
                domain.getSubtotal().amount(),
                domain.getTax().amount(),
                domain.getTotal().amount(),
                domain.getTotal().currency().getCurrencyCode()
        );
    }
}
