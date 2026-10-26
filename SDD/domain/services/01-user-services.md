# Especificación de Servicios de Dominio: Contexto de Usuario

## 1. Servicios del Paquete `application.domain.services.user`

### 1.1 `AuthenticationAccessControlService`
- **Propósito:** Valida el acceso de usuarios activos al sistema.
- **Regla:** Solo usuarios con `UserStatus.ACTIVE` pueden autenticarse.

### 1.2 `UniqueIdentityValidationService`
- **Propósito:** Asegura la unicidad global de correo electrónico y documento de identidad.
- **Regla:** Lanza `IllegalArgumentException` si el email o documento ya está registrado.

### 1.3 `UserInformationManagementService`
- **Propósito:** Maneja la actualización de datos personales del perfil de usuario.

### 1.4 `UserOperationalStateService`
- **Propósito:** Controla transiciones de estado operacional (`ACTIVE`, `INACTIVE`, `SUSPENDED`).

### 1.5 `BuyerManagementService` y `BuyerCommercialStateControlService`
- **Propósito:** Registro y control del estado comercial (`ACTIVE`, `SUSPENDED`, `DELINQUENT`) del comprador.

### 1.6 `SellerAdministrativeRegistrationService`
- **Propósito:** Registro de vendedores que requieren autorización explícita de un Administrador (`approvedByAdminId`).

### 1.7 `PrimaryAddressManagementService` y `SecondaryAddressManagementService`
- **Propósito:** Gestión de la dirección principal de entrega y libreta de direcciones opcionales.
