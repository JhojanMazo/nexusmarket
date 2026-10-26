# Seguridad REST, JWT y CORS: NexusMarket

## 1. Visión General de Seguridad
NexusMarket implementa seguridad stateless basada en tokens Bearer JWT:
- Algoritmo de firma: HMAC-SHA256 (`HmacSHA256`).
- Implementación criptográfica pura sin librerías externas pesadas (`JwtProvider` bajo `application.adapters.security`).
- Intercepción de peticiones HTTP mediante `JwtAuthenticationFilter` (`OncePerRequestFilter`).
- Contexto de usuario en memoria seguro para hilos concurrentes (`SecurityUserContext` con `ThreadLocal`).

---

## 2. Flujo de Autenticación y Autorización

```mermaid
sequenceDiagram
    autonumber
    actor Client as Cliente API
    participant Filter as JwtAuthenticationFilter
    participant Provider as JwtProvider
    participant Context as SecurityUserContext
    participant Controller as OrderRestController

    Client->>Filter: HTTP GET /api/orders (Authorization: Bearer <token>)
    Filter->>Provider: validateToken(token)
    alt Token Válido
        Filter->>Provider: extractUserId, extractEmail, extractRole
        Provider-->>Filter: Claims
        Filter->>Context: set(AuthenticatedUserPrincipal)
        Filter->>Controller: doFilter(request, response)
        Controller-->>Client: 200 OK (Datos de Órdenes)
        Filter->>Context: clear() (ThreadLocal cleanup)
    else Token Inválido o Ausente
        Filter->>Controller: doFilter(request, response)
    end
```

---

## 3. Configuración CORS (`SecurityConfig`)
- Orígenes permitidos: Configurable (por defecto comodín `*` para entornos de desarrollo y pruebas).
- Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Encabezados permitidos: `Authorization`, `Content-Type`, `Accept`, `Origin`, `X-Requested-With`.
- Credenciales: Soportadas (`AllowCredentials = true`).
