package application.adapters.useCases;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicAccessUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private JwtServicePort jwtServicePort;
    @Mock
    private PasswordServicePort passwordServicePort;
    @Mock
    private UniqueIdentityValidationService uniqueIdentityValidationService;
    @Mock
    private AuthorizeCustomerRegistrationService customerRegistrationService;

    private PublicAccessUseCaseImpl publicAccessUseCase;

    @BeforeEach
    void setUp() {
        publicAccessUseCase = new PublicAccessUseCaseImpl(
                userRepository,
                jwtServicePort,
                passwordServicePort,
                uniqueIdentityValidationService,
                customerRegistrationService
        );
    }

    @Test
    void authenticate_Success() {
        User user = User.createStaff("Staff One", new Email("staff@nexus.com"),
                new IdentityDocument("CC", "12345"), UserRole.ADMINISTRATOR, "hashed.secret");

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordServicePort.matches("Password123!", "hashed.secret")).thenReturn(true);
        when(jwtServicePort.generateToken(any(), any(), any())).thenReturn("mock.jwt.token");

        AuthenticationResult result = publicAccessUseCase.authenticate("staff@nexus.com", "Password123!");
        assertNotNull(result);
        assertEquals("mock.jwt.token", result.token());
        assertEquals("staff@nexus.com", result.email());
    }

    @Test
    void authenticate_Fails_WhenPasswordIsIncorrect() {
        User user = User.createStaff("Staff One", new Email("staff@nexus.com"),
                new IdentityDocument("CC", "12345"), UserRole.ADMINISTRATOR, "hashed.secret");

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordServicePort.matches("WrongPassword", "hashed.secret")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.authenticate("staff@nexus.com", "WrongPassword"));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void authenticate_Fails_WhenPasswordIsEmptyOrBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.authenticate("staff@nexus.com", ""));
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.authenticate("staff@nexus.com", "   "));
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.authenticate("staff@nexus.com", null));
    }

    @Test
    void authenticate_Fails_WhenUserDoesNotExist_ReturnsGenericError() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.authenticate("nonexistent@nexus.com", "Password123!"));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void authenticate_Fails_WhenUserHasNoPasswordHash() {
        User user = User.createStaff("Staff One", new Email("staff@nexus.com"),
                new IdentityDocument("CC", "12345"), UserRole.ADMINISTRATOR, null);

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.authenticate("staff@nexus.com", "Password123!"));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void authenticate_Fails_WhenUserIsInactive() {
        User user = User.createStaff("Staff One", new Email("staff@nexus.com"),
                new IdentityDocument("CC", "12345"), UserRole.ADMINISTRATOR, "hashed.secret");
        user.setStatus(UserStatus.BLOCKED);

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordServicePort.matches("Password123!", "hashed.secret")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () ->
                publicAccessUseCase.authenticate("staff@nexus.com", "Password123!"));
    }

    @Test
    void registerBuyer_Success() {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer One", new Email("buyer@nexus.com"), new IdentityDocument("CC", "12345"), address);

        when(userRepository.save(any(User.class))).thenReturn(buyer);

        Buyer saved = publicAccessUseCase.registerBuyer("Buyer One", "buyer@nexus.com", "CC", "12345", address);
        assertNotNull(saved);
        assertEquals("Buyer One", saved.getFullName());
    }

    @Test
    void authenticate_LocksAccount_AfterFiveFailedAttempts() {
        User user = User.createStaff("Staff Lock", new Email("lock@nexus.com"),
                new IdentityDocument("CC", "99999"), UserRole.ADMINISTRATOR, "hashed.secret");

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordServicePort.matches("BadPass1", "hashed.secret")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalArgumentException.class, () ->
                    publicAccessUseCase.authenticate("lock@nexus.com", "BadPass1"));
        }

        // 6th attempt even with valid password should throw BusinessRuleException due to temporary lockout
        BusinessRuleException lockEx = assertThrows(BusinessRuleException.class, () ->
                publicAccessUseCase.authenticate("lock@nexus.com", "Password123!"));
        org.junit.jupiter.api.Assertions.assertTrue(lockEx.getMessage().contains("bloqueada temporalmente"));
    }

    @Test
    void registerBuyerWithPassword_EnforcesPasswordPolicy() {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);

        // Weak passwords should fail policy validation
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.registerBuyer("Buyer Weak", "weak@nexus.com", "CC", "54321", address, "short1A"));
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.registerBuyer("Buyer Weak", "weak@nexus.com", "CC", "54321", address, "alllowercase123"));
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.registerBuyer("Buyer Weak", "weak@nexus.com", "CC", "54321", address, "NoDigitsPassword"));
    }

    @Test
    void passwordRecoveryAndReset_AntiEnumerationAndTokenConsumption() {
        User user = User.createStaff("Recover User", new Email("recover@nexus.com"),
                new IdentityDocument("CC", "77777"), UserRole.ADMINISTRATOR, "old.hash");

        when(userRepository.findByEmail(new Email("recover@nexus.com"))).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(new Email("unknown@nexus.com"))).thenReturn(Optional.empty());
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(passwordServicePort.encode("NewSecure2026!")).thenReturn("$2a$10$new.hash");

        // Anti-enumeration: neither existing nor non-existing accounts throw exceptions
        publicAccessUseCase.requestPasswordRecovery("recover@nexus.com");
        publicAccessUseCase.requestPasswordRecovery("unknown@nexus.com");

        String token = publicAccessUseCase.getLatestRecoveryTokenForEmail("recover@nexus.com");
        assertNotNull(token);
        org.junit.jupiter.api.Assertions.assertNull(publicAccessUseCase.getLatestRecoveryTokenForEmail("unknown@nexus.com"));

        publicAccessUseCase.resetPassword(token, "NewSecure2026!");
        assertEquals("$2a$10$new.hash", user.getPasswordHash());

        // Reusing same recovery token must fail
        assertThrows(IllegalArgumentException.class, () ->
                publicAccessUseCase.resetPassword(token, "AnotherPass2026!"));
    }
}
