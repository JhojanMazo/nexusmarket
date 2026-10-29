# Guía de Instalación, Configuración y Despliegue - NexusMarket

Esta guía detalla los pasos para configurar, compilar, probar y desplegar el microservicio **NexusMarket** tanto en un entorno local de desarrollo como en contenedores Docker.

---

## 1. Requisitos Previos

- **Java Development Kit (JDK):** Versión 17 LTS instalada y configurada en `JAVA_HOME`.
- **Git:** Para control de versiones.
- **Docker & Docker Compose:** Versión 24+ para la orquestación de SQL Server 2022, MongoDB 7.0 y el contenedor de la aplicación.
- **Maven Wrapper (`./nexusmarket/mvnw`):** Incluido en el proyecto (no requiere instalación global de Maven).

---

## 2. Configuración de Variables de Entorno

1. Copia el archivo de ejemplo en la raíz del proyecto:
   ```bash
   cp .env.example .env
   ```
2. Reemplaza todos los valores `<REPLACE_WITH_...>` en `.env` por contraseñas y claves seguras antes de desplegar en Docker/producción (`ProductionSecurityValidator` bloqueará el arranque en perfil `prod` si detecta valores vacíos o plantillas de ejemplo):

| Variable | Descripción | Valor por Defecto Local |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | `8080` |
| `SPRING_DATASOURCE_URL` / `DB_URL` | URL JDBC de conexión relacional | `jdbc:sqlserver://localhost:1433;databaseName=NexusMarket;encrypt=true;trustServerCertificate=true` |
| `SPRING_DATASOURCE_USERNAME` / `DB_USERNAME` | Usuario de base de datos relacional | `sa` |
| `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | Contraseña de base de datos relacional | Configurada en `.env` |
| `SPRING_DATA_MONGODB_URI` | URI de conexión a MongoDB (auditoría AOP) | `mongodb://localhost:27017/nexusmarket_audit` |
| `SECURITY_JWT_SECRET` | Secreto criptográfico HMAC-SHA256 para JWT | Configurado en `.env` |
| `SECURITY_JWT_EXPIRATION_SECONDS` | Vigencia del token JWT en segundos | `86400` (24 horas) |
| `NEXUSMARKET_ORDERS_TTL_MINUTES` | TTL en minutos para liberar stock de órdenes `PENDING_PAYMENT` | `30` |

---

## 3. Ejecución con Docker Compose (Recomendado)

El archivo [`docker-compose.yml`](./docker-compose.yml) orquesta:
- `nexusmarket-db` (Microsoft SQL Server 2022 con volumen persistente `sqlserver-data` y `healthcheck`).
- `nexusmarket-mongo` (MongoDB 7.0 con volumen persistente `mongodb-data` y `healthcheck`).
- `nexusmarket-app` (Microservicio Spring Boot construido con Dockerfile multi-etapa y usuario no-root `spring`).

```bash
# 1. Construir e iniciar todos los servicios en segundo plano
docker compose up --build -d

# 2. Verificar que los contenedores estén saludables (healthy)
docker compose ps

# 3. Inspeccionar logs de la aplicación
docker compose logs -f nexusmarket-app

# 4. Detener contenedores conservando los datos persistidos
docker compose down

# 5. Detener contenedores y eliminar volúmenes (reinicio limpio)
docker compose down -v
```

---

## 4. Compilación y Ejecución Local con Maven Wrapper

### 4.1. Iniciar únicamente las bases de datos en Docker
```bash
docker compose up -d nexusmarket-db nexusmarket-mongo
```

### 4.2. Compilar y empaquetar desde la raíz
```bash
./nexusmarket/mvnw -f nexusmarket/pom.xml clean package -DskipTests
```

### 4.3. Ejecutar el microservicio y Frontend Web localmente
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Opcionalmente, si deseas iniciar el servidor de desarrollo estático del Frontend en el puerto `3000` (conectado automáticamente al backend en `http://localhost:8080/api`):
```bash
cd frontend && npm start
```

Una vez iniciado, el sistema estará disponible en:
- **Marketplace Web UI (SPA Embebida):** `http://localhost:8080/`
- **Marketplace Web UI (Servidor Frontend Standalone):** `http://localhost:3000/`
- **API Base:** `http://localhost:8080/api`
- **Swagger UI (OpenAPI 3):** `http://localhost:8080/swagger-ui.html`
- **Healthcheck Actuator:** `http://localhost:8080/actuator/health`

---

## 5. Ejecución de Pruebas Automatizadas (Backend, Frontend y E2E)

La suite de pruebas utiliza **H2 en memoria**, mocks de infraestructura y el runner nativo de Node.js, por lo que se ejecuta al 100% sin requerir contenedores activos:

```bash
# 1. Ejecutar las 185 pruebas de Backend + Integración + E2E y verificar el Fat JAR ejecutable
./mvnw clean verify

# 2. Ejecutar en modo offline (usando la caché local ~/.m2)
./mvnw -o clean verify

# 3. Ejecutar las 4 suites automatizadas del Frontend Web (node --test)
cd frontend && npm test
```

---

## 6. Pruebas de API con Postman

1. Abre Postman e importa:
   - [`postman/NexusMarket.postman_environment.json`](./postman/NexusMarket.postman_environment.json)
   - [`postman/NexusMarket.postman_collection.json`](./postman/NexusMarket.postman_collection.json)
2. Selecciona el entorno **NexusMarket Local & Docker Environment**.
3. Ejecuta las carpetas en orden (`1. Auth & Public Catalog` -> `2. Administrators` -> `3. Sellers` -> `4. Buyers` -> `5. Logistics` -> `6. Supervisors`).
