# Especificación de Servicios de Dominio: Contexto de Órdenes

## 1. Servicios del Paquete `application.domain.services.order`

### 1.1 `ShoppingCartManagementService`
- **Propósito:** Valida que el carrito pertenezca a un comprador activo y calcula subtotales.

### 1.2 `OrderStatusMonitoringService`
- **Propósito:** Supervisa que las transiciones de estado de pedido respeten el flujo oficial (`PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED`).

### 1.3 `CompletedOrderImmutabilityService`
- **Propósito:** Impide cualquier modificación de precio o dirección a pedidos en estado final (`DELIVERED`, `CANCELLED`).
