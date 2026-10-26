package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingCartJpaEntity {

    @Id
    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id", nullable = false, unique = true)
    private BuyerEntity buyer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItemJpaEntity> items = new ArrayList<>();

    public ShoppingCartJpaEntity(UUID cartId, BuyerEntity buyer) {
        this.cartId = cartId;
        this.buyer = buyer;
    }

    public void addItem(CartItemJpaEntity item) {
        items.add(item);
        item.setCart(this);
    }
}
