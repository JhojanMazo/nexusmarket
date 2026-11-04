package application.adapters.useCases;

import application.adapters.aspect.Auditable;
import application.domain.enums.AuditSeverity;
import application.domain.enums.InventoryStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final Duration RECOVERY_TOKEN_TTL = Duration.ofMinutes(15);

    private record FailedLoginRecord(int attempts, Instant lockedUntil) {}
    private record PasswordRecoveryRecord(UUID userId, String email, Instant expiresAt) {}

    private final Map<String, FailedLoginRecord> failedLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, PasswordRecoveryRecord> recoveryTokens = new ConcurrentHashMap<>();
    private final Map<String, String> latestRecoveryTokenByEmail = new ConcurrentHashMap<>();

    private final UserRepositoryPort userRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final JwtServicePort jwtServicePort;
    private final PasswordServicePort passwordServicePort;
    private final UniqueIdentityValidationService uniqueIdentityValidationService;
    private final AuthorizeCustomerRegistrationService customerRegistrationService;

    @Autowired
    public PublicAccessUseCaseImpl(UserRepositoryPort userRepository,
                                  ProductRepositoryPort productRepository,
                                  InventoryRepositoryPort inventoryRepository,
                                  JwtServicePort jwtServicePort,
                                  PasswordServicePort passwordServicePort,
                                  UniqueIdentityValidationService uniqueIdentityValidationService,
                                  AuthorizeCustomerRegistrationService customerRegistrationService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.jwtServicePort = jwtServicePort;
        this.passwordServicePort = passwordServicePort;
        this.uniqueIdentityValidationService = uniqueIdentityValidationService;
        this.customerRegistrationService = customerRegistrationService;
    }

    public PublicAccessUseCaseImpl(UserRepositoryPort userRepository,
                                  ProductRepositoryPort productRepository,
                                  JwtServicePort jwtServicePort,
                                  PasswordServicePort passwordServicePort,
                                  UniqueIdentityValidationService uniqueIdentityValidationService,
                                  AuthorizeCustomerRegistrationService customerRegistrationService) {
        this(userRepository, productRepository, null, jwtServicePort, passwordServicePort,
                uniqueIdentityValidationService, customerRegistrationService);
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
    @Transactional(readOnly = true)
    @Auditable(action = "USER_LOGIN", resource = "User", severity = AuditSeverity.LOW)
    public AuthenticationResult authenticate(String emailStr, String password) {
        if (emailStr == null || emailStr.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        String normalizedEmailKey = emailStr.trim().toLowerCase(Locale.ROOT);
        checkBruteForceLockout(normalizedEmailKey);

        Email email;
        try {
            email = new Email(emailStr.trim());
        } catch (Exception e) {
            recordFailedLoginAttempt(normalizedEmailKey);
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getPasswordHash() == null) {
            recordFailedLoginAttempt(normalizedEmailKey);
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        boolean passwordMatches = passwordServicePort != null && passwordServicePort.matches(password, user.getPasswordHash());
        if (!passwordMatches) {
            recordFailedLoginAttempt(normalizedEmailKey);
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        failedLoginAttempts.remove(normalizedEmailKey);

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
    @Auditable(action = "USER_LOGOUT", resource = "User", severity = AuditSeverity.LOW)
    public void logout(String token) {
        if (jwtServicePort != null && token != null && !token.isBlank()) {
            jwtServicePort.revokeToken(token);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(action = "USER_TOKEN_REFRESH", resource = "User", severity = AuditSeverity.LOW)
    public AuthenticationResult refreshToken(String token) {
        if (token == null || token.isBlank() || jwtServicePort == null || !jwtServicePort.validateToken(token)) {
            throw new IllegalArgumentException("El token JWT es inválido o ha expirado");
        }
        UUID userId = jwtServicePort.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("La cuenta de usuario no se encuentra activa");
        }
        jwtServicePort.revokeToken(token);
        String newToken = jwtServicePort.generateToken(user.getUserId(), user.getEmail().value(), user.getRole().name());
        return new AuthenticationResult(
                user.getUserId(),
                user.getFullName(),
                user.getEmail().value(),
                user.getRole(),
                newToken
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(action = "PASSWORD_RECOVERY_REQUESTED", resource = "User", severity = AuditSeverity.MEDIUM)
    public void requestPasswordRecovery(String emailStr) {
        if (emailStr == null || emailStr.isBlank()) {
            return;
        }
        Email email;
        try {
            email = new Email(emailStr.trim());
        } catch (Exception e) {
            return;
        }
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == UserStatus.ACTIVE) {
                String recoveryToken = UUID.randomUUID().toString();
                recoveryTokens.put(recoveryToken, new PasswordRecoveryRecord(
                        user.getUserId(),
                        user.getEmail().value(),
                        Instant.now().plus(RECOVERY_TOKEN_TTL)
                ));
                latestRecoveryTokenByEmail.put(user.getEmail().value().toLowerCase(Locale.ROOT), recoveryToken);
            }
        });
    }

    public String getLatestRecoveryTokenForEmail(String email) {
        if (email == null) {
            return null;
        }
        return latestRecoveryTokenByEmail.get(email.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    @Transactional
    @Auditable(action = "PASSWORD_RESET_COMPLETED", resource = "User", severity = AuditSeverity.HIGH)
    public void resetPassword(String recoveryToken, String newPassword) {
        if (recoveryToken == null || recoveryToken.isBlank()) {
            throw new IllegalArgumentException("El token de recuperación es obligatorio");
        }
        validatePasswordPolicy(newPassword);

        PasswordRecoveryRecord record = recoveryTokens.remove(recoveryToken.trim());
        if (record == null || Instant.now().isAfter(record.expiresAt())) {
            throw new IllegalArgumentException("El token de recuperación es inválido o ha expirado");
        }
        latestRecoveryTokenByEmail.remove(record.email().toLowerCase(Locale.ROOT));

        User user = userRepository.findById(record.userId())
                .orElseThrow(() -> new IllegalArgumentException("El token de recuperación es inválido o ha expirado"));
        String encoded = passwordServicePort != null ? passwordServicePort.encode(newPassword) : newPassword;
        user.setPasswordHash(encoded);
        userRepository.save(user);
        failedLoginAttempts.remove(record.email().toLowerCase(Locale.ROOT));
    }

    @Override
    @Transactional
    public Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress) {
        String generatedCompliantPassword = "Nx" + UUID.randomUUID().toString().replace("-", "") + "9a!";
        return registerBuyer(fullName, email, docType, docNumber, primaryAddress, generatedCompliantPassword);
    }

    @Override
    @Transactional
    public Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress, String rawPassword) {
        customerRegistrationService.validateRegistrationAllowed(email);
        uniqueIdentityValidationService.validateUniqueIdentity(userRepository, email, docType, docNumber);

        String effectivePassword;
        if (rawPassword != null && !rawPassword.isBlank()) {
            validatePasswordPolicy(rawPassword);
            effectivePassword = rawPassword;
        } else {
            effectivePassword = "Nx" + UUID.randomUUID().toString().replace("-", "") + "9a!";
        }
        String passwordHash = passwordServicePort != null ? passwordServicePort.encode(effectivePassword) : effectivePassword;

        Buyer buyer = Buyer.create(
                fullName,
                new Email(email),
                new IdentityDocument(docType, docNumber),
                primaryAddress,
                passwordHash
        );
        User saved = userRepository.save(buyer);
        return (Buyer) saved;
    }

    private void checkBruteForceLockout(String normalizedEmailKey) {
        FailedLoginRecord record = failedLoginAttempts.get(normalizedEmailKey);
        if (record != null && record.lockedUntil() != null) {
            if (Instant.now().isBefore(record.lockedUntil())) {
                throw new BusinessRuleException("Cuenta bloqueada temporalmente por múltiples intentos fallidos de inicio de sesión. Intente nuevamente más tarde.");
            } else {
                failedLoginAttempts.remove(normalizedEmailKey);
            }
        }
    }

    private void recordFailedLoginAttempt(String normalizedEmailKey) {
        failedLoginAttempts.compute(normalizedEmailKey, (key, existing) -> {
            int nextAttempts = (existing == null || (existing.lockedUntil() != null && Instant.now().isAfter(existing.lockedUntil())))
                    ? 1
                    : existing.attempts() + 1;
            Instant lockedUntil = nextAttempts >= MAX_FAILED_ATTEMPTS
                    ? Instant.now().plus(LOCKOUT_DURATION)
                    : null;
            return new FailedLoginRecord(nextAttempts, lockedUntil);
        });
    }

    public static void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra mayúscula, una minúscula y un número");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getPublishedCatalog() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products_catalog", key = "#page + '-' + #size")
    public PageResult<Product> getPublishedCatalog(int page, int size) {
        return productRepository.findByStatus(ProductStatus.PUBLISHED, page, size, "name", "ASC");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products_catalog", key = "#page + '-' + #size + '-' + #sortBy + '-' + #direction")
    public PageResult<Product> getPublishedCatalog(int page, int size, String sortBy, String direction) {
        return productRepository.findByStatus(ProductStatus.PUBLISHED, page, size, sortBy, direction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> searchPublishedCatalog(String search, String type, BigDecimal minPrice, BigDecimal maxPrice,
                                                      int page, int size, String sortBy, String direction) {
        String normalizedSearch = search != null ? search.trim().toLowerCase(Locale.ROOT) : "";
        String normalizedType = type != null ? type.trim().toUpperCase(Locale.ROOT) : "";

        Comparator<Product> comparator = "price".equalsIgnoreCase(sortBy)
                ? Comparator.comparing(p -> p.getPrice().amount())
                : Comparator.comparing(p -> p.getName().toLowerCase(Locale.ROOT));
        if ("DESC".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        List<Product> filtered = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                .filter(p -> normalizedSearch.isEmpty()
                        || p.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || (p.getVariant() != null && p.getVariant().attribute().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                        || (p.getVariant() != null && p.getVariant().value().toLowerCase(Locale.ROOT).contains(normalizedSearch)))
                .filter(p -> normalizedType.isEmpty() || p.getType().name().equalsIgnoreCase(normalizedType))
                .filter(p -> minPrice == null || p.getPrice().amount().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().amount().compareTo(maxPrice) <= 0)
                .sorted(comparator)
                .toList();

        return PageResult.of(filtered, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getPublishedProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BusinessRuleException("El producto no está publicado");
        }
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStockForProduct(UUID productId) {
        Product product = getPublishedProductById(productId);
        if (product.getType() == ProductType.DIGITAL) {
            return 999;
        }
        if (inventoryRepository == null) {
            return 0;
        }
        return inventoryRepository.findByProductId(productId).stream()
                .filter(inv -> inv.getStatus() == InventoryStatus.AVAILABLE)
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
    }
}
