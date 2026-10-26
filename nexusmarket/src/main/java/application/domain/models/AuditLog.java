package application.domain.models;

import application.domain.enums.AuditSeverity;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an enterprise audit log entry.
 */
public class AuditLog {

    private UUID id;
    private String action;
    private UUID userId;
    private String userEmail;
    private String resource;
    private String resourceId;
    private AuditSeverity severity;
    private String details;
    private String ipAddress;
    private Instant timestamp;

    public AuditLog() {
    }

    public AuditLog(UUID id, String action, UUID userId, String userEmail, String resource,
                    String resourceId, AuditSeverity severity, String details, String ipAddress, Instant timestamp) {
        this.id = id != null ? id : UUID.randomUUID();
        this.action = action;
        this.userId = userId;
        this.userEmail = userEmail;
        this.resource = resource;
        this.resourceId = resourceId;
        this.severity = severity != null ? severity : AuditSeverity.LOW;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static AuditLog create(String action, UUID userId, String userEmail, String resource,
                                  String resourceId, AuditSeverity severity, String details, String ipAddress) {
        return new AuditLog(UUID.randomUUID(), action, userId, userEmail, resource, resourceId, severity, details, ipAddress, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public AuditSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AuditSeverity severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
