# NexusMarket - Microservicio de Comercio Electrónico y Marketplace

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%2F4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20DDD-blue.svg)](./SDD/Software%20Architecture/Software%20Architecture.md)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](./LICENSE)

**NexusMarket** es una plataforma de comercio electrónico y marketplace multi-actor construida bajo los principios de **Diseño Guiado por el Dominio (DDD)**, **Arquitectura Hexagonal Estricta (Puertos y Adaptadores)** y **Desarrollo Guiado por Especificaciones (SDD)**.

---

## 🏛️ Estructura del Repositorio y Arquitectura Hexagonal

El repositorio separa limpiamente la documentación arquitectónica en la raíz (`SDD/`) y encapsula el módulo Maven en su propia carpeta `nexusmarket/`.

```
nexusmarket/src/main/java/application/
├── domain/            # Núcleo puro agnóstico de frameworks
│   ├── enums/         # Clasificaciones, estados y tipos
│   ├── models/        # Agregados y entidades de negocio
│   ├── valueobjects/  # Objetos de valor inmutables
│   ├── exceptions/    # Excepciones específicas del dominio
│   ├── ports/
│   │   ├── in/        # Puertos de entrada 100% orientados a Actor (*Port.java)
│   │   └── out/       # Puertos de salida y repositorios (*RepositoryPort.java)
│   └── services/      # Servicios de dominio por contexto (user, product, order, etc.)
├── adapters/          # Adaptadores primarios (driving) y secundarios (driven)
│   ├── rest/          # Controladores por Actor, DTOs y Mappers
│   ├── useCases/      # Casos de uso por Actor (*UseCaseImpl.java)
│   ├── persistence/   # Adaptadores JPA (SQL) y MongoDB (Auditoría)
│   ├── security/      # JWT, filtros, configuración y contexto de seguridad
│   ├── notification/  # Notificaciones a clientes y operadores
│   └── config/        # Configuraciones de negocio
└── infrastructure/    # Configuración de arranque, seeders e infraestructura
    └── seed/          # Inicialización de datos semilla
```

---

## 🛠️ Tecnologías y Stack Técnico

- **Lenguaje:** Java 17 LTS (Eclipse Temurin)
- **Framework:** Spring Boot (Web, Data JPA, Data MongoDB, Actuator)
- **Persistencia Transaccional:** Microsoft SQL Server / H2 (testing)
- **Persistencia NoSQL:** MongoDB para logs de operaciones y auditoría
- **Seguridad:** Tokens JWT (HMAC-SHA256) y filtros de autorización RBAC
- **Contenerización:** Docker & Docker Compose
- **Testing:** JUnit 5, Mockito, MockMvc

---

## 📚 Documentación Técnica y Especificaciones (SDD)

La documentación exhaustiva del sistema se encuentra organizada en el directorio [`SDD/`](./SDD/):
- **[Enunciado y Reglas de Negocio](./SDD/enunciado.md)**
- **[Criterios de Evaluación y Casos de Uso](./SDD/enunciado%20evaluativo.md)**
- **[Especificación de Arquitectura de Software](./SDD/Software%20Architecture/Software%20Architecture.md)**
- **[Modelo C4 con Diagramas Mermaid](./SDD/C4/README.md)**
- **[Especificación de Adaptadores](./SDD/Adapters/)**
- **[Modelo de Dominio y Servicios](./SDD/domain/)**

---

## 🚀 Inicio Rápido

Consulta [`SETUP.md`](./SETUP.md) para las instrucciones detalladas de configuración, pruebas y despliegue local o con Docker.
