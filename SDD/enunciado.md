# Enunciado del Proyecto: NexusMarket Enterprise Platform

## 1. Visión General del Sistema
**NexusMarket** es una plataforma de comercio electrónico empresarial construida bajo los paradigmas de **Arquitectura Hexagonal Estricta (Ports & Adapters)**, **Diseño Guiado por el Dominio (DDD)** y **Desarrollo Guiado por Especificaciones (SDD)**.

El sistema proporciona una solución distribuida y desacoplada para la comercialización de productos físicos y digitales, soportando la gestión de inventario multialmacén, ciclos de vida de pedidos transaccionales, facturación fiscal, control de envíos y devoluciones, y auditoría corporativa de alta seguridad.

---

## 2. Actores del Sistema y Roles de Negocio
El sistema modela 5 roles de usuario claramente diferenciados con políticas de control de acceso basadas en roles (RBAC):

1. **Buyer (Comprador):**
   - Registro y actualización de perfil personal y libreta de direcciones.
   - Exploración del catálogo de productos disponibles.
   - Creación y gestión de carritos de compra (`ShoppingCart`).
   - Generación de órdenes de compra y pago de transacciones.
   - Solicitudes de devolución y seguimiento de pedidos.

2. **Seller (Vendedor):**
   - Proceso de incorporación y aprobación administrativa.
   - Publicación y control de catálogo de productos y variantes.
   - Gestión de precios, descuentos y estados comerciales.

3. **Administrator (Administrador del Sistema):**
   - Gestión integral de usuarios (activación, suspensión, roles).
   - Aprobación formal de incorporación de vendedores.
   - Monitoreo global de operaciones y auditoría del sistema.
   - Configuración de políticas de negocio (impuestos, umbrales).

4. **Supervisor (Supervisor de Operaciones):**
   - Revisión y resolución de disputas comerciales y solicitudes de reembolso.
   - Control de excepciones de inventario y auditoría operativa.

5. **LogisticsOperator (Operador Logístico):**
   - Despacho y actualización del ciclo de vida de envíos (`Shipment`).
   - Recepción de devoluciones e inspección de mercancías devueltas.
   - Transferencias entre almacenes físicos (`Warehouse`).

---

## 3. Módulos Funcionales
1. **Gestión de Identidad y Acceso:**
   - Modelado de agregados `User`, `Buyer`, `Seller`.
   - Autenticación criptográfica mediante tokens JWT (HS256).
   - Hashing seguro de contraseñas con salt SHA-256.

2. **Catálogo de Productos y Variantes:**
   - Agregado `Product` con tipos `PHYSICAL` y `DIGITAL`.
   - Value Objects: `ProductVariant`, `Money`.
   - Control de estados: `PUBLISHED`, `SUSPENDED`, `DISCONTINUED`.

3. **Carro de Compras y Pedidos:**
   - Agregados `ShoppingCart` y `Order`.
   - Transición de estados de orden: `PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED` / `CANCELLED`.
   - Inmutabilidad estricta de órdenes completadas.

4. **Facturación y Pagos:**
   - Agregado `Invoice` con cálculo automático de impuestos y moneda.
   - Soporte para reembolsos (`Refund`) y notas contables.

5. **Inventario Distribuido y Almacenes:**
   - Agregados `Warehouse`, `Inventory`, `InventoryMovement`.
   - Validación de stock crítico y reserva transaccional contra sobreventa.

6. **Operaciones y Auditoría Empresarial:**
   - Agregados `Operation` (SQL/JPA) y `AuditLog` (MongoDB).
   - Doble registro desacoplado para trazabilidad forense.

---

## 4. Requisitos de Arquitectura y Tecnología
- **Java 17 LTS & Spring Boot 3+**
- **Cero dependencias del framework en el dominio:** El paquete `domain/` no depende de controladores ni librerías de persistencia.
- **Persistencia Políglota:** Base de datos relacional (MySQL / SQL Server / H2) para transacciones de negocio y MongoDB para bitácoras de auditoría.
- **Maven Wrapper (`./mvnw`) y Contenerización Docker Multi-stage.**
