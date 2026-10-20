package com.nexusmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the NexusMarket microservice.
 * The application bundles domain, infrastructure and API layers.
 */
@SpringBootApplication(scanBasePackages = {
        "com.nexusmarket.domain",
        "com.nexusmarket.application",
        "com.nexusmarket.infrastructure",
        "com.nexusmarket.web"
})
public class NexusMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexusMarketApplication.class, args);
    }
}
