package application.adapters.persistence.mongodb;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogMongoAdapterTest {

    @Mock
    private AuditLogMongoRepository repository;

    private AuditLogMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuditLogMongoAdapter(repository);
    }

    @Test
    void shouldSaveAuditLog() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditLog domain = new AuditLog(id, "USER_LOGIN", userId, "user@nexusmarket.com", "User", userId.toString(), AuditSeverity.LOW, "Login success", "127.0.0.1", Instant.now());
        AuditLogDocument doc = new AuditLogDocument(id.toString(), "USER_LOGIN", userId.toString(), "user@nexusmarket.com", "User", userId.toString(), "LOW", "Login success", "127.0.0.1", Instant.now());

        when(repository.save(any(AuditLogDocument.class))).thenReturn(doc);

        AuditLog saved = adapter.save(domain);
        assertNotNull(saved);
        assertEquals(id, saved.getId());
        assertEquals("USER_LOGIN", saved.getAction());
        verify(repository).save(any(AuditLogDocument.class));
    }

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        AuditLogDocument doc = new AuditLogDocument(id.toString(), "USER_LOGIN", UUID.randomUUID().toString(), "user@nexusmarket.com", "User", id.toString(), "LOW", "Login success", "127.0.0.1", Instant.now());

        when(repository.findById(id.toString())).thenReturn(Optional.of(doc));

        Optional<AuditLog> found = adapter.findById(id);
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
    }

    @Test
    void shouldFindByUserId() {
        UUID userId = UUID.randomUUID();
        AuditLogDocument doc = new AuditLogDocument(UUID.randomUUID().toString(), "USER_LOGIN", userId.toString(), "user@nexusmarket.com", "User", userId.toString(), "LOW", "Login success", "127.0.0.1", Instant.now());

        when(repository.findByUserId(userId.toString())).thenReturn(List.of(doc));

        List<AuditLog> list = adapter.findByUserId(userId);
        assertEquals(1, list.size());
    }
}
