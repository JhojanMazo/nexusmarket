# Especificación de Servicios de Dominio: Contexto de Inventario

## 1. Servicios del Paquete `application.domain.services.inventory`

### 1.1 `CriticalStockValidationService`
- **Propósito:** Alerta cuando el inventario cae por debajo del umbral crítico configurado.

### 1.2 `DistributedInventoryManagementService`
- **Propósito:** Maneja el cálculo de stock disponible entre almacenes centrales y regionales.

### 1.3 `InventoryReservationService`
- **Propósito:** Reserva stock temporal para carritos de compra activos, evitando sobreventa.

### 1.4 `SalesExitRegistrationService`
- **Propósito:** Realiza la salida definitiva de inventario al confirmarse el pedido.

### 1.5 `InventoryAdjustmentsReturnsService`
- **Propósito:** Ajusta e incrementa el stock al confirmarse el reingreso de una devolución.

### 1.6 `InventoryEntryRegistrationService`
- **Propósito:** Registra nuevas recepciones de mercancía en almacenes.

### 1.7 `WarehouseClassificationService` y `WarehouseInformationControlService`
- **Propósito:** Clasifica y controla el estado operativo de los almacenes físicos.
