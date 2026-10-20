package com.nexusmarket.domain.valueobject;

import java.util.Objects;

/**
 * Immutable value object representing an identity document.
 * Both type and number must be non‑blank.
 */
public final class IdentityDocument {
    private final String type;
    private final String number;

    public IdentityDocument(String type, String number) {
        if (isBlank(type) || isBlank(number)) {
            throw new IllegalArgumentException("IdentityDocument fields must not be blank");
        }
        this.type = type.trim();
        this.number = number.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public String type() {
        return type;
    }

    public String number() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityDocument)) return false;
        IdentityDocument that = (IdentityDocument) o;
        return type.equals(that.type) && number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }

    @Override
    public String toString() {
        return type + ":" + number;
    }
}
