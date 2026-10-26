# C4 - Nivel 2: Diagrama de Contenedores

## 1. Diagrama de Contenedores
El diagrama de contenedores describe la arquitectura física de despliegue de NexusMarket:

```mermaid
graph TD
    Client["Aplicación Cliente / SPA Web / Móvil<br>[TypeScript / React / Flutter]"]

    subgraph NexusMarket_Boundary ["Límite del Sistema NexusMarket"]
        BackendApp["Backend Microservice (NexusMarket)<br>[Java 17, Spring Boot 3, Hexagonal Architecture]<br>Expone API REST en puerto 8080"]
        RelationalDB[("Base de Datos Relacional<br>[Microsoft SQL Server / MySQL 8 / H2]<br>Almacena datos ACID de usuarios, catálogo, carritos, órdenes y facturas")]
        MongoAuditDB[("Base de Datos Documental<br>[MongoDB 7]<br>Almacena registros inmutables de auditoría (audit_logs)")]
    end

    Client -->|"HTTP/JSON con Bearer JWT [8080]"| BackendApp
    BackendApp -->|"JDBC / JPA (HikariCP) [1433/3306]"| RelationalDB
    BackendApp -->|"MongoDB Driver [27017]"| MongoAuditDB
```

---

## 2. Contenedores y Tecnologías

### 2.1 Backend Microservice (`nexusmarket-backend`)
- **Tecnología:** Java 17 Temurin, Spring Boot, Spring Data JPA, Spring Data MongoDB.
- **Responsabilidad:** Implementa la lógica de negocio pura, autenticación JWT, validaciones de catálogo, transiciones de estado de órdenes y facturación fiscal.

### 2.2 Base de Datos Relacional (`nexusmarket-sql`)
- **Tecnología:** SQL Server / MySQL / H2 en memoria para pruebas.
- **Responsabilidad:** Garantiza la integridad transaccional ACID, unicidad de documentos de identidad y correos electrónicos.

### 2.3 Base de Datos NoSQL (`nexusmarket-mongodb`)
- **Tecnología:** MongoDB 7.
- **Responsabilidad:** Bitácora inmutable de eventos de auditoría (`audit_logs`) con alta concurrencia de escritura.
