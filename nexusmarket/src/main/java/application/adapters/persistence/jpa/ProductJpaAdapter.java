package application.adapters.persistence.jpa;

import application.domain.enums.ProductStatus;
import application.domain.models.Product;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.valueobjects.PageResult;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final SpringDataJpaProductRepository jpaRepository;
    private final ProductJpaMapper mapper;

    public ProductJpaAdapter(SpringDataJpaProductRepository jpaRepository, ProductJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        ProductJpaEntity entity = jpaRepository.findById(product.getProductId())
                .map(existing -> mapper.updateEntity(existing, product))
                .orElseGet(() -> mapper.toEntity(product));
        ProductJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(UUID productId) {
        return jpaRepository.findById(productId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> findAll(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<ProductJpaEntity> entityPage = jpaRepository.findAll(pageable);
        return toPageResult(entityPage, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> findByStatus(ProductStatus status, int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<ProductJpaEntity> entityPage = jpaRepository.findByStatus(status, pageable);
        return toPageResult(entityPage, page, size);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        String resolvedProperty = resolveSortProperty(sortBy);
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        return PageRequest.of(safePage, safeSize, Sort.by(dir, resolvedProperty));
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "name";
        }
        return switch (sortBy.trim()) {
            case "price", "priceAmount" -> "priceAmount";
            case "id", "productId" -> "productId";
            case "status" -> "status";
            case "type", "productType" -> "type";
            case "createdAt" -> "createdAt";
            default -> "name";
        };
    }

    private PageResult<Product> toPageResult(Page<ProductJpaEntity> entityPage, int fallbackPage, int fallbackSize) {
        if (entityPage == null) {
            return PageResult.of(List.of(), fallbackPage, fallbackSize);
        }
        List<Product> content = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new PageResult<>(
                content,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isLast()
        );
    }
}
