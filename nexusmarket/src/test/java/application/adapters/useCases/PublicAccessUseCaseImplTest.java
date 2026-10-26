package application.adapters.useCases;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
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
        User user = User.createStaff("Staff One", new Email("buyer@nexus.com"),
                new IdentityDocument("CC", "12345"), UserRole.ADMINISTRATOR);

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(jwtServicePort.generateToken(any(), any(), any())).thenReturn("mock.jwt.token");

        AuthenticationResult result = publicAccessUseCase.authenticate("buyer@nexus.com", "Password123!");
        assertNotNull(result);
        assertEquals("mock.jwt.token", result.token());
        assertEquals("buyer@nexus.com", result.email());
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
