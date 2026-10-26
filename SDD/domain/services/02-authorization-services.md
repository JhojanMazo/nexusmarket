# Especificación de Servicios de Dominio: Contexto de Autorización

## 1. Visión General del Paquete `application.domain.services.authorization`
Proporciona granularidad estricta para la validación de políticas de acceso, estados de cuenta, roles y propiedad sobre los recursos del dominio NexusMarket.

---

## 2. Catálogo Completo de Servicios de Autorización

| Servicio | Propósito y Regla de Negocio |
|---|---|
| `RolePermissionManagementService` | Evalúa correspondencia entre el rol del usuario (`UserRole`) y los requerimientos de la acción solicitada. |
| `AuthorizeActionService` | Delega la verificación de roles y permisos al puerto de salida `AuthorizationPort`, lanzando `UnauthorizedOperationException`. |
| `AuthorizeCustomerRegistrationService` | Valida que el registro público de compradores esté habilitado y que el dominio de correo no se encuentre bloqueado. |
| `AuthorizeUserRegistrationService` | Garantiza que únicamente usuarios con rol `ADMINISTRATOR` puedan dar de alta personal interno (Staff) o vendedores. |
| `AuthorizeChangeUserStatusService` | Exige privilegios de administrador para alterar estados de cuenta e impide que un administrador se inhabilite a sí mismo. |
| `AuthorizeProductOperationService` | Valida que la creación o modificación de productos en catálogo provenga de un `SELLER` o `ADMINISTRATOR`. |
| `AuthorizeInventoryOperationService` | Exige rol `LOGISTICS_OPERATOR` o `ADMINISTRATOR` para registrar movimientos, ajustes o reservas en almacenes. |
| `AuthorizeOrderOperationService` | Valida que las operaciones de modificación de pedido pertenezcan al comprador titular o al administrador. |
| `AuthorizeReturnApprovalService` | Restringe la aprobación de devoluciones y expedición de reembolsos exclusivamente a roles `SUPERVISOR` y `ADMINISTRATOR`. |
| `ValidateRoleAuthorizationService` | Comprueba de manera estricta que un objeto `User` contenga el `UserRole` requerido. |
| `ValidateUserAuthorizationStatusService` | Valida que la cuenta del usuario se encuentre en estado `UserStatus.ACTIVE` antes de permitir cualquier operación transaccional. |
| `ValidateBuyerOwnershipService` | Comprueba que el recurso (carrito, orden, devolución) pertenezca unívocamente al `buyerId` solicitante. |
| `ValidateSellerOwnershipService` | Comprueba que el catálogo o inventario corresponda al `sellerId` autenticado. |
| `ValidateSupervisorAuthorizationService` | Valida que el actor disponga del rol `SUPERVISOR` o `ADMINISTRATOR` para la gestión de disputas y auditoría. |
