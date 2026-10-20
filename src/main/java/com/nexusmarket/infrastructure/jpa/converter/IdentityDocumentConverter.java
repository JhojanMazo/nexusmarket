package com.nexusmarket.infrastructure.jpa.converter;

import com.nexusmarket.domain.valueobject.IdentityDocument;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for {@link IdentityDocument}.
 * Persists the document's raw string value and reconstructs the value object on read.
 */
@Converter(autoApply = false)
public class IdentityDocumentConverter implements AttributeConverter<IdentityDocument, String> {

    @Override
    public String convertToDatabaseColumn(IdentityDocument attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public IdentityDocument convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Expect format "type:number"
        int idx = dbData.indexOf(":");
        if (idx < 0) {
            throw new IllegalArgumentException("Invalid IdentityDocument stored value: " + dbData);
        }
        String type = dbData.substring(0, idx);
        String number = dbData.substring(idx + 1);
        return new IdentityDocument(type, number);
    }
}
