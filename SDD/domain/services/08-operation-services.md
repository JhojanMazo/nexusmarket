# Especificación de Servicios de Dominio: Contexto de Operaciones y Auditoría

## 1. Servicios del Paquete `application.domain.services.operation`

### 1.1 `RegisterOperationService`
- **Propósito:** Persiste el registro relacional (SQL) de la operación de negocio ejecutada.

### 1.2 `RegisterAuditLogService`
- **Propósito:** Persiste el documento inmutable (MongoDB) con severidad y metadatos de red (IP, usuario).

### 1.3 `RegisterOperationAndAuditService`
- **Propósito:** Orquesta el doble registro coordinado en ambos motores de persistencia.

### 1.4 `ConsultOperationsService` y `ConsultAuditLogsService`
- **Propósito:** Proveen consultas de trazabilidad forense e inspección administrativa.
