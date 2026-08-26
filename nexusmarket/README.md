# NexusMarket Domain Model

English translation of the domain model, plus a design review of the `User`
specializations that was requested alongside the translation.

## Package layout

```
com.nexusmarket.domain.enums        11 enumerations
com.nexusmarket.domain.valueobject  6 immutable value objects
com.nexusmarket.domain.model        13 entities
com.nexusmarket.domain.service      1 domain service (new)
```

## Design review: which specializations are really entities, and which are just roles?

The original model had five `User` specializations: `Buyer`, `Seller`,
`LogisticsOperator`, `Administrator`, `Supervisor`. Reviewing each one against
a simple test — *does it add attributes or invariants beyond `User` +
`role`?* — splits them into two groups:

| Class | Extra state? | Outcome |
|---|---|---|
| **Buyer** | Yes — primary/additional addresses, commercial status, `canPurchase()` | Kept as a subclass |
| **Seller** | Yes — trade name, onboarding date, associated warehouses | Kept as a subclass |
| ~~Administrator~~ | No extra fields at all | **Removed** — just a role |
| ~~LogisticsOperator~~ | No extra fields at all | **Removed** — just a role |
| ~~Supervisor~~ | No extra fields at all | **Removed** — just a role |

`Administrator`, `LogisticsOperator`, and `Supervisor` carried no state or
behavior of their own in this specification — they existed purely to pin down
a `role` value at the type level. That's a role, not a domain concept with its
own identity and lifecycle, so modeling each as its own class added type
hierarchy without expressing any real business rule.

### What changed as a result

- **`User` is now concrete** (previously abstract) so it can be instantiated
  directly for staff roles via `User.createStaff(...)`, which rejects `BUYER`
  and `SELLER` — those still require the `Buyer`/`Seller` constructors, since
  only they carry the extra required fields.
- **`Shipment.logisticsOperator`** is now typed as `User` and validates
  `role == LOGISTICS_OPERATOR` in the constructor, instead of being typed as a
  dedicated `LogisticsOperator` class.
- **The seller-onboarding rule** ("sellers cannot self-register; they are
  onboarded by an Administrator") moved from an `Administrator.onboardSeller(...)`
  instance method into `SellerOnboardingService.onboard(actor, seller,
  warehouse)`, a small domain service that checks `actor.getRole() ==
  ADMINISTRATOR` at runtime.

### The trade-off, stated plainly

The previous design got a *compile-time* guarantee: only an `Administrator`
instance had an `onboardSeller` method to call. The new design checks the same
rule at *runtime*, inside the service. That's a real trade-off, not a free
win — but keeping three near-empty subclasses solely to preserve one
compile-time check, for roles that add nothing else to the domain, wasn't
proportional. If any of these three roles later grow real state or behavior
of their own, promoting it back to a dedicated subclass is a small, local
change.

## Class name mapping (Spanish → English)

| Spanish | English |
|---|---|
| Usuario | User |
| Comprador | Buyer |
| Vendedor | Seller |
| Bodega | Warehouse |
| Producto | Product |
| Inventario | Inventory |
| MovimientoInventario | InventoryMovement |
| CarritoDeCompras | ShoppingCart |
| Pedido | Order |
| Factura | Invoice |
| Envio | Shipment |
| Devolucion | ReturnRequest |
| Reembolso | Refund |
| Direccion | Address |
| Dinero | Money |
| CorreoElectronico | Email |
| DocumentoIdentidad | IdentityDocument |
| VarianteProducto | ProductVariant |
| ItemPedido | OrderItem |

## Build

```
mvn compile
```

Or compile directly with `javac` — the module has no external dependencies.
