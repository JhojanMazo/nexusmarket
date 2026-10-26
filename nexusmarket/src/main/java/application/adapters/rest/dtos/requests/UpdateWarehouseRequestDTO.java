package application.adapters.rest.dtos.requests;

public record UpdateWarehouseRequestDTO(
        String name,
        double capacity
) {
}
