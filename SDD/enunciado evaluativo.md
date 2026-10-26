# Enunciado Evaluativo y Rúbrica de Calidad: NexusMarket

## 1. Criterios de Evaluación Arquitectónica

### 1.1 Estructura Hexagonal Estricta (25%)
- El código fuente bajo `application` se divide únicamente en 3 capas de primer nivel: `domain/`, `adapters/` e `infrastructure/`.
- No existen paquetes de primer nivel `web/` ni `application/`.
- El núcleo `domain/` define puertos de entrada (`ports/in/`) y salida (`ports/out/`), siendo 100% independiente de las tecnologías externas.
- Los adaptadores implementan estrictamente las interfaces de puertos (`*UseCaseImpl`, `*JpaAdapter`, `*MongoAdapter`).

### 1.2 Modelado de Dominio y DDD (20%)
- Subpaquetes en plural: `enums/`, `models/`, `valueobjects/`, `exceptions/`, `ports/in/`, `ports/out/`, `services/`.
- Agregados con encapsulamiento estricto, métodos de fábrica y constructores de validación de invariantes.
- Value Objects inmutables (`Address`, `Email`, `IdentityDocument`, `Money`, `OrderItem`, `ProductVariant`, `DomainCatalog`).
- Excepciones tipadas de dominio (`InvalidCredentialsException`, `UnauthorizedOperationException`, `InsufficientStockException`, `InvalidStatusTransitionException`, etc.).

### 1.3 Adaptadores REST y Seguridad (20%)
- Controladores con sufijo `*RestController` bajo `adapters/rest/controllers/`.
- DTOs separados e inmutables con sufijos `*RequestDTO` y `*ResponseDTO`.
- Mappers dedicados `*RestMapper` sin lógica de negocio incrustada en controladores.
- Manejo centralizado de excepciones con `GlobalExceptionHandler` retornando `ApiErrorResponseDTO`.
- Autenticación JWT stateless con HMAC-SHA256, filtros `OncePerRequestFilter` y contexto de seguridad thread-safe.

### 1.4 Persistencia y Respaldo de Datos (15%)
- Entidades JPA (`*JpaEntity`, `*Entity`) limpias y separadas de los modelos de dominio.
- Repositorios de Spring Data JPA nombrados `SpringDataJpa*Repository`.
- Repositorios MongoDB para logs de auditoría (`AuditLogMongoRepository`).
- Seeder automático de datos iniciales en `infrastructure/seed/DatabaseSeeder`.

### 1.5 Calidad de Software, Pruebas y DevOps (20%)
- Suite de pruebas completa (`./mvnw clean test`) con tasa de éxito del 100%.
- Pruebas unitarias y de integración para adaptadores REST, adaptadores de persistencia, seguridad y servicios de dominio.
- Configuración de Maven Wrapper (`mvnw`, `mvnw.cmd`).
- `Dockerfile` multi-stage y `docker-compose.yml` orquestando base de datos SQL, MongoDB y microservicio.
- Documentación SDD y diagramas C4 completos en Mermaid.
