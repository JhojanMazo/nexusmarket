# Puertos de Salida (Outbound Ports): NexusMarket

## 1. Definición y Propósito
Ubicados en `application.domain.ports.out`:
Definen las pasarelas que el dominio requiere para interactuar con sistemas de persistencia, servicios de seguridad, configuración y notificaciones.

---

## 2. Catálogo de Puertos de Salida

| Puerto (`*Port` / `*RepositoryPort`) | Métodos Clave | Implementación Principal |
|---|---|---|
| `UserRepositoryPort` | `save(user)`, `findById(id)`, `findByEmail(email)`, `findByIdentityDocument(doc)`, `findAll()` | `UserJpaAdapter` (JPA/SQL) |
| `ProductRepositoryPort` | `save(product)`, `findById(id)`, `findAll()` | `ProductJpaAdapter` (JPA/SQL) |
| `ShoppingCartRepositoryPort` | `save(cart)`, `findById(id)`, `findByBuyerId(id)`, `deleteById(id)` | `ShoppingCartJpaAdapter` (JPA/SQL) |
| `OrderRepositoryPort` | `save(order)`, `findById(id)`, `findByBuyerId(id)`, `findAll()` | `OrderJpaAdapter` (JPA/SQL) |
| `WarehouseRepositoryPort` | `save(warehouse)`, `findById(id)`, `findBySellerId(id)`, `findByType(type)`, `findAll()` | `WarehouseJpaAdapter` (JPA/SQL) |
| `InventoryRepositoryPort` | `save(inventory)`, `findById(id)`, `findByProductIdAndWarehouseId(...)`, `findByWarehouseId(...)`, `findByProductId(...)`, `findAll()` | `InventoryJpaAdapter` (JPA/SQL) |
| `InventoryMovementRepositoryPort` | `save(movement)`, `findByInventoryId(id)`, `findAll()` | `InventoryMovementJpaAdapter` (JPA/SQL) |
| `ShipmentRepositoryPort` | `save(shipment)`, `findById(id)`, `findByOrderId(id)`, `findByStatus(status)`, `findAll()` | `ShipmentJpaAdapter` (JPA/SQL) |
| `ReturnRequestRepositoryPort` | `save(returnRequest)`, `findById(id)`, `findByOrderId(id)`, `findAll()` | `ReturnRequestJpaAdapter` (JPA/SQL) |
| `RefundRepositoryPort` | `save(refund)`, `findById(id)`, `findByReturnRequestId(id)`, `findAll()` | `RefundJpaAdapter` (JPA/SQL) |
| `InvoiceRepositoryPort` | `save(invoice)`, `findById(id)`, `findByOrderId(id)`, `findAll()` | `InvoiceJpaAdapter` (JPA/SQL) |
| `OperationRepositoryPort` | `save(operation)`, `findById(id)`, `findAll()` | `OperationJpaAdapter` (JPA/SQL) |
| `AuditLogRepositoryPort` | `save(auditLog)`, `findById(id)`, `findByUserId(id)`, `findAll()` | `AuditLogMongoAdapter` (MongoDB) |
| `JwtServicePort` | `generateToken(...)`, `validateToken(...)`, `extractUserId(...)`, `extractEmail(...)`, `extractRole(...)` | `JwtProvider` (HS256) |
| `PasswordServicePort` | `encode(rawPassword)`, `matches(rawPassword, encodedPassword)` | `BCryptPasswordServiceAdapter` |
| `AuthorizationPort` | `isAuthorized(userId, role)`, `hasPermission(userId, permission)` | `NexusMarketUserDetailsAdapter` |
| `BusinessConfigurationPort` | `getDefaultTaxRate()`, `getDefaultCurrency()`, `getCriticalStockThreshold()` | `DefaultBusinessConfigurationAdapter` |
| `NotificationPort` | `sendNotification(recipientId, subject, message, channel)` | `NoOpNotificationAdapter` |
