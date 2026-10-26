# Adaptadores de Casos de Uso: NexusMarket

## 1. Visión General
Los adaptadores de casos de uso bajo `application.adapters.useCases` implementan los puertos de entrada (`domain/ports/in/`) y orquestan la lógica de negocio coordinando los servicios de dominio y los puertos de salida (`domain/ports/out/`).
En consonancia con el diseño **100% orientado a actores**, cada caso de uso implementa el contrato de negocio exclusivo de un actor o rol del sistema.

---

## 2. Catálogo Oficial de Casos de Uso por Actor

| Caso de Uso (`*UseCaseImpl`) | Puerto de Entrada Implementado | Dependencias de Salida Inyectadas | Responsabilidad Principal |
|---|---|---|---|
| `PublicAccessUseCaseImpl` | `PublicAccessPort` | `UserRepositoryPort`, `JwtServicePort`, `PasswordServicePort`, `UniqueIdentityValidationService`, `AuthorizeCustomerRegistrationService` | Autenticación pública, verificación de estado de cuenta, emisión de JWT y registro inicial de compradores. |
| `BuyerUseCaseImpl` | `BuyerPort` | `UserRepositoryPort`, `ShoppingCartRepositoryPort`, `ProductRepositoryPort`, `OrderRepositoryPort`, `UniqueIdentityValidationService` | Orquestación del carrito de compras, cálculo de líneas, conversión en pedido en firme, actualización de dirección y pagos. |
| `SellerUseCaseImpl` | `SellerPort` | `UserRepositoryPort`, `ProductRepositoryPort`, `OrderRepositoryPort`, `UniqueIdentityValidationService` | Registro de vendedores por onboarding, publicación de productos, gestión de variantes, precios e inventario de ventas. |
| `AdministratorUseCaseImpl` | `AdministratorPort` | `UserRepositoryPort`, `InvoiceRepositoryPort`, `OrderRepositoryPort`, `OperationRepositoryPort`, `UniqueIdentityValidationService` | Alta de personal interno (Staff), bloqueo/activación de cuentas, emisión de facturas fiscales y bitácora de operaciones de administración. |
| `SupervisorUseCaseImpl` | `SupervisorPort` | `OrderRepositoryPort`, `OperationRepositoryPort` | Gestión de disputas de clientes, aprobación o denegación de solicitudes de retorno, emisión de reembolsos y auditoría de pedidos. |
| `LogisticsOperatorUseCaseImpl` | `LogisticsOperatorPort` | `OrderRepositoryPort` | Gestión del flujo de entrega (despacho, en tránsito, entregado), auditoría de movimientos de almacén y reservas/liberaciones de stock. |

---

## 3. Principio de Aislamiento
Ningún caso de uso conoce la tecnología subyacente de base de datos (JPA, MongoDB, Hibernate) ni de presentación (Spring MVC, Servlets). Solo interactúan con interfaces de dominio (`domain/ports/out/`) y servicios de dominio (`domain/services/`).
