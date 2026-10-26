package application.adapters.useCases;

import application.domain.enums.ProductStatus;
import application.domain.enums.UserStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final UserRepositoryPort userRepository;
    private final ProductRepositoryPort productRepository;
    private final JwtServicePort jwtServicePort;
    private final PasswordServicePort passwordServicePort;
    private final UniqueIdentityValidationService uniqueIdentityValidationService;
    private final AuthorizeCustomerRegistrationService customerRegistrationService;

    @Autowired
    public PublicAccessUseCaseImpl(UserRepositoryPort userRepository,
                                  ProductRepositoryPort productRepository,
                                  JwtServicePort jwtServicePort,
                                  PasswordServicePort passwordServicePort,
                                  UniqueIdentityValidationService uniqueIdentityValidationService,
                                  AuthorizeCustomerRegistrationService customerRegistrationService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.jwtServicePort = jwtServicePort;
        this.passwordServicePort = passwordServicePort;
        this.uniqueIdentityValidationService = uniqueIdentityValidationService;
        this.customerRegistrationService = customerRegistrationService;
    }

    public PublicAccessUseCaseImpl(UserRepositoryPort userRepository,
                                  JwtServicePort jwtServicePort,
                                  PasswordServicePort passwordServicePort,
                                  UniqueIdentityValidationService uniqueIdentityValidationService,
                                  AuthorizeCustomerRegistrationService customerRegistrationService) {
        this(userRepository, null, jwtServicePort, passwordServicePort,
                uniqueIdentityValidationService, customerRegistrationService);
    }

    @Override
    public AuthenticationResult authenticate(String emailStr, String password) {
        if (emailStr == null || emailStr.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        Email email = new Email(emailStr);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas: correo no registrado"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("La cuenta de usuario no se encuentra activa (Estado: " + user.getStatus() + ")");
        }

        String token = jwtServicePort.generateToken(user.getUserId(), user.getEmail().value(), user.getRole().name());

        return new AuthenticationResult(
                user.getUserId(),
                user.getFullName(),
                user.getEmail().value(),
                user.getRole(),
                token
        );
    }

    @Override
    public void logout(String token) {
        // Token invalidated or logged for session tracking
    }

    @Override
    public Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress) {
        customerRegistrationService.validateRegistrationAllowed(email);
        uniqueIdentityValidationService.validateUniqueIdentity(userRepository, email, docType, docNumber);

        Buyer buyer = Buyer.create(
                fullName,
                new Email(email),
                new IdentityDocument(docType, docNumber),
                primaryAddress
        );
        User saved = userRepository.save(buyer);
        return (Buyer) saved;
    }

    @Override
    public List<Product> getPublishedCatalog() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                .toList();
    }

    @Override
    public Product getPublishedProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BusinessRuleException("El producto no está publicado");
        }
        return product;
    }
}
