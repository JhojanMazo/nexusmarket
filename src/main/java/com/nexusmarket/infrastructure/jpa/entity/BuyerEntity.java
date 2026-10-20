package com.nexusmarket.infrastructure.jpa.entity;

import com.nexusmarket.domain.enums.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * JPA representation of a Buyer user.
 */
@Entity(name = "BuyerEntity")
@DiscriminatorValue("BUYER")
public class BuyerEntity extends UserEntity {
    public BuyerEntity() { super(); }
    public BuyerEntity(java.util.UUID userId, String fullName,
                       com.nexusmarket.domain.valueobject.Email email,
                       com.nexusmarket.domain.valueobject.IdentityDocument identityDocument,
                       com.nexusmarket.domain.enums.UserStatus status) {
        super(userId, fullName, email, identityDocument, UserRole.BUYER, status);
    }
}
