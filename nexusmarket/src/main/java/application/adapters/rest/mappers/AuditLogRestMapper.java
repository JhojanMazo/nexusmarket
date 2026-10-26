package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.AuditLogPageResponseDTO;
import application.adapters.rest.dtos.responses.AuditLogResponseDTO;
import application.domain.models.AuditLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditLogRestMapper {

    public AuditLogResponseDTO toResponse(AuditLog model) {
        if (model == null) {
            return null;
        }
        return new AuditLogResponseDTO(
                model.getId(),
                model.getAction(),
                model.getUserId(),
                model.getUserEmail(),
                model.getResource(),
                model.getResourceId(),
                model.getSeverity(),
                model.getDetails(),
                model.getIpAddress(),
                model.getTimestamp()
        );
    }

    public List<AuditLogResponseDTO> toResponseList(List<AuditLog> models) {
        if (models == null) {
            return List.of();
        }
        return models.stream().map(this::toResponse).toList();
    }

    public AuditLogPageResponseDTO toPageResponse(List<AuditLog> models, int page, int size, long total) {
        List<AuditLogResponseDTO> dtos = toResponseList(models);
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;
        boolean isLast = (page + 1) >= totalPages;
        return new AuditLogPageResponseDTO(dtos, page, size, total, totalPages, isLast);
    }
}
