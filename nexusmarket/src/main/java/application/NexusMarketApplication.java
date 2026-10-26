package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the NexusMarket enterprise microservice.
 * Strict Hexagonal Architecture: domain (core), adapters (inbound/outbound), infrastructure (runtime/seed).
 */
@SpringBootApplication(scanBasePackages = {
        "application.domain",
        "application.adapters",
        "application.infrastructure"
})
public class NexusMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexusMarketApplication.class, args);
    }
}
