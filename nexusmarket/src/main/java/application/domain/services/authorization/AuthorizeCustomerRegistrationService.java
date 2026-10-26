package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizeCustomerRegistrationService {

    public void validateRegistrationAllowed(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (email.endsWith("@blocked-domain.com")) {
            throw new UnauthorizedOperationException("Customer registration is blocked for email domain: " + email);
        }
    }
}
