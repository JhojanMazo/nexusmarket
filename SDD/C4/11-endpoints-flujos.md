# C4 - Endpoints y Flujos de Negocio de la API

## 1. Flujo Completo de Creación de Orden y Facturación
En el modelo orientado a actores, el Comprador interactúa con `BuyerRestController` y la facturación se emite a través de `AdministratorRestController`:

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Comprador HTTP
    participant BuyerCtrl as BuyerRestController
    participant BuyerPort as BuyerPort (BuyerUseCaseImpl)
    actor Admin as Administrador HTTP
    participant AdminCtrl as AdministratorRestController
    participant AdminPort as AdministratorPort (AdministratorUseCaseImpl)
    participant DB as SQL Database

    Buyer->>BuyerCtrl: POST /api/buyers/{buyerId}/cart
    BuyerCtrl->>BuyerPort: createCart(buyerId)
    BuyerPort->>DB: save(ShoppingCart)
    DB-->>BuyerPort: Cart guardado
    BuyerPort-->>BuyerCtrl: ShoppingCart
    BuyerCtrl-->>Buyer: 201 Created (ShoppingCartResponseDTO)

    Buyer->>BuyerCtrl: POST /api/buyers/cart/{cartId}/items (productId, qty, price)
    BuyerCtrl->>BuyerPort: addItem(cartId, productId, qty, price, currency)
    BuyerPort->>DB: save(ShoppingCart con nuevo ítem)
    DB-->>BuyerPort: Cart actualizado
    BuyerPort-->>BuyerCtrl: ShoppingCart
    BuyerCtrl-->>Buyer: 200 OK (ShoppingCartResponseDTO)

    Buyer->>BuyerCtrl: POST /api/buyers/orders (cartId, deliveryAddress)
    BuyerCtrl->>BuyerPort: createOrderFromCart(cartId, address)
    BuyerPort->>DB: save(Order) + clear(ShoppingCart)
    DB-->>BuyerPort: Order confirmada
    BuyerPort-->>BuyerCtrl: Order
    BuyerCtrl-->>Buyer: 201 Created (OrderResponseDTO)

    Admin->>AdminCtrl: POST /api/admin/invoices (orderId, taxAmount, USD)
    AdminCtrl->>AdminPort: generateInvoice(orderId, taxAmount, currency)
    AdminPort->>DB: save(Invoice)
    DB-->>AdminPort: Invoice registrada
    AdminPort-->>AdminCtrl: Invoice
    AdminCtrl-->>Admin: 201 Created (InvoiceResponseDTO)
```

---

## 2. Flujo de Autenticación y Autorización
La autenticación pública valida credenciales y entrega el token JWT para los flujos subsiguientes:

```mermaid
sequenceDiagram
    autonumber
    actor User as Usuario HTTP
    participant AuthCtrl as AuthRestController
    participant PubPort as PublicAccessPort (PublicAccessUseCaseImpl)
    participant JwtProv as JwtProvider
    participant DB as SQL Database

    User->>AuthCtrl: POST /api/auth/login (email, password)
    AuthCtrl->>PubPort: authenticate(email, password)
    PubPort->>DB: findByEmail(email)
    DB-->>PubPort: User (ACTIVE)
    PubPort->>JwtProv: generateToken(userId, email, role)
    JwtProv-->>PubPort: Token JWT (HS256)
    PubPort-->>AuthCtrl: AuthenticationResult
    AuthCtrl-->>User: 200 OK (LoginResponseDTO con Bearer Token)
```
