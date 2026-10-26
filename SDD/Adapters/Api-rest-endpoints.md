# Catálogo de Endpoints de la API REST: NexusMarket

NexusMarket organiza su API REST de manera **100% orientada a actores**. Cada recurso base se corresponde con el actor que tiene permiso para ejecutar la operación.

---

## 1. Actor: Acceso Público (`/api/auth`)
Controlador: `AuthRestController` (Inyecta `PublicAccessPort`)

| Método | Endpoint | Descripción | Body Solicitud | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/api/auth/login` | Autentica credenciales y emite token JWT | `LoginRequestDTO` | `200 OK` + `LoginResponseDTO` |
| `POST` | `/api/auth/register` | Registro público de un nuevo comprador | `CreateBuyerRequestDTO` | `201 CREATED` + `BuyerResponseDTO` |
| `POST` | `/api/auth/logout` | Cierre de sesión y revocación | Header: `Authorization: Bearer <token>` | `200 OK` + `Map<String, String>` |
| `GET` | `/api/auth/catalog` | Catálogo público de productos disponibles | - | `200 OK` + `List<ProductResponseDTO>` |
| `GET` | `/api/auth/catalog/{productId}` | Detalle público de producto publicado | - | `200 OK` + `ProductResponseDTO` |

---

## 2. Actor: Comprador (`/api/buyers`)
Controlador: `BuyerRestController` (Inyecta `BuyerPort`)

| Método | Endpoint | Descripción | Body Solicitud | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/api/buyers/register` | Registro formal de perfil de comprador | `CreateBuyerRequestDTO` | `201 CREATED` + `BuyerResponseDTO` |
| `GET` | `/api/buyers/{buyerId}` | Consulta el perfil del comprador | - | `200 OK` + `BuyerResponseDTO` |
| `POST` | `/api/buyers/{buyerId}/addresses` | Agrega una dirección secundaria de entrega | `AddressDTO` | `200 OK` + `BuyerResponseDTO` |
| `PUT` | `/api/buyers/{buyerId}/addresses` | Actualiza direcciones primaria y secundarias | `UpdateBuyerAddressesRequestDTO` | `200 OK` + `BuyerResponseDTO` |
| `POST` | `/api/buyers/{buyerId}/cart` | Crea un carrito para el comprador | - | `201 CREATED` + `ShoppingCartResponseDTO` |
| `GET` | `/api/buyers/{buyerId}/cart` | Obtiene el carrito activo del comprador | - | `200 OK` + `ShoppingCartResponseDTO` |
| `GET` | `/api/buyers/carts/{cartId}` | Consulta carrito por su ID | - | `200 OK` + `ShoppingCartResponseDTO` |
| `POST` | `/api/buyers/carts/{cartId}/items` | Agrega un producto al carrito | `AddCartItemRequestDTO` | `200 OK` + `ShoppingCartResponseDTO` |
| `DELETE`| `/api/buyers/carts/{cartId}/items/{productId}` | Elimina un producto del carrito | - | `200 OK` + `ShoppingCartResponseDTO` |
| `DELETE`| `/api/buyers/carts/{cartId}` | Vacía y limpia el carrito de compras | - | `204 NO CONTENT` |
| `POST` | `/api/buyers/orders` | Genera una orden a partir del carrito | `CreateOrderRequestDTO` | `201 CREATED` + `OrderResponseDTO` |
| `GET` | `/api/buyers/orders/{orderId}` | Consulta detalle de un pedido | - | `200 OK` + `OrderResponseDTO` |
| `GET` | `/api/buyers/{buyerId}/orders` | Lista el historial de pedidos del comprador | - | `200 OK` + `List<OrderResponseDTO>` |
| `PUT` | `/api/buyers/orders/{orderId}/delivery-address` | Modifica dirección de entrega | `AddressDTO` | `200 OK` + `OrderResponseDTO` |
| `POST` | `/api/buyers/orders/{orderId}/pay` | Procesa pago (digitales auto-completados a DELIVERED) | `ProcessPaymentRequestDTO` | `200 OK` + `PaymentResponseDTO` |
| `POST` | `/api/buyers/orders/{orderId}/return` | Solicita devolución de un pedido entregado | `Map<String, String>` (reason) | `201 CREATED` + `Map<String, Object>` |
| `GET` | `/api/buyers/{buyerId}/returns` | Consulta el historial de devoluciones solicitadas | - | `200 OK` + `List<ReturnRequestResponseDTO>` |

---

## 3. Actor: Vendedor (`/api/sellers`)
Controlador: `SellerRestController` (Inyecta `SellerPort`)

| Método | Endpoint | Descripción | Body Solicitud | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/api/sellers/register` | Registro / Onboarding de vendedor | `CreateSellerRequestDTO` | `201 CREATED` + `SellerResponseDTO` |
| `GET` | `/api/sellers/{sellerId}` | Consulta el perfil comercial del vendedor | - | `200 OK` + `SellerResponseDTO` |
| `POST` | `/api/sellers/products` | Publica un nuevo producto con variante | `ProductRequestDTO` | `201 CREATED` + `ProductResponseDTO` |
| `GET` | `/api/sellers/products` | Lista catálogo global de productos | - | `200 OK` + `List<ProductResponseDTO>` |
| `GET` | `/api/sellers/products/{productId}` | Consulta detalle de un producto | - | `200 OK` + `ProductResponseDTO` |
| `PATCH` | `/api/sellers/products/{productId}/status` | Cambia estado comercial del producto | `ProductStatusUpdateRequestDTO` | `200 OK` + `ProductResponseDTO` |
| `PATCH` | `/api/sellers/products/{productId}/price` | Actualiza precio y moneda del producto | `ProductPriceUpdateRequestDTO` | `200 OK` + `ProductResponseDTO` |
| `GET` | `/api/sellers/{sellerId}/orders` | Consulta órdenes asociadas al vendedor | - | `200 OK` + `List<OrderResponseDTO>` |
| `GET` | `/api/sellers/{sellerId}/warehouses` | Lista bodegas asignadas al vendedor | - | `200 OK` + `List<WarehouseResponseDTO>` |
| `POST` | `/api/sellers/{sellerId}/inventory/initial` | Registra entrada de stock inicial en bodega | `Map` (productId, warehouseId, quantity) | `201 CREATED` + `InventoryResponseDTO` |
| `PATCH` | `/api/sellers/{sellerId}/inventory/{inventoryId}/adjust` | Ajuste manual de stock por vendedor | `Map` (quantity, reason) | `200 OK` + `InventoryResponseDTO` |
| `GET` | `/api/sellers/{sellerId}/inventory` | Lista inventario distribuido del vendedor | - | `200 OK` + `List<InventoryResponseDTO>` |

---

## 4. Actor: Administrador (`/api/admin`)
Controlador: `AdministratorRestController` (Inyecta `AdministratorPort`)

| Método | Endpoint | Descripción | Body Solicitud | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/api/admin/users/staff` | Da de alta personal interno (Staff) | `CreateStaffUserRequestDTO` | `201 CREATED` + `UserResponseDTO` |
| `POST` | `/api/admin/users/seller` | Da de alta vendedor + 1ra bodega en una transacción | `CreateSellerRequestDTO` | `201 CREATED` + `SellerResponseDTO` |
| `GET` | `/api/admin/users` | Lista todos los usuarios registrados | - | `200 OK` + `List<UserResponseDTO>` |
| `GET` | `/api/admin/users/{userId}` | Consulta información de un usuario | - | `200 OK` + `UserResponseDTO` |
| `PATCH` | `/api/admin/users/{userId}/status` | Modifica estado de cuenta (ACTIVE, BLOCKED, INACTIVE) | `UpdateUserStatusRequestDTO` | `200 OK` + `UserResponseDTO` |
| `PATCH` | `/api/admin/users/buyer/{buyerId}/commercial-status` | Modifica estado comercial de comprador | `UpdateBuyerCommercialStatusRequestDTO` | `200 OK` + `BuyerResponseDTO` |
| `POST` | `/api/admin/warehouses` | Crea una bodega física | `CreateWarehouseRequestDTO` | `201 CREATED` + `WarehouseResponseDTO` |
| `GET` | `/api/admin/warehouses` | Lista bodegas (filtros opcionales por type/sellerId) | Query Params opcionales: `type`, `sellerId` | `200 OK` + `List<WarehouseResponseDTO>` |
| `GET` | `/api/admin/warehouses/{warehouseId}` | Consulta detalle de una bodega | - | `200 OK` + `WarehouseResponseDTO` |
| `PATCH` | `/api/admin/warehouses/{warehouseId}/capacity` | Modifica capacidad máxima de bodega | `UpdateWarehouseRequestDTO` | `200 OK` + `WarehouseResponseDTO` |
| `GET` | `/api/admin/returns` | Monitorea todas las solicitudes de devolución | - | `200 OK` + `List<ReturnRequestResponseDTO>` |
| `PATCH` | `/api/admin/returns/{requestId}/approve` | Aprueba solicitud de devolución | - | `200 OK` + `ReturnRequestResponseDTO` |
| `PATCH` | `/api/admin/returns/{requestId}/reject` | Deniega solicitud de devolución | - | `200 OK` + `ReturnRequestResponseDTO` |
| `GET` | `/api/admin/refunds` | Lista todos los reembolsos gestionados | - | `200 OK` + `List<RefundResponseDTO>` |
| `POST` | `/api/admin/refunds` | Ejecuta reembolso financiero compensatorio | `ProcessRefundRequestDTO` | `201 CREATED` + `RefundResponseDTO` |
| `GET` | `/api/admin/reports/consolidated` | Reporte administrativo integral del negocio (OBJ-12) | - | `200 OK` + `AdministrativeReportResponseDTO` |
| `POST` | `/api/admin/invoices` | Genera factura fiscal para una orden | `GenerateInvoiceRequestDTO` | `201 CREATED` + `InvoiceResponseDTO` |
| `GET` | `/api/admin/invoices` | Lista todas las facturas emitidas | - | `200 OK` + `List<InvoiceResponseDTO>` |
| `GET` | `/api/admin/invoices/{invoiceId}` | Consulta factura fiscal por ID | - | `200 OK` + `InvoiceResponseDTO` |
| `GET` | `/api/admin/invoices/order/{orderId}` | Consulta factura asociada a una orden | - | `200 OK` + `InvoiceResponseDTO` |
| `POST` | `/api/admin/operations` | Registra operación administrativa en bitácora | `Map<String, String>` | `201 CREATED` + `OperationResponseDTO` |
| `GET` | `/api/admin/operations` | Consulta bitácora de operaciones | - | `200 OK` + `List<OperationResponseDTO>` |
| `GET` | `/api/admin/operations/{operationId}`| Consulta detalle de una operación | - | `200 OK` + `OperationResponseDTO` |

---

## 5. Actor: Supervisor (`/api/supervisors`)
Controlador: `SupervisorRestController` (Inyecta `SupervisorPort`)

| Método | Endpoint | Descripción | Body Solicitud | Respuesta Exitosa |
|---|---|---|---|---|
| `GET` | `/api/supervisors/orders` | Monitorea todas las órdenes del sistema | - | `200 OK` + `List<OrderResponseDTO>` |
| `GET` | `/api/supervisors/orders/{orderId}` | Inspecciona orden en supervisión | - | `200 OK` + `OrderResponseDTO` |
| `GET` | `/api/supervisors/warehouses` | Supervisión de todas las bodegas de la red | - | `200 OK` + `List<WarehouseResponseDTO>` |
| `GET` | `/api/supervisors/inventories` | Supervisión integral del stock e inventarios | - | `200 OK` + `List<InventoryResponseDTO>` |
| `GET` | `/api/supervisors/shipments` | Monitoreo del estado de envíos y tránsitos | - | `200 OK` + `List<ShipmentResponseDTO>` |
| `GET` | `/api/supervisors/returns` | Auditoría de solicitudes de devolución | - | `200 OK` + `List<ReturnRequestResponseDTO>` |
| `GET` | `/api/supervisors/refunds` | Auditoría de reembolsos procesados | - | `200 OK` + `List<RefundResponseDTO>` |
| `GET` | `/api/supervisors/reports/consolidated` | Reporte consolidado de telemetría de negocio | - | `200 OK` + `AdministrativeReportResponseDTO` |
| `GET` | `/api/supervisors/audit/operations` | Auditoría de operaciones del sistema | - | `200 OK` + `List<OperationResponseDTO>` |
| `POST` | `/api/supervisors/disputes/{disputeId}/review` | Emite decisión sobre disputa (APPROVED/REJECTED) | `Map<String, String>` + Query Param: `decision` | `200 OK` + `Map<String, String>` |

---

## 6. Actor: Operador Logístico (`/api/logistics`)
Controlador: `LogisticsOperatorRestController` (Inyecta `LogisticsOperatorPort`)

| Método | Endpoint | Descripción | Body Solicitud | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/api/logistics/shipments/dispatch` | Despacho de orden: PAID -> DISPATCHED (crea envío) | `CreateShipmentRequestDTO` | `201 CREATED` + `ShipmentResponseDTO` |
| `PATCH` | `/api/logistics/shipments/{shipmentId}/deliver` | Confirmación de entrega física: DISPATCHED -> DELIVERED | - | `200 OK` + `ShipmentResponseDTO` |
| `GET` | `/api/logistics/shipments` | Lista todos los envíos gestionados | - | `200 OK` + `List<ShipmentResponseDTO>` |
| `GET` | `/api/logistics/shipments/{shipmentId}` | Consulta detalle de un envío | - | `200 OK` + `ShipmentResponseDTO` |
| `GET` | `/api/logistics/shipments/order/{orderId}` | Consulta envío asociado a una orden | - | `200 OK` + `ShipmentResponseDTO` |
| `PATCH` | `/api/logistics/inventory/{inventoryId}/status` | Modifica estado del lote (AVAILABLE, DAMAGED, etc.) | `Map` (status) | `200 OK` + `InventoryResponseDTO` |
| `POST` | `/api/logistics/inventory/{inventoryId}/movement` | Registra movimiento físico tipificado con auditoría | `InventoryMovementRequestDTO` | `201 CREATED` + `InventoryMovementResponseDTO` |
| `GET` | `/api/logistics/inventory/{inventoryId}/movements` | Consulta bitácora de movimientos del inventario | - | `200 OK` + `List<InventoryMovementResponseDTO>` |
| `PATCH` | `/api/logistics/orders/{orderId}/status` | Actualiza estado de orden | `UpdateOrderStatusRequestDTO` | `200 OK` + `OrderResponseDTO` |
| `POST` | `/api/logistics/orders/{orderId}/advance` | Avanza secuencialmente el estado de pedido | `UpdateOrderStatusRequestDTO` | `200 OK` + `OrderResponseDTO` |
| `POST` | `/api/logistics/inventory/reserve` | Reserva unidades de producto | Query Params: `productId`, `quantity` | `200 OK` + `Map<String, Object>` |
| `POST` | `/api/logistics/inventory/release` | Libera unidades reservadas | Query Params: `productId`, `quantity` | `200 OK` + `Map<String, Object>` |
