package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;

import java.util.Optional;

/**
 * Thread-safe holder for the authenticated user principal during HTTP request lifecycle.
 */
public final class SecurityUserContext {

    private static final ThreadLocal<AuthenticatedUserPrincipal> CURRENT_USER = new ThreadLocal<>();

    private SecurityUserContext() {
    }

    public static void set(AuthenticatedUserPrincipal user) {
        CURRENT_USER.set(user);
    }

    public static Optional<AuthenticatedUserPrincipal> get() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
