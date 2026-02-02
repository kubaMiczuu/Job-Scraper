package pl.jobscraper.core.infrastructure.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.jobscraper.core.domain.identity.DefaultJobIdentityCalculator;
import pl.jobscraper.core.domain.identity.HashingService;
import pl.jobscraper.core.domain.identity.JobIdentityCalculator;
import pl.jobscraper.core.infrastructure.hashing.Sha256HashingService;

/**
 * Spring Configuration for Domain beans.
 *
 * Registers domain services (identity calculation, hashing ) as Spring beans
 * so they can be dependency-injected into application services.
 */

@Configuration
public class DomainConfig {

    /**
     * Creates HashingService bean (SHA-256 implementation).
     */
    @Bean
    public HashingService hashingService() {
        return new Sha256HashingService();
    }

    /**
     * Creates JobIdentityCalculator bean.
     *
     * @param hashingService injected by Spring (from hashingService() bean above)
     */
    @Bean
    public JobIdentityCalculator jobIdentityCalculator(HashingService hashingService) {
        return new DefaultJobIdentityCalculator(hashingService);
    }
}