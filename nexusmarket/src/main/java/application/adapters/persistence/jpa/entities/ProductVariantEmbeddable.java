package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantEmbeddable {
    @Column(name = "variant_attribute", length = 100)
    private String attribute;

    @Column(name = "variant_value", length = 100)
    private String value;

    @Column(name = "sku", length = 100)
    private String sku;
}
