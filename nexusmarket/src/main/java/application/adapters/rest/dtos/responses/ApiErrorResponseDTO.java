package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.time.Instant;
import java.util.Map;

/**
 * Standard RFC-compliant API error response body.
 */
public record ApiErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
) {
    public static ApiErrorResponseDTO of(int status, String error, String message, String path) {
        return new ApiErrorResponseDTO(Instant.now(), status, error, message, path, null);
    }

    public static ApiErrorResponseDTO of(int status, String error, String message, String path, Map<String, String> details) {
        return new ApiErrorResponseDTO(Instant.now(), status, error, message, path, details);
    }
}
