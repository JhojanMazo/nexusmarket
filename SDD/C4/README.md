# Modelo C4 y Diagramas de Arquitectura: NexusMarket

## 1. Introducción al Modelo C4
El modelo C4 (Contexto, Contenedores, Componentes y Código) proporciona un enfoque estructurado para visualizar y documentar la arquitectura de software de NexusMarket en múltiples niveles de abstracción jerárquica.

---

## 2. Índice de Documentos C4

| Nivel / Documento | Archivo | Descripción |
|---|---|---|
| **Nivel 1: Contexto** | [01-nivel-1-contexto.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/01-nivel-1-contexto.md) | Sistema NexusMarket y su interacción con usuarios y sistemas externos |
| **Nivel 2: Contenedores** | [02-nivel-2-contenedores.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/02-nivel-2-contenedores.md) | Despliegue de backend, base de datos relacional y MongoDB |
| **Nivel 3: Componentes** | [03-nivel-3-componentes.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/03-nivel-3-componentes.md) | Descomposición de controladores, casos de uso, servicios y adaptadores |
| **Nivel 4: Código** | [04-nivel-4-codigo.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/04-nivel-4-codigo.md) | Diagrama de clases de los agregados y value objects principales |
| **Capa Adapters REST** | [05-capa-adapters-rest.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/05-capa-adapters-rest.md) | Arquitectura y flujo de controladores, DTOs y mappers |
| **Capa Adapters UseCases** | [06-capa-adapters-usecases.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/06-capa-adapters-usecases.md) | Implementaciones de puertos de entrada e inyección de dependencias |
| **Capa Adapters Persistence** | [07-capa-adapters-persistence.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/07-capa-adapters-persistence.md) | Adaptadores JPA y MongoDB, entidades, repositorios y mappers |
| **Capa Adapters Security** | [08-capa-adapters-security.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/08-capa-adapters-security.md) | JWT Provider, BCrypt hashing, filtros y contexto de seguridad |
| **Capa Domain** | [09-capa-domain.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/09-capa-domain.md) | Núcleo agnóstico: modelos, value objects, excepciones, puertos y 30 servicios |
| **Capa Infrastructure** | [10-capa-infrastructure.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/10-capa-infrastructure.md) | Configuración de arranque, seeders de datos y perfiles |
| **Endpoints y Flujos** | [11-endpoints-flujos.md](file:///Users/pc/Downloads/Jhojan/TdeA/construccion%20II%20/nexusmarket/SDD/C4/11-endpoints-flujos.md) | Diagramas de secuencia de los flujos de negocio principales de la API |
