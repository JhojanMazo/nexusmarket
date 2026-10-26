package application.domain.valueobjects;

import java.util.Objects;

/**
 * Immutable value object representing a postal address.
 * All fields are required except {@code reference} which may be null.
 * Validation: no field (except reference) may be blank or null.
 */
public final class Address {
    private final String street;
    private final String city;
    private final String stateOrProvince;
    private final String country;
    private final String postalCode;
    private final String reference; // optional

    public Address(String street, String city, String stateOrProvince, String country, String postalCode, String reference) {
        if (isBlank(street) || isBlank(city) || isBlank(stateOrProvince) || isBlank(country) || isBlank(postalCode)) {
            throw new IllegalArgumentException("Address fields must not be blank");
        }
        this.street = street;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.country = country;
        this.postalCode = postalCode;
        this.reference = reference; // may be null or blank
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public String street() { return street; }
    public String city() { return city; }
    public String stateOrProvince() { return stateOrProvince; }
    public String country() { return country; }
    public String postalCode() { return postalCode; }
    public String reference() { return reference; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address that = (Address) o;
        return street.equals(that.street) &&
                city.equals(that.city) &&
                stateOrProvince.equals(that.stateOrProvince) &&
                country.equals(that.country) &&
                postalCode.equals(that.postalCode) &&
                Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, stateOrProvince, country, postalCode, reference);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s %s%s",
                street, city, stateOrProvince, country, postalCode,
                reference != null && !reference.isBlank() ? " (" + reference + ")" : "");
    }
}
