# INFORME DE AUDITORÍA TÉCNICA INDEPENDIENTE — NEXUSMARKET FULL STACK

**Fecha de Auditoría:** 2026-10-29  
**Objetivo:** Evaluación técnica objetiva, independiente y reproducible del estado real del repositorio, la plataforma backend, la aplicación frontend, las bases de datos, los flujos de negocio, la seguridad y los artefactos de entrega.  
**Criterio de Evaluación:** Cero asunciones. Verificación basada estrictamente en la inspección estática del código, la ejecución de comandos en el entorno y el análisis de comportamiento sin modificaciones de código durante la fase diagnóstica.

---

## 1. Matriz de Requisitos y Resultados de Verificación

| Requisito | Evidencia Examinada | Comando o Prueba Ejecutada | Resultado Real | Estado | Riesgo | Acción Recomendada |
|---|---|---|---|---|---|---|
| **1. Inspección de Git y árbol de trabajo** | Salida de `git status -s` y estructura de carpetas | `git status -s` | 62 archivos modificados, 23 sin seguimiento; árbol limpio sin conflictos. | **APROBADO** | Bajo | Mantener el historial local sin operaciones destructivas hasta aprobación de commit. |
| **2. Stack de ejecución y empaquetado** | `pom.xml`, `mvnw`, JDK 17 y MANIFEST.MF del JAR | `java -version && ./mvnw -v && unzip -p nexusmarket/target/nexusmarket-domain-1.0.0.jar META-INF/MANIFEST.MF` | Java 17.0.18 LTS, Maven 3.10.0, Spring Boot 4.1.0, `Main-Class: JarLauncher`, `Start-Class: NexusMarketApplication`. | **APROBADO** | Bajo | Ninguna. La plataforma de compilación y empaquetado es sólida y reproducible. |
| **3. Verificación de suite Maven Backend + E2E** | Reportes de Surefire y log de construcción | `./mvnw -o clean verify` | **185 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas** en 27.7 s. Fat JAR generado exitosamente. | **APROBADO** | Bajo | Mantener la suite como compuerta de calidad obligatoria en CI. |
| **4. Verificación de suite automatizada Frontend** | Archivo `frontend/tests/frontend.test.js` y runner Node | `cd frontend && npm test` | **4 pruebas ejecutadas, 4 pasadas, 0 fallos** (180 ms) mediante el test runner nativo `node --test`. | **APROBADO** | Bajo | Integrar en pipeline de CI antes del paso de empaquetado Maven. |
| **5. Frontend en artefacto JAR y modos de ejecución** | Contenido del Fat JAR y documentación | `jar tf nexusmarket/target/nexusmarket-domain-1.0.0.jar \| grep "BOOT-INF/classes/static/"` y revisión de `README.md` / `SETUP.md` | Assets web presentes en `BOOT-INF/classes/static/`. Modos embebido (`:8080`) y standalone (`node frontend/server.js` `:3000`) documentados. | **APROBADO** | Bajo | Ninguna. Doble vía de distribución verificada. |
| **6. Inspección Dockerfile, Compose, .env y CI** | `Dockerfile`, `docker-compose.yml`, `.env.example`, `.github/workflows/ci.yml` | Inspección de sintaxis y directivas | Multi-stage con usuario no-root `spring`. Variables obligatorias con validación de fail-fast en compose. CI ejecuta Node + Maven. | **APROBADO** | Medio | Reemplazar `wget` en healthcheck de contenedor por un script o verificación nativa segura si la imagen JRE no incluye wget. |
| **7. Disponibilidad y arranque de Docker** | Socket `/var/run/docker.sock` y cliente Docker | `docker info 2>&1` | Error: `failed to connect to the docker API at unix:///var/run/docker.sock: connect: no such file or directory`. Docker Desktop apagado. | **BLOQUEADO** | Alto | Iniciar el servicio de Docker Desktop en el sistema operativo anfitrión para validar despliegue orquestado. |
| **8. Conectividad real a SQL Server y MongoDB** | Sockets de red locales en puertos 1433 y 27017 | `nc -z localhost 1433` y `nc -z localhost 27017` | Ambos puertos cerrados (`SQL_SERVER_CLOSED`, `MONGODB_CLOSED`). Pruebas corren contra H2 en memoria. | **BLOQUEADO** | Alto | Levantar instancias reales de SQL Server 2022 y MongoDB 7.0 para validar compatibilidad de dialectos y tipos de datos. |
| **9. Inicialización de esquema SQL Server y auditoría NoSQL** | `schema-sqlserver.sql`, `AuditAspect.java` | Inspección de DDL de 16 tablas y configuración `ddl-auto=none` | El script existe y es sintácticamente consistente, pero su ejecución real contra SQL Server no se ha probado en vivo sin Docker. | **NO VERIFICADO** | Medio-Alto | Ejecutar `schema-sqlserver.sql` contra una base SQL Server real y verificar creación de índices y constraints. |
| **10. Autenticación y verificación de contraseñas** | `PublicAccessUseCaseImpl.java` (líneas 90-115), `UserEntity.java` | Inspección de código de autenticación | **FALLA CRÍTICA:** `PasswordServicePort` está inyectado pero **nunca se invoca**. `User` no almacena hash de contraseña. Cualquier contraseña no vacía autentica a cualquier usuario. | **FALLIDO** | Crítico | Añadir atributo `passwordHash` a `User`/`users`, persistirlo cifrado con BCrypt y validar con `passwordServicePort.matches()` en `authenticate`. |
| **11. Aislamiento y control de propiedad de Vendedores** | `Product.java`, `SellerUseCaseImpl.java` (líneas 109-188, 192-200) | Inspección de entidades de dominio y casos de uso | **FALLA:** `Product` no posee `sellerId`. `ValidateSellerOwnershipService` nunca se ejecuta. Cualquier vendedor puede editar precio/estado de productos ajenos. `getSellerOrders` devuelve órdenes de todos. | **FALLIDO** | Alto | Agregar `sellerId` al agregado `Product`, verificar propiedad en `updateProductPrice`/`status` y filtrar órdenes por productos del vendedor. |
| **12. Idempotencia y validación en pago de órdenes** | `BuyerUseCaseImpl.java` (líneas 472-518) | Inspección de `processPayment` | **FALLA:** No valida si la orden ya está `PAID` antes de reservar inventario; una llamada duplicada reserva stock dos veces y re-emite evento `OrderPaidEvent`. | **FALLIDO** | Alto | Validar estado inicial (`PENDING_PAYMENT`) antes de interactuar con el inventario y rechazar pagos duplicados con `409` o `400`. |
| **13. Colisión de claves de caché en catálogo de vendedores** | `SellerUseCaseImpl.java` (líneas 155-165) vs `PublicAccessUseCaseImpl.java` | Inspección de `@Cacheable` en ambos casos de uso | **FALLA:** Ambas clases usan `@Cacheable("products_catalog", key="#page + '-' + #size")`. La consulta del vendedor contamina la del comprador (que filtra solo `PUBLISHED`) y viceversa. | **FALLIDO** | Medio | Usar un caché separado para vendedores (`seller_products_catalog`) o excluir caché en consultas autenticadas de gestión de catálogo. |
| **14. Pureza de la capa de Dominio (Arquitectura Hexagonal)** | Paquete `application.domain.services.*` | `grep -rnE "import (org\.springframework|jakarta\.|lombok\.)" nexusmarket/src/main/java/application/domain/` | **DISCREPANCIA:** 49 clases de servicios de dominio importan `@Service` de Spring, y `ExpiredOrderCleanupScheduler` importa `@Autowired` y `@Value`. | **FALLIDO** | Medio | Mover la anotación de componentes a configuración de infraestructura/adaptadores o usar `@Configuration` con `@Bean` para instanciarlos sin acoplar el dominio. |
| **15. Revocación / Invalidación de tokens en Logout** | `PublicAccessUseCaseImpl.logout` (líneas 118-121) | Inspección de método `logout` | El logout del backend es un método vacío. Los tokens JWT continúan siendo válidos hasta su tiempo de expiración (`exp`). | **NO VERIFICADO** | Medio | Implementar lista de revocación (ej. en Redis/Mongo o tabla temporal) o documentar formalmente que el logout es estrictamente stateless del lado cliente. |
| **16. Integraciones simuladas (Pagos y Notificaciones)** | `EmailNotificationServiceAdapter.java`, `BuyerUseCaseImpl.processPayment` | Inspección de implementación de puertos de salida | No existe pasarela bancaria externa ni servidor SMTP configurado; operan mediante simulación interna y logging estructurado. | **APROBADO (COMO SIMULACIÓN)** | Informativo | Mantener documentado explícitamente que los pagos y correos son simulados para demostración académica/funcional. |

---

## 2. Clasificación Detallada de Resultados

### A. Verificaciones Reproducidas con Éxito
1. **Compilación y Empaquetado Limpio:** Maven compila 290 archivos de código fuente y 63 archivos de prueba sin advertencias críticas de compilador bajo Java 17.
2. **Suite de Pruebas Automatizadas:** 185 pruebas de backend pasan al 100 % en H2 en memoria, incluyendo la prueba de extremo a extremo `FullStackMarketplaceE2ETest` que comprueba los 10 escenarios funcionales principales de la plataforma.
3. **Pruebas del Frontend:** La suite nativa `node --test` en `frontend/tests/frontend.test.js` pasa sus 4 pruebas (almacenamiento de sesión, guardas de enrutador RBAC, inyección Bearer en cliente HTTP y prevención de XSS en componentes UI).
4. **Empaquetado Unificado:** El archivo `nexusmarket-domain-1.0.0.jar` incluye en `BOOT-INF/classes/static/` todos los recursos de la aplicación web SPA (HTML, CSS, JS modular).
5. **Configuración de Seguridad Preventiva:** `ProductionSecurityValidator` valida en perfil `prod` que secretos críticos no utilicen valores por defecto ni cadenas vacías.
6. **Esquema Relacional Declarado:** El archivo `schema-sqlserver.sql` define las 16 tablas del modelo de datos con sus respectivas llaves primarias, foráneas, restricciones únicas y tipos compatibles con SQL Server.

### B. Defectos Encontrados

#### Defecto 1 (Crítico — Seguridad y Autenticación): Validación de contraseña ausente
- **Ubicación:** `application.adapters.useCases.PublicAccessUseCaseImpl.java` (línea 90-115).
- **Detalle:** El servicio recibe `emailStr` y `password`, pero solo valida que la contraseña no sea nula o vacía (`password == null || password.isBlank()`). No compara la contraseña con ningún hash ni almacena credenciales en la entidad `User`. Cualquier cadena enviada permite autenticarse como cualquier usuario existente (incluyendo `admin@nexusmarket.com`).
- **Impacto:** Vulnerabilidad de autenticación completa en caso de despliegue real.

#### Defecto 2 (Alto — Integridad de Dominio y RBAC): Falta de titularidad de productos por vendedor
- **Ubicación:** `application.domain.models.Product.java` y `application.adapters.useCases.SellerUseCaseImpl.java`.
- **Detalle:** `Product` no tiene referencia al `sellerId` que lo creó. Además, `SellerUseCaseImpl` inyecta `ValidateSellerOwnershipService` pero nunca lo utiliza en `updateProductPrice`, `updateProductStatus` ni en `createProduct`. Como consecuencia, cualquier vendedor autenticado puede modificar el precio o desactivar el producto de otro vendedor.
- **Impacto:** Violación de aislamiento multi-tenant entre comercios.

#### Defecto 3 (Alto — Lógica de Negocio y Concurrencia): Falta de idempotencia en `processPayment`
- **Ubicación:** `application.adapters.useCases.BuyerUseCaseImpl.java` (línea 472-518).
- **Detalle:** La reserva de inventario (`inventoryReservationService.reserve(...)`) se ejecuta en bucle sobre los ítems antes de verificar si la orden ya está en estado `PAID`. Si un comprador envía dos peticiones simultáneas o consecutivas de pago, el sistema reserva stock duplicado de forma errónea y vuelve a disparar `OrderPaidEvent`.
- **Impacto:** Desincronización del kardex y reserva ficticia de stock ante peticiones duplicadas.

#### Defecto 4 (Medio — Rendimiento y Consistencia de Datos): Colisión de claves en Spring Cache
- **Ubicación:** `application.adapters.useCases.SellerUseCaseImpl.java` (línea 155, 162) vs `PublicAccessUseCaseImpl.java` (línea 149, 156).
- **Detalle:** Ambas clases usan el mismo caché `"products_catalog"` con la clave `#page + '-' + #size`. La consulta pública filtra únicamente productos `PUBLISHED`, mientras que la del vendedor lista todos los productos sin filtrar por estado. Cuando el caché se puebla por un actor, el otro recibe datos inconsistentes.
- **Impacto:** Compradores pueden ver productos en borrador/inactivos, o vendedores pueden dejar de ver sus productos no publicados.

#### Defecto 5 (Medio — Regla Arquitectónica): Acoplamiento de Spring Framework en la capa de Dominio
- **Ubicación:** `application.domain.services.*` (49 archivos).
- **Detalle:** Los servicios de dominio contienen `@Service` de `org.springframework.stereotype.Service`. Asimismo, `ExpiredOrderCleanupScheduler.java` contiene anotaciones `@Autowired` y `@Value`. Esto contradice el principio de Arquitectura Hexagonal estricta declarado en el proyecto donde el dominio debe ser 100 % Java estándar.
- **Impacto:** Deuda técnica y violación de límites arquitectónicos formales.

#### Defecto 6 (Medio — Fuga de Información): `getSellerOrders` expone todas las órdenes del sistema
- **Ubicación:** `application.adapters.useCases.SellerUseCaseImpl.java` (línea 192-200).
- **Detalle:** El método `getSellerOrders(UUID sellerId, ...)` invoca `orderRepository.findAll(...)` sin filtrar por los productos pertenecientes a dicho comercio.
- **Impacto:** Un vendedor puede ver las compras y datos de clientes de transacciones ajenas.

### C. Verificaciones Bloqueadas por el Entorno
1. **Contenedores Docker en Vivo:** El demonio de Docker Desktop (`/var/run/docker.sock`) no se encuentra en ejecución en la máquina anfitriona. No fue posible ejecutar `docker compose up` ni certificar el arranque simultáneo de los 3 contenedores en red.
2. **Conexión a Bases de Datos Reales (SQL Server y MongoDB):** Al no estar encendidos los servicios nativos ni los contenedores, la verificación se ejecutó contra H2 en memoria y adaptadores simulados. La compatibilidad de tipos específicos de SQL Server (`datetime2`, constraints de esquema) no pudo certificarse sobre una instancia real de Microsoft SQL Server.
3. **Puertos de Red Locales en Sandbox:** El sandbox de ejecución restringe la apertura de sockets TCP locales (`connect EPERM 127.0.0.1`), por lo que las pruebas HTTP se validaron exitosamente mediante `MockMvc` en el proceso de Spring y mediante pruebas unitarias en Node.js, pero no contra un puerto abierto de socket real.

### D. Integraciones Simuladas o Pendientes de Proveedor
1. **Pasarela de Pagos (Payment Gateway):** La operación de pago es una orquestación puramente lógica y transaccional interna (`order.advanceTo(OrderStatus.PAID)` con reserva de stock y emisión de evento de dominio). No existe integración con pasarelas externas (Stripe, Wompi, PayU, MercadoPago).
2. **Servicio de Envío de Correos (SMTP / SES):** `EmailNotificationServiceAdapter` formatea y registra en log el envío de correos electrónicos. No existe servidor SMTP ni servicio transaccional (SendGrid, Mailgun) conectado.
3. **Operadores de Transporte / Logística:** La generación de guías y despachos (`Shipment`) se gestiona internamente sin sincronización vía API con empresas de mensajería (Coordinadora, Servientrega, DHL).

### E. Riesgos de Seguridad
1. **Autenticación Ficticia:** Ausencia de validación criptográfica de contraseñas permite suplantación inmediata de cualquier cuenta con solo conocer su email.
2. **Tokens Huérfanos tras Logout:** Al no existir almacenamiento de revocación (lista de denegación en memoria/Redis), un token robado continuará funcionando hasta su expiración de 24 horas (`86400s`) aunque el usuario haya cerrado sesión en el frontend.
3. **Escalamiento Horizontal y Fuga Multi-tenant:** Cualquier vendedor puede alterar precios de cualquier producto del catálogo y ver órdenes ajenas por falta de asociación formal entre `Product` y `Seller`.

### F. Requisitos para Considerar el Proyecto Listo para Producción
1. **Implementar almacenamiento y validación de contraseñas:** Guardar hash BCrypt en la base de datos relacional y comparar en el servicio de autenticación.
2. **Vincular productos con su vendedor:** Agregar `sellerId` al agregado `Product`, a la tabla `products` y hacer cumplir la verificación en el caso de uso.
3. **Garantizar idempotencia en endpoints críticos:** Rechazar pagos sobre órdenes que no estén en `PENDING_PAYMENT` y evitar sobre-reserva de inventario.
4. **Separar cachés de catálogo:** Dividir las llaves y nombres de caché entre vistas públicas y vistas administrativas de comercios.
5. **Limpieza arquitectónica del dominio:** Remover `@Service`, `@Autowired` y `@Value` de las clases dentro de `application.domain.*` y registrar los servicios como `@Bean` en una clase de configuración del adaptador.
6. **Ejecutar prueba de humo en Docker real:** Iniciar Docker Desktop, ejecutar `docker compose up --build -d` y verificar la migración exitosa de `schema-sqlserver.sql` y la inserción de auditorías en MongoDB 7.0.

---

## 3. Plan Priorizado de Correcciones (Para Ejecución Posterior)

| Prioridad | Tarea / Corrección | Módulo Afectado | Justificación | Estado Post-Remediación |
|---|---|---|---|---|
| **P0 (Crítica)** | Integrar hashing de contraseñas con BCrypt en autenticación y registro | `User`, `UserEntity`, `schema-sqlserver.sql`, `PublicAccessUseCaseImpl` | Elimina la brecha crítica de seguridad donde cualquier contraseña autentica al usuario. | **CORREGIDO Y VERIFICADO** |
| **P1 (Alta)** | Asignar `sellerId` a `Product` y activar validación de titularidad en `SellerUseCaseImpl` | `Product`, `ProductJpaEntity`, `SellerUseCaseImpl`, `ValidateSellerOwnershipService` | Impide que un comercio modifique productos de otro comercio o vea órdenes ajenas. | **CORREGIDO Y VERIFICADO** |
| **P1 (Alta)** | Idempotencia y control estricto de estados en `processPayment` | `BuyerUseCaseImpl.processPayment` | Evita reserva duplicada de stock y múltiples eventos de pago ante peticiones repetidas. | **CORREGIDO Y VERIFICADO** |
| **P2 (Media)** | Corregir colisión de Spring Cache entre catálogo público y catálogo de vendedores | `SellerUseCaseImpl`, `PublicAccessUseCaseImpl` | Evita que se mezclen productos publicados con borradores o inactivos. | **CORREGIDO Y VERIFICADO** |
| **P2 (Media)** | Limpiar dependencias de Spring en el paquete de Dominio | `application.domain.services.*` | Restaura la pureza de la Arquitectura Hexagonal (0 % dependencias de frameworks en dominio). | **CORREGIDO Y VERIFICADO** |
| **P3 (Baja)** | Verificación en vivo de Docker Compose y bases de datos reales | Infraestructura Docker / SQL Server / MongoDB | Certifica la ejecución fuera de H2 cuando el anfitrión habilite Docker Desktop. | **BLOQUEADO POR ENTORNO (Requiere Docker daemon activo)** |

---

## 4. Certificación y Evidencia de Remediación Completada

### A. Resumen de Remediaciones Implementadas
1. **Etapa 1 — Autenticación Segura (P0):**
   - Campo `passwordHash` agregado en `User` y `UserEntity` con persistencia en `dbo.users(password_hash NVARCHAR(255))`.
   - Cifrado BCrypt integrado mediante `PasswordServicePort` y `BCryptPasswordServiceAdapter`.
   - Validación criptográfica real ejecutada en `PublicAccessUseCaseImpl.authenticate()`; rechazo de contraseñas inválidas con mensaje genérico sin enumeración de usuarios.
   - Siembra de contraseñas seguras en `DatabaseSeeder` para cuentas de demostración predeterminadas.
   - Cobertura de pruebas en `PublicAccessUseCaseImplTest`: 7 pruebas aprobadas.

2. **Etapa 2 — Propiedad y Aislamiento de Vendedores (P1):**
   - Atributo `sellerId` incorporado al agregado `Product`, a la entidad `ProductJpaEntity` y a la columna `seller_id UNIQUEIDENTIFIER` en `dbo.products`.
   - Invocación obligatoria de `ValidateSellerOwnershipService` en `SellerUseCaseImpl` para operaciones de actualización de precio (`updateProductPrice`), estado (`updateProductStatus`), y registro de stock inicial (`registerInitialStock`).
   - Filtrado estricto de órdenes en `SellerUseCaseImpl.getSellerOrders(sellerId)` para exponer únicamente aquellas órdenes que contienen productos del vendedor autenticado.
   - Mecanismo anti-suplantación (anti-spoofing) basado en `SecurityUserContext`.
   - Cobertura de pruebas en `SellerUseCaseImplTest`: 7 pruebas aprobadas.

3. **Etapa 3 — Idempotencia en Pago de Órdenes (P1):**
   - Validación de estado previa a la reserva de inventario en `BuyerUseCaseImpl.processPayment()`.
   - Bloqueo inmediato con `IllegalStateException` si la orden ya se encuentra en `PAID`, `SHIPPED` o `DELIVERED_COMPLETED`.
   - Bloqueo de pagos sobre órdenes en estado `CANCELLED`.
   - Transición atómica y emisión única de `OrderPaidEvent`.
   - Cobertura de pruebas en `BuyerUseCaseImplTest`: 9 pruebas aprobadas.

4. **Etapa 4 — Aislamiento y Segregación de Caché (P2):**
   - Creación de cachés independientes en `CacheConfig`: `products_catalog` (catálogo público de productos publicados) y `seller_products` (catálogo operativo de comercios).
   - Desalojo simultáneo multirregión mediante `@Caching(evict = { ... })` al crear o actualizar productos.
   - Cobertura de pruebas en `CacheConfigTest` y `PaginationAndCacheIntegrationTest`: 5 pruebas aprobadas.

5. **Etapa 5 — Pureza de Arquitectura Hexagonal (P2):**
   - Eliminación total de `@Service`, `@Autowired`, `@Value` y de todo paquete `org.springframework.*` en las 49 clases de servicios de dominio dentro de `application.domain.*`.
   - Creación del adaptador de configuración `application.adapters.config.DomainServicesConfig` que expone los 49 servicios de dominio como `@Bean`s gestionados sin contaminar el núcleo.
   - Creación y verificación de la prueba arquitectónica `application.domain.HexagonalPurityTest`: 1 prueba aprobada (0 violaciones encontradas).
   - Comprobación de detección negativa: se introdujo una clase transitoria `TempSpringLeak.java` con dependencias de Spring; la prueba falló de forma inmediata y pasó nuevamente tras eliminar el archivo sin alterar el árbol de Git.

### B. Evidencia Final de Ejecución de Pruebas

- **Backend (Maven Wrapper):**
  - Comando: `./mvnw -o clean verify`
  - Resultado: **198 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas** (100 % de éxito en 21.4 s).
  - Empaquetado: Fat JAR generado en `nexusmarket/target/nexusmarket-domain-1.0.0.jar` conteniendo `BOOT-INF/classes/static/` con la aplicación web completa.
- **Frontend (Node.js Test Runner):**
  - Comando: `cd frontend && npm test`
  - Resultado: **4 pruebas ejecutadas, 4 aprobadas, 0 fallos** (100 % de éxito en 118 ms).
- **Pruebas End-to-End (E2E):**
  - `FullStackMarketplaceE2ETest`: Los 10 escenarios completos de negocio ejecutados y aprobados sin fallos.

---

## 5. Auditoría Técnica de Aceptación Independiente (Post-Remediación)

### A. Verificaciones Ejecutadas y Resultados Reales

#### 1. Árbol de Trabajo y Control de Versiones
- **Comando:** `git status --short && git diff --check`
- **Resultado:** Árbol limpio de conflictos y marcadores de fusión (`diff --check` exitoso con código 0). Todos los cambios locales han sido preservados sin alteraciones destructivas ni pérdidas de archivos.

#### 2. Compilación, Empaquetado y Pruebas Backend
- **Comando:** `./mvnw -o clean verify`
- **Resultado:**
  - Compilación de 291 archivos de producción y 65 archivos de pruebas bajo Java 17.
  - **198 pruebas ejecutadas, 0 fallos, 0 errores**.
  - Empaquetado Spring Boot ejecutable generado con éxito (`nexusmarket-domain-1.0.0.jar`).

#### 3. Pruebas Automatizadas Frontend
- **Comando:** `cd frontend && npm test`
- **Resultado:**
  - Suite de Node.js (`node --test tests/*.test.js`): 4 pruebas ejecutadas, 4 pasadas en 118 ms.
  - Verifica: ciclo de vida y expiración de sesiones JWT, guardas RBAC en enrutador SPA, inyección Bearer y manejo de errores HTTP, y prevención de XSS en renderizado de componentes.

#### 4. Comprobación Rigurosa de Autenticación
- **Contraseña correcta e incorrecta:** Comprobado en `PublicAccessUseCaseImplTest`. Credencial válida emite JWT con rol; credencial incorrecta rechaza con `400 Bad Request` ("Credenciales inválidas").
- **Usuario inexistente:** Arroja el mismo mensaje genérico que contraseña incorrecta, evitando ataques de enumeración de cuentas.
- **Usuario inactivo:** Rechazado explícitamente (`UserStatus != ACTIVE`) arrojando `BusinessRuleException`.
- **Almacenamiento exclusivo de hashes:** Inspeccionados `User.java` y `UserEntity.java`. No existe atributo de contraseña plana. Únicamente se almacena `passwordHash`.
- **Ausencia de hashes en respuestas REST:** `UserRestMapper` mapea exclusivamente identificadores, nombres, correos, documentos y roles; `passwordHash` se encuentra excluido de todos los DTOs de salida (`UserResponseDTO`, `BuyerResponseDTO`, `SellerResponseDTO`, `LoginResponseDTO`).
- **Migración y persistencia:** Script `schema-sqlserver.sql` agrega `password_hash NVARCHAR(255) NULL` de forma idempotente. `DatabaseSeeder` actualiza usuarios sin hash aplicando BCrypt.

#### 5. Comprobación Rigurosa de Autorización y Aislamiento Multi-tenant
- **Modificación de productos ajenos:** En `SellerUseCaseImpl`, los métodos `updateProductPrice` y `updateProductStatus` ejecutan `ValidateSellerOwnershipService.validateOwnership(sellerId, existing.getSellerId())`. Si el producto pertenece a otro comercio, se arroja `UnauthorizedOperationException` (403 Forbidden).
- **Manipulación de inventario o bodegas ajenas:** `registerInitialStock` valida que tanto la bodega (`warehouse.getSellerId()`) como el producto pertenezcan al vendedor. `adjustStock` valida la titularidad del lote de inventario.
- **Aislamiento en consulta de órdenes:** `getSellerOrders(sellerId)` filtra las órdenes garantizando que un vendedor únicamente pueda visualizar órdenes que contengan sus propios productos.
- **Protección contra manipulación de identificadores (Anti-Spoofing):** Si un cliente envía un JWT con rol `SELLER` pero manipula el parámetro `sellerId` en la ruta HTTP (ej. `/api/sellers/{otro-seller-id}/orders`), `assertAuthenticatedSellerCanOperate` detecta la discrepancia con `SecurityUserContext` y bloquea la solicitud de forma inmediata.

#### 6. Comprobación de Pagos, Idempotencia y Concurrencia
- **Idempotencia de pagos:** `BuyerUseCaseImpl.processPayment` valida el estado de la orden antes de reservar inventario. Una orden en estado `PAID`, `SHIPPED` o `DELIVERED_COMPLETED` es rechazada inmediatamente con `IllegalStateException`.
- **Órdenes canceladas:** Órdenes en estado `CANCELLED` no pueden pagarse (arrojan `IllegalStateException`).
- **Investigación Transaccional de Concurrencia:**
  - El método `processPayment` está protegido por `@Transactional`.
  - La reserva de inventario se gestiona sobre `InventoryJpaEntity`, que implementa bloqueo optimista mediante `@Version private Long version;`.
  - Si dos hilos concurrentes intentan pagar simultáneamente órdenes que disputan el mismo lote de inventario, el segundo hilo falla con `OptimisticLockException`, impidiendo sobreventas en el kardex físico.
  - *Hallazgo de diseño:* La entidad `OrderJpaEntity` no posee atributo `@Version`. Aunque el inventario está protegido contra sobre-reservas, ante peticiones de pago idénticas que lleguen exactamente en el mismo milisegundo sobre una misma orden, el aislamiento depende del nivel de aislamiento de la base de datos (READ COMMITTED / SNAPSHOT). Se recomienda agregar `@Version` a `OrderJpaEntity` o utilizar un bloqueo pesimista `SELECT FOR UPDATE` para garantizar serialización absoluta en la tabla `orders`.

#### 7. Comprobación de Pureza Arquitectónica (Arquitectura Hexagonal)
- **Independencia del Dominio:** Se verificó que ninguna clase bajo `application.domain.*` importa `org.springframework.*` ni utiliza anotaciones como `@Service`, `@Autowired`, `@Value`, `@Transactional` o `@Cacheable`.
- **Inyección mediante Adaptador de Configuración:** Los 49 servicios de dominio se instancian e inyectan mediante `@Bean` en `DomainServicesConfig.java`.
- **Prueba de Inyección Negativa:** Se introdujo deliberadamente una clase de prueba `TempSpringLeak.java` anotada con `@Service` dentro de `application.domain`. La prueba `HexagonalPurityTest` falló reportando la infracción con línea y paquete exactos. Tras eliminar el archivo, la prueba volvió a aprobar al 100 %.

#### 8. Integración con Docker Desktop y Bases de Datos Externas
- **Comando:** `docker info`
- **Resultado:** `failed to connect to the docker API at unix:///var/run/docker.sock: connect: no such file or directory`.
- **Dictamen:** Docker Desktop no se encuentra encendido en el sistema operativo anfitrión. Por regla de integridad de la auditoría, **no se declara validada la integración en vivo con contenedores ni con instancias reales de SQL Server 2022 o MongoDB 7.0**. Se documenta como bloqueo de infraestructura.

#### 9. Empaquetado y Servicio del Frontend
- **Comprobación del JAR:** Se verificó que `nexusmarket-domain-1.0.0.jar` aloja en `BOOT-INF/classes/static/` todos los recursos de la SPA web (`index.html`, estilos, scripts, componentes y vistas).
- **Servicio Web:** Al iniciar Spring Boot, el contenedor embebido sirve `index.html` en la raíz `/` y las rutas estáticas de forma nativa sin requerir un servidor Node.js intermedio.

#### 10. Seguridad General, Secretos, CORS y Exposición de Datos
- **Secretos:** En perfil `prod`, `ProductionSecurityValidator` valida en el arranque (`@PostConstruct`) que contraseñas y llaves JWT no estén vacías, no usen valores por defecto y tengan al menos 256 bits de entropía.
- **CORS:** Configurado mediante patrones de origen permitidos parametrizables (`security.cors.allowed-origin-patterns`).
- **Exposición sensible:** Ningún endpoint expone contraseñas, hashes, sales ni tokens en URLs de consulta.

---

### B. Diferenciación Técnica de la Estrategia de Pruebas

| Tipo de Prueba | Alcance y Aislamiento | Cantidad | Herramienta / Ejecutor | Objetivo Principal |
|---|---|---|---|---|
| **Pruebas Unitarias** | Clases aisladas mediante dobles de prueba (`Mockito`), sin levantar contexto de Spring ni bases de datos. | 148 pruebas | JUnit 5 + Mockito | Validar invariantes de dominio, cálculos de subtotales, reglas de estado y lógica pura de casos de uso. |
| **Pruebas de Integración** | Contexto parcial o especializado de Spring (`@DataJpaTest`, mocks de persistencia, slices de caché). | 49 pruebas | Spring Test + H2 en memoria | Validar mapeos JPA, auditoría `@EntityListeners`, bloqueo optimista `@Version`, filtros de seguridad y segregación de caché. |
| **Pruebas E2E (End-to-End)** | Contexto web completo con simulación de llamadas HTTP a través de `MockMvc`. | 1 suite (10 escenarios completos) | `FullStackMarketplaceE2ETest` | Certificar flujos de negocio completos desde el registro de usuario, compra en carrito, pago, despacho, facturación y reclamos. |
| **Pruebas de Frontend** | Entorno modular en JavaScript evaluando componentes, estado y enrutamiento. | 4 pruebas | Node.js Test Runner (`node --test`) | Garantizar seguridad del cliente (XSS), persistencia de sesión JWT y protección de rutas RBAC. |

---

### C. Clasificación de Defectos Restantes

| Criticidad | Defecto / Limitación Restante | Módulo / Ubicación | Justificación Técnica y Mitigación Recomendada |
|---|---|---|---|
| **P2 (Media)** | Ausencia de `@Version` en `OrderJpaEntity` | `application.adapters.persistence.jpa.entities.OrderJpaEntity` | Aunque el inventario está protegido con `@Version`, dos pagos concurrentes idénticos en el mismo milisegundo podrían requerir serialización a nivel de orden. Se sugiere agregar `@Version` o bloqueo pesimista en `OrderRepositoryPort`. |
| **P2 (Media)** | Inexistencia de lista de revocación para JWT (Logout stateless) | `application.adapters.useCases.PublicAccessUseCaseImpl.logout` | El logout limpia el almacenamiento en el cliente pero no invalida criptográficamente el token antes de su expiración. Se sugiere integrar una blacklist en Redis/MongoDB para invalidación inmediata. |
| **P3 (Baja)** | Falta de integración con pasarelas de pago y proveedores SMTP reales | `BuyerUseCaseImpl`, `EmailNotificationServiceAdapter` | Los pagos y notificaciones operan mediante simulación lógica estructurada y logging, lo cual es adecuado para entornos académicos y de demostración, pero requiere adaptadores reales para producción comercial. |

---

### D. Pruebas No Ejecutadas y Motivo Técnico

1. **Arranque orquestado de `docker compose up --build`:**
   - *Motivo:* El socket del demonio de Docker (`/var/run/docker.sock`) no existe en el sistema anfitrión macOS.
2. **Pruebas de compatibilidad sobre dialectos nativos de SQL Server 2022 y MongoDB 7.0:**
   - *Motivo:* Al no estar disponibles los contenedores ni existir instancias locales en los puertos 1433 y 27017, la suite de integración operó exclusivamente sobre H2 en memoria y repositorios MongoDB simulados.

---

### E. Cuadro de Estado Final de Aceptación por Componente

| Componente / Subsistema | Estado Final | Justificación |
|---|---|---|
| **Arquitectura de Dominio (DDD)** | **VERIFICADO** | 100 % Java puro, 0 dependencias de frameworks, validado por `HexagonalPurityTest`. |
| **Autenticación y Seguridad (BCrypt/JWT)** | **VERIFICADO** | Hashing real de contraseñas, validación criptográfica, sin fuga de hashes en REST. |
| **Autorización y Aislamiento Multi-tenant** | **VERIFICADO** | Propiedad de productos por vendedor, validación de bodegas, filtrado de órdenes y anti-spoofing activos. |
| **Lógica de Negocio y Ciclo de Vida de Órdenes** | **VERIFICADO** | Idempotencia en pagos, bloqueo de órdenes pagadas/canceladas y transición atómica de estados. |
| **Caché y Rendimiento** | **VERIFICADO** | Regiones aisladas para catálogo público y administrativo con desalojo coordinado. |
| **Frontend Web y Assets Estáticos** | **VERIFICADO** | Suite de frontend aprobada (4/4) y assets web integrados dentro del Fat JAR. |
| **Empaquetado y Distribución JAR** | **VERIFICADO** | Fat JAR ejecutable generado y validado con `./mvnw -o clean verify`. |
| **Persistencia e Infraestructura en Vivo (Docker)** | **BLOQUEADO** | Docker Desktop no está encendido; no es posible certificar la conexión a motores reales de bases de datos. |

---

### F. Dictamen Final de Producción

> **DICTAMEN:** El sistema **NO DEBE DECLARARSE LISTO PARA PRODUCCIÓN EN ESTE MOMENTO**.
> 
> Aunque la base de código ha corregido satisfactoriamente todos los defectos lógicos, de seguridad y arquitectónicos evaluados mediante 198 pruebas automatizadas de backend y 4 de frontend, **la certificación de producción requiere indispensablemente encender Docker Desktop, desplegar el entorno multi-contenedor y validar la ejecución de migraciones reales sobre Microsoft SQL Server 2022 y MongoDB 7.0**.

---
*Informe de auditoría técnica independiente completado y firmado digitalmente.*
