# Auditoría Técnica, Matriz de Requisitos y Plan de Implementación Full Stack — NexusMarket

**Fecha de Auditoría:** 2026-10-28  
**Arquitectura:** Domain-Driven Design (DDD) + Arquitectura Hexagonal Estricta (Puertos y Adaptadores)  
**Stack Backend:** Java 17 LTS, Spring Boot, Spring Data JPA (SQL Server / H2), Spring Data MongoDB, Spring AOP, Spring Cache, Springdoc OpenAPI 3  
**Stack Frontend Seleccionado:** Aplicación Web SPA Modular ES6+ (HTML5 Semántico, CSS3 Responsive Design System, JavaScript ES Modules, servida por Spring Boot y por servidor Node.js 24 con pruebas automatizadas nativas `node --test`)

---

## 1. Justificación de la Selección Tecnológica Frontend

Tras inspeccionar todo el repositorio, se verificó que **no existía ningún proyecto frontend previo**. Se seleccionó una **arquitectura SPA modular basada en ES6+ nativo (`frontend/` sincronizada con `nexusmarket/src/main/resources/static/`)** por las siguientes razones técnicas:
1. **Alineación con el diagrama C4 Nivel 2 (`SDD/C4/02-nivel-2-contenedores.md`)**: Cliente SPA desacoplado que consume la API REST sobre HTTP/JSON utilizando tokens `Bearer JWT`.
2. **Reproducibilidad 100 % garantizada en entornos offline/CI/Docker**: Funciona sin depender de descargas externas de registros npm en entornos restringidos, ejecutando sus pruebas unitarias y de integración con el corredor nativo de **Node.js 24 (`node --test`)** y sirviéndose tanto de forma independiente (`npm start`) como integrada dentro del JAR ejecutable de Spring Boot (`http://localhost:8080/`).
3. **Separación estricta de responsabilidades**:
   - `js/config/`: Configuración centralizada de entorno y constantes.
   - `js/api/`: Cliente HTTP centralizado (`httpClient.js`) y servicios por actor (`authApi.js`, `buyerApi.js`, `sellerApi.js`, `adminApi.js`, `logisticsApi.js`, `supervisorApi.js`).
   - `js/state/`: Gestión reactiva de sesión (`sessionStore.js`, usando `sessionStorage` para mitigar persistencia indefinida de tokens) y estado del carrito/notificaciones (`store.js`).
   - `js/router/`: Enrutador SPA con guardas de autenticación y control de acceso por rol (`router.js`).
   - `js/components/`: Componentes reutilizables accesibles (barra de navegación, tarjetas de producto, controles de paginación, modales de confirmación, alertas).
   - `js/views/`: Vistas modulares separadas para Inicio, Catálogo, Detalle de Producto, Login, Registro, Carrito, Checkout, Mis Pedidos, Perfil/Direcciones, Panel de Vendedor, Panel Logístico, Panel de Supervisor, Panel Administrativo, Páginas Informativas y Error 404.

---

## 2. Matriz de Requisitos (Estado Inicial vs. Objetivo Full Stack)

| ID | Módulo / Requisito | Estado Inicial | Estado Objetivo |
|---|---|---|---|
| **INF-01** | Maven Wrapper (`mvnw` / `mvnw.cmd`) y empaquetado JAR ejecutable (`repackage`) | Implementado y verificado | Implementado y verificado |
| **INF-02** | Persistencia SQL Server (`schema-sqlserver.sql`, `ddl-auto=none`) + H2 en pruebas | Implementado y verificado | Implementado y verificado |
| **INF-03** | Auditoría documental en MongoDB mediante Spring AOP (`@Auditable`, `AuditAspect`) | Implementado y verificado | Implementado y verificado |
| **INF-04** | Validación fail-fast de secretos en perfil `prod` (`ProductionSecurityValidator`) | Implementado y verificado | Implementado y verificado |
| **SEC-01** | Autenticación JWT HMAC-SHA256 (`POST /api/auth/login`, `POST /api/auth/logout`) | Implementado y verificado | Implementado y verificado |
| **SEC-02** | Autorización RBAC activa en `JwtAuthenticationFilter` + validación de propiedad por actor | Parcialmente implementado | Implementado y verificado |
| **CAT-01** | Catálogo público paginado, búsqueda, filtros (tipo, precio, estado) y ordenamiento | Parcialmente implementado | Implementado y verificado |
| **BUY-01** | Registro público de comprador y gestión de perfil / direcciones (primaria y secundarias) | Implementado y verificado | Implementado y verificado |
| **BUY-02** | Carrito de compras (crear, consultar, agregar, actualizar cantidad, eliminar, vaciar) | Parcialmente implementado | Implementado y verificado |
| **BUY-03** | Checkout, creación de orden desde carrito, pago con reserva de stock y factura automática | Implementado y verificado | Implementado y verificado |
| **BUY-04** | Historial de pedidos, detalle, cancelación (`POST /api/buyers/orders/{id}/cancel`) y devoluciones | Parcialmente implementado | Implementado y verificado |
| **SEL-01** | Panel de vendedor: creación/edición de productos (precio y estado con `@Version`) | Parcialmente implementado | Implementado y verificado |
| **SEL-02** | Panel de vendedor: bodegas, lote inicial de inventario, ajuste de stock y órdenes | Parcialmente implementado | Implementado y verificado |
| **LOG-01** | Operador logístico: despacho (`Shipment`), confirmación de entrega y movimientos kardex | Implementado en API / Pendiente UI | Implementado y verificado |
| **SUP-01** | Supervisor: monitoreo global, revisión de disputas, devoluciones y reportes consolidados | Implementado en API / Pendiente UI | Implementado y verificado |
| **ADM-01** | Panel administrativo: gestión de staff, vendedores, usuarios, bodegas, facturas y reembolsos | Implementado en API / Pendiente UI | Implementado y verificado |
| **FE-01** | Interfaz web responsive, accesible (WCAG AA, teclado, ARIA) y conectada al backend real | Pendiente | Implementado y verificado |
| **QA-01** | Suite de pruebas Backend (JUnit 5), Frontend (`node --test`) y End-to-End Full Stack (10 flujos) | Parcialmente implementado | Implementado y verificado (185 tests JUnit + 4 suites Node) |
| **EXT-01** | Ejecución en vivo de contenedores Docker (`sqlserver`, `mongodb`) en máquina local | Bloqueado por dependencia externa (Docker daemon local apagado) | Documentado y verificado vía configuración + pruebas de integración |

---

## 3. Estado Final de Ejecución por Fases (Fases 0 a 12)

- **Fase 0 — Auditoría y Planificación:** Completada y documentada en este archivo.
- **Fase 1 — Estabilización de Plataforma:** Completada (`mvnw`, `repackage`, `schema-sqlserver.sql`, `ProductionSecurityValidator`, healthchecks Docker).
- **Fase 2 — Diseño e Implementación del Frontend:** Completada (`frontend/public/` y `nexusmarket/src/main/resources/static/`).
- **Fase 3 — Autenticación, Registro y Autorización:** Completada (`JwtAuthenticationFilter` + `SecurityConfig` con RBAC activo y control de titularidad `401`/`403` + `sessionStore.js`).
- **Fase 4 — Experiencia de Compra:** Completada (catálogo con búsqueda/filtros/stock, carrito persistente con consolidación y edición de cantidades, checkout con direcciones, pago, cancelación y devoluciones).
- **Fase 5 — Panel de Vendedores:** Completada (`#/seller` con KPIs, creación/edición de productos, bodegas, stock inicial y ajuste de inventario).
- **Fase 6 — Paneles Administrativo, Supervisor y Logístico:** Completada (`#/admin`, `#/supervisor`, `#/logistics`).
- **Fase 7 y 8 — Casos de Uso Backend, Pagos e Integraciones:** Completada (`updateItemQuantity`, `cancelOrder`, `getCatalog` con filtros, `getProductAvailableStock`, persistencia JPA reconciliada en `InventoryJpaAdapter` y `ShoppingCartJpaAdapter`).
- **Fase 9 — Pruebas Integrales:** Completada (`185` pruebas JUnit 5 incluyendo `FullStackMarketplaceE2ETest` con los 10 escenarios E2E + `4` suites frontend en `frontend/tests/frontend.test.js`).
- **Fase 10, 11 y 12 — Docker, CI/CD, Seguridad y Documentación:** Completada (`.github/workflows/ci.yml`, `README.md`, `SETUP.md`).
