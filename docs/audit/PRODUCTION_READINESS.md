# EVALUACIÓN DE PREPARACIÓN PARA PRODUCCIÓN (PRODUCTION READINESS) — NEXUSMARKET

**Fecha:** 2026-11-04  
**Versión del Artefacto:** `nexusmarket-domain-1.0.0.jar` (Java 17 / Spring Boot 4.1.0)

---

## 1. Lista de Verificación de Producción (Production Readiness Checklist)

| Categoría | Criterio | Estado | Observaciones Técnicas |
|---|---|---|---|
| **Arquitectura y Código** | Separación estricta Dominio / Aplicación / Infraestructura | **VERIFICADO** | Certificada automáticamente por `HexagonalPurityTest` en cada compilación. |
| **Compilación y Empaquetado** | Empaquetado reproducible Fat JAR con Frontend embebido | **VERIFICADO** | `./mvnw -o clean verify` genera `nexusmarket-domain-1.0.0.jar` con `BOOT-INF/classes/static/`. |
| **Seguridad Aplicativa** | BCrypt, JWT HMAC-SHA256, revocación, RBAC, Anti-IDOR, cabeceras HTTP | **VERIFICADO** | Validación de secretos en arranque (`ProductionSecurityValidator`) activa en perfil `prod`. |
| **Concurrencia y Transacciones** | `@Transactional` y bloqueo optimista `@Version` | **VERIFICADO** | `@Version` implementado en `ProductJpaEntity`, `InventoryJpaEntity` y `OrderJpaEntity`. |
| **Esquema Relacional** | Migración idempotente SQL Server (`schema-sqlserver.sql`) | **IMPLEMENTADO** | Incluye las 16 tablas, columnas de auditoría, `version`, `reason`, FKs e índices. |
| **Contenedores y Orquestación** | `Dockerfile` multi-etapa (no-root) y `docker-compose.yml` | **IMPLEMENTADO** | Volúmenes persistentes `sqlserver-data` y `mongodb-data`, red bridge y healthchecks configurados. |
| **Pipeline CI/CD** | Compilación, sincronización de frontend, pruebas y Docker build | **IMPLEMENTADO** | Definido en `.github/workflows/ci.yml` para ramas `main`, `master` y `develop`. |
| **Pruebas en Vivo sobre Motores Docker** | Arranque real de contenedores SQL Server 2022 y MongoDB 7.0 | **BLOQUEADO EN ANFITRIÓN** | Requiere iniciar Docker Desktop (`/var/run/docker.sock` inactivo en el entorno actual). |
| **Pasarela Bancaria y SMTP Externos** | Integración con proveedor de pagos real y servidor de correo | **SIMULADO (DISEÑO)** | La validación de montos, estados e inventario es real; el cobro bancario y envío SMTP están simulados internamente. |

---

## 2. Procedimiento Exacto para Despliegue en Entorno con Docker Activo

1. **Preparar variables de entorno seguras:**
   ```bash
   cp .env.example .env
   ```
   Edite `.env` y defina contraseñas robustas para `MSSQL_SA_PASSWORD`, `DB_PASSWORD`, `SPRING_DATASOURCE_PASSWORD`, `MONGO_INITDB_ROOT_PASSWORD` y una clave `SECURITY_JWT_SECRET` de al menos 32 bytes (`openssl rand -base64 48`).

2. **Construir e iniciar la infraestructura completa:**
   ```bash
   docker compose up --build -d
   ```

3. **Verificar salud de los 3 servicios (`nexusmarket-db`, `nexusmarket-mongo`, `nexusmarket-app`):**
   ```bash
   docker compose ps
   curl -fsS http://localhost:8080/actuator/health
   ```

4. **Verificar persistencia tras reinicio de contenedores:**
   ```bash
   docker compose restart
   docker compose ps
   ```

5. **Detener los servicios conservando los volúmenes de datos:**
   ```bash
   docker compose down
   ```

---

## 3. Requisitos Finales antes de Exposición a Tráfico Comercial Real

1. **Certificación en Vivo de Contenedores:** Iniciar Docker Desktop en el servidor o estación de despliegue y ejecutar `docker compose up --build -d` para confirmar la ejecución en vivo de `schema-sqlserver.sql` sobre Microsoft SQL Server 2022 y la escritura de `audit_logs` en MongoDB 7.0.
2. **Revocación Distribuida de Tokens (si se escala horizontalmente a múltiples réplicas):** Si `nexusmarket-app` se despliega con más de una réplica detrás de un balanceador de carga, migrar el almacén de tokens revocados y de tokens de recuperación de `ConcurrentHashMap` a Redis o una tabla compartida en SQL Server/MongoDB.
3. **Adaptadores de Proveedores Externos (Opcional según alcance comercial):** Si el sistema procesará cobros bancarios reales o correos salientes por Internet, implementar adaptadores secundarios para la pasarela de pagos elegida (con validación de firma de webhooks) y un proveedor SMTP/SES sobre `NotificationPort`.
