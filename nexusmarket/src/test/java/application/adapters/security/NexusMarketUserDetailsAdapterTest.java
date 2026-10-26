package application.adapters.security;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NexusMarketUserDetailsAdapterTest {

    @Mock
    private UserRepositoryPort userRepository;

    private NexusMarketUserDetailsAdapter userDetailsAdapter;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userDetailsAdapter = new NexusMarketUserDetailsAdapter(userRepository);
    }

    @Test
    @DisplayName("isAuthorized should return false when parameters are null")
    void shouldReturnFalseWhenParamsNull() {
        assertFalse(userDetailsAdapter.isAuthorized(null, UserRole.ADMINISTRATOR));
        assertFalse(userDetailsAdapter.isAuthorized(userId, null));
    }

    @Test
    @DisplayName("isAuthorized should return false when user is not found")
    void shouldReturnFalseWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertFalse(userDetailsAdapter.isAuthorized(userId, UserRole.BUYER));
    }

    @Test
    @DisplayName("isAuthorized should return false when user is not active")
    void shouldReturnFalseWhenUserInactive() {
        User user = User.createStaff("Inactive User", new Email("inactive@test.com"),
                new IdentityDocument("CC", "12345"), UserRole.LOGISTICS_OPERATOR);
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertFalse(userDetailsAdapter.isAuthorized(userId, UserRole.LOGISTICS_OPERATOR));
    }

    @Test
    @DisplayName("isAuthorized should return true when user role matches")
    void shouldReturnTrueWhenRoleMatches() {
        User user = User.createStaff("Staff User", new Email("staff@test.com"),
                new IdentityDocument("CC", "12345"), UserRole.LOGISTICS_OPERATOR);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertTrue(userDetailsAdapter.isAuthorized(userId, UserRole.LOGISTICS_OPERATOR));
    }

    @Test
    @DisplayName("isAuthorized should return true for administrator regardless of required role")
    void shouldReturnTrueForAdmin() {
        User admin = User.createStaff("Admin User", new Email("admin@test.com"),
                new IdentityDocument("CC", "99999"), UserRole.ADMINISTRATOR);
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        assertTrue(userDetailsAdapter.isAuthorized(userId, UserRole.BUYER));
        assertTrue(userDetailsAdapter.isAuthorized(userId, UserRole.SELLER));
        assertTrue(userDetailsAdapter.isAuthorized(userId, UserRole.LOGISTICS_OPERATOR));
    }

    @Test
    @DisplayName("hasPermission should return true for active user")
    void shouldReturnTrueForActiveUserPermission() {
        User user = User.createStaff("User", new Email("user@test.com"),
                new IdentityDocument("CC", "12345"), UserRole.LOGISTICS_OPERATOR);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertTrue(userDetailsAdapter.hasPermission(userId, "READ_ORDER"));
    }

    @Test
    @DisplayName("hasPermission should return false for null input or inactive user")
    void shouldReturnFalseForNullOrInactiveUserPermission() {
        assertFalse(userDetailsAdapter.hasPermission(null, "READ"));
        assertFalse(userDetailsAdapter.hasPermission(userId, null));

        User user = User.createStaff("User", new Email("user@test.com"),
                new IdentityDocument("CC", "12345"), UserRole.SUPERVISOR);
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertFalse(userDetailsAdapter.hasPermission(userId, "READ"));
    }
}
