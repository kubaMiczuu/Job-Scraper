package pl.jobscraper.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Main Spring Boot application class.
 * <p>
 * Entry point for Job Scraper Data Layer backend.
 *
 * <p><strong>Features enabled:</strong>
 * <ul>
 *   <li>@SpringBootApplication: auto-configuration, component scanning</li>
 *   <li>@EnableScheduling: scheduled tasks (TTL cleanup)</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class JobScraperDataLayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobScraperDataLayerApplication.class, args);
    }

}
