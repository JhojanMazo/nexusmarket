package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.User;
import application.domain.services.user.UserOperationalStateService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserOperationalStateServiceTest {

    private UserOperationalStateService service;
    private User testUser;

    @BeforeEach
    void setUp() {
        service = new UserOperationalStateService();
        testUser = User.createStaff("Test Operator", new Email("op@nexusmarket.com"), new IdentityDocument("CC", "12345"), UserRole.LOGISTICS_OPERATOR);
    }

    @Test
    void shouldBlockUserSuccessfullyWithReason() {
        service.blockUser(testUser, "Violación reiterada de políticas de despacho");
        assertEquals(UserStatus.BLOCKED, testUser.getStatus());
    }

    @Test
    void shouldThrowWhenBlockingWithoutReason() {
        assertThrows(IllegalArgumentException.class, () -> service.blockUser(testUser, ""));
        assertThrows(IllegalArgumentException.class, () -> service.blockUser(testUser, "   "));
        assertThrows(IllegalArgumentException.class, () -> service.blockUser(testUser, null));
    }

    @Test
    void shouldActivateUserSuccessfully() {
        testUser.setStatus(UserStatus.INACTIVE);
        service.activateUser(testUser);
        assertEquals(UserStatus.ACTIVE, testUser.getStatus());
    }

    @Test
    void shouldThrowWhenUserIsNull() {
        assertThrows(NullPointerException.class, () -> service.blockUser(null, "Motivo"));
        assertThrows(NullPointerException.class, () -> service.activateUser(null));
    }
}
