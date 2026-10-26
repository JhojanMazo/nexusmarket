package application.adapters.security.mappers;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.adapters.security.dtos.JwtClaimsDTO;

/**
 * Mapper between JWT claims and authenticated principal records.
 */
public final class JwtClaimsMapper {

    private JwtClaimsMapper() {
    }

    public static AuthenticatedUserPrincipal toPrincipal(JwtClaimsDTO claims) {
        if (claims == null) {
            return null;
        }
        return new AuthenticatedUserPrincipal(claims.userId(), claims.email(), claims.role());
    }
}
