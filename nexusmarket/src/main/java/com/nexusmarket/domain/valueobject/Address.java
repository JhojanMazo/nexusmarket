package com.nexusmarket.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a physical address.
 * Immutable and compared by structural equality.
 * Used by: Buyer (addresses), Warehouse (location), Order and Shipment (delivery address).
 */
public final class Address {

    private final String street;
    private final String city;
    private final String stateOrProvince;
    private final String country;
    private final String postalCode;
    private final String reference; // optional

    public Address(String street, String city, String stateOrProvince,
                    String country, String postalCode, String reference) {
        this.street = requireNotBlank(street, "street");
        this.city = requireNotBlank(city, "city");
        this.stateOrProvince = requireNotBlank(stateOrProvince, "stateOrProvince");
        this.country = requireNotBlank(country, "country");
        this.postalCode = requireNotBlank(postalCode, "postalCode");
        this.reference = reference; // may be null
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' of Address cannot be blank.");
        }
        return value;
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getStateOrProvince() { return stateOrProvince; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public String getReference() { return reference; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address that = (Address) o;
        return street.equals(that.street)
                && city.equals(that.city)
                && stateOrProvince.equals(that.stateOrProvince)
                && country.equals(that.country)
                && postalCode.equals(that.postalCode)
                && Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, stateOrProvince, country, postalCode, reference);
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + stateOrProvince + ", " + country + " (" + postalCode + ")";
    }
}
