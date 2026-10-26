package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.services.authorization.RolePermissionManagementService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionManagementServiceTest {

    private RolePermissionManagementService service;
    private User adminUser;

    @BeforeEach
    void setUp() {
        service = new RolePermissionManagementService();
        adminUser = User.createStaff("Admin Sample", new Email("admin@nexusmarket.com"), new IdentityDocument("CC", "1234"), UserRole.ADMINISTRATOR);
    }

    @Test
    void shouldPassWhenUserHasRequiredRole() {
        assertDoesNotThrow(() -> service.validateActionPermission(adminUser, UserRole.ADMINISTRATOR));
    }

    @Test
    void shouldThrowSecurityExceptionWhenRoleMismatches() {
        SecurityException ex = assertThrows(SecurityException.class, () ->
                service.validateActionPermission(adminUser, UserRole.LOGISTICS_OPERATOR)
        );
        assertTrue(ex.getMessage().contains("Permiso denegado"));
    }

    @Test
    void shouldThrowWhenArgumentsAreNull() {
        assertThrows(NullPointerException.class, () -> service.validateActionPermission(null, UserRole.ADMINISTRATOR));
        assertThrows(NullPointerException.class, () -> service.validateActionPermission(adminUser, null));
    }
}
