# Adaptadores de Persistencia: NexusMarket

## 1. Arquitectura de Persistencia Políglota
NexusMarket implementa el patrón de persistencia desacoplada utilizando el patrón Repository de DDD a través de puertos de salida (`ports/out/`):

```mermaid
graph LR
    subgraph Domain_Core ["Capa de Dominio"]
        ProductRepoPort["ProductRepositoryPort"]
        OrderRepoPort["OrderRepositoryPort"]
        AuditRepoPort["AuditLogRepositoryPort"]
    end

    subgraph Adapters_Persistence ["Adaptadores de Persistencia"]
        ProductJpaAdapter["ProductJpaAdapter"]
        OrderJpaAdapter["OrderJpaAdapter"]
        AuditMongoAdapter["AuditLogMongoAdapter"]
    end

    subgraph Infrastructure_Engines ["Motores de Datos"]
        SQL_DB[("SQL Server / MySQL / H2")]
        MONGO_DB[("MongoDB NoSQL")]
    end

    ProductRepoPort <|.. ProductJpaAdapter
    OrderRepoPort <|.. OrderJpaAdapter
    AuditRepoPort <|.. AuditMongoAdapter

    ProductJpaAdapter --> SQL_DB
    OrderJpaAdapter --> SQL_DB
    AuditMongoAdapter --> MONGO_DB
```

---

## 2. Adaptadores Relacionales JPA (`application.adapters.persistence.jpa`)
Implementan los puertos de salida para las entidades del modelo transaccional:
- `ProductJpaAdapter`: Persiste productos mediante `SpringDataJpaProductRepository` y `ProductJpaMapper`.
- `UserJpaAdapter`: Persiste usuarios polimórficos (`Buyer`, `Seller`, `Staff`) en tabla única con estrategia de herencia.
- `OrderJpaAdapter`: Persiste pedidos con sus ítems en cascada.
- `ShoppingCartJpaAdapter`: Persiste carritos y líneas de carrito.
- `InvoiceJpaAdapter`: Persiste facturas fiscales asociadas a pedidos.
- `OperationJpaAdapter`: Persiste ejecuciones de operaciones de auditoría operativa.
- `WarehouseJpaAdapter`: Persiste bodegas físicas y centros de distribución (`MARKETPLACE` y `SELLER`).
- `InventoryJpaAdapter`: Persiste stock distribuido por producto y bodega con sus movimientos de auditoría.
- `InventoryMovementJpaAdapter`: Persiste historial inmutable de movimientos de inventario (`INBOUND`, `RESERVATION`, `SALE_OUTBOUND`, `RETURN`, `ADJUSTMENT`).
- `ShipmentJpaAdapter`: Persiste despachos logísticos asignados a operadores.
- `ReturnRequestJpaAdapter`: Persiste solicitudes de devolución post-entrega.
- `RefundJpaAdapter`: Persiste reembolsos monetarios aprobados.

---

## 3. Adaptador Documental MongoDB (`application.adapters.persistence.mongodb`)
Implementa `AuditLogRepositoryPort`:
- `AuditLogMongoAdapter`: Persiste documentos `AuditLogDocument` en la colección `audit_logs` mediante `AuditLogMongoRepository` y `AuditLogMongoMapper`.
- Permite consultas rápidas por usuario o severidad (`AuditSeverity.LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
