package com.nexusmarket.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable email value object.
 * Stores the email in lower‑case and validates a basic email pattern.
 */
public final class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private final String value;

    public Email(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        String trimmed = value.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + trimmed);
        }
        this.value = trimmed.toLowerCase();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
