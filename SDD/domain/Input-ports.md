# Puertos de Entrada (Inbound Ports): NexusMarket

## 1. Definición y Propósito
Ubicados en `application.domain.ports.in`:
Representan los contratos formales de las operaciones de negocio del dominio en una arquitectura **100% orientada a actores**. En NexusMarket no existen puertos divididos por entidad/recurso, sino interfaces acopladas estrictamente a los roles y actores del sistema.

---

## 2. Catálogo Oficial de Puertos por Actor

| Puerto de Entrada (`*Port`) | Métodos Clave | Actor / Propósito de Negocio |
|---|---|---|
| `PublicAccessPort` | `authenticate(email, password)`, `logout(token)`, `registerBuyer(...)`, `getPublishedCatalog()`, `getPublishedProductById(id)` | **Acceso Público:** Autenticación de credenciales, emisión de token JWT, registro libre de compradores y consulta de catálogo público. |
| `BuyerPort` | `registerBuyer(...)`, `getBuyerProfile(id)`, `addAdditionalAddress(...)`, `updateAddresses(...)`, `createCart(id)`, `getCartById(id)`, `getCartByBuyerId(id)`, `addItem(...)`, `removeItem(...)`, `clearCart(id)`, `createOrderFromCart(...)`, `getOrderById(id)`, `getOrdersByBuyerId(id)`, `updateDeliveryAddress(...)`, `processPayment(...)`, `requestReturn(...)`, `getBuyerReturns(id)` | **Comprador (Buyer):** Gestión de direcciones primarias/secundarias, carrito de compras, confirmación y consulta de pedidos, procesamiento de pagos (auto-entrega para digitales) y solicitudes de devolución. |
| `SellerPort` | `registerSeller(adminId, ...)`, `getSellerProfile(id)`, `createProduct(...)`, `getProductById(id)`, `getAllProducts()`, `updateProductStatus(...)`, `updateProductPrice(...)`, `getSellerOrders(id)`, `getSellerWarehouses(id)`, `registerInitialStock(...)`, `adjustStock(...)`, `getSellerInventory(id)` | **Vendedor (Seller):** Publicación de catálogo de productos, variantes y precios, supervisión de pedidos de venta, visualización de bodegas y control de inventario/ajustes. |
| `AdministratorPort` | `registerStaff(...)`, `registerSeller(...)`, `registerSellerWithWarehouse(...)`, `updateUserStatus(...)`, `updateBuyerCommercialStatus(...)`, `getUserById(id)`, `listAllUsers()`, `createWarehouse(...)`, `updateWarehouseCapacity(...)`, `getWarehouseById(id)`, `listAllWarehouses()`, `listWarehousesByType(...)`, `listWarehousesBySeller(...)`, `approveReturn(...)`, `rejectReturn(...)`, `processRefund(...)`, `listAllReturns()`, `listAllRefunds()`, `getAdministrativeReport()`, `generateInvoice(...)`, `getInvoiceById(id)`, `getInvoiceByOrderId(id)`, `getAllInvoices()`, `recordOperation(...)`, `getOperationById(id)`, `getAllOperations()` | **Administrador (Administrator):** Gobernanza integral de usuarios y estados comerciales, onboarding con 1ra bodega, red de bodegas físicas, resolución de devoluciones y reembolsos, emisión de facturación y reporte administrativo consolidado (OBJ-12). |
| `SupervisorPort` | `reviewDispute(...)`, `approveReturn(...)`, `rejectReturn(...)`, `processRefund(...)`, `getOrderById(id)`, `getAllOrders()`, `getAllWarehouses()`, `getAllInventories()`, `getAllShipments()`, `getAllReturns()`, `getAllRefunds()`, `getConsolidatedReport()`, `getAuditOperations()` | **Supervisor (Supervisor):** Perfil de supervisión y monitoreo estrictamente de consulta: monitoreo de órdenes, estado de bodegas/inventarios, tránsitos de envíos, devoluciones/reembolsos y telemetría consolidada de negocio. |
| `LogisticsOperatorPort` | `dispatchOrder(orderId, operatorId)`, `confirmDelivery(shipmentId)`, `getShipmentById(id)`, `getShipmentByOrderId(id)`, `listShipments()`, `updateInventoryStatus(id, status)`, `registerInventoryMovement(...)`, `getInventoryMovements(id)`, `updateDeliveryStatus(orderId, status)`, `advanceOrderStatus(orderId, nextStatus)`, `registerMovement(inventoryId, qty, type, reason)`, `reserveInventory(...)`, `releaseInventory(...)` | **Operador Logístico (Logistics Operator):** Despacho y entrega física de envíos con asignación de operador, actualización de estados de lotes (AVAILABLE/DAMAGED), bitácora de movimientos físicos y transiciones de pedidos. |

---

## 3. Principios de Diseño
- **Inversión de Dependencias (DIP):** La capa de dominio define estos puertos de entrada. La capa de adaptadores (`application.adapters.useCases.*UseCaseImpl`) los implementa.
- **Segregación de Interfaces (ISP):** Cada actor interactúa únicamente con los casos de uso para los cuales tiene permisos explícitos.
- **Tipado Fuerte de Dominio:** Los puertos utilizan agregados (`Order`, `Buyer`, `Product`), Value Objects (`Address`, `Money`, `Email`, `IdentityDocument`) y modelos agnósticos de framework (`AuthenticationResult`).
