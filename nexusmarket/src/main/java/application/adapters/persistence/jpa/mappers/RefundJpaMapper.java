package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.RefundJpaEntity;
import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.valueobjects.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class RefundJpaMapper {

    private final ReturnRequestJpaMapper returnRequestJpaMapper;

    public RefundJpaMapper(ReturnRequestJpaMapper returnRequestJpaMapper) {
        this.returnRequestJpaMapper = returnRequestJpaMapper;
    }

    public Refund toDomain(RefundJpaEntity entity) {
        if (entity == null) return null;
        ReturnRequest returnRequest = returnRequestJpaMapper.toDomain(entity.getReturnRequest());
        Money amount = new Money(entity.getAmount(), Currency.getInstance(entity.getCurrencyCode()));
        return Refund.reconstitute(entity.getRefundId(), returnRequest, amount, entity.getStatus());
    }

    public RefundJpaEntity toEntity(Refund domain,
                                    SpringDataJpaReturnRequestRepository returnRequestRepository,
                                    SpringDataJpaOrderRepository orderRepository,
                                    SpringDataJpaProductRepository productRepository) {
        if (domain == null) return null;
        ReturnRequestJpaEntity returnRequestEntity = returnRequestRepository != null && domain.getReturnRequest() != null
                ? returnRequestRepository.findById(domain.getReturnRequest().getRequestId())
                    .orElseGet(() -> returnRequestJpaMapper.toEntity(domain.getReturnRequest(), orderRepository, productRepository))
                : (domain.getReturnRequest() != null ? returnRequestJpaMapper.toEntity(domain.getReturnRequest(), orderRepository, productRepository) : null);

        return new RefundJpaEntity(
                domain.getRefundId(),
                returnRequestEntity,
                domain.getAmount().amount(),
                domain.getAmount().currency().getCurrencyCode(),
                domain.getStatus()
        );
    }
}
