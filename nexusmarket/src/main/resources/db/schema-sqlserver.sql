-- =============================================================================
-- NexusMarket - Esquema Relacional Idempotente para Microsoft SQL Server
-- Ejecutado de forma segura sin eliminar ni truncar tablas existentes
-- =============================================================================

IF OBJECT_ID('dbo.users', 'U') IS NULL
CREATE TABLE dbo.users (
    user_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    role VARCHAR(31) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL CONSTRAINT uq_users_email UNIQUE,
    identity_document NVARCHAR(255) NOT NULL CONSTRAINT uq_users_identity_document UNIQUE,
    status VARCHAR(30) NOT NULL,
    password_hash NVARCHAR(255) NULL,
    primary_street NVARCHAR(255) NULL,
    primary_city NVARCHAR(255) NULL,
    primary_state_or_province NVARCHAR(255) NULL,
    primary_country NVARCHAR(255) NULL,
    primary_postal_code NVARCHAR(100) NULL,
    primary_reference NVARCHAR(255) NULL,
    commercial_status VARCHAR(30) NULL,
    onboarding_date DATE NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL
    AND COL_LENGTH('dbo.users', 'password_hash') IS NULL
    ALTER TABLE dbo.users ADD password_hash NVARCHAR(255) NULL;

IF OBJECT_ID('dbo.buyer_secondary_addresses', 'U') IS NULL
CREATE TABLE dbo.buyer_secondary_addresses (
    user_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_buyer_sec_addr_user FOREIGN KEY REFERENCES dbo.users(user_id),
    street NVARCHAR(255) NOT NULL,
    city NVARCHAR(255) NOT NULL,
    state_or_province NVARCHAR(255) NOT NULL,
    country NVARCHAR(255) NOT NULL,
    postal_code NVARCHAR(100) NOT NULL,
    reference NVARCHAR(255) NULL
);

IF OBJECT_ID('dbo.products', 'U') IS NULL
CREATE TABLE dbo.products (
    product_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    seller_id UNIQUEIDENTIFIER NULL CONSTRAINT fk_products_seller FOREIGN KEY REFERENCES dbo.users(user_id),
    version BIGINT NULL,
    name NVARCHAR(150) NOT NULL,
    variant_attribute NVARCHAR(100) NOT NULL,
    variant_value NVARCHAR(100) NOT NULL,
    price_amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    product_type VARCHAR(30) NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);
IF OBJECT_ID('dbo.products', 'U') IS NOT NULL
    AND COL_LENGTH('dbo.products', 'seller_id') IS NULL
    ALTER TABLE dbo.products ADD seller_id UNIQUEIDENTIFIER NULL CONSTRAINT fk_products_seller FOREIGN KEY REFERENCES dbo.users(user_id);

IF OBJECT_ID('dbo.product_variants', 'U') IS NULL
CREATE TABLE dbo.product_variants (
    product_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_product_variants_product FOREIGN KEY REFERENCES dbo.products(product_id),
    variant_attribute NVARCHAR(100) NULL,
    variant_value NVARCHAR(100) NULL,
    sku NVARCHAR(100) NULL
);

IF OBJECT_ID('dbo.warehouses', 'U') IS NULL
CREATE TABLE dbo.warehouses (
    warehouse_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    street NVARCHAR(255) NOT NULL,
    city NVARCHAR(255) NOT NULL,
    state_or_province NVARCHAR(255) NOT NULL,
    country NVARCHAR(255) NOT NULL,
    postal_code NVARCHAR(100) NOT NULL,
    reference NVARCHAR(255) NULL,
    type VARCHAR(30) NOT NULL,
    seller_id UNIQUEIDENTIFIER NULL,
    capacity FLOAT NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.inventories', 'U') IS NULL
CREATE TABLE dbo.inventories (
    inventory_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    version BIGINT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_inventories_product FOREIGN KEY REFERENCES dbo.products(product_id),
    warehouse_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_inventories_warehouse FOREIGN KEY REFERENCES dbo.warehouses(warehouse_id),
    available_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT uq_inventory_product_warehouse UNIQUE (product_id, warehouse_id)
);

IF OBJECT_ID('dbo.inventory_movements', 'U') IS NULL
CREATE TABLE dbo.inventory_movements (
    movement_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    inventory_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_inv_movements_inventory FOREIGN KEY REFERENCES dbo.inventories(inventory_id),
    type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    date DATETIME2 NOT NULL,
    reference NVARCHAR(255) NULL
);

IF OBJECT_ID('dbo.shopping_carts', 'U') IS NULL
CREATE TABLE dbo.shopping_carts (
    cart_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    buyer_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT uq_shopping_carts_buyer UNIQUE CONSTRAINT fk_shopping_carts_buyer FOREIGN KEY REFERENCES dbo.users(user_id),
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.cart_items', 'U') IS NULL
CREATE TABLE dbo.cart_items (
    item_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    cart_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_cart_items_cart FOREIGN KEY REFERENCES dbo.shopping_carts(cart_id),
    product_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_cart_items_product FOREIGN KEY REFERENCES dbo.products(product_id),
    quantity INT NOT NULL,
    unit_price_amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL
);

IF OBJECT_ID('dbo.orders', 'U') IS NULL
CREATE TABLE dbo.orders (
    order_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    buyer_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_orders_buyer FOREIGN KEY REFERENCES dbo.users(user_id),
    delivery_street NVARCHAR(255) NOT NULL,
    delivery_city NVARCHAR(255) NOT NULL,
    delivery_state_or_province NVARCHAR(255) NOT NULL,
    delivery_country NVARCHAR(255) NOT NULL,
    delivery_postal_code NVARCHAR(100) NOT NULL,
    delivery_reference NVARCHAR(255) NULL,
    status VARCHAR(30) NOT NULL,
    creation_date DATE NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.order_items', 'U') IS NULL
CREATE TABLE dbo.order_items (
    order_item_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    order_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_order_items_order FOREIGN KEY REFERENCES dbo.orders(order_id),
    product_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_order_items_product FOREIGN KEY REFERENCES dbo.products(product_id),
    quantity INT NOT NULL,
    unit_price_amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL
);

IF OBJECT_ID('dbo.invoices', 'U') IS NULL
CREATE TABLE dbo.invoices (
    invoice_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    order_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT uq_invoices_order UNIQUE CONSTRAINT fk_invoices_order FOREIGN KEY REFERENCES dbo.orders(order_id),
    issue_date DATE NOT NULL,
    subtotal_amount DECIMAL(19, 4) NOT NULL,
    tax_amount DECIMAL(19, 4) NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.shipments', 'U') IS NULL
CREATE TABLE dbo.shipments (
    shipment_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    order_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT uq_shipments_order UNIQUE CONSTRAINT fk_shipments_order FOREIGN KEY REFERENCES dbo.orders(order_id),
    operator_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_shipments_operator FOREIGN KEY REFERENCES dbo.users(user_id),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.return_requests', 'U') IS NULL
CREATE TABLE dbo.return_requests (
    request_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    order_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT fk_return_requests_order FOREIGN KEY REFERENCES dbo.orders(order_id),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.refunds', 'U') IS NULL
CREATE TABLE dbo.refunds (
    refund_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    return_request_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT uq_refunds_return_request UNIQUE CONSTRAINT fk_refunds_return_request FOREIGN KEY REFERENCES dbo.return_requests(request_id),
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL
);

IF OBJECT_ID('dbo.operations', 'U') IS NULL
CREATE TABLE dbo.operations (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    operation_type NVARCHAR(255) NOT NULL,
    description NVARCHAR(1000) NOT NULL,
    operator_id UNIQUEIDENTIFIER NULL,
    status VARCHAR(50) NOT NULL,
    payload NVARCHAR(MAX) NULL,
    created_at DATETIME2 NOT NULL
);
