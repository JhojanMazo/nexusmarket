# Especificación de Servicios de Dominio: Contexto de Productos

## 1. Servicios del Paquete `application.domain.services.product`

### 1.1 `ProductCatalogManagementService`
- **Propósito:** Mantiene la coherencia del catálogo comercial activo.

### 1.2 `ProductStateControlService`
- **Propósito:** Valida transiciones de estado permitidas (`PUBLISHED` -> `SUSPENDED` -> `DISCONTINUED`).

### 1.3 `ProductTypeDifferentiationService`
- **Propósito:** Separa reglas de envío para productos físicos vs entrega inmediata de productos digitales.

### 1.4 `ProductVariantManagementService`
- **Propósito:** Valida que las variantes del producto mantengan atributos obligatorios consistentes.
