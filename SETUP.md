# Guía de Instalación y Despliegue - NexusMarket

Esta guía describe los pasos necesarios para configurar, ejecutar y probar el microservicio **NexusMarket** tanto en un entorno local de desarrollo como en contenedores Docker.

---

## 1. Requisitos Previos

- **Java Development Kit (JDK):** Versión 17 o superior instalada y configurada (`JAVA_HOME`).
- **Git:** Para el clonado y gestión de versiones.
- **Docker & Docker Compose:** Versión 24+ para ejecución contenerizada de base de datos SQL y MongoDB.
- *(Opcional)* **Maven 3.9+:** Si no se utiliza el Maven Wrapper incluido (`./mvnw`).

---

## 2. Variables de Entorno

El microservicio utiliza perfiles de Spring Boot y variables de entorno estándar:

| Variable | Descripción | Valor por Defecto Local |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | `8080` |
| `DB_URL` | URL de conexión JDBC a SQL Server | `jdbc:sqlserver://localhost:1433;databaseName=NexusMarket;encrypt=true;trustServerCertificate=true` |
| `DB_USERNAME` | Usuario de base de datos relacional | `sa` |
| `DB_PASSWORD` | Contraseña de base de datos relacional | `Password123!` |
| `SPRING_DATA_MONGODB_URI` | URI de conexión a MongoDB | `mongodb://admin:password123@localhost:27017/nexusmarket_audit?authSource=admin` |
| `SECURITY_JWT_SECRET` | Clave secreta para firma HMAC-SHA256 de JWT | `NexusMarketSuperSecretKeyForJwtSigning2026EnterpriseSecurity` |
| `SECURITY_JWT_EXPIRATION_SECONDS` | Tiempo de vida del token JWT | `86400` (24 horas) |

---

## 3. Ejecución con Docker Compose (Recomendado)

El archivo `docker-compose.yml` orquesta la base de datos relacional (SQL Server), la base de datos NoSQL (MongoDB) y el contenedor de la aplicación `nexusmarket`:

```bash
# 1. Levantar todos los servicios en segundo plano
docker-compose up --build -d

# 2. Inspeccionar logs del servicio backend
docker-compose logs -f nexusmarket-app

# 3. Verificar estado de los contenedores
docker-compose ps

# 4. Detener y remover contenedores y volúmenes
docker-compose down -v
```

---

## 4. Ejecución Local con Maven Wrapper

Si prefieres ejecutar el microservicio directamente en tu host:

### 4.1. Iniciar las bases de datos requeridas
```bash
docker-compose up -d nexusmarket-db nexusmarket-mongo
```

### 4.2. Compilar el proyecto
```bash
cd nexusmarket
./mvnw clean compile
```

### 4.3. Ejecutar la aplicación
```bash
cd nexusmarket
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080/`.

---

## 5. Ejecución de Pruebas Automatizadas

La suite completa de pruebas unitarias y de integración se ejecuta con:

```bash
cd nexusmarket
./mvnw clean test
```

Para generar reportes de pruebas o ejecutar en modo offline si estás en un entorno aislado:
```bash
cd nexusmarket
./mvnw test -o
```
