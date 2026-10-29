package application.adapters.persistence.jpa;

import application.domain.models.ShoppingCart;
import application.domain.ports.out.ShoppingCartRepositoryPort;
import application.adapters.persistence.jpa.entities.ShoppingCartJpaEntity;
import application.adapters.persistence.jpa.mappers.CartJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaShoppingCartRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class ShoppingCartJpaAdapter implements ShoppingCartRepositoryPort {

    private final SpringDataJpaShoppingCartRepository jpaRepository;
    private final SpringDataJpaProductRepository productJpaRepository;
    private final CartJpaMapper mapper;

    public ShoppingCartJpaAdapter(SpringDataJpaShoppingCartRepository jpaRepository,
                                         SpringDataJpaProductRepository productJpaRepository,
                                         CartJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.productJpaRepository = productJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ShoppingCart save(ShoppingCart cart) {
        ShoppingCartJpaEntity entity = jpaRepository.findById(cart.getCartId())
                .map(existing -> mapper.updateEntity(existing, cart, productJpaRepository))
                .orElseGet(() -> mapper.toEntity(cart, productJpaRepository));
        ShoppingCartJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ShoppingCart> findById(UUID cartId) {
        return jpaRepository.findById(cartId).map(mapper::toDomain);
    }

    @Override
    public Optional<ShoppingCart> findByBuyerId(UUID buyerId) {
        return jpaRepository.findByBuyer_UserId(buyerId).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID cartId) {
        jpaRepository.deleteById(cartId);
    }
}
