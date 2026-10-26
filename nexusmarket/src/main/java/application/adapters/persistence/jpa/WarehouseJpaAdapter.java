package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.enums.WarehouseType;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final SpringDataJpaWarehouseRepository jpaRepository;
    private final WarehouseJpaMapper mapper;

    public WarehouseJpaAdapter(SpringDataJpaWarehouseRepository jpaRepository, WarehouseJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseJpaEntity entity = mapper.toEntity(warehouse);
        WarehouseJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Warehouse> findById(UUID warehouseId) {
        return jpaRepository.findById(warehouseId).map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findBySellerId(UUID sellerId) {
        return jpaRepository.findBySellerId(sellerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Warehouse> findByType(WarehouseType type) {
        return jpaRepository.findByType(type).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Warehouse> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
