# C4 - Capa Adapters REST

## 1. Estructura y Componentes
La capa de adaptadores REST (`application.adapters.rest`) gestiona todas las interacciones HTTP externas mediante controladores organizados por Actor:

```mermaid
graph TD
    subgraph REST_Layer ["application.adapters.rest"]
        direction TB
        subgraph Controllers ["controllers/ (Por Actor)"]
            AuthCtrl["AuthRestController (/api/auth)"]
            BuyerCtrl["BuyerRestController (/api/buyers)"]
            SellerCtrl["SellerRestController (/api/sellers)"]
            AdminCtrl["AdministratorRestController (/api/admin)"]
            SuperCtrl["SupervisorRestController (/api/supervisors)"]
            LogistCtrl["LogisticsOperatorRestController (/api/logistics)"]
        end

        subgraph DTOs ["dtos/"]
            ReqDTO["requests/*RequestDTO"]
            ResDTO["responses/*ResponseDTO (incl. AuditLogResponseDTO)"]
        end

        subgraph Mappers ["mappers/"]
            RestMappers["*RestMapper (incl. AuditLogRestMapper)"]
        end

        subgraph Exceptions ["exception/"]
            GlobalAdvice["GlobalExceptionHandler"]
        end
    end

    Controllers --> ReqDTO
    Controllers --> ResDTO
    Controllers --> RestMappers
    Controllers -.-> GlobalAdvice
```

---

## 2. Puntos Clave de Implementación
1. **Separación DTO vs Entidad:** Los controladores nunca exponen modelos de dominio directamente; utilizan registros inmutables DTO.
2. **Transformación Pura:** `*RestMapper` traduce bidireccionalmente entre DTOs y modelos de dominio mediante componentes Spring reutilizables.
3. **Manejo de Errores Uniforme:** `GlobalExceptionHandler` intercepta cualquier fallo y produce `ApiErrorResponseDTO` con códigos HTTP estándar (400, 401, 404, 409, 500).
