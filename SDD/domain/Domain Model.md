# Especificación del Modelo de Dominio: NexusMarket

## 1. Agregados y Entidades de Dominio
Ubicados en `application.domain.models`:

### 1.1 `User`, `Buyer`, `Seller`
- **`User`:** Entidad base con `id`, `fullName`, `email`, `identityDocument`, `role` (`UserRole`) y `status` (`UserStatus`).
- **`Buyer`:** Extiende de `User`. Añade `primaryAddress`, `secondaryAddresses` y `commercialStatus` (`BuyerCommercialStatus`).
- **`Seller`:** Extiende de `User`. Añade `approvedByAdminId` y `onboardingDate`.

### 1.2 `Product`
- Representa un ítem comercializable.
- Atributos: `productId`, `name`, `variant` (`ProductVariant`), `price` (`Money`), `status` (`ProductStatus`), `type` (`ProductType`).
- Métodos inmutables: `withStatus(...)`, `withPrice(...)`.

### 1.3 `ShoppingCart`
- Agregado que acumula los ítems temporales de compra de un comprador antes de ordenar.
- Atributos: `cartId`, `buyerId`, `items` (`List<OrderItem>`).

### 1.4 `Order`
- Transacción formal de compra con dirección de entrega y total monetario.
- Atributos: `orderId`, `buyerId`, `items`, `totalAmount`, `status` (`OrderStatus`), `deliveryAddress`.
- Ciclo de vida: `PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED` / `CANCELLED`.

### 1.5 `Invoice`
- Documento fiscal derivado de una orden completada.
- Atributos: `invoiceId`, `orderId`, `subtotal`, `taxAmount`, `totalAmount`, `issueDate`.

### 1.6 `Warehouse`, `Inventory`, `InventoryMovement`
- Gestión física y lógica de existencias en múltiples almacenes.
- Atributos: Almacenes clasificados (`MAIN_DISTRIBUTION`, `REGIONAL_FULFILLMENT`, `LOCAL_DISPATCH`), stock disponible, reservado y movimientos trazables.

### 1.7 `Shipment`, `ReturnRequest`, `Refund`
- Control logístico de despacho, solicitud formal de retorno de mercancía y emisión de reembolsos contables.

### 1.8 `Operation` y `AuditLog`
- Registro de transacciones operativas (SQL) y eventos forenses inmutables (MongoDB).

### 1.9 `AdministrativeReport`
- Modelo de agregación y telemetría consolidada del negocio (OBJ-12): total usuarios por rol, catálogo de productos, recuento de bodegas, pedidos clasificados por estado, recaudación bruta total, envíos, devoluciones y reembolsos.
