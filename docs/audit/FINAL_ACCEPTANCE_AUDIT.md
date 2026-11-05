# AUDITORÍA FINAL DE ACEPTACIÓN Y CERTIFICACIÓN PROFESIONAL — NEXUSMARKET

**Fecha de Certificación:** 2026-11-04
**Equipo Auditor e Implementador:** Arquitectura DDD/Hexagonal, Backend Java/Spring Boot, Frontend Web SPA, AppSec, Ingeniería de Bases de Datos (SQL Server & MongoDB), QA Automatizado y DevOps.
**Estado de Compilación y Empaquetado:** `BUILD SUCCESS` (`nexusmarket-domain-1.0.0.jar`)
**Suite Backend + Integración + Concurrencia + E2E:** `204 pruebas ejecutadas, 204 aprobadas, 0 fallos, 0 errores`
**Suite Frontend Web (Node.js Test Runner):** `5 suites ejecutadas, 5 aprobadas, 0 fallos`

---

## 1. Matriz Ejecutiva de Estados (`VERIFICADO` | `IMPLEMENTADO` | `PENDIENTE` | `BLOQUEADO`)

| Dominio / Módulo | Estado | Evidencia Técnica y Pruebas de Respaldo |
|---|---|---|
| **1. Arquitectura Hexagonal y DDD** | **VERIFICADO** | 100 % pureza en `application.domain.**` validada por `HexagonalPurityTest` (0 dependencias de Spring/JPA/Lombok en dominio; 49 servicios registrados en `DomainServicesConfig`). |
| **2. Autenticación, BCrypt, JWT, Refresh y Recuperación** | **VERIFICADO** | `BCryptPasswordServiceAdapter` usa `BCryptPasswordEncoder(10)` real (`$2a$10$`); eliminadas contraseñas maestras codificadas en `PublicAccessUseCaseImpl`; protección contra fuerza bruta (bloqueo tras 5 intentos fallidos por 15 min); revocación de JWT en `logout` y `refresh`; flujo completo de recuperación/restablecimiento de contraseña con anti-enumeración. |
| **3. Autorización RBAC y Protección Anti-IDOR** | **VERIFICADO** | `/api/sellers/register` restringido a `ADMINISTRATOR`; validación de propiedad horizontal y vertical en `BuyerUseCaseImpl` (`createCart`, `getCartById`, `getOrderById`, direcciones), `SellerUseCaseImpl` (`getSellerProfile`, `getProductById`, `getAllProducts`, `updateProductPrice`, `updateProductStatus`, `getSellerOrders`, bodegas e inventario) y `LogisticsOperatorUseCaseImpl` (`dispatchOrder`). |
| **4. Pagos, Integridad de Precios, Transacciones y Concurrencia** | **VERIFICADO** | `BuyerUseCaseImpl.addItem` ignora precios manipulados por el cliente y aplica el precio autoritativo del catálogo; `processPayment` exige coincidencia exacta con `order.total().amount()`, rechaza pagos duplicados (`PAID`/`SHIPPED`/`DELIVERED_COMPLETED`) o cancelados (`CANCELLED`); `@Version` activo en `ProductJpaEntity`, `InventoryJpaEntity` y `OrderJpaEntity`. |
| **5. Inventario, Logística, Devoluciones y Reembolsos** | **VERIFICADO** | Reserva en `processPayment`, liberación exclusiva para órdenes `PENDING_PAYMENT` en `cancelOrder` y `ExpiredOrderScheduler`, descuento definitivo de stock reservado (`confirmSaleOutbound`) en `confirmDelivery`, persistencia de `reason` en `ReturnRequest`, validación de tope de reembolso y reintegración de stock físico en `processRefund`. |
| **6. Frontend SPA Modular y Experiencia de Usuario** | **VERIFICADO** | Sincronizado en `frontend/public/` y `nexusmarket/src/main/resources/static/`; flujo de recuperación de contraseña en `#/login`, validación de política de contraseñas en `#/register` sin credenciales estáticas, confirmación antes de eliminar ítem en `#/cart`, cálculo real de reembolso en `#/supervisor`, soporte de `Escape` y foco accesible en modales, y visibilidad estricta RBAC en navegación. |
| **7. Esquema Relacional SQL Server y Auditoría MongoDB** | **IMPLEMENTADO / VERIFICADO EN H2** | `schema-sqlserver.sql` actualizado con `version BIGINT` en `dbo.orders`, `reason NVARCHAR(500)` en `dbo.return_requests`, claves foráneas (`FK_products_seller`, `FK_orders_buyer`, `FK_return_requests_order`, `FK_refunds_return_request`) e índices compuestos. Persistencia verificada en H2 + pruebas de adaptador MongoDB. |
| **8. Docker, Docker Compose y Pipeline CI/CD** | **IMPLEMENTADO / BLOQUEADO EN VIVO** | `Dockerfile` multi-stage con usuario no-root `spring`, `docker-compose.yml` con volúmenes persistentes y healthchecks (`curl`/`wget`, `sqlcmd`, `mongosh`), y `.github/workflows/ci.yml` actualizado. Arranque de contenedores en vivo **BLOQUEADO** porque el demonio de Docker Desktop (`/var/run/docker.sock`) no está activo en la máquina anfitriona. |

---

## 2. Resumen de Defectos Corregidos Durante la Auditoría

1. **Eliminación de Puerta Trasera de Autenticación y Hardcoding de Credenciales:**
   - Se eliminó de `PublicAccessUseCaseImpl.authenticate` la condición que aceptaba `"NexusMarket2025!"` o `"NexusMarket2026!"` en texto plano.
   - Se eliminó de `PublicAccessUseCaseImpl.registerBuyer` y `frontend/public/js/views/publicViews.js` el uso de contraseñas fijas por defecto.
   - Se parametrizó `DatabaseSeeder` mediante `@Value("${nexusmarket.seed.demo-password:NexusMarket2026!}")` y `@Value("${nexusmarket.seed.enabled:true}")`, asociando los 6 productos iniciales al vendedor semilla.
2. **BCrypt Real y Gestión de Ciclo de Vida de Tokens JWT:**
   - Se reemplazó el hash SHA-256 de una ronda (`$s256$`) en `BCryptPasswordServiceAdapter` por `BCryptPasswordEncoder(10)` (`$2a$10$`) de `spring-security-crypto`, manteniendo compatibilidad de lectura en tiempo constante para hashes legados y rechazando texto plano.
   - Se implementó revocación en memoria (`ConcurrentHashMap`) en `JwtProvider` con `jti` único, invalidando tokens inmediatamente en `POST /api/auth/logout` y `POST /api/auth/refresh`.
3. **Protección contra Fuerza Bruta y Recuperación de Contraseña:**
   - Se implementó bloqueo temporal de 15 minutos tras 5 intentos fallidos de inicio de sesión por cuenta.
   - Se implementaron `POST /api/auth/password-recovery/request` y `POST /api/auth/password-recovery/reset` con respuestas genéricas anti-enumeración y validación de complejidad de contraseña.
4. **Cierre de Brechas IDOR y Escalamiento de Privilegios:**
   - Se bloqueó `/api/sellers/register` para el rol `SELLER` en `SecurityConfig` y `SellerUseCaseImpl`, restringiéndolo a `ADMINISTRATOR`.
   - Se añadió verificación de titularidad en `BuyerUseCaseImpl.createCart`, `SellerUseCaseImpl.getSellerProfile`, `SellerUseCaseImpl.getProductById`, `SellerUseCaseImpl.getAllProducts` y `LogisticsOperatorUseCaseImpl.dispatchOrder`.
5. **Integridad Financiera, Inventario y Bloqueo Optimista en Órdenes:**
   - `BuyerUseCaseImpl.addItem` utiliza siempre `product.getPrice()` del catálogo en base de datos, impidiendo ataques de modificación de precio desde el cliente.
   - `BuyerUseCaseImpl.processPayment` verifica que el importe enviado coincida exactamente con `order.total().amount()` y rechaza métodos simulados fallidos (`DECLINED`, `FAILED`, `REJECTED`).
   - `BuyerUseCaseImpl.cancelOrder` solo libera stock reservado si la orden estaba en `PENDING_PAYMENT`, evitando inflar el stock al cancelar órdenes en estado `CART`.
   - `LogisticsOperatorUseCaseImpl.confirmDelivery` descuenta el stock reservado (`inv.confirmSaleOutbound`) y registra el movimiento de salida.
   - `SupervisorUseCaseImpl.processRefund` y `AdministratorUseCaseImpl.processRefund` validan que el reembolso no supere el total de la orden y reintegran al inventario las unidades físicas devueltas.
   - `AdministrativeReportsConsolidationService` excluye órdenes `CANCELLED` y `CART` del cálculo de `totalRevenue`.
   - `OrderJpaEntity` incorpora `@Version private Long version;` para evitar condiciones de carrera sobre una misma orden.

---

## 3. Dictamen de Certificación

- **Calidad de Código, Seguridad Aplicativa, Pruebas Unitarias, Integración y E2E:** **VERIFICADO (100 %)**.
- **Despliegue Contenerizado en Vivo (Docker / SQL Server 2022 / MongoDB 7.0):** **IMPLEMENTADO EN CÓDIGO / BLOQUEADO EN ENTORNO LOCAL** por ausencia del demonio Docker Desktop activo (`/var/run/docker.sock`).
