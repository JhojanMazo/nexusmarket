package application.adapters.persistence.sql;
import application.adapters.persistence.jpa.*;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.adapters.persistence.jpa.entities.BuyerEntity;
import application.adapters.persistence.jpa.entities.UserEntity;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserJpaAdapterTest {

    @Mock
    private SpringDataJpaUserRepository jpaRepository;

    private UserJpaMapper mapper;
    private UserJpaAdapter adapter;

    private Buyer buyer;
    private BuyerEntity buyerEntity;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mapper = new UserJpaMapper();
        adapter = new UserJpaAdapter(jpaRepository, mapper);

        userId = UUID.randomUUID();
        buyer = Buyer.reconstitute(
                userId,
                "Santiago Morales",
                new Email("santiago@example.com"),
                new IdentityDocument("CC", "1020304050"),
                new Address("Calle 100", "Bogota", "Cundinamarca", "Colombia", "110111", null),
                Collections.emptyList(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );
        buyerEntity = (BuyerEntity) mapper.toEntity(buyer);
    }

    @Test
    void save_Buyer_Success() {
        when(jpaRepository.save(any(UserEntity.class))).thenReturn(buyerEntity);

        User saved = adapter.save(buyer);

        assertNotNull(saved);
        assertTrue(saved instanceof Buyer);
        assertEquals(userId, saved.getUserId());
        assertEquals("Santiago Morales", saved.getFullName());
        assertEquals(UserRole.BUYER, saved.getRole());
        verify(jpaRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(userId)).thenReturn(Optional.of(buyerEntity));

        Optional<User> found = adapter.findById(userId);

        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    void findByEmail_Success() {
        Email email = new Email("santiago@example.com");
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(buyerEntity));

        Optional<User> found = adapter.findByEmail(email);

        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    void findByIdentityDocument_Success() {
        IdentityDocument doc = new IdentityDocument("CC", "1020304050");
        when(jpaRepository.findByIdentityDocument(doc)).thenReturn(Optional.of(buyerEntity));

        Optional<User> found = adapter.findByIdentityDocument(doc);

        assertTrue(found.isPresent());
        assertEquals(doc, found.get().getIdentityDocument());
    }
}
