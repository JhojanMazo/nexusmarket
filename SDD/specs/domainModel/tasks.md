# Tasks: NexusMarket Domain Model

**Input**: `plan.md`, `data-model.md` in this directory
**Convention**: `[X]` = implemented and present in `src/`. `[ ]` = open.
`[P]` = safe to do in parallel (touches a different file, no dependency on
another unfinished task in this list). Each task cites the FR-xxx it
satisfies (Constitution Article VII).

## Phase 3.1 — Setup

- [X] **T001** Create Maven project skeleton — `pom.xml` (Java 17, no
      external dependencies)

## Phase 3.2 — Enumerations `enums/` (no dependencies, all parallel)

- [X] **T002** `[P]` UserRole — `enums/UserRole.java` (FR-001)
- [X] **T003** `[P]` UserStatus — `enums/UserStatus.java`
- [X] **T004** `[P]` BuyerCommercialStatus — `enums/BuyerCommercialStatus.java` (FR-003)
- [X] **T005** `[P]` WarehouseType — `enums/WarehouseType.java` (FR-007)
- [X] **T006** `[P]` ProductType — `enums/ProductType.java` (FR-008)
- [X] **T007** `[P]` ProductStatus — `enums/ProductStatus.java` (FR-009)
- [X] **T008** `[P]` InventoryStatus — `enums/InventoryStatus.java` (FR-011)
- [X] **T009** `[P]` InventoryMovementType — `enums/InventoryMovementType.java` (FR-015)
- [X] **T010** `[P]` OrderStatus — `enums/OrderStatus.java` (FR-017, FR-018)
- [X] **T011** `[P]` ReturnStatus — `enums/ReturnStatus.java` (FR-023, FR-024, FR-025)
- [X] **T012** `[P]` RefundStatus — `enums/RefundStatus.java` (FR-026)

## Phase 3.3 — Foundational Value Objects `valueobject/` (depend only on enums)

- [X] **T013** `[P]` Address — `valueobject/Address.java` (FR-029)
- [X] **T014** `[P]` Money — `valueobject/Money.java` (FR-027, FR-028)
- [X] **T015** `[P]` Email — `valueobject/Email.java` (FR-029)
- [X] **T016** `[P]` IdentityDocument — `valueobject/IdentityDocument.java` (FR-029)
- [X] **T017** `[P]` ProductVariant — `valueobject/ProductVariant.java`

## Phase 3.4 — User Hierarchy (Constitution Article V / Decision D-001)

- [X] **T018** User (concrete base + `createStaff`) — `model/User.java` (FR-001, FR-002)
- [X] **T019** `[P]` Buyer extends User — `model/Buyer.java` (FR-003, FR-004) — depends on T018
- [X] **T020** `[P]` Seller extends User — `model/Seller.java` (FR-005) — depends on T018

## Phase 3.5 — Catalog & Warehousing

- [X] **T021** Warehouse — `model/Warehouse.java` (FR-007) — depends on T020, T013
- [X] **T022** Product — `model/Product.java` (FR-008, FR-009) — depends on T020, T014, T017

## Phase 3.6 — Inventory

- [X] **T023** Inventory — `model/Inventory.java` (FR-010, FR-011, FR-012, FR-013) — depends on T021, T022
- [X] **T024** InventoryMovement — `model/InventoryMovement.java` (FR-015) — depends on T023

## Phase 3.7 — Ordering

- [X] **T025** OrderItem (value object) — `valueobject/OrderItem.java` (FR-016, FR-019) — depends on T022, T014
- [X] **T026** `[P]` ShoppingCart — `model/ShoppingCart.java` — depends on T019, T025
- [X] **T027** `[P]` Order — `model/Order.java` (FR-016, FR-017, FR-018, FR-019) — depends on T019, T025, T013

## Phase 3.8 — Billing & Fulfillment

- [X] **T028** `[P]` Invoice — `model/Invoice.java` (FR-020) — depends on T027, T014
- [X] **T029** `[P]` Shipment — `model/Shipment.java` (FR-021, FR-022) — depends on T027, T018, T013

## Phase 3.9 — Returns & Refunds

- [X] **T030** ReturnRequest — `model/ReturnRequest.java` (FR-023, FR-024, FR-025) — depends on T027
- [X] **T031** Refund — `model/Refund.java` (FR-026) — depends on T030, T014

## Phase 3.10 — Domain Services

- [X] **T032** SellerOnboardingService — `service/SellerOnboardingService.java` (FR-006) — depends on T018, T020, T021

## Phase 3.11 — Polish & Verification (open)

- [ ] **T033** JUnit 5 suite covering FR-001–FR-029, one test class per
      aggregate — not started
- [ ] **T034** Property-style tests for `Money` arithmetic and `Inventory`
      quantity invariants (FR-010, FR-027, FR-028) — not started
- [ ] **T035** CI check enforcing Constitution Article VII: every `FR-xxx`
      in `spec.md` has a corresponding reference in code/tests — not started

## Coverage Check

29 functional requirements (FR-001–FR-029) in `spec.md`; every one above is
referenced by at least one completed task (T002–T032). No orphaned
requirement and no undocumented task.
