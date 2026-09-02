# Feature Specification: NexusMarket Domain Model

**Feature branch**: `001-domain-model`
**Status**: Implemented (retrofitted spec — see `plan.md` for rationale)
**Input**: Business Functional Specification — NexusMarket (Administración de
Usuarios, Gestión de Compradores, Gestión de Vendedores, Gestión de Bodegas,
Gestión del Catálogo, Gestión del Inventario, Gestión de Pedidos)

## User Scenarios & Testing

### US-1: Buyer places an order
A Buyer with an enabled commercial status adds physical and digital products
to a cart, confirms the order against a delivery address, and the order
carries a single immutable total from that point on.

### US-2: Administrator onboards a Seller
An Administrator registers a new Seller and links it to a first Warehouse.
The Seller cannot perform this step itself.

### US-3: Logistics Operator fulfills an order
A user holding the LOGISTICS_OPERATOR role is assigned to a Shipment for a
paid Order, dispatches it, and later confirms delivery.

### US-4: Buyer requests a return and refund
A Buyer requests a return on a delivered order; the request is approved,
completed, and a Refund is issued for the approved amount.

### Edge Cases
- Reserving more units than are available for a product/warehouse pair.
- Reserving units from inventory marked `DAMAGED`.
- Attempting to modify an order that has already reached `DELIVERED_COMPLETED`.
- Attempting to skip a step in the order lifecycle (e.g. `CART` → `PAID`
  directly).
- A non-Administrator attempting to onboard a Seller.
- Assigning a Shipment to a user who does not hold `LOGISTICS_OPERATOR`.
- Requesting a Refund from a Return that was `REJECTED` or is still
  `REQUESTED`.
- Two `Money` values in different currencies being added together.

## Functional Requirements

Requirements are written in EARS (Easy Approach to Requirements Syntax) and
are grouped by the aggregate they govern. Each ID is referenced from
`tasks.md` against the class/method that implements it.

### Identity & Access (User, Buyer, Seller)

- **FR-001** (Ubiquitous): The system shall assign exactly one `UserRole` to
  every user, fixed at creation.
- **FR-002** (Unwanted behavior): IF a caller attempts to create a staff user
  with role `BUYER` or `SELLER` via the staff-creation path, THEN the system
  shall reject the request and direct the caller to the `Buyer`/`Seller`
  constructor instead.
- **FR-003** (Ubiquitous): The system shall consider a Buyer able to
  purchase only while the Buyer's `UserStatus` is `ACTIVE` and
  `BuyerCommercialStatus` is `ENABLED`.
- **FR-004** (Ubiquitous): The system shall require every Buyer to have
  exactly one primary `Address` and permit zero or more additional
  addresses.
- **FR-005** (Unwanted behavior): IF a Seller is created without an
  onboarding date, THEN the system shall reject the creation.
- **FR-006** (Unwanted behavior): IF a user without the `ADMINISTRATOR` role
  attempts to onboard a Seller, THEN the system shall reject the operation.

### Catalog & Warehousing (Warehouse, Product)

- **FR-007** (Unwanted behavior): IF a Warehouse is created with type
  `SELLER` and no associated Seller, THEN the system shall reject the
  creation.
- **FR-008** (Ubiquitous): The system shall classify every Product as
  either `PHYSICAL` or `DIGITAL`.
- **FR-009** (Ubiquitous): The system shall consider a Product visible in
  the public catalog only while its status is `PUBLISHED`.

### Inventory (Inventory, InventoryMovement)

- **FR-010** (Ubiquitous): The system shall never allow a `availableQuantity`
  or `reservedQuantity` on Inventory to become negative, under any
  circumstance.
- **FR-011** (Unwanted behavior): IF a reservation is requested against
  Inventory whose status is `DAMAGED`, THEN the system shall reject the
  reservation.
- **FR-012** (Unwanted behavior): IF a reservation quantity exceeds the
  current `availableQuantity`, THEN the system shall reject the reservation.
- **FR-013** (Event-driven): WHEN a reservation is accepted, the system
  shall move the reserved quantity from `availableQuantity` to
  `reservedQuantity` atomically.
- **FR-014** (Event-driven): WHEN a return is completed, the system shall
  restock the returned quantity back into `availableQuantity`.
- **FR-015** (Ubiquitous): The system shall record every quantity change to
  Inventory as an immutable `InventoryMovement` with a non-zero quantity and
  a movement type.

### Orders (ShoppingCart, Order)

- **FR-016** (Ubiquitous): The system shall require at least one line item
  on any Order.
- **FR-017** (Event-driven): WHEN an Order transitions between statuses,
  the system shall permit only the exact sequence `CART` →
  `PENDING_PAYMENT` → `PAID` → `SHIPPED` → `DELIVERED_COMPLETED`, one step
  at a time.
- **FR-018** (State-driven): WHILE an Order's status is
  `DELIVERED_COMPLETED`, the system shall reject any further status
  transition or modification, under any circumstance.
- **FR-019** (Ubiquitous): The system shall compute an Order's total as the
  sum of each line item's quantity multiplied by its recorded unit price.

### Billing & Fulfillment (Invoice, Shipment)

- **FR-020** (Ubiquitous): The system shall compute an Invoice's total as
  the sum of its subtotal and its tax.
- **FR-021** (Unwanted behavior): IF a Shipment is created with a user who
  does not hold the `LOGISTICS_OPERATOR` role, THEN the system shall reject
  the creation.
- **FR-022** (Unwanted behavior): IF delivery is confirmed on a Shipment
  that has not been dispatched, THEN the system shall reject the
  confirmation.

### Returns & Refunds (ReturnRequest, Refund)

- **FR-023** (Ubiquitous): The system shall initialize every ReturnRequest
  in status `REQUESTED`.
- **FR-024** (Unwanted behavior): IF `approve()` or `reject()` is called on
  a ReturnRequest whose status is not `REQUESTED`, THEN the system shall
  reject the call.
- **FR-025** (Unwanted behavior): IF `complete()` is called on a
  ReturnRequest whose status is not `APPROVED`, THEN the system shall reject
  the call.
- **FR-026** (Unwanted behavior): IF a Refund is created from a
  ReturnRequest whose status is neither `APPROVED` nor `COMPLETED`, THEN the
  system shall reject the creation.

### Cross-Cutting (Value Objects)

- **FR-027** (Ubiquitous): The system shall reject any `Money` amount that
  is negative.
- **FR-028** (Unwanted behavior): IF an arithmetic operation is attempted
  between two `Money` values with different currencies, THEN the system
  shall reject the operation.
- **FR-029** (Ubiquitous): The system shall accept only syntactically valid
  addresses for `Email` and shall require every non-optional field of
  `Address` and `IdentityDocument` to be non-blank.

## Key Entities

See `data-model.md` for full attribute lists, relationships, and validation
rules. Summary: 13 entities (`User`, `Buyer`, `Seller`, `Warehouse`,
`Product`, `Inventory`, `InventoryMovement`, `ShoppingCart`, `Order`,
`Invoice`, `Shipment`, `ReturnRequest`, `Refund`), 6 value objects
(`Address`, `Money`, `Email`, `IdentityDocument`, `ProductVariant`,
`OrderItem`), 11 enumerations, and 1 domain service
(`SellerOnboardingService`).

## Review Checklist

- [x] Every requirement is written in EARS and is testable.
- [x] No `[NEEDS CLARIFICATION]` markers remain.
- [x] Every requirement maps to at least one task in `tasks.md`.
- [x] Scope is limited to the domain layer — no API, persistence, or UI
      requirements are included here (out of scope for this feature).
