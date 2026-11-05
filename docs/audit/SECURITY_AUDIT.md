# AUDITORÍA DE SEGURIDAD APLICATIVA (APPSEC) — NEXUSMARKET

**Fecha:** 2026-11-04  
**Alcance:** Autenticación, Autorización (RBAC/ABAC/IDOR), Criptografía, Gestión de Secretos, Validación de Entradas, Cabeceras HTTP, Dependencias y Trazabilidad de Auditoría.

---

## 1. Controles de Autenticación y Gestión de Credenciales

| Control de Seguridad | Estado | Implementación y Archivo Fuente |
|---|---|---|
| **Hashing de Contraseñas con BCrypt** | **VERIFICADO** | `BCryptPasswordServiceAdapter.java` utiliza `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10)` generando hashes `$2a$10$...` con sal aleatoria por usuario. Nunca acepta texto plano. |
| **Eliminación de Contraseñas Maestras / Backdoors** | **VERIFICADO** | Se eliminó el bypass de `"NexusMarket2025!"`/`"NexusMarket2026!"` en `PublicAccessUseCaseImpl.authenticate` y la contraseña fija en `registerBuyer`. |
| **Política de Complejidad de Contraseñas** | **VERIFICADO** | `PublicAccessUseCaseImpl.validatePasswordPolicy` exige mínimo 8 caracteres, al menos una mayúscula, una minúscula y un dígito tanto en registro como en restablecimiento de contraseña. |
| **Protección contra Fuerza Bruta** | **VERIFICADO** | `PublicAccessUseCaseImpl` contabiliza intentos fallidos por correo y bloquea temporalmente la cuenta durante 15 minutos al alcanzar 5 intentos fallidos consecutivos (`MAX_FAILED_ATTEMPTS = 5`). |
| **Prevención de Enumeración de Usuarios** | **VERIFICADO** | `POST /api/auth/login` devuelve siempre `"Credenciales inválidas"` y `POST /api/auth/password-recovery/request` devuelve siempre el mismo mensaje de confirmación exista o no el correo. |
| **Firma, Expiración y Revocación de JWT** | **VERIFICADO** | `JwtProvider.java` firma tokens con `HMAC-SHA256`, incluye `jti` único, valida `exp` en tiempo constante (`MessageDigest.isEqual`) y mantiene registro de tokens revocados en `logout` y `refresh`. |

---

## 2. Controles de Autorización (RBAC y Anti-IDOR)

| Vector Evaluado | Estado | Mecanismo de Protección |
|---|---|---|
| **Escalamiento Vertical de Privilegios** | **VERIFICADO** | `SecurityConfig.java` y `JwtAuthenticationFilter.java` restringen `/api/admin/**` a `ADMINISTRATOR`, `/api/supervisors/**` a `SUPERVISOR`/`ADMINISTRATOR`, `/api/logistics/**` a `LOGISTICS_OPERATOR`/`ADMINISTRATOR`, `/api/sellers/register` exclusivamente a `ADMINISTRATOR`, `/api/sellers/**` a `SELLER`/`ADMINISTRATOR` y `/api/buyers/**` a `BUYER`/`ADMINISTRATOR`. |
| **IDOR Horizontal entre Compradores** | **VERIFICADO** | `BuyerUseCaseImpl.assertAuthenticatedBuyerMatches` verifica que el `userId` del token JWT coincida con el `buyerId` del perfil, carrito (`createCart`, `getCartById`, `getCartByBuyerId`), direcciones secundarias, órdenes (`getOrderById`, `getOrdersByBuyerId`, `processPayment`, `cancelOrder`) y devoluciones. |
| **IDOR Horizontal entre Vendedores** | **VERIFICADO** | `SellerUseCaseImpl` verifica la titularidad del vendedor autenticado en `getSellerProfile`, `getProductById`, `getAllProducts` (filtrado por `sellerId`), `updateProductPrice`, `updateProductStatus`, `getSellerOrders`, `getSellerWarehouses`, `getSellerInventory`, `registerInitialStock` y `adjustStock`. |
| **IDOR en Despacho Logístico** | **VERIFICADO** | `LogisticsOperatorUseCaseImpl.dispatchOrder` valida que el operador autenticado no suplante el `logisticsOperatorId` de otro operador. |
| **Auto-bloqueo Administrativo** | **VERIFICADO** | `AdministratorUseCaseImpl.updateUserStatus` impide que un administrador bloquee o desactive su propia cuenta activa. |

---

## 3. Seguridad en Pagos, Cabeceras HTTP, XSS, SQLi y Secretos

1. **Integridad de Precios y Pagos:**
   - `BuyerUseCaseImpl.addItem` ignora cualquier precio positivo manipulado desde el cliente y toma exclusivamente el precio oficial del producto publicado en base de datos (`product.getPrice()`).
   - `BuyerUseCaseImpl.processPayment` valida que `amount` sea exactamente igual a `order.total().amount()`.
2. **Cabeceras de Seguridad HTTP (`JwtAuthenticationFilter.java` y `frontend/server.js`):**
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: DENY`
   - `X-XSS-Protection: 0`
   - `Referrer-Policy: strict-origin-when-cross-origin`
   - `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`
3. **Prevención de XSS y SQL Injection:**
   - En el Frontend, `escapeHtml` en `uiComponents.js` escapa todos los datos dinámicos antes de insertarlos en plantillas HTML.
   - En el Backend, todas las consultas utilizan Spring Data JPA con parámetros enlazados (`@Param`), eliminando concatenación de SQL.
4. **Gestión de Secretos y Dependencias:**
   - `.gitignore` excluye `.env` y `.env.*` conservando únicamente `.env.example` con marcadores `<REPLACE_WITH_...>`.
   - `ProductionSecurityValidator.java` aborta el arranque en perfil `prod` si `SECURITY_JWT_SECRET`, `SPRING_DATASOURCE_PASSWORD` o `SPRING_DATA_MONGODB_URI` están vacíos, usan valores de ejemplo o tienen menos de 32 bytes de longitud.
   - Se eliminó la dependencia innecesaria `com.mysql:mysql-connector-j:8.0.33` de `pom.xml`.
