package com.nexusmarket.domain.valueobject;

import java.util.Objects;

/**
 * Value object: a person's identity document.
 * Uniqueness across the platform is the responsibility of the
 * application/repository layer.
 */
public final class IdentityDocument {

    private final String type;   // e.g. "NATIONAL_ID", "PASSPORT", "TAX_ID"
    private final String number;

    public IdentityDocument(String type, String number) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("The document type is required.");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("The document number is required.");
        }
        this.type = type;
        this.number = number;
    }

    public String getType() { return type; }
    public String getNumber() { return number; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityDocument)) return false;
        IdentityDocument that = (IdentityDocument) o;
        return type.equals(that.type) && number.equals(that.number);
    }

    @Override
    public int hashCode() { return Objects.hash(type, number); }

    @Override
    public String toString() { return type + " " + number; }
}
