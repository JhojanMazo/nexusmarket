# Manejador Global de Excepciones: NexusMarket

## 1. Visión General
El componente `GlobalExceptionHandler` (`application.adapters.rest.exception`) actúa como el interceptor centralizado (`@RestControllerAdvice`) para traducir excepciones del núcleo de dominio y errores HTTP a una estructura estandarizada `ApiErrorResponseDTO`.

---

## 2. Estructura de Respuesta de Error (`ApiErrorResponseDTO`)
```json
{
  "timestamp": "2026-10-25T17:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado con el identificador: 1a39225a-e30f-43c5-927b-09d55299f494",
  "path": "/api/products/1a39225a-e30f-43c5-927b-09d55299f494"
}
```

---

## 3. Matriz de Mapeo de Excepciones a Códigos HTTP

| Excepción de Dominio / Sistema | Código HTTP | Mensaje / Razón |
|---|---|---|
| `ResourceNotFoundException`, `EntityNotFoundException` | `404 Not Found` | El recurso solicitado no existe |
| `InvalidCredentialsException` | `401 Unauthorized` | Credenciales de acceso incorrectas o no autorizadas |
| `UnauthorizedOperationException` | `403 Forbidden` | El usuario no cuenta con el rol o permiso requerido |
| `IllegalArgumentException`, `DomainException` | `400 Bad Request` | Formato o parámetros de solicitud inválidos |
| `IllegalStateException`, `InvalidStatusTransitionException` | `409 Conflict` | Conflicto o transición de estado no permitida |
| `BusinessRuleException`, `InsufficientStockException` | `422 Unprocessable Entity` | Violación de reglas de negocio o falta de stock |
| `Exception` (cualquier otro error no capturado) | `500 Internal Server Error` | Error no controlado en el servidor |
