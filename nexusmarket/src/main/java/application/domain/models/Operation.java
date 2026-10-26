package application.domain.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a business or administrative operation execution.
 */
public class Operation {

    private UUID id;
    private String operationType;
    private String description;
    private UUID operatorId;
    private String status;
    private String payload;
    private Instant createdAt;

    public Operation() {
    }

    public Operation(UUID id, String operationType, String description, UUID operatorId, String status, String payload, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.operationType = operationType;
        this.description = description;
        this.operatorId = operatorId;
        this.status = status != null ? status : "COMPLETED";
        this.payload = payload;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Operation create(String operationType, String description, UUID operatorId, String payload) {
        return new Operation(UUID.randomUUID(), operationType, description, operatorId, "COMPLETED", payload, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
