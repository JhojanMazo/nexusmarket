# Implementation Plan: NexusMarket Domain Model

**Spec**: `spec.md` in this directory | **Constitution**: `.specify/memory/constitution.md` v1.0.0

## Technical Context

- **Language**: Java 17
- **Build**: Maven (`pom.xml`, no external runtime dependencies)
- **Testing**: JUnit 5 (not yet wired — see `tasks.md`, Phase 6)
- **Architecture style**: Tactical DDD — Entities, Value Objects, a single
  Domain Service; no application or infrastructure layer in this feature
- **Project type**: Single library (`com.nexusmarket.domain`)

## Constitution Check

| Article | Check | Result |
|---|---|---|
| I — Ubiquitous Language | Class names cross-checked against the Business Functional Specification glossary | PASS |
| II — Domain Purity | No persistence/web/messaging imports in `src/main/java` | PASS |
| III — Value Objects | All 6 VOs validate in-constructor, override `equals`/`hashCode`, no setters | PASS |
| IV — Entities Own Their Invariants | Quantity, status, and role checks live on the entity, not callers | PASS |
| V — Role Is Not Automatically an Entity | See Decision D-001 below | PASS (after revision) |
| VI — Cross-Aggregate Rules in Services | `SellerOnboardingService` is the only cross-aggregate authorization rule | PASS |
| VII — Spec-to-Code Traceability | Every FR-xxx has a task in `tasks.md` | PASS |

No violations requiring a constitution amendment or an explicit exception.

## Project Structure

```
src/main/java/com/nexusmarket/domain/
├── enums/          11 files — closed value sets used as discriminators
├── valueobject/     6 files — immutable, structurally-compared
├── model/          13 files — entities, identity-compared
└── service/         1 file  — cross-aggregate authorization rules
```

Package `valueobject` depends on `model` only for `OrderItem → Product`
(a line item must reference a real product). No other dependency crosses
from `valueobject` into `model`. `model` depends on `enums` and
`valueobject`. `service` depends on `model` and `enums`. `enums` has no
dependencies. This is a strict DAG; there are no cycles.

## Decision Log

### D-001: Which `User` specializations deserve their own class?

**Context.** The initial pass at this model gave every one of the five
roles from the Business Functional Specification (`Buyer`, `Seller`,
`LogisticsOperator`, `Administrator`, `Supervisor`) its own subclass of
`User`, mirroring the specification's role list one-to-one.

**Problem found in review.** `LogisticsOperator`, `Administrator`, and
`Supervisor` added no field, no invariant, and no behavior beyond what
`User` plus a `UserRole` value already provides. Their only function was to
exist as a distinct compile-time type — e.g. so `Shipment` could declare a
field of type `LogisticsOperator` instead of `User`, and so `Administrator`
could host an `onboardSeller(...)` method. `Buyer` and `Seller`, by
contrast, each hold required fields with their own validation (a primary
address and commercial status; a trade name, onboarding date, and
warehouse list) that `User` cannot express.

**Decision.** Keep `Buyer` and `Seller` as subclasses. Remove
`LogisticsOperator`, `Administrator`, and `Supervisor`. Make `User` concrete
and add `User.createStaff(role, ...)`, restricted at runtime to those three
roles. `Shipment.logisticsOperator` becomes a plain `User` field, validated
in the constructor against `UserRole.LOGISTICS_OPERATOR`. The seller
onboarding rule moves from an `Administrator` instance method into
`SellerOnboardingService.onboard(actor, seller, warehouse)`, which checks
`actor.getRole() == ADMINISTRATOR` at runtime.

**Consequence, stated plainly.** This trades a compile-time guarantee for a
runtime check. Before, only an `Administrator` instance could call
`onboardSeller`; now, any `User` can be passed to
`SellerOnboardingService.onboard`, and an ineligible actor is caught by an
`IllegalStateException` rather than by the compiler. That trade-off is
accepted and captured as **Article V** of the constitution, so it is a
considered default for future roles, not a one-off shortcut: a role gets a
subclass only when it earns one by carrying real state.

**Alternatives considered.**
- *Composition instead of inheritance* (`User` holds an optional
  `BuyerProfile`/`SellerProfile`) — rejected for this pass; it solves a
  different problem (multiple simultaneous roles per user) that the
  specification does not require, since Business Rule BR-02 fixes exactly
  one role per user.
- *Keep all five subclasses* — rejected per the Problem above; three of
  them were pure ceremony.
- *A marker interface per staff role* (e.g. `LogisticsCapable`) — rejected
  as unnecessary indirection for a single field-type check that a runtime
  guard already covers.

## Phase Outputs

- Phase 0 (research): not required — no unresolved technical unknowns, no
  external integrations.
- Phase 1 (design): `data-model.md` in this directory.
- Phase 2 (tasks): `tasks.md` in this directory.
