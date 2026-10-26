# C4 - Capa Domain (Núcleo Hexagonal)

## 1. Subpaquetes del Dominio
El núcleo agnóstico se organiza bajo `application.domain`:

```mermaid
graph TD
    subgraph Domain_Package ["domain/"]
        enums["enums/<br>UserRole, OrderStatus, ProductStatus, etc."]
        models["models/<br>Buyer, Seller, Product, Order, Invoice, etc."]
        valueobjects["valueobjects/<br>Address, Email, Money, OrderItem, etc."]
        exceptions["exceptions/<br>DomainException, EntityNotFoundException, etc."]

        subgraph Ports ["ports/"]
            portsIn["in/<br>*Port"]
            portsOut["out/<br>*RepositoryPort, *Port"]
        end

        subgraph Services ["services/ (Contextos Funcionales)"]
            userSrv["user/"]
            authSrv["authorization/"]
            prodSrv["product/"]
            invSrv["inventory/"]
            ordSrv["order/"]
            billSrv["billing/"]
            logSrv["logistics/"]
            opSrv["operation/"]
        end
    end

    models --> valueobjects
    models --> enums
    models --> exceptions
    portsIn --> models
    portsOut --> models
    Services --> models
    Services --> portsOut
```

---

## 2. Invariantes y Reglas de Negocio
El dominio concentra el 100% de las políticas y validaciones esenciales. No permite la existencia de estados inconsistentes (ej. productos con precios negativos o carritos con cantidades menores a 1).
