package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniqueIdentityValidationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private UniqueIdentityValidationService service;

    @BeforeEach
    void setUp() {
        service = new UniqueIdentityValidationService();
    }

    @Test
    void shouldPassWhenIdentityIsUnique() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
        when(userRepository.findByIdentityDocument(any(IdentityDocument.class))).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                service.validateUniqueIdentity(userRepository, "unique@nexusmarket.com", "CC", "99887766")
        );
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        User existing = User.createStaff("Existing", new Email("taken@nexusmarket.com"), new IdentityDocument("CC", "1111"), UserRole.ADMINISTRATOR);
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existing));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                service.validateUniqueIdentity(userRepository, "taken@nexusmarket.com", "CC", "99887766")
        );
        assertTrue(ex.getMessage().contains("El correo ya se encuentra registrado"));
    }

    @Test
    void shouldThrowWhenDocumentAlreadyExists() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
        User existing = User.createStaff("Existing", new Email("other@nexusmarket.com"), new IdentityDocument("CC", "99887766"), UserRole.ADMINISTRATOR);
        when(userRepository.findByIdentityDocument(any(IdentityDocument.class))).thenReturn(Optional.of(existing));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                service.validateUniqueIdentity(userRepository, "unique@nexusmarket.com", "CC", "99887766")
        );
        assertTrue(ex.getMessage().contains("El documento de identidad ya se encuentra registrado"));
    }
}
