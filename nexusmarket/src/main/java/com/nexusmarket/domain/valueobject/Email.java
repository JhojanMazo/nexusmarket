package com.nexusmarket.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object: a validated email address.
 * Uniqueness across the platform is the responsibility of the
 * application/repository layer, not of this value object.
 */
public final class Email {

    private static final Pattern VALID_FORMAT =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final String value;

    public Email(String value) {
        if (value == null || !VALID_FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        this.value = value.toLowerCase();
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        return value.equals(((Email) o).value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
