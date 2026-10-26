# C4 - Capa Infrastructure

## 1. Responsabilidad y Componentes
La capa de infraestructura (`application.infrastructure`) contiene la inicialización del microservicio:

```mermaid
graph TD
    BootApp["NexusMarketApplication<br>[@SpringBootApplication]"]
    Seeder["DatabaseSeeder<br>[CommandLineRunner]"]

    UserRepo["UserRepositoryPort"]
    ProdRepo["ProductRepositoryPort"]

    BootApp --> Seeder
    Seeder --> UserRepo
    Seeder --> ProdRepo
```

---

## 2. Inicialización Automática (Seeding)
En cada arranque de la aplicación, el `DatabaseSeeder` verifica si existe el usuario administrador raíz (`admin@nexusmarket.com`) y productos iniciales de catálogo. Si la base de datos se encuentra vacía, procede con la carga automática de demostración.
