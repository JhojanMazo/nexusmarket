# C4 - Capa Adapters Persistence

## 1. Organización de Persistencia
La persistencia se organiza bajo `application.adapters.persistence`:

```mermaid
graph TD
    subgraph Persistence_Root ["adapters/persistence/"]
        subgraph JPA ["jpa/"]
            JPA_Adapters["*JpaAdapter"]
            JPA_Entities["entities/*JpaEntity, *Entity"]
            JPA_Mappers["mappers/*JpaMapper"]
            JPA_Repos["repositories/SpringDataJpa*Repository"]
        end

        subgraph MongoDB ["mongodb/"]
            Mongo_Adapter["AuditLogMongoAdapter"]
            Mongo_Docs["documents/AuditLogDocument"]
            Mongo_Mappers["mappers/AuditLogMongoMapper"]
            Mongo_Repos["repositories/AuditLogMongoRepository"]
        end
    end

    JPA_Adapters --> JPA_Repos
    JPA_Adapters --> JPA_Mappers
    JPA_Mappers --> JPA_Entities

    Mongo_Adapter --> Mongo_Repos
    Mongo_Adapter --> Mongo_Mappers
    Mongo_Mappers --> Mongo_Docs
```

---

## 2. Aislamiento Tecnológico
Los modelos de dominio no contienen anotaciones de `@Entity`, `@Table`, `@Id` ni `@Document`. Es el mapper correspondiente quien realiza la conversión bidireccional entre la estructura de persistencia y el agregado de negocio.
