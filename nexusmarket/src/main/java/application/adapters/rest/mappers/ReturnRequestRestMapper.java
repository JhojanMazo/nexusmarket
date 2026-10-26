package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.domain.models.ReturnRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ReturnRequestRestMapper {

    public ReturnRequestResponseDTO toResponse(ReturnRequest request) {
        if (request == null) return null;
        return new ReturnRequestResponseDTO(
                request.getRequestId(),
                request.getOrder() != null ? request.getOrder().getOrderId() : null,
                request.getStatus()
        );
    }

    public List<ReturnRequestResponseDTO> toResponseList(List<ReturnRequest> requests) {
        if (requests == null) return Collections.emptyList();
        return requests.stream().map(this::toResponse).toList();
    }
}
