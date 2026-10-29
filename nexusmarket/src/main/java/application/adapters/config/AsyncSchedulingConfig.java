package application.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;

/**
 * Enables asynchronous domain event processing ({@code @EnableAsync}) and
 * scheduled background tasks ({@code @EnableScheduling}) such as expired order TTL cleanup.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("nexusmarket-async-");
        return executor;
    }
}
