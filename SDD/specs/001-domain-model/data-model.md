# Data Model: NexusMarket Domain

Companion to `spec.md` (Phase 1 output referenced from `plan.md`). Every
entry below is implemented exactly as named under
`src/main/java/com/nexusmarket/domain/`.

## Enumerations (`enums/`)

| Enum | Values | Used by |
|---|---|---|
| `UserRole` | BUYER, SELLER, LOGISTICS_OPERATOR, ADMINISTRATOR, SUPERVISOR | User |
| `UserStatus` | ACTIVE, BLOCKED, INACTIVE | User |
| `BuyerCommercialStatus` | ENABLED, SUSPENDED | Buyer |
| `WarehouseType` | MARKETPLACE, SELLER | Warehouse |
| `ProductType` | PHYSICAL, DIGITAL | Product |
| `ProductStatus` | PUBLISHED, SUSPENDED, DISCONTINUED | Product |
| `InventoryStatus` | AVAILABLE, RESERVED, DAMAGED | Inventory |
| `InventoryMovementType` | INBOUND, RESERVATION, SALE_OUTBOUND, ADJUSTMENT, RETURN | InventoryMovement |
| `OrderStatus` | CART, PENDING_PAYMENT, PAID, SHIPPED, DELIVERED_COMPLETED | Order |
| `ReturnStatus` | REQUESTED, APPROVED, REJECTED, COMPLETED | ReturnRequest |
| `RefundStatus` | PENDING, PROCESSED, REJECTED | Refund |

## Value Objects (`valueobject/`)

Immutable; equality is structural (FR-027–FR-029; Constitution Article III).

| Value Object | Fields | Validation |
|---|---|---|
| `Address` | street, city, stateOrProvince, country, postalCode, reference? | All fields non-blank except `reference` |
| `Money` | amount (`BigDecimal`), currency (ISO 4217) | amount ≥ 0; `add`/`subtract` require matching currency |
| `Email` | value | Must match a valid email pattern; stored lower-cased |
| `IdentityDocument` | type, number | Both non-blank |
| `ProductVariant` | attribute, value | Both non-blank; equality is case-insensitive |
| `OrderItem` | product (ref), quantity, unitPrice (`Money`) | quantity > 0; exposes `subtotal()` |

## Entities (`model/`)

Identity-compared (by ID); mutable where the business process requires it,
but only through methods that enforce the invariants below (Constitution
Article IV).

### User (concrete base — see `plan.md` Decision D-001)
| Field | Type | Notes |
|---|---|---|
| userId | String | Identity |
| fullName | String | Mutable |
| email | Email | Mutable |
| identityDocument | IdentityDocument | Immutable after construction |
| role | UserRole | Fixed at construction (FR-001) |
| status | UserStatus | Mutable via `activate()`/`block()` |

Created directly only via `User.createStaff(...)` for
ADMINISTRATOR / LOGISTICS_OPERATOR / SUPERVISOR (FR-002).

### Buyer extends User
Adds: `primaryAddress` (required), `additionalAddresses` (0..*),
`commercialStatus`. `canPurchase()` implements FR-003.

### Seller extends User
Adds: `tradeName`, `onboardingDate` (required, FR-005),
`associatedWarehouses` (0..*, populated via `linkWarehouse`, normally called
by `SellerOnboardingService`).

### Warehouse
| Field | Type | Notes |
|---|---|---|
| warehouseId | String | Identity |
| name | String | Mutable |
| type | WarehouseType | Fixed |
| location | Address | Mutable |
| associatedSeller | Seller? | Required iff type == SELLER (FR-007) |

### Product
| Field | Type | Notes |
|---|---|---|
| productId | String | Identity |
| name | String | Mutable |
| type | ProductType | Fixed (FR-008) |
| variants | ProductVariant[] | 0..* |
| status | ProductStatus | Mutable; `isPublished()` implements FR-009 |
| price | Money | Mutable |
| seller | Seller | Fixed |

### Inventory
| Field | Type | Notes |
|---|---|---|
| inventoryId | String | Identity |
| product | Product | Fixed |
| warehouse | Warehouse | Fixed |
| availableQuantity | int | ≥ 0 always (FR-010) |
| reservedQuantity | int | ≥ 0 always (FR-010) |
| status | InventoryStatus | Mutable |

Methods `reserve()`, `confirmSaleOutbound()`, `restockFromReturn()`
implement FR-011–FR-014.

### InventoryMovement (immutable)
movementId, inventory (ref), type, quantity (≠ 0), date, reference? — one
row per quantity change (FR-015).

### ShoppingCart
cartId, buyer (ref), items (`OrderItem[]`, 0..*).

### Order
| Field | Type | Notes |
|---|---|---|
| orderId | String | Identity |
| buyer | Buyer | Fixed |
| items | OrderItem[] | 1..* (FR-016) |
| deliveryAddress | Address | Mutable until finalized |
| status | OrderStatus | `advanceTo()` implements FR-017/FR-018 |
| creationDate | LocalDate | Fixed |

`total()` implements FR-019.

### Invoice (immutable)
invoiceId, order (ref), issueDate, subtotal, tax, total = subtotal + tax
(FR-020).

### Shipment
shipmentId, order (ref), logisticsOperator (`User`, role-checked at
construction — FR-021), dispatchDate?, deliveryDate?, deliveryAddress.
`registerDispatch()` / `confirmDelivery()` implement FR-022.

### ReturnRequest
returnId, order (ref), reason, status (starts `REQUESTED`, FR-023),
requestDate. `approve()` / `reject()` / `complete()` implement
FR-024/FR-025.

### Refund
refundId, returnRequest (ref, must be APPROVED or COMPLETED — FR-026),
amount, status (starts `PENDING`).

## Relationships

```
User ──────────────┬── Buyer ── owns ──> ShoppingCart, Order (0..*)
                    └── Seller ── operates ──> Warehouse, Product (0..*)

Warehouse ── stores ──> Inventory (0..*) ── references ──> Product
Product ── priced by ──> Money; described by ──> ProductVariant (0..*)

Order ── composed of ──> OrderItem (1..*) ── references ──> Product
Order ──> Invoice (0..1) ──> Shipment (0..1) ──> ReturnRequest (0..*) ──> Refund (0..1)

Shipment.logisticsOperator ──> User (role == LOGISTICS_OPERATOR)
SellerOnboardingService(actor: User, seller: Seller, warehouse: Warehouse)
  requires actor.role == ADMINISTRATOR
```
