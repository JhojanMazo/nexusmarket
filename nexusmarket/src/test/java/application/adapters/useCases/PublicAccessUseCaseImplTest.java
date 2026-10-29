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
}
