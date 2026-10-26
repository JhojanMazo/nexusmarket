package application.adapters.persistence.jpa;

import application.domain.models.Invoice;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInvoiceRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final SpringDataJpaInvoiceRepository jpaRepository;
    private final SpringDataJpaOrderRepository orderJpaRepository;
    private final SpringDataJpaProductRepository productJpaRepository;
    private final InvoiceJpaMapper mapper;
    private final OrderJpaMapper orderJpaMapper;

    public InvoiceJpaAdapter(SpringDataJpaInvoiceRepository jpaRepository,
                                    SpringDataJpaOrderRepository orderJpaRepository,
                                    SpringDataJpaProductRepository productJpaRepository,
                                    InvoiceJpaMapper mapper,
                                    OrderJpaMapper orderJpaMapper) {
        this.jpaRepository = jpaRepository;
        this.orderJpaRepository = orderJpaRepository;
        this.productJpaRepository = productJpaRepository;
        this.mapper = mapper;
        this.orderJpaMapper = orderJpaMapper;
    }

    @Override
    public Invoice save(Invoice invoice) {
        OrderJpaEntity orderEntity = orderJpaRepository.findById(invoice.getOrder().getOrderId())
                .orElseGet(() -> orderJpaRepository.save(orderJpaMapper.toEntity(invoice.getOrder(), productJpaRepository)));

        InvoiceJpaEntity entity = mapper.toEntity(invoice, orderEntity);
        InvoiceJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(UUID invoiceId) {
        return jpaRepository.findById(invoiceId).map(mapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrder_OrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
