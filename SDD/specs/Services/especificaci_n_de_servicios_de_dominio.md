# Software Design Document (SDD) - Domain Services Specification

## Overview
This document details the specifications for the **30 Domain Services** within `nexusmarket`. Each domain service encapsulates business logic requiring coordination across domain entities (`User`, `Buyer`, `Seller`, `Product`, `Warehouse`, `Inventory`, `Order`, `Invoice`, `Cart`, etc.) following Domain-Driven Design (DDD) principles.

---

## 1. User Management & Access Control Services

### 1.1 `UserInformationManagementService`
* **Purpose:** Handles end-to-end registration and updating of platform user profiles.
* **Responsibilities:**
  * Coordinates unique identity verification before user creation.
  * Persists updated user information via `UserRepository`.
* **Primary Methods:**
  * `registerNewUser(User user): User`
  * `updateUserInfo(User user): User`
* **Business Rules:**
  * Identity document and email must be unique across the platform.

### 1.2 `AuthenticationAccessControlService`
* **Purpose:** Manages secure credential authentication and initial operational checks.
* **Responsibilities:**
  * Verifies email presence and password hash matching.
  * Ensures account operational status is active prior to granting access.
* **Primary Methods:**
  * `authenticate(String email, String rawPassword): User`
* **Exceptions:** `IllegalArgumentException` (Invalid credentials), `IllegalStateException` (Inactive account).

### 1.3 `RolePermissionManagementService`
* **Purpose:** Enforces RBAC (Role-Based Access Control) policies.
* **Responsibilities:**
  * Ensures each user possesses a single assigned `Role`.
  * Validates action permissions before privileged operation execution.
* **Primary Methods:**
  * `assignRoleToUser(User user, Role newRole): void`
  * `validateActionPermission(User user, String requiredAction): void`

### 1.4 `UserOperationalStateService`
* **Purpose:** Controls user account lifecycle and state transitions.
* **Responsibilities:**
  * Blocks accounts with reason tracking.
  * Restores blocked accounts to active status.
* **Primary Methods:**
  * `blockUser(User user, String reason): void`
  * `activateUser(User user): void`

### 1.5 `UniqueIdentityValidationService`
* **Purpose:** Validates global platform uniqueness constraints for identity attributes.
* **Responsibilities:**
  * Asserts non-existence of email address in `UserRepository`.
  * Asserts non-existence of national identity document in `UserRepository`.
* **Primary Methods:**
  * `validateUniqueIdentity(String identityDocument, String email): void`

---

## 2. Buyer & Seller Administration Services

### 2.1 `SellerAdministrativeRegistrationService`
* **Purpose:** Manages administrative onboarding of Marketplace Sellers.
* **Responsibilities:**
  * Verifies administrator credentials and registration rights.
  * Sets initial seller status to `PENDING_APPROVAL`.
* **Primary Methods:**
  * `registerSeller(Admin admin, Seller newSeller): Seller`

### 2.2 `BuyerManagementService`
* **Purpose:** Manages buyer commercial records and profiles.
* **Responsibilities:**
  * Updates tax, commercial, and profile metadata for registered buyers.
* **Primary Methods:**
  * `updateCommercialInfo(Buyer buyer, String commercialData): void`

### 2.3 `PrimaryAddressManagementService`
* **Purpose:** Controls default/primary delivery location for buyers.
* **Responsibilities:**
  * Validates structure and completeness of primary shipping addresses.
* **Primary Methods:**
  * `setPrimaryAddress(Buyer buyer, Address address): void`

### 2.4 `SecondaryAddressManagementService`
* **Purpose:** Manages auxiliary delivery addresses.
* **Responsibilities:**
  * Appends validated secondary addresses to the buyer's address collection.
* **Primary Methods:**
  * `addSecondaryAddress(Buyer buyer, Address address): void`

### 2.5 `BuyerCommercialStateControlService`
* **Purpose:** Evaluates commercial transaction eligibility.
* **Responsibilities:**
  * Verifies primary address and payment method presence.
  * Toggles `canTransact` flag accordingly.
* **Primary Methods:**
  * `verifyAndEnableTransactions(Buyer buyer): void`

---

## 3. Warehouse & Product Catalog Services

### 3.1 `WarehouseInformationControlService`
* **Purpose:** Manages physical warehouse properties and parameters.
* **Responsibilities:**
  * Updates total storage capacity ensuring non-negative values.
* **Primary Methods:**
  * `updateWarehouseCapacity(Warehouse warehouse, double capacity): void`

### 3.2 `WarehouseClassificationService`
* **Purpose:** Segregates storage facilities by ownership model.
* **Responsibilities:**
  * Classifies warehouses as either `MARKETPLACE` owned or `SELLER` owned.
* **Primary Methods:**
  * `classifyWarehouse(Warehouse warehouse, String type): void`

### 3.3 `ProductCatalogManagementService`
* **Purpose:** Governs creation and basic attributes of catalog products.
* **Responsibilities:**
  * Instantiates new catalog entries ensuring mandatory attributes exist.
* **Primary Methods:**
  * `createProduct(String name, String description): Product`

### 3.4 `ProductTypeDifferentiationService`
* **Purpose:** Customizes workflow handling based on product nature.
* **Responsibilities:**
  * Configures shipping requirements (`requiresShipping = false` for digital goods).
* **Primary Methods:**
  * `configureProductShipping(Product product): void`

### 3.5 `ProductVariantManagementService`
* **Purpose:** Manages product SKUs/variants (size, color, specifications).
* **Responsibilities:**
  * Binds new variants to parent products.
* **Primary Methods:**
  * `addVariant(Product product, Variant variant): void`

---

## 4. Inventory Management Services

### 4.1 `ProductStateControlService`
* **Purpose:** Controls product catalog visibility state transitions.
* **Responsibilities:**
  * Transition states: `PUBLISHED`, `SUSPENDED`, `DISCONTINUED`.
* **Primary Methods:**
  * `publishProduct(Product product): void`
  * `suspendProduct(Product product): void`
  * `discontinueProduct(Product product): void`

### 4.2 `DistributedInventoryManagementService`
* **Purpose:** Links catalog items with physical storage facilities.
* **Responsibilities:**
  * Establishes mandatory mapping between `Product` and `Warehouse`.
* **Primary Methods:**
  * `linkInventory(Product product, Warehouse warehouse): Inventory`

### 4.3 `InventoryEntryRegistrationService`
* **Purpose:** Records inbound stock additions.
* **Responsibilities:**
  * Increases physical stock counts following inbound shipments.
* **Primary Methods:**
  * `registerEntry(Inventory inventory, int quantity): void`

### 4.4 `InventoryReservationService`
* **Purpose:** Temporarily locks stock during checkout workflows.
* **Responsibilities:**
  * Holds inventory to prevent overselling.
* **Primary Methods:**
  * `reserve(Inventory inventory, int quantity): void`

### 4.5 `SalesExitRegistrationService`
* **Purpose:** Commits stock reductions upon confirmed purchases.
* **Responsibilities:**
  * Converts reserved inventory into finalized sales deduction.
* **Primary Methods:**
  * `registerExit(Inventory inventory, int quantity): void`

---

## 5. Stock Validation & Order Processing Services

### 5.1 `InventoryAdjustmentsReturnsService`
* **Purpose:** Handles physical stock reconciliations and return integrations.
* **Responsibilities:**
  * Restores stock levels for returned items or manual inventory audits.
* **Primary Methods:**
  * `processReturn(Inventory inventory, int quantity): void`
  * `adjustStock(Inventory inventory, int realQuantity): void`

### 5.2 `CriticalStockValidationService`
* **Purpose:** Prevents negative inventory and protects critical thresholds.
* **Responsibilities:**
  * Blocks checkout when available stock is insufficient.
* **Primary Methods:**
  * `validateStock(Inventory inventory, int requestedQuantity): void`

### 5.3 `ShoppingCartManagementService`
* **Purpose:** Manages pre-checkout buyer carts.
* **Responsibilities:**
  * Adds items and quantities to buyer sessions.
* **Primary Methods:**
  * `addItem(Cart cart, Product product, int quantity): void`

### 5.4 `OrderStatusMonitoringService`
* **Purpose:** Coordinates order lifecycle status transitions.
* **Responsibilities:**
  * Updates statuses across state machine transitions.
* **Primary Methods:**
  * `updateStatus(Order order, String newStatus): void`

### 5.5 `CompletedOrderImmutabilityService`
* **Purpose:** Guarantees audit compliance and data integrity for finalized orders.
* **Responsibilities:**
  * Immutably locks orders in `COMPLETED` status.
* **Primary Methods:**
  * `ensureImmutability(Order order): void`

---

## 6. Billing, Logistics & Operational Reporting Services

### 6.1 `BillingAdministrationService`
* **Purpose:** Manages tax and commercial invoicing.
* **Responsibilities:**
  * Generates `Invoice` entities for paid orders.
* **Primary Methods:**
  * `generateInvoice(Order order): Invoice`

### 6.2 `LogisticsShippingManagementService`
* **Purpose:** Coordinates physical fulfillment with logistics operators.
* **Responsibilities:**
  * Transitions order state to `DISPATCHED` and triggers dispatch routines.
* **Primary Methods:**
  * `coordinateShipping(Order order): void`

### 6.3 `ReturnsAdministrationService`
* **Purpose:** Manages reverse logistics workflows.
* **Responsibilities:**
  * Initiates return processes for delivered orders.
* **Primary Methods:**
  * `initiateReturn(Order order): void`

### 6.4 `RefundsManagementService`
* **Purpose:** Governs financial compensation upon return receipt.
* **Responsibilities:**
  * Validates return physical processing before setting state to `REFUNDED`.
* **Primary Methods:**
  * `processRefund(Order order): void`

### 6.5 `AdministrativeReportsConsolidationService`
* **Purpose:** Aggregates platform operational telemetry for reporting.
* **Responsibilities:**
  * Compiles cross-domain metrics for administrative consumption.
* **Primary Methods:**
  * `generatePlatformReport(): String`