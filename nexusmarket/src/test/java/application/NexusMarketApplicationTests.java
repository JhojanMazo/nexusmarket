package application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NexusMarketApplicationTests {

    @Test
    void applicationClassLoads() {
        // Verifies entrypoint class definition and package conventions
        assertTrue(NexusMarketApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}
