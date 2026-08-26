package com.nexusmarket.domain.service;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.model.Seller;
import com.nexusmarket.domain.model.User;
import com.nexusmarket.domain.model.Warehouse;

/**
 * Domain service enforcing the rule that sellers cannot self-register: a
 * Seller is linked to its first Warehouse only through this service, acting
 * on behalf of a {@code User} who must hold the ADMINISTRATOR role.
 *
 * <p>This rule used to live as a method on a dedicated {@code Administrator}
 * entity, which gave a compile-time guarantee (only an Administrator instance
 * had the method). Since ADMINISTRATOR is now just a role on {@link User}
 * (see the design note on {@link User}) rather than its own type, the same
 * rule is enforced here with a runtime check instead. That trade-off is
 * intentional: keeping three extra subclasses only to preserve one
 * compile-time check — for roles that otherwise add no state or behavior —
 * was not worth the added hierarchy.
 */
public final class SellerOnboardingService {

    private SellerOnboardingService() {
    }

    public static void onboard(User actor, Seller seller, Warehouse firstWarehouse) {
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalStateException("Only a user with the ADMINISTRATOR role can onboard a new seller.");
        }
        seller.linkWarehouse(firstWarehouse);
    }
}
