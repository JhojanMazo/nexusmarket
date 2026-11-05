# REPORTE DE EJECUCIÓN DE PRUEBAS Y CONTROL DE CALIDAD — NEXUSMARKET

**Fecha de Ejecución:** 2026-11-04  
**Entorno de Ejecución:** Java 17.0.18 LTS (Eclipse Temurin), Maven 3.10.0, Spring Boot 4.1.0, Node.js Test Runner.

---

## 1. Resumen Consolidado de Resultados

| Suite de Pruebas | Comando Ejecutado | Total Pruebas | Aprobadas | Fallidas | Errores | Omitidas | Tiempo | Estado |
|---|---|---:|---:|---:|---:|---:|---:|---|
| **Backend Unitarias, Integración, Concurrencia y E2E** | `cd nexusmarket && ./mvnw -o clean verify` | **204** | **204** | **0** | **0** | **0** | 23.6 s | **VERIFICADO (100 %)** |
| **Frontend Modular SPA (JS/ES6)** | `cd frontend && npm run sync && npm test` | **5** | **5** | **0** | **0** | **0** | 0.10 s | **VERIFICADO (100 %)** |
| **Total General Automatizado** | — | **209** | **209** | **0** | **0** | **0** | **23.7 s** | **VERIFICADO (100 %)** |

---

## 2. Desglose por Categoría de Pruebas Backend (204 Pruebas)

1. **Arquitectura y Pureza de Dominio (`HexagonalPurityTest`):**
   - Verifica mediante reflexión y análisis de imports que ninguna clase dentro de `application.domain.**` dependa de Spring Framework, Jakarta Persistence/Validation ni Lombok (`1/1 aprobada`).
2. **Modelos y Servicios de Dominio (DDD):**
   - `OrderTest`, `ShoppingCartTest`, `InventoryReservationServiceTest`, `CriticalStockValidationServiceTest`, `ProductStateControlServiceTest`, `LogisticsAndReturnsServiceTest`, `BillingAdministrationServiceTest`, `CompletedOrderImmutabilityServiceTest`, `RolePermissionManagementServiceTest`, `AuthorizeActionServiceTest`, `UserOperationalStateServiceTest`, `BuyerCommercialStateControlServiceTest`, `UniqueIdentityValidationServiceTest`, `RegisterOperationAndAuditServiceTest`.
3. **Seguridad y Criptografía:**
   - `BCryptPasswordServiceAdapterTest`: Cifrado BCrypt `$2a$10$` con sal aleatoria y verificación constante.
   - `JwtProviderTest`: Emisión, validación, expiración y rechazo de tokens manipulados.
   - `NexusMarketUserDetailsAdapterTest` y `OpenApiAndSecurityConfigTest`: Reglas de acceso por ruta y rol.
4. **Casos de Uso Aplicativos (`*UseCaseImplTest`):**
   - `PublicAccessUseCaseImplTest` (11 pruebas): Login exitoso/fallido, bloqueo por fuerza bruta tras 5 intentos, validación de política de contraseñas, recuperación y restablecimiento de contraseña con anti-enumeración y consumo único de token.
   - `BuyerUseCaseImplTest` (11 pruebas): Carrito, prevención de manipulación de precio del cliente en `addItem`, creación de orden, pago válido, rechazo de monto alterado, rechazo de pasarela declinada, idempotencia de pago en órdenes pagadas/canceladas, liberación de stock y gestión de direcciones con validación de propiedad.
   - `SellerUseCaseImplTest` (7 pruebas): Creación y actualización de productos, validación de propiedad multi-tenant y filtrado de órdenes por vendedor.
   - `LogisticsOperatorUseCaseImplTest`, `SupervisorUseCaseImplTest`, `AdministratorUseCaseImplTest`.
5. **Persistencia JPA, Auditoría, Caché y Concurrencia (`@DataJpaTest`):**
   - `ConcurrencyAndAuditingIntegrationTest` (4 pruebas): Bloqueo optimista `@Version` (`OptimisticLockException`) en `ProductJpaEntity`, `InventoryJpaEntity` y `OrderJpaEntity`, además de inmutabilidad de `createdAt` y actualización de `updatedAt`.
   - `PaginationAndCacheIntegrationTest` y `CacheConfigTest`: Segregación entre `products_catalog` y `seller_products`.
   - `UserJpaAdapterTest`, `ProductJpaAdapterTest`, `InventoryJpaAdapterTest`, `ShoppingCartJpaAdapterTest`, `OrderJpaAdapterTest`, `OrderJpaAdapterIntegrationTest`, `InvoiceJpaAdapterTest`, `ShipmentJpaAdapterTest`, `ReturnRefundJpaAdapterTest`, `WarehouseJpaAdapterTest`, `OperationJpaAdapterTest`, `AuditLogMongoAdapterTest`, `AuditAspectTest`, `OrderEventListenerTest`, `ExpiredOrderSchedulerTest`.
6. **Controladores REST y Validación de Contratos:**
   - `AuthRestControllerTest`, `BuyerRestControllerTest`, `SellerRestControllerTest`, `LogisticsOperatorRestControllerTest`, `SupervisorRestControllerTest`, `AdministratorRestControllerTest`, `RequestCatalogValidationTest`.

---

## 3. Cobertura de los 15 Escenarios End-to-End (`FullStackMarketplaceE2ETest`)

La suite `FullStackMarketplaceE2ETest` levanta el esquema relacional, `DatabaseSeeder`, servicios de dominio, casos de uso, `JwtProvider`, `JwtAuthenticationFilter`, `BCryptPasswordServiceAdapter`, los 6 controladores REST y `GlobalExceptionHandler`, verificando tanto las respuestas HTTP como el estado real en los repositorios de base de datos:

1. **Registro de comprador:** `POST /api/auth/register` -> `201 Created` con rol `BUYER`.
2. **Inicio de sesión:** `POST /api/auth/login` -> `200 OK` con token JWT Bearer válido.
3. **Registro de vendedor:** `POST /api/admin/users/seller` por administrador + activación de contraseña con `/api/auth/password-recovery/request` y `/reset` + login del nuevo vendedor.
4. **Creación de productos:** `POST /api/sellers/products` y actualización de precio `PATCH /api/sellers/products/{id}/price`.
5. **Consulta del catálogo:** `GET /api/auth/catalog` y detalle `GET /api/auth/catalog/{id}`.
6. **Búsqueda y filtrado:** Filtro combinado por `search=MacBook`, `type=PHYSICAL`, `sortBy=price`, `direction=DESC`.
7. **Agregar y actualizar productos en el carrito:** `POST /api/buyers/carts/{cartId}/items` y `PUT /api/buyers/carts/{cartId}/items/{productId}`.
8. **Realizar una compra:** `POST /api/buyers/orders` -> `201 Created`.
9. **Procesar el pago:** `POST /api/buyers/orders/{orderId}/pay` -> `200 OK` (`APPROVED`).
10. **Actualizar e inspeccionar el inventario:** Reserva de stock de 50 a 48 unidades tras el pago, registro de stock inicial (`POST /api/sellers/{sellerId}/inventory/initial`) y reintegración de 48 a 50 unidades tras el reembolso.
11. **Consultar historial de pedidos y cancelación:** `GET /api/buyers/{buyerId}/orders` y `POST /api/buyers/orders/{orderId}/cancel`.
12. **Gestionar el envío:** Despacho (`POST /api/logistics/shipments/dispatch`) y confirmación de entrega (`PATCH /api/logistics/shipments/{shipmentId}/deliver`) con descuento definitivo de stock reservado.
13. **Acceder al panel administrativo y gestión post-venta:** Consulta de reporte consolidado (`GET /api/admin/reports/consolidated`), aprobación de devolución (`PATCH /api/admin/returns/{id}/approve`) y emisión de reembolso (`POST /api/admin/refunds`).
14. **Intentar acceder a recursos sin autorización:** Solicitudes sin token (`401 Unauthorized`), token revocado tras `/api/auth/refresh` y `/api/auth/logout` (`401 Unauthorized`), y comprador intentando acceder a rutas de administrador o vendedor (`403 Forbidden`).
15. **Intentar acceder a información perteneciente a otro usuario o vendedor (Anti-IDOR):** Comprador intentando leer perfil de otro comprador (`403 Forbidden`) y Vendedor 2 intentando modificar precio, ver órdenes o perfil de Vendedor 1 (`403 Forbidden`).

---

## 4. Suite de Pruebas Frontend (`frontend/tests/frontend.test.js`)

1. `sessionStore gestiona ciclo de vida de sesión JWT, expiración y roles`: Verifica decodificación base64url, detección automática de tokens expirados (`exp`) y evaluación de roles.
2. `router evalúa parámetros dinámicos y guardas de autorización RBAC`: Verifica coincidencia de rutas parametrizadas (`/product/:id`) y redirección de rutas protegidas (`#/login`, `#/catalog`).
3. `httpClient inyecta Bearer token, construye parámetros de consulta y maneja errores HTTP`: Verifica cabeceras `Authorization`, serialización de query params y limpieza automática de sesión ante errores `401`.
4. `uiComponents previene XSS y renderiza tarjetas de producto y paginación accesible`: Verifica escape de etiquetas `<script>` maliciosas, ilustraciones SVG y paginador accesible.
5. `marketplaceApi cubre recuperación de contraseña, renovación JWT, facturación y auditoría`: Verifica contratos de `requestPasswordRecovery`, `resetPassword`, `refreshToken`, `generateInvoice` y `getAuditLogs`.
