package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.ports.out.AuthorizationPort;
import application.domain.services.authorization.AuthorizeActionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizeActionServiceTest {

    @Mock
    private AuthorizationPort authorizationPort;

    private AuthorizeActionService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizeActionService(authorizationPort);
    }

    @Test
    void shouldPassWhenUserIsAuthorizedForRole() {
        UUID userId = UUID.randomUUID();
        when(authorizationPort.isAuthorized(userId, UserRole.ADMINISTRATOR)).thenReturn(true);

        assertDoesNotThrow(() -> service.validateRole(userId, UserRole.ADMINISTRATOR));
        verify(authorizationPort).isAuthorized(userId, UserRole.ADMINISTRATOR);
    }

    @Test
    void shouldThrowWhenUserIsNotAuthorizedForRole() {
        UUID userId = UUID.randomUUID();
        when(authorizationPort.isAuthorized(userId, UserRole.ADMINISTRATOR)).thenReturn(false);

        UnauthorizedOperationException ex = assertThrows(UnauthorizedOperationException.class, () ->
                service.validateRole(userId, UserRole.ADMINISTRATOR)
        );
        assertTrue(ex.getMessage().contains("does not hold required role"));
    }

    @Test
    void shouldPassWhenUserHasPermission() {
        UUID userId = UUID.randomUUID();
        when(authorizationPort.hasPermission(userId, "CATALOG_WRITE")).thenReturn(true);

        assertDoesNotThrow(() -> service.validatePermission(userId, "CATALOG_WRITE"));
    }

    @Test
    void shouldThrowWhenUserLacksPermission() {
        UUID userId = UUID.randomUUID();
        when(authorizationPort.hasPermission(userId, "CATALOG_WRITE")).thenReturn(false);

        assertThrows(UnauthorizedOperationException.class, () ->
                service.validatePermission(userId, "CATALOG_WRITE")
        );
    }
}
