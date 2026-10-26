package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.CreateWarehouseRequestDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class WarehouseRestMapper {

    public Warehouse toDomain(CreateWarehouseRequestDTO dto) {
        if (dto == null) return null;
        Address address = dto.address() != null ? OrderRestMapper.toAddress(dto.address()) : null;
        return new Warehouse(
                UUID.randomUUID(),
                dto.name(),
                address,
                dto.type(),
                dto.sellerId(),
                dto.capacity()
        );
    }

    public WarehouseResponseDTO toResponse(Warehouse warehouse) {
        if (warehouse == null) return null;
        return new WarehouseResponseDTO(
                warehouse.getWarehouseId(),
                warehouse.getName(),
                OrderRestMapper.toAddressDto(warehouse.getAddress()),
                warehouse.getType(),
                warehouse.getSellerId(),
                warehouse.getCapacity()
        );
    }

    public List<WarehouseResponseDTO> toResponseList(List<Warehouse> warehouses) {
        if (warehouses == null) return Collections.emptyList();
        return warehouses.stream().map(this::toResponse).toList();
    }
}
