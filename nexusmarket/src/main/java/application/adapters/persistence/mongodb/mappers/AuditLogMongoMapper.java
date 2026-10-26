package application.adapters.persistence.mongodb.mappers;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;

import java.util.UUID;

public final class AuditLogMongoMapper {

    private AuditLogMongoMapper() {
    }

    public static AuditLogDocument toDocument(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        return new AuditLogDocument(
                domain.getId() != null ? domain.getId().toString() : UUID.randomUUID().toString(),
                domain.getAction(),
                domain.getUserId() != null ? domain.getUserId().toString() : null,
                domain.getUserEmail(),
                domain.getResource(),
                domain.getResourceId(),
                domain.getSeverity() != null ? domain.getSeverity().name() : AuditSeverity.LOW.name(),
                domain.getDetails(),
                domain.getIpAddress(),
                domain.getTimestamp()
        );
    }

    public static AuditLog toDomain(AuditLogDocument doc) {
        if (doc == null) {
            return null;
        }
        AuditSeverity severity;
        try {
            severity = doc.getSeverity() != null ? AuditSeverity.valueOf(doc.getSeverity()) : AuditSeverity.LOW;
        } catch (IllegalArgumentException e) {
            severity = AuditSeverity.LOW;
        }
        UUID id = doc.getId() != null ? UUID.fromString(doc.getId()) : null;
        UUID userId = doc.getUserId() != null ? UUID.fromString(doc.getUserId()) : null;

        return new AuditLog(
                id,
                doc.getAction(),
                userId,
                doc.getUserEmail(),
                doc.getResource(),
                doc.getResourceId(),
                severity,
                doc.getDetails(),
                doc.getIpAddress(),
                doc.getTimestamp()
        );
    }
}
