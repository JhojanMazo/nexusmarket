# C4 - Capa Adapters Security

## 1. Componentes de Seguridad
La capa `application.adapters.security` proporciona autenticación y autorización:

```mermaid
graph TD
    subgraph Security_Package ["adapters/security/"]
        JwtProv["JwtProvider<br>[implements JwtServicePort]"]
        PassAdapter["BCryptPasswordServiceAdapter<br>[implements PasswordServicePort]"]
        UserDetails["NexusMarketUserDetailsAdapter<br>[implements AuthorizationPort]"]
        JwtFilter["JwtAuthenticationFilter<br>[OncePerRequestFilter]"]
        SecContext["SecurityUserContext<br>[ThreadLocal Principal]"]
        SecCfg["SecurityConfig<br>[CORS & Filter Registration]"]

        subgraph Security_DTOs ["dtos/ & mappers/"]
            Principal["AuthenticatedUserPrincipal"]
            ClaimsDTO["JwtClaimsDTO"]
            ClaimsMapper["JwtClaimsMapper"]
        end
    end

    JwtFilter --> JwtProv
    JwtFilter --> SecContext
    SecContext --> Principal
    ClaimsMapper --> Principal
    ClaimsMapper --> ClaimsDTO
    UserDetails --> AuthorizationPort
```

---

## 2. Inmutabilidad y Aislamiento por Hilo
El contexto de seguridad utiliza `ThreadLocal<AuthenticatedUserPrincipal>` asegurando que cada petición HTTP mantenga su propia identidad aislada, la cual es destruida en el bloque `finally` del filtro.
