# Validación de Datos en la Capa REST: NexusMarket

## 1. Estrategia de Validación en Dos Niveles
NexusMarket aplica validación defensiva en dos niveles complementarios:

1. **Nivel REST / DTO (Formato y Estructura Sintáctica):**
   - Validación de no nulidad y no vacíos en campos obligatorios (`name`, `email`, `currencyCode`).
   - Coherencia de tipos de datos primitivos y parseo seguro de identificadores UUID.
   - Si un DTO no cumple con los requerimientos estructurales mínimos, se dispara `IllegalArgumentException` traduciéndose inmediatamente a `400 Bad Request`.

2. **Nivel Dominio / Value Objects (Invariantes de Negocio Semánticas):**
   - Objetos de Valor encapsulan sus propias restricciones invariables:
     - `Email`: Verifica formato sintáctico de correo electrónico corporativo mediante expresión regular.
     - `Money`: Asegura monto no negativo y moneda estándar ISO 4217 no nula.
     - `IdentityDocument`: Exige tipo de documento y número no vacío.
     - `OrderItem`: Requiere cantidad entera mayor o igual a 1 y precio unitario válido.

---

## 2. Reglas de Validación por Entidad Clave

| Entidad / DTO | Campo | Restricción / Invariante | Consecuencia en Violación |
|---|---|---|---|
| `ProductRequestDTO` | `name` | No nulo, no en blanco | `400 Bad Request` |
| `ProductRequestDTO` | `price` | Mayor o igual a 0 | `400 Bad Request` |
| `ProductRequestDTO` | `currency` | Código ISO 4217 válido (ej. COP, USD) | `400 Bad Request` |
| `CreateCartRequestDTO` | `buyerId` | UUID válido | `400 Bad Request` |
| `AddCartItemRequestDTO` | `quantity` | Entero positivo $\ge 1$ | `400 Bad Request` |
| `CreateBuyerRequestDTO` | `email` | Formato regex de correo electrónico | `400 Bad Request` |
| `CreateStaffUserRequestDTO` | `role` | Rol válido (`ADMINISTRATOR`, etc.) | `400 Bad Request` |
