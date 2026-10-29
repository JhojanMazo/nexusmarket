package application.domain.services.user;

import application.domain.exceptions.BusinessRuleException;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import java.util.Objects;

/**
 * 1.5 Validates uniqueness constraints for identity attributes.
 */
public class UniqueIdentityValidationService {

    public void validateUniqueIdentity(UserRepositoryPort userRepository, String emailStr, String docType, String docNumber) {
        validateUniqueEmail(userRepository, emailStr);
        validateUniqueDocument(userRepository, docType, docNumber);
    }

    public void validateUniqueEmail(UserRepositoryPort userRepository, String emailStr) {
        Objects.requireNonNull(userRepository, "UserRepositoryPort must not be null");
        Email email = new Email(emailStr);
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("El correo ya se encuentra registrado: " + emailStr);
        }
    }

    public void validateUniqueDocument(UserRepositoryPort userRepository, String docType, String docNumber) {
        Objects.requireNonNull(userRepository, "UserRepositoryPort must not be null");
        IdentityDocument doc = new IdentityDocument(docType, docNumber);
        if (userRepository.findByIdentityDocument(doc).isPresent()) {
            throw new BusinessRuleException("El documento de identidad ya se encuentra registrado: " + doc);
        }
    }
}
