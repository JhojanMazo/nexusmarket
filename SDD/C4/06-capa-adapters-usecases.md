# C4 - Capa Adapters UseCases

## 1. Implementaciones de Casos de Uso por Actor
Los casos de uso residen bajo `application.adapters.useCases` y materializan los puertos de entrada por Actor:

```mermaid
classDiagram
    class PublicAccessPort {
        <<interface>>
        +authenticate(String, String) AuthenticationResult
        +logout(String) void
        +registerBuyer(...) Buyer
    }

    class PublicAccessUseCaseImpl {
        -UserRepositoryPort userRepository
        -JwtServicePort jwtServicePort
        +authenticate(String, String) AuthenticationResult
        +logout(String) void
        +registerBuyer(...) Buyer
    }

    class BuyerPort {
        <<interface>>
        +createCart(UUID) ShoppingCart
        +addItem(...) ShoppingCart
        +createOrderFromCart(...) Order
        +processPayment(...) Order
        +requestReturn(...) ReturnRequest
    }

    class BuyerUseCaseImpl {
        -ShoppingCartRepositoryPort cartRepository
        -OrderRepositoryPort orderRepository
        +createCart(UUID) ShoppingCart
        +addItem(...) ShoppingCart
        +createOrderFromCart(...) Order
        +processPayment(...) Order
        +requestReturn(...) ReturnRequest
    }

    class SellerPort {
        <<interface>>
        +createProduct(...) Product
        +updateProductPrice(...) Product
        +getSellerOrders(UUID) List~Order~
    }

    class SellerUseCaseImpl {
        -ProductRepositoryPort productRepository
        +createProduct(...) Product
        +updateProductPrice(...) Product
        +getSellerOrders(UUID) List~Order~
    }

    PublicAccessPort <|.. PublicAccessUseCaseImpl
    BuyerPort <|.. BuyerUseCaseImpl
    SellerPort <|.. SellerUseCaseImpl
```

---

## 2. Inyección de Dependencias
Todos los casos de uso implementan interfaces de puertos de actor (`*Port`) y son registrados como beans de Spring mediante `@Service`. Inyectan exclusivamente puertos de salida (`*RepositoryPort`, `*ServicePort`) y servicios de dominio para garantizar la pureza hexagonal.
