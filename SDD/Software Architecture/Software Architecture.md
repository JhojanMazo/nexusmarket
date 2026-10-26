# Especificación de Arquitectura de Software: NexusMarket

## 1. Visión General y Estilo Arquitectónico
NexusMarket implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** combinada con **Domain-Driven Design (DDD)**. El objetivo principal es aislar completamente el núcleo de negocio de cualquier detalle tecnológico, permitiendo que frameworks web, motores de bases de datos y servicios criptográficos sean componentes intercambiables conectados a través de puertos.

```mermaid
graph TD
    subgraph Primary_Adapters ["Adaptadores Primarios (Inbound)"]
        REST["REST Controllers (*RestController)"]
        JWT_FILTER["Filtro JWT & CORS"]
    end

    subgraph Hexagonal_Core ["Núcleo Hexagonal"]
        subgraph Ports_In ["Puertos de Entrada (ports/in)"]
            IN_PORTS["ProductPort, OrderPort, ShoppingCartPort, UserPort, InvoicePort, AuthPort"]
        end

        subgraph UseCases ["Casos de Uso (adapters/useCases)"]
            UC_IMPL["*UseCaseImpl"]
        end

        subgraph Domain_Services ["Servicios de Dominio (domain/services)"]
            DOM_SRV["user, product, inventory, order, billing, logistics, operation, authorization"]
        end

        subgraph Domain_Core ["Modelos y Value Objects"]
            MODELS["Buyer, Seller, Product, Order, Invoice, Inventory, Operation, AuditLog"]
            VO["Money, Address, Email, IdentityDocument, ProductVariant"]
        end

        subgraph Ports_Out ["Puertos de Salida (ports/out)"]
            OUT_PORTS["UserRepositoryPort, ProductRepositoryPort, OrderRepositoryPort, ShoppingCartRepositoryPort, InvoiceRepositoryPort, OperationRepositoryPort, AuditLogRepositoryPort, JwtServicePort, PasswordServicePort"]
        end
    end

    subgraph Secondary_Adapters ["Adaptadores Secundarios (Outbound)"]
        JPA_ADAPTER["JPA Adapters (*JpaAdapter)"]
        MONGO_ADAPTER["MongoDB Adapter (AuditLogMongoAdapter)"]
        SEC_ADAPTER["Criptografía y JWT (JwtProvider, BCryptPasswordServiceAdapter)"]
    end

    REST --> IN_PORTS
    IN_PORTS -.-> UC_IMPL
    UC_IMPL --> DOM_SRV
    DOM_SRV --> MODELS
    DOM_SRV --> OUT_PORTS
    UC_IMPL --> OUT_PORTS

    OUT_PORTS -.-> JPA_ADAPTER
    OUT_PORTS -.-> MONGO_ADAPTER
    OUT_PORTS -.-> SEC_ADAPTER
```

---

## 2. Descripción Detallada de Capas

### 2.1 Capa de Dominio (`application.domain`)
Es el corazón de la aplicación. No tiene dependencia de ninguna infraestructura ni framework externo.
- **`enums/`:** Clasificaciones y estados de entidades del negocio.
- **`models/`:** Entidades y agregados raíz que mantienen la identidad y encapsulan las reglas invariantes.
- **`valueobjects/`:** Objetos de valor inmutables identificados por sus atributos.
- **`exceptions/`:** Jerarquía de excepciones de negocio tipadas derivadas de `DomainException`.
- **`ports/in/`:** Contratos de los casos de uso disponibles para los actores externos.
- **`ports/out/`:** Interfaces de pasarelas de persistencia, seguridad y notificaciones.
- **`services/`:** Lógica de negocio orquestada dividida en 8 subpaquetes funcionales (`user`, `authorization`, `product`, `inventory`, `order`, `billing`, `logistics`, `operation`).

### 2.2 Capa de Adaptadores (`application.adapters`)
Conecta el mundo exterior con el núcleo de dominio:
- **`useCases/`:** Implementaciones de los puertos de entrada (`*UseCaseImpl`).
- **`rest/`:** Exposición HTTP REST (`controllers/`, `dtos/requests/`, `dtos/responses/`, `mappers/`, `exception/`).
- **`security/`:** Implementación de hashing de contraseñas (`BCryptPasswordServiceAdapter`), emisor/validador JWT (`JwtProvider`), filtros y contexto de seguridad thread-safe.
- **`persistence/jpa/`:** Implementación relacional SQL con Spring Data JPA y mapeadores dedicados.
- **`persistence/mongodb/`:** Implementación documental para bitácoras de auditoría en MongoDB.
- **`config/`:** Proveedor de parámetros de negocio en tiempo de ejecución.
- **`notification/`:** Pasarela de notificaciones multicanal.

### 2.3 Capa de Infraestructura (`application.infrastructure`)
Configuraciones y componentes del ciclo de vida del sistema:
- **`seed/DatabaseSeeder.java`:** `CommandLineRunner` que inicializa el administrador predeterminado y productos iniciales de demostración.
- **`NexusMarketApplication.java`:** Clase de arranque Spring Boot.

---

## 3. Persistencia Políglota y Gestión de Datos
- **Base de Datos Relacional (SQL Server / MySQL / H2):** Maneja las transacciones críticas con integridad referencial ACID (usuarios, productos, carritos, pedidos, facturas y operaciones).
- **Base de Datos NoSQL (MongoDB):** Maneja documentos de auditoría (`audit_logs`) con esquema flexible y alta velocidad de inserción para trazabilidad forense.
