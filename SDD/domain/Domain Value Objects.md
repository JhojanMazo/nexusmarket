# Objetos de Valor (Value Objects): NexusMarket

## 1. Principios de los Value Objects
Ubicados en `application.domain.valueobjects`:
- Inmutabilidad total (definidos mediante `record` o clases con atributos `final`).
- Igualdad basada en valor estructural (`equals` y `hashCode` por campos).
- Validación de invariantes obligatoria en el constructor canónico.

---

## 2. Catálogo de Value Objects

### 2.1 `Money`
- Atributos: `BigDecimal amount`, `Currency currency`.
- Invariante: Monto mayor o igual a cero; moneda no nula. Operaciones aritméticas seguras (`add`, `multiply`).

### 2.2 `Address`
- Atributos: `street`, `city`, `state`, `country`, `postalCode`.
- Invariante: Calle, ciudad y país no pueden ser nulos ni vacíos.

### 2.3 `Email`
- Atributo: `String value`.
- Invariante: Validación por expresión regular `^[A-Za-z0-9+_.-]+@(.+)$`.

### 2.4 `IdentityDocument`
- Atributos: `String type` (CC, CE, NIT, PASAPORTE), `String number`.
- Invariante: Tipo y número no vacíos.

### 2.5 `ProductVariant`
- Atributos: `String attribute` (ej. Color, Talla, Almacenamiento), `String value` (ej. Rojo, XL, 512GB).

### 2.6 `OrderItem`
- Atributos: `Product product`, `int quantity`, `Money unitPrice`.
- Invariante: Cantidad $\ge 1$, precio unitario no negativo.

### 2.7 `DomainCatalog`
- Atributos: `String code`, `String category`, `String description`, `boolean active`.
- Clasificación de taxonomía empresarial para categorías de productos.
