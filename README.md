# NexusMarket - Plataforma Full Stack de Comercio Electrónico y Marketplace

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%2F4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Frontend](https://img.shields.io/badge/Frontend-Modular%20ES6%2B%20SPA-6366f1.svg)](./frontend/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20DDD-blue.svg)](./SDD/Software%20Architecture/Software%20Architecture.md)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](./LICENSE)

**NexusMarket** es una plataforma empresarial Full Stack de comercio electrónico y marketplace multi-actor construida bajo los principios de **Diseño Guiado por el Dominio (DDD)**, **Arquitectura Hexagonal Estricta (Puertos y Adaptadores)** y **Desarrollo Guiado por Especificaciones (SDD)**.

---

## 1. Descripción y Alcance Funcional Full Stack

NexusMarket gestiona el ciclo de vida completo de un marketplace multi-actor para productos físicos y digitales a través de su **API RESTful** y su **Aplicación Web SPA Modular** (`frontend/` e integrada en `src/main/resources/static/`):

- **Experiencia Pública y Catálogo Web (`#/` y `#/catalog`):**
  - Página de inicio con propuesta de valor del marketplace, accesos rápidos por categoría, productos destacados y accesos de demostración multi-rol en 1 clic.
  - Catálogo interactivo con **búsqueda en tiempo real** (`search`), **filtro por tipo** (`PHYSICAL` / `DIGITAL`), **rango de precios** (`minPrice` / `maxPrice`), **ordenamiento** (`sortBy`, `direction`) y **paginación** (`GET /api/auth/catalog`).
  - Vista de **Detalle de Producto (`#/product/{id}`)** con ilustraciones vectoriales dedicadas, especificaciones de variante (`SKU`), precio formateado en moneda local (`COP`) y **consulta de stock real por bodega** (`GET /api/auth/catalog/{productId}/stock`).
- **Autenticación, Registro y Control de Acceso Basado en Roles (RBAC):**
  - Inicio de sesión (`#/login`) con validación de credenciales contra BCrypt y emisión de JWT firmado con `HMAC-SHA256`.
  - Registro público de compradores (`#/register`) con dirección principal e identificación única.
  - Restauración automática de sesión en navegador (`sessionStorage`), decodificación y verificación de expiración (`exp`) del JWT, cierre de sesión limpio y protección de rutas por rol y titularidad (`401 Unauthorized` y `403 Forbidden` activos en `JwtAuthenticationFilter` y `SecurityConfig`).
- **Experiencia de Compra E-Commerce (`#/cart`, `#/checkout`, `#/orders`, `#/profile`):**
  - Carrito de compras persistente en base de datos relacional (`GET/POST /api/buyers/{buyerId}/cart`), consolidación automática de cantidades, actualización de cantidad (`PUT /api/buyers/carts/{cartId}/items/{productId}`) y eliminación de ítems.
  - Checkout guiado con selección de dirección principal o registro/selección de direcciones secundarias de entrega, selección de método de pago (`CARD_CREDIT`, `PSE_TRANSFER`, `NEXUS_WALLET`), creación de orden (`OrderPlacedEvent`) y procesamiento de pago (`OrderPaidEvent`) con reserva atómica de inventario.
  - Historial de órdenes paginado, vista de confirmación/recibo de orden (`#/order-confirmation/{id}`), **cancelación de órdenes pendientes** (`POST /api/buyers/orders/{orderId}/cancel`) y solicitud de devoluciones (`POST /api/buyers/orders/{orderId}/returns`).
- **Centro de Vendedores (`#/seller`):**
  - Dashboard con KPIs comerciales, publicación de nuevos productos (`PHYSICAL` y `DIGITAL`), actualización de precios con control optimista (`@Version`), cambio de estado (`DRAFT`, `PUBLISHED`, `INACTIVE`, `BLOCKED`), consulta de bodegas del comercio, registro de lotes iniciales de inventario y ajuste de stock con motivo de auditoría.
- **Paneles Operativos de Administración (`#/admin`), Supervisión (`#/supervisor`) y Logística (`#/logistics`):**
  - **Administrador:** Métricas ejecutivas de plataforma, alta de personal (`STAFF`), alta de vendedores con bodega inicial, bloqueo/activación de usuarios, creación de bodegas y emisión de facturas fiscales (`Invoice`).
  - **Supervisor:** Reporte consolidado de auditoría, aprobación/rechazo de solicitudes de devolución (`ReturnRequest`), emisión de reembolsos (`Refund`), moderación de productos/usuarios y registro de operaciones auditadas.
  - **Operador Logístico:** Tablero de órdenes pagadas listas para despacho, coordinación de envíos (`Shipment`) con transportadora y guía (`trackingNumber`), confirmación de entrega final (`DELIVERED`) con salida definitiva de kardex (`SALE_OUTBOUND`) y consulta de movimientos de inventario.
- **Capacidades Transversales Enterprise:**
  - **Concurrencia Optimista (`@Version`):** Control de colisiones concurrentes en `ProductJpaEntity` e `InventoryJpaEntity` con respuesta estándar `409 CONFLICT`.
  - **JPA Auditing Automático:** Trazabilidad `createdAt` y `updatedAt` mediante `AuditableJpaEntity`.
  - **Auditoría NoSQL Automatizada (Spring AOP + MongoDB):** Anotación `@Auditable` e interceptor `AuditAspect` que registra operaciones críticas en MongoDB sin acoplar los casos de uso.
  - **Eventos de Dominio Asíncronos y Scheduler TTL:** Publicación de `OrderPlacedEvent`, `OrderPaidEvent` y `OrderCancelledEvent`, generación automática de facturas tras el pago, notificaciones estructuradas (`EmailNotificationServiceAdapter`) y liberación automática de stock reservado para órdenes `PENDING_PAYMENT` expiradas (`ExpiredOrderScheduler`).
  - **Caché de Catálogo y Paginación Estandarizada:** Caché en memoria (`products_catalog`) con invalidación automática (`@CacheEvict`) y respuestas paginadas mediante `PageResponseDTO<T>`.
  - **Documentación Interactiva OpenAPI 3 / Swagger UI:** Esquema de seguridad JWT Bearer integrado en `/swagger-ui.html`.

---

## 2. Requisitos de Software

- **JDK:** Java 17 LTS (Eclipse Temurin recomendado).
- **Maven:** Maven Wrapper incluido (`./mvnw` en raíz y `./nexusmarket/mvnw`), compatible con Maven 3.9+.
- **Node.js (Opcional para servidor frontend standalone y suite de pruebas JS):** Node.js 18+ / 20+ / 24+ (el frontend también se sirve embebido desde Spring Boot en `http://localhost:8080/`).
- **Docker y Docker Compose:** Docker Engine 24+ y Docker Compose v2+ (para despliegue contenerizado con SQL Server 2022 y MongoDB 7.0).
- **Postman:** Opcional, para importar la colección incluida en [`postman/`](./postman/).

---

## 3. Arquitectura Full Stack y Estructura de Módulos

El repositorio mantiene la especificación arquitectónica en [`SDD/`](./SDD/), la aplicación web frontend modular en [`frontend/`](./frontend/), la colección de Postman en [`postman/`](./postman/) y el microservicio Maven en [`nexusmarket/`](./nexusmarket/):

```
nexusmarket/
├── frontend/                              # Aplicación Web SPA Modular ES6+ (WCAG AA)
│   ├── package.json                       # Scripts de ejecución y pruebas (npm start / npm test)
│   ├── server.js                          # Servidor estático Node.js con cabeceras de seguridad
│   ├── public/                            # Recursos web sincronizados con src/main/resources/static/
│   │   ├── index.html                     # Shell HTML5 semántico con navegación accesible
│   │   ├── css/styles.css                 # Design System moderno (tokens, dark/light, responsive)
│   │   └── js/
│   │       ├── config/env.js              # Detección dinámica de API_BASE_URL y catálogo demo
│   │       ├── state/sessionStore.js      # Gestión de sesión JWT, expiración (exp) y RBAC
│   │       ├── api/httpClient.js          # Cliente Fetch con inyección Bearer y manejo 401/403
│   │       ├── api/marketplaceApi.js      # SDK tipado para los 6 controladores REST del backend
│   │       ├── router/router.js           # Enrutador SPA con guardas de rol por ruta
│   │       ├── components/uiComponents.js # Ilustraciones SVG, modales, toasts, estados vacíos
│   │       └── views/                     # Vistas por dominio (Public, Buyer, Seller, Admin/Sup/Logistics)
│   └── tests/frontend.test.js             # Suite automatizada de pruebas frontend (node --test)
└── nexusmarket/src/main/java/application/
    ├── domain/                            # Núcleo de negocio puro (0% dependencias de Spring/JPA/Lombok)
    │   ├── enums/                         # Estados, roles, tipos de producto/bodega/severidad
    │   ├── events/                        # Eventos de dominio (OrderPlacedEvent, OrderPaidEvent, OrderCancelledEvent)
    │   ├── exceptions/                    # Excepciones de negocio y dominio
    │   ├── models/                        # Agregados y entidades (Order, Product, Inventory, User, Buyer, Seller, etc.)
    │   ├── ports/
    │   │   ├── in/                        # Puertos de entrada orientados a Actor (*Port.java)
    │   │   └── out/                       # Puertos de salida (*RepositoryPort.java, EventPublisherPort, NotificationPort)
    │   ├── services/                      # Servicios de dominio puros (inventario, facturación, logística, usuarios, órdenes)
    │   └── valueobjects/                  # Objetos de valor inmutables (Money, Email, Address, OrderItem, PageResult)
    ├── adapters/                          # Adaptadores primarios (driving) y secundarios (driven)
    │   ├── aspect/                        # @Auditable y AuditAspect (Spring AOP -> MongoDB)
    │   ├── config/                        # Configuración JPA Auditing, Cache, Async/Scheduling, AOP, OpenAPI 3 y ProductionSecurityValidator
    │   ├── events/                        # SpringEventPublisherAdapter y OrderEventListener (@Async)
    │   ├── notification/                  # EmailNotificationServiceAdapter (@Primary)
    │   ├── persistence/
    │   │   ├── jpa/                       # Entidades JPA (@Version, AuditableJpaEntity), Mappers, Repositorios y Adaptadores SQL
    │   │   └── mongodb/                   # AuditLogDocument, AuditLogMongoRepository y AuditLogMongoAdapter
    │   ├── rest/                          # Controladores REST, DTOs (incl. PageResponseDTO), Mappers y GlobalExceptionHandler
    │   ├── scheduling/                    # ExpiredOrderScheduler (liberación automática de stock por TTL)
    │   ├── security/                      # JwtProvider, JwtAuthenticationFilter, SecurityConfig, BCryptPasswordServiceAdapter
    │   └── useCases/                      # Implementaciones transaccionales (@Transactional) de los puertos de entrada
    └── infrastructure/
        └── seed/                          # DatabaseSeeder (inicialización idempotente de usuarios, bodegas, catálogo e inventario)
```

---

## 4. Roles, Permisos y Credenciales de Demostración

`DatabaseSeeder` inicializa de forma idempotente 5 usuarios de demostración y un catálogo inicial de 6 productos con stock en bodega:

| Rol (`UserRole`) | Correo de Demostración | Contraseña Demo | Rutas Web Autorizadas | Prefijo API Protegido (RBAC) |
|---|---|---|---|---|
| `BUYER` | `buyer@nexusmarket.com` | `NexusMarket2025!` | `#/cart`, `#/checkout`, `#/orders`, `#/profile` | `/api/buyers/**` (con validación de titularidad `buyerId`) |
| `SELLER` | `seller@nexusmarket.com` | `NexusMarket2025!` | `#/seller` | `/api/sellers/**` (con validación de titularidad `sellerId`) |
| `LOGISTICS_OPERATOR` | `logistics@nexusmarket.com` | `NexusMarket2025!` | `#/logistics` | `/api/logistics/**` |
| `SUPERVISOR` | `supervisor@nexusmarket.com` | `NexusMarket2025!` | `#/supervisor` | `/api/supervisors/**` |
| `ADMINISTRATOR` | `admin@nexusmarket.com` | `NexusMarket2025!` | `#/admin` + acceso transversal | `/api/admin/**` y acceso administrativo transversal |

---

## 5. Configuración de Variables de Entorno y Bases de Datos

Copia la plantilla [`.env.example`](./.env.example) a `.env` antes de levantar contenedores o configurar producción:

```bash
cp .env.example .env
```

| Variable | Descripción | Valor por Defecto (Desarrollo) |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP del microservicio y Web UI embebida | `8080` |
| `SPRING_DATASOURCE_URL` / `DB_URL` | URL JDBC (SQL Server oficial en Docker / H2 en test) | `jdbc:sqlserver://localhost:1433;databaseName=NexusMarket;encrypt=true;trustServerCertificate=true` |
| `SPRING_DATASOURCE_USERNAME` / `DB_USERNAME` | Usuario de base de datos relacional | `sa` |
| `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | Contraseña de base de datos relacional | Definida en `.env` |
| `SPRING_DATA_MONGODB_URI` | URI de conexión a MongoDB para auditoría | `mongodb://localhost:27017/nexusmarket_audit` |
| `SECURITY_JWT_SECRET` | Clave secreta HMAC-SHA256 (>= 32 caracteres) | Definida en `.env` |
| `SECURITY_JWT_EXPIRATION_SECONDS` | Tiempo de expiración del token JWT | `86400` |
| `NEXUSMARKET_ORDERS_TTL_MINUTES` | Minutos antes de cancelar órdenes `PENDING_PAYMENT` expiradas | `30` |

### Responsabilidad de cada Base de Datos
1. **Microsoft SQL Server 2022 (`nexusmarket-db`):** Almacena las 16 tablas transaccionales del negocio inicializadas de forma idempotente mediante [`db/schema-sqlserver.sql`](./nexusmarket/src/main/resources/db/schema-sqlserver.sql) con `ddl-auto=none`. En pruebas automatizadas se utiliza **H2 en memoria**.
2. **MongoDB 7.0 (`nexusmarket-mongo`):** Almacena la colección documental `audit_logs` generada automáticamente por `AuditAspect` (`@Auditable`) y `RegisterAuditLogService`.

---

## 6. Compilación, Pruebas Automatizadas (Backend, Frontend y E2E) y Empaquetado

Desde la raíz del repositorio:

```bash
# 1. Ejecutar toda la suite Backend + E2E (185 tests) y empaquetar el Fat JAR ejecutable
./mvnw clean verify

# 2. Ejecutar la suite Backend + E2E en modo offline (sin descargas de red)
./mvnw -o clean verify

# 3. Ejecutar la suite automatizada de pruebas del Frontend (Node Test Runner)
cd frontend && npm test
```

---

## 7. Ejecución del Sistema Full Stack (Local y Docker Compose)

### Opción A: Despliegue Completo con Docker Compose
```bash
# 1. Levantar SQL Server, MongoDB y NexusMarket (Backend + Frontend Web UI en puerto 8080)
docker compose up --build -d

# 2. Abrir la aplicación web en el navegador
#    Web Marketplace UI: http://localhost:8080/
#    Swagger UI OpenAPI: http://localhost:8080/swagger-ui.html
#    Healthcheck:        http://localhost:8080/actuator/health

# 3. Verificar estado de contenedores y logs
docker compose ps
docker compose logs -f nexusmarket-app

# 4. Detener los servicios conservando volúmenes
docker compose down
```

### Opción B: Ejecución Local (Backend + Frontend Embebido o Standalone)
```bash
# 1. Levantar bases de datos en Docker
docker compose up -d nexusmarket-db nexusmarket-mongo

# 2. Iniciar el servidor Spring Boot (sirve API REST + Frontend en http://localhost:8080/)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. (Opcional) Iniciar servidor de desarrollo Frontend independiente en http://localhost:3000
cd frontend && npm start
```

---

## 8. Flujo Completo de Compra E-Commerce y Endpoints Principales

### Recorrido de Compra End-to-End en la Interfaz Web (`http://localhost:8080/`)
1. **Explorar Catálogo (`#/catalog`):** Filtra productos por nombre, categoría (`PHYSICAL` / `DIGITAL`), rango de precios u ordenamiento.
2. **Detalle de Producto (`#/product/{id}`):** Revisa el stock disponible en tiempo real por bodega y haz clic en **Agregar al carrito**.
3. **Autenticación (`#/login` o `#/register`):** Inicia sesión como comprador (`buyer@nexusmarket.com`) o registra una cuenta nueva.
4. **Carrito (`#/cart`):** Ajusta cantidades (`+` / `-`), elimina ítems o continúa al checkout.
5. **Checkout (`#/checkout`):** Selecciona la dirección principal o registra una dirección secundaria de entrega, elige el medio de pago y confirma el pedido.
6. **Confirmación y Post-Compra (`#/order-confirmation/{id}` y `#/orders`):** Visualiza el recibo de compra, cancela órdenes pendientes o solicita devoluciones sobre órdenes pagadas/entregadas.
7. **Despacho y Entrega (`#/logistics`):** Inicia sesión con `logistics@nexusmarket.com` para despachar la orden pagada y confirmar su entrega final.

### Endpoints Principales por Módulo
| Módulo | Método y Ruta | Descripción |
|---|---|---|
| **Auth & Catálogo** | `POST /api/auth/register` | Registro público de comprador |
| | `POST /api/auth/login` | Autenticación y emisión de JWT Bearer |
| | `GET /api/auth/catalog` | Catálogo público con búsqueda (`search`), filtros (`type`, `minPrice`, `maxPrice`), ordenamiento y paginación |
| | `GET /api/auth/catalog/{productId}/stock` | Consulta pública de disponibilidad de stock por producto |
| **Compradores** | `GET / POST /api/buyers/{buyerId}/cart` | Obtener o crear carrito de compras |
| | `POST /api/buyers/carts/{cartId}/items` | Agregar producto publicado al carrito |
| | `PUT /api/buyers/carts/{cartId}/items/{productId}` | Actualizar cantidad de un ítem del carrito |
| | `DELETE /api/buyers/carts/{cartId}/items/{productId}` | Eliminar producto del carrito |
| | `POST /api/buyers/orders` | Crear orden desde el carrito (`OrderPlacedEvent`) |
| | `POST /api/buyers/orders/{orderId}/pay` | Pagar orden, reservar stock y emitir `OrderPaidEvent` |
| | `POST /api/buyers/orders/{orderId}/cancel` | Cancelar orden pendiente (`OrderCancelledEvent`) |
| | `POST /api/buyers/orders/{orderId}/returns` | Solicitar devolución de orden pagada/entregada |
| **Vendedores** | `POST /api/sellers/products` | Crear producto (invalida caché `products_catalog`) |
| | `PATCH /api/sellers/products/{productId}/price` | Actualizar precio con bloqueo optimista (`@Version`) |
| | `PATCH /api/sellers/products/{productId}/status` | Cambiar estado de publicación del producto |
| | `GET /api/sellers/{sellerId}/warehouses` | Listar bodegas asociadas al vendedor |
| | `POST /api/sellers/{sellerId}/inventory/initial` | Registrar lote inicial de inventario |
| | `PATCH /api/sellers/{sellerId}/inventory/{inventoryId}/adjust` | Ajustar stock disponible con motivo |
| **Logística** | `POST /api/logistics/shipments/dispatch` | Coordinar despacho de orden pagada |
| | `PATCH /api/logistics/shipments/{shipmentId}/deliver` | Confirmar entrega final de envío (`SALE_OUTBOUND`) |
| **Supervisores** | `PATCH /api/supervisors/returns/{returnRequestId}/approve` | Aprobar solicitud de devolución |
| | `POST /api/supervisors/refunds` | Emitir reembolso sobre devolución aprobada |
| | `GET /api/supervisors/reports/consolidated` | Consultar reporte consolidado de auditoría |
| **Administración** | `POST /api/admin/users/staff` | Registrar personal (`ADMINISTRATOR`, `SUPERVISOR`, `LOGISTICS_OPERATOR`) |
| | `POST /api/admin/users/sellers` | Registrar vendedor con bodega inicial |
| | `POST /api/admin/warehouses` | Crear bodega de marketplace o de vendedor |
| | `POST /api/admin/invoices` | Generar factura fiscal de una orden |

### Colección de Postman
Importa en Postman los archivos ubicados en [`postman/`](./postman/):
1. [`postman/NexusMarket.postman_environment.json`](./postman/NexusMarket.postman_environment.json)
2. [`postman/NexusMarket.postman_collection.json`](./postman/NexusMarket.postman_collection.json)

Las peticiones incluyen scripts automáticos en la pestaña **Tests** que almacenan el `jwtToken` y los UUIDs generados (`buyerId`, `sellerId`, `productId`, `warehouseId`, `cartId`, `orderId`, `shipmentId`) en las variables del entorno.

---

## 8. Solución de Problemas Comunes y Limitaciones

- **Conflicto de concurrencia (`409 Conflict`):** Si dos peticiones intentan modificar simultáneamente el mismo producto o lote de inventario, `@Version` rechaza la segunda escritura y `GlobalExceptionHandler` devuelve un `409 Conflict` indicando recargar el recurso.
- **Conexión a SQL Server al iniciar Docker:** El contenedor de SQL Server puede tardar entre 15 y 25 segundos en aceptar conexiones iniciales; `docker-compose.yml` incluye un `healthcheck` con `start_period: 20s` para asegurar que `nexusmarket-app` inicie cuando la base de datos esté lista.
- **Integraciones Externas (Pasarela de Pagos y Servidor SMTP):**
  - El procesamiento de pagos valida reglas de dominio, reserva de inventario y transición de estados internamente sin requerir credenciales de pasarelas bancarias externas.
  - El envío de correos electrónicos se realiza mediante `EmailNotificationServiceAdapter`, que estructura y registra el mensaje sin depender de un servidor SMTP externo.
